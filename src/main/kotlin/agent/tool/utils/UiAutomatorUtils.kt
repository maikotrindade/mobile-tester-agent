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

    fun findUiElementsByText(text: String): MatchResult {
        val xml = dumpUiHierarchy()
        if (xml.startsWith("Failed")) {
            throw IllegalStateException("UI hierarchy dump failed: $xml")
        }
        val regex = Regex(
            "<node[^>]*text=\\\"([^\"]*${Regex.escape(text)}[^\"]*)\\\"[^>]*bounds=\\\"\\[(\\d+),(\\d+)\\]\\[(\\d+),(\\d+)\\]\\\"",
            RegexOption.IGNORE_CASE
        )
        val match = regex.find(xml)
        if (match == null) {
            throw NoSuchElementException("No UI element found with text: '$text'")
        }
        return match
    }

    /**
     * Taps on a UI element by its text using UIAutomator dump and adb input tap.
     * Returns a success message or an error if the element is not found or the tap fails.
     */
    fun tapByText(match: MatchResult): String {
        val (_, x1, y1, x2, y2) = match.destructured
        val centerX = (x1.toInt() + x2.toInt()) / 2
        val centerY = (y1.toInt() + y2.toInt()) / 2
        val tapResult = AdbUtils.runAdb("shell", "input", "tap", centerX.toString(), centerY.toString())
        return if (tapResult.isBlank() || tapResult == "\n") {
            "Tapped element with at ($centerX, $centerY)."
        } else if (tapResult.contains("Error")) {
            "Tap command failed: $tapResult"
        } else {
            "Tap command output: $tapResult"
        }
    }
}
