package agent.reporting

import agent.tool.mobile.test.utils.AdbUtils
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Records screen video in ~3-minute chunks via `adb shell screenrecord`.
 * Each chunk is pulled to [videoDir] as `chunk-NNN.mp4` and [onChunkPulled] is called.
 *
 * `screenrecord` has a 180s built-in cap; we let it self-terminate then immediately start the next.
 */
class VideoChunker(
    private val videoDir: File,
    private val onChunkPulled: (relativePath: String) -> Unit
) {
    private val running = AtomicBoolean(false)
    private var workerThread: Thread? = null
    @Volatile private var currentProcess: Process? = null
    @Volatile private var currentRemotePath: String? = null
    @Volatile private var currentChunkIndex: Int = 0

    fun start() {
        if (!running.compareAndSet(false, true)) return
        videoDir.mkdirs()
        workerThread = thread(name = "video-chunker", isDaemon = true) { loop() }
    }

    fun stop() {
        if (!running.compareAndSet(true, false)) return
        // Kill the active screenrecord on device so the file is finalized.
        try {
            AdbUtils.runAdb("shell", "pkill", "-SIGINT", "screenrecord")
        } catch (_: Exception) { /* best-effort */ }
        currentProcess?.destroy()
        workerThread?.join(5_000)
        workerThread = null
    }

    private fun loop() {
        while (running.get()) {
            currentChunkIndex++
            val padded = currentChunkIndex.toString().padStart(3, '0')
            val remote = "/sdcard/report-chunk-$padded.mp4"
            currentRemotePath = remote
            // screenrecord has its own 180s cap; rely on that.
            val cmd = buildList {
                add("adb")
                AdbUtils.targetSerial?.let { add("-s"); add(it) }
                add("shell"); add("screenrecord"); add(remote)
            }
            val proc = try {
                ProcessBuilder(cmd).redirectErrorStream(true).start()
            } catch (e: Exception) {
                running.set(false)
                return
            }
            currentProcess = proc
            try {
                proc.waitFor()
            } catch (_: InterruptedException) {
                // External interruption; fall through and try to pull whatever was written.
            }
            currentProcess = null
            // Give the device a moment to finalize the file.
            try { Thread.sleep(800) } catch (_: InterruptedException) {}
            val localName = "chunk-$padded.mp4"
            val localFile = File(videoDir, localName)
            val pullResult = AdbUtils.runAdb("pull", remote, localFile.absolutePath)
            AdbUtils.runAdb("shell", "rm", "-f", remote)
            if (!pullResult.contains("Error", ignoreCase = true) && localFile.exists() && localFile.length() > 0) {
                onChunkPulled("video/$localName")
            }
        }
    }
}
