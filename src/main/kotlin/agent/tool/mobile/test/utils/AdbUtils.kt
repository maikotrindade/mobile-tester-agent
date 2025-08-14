package agent.tool.mobile.test.utils

object AdbUtils {
    /**
     * Runs an adb command with the given arguments.
     *
     * @param args The arguments to pass to the adb command.
     * @return The output of the command, or an error message if it fails.
     */
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

    /**
     * Gets the list of connected adb devices.
     *
     * @return A pair containing the raw output and a list of device identifiers.
     */
    fun getDevices(): Pair<String, List<String>> {
        val process = ProcessBuilder("adb", "devices").redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        val devices = output.lines()
            .drop(1)
            .filter { it.isNotBlank() && !it.contains("List of devices attached") }
        return output to devices
    }

    /**
     * Connects to a device, handling offline devices by restarting the adb server.
     *
     * @return A string summarizing the connection status.
     */
    fun connectDevice(): String {
        return try {
            var (output, devices) = getDevices()
            val offlineDevices = devices.filter { it.contains("offline") }
            if (offlineDevices.isNotEmpty()) {
                ProcessBuilder("adb", "kill-server").start().waitFor()
                ProcessBuilder("adb", "start-server").start().waitFor()
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

    /**
     * Closes the current foreground application on the device.
     *
     * @return A message indicating which app was closed, or a failure message.
     */
    fun closeCurrentApp(): String {
        return try {
            val output = runAdb("shell", "dumpsys", "activity", "top")
            
            val regex = Regex("ACTIVITY\\s+([a-zA-Z0-9_.]+)/")
            val packageName = regex.find(output)?.groups?.get(1)?.value

            if (packageName != null && packageName != "system") {
                runAdb("shell", "am", "force-stop", packageName)
                "App '$packageName' has been force-stopped."
            } else {
                "Failed to identify the current foreground app."
            }
        } catch (e: Exception) {
            "Error closing app: ${e.message}"
        }
    }

    /**
     * Gathers and returns detailed information about the connected device.
     *
     * @return A formatted string with device information, or an error message.
     */
    fun deviceInformation(): String {
        return try {
            val manufacturer = runAdb("shell", "getprop", "ro.product.manufacturer")
            val model = runAdb("shell", "getprop", "ro.product.model")
            val androidVersion = runAdb("shell", "getprop", "ro.build.version.release")
            val sdk = runAdb("shell", "getprop", "ro.build.version.sdk")
            val platform = runAdb("shell", "getprop", "ro.board.platform")
            val memoryTotal = runAdb("shell", "cat", "/proc/meminfo")
                .lines().find { it.contains("MemTotal") } ?: "N/A"
            val batteryLevel = runAdb("shell", "dumpsys", "battery")
                .lines().find { it.contains("level") } ?: "N/A"
            val ipInfo = runAdb("shell", "ip", "addr", "show", "wlan0")
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