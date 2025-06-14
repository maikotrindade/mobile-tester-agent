package agent.tool

import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.reflect.ToolSet

class MobileTestTools : ToolSet {
    @Tool
    @LLMDescription("Tap on a UI element by its selector or coordinates.")
    suspend fun tap(selector: String): String {
        // TODO: Implement tap action on emulator
        return "NOT_IMPLEMENTED"
    }

    @Tool
    @LLMDescription("Input text into a UI element by its selector.")
    suspend fun inputText(selector: String, text: String): String {
        // TODO: Implement input text action on emulator
        return "NOT_IMPLEMENTED"
    }

    @Tool
    @LLMDescription("Get the current screen UI hierarchy as JSON.")
    suspend fun getScreen(): String {
        // TODO: Implement screen dump from emulator
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
        fun getDevices(): Pair<String, List<String>> {
            val process = ProcessBuilder("adb", "devices").redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            val devices = output.lines()
                .drop(1)
                .filter { it.isNotBlank() && !it.contains("List of devices attached") }
            return output to devices
        }
        return try {
            var (output, devices) = getDevices()
            val offlineDevices = devices.filter { it.contains("offline") }
            if (offlineDevices.isNotEmpty()) {
                // Restart ADB server if any device is offline
                ProcessBuilder("adb", "kill-server").start().waitFor()
                ProcessBuilder("adb", "start-server").start().waitFor()
                // Wait a moment for server to restart
                Thread.sleep(1500)
                val result = getDevices()
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
        fun runAdbCommand(vararg args: String): String {
            return try {
                val process = ProcessBuilder("adb", *args)
                    .redirectErrorStream(true)
                    .start()
                val output = process.inputStream.bufferedReader().readText().trim()
                process.waitFor()
                output
            } catch (e: Exception) {
                "Error running adb ${args.joinToString(" ")}: ${e.message}"
            }
        }

        return try {
            val manufacturer = runAdbCommand("shell", "getprop", "ro.product.manufacturer")
            val model = runAdbCommand("shell", "getprop", "ro.product.model")
            val androidVersion = runAdbCommand("shell", "getprop", "ro.build.version.release")
            val sdk = runAdbCommand("shell", "getprop", "ro.build.version.sdk")
            val platform = runAdbCommand("shell", "getprop", "ro.board.platform")
            val memoryTotal = runAdbCommand("shell", "cat", "/proc/meminfo")
                .lines().find { it.contains("MemTotal") } ?: "N/A"
            val batteryLevel = runAdbCommand("shell", "dumpsys", "battery")
                .lines().find { it.contains("level") } ?: "N/A"
            val ipInfo = runAdbCommand("shell", "ip", "addr", "show", "wlan0")
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