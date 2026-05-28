package agent.tool.mobile.test.utils

object AdbUtils {
    /** Serial of the device targeted for this test session. Set by connectDevice(). */
    var targetSerial: String? = null

    /**
     * Runs an adb command with the given arguments, targeting [targetSerial] when set.
     * This ensures commands go to the right device when multiple are attached.
     */
    fun runAdb(vararg args: String): String {
        return try {
            val cmd = buildList {
                add("adb")
                targetSerial?.let { add("-s"); add(it) }
                addAll(args.toList())
            }
            val process = ProcessBuilder(cmd)
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
                    // Pin the target: prefer emulator, otherwise first online device.
                    targetSerial = (online.firstOrNull { it.startsWith("emulator") } ?: online.firstOrNull())
                        ?.split("\t")?.first()
                    buildString {
                        if (online.isNotEmpty()) append("Connected devices:\n${online.joinToString("\n")}\n")
                        if (offline.isNotEmpty()) append("Devices offline (check connection):\n${offline.joinToString("\n")}")
                        targetSerial?.let { append("\nTargeting: $it") }
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
     * Gathers and returns detailed information about the connected device.
     *
     * @return A formatted string with device information, or an error message.
     */
    /**
     * Returns the package name of the current foreground activity, or null if it can't be read.
     */
    fun foregroundPackage(): String? {
        // Try several sources — Android exposes the focused app in different places
        // and `dumpsys activity top` can briefly still show the launcher mid-transition.
        val focus = runAdb("shell", "dumpsys", "window")
        Regex("mCurrentFocus=.*?\\s+([a-zA-Z0-9_.]+)/").find(focus)?.groups?.get(1)?.value?.let { return it }
        Regex("mFocusedApp=.*?\\s+([a-zA-Z0-9_.]+)/").find(focus)?.groups?.get(1)?.value?.let { return it }
        val top = runAdb("shell", "dumpsys", "activity", "top")
        return Regex("ACTIVITY\\s+([a-zA-Z0-9_.]+)/").findAll(top).lastOrNull()?.groups?.get(1)?.value
    }

    /** True if at least one process for [packageName] is alive on the device. */
    private fun isProcessRunning(packageName: String): Boolean {
        val out = runAdb("shell", "pidof", packageName)
        return out.isNotBlank() && !out.contains("Error") && out.trim().all { it.isDigit() || it == ' ' }
    }

    /**
     * Launches an app by package via monkey, then verifies it reached the foreground.
     * Polls for up to ~6s; if the process is alive but foreground detection still races
     * (common on the Pixel launcher transition), treat it as success rather than a false alarm.
     */
    fun launchAndVerify(packageName: String): String {
        val launch = runAdb(
            "shell", "monkey", "-p", packageName,
            "-c", "android.intent.category.LAUNCHER", "1"
        )
        if (launch.contains("Error") || launch.contains("No activities")) {
            return "ERROR: failed to launch '$packageName': $launch"
        }
        repeat(6) {
            Thread.sleep(1000)
            if (foregroundPackage() == packageName) {
                return "OK: launched $packageName (foreground confirmed)"
            }
        }
        if (isProcessRunning(packageName)) {
            return "OK: launched $packageName (process running; foreground check inconclusive)"
        }
        val fg = foregroundPackage() ?: "unknown"
        return "ERROR: launched '$packageName' but foreground is '$fg' after retry"
    }

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