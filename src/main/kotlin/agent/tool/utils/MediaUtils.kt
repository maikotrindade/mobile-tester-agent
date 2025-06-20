package agent.tool.utils

object MediaUtils {
    private var isRecording: Boolean = false
    private var recordingProcess: Process? = null

    // TODO make these paths configurable
    private val remoteRecordingPath = "/sdcard/screenrecord.mp4"
    private val localRecordingPath = "/home/maiko/screenrecord.mp4"
    private val remotePath: String = "/sdcard/screen.png"
    private val localPath: String = "/home/maiko/screen.png"

    fun takeScreenshot(): String {
        val screencapResult = AdbUtils.runAdb("shell", "screencap", "-p", remotePath)
        if (screencapResult.contains("Error")) return "Failed to take screenshot: $screencapResult"
        val pullResult = AdbUtils.runAdb("pull", remotePath, localPath)
        return if (pullResult.contains("Error")) "Failed to pull screenshot: $pullResult" else localPath
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

