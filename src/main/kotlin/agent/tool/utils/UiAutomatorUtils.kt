package agent.tool.utils

object UiAutomatorUtils {
    /**
     * Dumps the current UI hierarchy using UIAutomator and returns the XML as a String.
     * Returns an error message if the dump or read fails.
     */
    fun dumpUiHierarchy(): String {
        val dumpResult = AdbUtils.runAdb("shell", "uiautomator", "dump")
        if (dumpResult.contains("Error")) {
            return "Failed to dump UI hierarchy: $dumpResult"
        }
        val xmlPath = "/sdcard/window_dump.xml"
        val xml = AdbUtils.runAdb("shell", "cat", xmlPath)
        if (xml.isBlank() || xml.contains("Error")) return "Failed to read UI hierarchy XML."
        return xml
    }

    /**
     * Taps on a UI element by its text using UIAutomator dump and adb input tap.
     * Returns a success message or an error if the element is not found or the tap fails.
     */
    fun tapByText(text: String): String {
        val xml = dumpUiHierarchy()
        if (xml.startsWith("Failed")) return xml
        // Find node with text=text
        val regex = Regex(
            "<node[^>]*text=\\\"([^\"]*${Regex.escape(text)}[^\"]*)\\\"[^>]*bounds=\\\"\\[(\\d+),(\\d+)\\]\\[(\\d+),(\\d+)\\]\\\"",
            RegexOption.IGNORE_CASE
        )
        val match = regex.find(xml)
        if (match == null) {
            return "Element matching text '$text' not found."
        }
        val (_, x1, y1, x2, y2) = match.destructured
        val centerX = (x1.toInt() + x2.toInt()) / 2
        val centerY = (y1.toInt() + y2.toInt()) / 2
        val tapResult = AdbUtils.runAdb("shell", "input", "tap", centerX.toString(), centerY.toString())
        return if (tapResult.isBlank() || tapResult == "\n") {
            "Tapped element with text '$text' at ($centerX, $centerY)."
        } else if (tapResult.contains("Error")) {
            "Tap command failed: $tapResult"
        } else {
            "Tap command output: $tapResult"
        }
    }
}
