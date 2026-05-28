package agent.reporting

import agent.tool.mobile.test.utils.AdbUtils
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Records the device screen via `adb shell screenrecord` and pulls a single
 * playable MP4 to [videoDir] when [stop] is called. [onChunkPulled] is fired
 * once with the relative path of the produced file.
 *
 * Implementation notes:
 * - screenrecord must receive SIGINT on the *device* side to finalize the MP4
 *   moov atom. Killing the local `adb shell` process leaves an unplayable file.
 * - We pass `--time-limit 1800` (max accepted by most Android versions). The
 *   recording self-terminates after 30 minutes if [stop] is never called.
 * - After signaling, we wait for the local adb process to exit naturally, then
 *   poll the remote file size until it stops growing before pulling.
 */
class VideoChunker(
    private val videoDir: File,
    private val onChunkPulled: (relativePath: String) -> Unit
) {
    private val running = AtomicBoolean(false)
    private var workerThread: Thread? = null
    @Volatile private var recordProcess: Process? = null

    private val remotePath = "/sdcard/report-video.mp4"
    private val localName = "recording.mp4"

    fun start() {
        if (!running.compareAndSet(false, true)) return
        videoDir.mkdirs()
        AdbUtils.runAdb("shell", "rm", "-f", remotePath)
        val cmd = buildList {
            add("adb")
            AdbUtils.targetSerial?.let { add("-s"); add(it) }
            add("shell"); add("screenrecord"); add("--time-limit"); add("1800"); add(remotePath)
        }
        recordProcess = try {
            ProcessBuilder(cmd).redirectErrorStream(true).start()
        } catch (e: Exception) {
            running.set(false)
            null
        }
        workerThread = thread(name = "video-recorder", isDaemon = true) {
            // Drain stdout so the process doesn't block on a full pipe.
            try { recordProcess?.inputStream?.bufferedReader()?.forEachLine { } } catch (_: Exception) {}
        }
    }

    fun stop() {
        if (!running.compareAndSet(true, false)) return
        val proc = recordProcess ?: return

        // Signal device-side screenrecord so it flushes the moov atom.
        try { AdbUtils.runAdb("shell", "pkill", "-SIGINT", "screenrecord") } catch (_: Exception) {}

        // Wait for local adb shell to exit cleanly after device-side finishes.
        if (!proc.waitFor(10, TimeUnit.SECONDS)) {
            proc.destroy()
            proc.waitFor(2, TimeUnit.SECONDS)
        }
        workerThread?.join(2_000)
        workerThread = null
        recordProcess = null

        // Wait for the remote file size to stabilize (device flush).
        waitForRemoteStable()

        val localFile = File(videoDir, localName)
        val pullResult = AdbUtils.runAdb("pull", remotePath, localFile.absolutePath)
        AdbUtils.runAdb("shell", "rm", "-f", remotePath)

        if (!pullResult.contains("Error", ignoreCase = true) &&
            localFile.exists() && localFile.length() > 0
        ) {
            onChunkPulled("video/$localName")
        }
    }

    private fun waitForRemoteStable() {
        var lastSize = -1L
        repeat(10) {
            val sizeStr = AdbUtils.runAdb("shell", "stat", "-c", "%s", remotePath).trim()
            val size = sizeStr.toLongOrNull() ?: return@repeat
            if (size > 0 && size == lastSize) return
            lastSize = size
            try { Thread.sleep(300) } catch (_: InterruptedException) { return }
        }
    }
}
