package agent.tool

import agent.tool.utils.AdbUtils
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
    suspend fun tap(text: String, position: Int): String {
        val clickableUIs = findUiElementsByText(text)
        return UiAutomatorUtils.tapByText(clickableUIs, position)
    }

    @Tool
    @LLMDescription("Input text into a UI element by its selector.")
    suspend fun inputText(selector: String, text: String): String {
        // TODO: Implement input text action on emulator
        return "NOT_IMPLEMENTED"
    }

    @Tool
    @LLMDescription("Go back in the app navigation.")
    suspend fun goBack(): String {
        // TODO: Implement back navigation
        return "NOT_IMPLEMENTED"
    }

    @Tool
    @LLMDescription("Take a screenshot of the current screen.")
    suspend fun takeScreenshot(): String {
        // TODO: Implement screenshot capture
        return "NOT_IMPLEMENTED"
    }

    @Tool
    @LLMDescription(
        description = "Connect to a local Android device or emulator using ADB"
    )
    suspend fun connectDevice(): String {
        return try {
            var (output, devices) = AdbUtils.getDevices()
            val offlineDevices = devices.filter { it.contains("offline") }
            if (offlineDevices.isNotEmpty()) {
                // Restart ADB server if any device is offline
                ProcessBuilder("adb", "kill-server").start().waitFor()
                ProcessBuilder("adb", "start-server").start().waitFor()
                // Wait a moment for server to restart
                Thread.sleep(1500)
                val result = AdbUtils.getDevices()
                output = result.first
                devices = result.second
            }
            if (output.contains("List of devices attached")) {
                if (devices.isEmpty()) {
                    "No devices connected."
                } else {
                    val offline = devices.filter { it.contains("offline") }
                    val online = devices.filter { it.contains("device") && !it.contains("offline") }
                    buildString {
                        if (online.isNotEmpty()) append("Connected devices:\n${online.joinToString("\n")}\n")
                        if (offline.isNotEmpty()) append("Devices offline (check connection):\n${offline.joinToString("\n")}")
                    }.trim()
                }
            } else {
                "Failed to get device list: $output"
            }
        } catch (e: Exception) {
            "Error connecting to device: ${e.message}"
        }
    }

    @Tool
    @LLMDescription(
        description = "Get detailed information about the connected Android device using adb, " +
                "including manufacturer, model, Android version, SDK, platform, total memory, " +
                "data partition usage, battery level, and IP address."
    )
    suspend fun deviceInformation(): String {
        return try {
            val manufacturer = AdbUtils.runAdb("shell", "getprop", "ro.product.manufacturer")
            val model = AdbUtils.runAdb("shell", "getprop", "ro.product.model")
            val androidVersion = AdbUtils.runAdb("shell", "getprop", "ro.build.version.release")
            val sdk = AdbUtils.runAdb("shell", "getprop", "ro.build.version.sdk")
            val platform = AdbUtils.runAdb("shell", "getprop", "ro.board.platform")
            val memoryTotal = AdbUtils.runAdb("shell", "cat", "/proc/meminfo")
                .lines().find { it.contains("MemTotal") } ?: "N/A"
            val batteryLevel = AdbUtils.runAdb("shell", "dumpsys", "battery")
                .lines().find { it.contains("level") } ?: "N/A"
            val ipInfo = AdbUtils.runAdb("shell", "ip", "addr", "show", "wlan0")
                .lines().find { it.trim().startsWith("inet ") } ?: "N/A"

            """
                |Manufacturer: $manufacturer
                |Model: $model
                |Android Version: $androidVersion
                |SDK: $sdk
                |Platform: $platform
                |Memory: $memoryTotal
                |Battery Level: $batteryLevel
                |IP Info: $ipInfo
            """.trimMargin()
        } catch (e: Exception) {
            "Error getting device info: ${e.message}"
        }
    }
}