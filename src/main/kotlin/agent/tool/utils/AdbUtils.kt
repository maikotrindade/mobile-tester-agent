package agent.tool.utils

object AdbUtils {
    fun runAdb(vararg args: String): String {
        return try {
            val process = ProcessBuilder("adb", *args)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            output.trim()
        } catch (e: Exception) {
            "Error running adb ${args.joinToString(" ")}: ${e.message}"
        }
    }

    fun getDevices(): Pair<String, List<String>> {
        val process = ProcessBuilder("adb", "devices").redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        val devices = output.lines()
            .drop(1)
            .filter { it.isNotBlank() && !it.contains("List of devices attached") }
        return output to devices
    }
}