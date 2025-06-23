package agent.tool

import agent.tool.utils.AdbUtils
import agent.tool.utils.MediaUtils
import agent.tool.utils.TestReportUtils
import agent.tool.utils.UiAutomatorUtils
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet

class MobileTestTools : ToolSet {
    @Tool
    @LLMDescription("Get the current screen UI hierarchy as XML using UIAutomator dump.")
    suspend fun findUiElementsByText(text: String): List<MatchResult> {
        return UiAutomatorUtils.findUiElementsByText(text)
    }

    @Tool
    @LLMDescription(
        """
            Tap element by text: Locate and tap the clickable UI element that best matches the provided text, 
            even if the text is not an exact match or is only a partial phrase. 
            Prioritize elements relevant to the current screen's context.
            If there is more than one clickable button, use its position to define which button to click.
        """
    )
    suspend fun tap(text: String, position: Int = 0): String {
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
    suspend fun scrollVertically(distance: Int = 1000, durationMs: Int = 300): String {
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
    suspend fun scrollHorizontally(distance: Int = 1000, durationMs: Int = 300): String {
        return UiAutomatorUtils.scrollScreenHorizontally(distance, durationMs)
    }

    @Tool
    @LLMDescription(
        "Input text into a UI element by its selector. " +
                "The selector should be the text of the element. " +
                "Returns a success or error message."
    )
    suspend fun inputText(selector: String, text: String): String {
        return UiAutomatorUtils.inputTextBySelector(selector, text)
    }

    @Tool
    @LLMDescription("Go back in the app navigation by simulating the Android back button.")
    suspend fun goBack(): String {
        val result = AdbUtils.runAdb("shell", "input", "keyevent", "4")
        return if (result.contains("Error")) "Failed to go back: $result" else "Went back in navigation."
    }

    @Tool
    @LLMDescription(
        "Take a screenshot of the current screen and pull it to a remote path. " +
                "Returns the local file path or error message."
    )
    suspend fun takeScreenshot(): String {
        return MediaUtils.takeScreenshot()
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
    suspend fun connectDevice(): String {
        return AdbUtils.connectDevice()
    }

    @Tool
    @LLMDescription(
        description = "Get detailed information about the connected Android device using adb, including manufacturer, model, Android version, SDK, platform, total memory, data partition usage, battery level, and IP address."
    )
    suspend fun deviceInformation(): String {
        return AdbUtils.deviceInformation()
    }

    @Tool
    @LLMDescription("Generate a human-readable test report for a given TestScenario. Returns a Markdown summary of the scenario, steps, errors, and result.")
    fun generateTestScenarioReport(scenario: testing.TestScenario): String {
        return TestReportUtils.generateTestReport(scenario)
    }
}