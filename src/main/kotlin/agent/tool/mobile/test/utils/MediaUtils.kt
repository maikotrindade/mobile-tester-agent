package agent.tool.mobile.test.utils

import agent.tool.mobile.test.utils.Formatter.formatToSlug
import io.github.cdimascio.dotenv.dotenv

object MediaUtils {
    val dotenv = dotenv()
    private var isRecording: Boolean = false
    private var recordingProcess: Process? = null

    private val homePath = dotenv["HOME_PATH"] ?: IllegalStateException("Home path is not set")
    private const val remoteRecordingPath = "/sdcard/video.mp4"
    private val localRecordingPath = "$homePath/video.mp4"

    private var screenshotIndex = 0

    fun takeScreenshot(goalName: String): String {
        screenshotIndex++
        val remoteScreenshotPath = "/sdcard/screen-$screenshotIndex.png"
        val screencapResult = AdbUtils.runAdb("shell", "screencap", "-p", remoteScreenshotPath)
        if (screencapResult.contains("Error")) return "Failed to take screenshot: $screencapResult"

        val screenshotName = "$homePath/${goalName.formatToSlug()}-$screenshotIndex.png"
        val pullResult = AdbUtils.runAdb("pull", remoteScreenshotPath, screenshotName)
        return if (pullResult.contains("Error")) "Failed to pull screenshot: $pullResult" else screenshotName
    }

    fun startScreenRecording(): String {
        return try {
            if (isRecording) return "Screen recording is already in progress."
            val process = ProcessBuilder("adb", "shell", "screenrecord", remoteRecordingPath)
                .redirectErrorStream(true)
                .start()
            recordingProcess = process
            isRecording = true
            "Screen recording started. Call stopScreenRecording to finish and save the video."
        } catch (e: Exception) {
            isRecording = false
            "Failed to start screen recording: ${e.message}"
        }
    }

    fun stopScreenRecording(): String {
        return try {
            if (!isRecording || recordingProcess == null) return "No screen recording in progress."
            recordingProcess?.destroy()
            recordingProcess = null
            isRecording = false
            Thread.sleep(1000)
            val pullResult = AdbUtils.runAdb("pull", remoteRecordingPath, localRecordingPath)
            if (pullResult.contains("Error")) "Failed to pull video: $pullResult" else localRecordingPath
        } catch (e: Exception) {
            isRecording = false
            "Failed to stop screen recording: ${e.message}"
        }
    }
}