package agent.tool.mobile.test

import agent.tool.mobile.test.utils.AdbUtils
import agent.tool.mobile.test.utils.MediaUtils
import agent.tool.mobile.test.utils.UiAutomatorUtils
import agent.tool.mobile.test.utils.UiMatchResult
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet

class MobileTestTools : ToolSet {

    @Tool
    @LLMDescription(
        "Find all UI elements whose text, content-desc, or resource-id matches or" +
                " contains the given string, considering Android Accessibility tags."
    )
    fun findUiElementsByText(
        @LLMDescription("The text to search for in the UI elements.")
        text: String
    ): List<UiMatchResult> {
        return try {
            UiAutomatorUtils.findUiElementsByText(text)
        } catch (e: NoSuchElementException) {
            println(e.localizedMessage)
            emptyList()
        }
    }

    @Tool
    @LLMDescription(
        "Tap element by text, content-desc, or resource-id: " +
                "Locate and tap the clickable UI element that best matches the provided text, " +
                "content-desc, or resource-id, " +
                "even if the text is not an exact match or is only a partial phrase. " +
                "Prioritize elements relevant to the current screen's context. " +
                "If there is more than one clickable button, use its position to define which button to click. "
    )
    fun tap(
        @LLMDescription("The text, content-desc, or resource-id of the element to tap.")
        text: String,
        @LLMDescription("The position of the element to tap if multiple elements are found.")
        position: Int = 0
    ): String {
        val clickableUIs = findUiElementsByText(text)
        return UiAutomatorUtils.tapByText(clickableUIs, position)
    }

    @Tool
    @LLMDescription(
        "Scrolls the screen vertically to simulate user interaction. " +
                "Use a positive distance (e.g., 1000) to scroll upward (i.e., swipe up), " +
                "and a negative distance to scroll downward (i.e., swipe down). " +
                "Optional: specify duration in milliseconds to control swipe speed. " +
                "Example: scrollVertically(distance = 1500, durationMs = 500)"
    )
    fun scrollVertically(
        @LLMDescription("The distance to scroll. Positive for up, negative for down.")
        distance: Int = 1000,
        @LLMDescription("The duration of the scroll in milliseconds.")
        durationMs: Int = 300
    ): String {
        return UiAutomatorUtils.scrollScreenVertically(distance, durationMs)
    }

    @Tool
    @LLMDescription(
        "Scrolls the screen horizontally to simulate user interaction. " +
                "Use a positive distance (e.g., 1000) to scroll right (i.e., swipe left to right), " +
                "and a negative distance to scroll left (i.e., swipe right to left). " +
                "Optional: specify duration in milliseconds to control swipe speed. " +
                "Example: scrollHorizontally(distance = -1200, durationMs = 400)"
    )
    fun scrollHorizontally(
        @LLMDescription("The distance to scroll. Positive for right, negative for left.")
        distance: Int = 1000,
        @LLMDescription("The duration of the scroll in milliseconds.")
        durationMs: Int = 300
    ): String {
        return UiAutomatorUtils.scrollScreenHorizontally(distance, durationMs)
    }

    @Tool
    @LLMDescription(
        "Input text into a UI element by its selector. " +
                "The selector should be the text of the element. " +
                "The keyboard must be hidden after inputting text. " +
                "Returns a success or error message."
    )
    fun inputText(
        @LLMDescription("The selector of the UI element to input text into.")
        selector: String,
        @LLMDescription("The text to input.")
        text: String
    ): String {
        return UiAutomatorUtils.inputTextBySelector(selector, text)
    }

    @Tool
    @LLMDescription("Go back in the app navigation by simulating the Android back button.")
    fun goBack(): String {
        val result = AdbUtils.runAdb("shell", "input", "keyevent", "4")
        return if (result.contains("Error")) "Failed to go back: $result" else "Went back in navigation."
    }

    @Tool
    @LLMDescription(
        "Hide keyboard by simulating user input or back press." +
                "Usually the keyboard should be hidden after a text is input."
    )
    fun hideKeyboard(): String {
        // First try dismissing it by sending an empty input text
        var result = AdbUtils.runAdb("shell", "input", "text", "\"\"")

        // If that fails, try the KEYCODE_BACK as a fallback
        if (result.contains("Error") || result.isBlank()) {
            result = AdbUtils.runAdb("shell", "input", "keyevent", "111") // KEYCODE_ESCAPE (API 11+)
            if (result.contains("Error") || result.isBlank()) {
                result = AdbUtils.runAdb("shell", "input", "keyevent", "4") // KEYCODE_BACK
            }
        }

        return if (result.contains("Error")) "Failed to hide keyboard: $result" else "Keyboard hidden successfully."
    }

    @Tool
    @LLMDescription(
        "Take a screenshot of the current screen and pull it to a remote path. " +
                "`goalName` is the test goal name. " +
                "Returns the local file path or error message. "
    )
    suspend fun takeScreenshot(
        @LLMDescription("The name of the goal for which the screenshot is being taken.")
        goalName: String
    ): String {
        return MediaUtils.takeScreenshot(goalName)
    }

    @Tool
    @LLMDescription(
        "Start recording a video of the device screen. " +
                "This will record until you call stopScreenRecording. " +
                "Returns a message indicating recording has started or an error."
    )
    suspend fun startScreenRecording(): String {
        return MediaUtils.startScreenRecording()
    }

    @Tool
    @LLMDescription(
        "Stop the ongoing screen recording, pull the video to the local machine, " +
                "and return the local file path or error message."
    )
    suspend fun stopScreenRecording(): String {
        return MediaUtils.stopScreenRecording()
    }

    @Tool
    @LLMDescription(
        "Connect to a local Android device or emulator using ADB. " +
                "If any device is offline, the ADB server will be restarted and the connection retried. " +
                "Returns a summary of connected and offline devices, or an error message if no devices are found."
    )
    fun connectDevice(): String {
        return AdbUtils.connectDevice()
    }

    @Tool
    @LLMDescription(
        description = "Get detailed information about the connected Android device using adb, " +
                "including manufacturer, model, Android version, SDK, platform, total memory," +
                " data partition usage, battery level, and IP address."
    )
    fun deviceInformation(): String {
        return AdbUtils.deviceInformation()
    }

    @Tool
    @LLMDescription(
        "Close the currently active foreground app on a connected Android device via ADB." +
                "Identifies the package name of the app currently in focus, then runs `adb shell am force-stop " +
                "to close it."
    )
    fun closeApp(): String {
        val result = AdbUtils.closeCurrentApp()
        return result
    }
}