package agent.reporting

import agent.model.MobileTesterConfig
import agent.tool.mobile.test.utils.AdbUtils
import io.github.cdimascio.dotenv.dotenv
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Serializable
data class ReportEvent(
    val index: Int,
    val timestamp: String,
    val type: String, // "tool" | "screenshot" | "log"
    val toolName: String? = null,
    val args: String? = null,
    val result: String? = null,
    val resultPrefix: String? = null,
    val screenshotPath: String? = null,
    val message: String? = null
)

@Serializable
data class ReportManifest(
    val startedAt: String,
    var endedAt: String? = null,
    val scenario: String,
    val packageName: String,
    val logsEnabled: Boolean,
    val screenshotsEnabled: Boolean,
    val recordingEnabled: Boolean,
    var status: String = "running",
    val videoChunks: MutableList<String> = mutableListOf(),
    val events: MutableList<ReportEvent> = mutableListOf()
)

object ReportRecorder {
    private val dotenv = dotenv()
    private val homePath: String = dotenv["HOME_PATH"]
        ?: error("HOME_PATH is not set")

    private val json = Json { prettyPrint = true; encodeDefaults = true }
    private val tsFmt = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault())
    private val isoFmt = DateTimeFormatter.ISO_INSTANT

    val rootDir: File get() = File("$homePath/reports/latest")
    private val manifestFile get() = File(rootDir, "manifest.json")
    private val logFile get() = File(rootDir, "log.txt")
    private val screenshotsDir get() = File(rootDir, "screenshots")
    private val videoDir get() = File(rootDir, "video")

    @Volatile private var manifest: ReportManifest? = null
    @Volatile private var eventCounter: Int = 0
    @Volatile private var screenshotCounter: Int = 0
    @Volatile private var videoChunker: VideoChunker? = null

    @Synchronized
    fun startRun(scenario: String, packageName: String, config: MobileTesterConfig) {
        if (rootDir.exists()) rootDir.deleteRecursively()
        rootDir.mkdirs()
        if (config.screenshotsEnabled) screenshotsDir.mkdirs()
        if (config.recordingEnabled) videoDir.mkdirs()

        eventCounter = 0
        screenshotCounter = 0
        manifest = ReportManifest(
            startedAt = isoFmt.format(Instant.now()),
            scenario = scenario,
            packageName = packageName,
            logsEnabled = config.logsEnabled,
            screenshotsEnabled = config.screenshotsEnabled,
            recordingEnabled = config.recordingEnabled
        )
        flush()

        if (config.logsEnabled) {
            logFile.writeText("[${tsFmt.format(Instant.now())}] Run started — scenario=$scenario package=$packageName\n")
        }

        if (config.recordingEnabled) {
            videoChunker = VideoChunker(videoDir) { chunk ->
                synchronized(this) {
                    manifest?.videoChunks?.add(chunk)
                    flush()
                }
            }.also { it.start() }
        }
    }

    @Synchronized
    fun logToolCall(toolName: String, args: String, result: String?) {
        val m = manifest ?: return
        if (!m.logsEnabled) return
        val now = Instant.now()
        val prefix = result?.substringBefore(' ')?.trimEnd(':', ',')
        val event = ReportEvent(
            index = ++eventCounter,
            timestamp = isoFmt.format(now),
            type = "tool",
            toolName = toolName,
            args = args,
            result = result,
            resultPrefix = prefix
        )
        m.events.add(event)
        logFile.appendText(
            "[${tsFmt.format(now)}] Tool called: tool $toolName, args $args" +
                (result?.let { " -> ${it.take(200)}" } ?: "") + "\n"
        )
        flush()
    }

    @Synchronized
    fun captureScreenshot(label: String) {
        val m = manifest ?: return
        if (!m.screenshotsEnabled) return
        screenshotCounter++
        val padded = screenshotCounter.toString().padStart(4, '0')
        val safeLabel = label.replace(Regex("[^A-Za-z0-9_-]"), "_").take(40)
        val remote = "/sdcard/report-screen-$padded.png"
        val capResult = AdbUtils.runAdb("shell", "screencap", "-p", remote)
        if (capResult.contains("Error", ignoreCase = true)) return
        val localName = "screenshots/$padded-$safeLabel.png"
        val localFile = File(rootDir, localName)
        val pullResult = AdbUtils.runAdb("pull", remote, localFile.absolutePath)
        if (pullResult.contains("Error", ignoreCase = true) || !localFile.exists()) return

        m.events.add(
            ReportEvent(
                index = ++eventCounter,
                timestamp = isoFmt.format(Instant.now()),
                type = "screenshot",
                screenshotPath = localName
            )
        )
        flush()
    }

    @Synchronized
    fun endRun(status: String) {
        videoChunker?.stop()
        videoChunker = null
        val m = manifest ?: return
        m.status = status
        m.endedAt = isoFmt.format(Instant.now())
        if (m.logsEnabled) {
            logFile.appendText("[${tsFmt.format(Instant.now())}] Run ended — status=$status\n")
        }
        flush()
    }

    private fun flush() {
        val m = manifest ?: return
        manifestFile.writeText(json.encodeToString(m))
    }
}
