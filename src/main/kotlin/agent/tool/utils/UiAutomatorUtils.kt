package agent.tool.utils

object UiAutomatorUtils {
    /**
     * Dumps the current UI hierarchy using UIAutomator and returns the XML as a String.
     * Returns an error message if the dump or read fails.
     */
    private fun dumpUiHierarchy(): String {
        val dumpResult = AdbUtils.runAdb("shell", "uiautomator", "dump")
        if (dumpResult.contains("Error")) {
            return "Failed to dump UI hierarchy: $dumpResult"
        }
        val xmlPath = "/sdcard/window_dump.xml"
        val xml = AdbUtils.runAdb("shell", "cat", xmlPath)
        if (xml.isBlank() || xml.contains("Error") || xml.startsWith("Failed"))
            return "Failed to read UI hierarchy XML."
        return xml
    }

    fun findUiElementsByText(text: String): List<MatchResult> {
        val xml = dumpUiHierarchy()
        val regex = Regex(
            "<node[^>]*text=\\\"([^\"]*${Regex.escape(text)}[^\"]*)\\\"[^>]*bounds=\\\"\\[(\\d+),(\\d+)\\]\\[(\\d+),(\\d+)\\]\\\"",
            RegexOption.IGNORE_CASE
        )
        val matches = regex.findAll(xml).toList()
        if (matches.isEmpty()) {
            throw NoSuchElementException("No UI element found with text: '$text'")
        }
        return matches
    }

    /**
     * Taps on a UI element by its text using UIAutomator dump and adb input tap.
     * Returns a success message or an error if the element is not found or the tap fails.
     */
    fun tapByText(matches: List<MatchResult>, position: Int = 0): String {
        if (matches.isEmpty()) throw NoSuchElementException("No UI elements to tap.")
        if (position !in matches.indices) throw IndexOutOfBoundsException("uiIndex $position is out of bounds for matches list of size ${matches.size}.")
        val (_, x1, y1, x2, y2) = matches[position].destructured
        val centerX = (x1.toInt() + x2.toInt()) / 2
        val centerY = (y1.toInt() + y2.toInt()) / 2
        val tapResult = AdbUtils.runAdb("shell", "input", "tap", centerX.toString(), centerY.toString())
        return when {
            tapResult.isBlank() || tapResult == "\n" -> "Tapped element at ($centerX, $centerY)."
            tapResult.contains("Error") -> "Tap command failed: $tapResult"
            else -> "Tap command output: $tapResult"
        }
    }
}
