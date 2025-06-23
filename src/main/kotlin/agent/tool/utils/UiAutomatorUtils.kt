package agent.tool.utils

object UiAutomatorUtils {
    /**
     * Dumps the current UI hierarchy using UIAutomator and returns the XML as a String.
     * Returns an error message if the dump or read fails.
     * Considers Android Accessibility tags (content-desc, resource-id, etc).
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

    /**
     * Finds all UI elements on the current screen whose text, content-desc, or resource-id matches or contains the given string.
     * Uses a regex search on the UIAutomator XML dump to locate nodes with matching attributes.
     *
     * @param text The text to search for in UI elements.
     * @return A list of MatchResult objects for each matching UI element found.
     * @throws NoSuchElementException if no UI element with the given text is found.
     */
    fun findUiElementsByText(text: String): List<MatchResult> {
        val xml = dumpUiHierarchy()
        val regex = Regex(
            "<node[^>]*(text=\\\"([^\\\"]*${Regex.escape(text)}[^\\\"]*)\\\"|content-desc=\\\"([^\\\"]*${
                Regex.escape(
                    text
                )
            }[^\\\"]*)\\\"|resource-id=\\\"([^\\\"]*${Regex.escape(text)}[^\\\"]*)\\\")[^>]*bounds=\\\"\\[(\\d+),(\\d+)\\]\\[(\\d+),(\\d+)\\]\\\"",
            RegexOption.IGNORE_CASE
        )
        val matches = regex.findAll(xml).toList()
        if (matches.isEmpty()) {
            throw NoSuchElementException("No UI element found with text/accessibility: '$text'")
        }
        return matches
    }

    /**
     * Taps on a UI element by its text, content-desc, or resource-id using UIAutomator dump and adb input tap.
     * Returns a success message or an error if the element is not found or the tap fails.
     */
    fun tapByText(matches: List<MatchResult>, position: Int): String {
        if (matches.isEmpty()) throw NoSuchElementException("No UI elements to tap.")
        if (position !in matches.indices) throw IndexOutOfBoundsException("position $position is out of bounds for matches list of size ${matches.size}.")
        // The regex may have up to 4 capturing groups before bounds, so find the bounds at the end
        val groups = matches[position].groupValues
        val x1 = groups[groups.size - 4]
        val y1 = groups[groups.size - 3]
        val x2 = groups[groups.size - 2]
        val y2 = groups[groups.size - 1]
        val centerX = (x1.toInt() + x2.toInt()) / 2
        val centerY = (y1.toInt() + y2.toInt()) / 2
        val tapResult = AdbUtils.runAdb("shell", "input", "tap", centerX.toString(), centerY.toString())
        return when {
            tapResult.isBlank() || tapResult == "\n" -> "Tapped element at ($centerX, $centerY)."
            tapResult.contains("Error") -> "Tap command failed: $tapResult"
            else -> "Tap command output: $tapResult"
        }
    }

    /**
     * Finds a UI element by selector (text), taps it, and inputs the given text.
     * Returns a success or error message.
     */
    fun inputTextBySelector(selector: String, text: String): String {
        return try {
            val matches = findUiElementsByText(selector)
            if (matches.isEmpty()) return "No element found with selector: $selector"
            tapByText(matches, 0)
            val encodedText = text.replace(" ", "%s")
            val inputResult = AdbUtils.runAdb("shell", "input", "text", encodedText)
            if (inputResult.contains("Error")) "Failed to input text: $inputResult" else "Input text '$text' into element with selector '$selector'"
        } catch (e: Exception) {
            "Error inputting text: ${e.message}"
        }
    }

    /**
     * Performs a swipe gesture from (startX, startY) to (endX, endY).
     * Returns the result of the adb command.
     */
    private fun swipeScreen(startX: Int, startY: Int, endX: Int, endY: Int): String {
        return AdbUtils.runAdb(
            "shell", "input", "swipe",
            startX.toString(), startY.toString(), endX.toString(), endY.toString()
        )
    }

    /**
     * Scrolls the screen vertically. Positive distance scrolls up, negative scrolls down.
     * @param distance The distance in pixels to scroll. Default is 1000 (up).
     * @param durationMs The duration of the swipe in ms. Default is 300.
     */
    fun scrollScreenVertically(distance: Int = 1000, durationMs: Int = 300): String {
        val startX = 500
        val startY = if (distance > 0) 1500 else 500
        val endY = startY - distance
        return swipeScreen(startX, startY, startX, endY)
    }

    /**
     * Scrolls the screen horizontally. Positive distance scrolls right, negative scrolls left.
     * @param distance The distance in pixels to scroll. Default is 1000 (right).
     * @param durationMs The duration of the swipe in ms. Default is 300.
     */
    fun scrollScreenHorizontally(distance: Int = 1000, durationMs: Int = 300): String {
        val startY = 1000
        val startX = if (distance > 0) 500 else 1500
        val endX = startX + distance
        return swipeScreen(startX, startY, endX, startY)
    }
}
