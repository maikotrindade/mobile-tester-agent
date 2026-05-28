package agent.tool.mobile.test

import agent.tool.mobile.test.utils.AdbUtils
import agent.tool.mobile.test.utils.PageNode
import agent.tool.mobile.test.utils.UiAutomatorUtils
import agent.tool.mobile.test.utils.UiMatchResult
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import kotlinx.coroutines.delay

/**
 * All tools return strings prefixed with a STATUS token the agent can pattern-match:
 *   OK | TAPPED | VISIBLE | NOT_VISIBLE | NOT_FOUND | AMBIGUOUS | ERROR | TIMEOUT
 */
class MobileTestTools : ToolSet {

    private var scenarioStarted: Boolean = false

    @Tool
    @LLMDescription(
        "Connect the device/emulator and launch the target app via ADB. Call ONCE as the first action. " +
                "Connects ADB, wakes the screen, force-stops any stale instance, launches the package via monkey, " +
                "then verifies the package is foreground. After this returns OK the app IS open — do NOT tap " +
                "a launcher icon or home screen to open it. " +
                "Returns 'OK: launched <package> (foreground confirmed)' or 'ERROR: ...'."
    )
    fun startTestingScenario(
        @LLMDescription("Android package name to launch, e.g. com.maikogram. Required.")
        appPackage: String
    ): String {
        if (scenarioStarted) {
            return "ERROR: startTestingScenario was already called this run — DO NOT call it again. " +
                    "The app is already launched. Proceed directly to Step 1 of the scenario."
        }
        if (appPackage.isBlank()) return "ERROR: appPackage is required"
        val connect = AdbUtils.connectDevice()
        if (connect.contains("No devices") || connect.startsWith("Failed") || connect.startsWith("Error")) {
            return "ERROR: $connect"
        }
        AdbUtils.runAdb("shell", "input", "keyevent", "224") // KEYCODE_WAKEUP
        Thread.sleep(300)
        AdbUtils.runAdb("shell", "wm", "dismiss-keyguard")
        Thread.sleep(200)
        AdbUtils.runAdb("shell", "am", "force-stop", appPackage)
        Thread.sleep(200)
        val result = AdbUtils.launchAndVerify(appPackage)
        if (result.startsWith("OK")) scenarioStarted = true
        return result
    }

    @Tool
    @LLMDescription(
        "Find UI elements whose text, content-desc, or resource-id contains the given string (case-insensitive, partial). " +
                "Use when you need to inspect candidates before tapping, or to verify presence. " +
                "Search text may differ slightly from on-screen text (case, partial, synonym) — try variations if empty. " +
                "Returns a list of matches (empty list = not found)."
    )
    fun findUiElementsByText(
        @LLMDescription("Text fragment to search for (case-insensitive substring match). Required — must be non-empty.")
        text: String = "",
        @LLMDescription("Attribute to filter on: 'text', 'content-desc', 'resource-id', or 'any' (default).")
        selectorType: String = "any"
    ): List<UiMatchResult> {
        if (text.isBlank()) return emptyList()
        return UiAutomatorUtils.findUiElementsByText(text, selectorType)
    }

    @Tool
    @LLMDescription(
        "Tap a UI element by selector. Preferred over tapByCoordinates. " +
                "Returns 'TAPPED: (x,y)' on success, 'NOT_FOUND: ...' if no match, " +
                "'AMBIGUOUS: <n> matches — retry with position=<i>' if multiple matches and position is out of range. " +
                "When ambiguous, retry with position=1, 2, ... — do not re-search with different text."
    )
    fun tap(
        @LLMDescription("Selector value (text, content-desc, or resource-id fragment). Required — must be non-empty.")
        text: String = "",
        @LLMDescription("Attribute to match on: 'text', 'content-desc', 'resource-id', or 'any' (default).")
        selectorType: String = "any",
        @LLMDescription("0-based index when multiple elements match. Default 0 = first match.")
        position: Int = 0
    ): String {
        if (text.isBlank()) return "ERROR: tap requires a non-empty 'text' selector"
        val matches = UiAutomatorUtils.findUiElementsByText(text, selectorType)
        return when {
            matches.isEmpty() -> "NOT_FOUND: no element matches '$text' (selectorType=$selectorType)"
            position !in matches.indices && matches.size > 1 ->
                "AMBIGUOUS: ${matches.size} matches for '$text' — retry with position in 0..${matches.size - 1}"

            else -> UiAutomatorUtils.tapByText(matches, position.coerceIn(0, matches.size - 1))
        }
    }

    @Tool
    @LLMDescription(
        "Tap exact screen coordinates. Use ONLY as a fallback when selector-based tap cannot find the element. " +
                "Returns 'TAPPED: (x,y)' or 'ERROR: ...'."
    )
    fun tapByCoordinates(
        @LLMDescription("X coordinate in pixels. Required — must be >= 0.") x: Int = -1,
        @LLMDescription("Y coordinate in pixels. Required — must be >= 0.") y: Int = -1
    ): String {
        if (x < 0 || y < 0) return "ERROR: tapByCoordinates requires non-negative x and y (got x=$x, y=$y)"
        return UiAutomatorUtils.tapByCoordinates(x, y)
    }

    @Tool
    @LLMDescription(
        "Verify a UI element is visible on the current screen. Call after an action to confirm the expected state. " +
                "Returns 'VISIBLE: <n> match(es)' or 'NOT_VISIBLE: ...'. " +
                "If NOT_VISIBLE, consider scrolling or waiting before declaring the step failed."
    )
    fun verifyElementVisible(
        @LLMDescription("Text fragment expected on screen. Required — must be non-empty.") text: String = "",
        @LLMDescription("Attribute to match on: 'text', 'content-desc', 'resource-id', or 'any' (default).")
        selectorType: String = "any"
    ): String {
        if (text.isBlank()) return "ERROR: verifyElementVisible requires a non-empty 'text' selector"
        val matches = UiAutomatorUtils.findUiElementsByText(text, selectorType)
        return if (matches.isEmpty()) "NOT_VISIBLE: '$text' not on screen"
        else "VISIBLE: ${matches.size} match(es) for '$text'"
    }

    @Tool
    @LLMDescription(
        "Verify a UI element is NOT visible. Use to confirm a screen transition has occurred (the old element is gone). " +
                "Returns 'OK: gone' or 'NOT_VISIBLE: still present'."
    )
    fun verifyElementNotVisible(
        @LLMDescription("Text fragment that should NOT be on screen. Required — must be non-empty.") text: String = "",
        @LLMDescription("Attribute to match on: 'text', 'content-desc', 'resource-id', or 'any' (default).")
        selectorType: String = "any"
    ): String {
        if (text.isBlank()) return "ERROR: verifyElementNotVisible requires a non-empty 'text' selector"
        val matches = UiAutomatorUtils.findUiElementsByText(text, selectorType)
        return if (matches.isEmpty()) "OK: '$text' is not visible (as expected)"
        else "NOT_VISIBLE: '$text' is still visible (${matches.size} match(es))"
    }

    @Tool
    @LLMDescription(
        "Pause execution for the given milliseconds. Use after navigation, animations, or async loads (typically 300–1500ms). " +
                "Bounded 50..10000. Returns 'OK: waited <ms>ms'."
    )
    suspend fun wait(
        @LLMDescription("Milliseconds to sleep (50..10000). Defaults to 500 if omitted.") ms: Int = 500
    ): String {
        val bounded = ms.coerceIn(50, 10000)
        delay(bounded.toLong())
        return "OK: waited ${bounded}ms"
    }

    @Tool
    @LLMDescription(
        "Dump the raw UI hierarchy XML of the current screen. Use ONLY when stuck — prefer findUiElementsByText for targeted queries. " +
                "Truncated to ~2KB. Returns the XML string or 'ERROR: ...'."
    )
    fun getScreenDump(): String {
        val xml = UiAutomatorUtils.dumpUiHierarchy()
        if (xml.startsWith("ERROR:")) return xml
        return if (xml.length > 2000) xml.take(2000) + "\n... [truncated, use findUiElementsByText for targeted search]"
        else xml
    }

    @Tool
    @LLMDescription(
        "Return a compact JSON snapshot of every meaningful element on the current screen — " +
                "interactive (clickable/long-clickable/focusable) AND non-interactive (labels, images). " +
                "Each item has: i (index), role ('button'|'input'|'text'|'image'|'list'|'listItem'|'tab'|" +
                "'toggle'|'scroll'|'dialog'|'container'), text, desc (content-desc), id (resource-id suffix), " +
                "childTexts (descendant labels in visual order — surfaces nested texts under a card), " +
                "bounds [l,t,r,b], cx, cy (tap center), parent (index of nearest interactive ancestor, " +
                "or -1), and clickable/long/enabled/selected/checked flags. " +
                "Sorted top-to-bottom, left-to-right. Capped at ~60 items / ~6KB with a 'truncated' flag. " +
                "Use this for ANY generic step (positional, semantic, descriptive, icon-only, ambiguous). " +
                "Pick the right item by reasoning over childTexts/role/parent, then tap via tapByCoordinates(cx,cy). " +
                "Returns 'OK: <json>' or 'ERROR: ...'."
    )
    fun perceiveScreen(): String {
        return try {
            val nodes = UiAutomatorUtils.buildPageModel(maxItems = 40)
            if (nodes.isEmpty()) {
                val dump = UiAutomatorUtils.dumpUiHierarchyRaw()
                if (dump.startsWith("ERROR:")) return dump
                return "OK: ${buildPageJson(nodes, truncated = false)}"
            }
            val (w, h) = UiAutomatorUtils.getScreenSize() ?: (0 to 0)
            var truncated = false
            var items = nodes
            var json = buildPageJson(items, truncated = false, screenW = w, screenH = h)
            while (json.length > 4000 && items.size > 1) {
                items = items.dropLast(1)
                truncated = true
                json = buildPageJson(items, truncated = true, screenW = w, screenH = h)
            }
            "OK: $json"
        } catch (e: Exception) {
            "ERROR: perceiveScreen failed: ${e.message}"
        }
    }

    private fun buildPageJson(items: List<PageNode>, truncated: Boolean, screenW: Int = 0, screenH: Int = 0): String {
        val sb = StringBuilder()
        sb.append('{')
        if (screenW > 0) sb.append("\"w\":").append(screenW).append(',')
        if (screenH > 0) sb.append("\"h\":").append(screenH).append(',')
        sb.append("\"count\":").append(items.size)
        sb.append(",\"truncated\":").append(truncated)
        sb.append(",\"items\":[")
        items.forEachIndexed { idx, n ->
            if (idx > 0) sb.append(',')
            sb.append('{')
            sb.append("\"i\":").append(n.i)
            sb.append(",\"role\":\"").append(escapeJson(n.role)).append('"')
            if (n.text.isNotEmpty()) sb.append(",\"text\":\"").append(escapeJson(n.text)).append('"')
            if (n.desc.isNotEmpty()) sb.append(",\"desc\":\"").append(escapeJson(n.desc)).append('"')
            if (n.id.isNotEmpty()) sb.append(",\"id\":\"").append(escapeJson(n.id)).append('"')
            if (n.childTexts.isNotEmpty()) {
                sb.append(",\"childTexts\":[")
                n.childTexts.forEachIndexed { i, t ->
                    if (i > 0) sb.append(',')
                    sb.append('"').append(escapeJson(t)).append('"')
                }
                sb.append(']')
            }
            sb.append(",\"bounds\":[").append(n.boundsLeft).append(',').append(n.boundsTop).append(',')
                .append(n.boundsRight).append(',').append(n.boundsBottom).append(']')
            sb.append(",\"cx\":").append(n.cx).append(",\"cy\":").append(n.cy)
            if (n.clickable) sb.append(",\"clickable\":true")
            if (n.long) sb.append(",\"long\":true")
            if (n.selected) sb.append(",\"selected\":true")
            if (n.checked) sb.append(",\"checked\":true")
            if (!n.enabled) sb.append(",\"enabled\":false")
            sb.append(",\"parent\":").append(n.parent)
            sb.append('}')
        }
        sb.append("]}")
        return sb.toString()
    }

    private fun escapeJson(s: String): String {
        val sb = StringBuilder(s.length + 8)
        for (c in s) when (c) {
            '\\' -> sb.append("\\\\")
            '"' -> sb.append("\\\"")
            '\n' -> sb.append("\\n")
            '\r' -> sb.append("\\r")
            '\t' -> sb.append("\\t")
            in '\u0000'..'\u001f' -> sb.append("\\u%04x".format(c.code))
            else -> sb.append(c)
        }
        return sb.toString()
    }

    @Tool
    @LLMDescription(
        "Get the device screen size in pixels. Returns 'OK: <width>x<height>' or 'ERROR: ...'. " +
                "Useful before computing scroll distances on tablets."
    )
    fun getScreenSize(): String {
        val size = UiAutomatorUtils.getScreenSize() ?: return "ERROR: could not read screen size"
        return "OK: ${size.first}x${size.second}"
    }

    @Tool
    @LLMDescription(
        "Swipe up ~70% of screen height (reveals content below). Adaptive to device size. " +
                "Prefer this over scrollVertically for normal scrolling."
    )
    fun swipeUp(): String {
        val (w, h) = UiAutomatorUtils.getScreenSize() ?: (1080 to 1920)
        return UiAutomatorUtils.scrollScreenVertically(distance = (h * 0.6).toInt(), durationMs = 300)
            .let { if (it.startsWith("OK")) "OK: swiped up on ${w}x${h}" else it }
    }

    @Tool
    @LLMDescription(
        "Swipe down ~70% of screen height (reveals content above). Adaptive to device size. " +
                "Prefer this over scrollVertically for normal scrolling."
    )
    fun swipeDown(): String {
        val (w, h) = UiAutomatorUtils.getScreenSize() ?: (1080 to 1920)
        return UiAutomatorUtils.scrollScreenVertically(distance = -(h * 0.6).toInt(), durationMs = 300)
            .let { if (it.startsWith("OK")) "OK: swiped down on ${w}x${h}" else it }
    }

    @Tool
    @LLMDescription(
        "Precise vertical scroll by pixel distance. Use swipeUp/swipeDown for normal scrolling. " +
                "Positive distance = swipe up (scroll content up); negative = swipe down."
    )
    fun scrollVertically(
        @LLMDescription("Pixels. Positive = up, negative = down.") distance: Int = 1000,
        @LLMDescription("Swipe duration in ms.") durationMs: Int = 300
    ): String = UiAutomatorUtils.scrollScreenVertically(distance, durationMs)

    @Tool
    @LLMDescription(
        "Precise horizontal scroll by pixel distance. Positive distance = swipe right; negative = swipe left."
    )
    fun scrollHorizontally(
        @LLMDescription("Pixels. Positive = right, negative = left.") distance: Int = 1000,
        @LLMDescription("Swipe duration in ms.") durationMs: Int = 300
    ): String = UiAutomatorUtils.scrollScreenHorizontally(distance, durationMs)

    @Tool
    @LLMDescription(
        "Type text into an input field identified by selector. Auto-hides the keyboard after typing. " +
                "Returns 'OK: typed ...', 'NOT_FOUND: ...', or 'ERROR: ...'."
    )
    fun inputText(
        @LLMDescription("Selector matching the input field's text, hint (content-desc), or resource-id. Required.")
        fieldSelector: String = "",
        @LLMDescription("Text to type. Required.")
        text: String = "",
        @LLMDescription("Attribute to match on: 'text', 'content-desc', 'resource-id', or 'any' (default).")
        selectorType: String = "any"
    ): String {
        if (fieldSelector.isBlank()) return "ERROR: inputText requires a non-empty 'fieldSelector'"
        if (text.isBlank()) return "ERROR: inputText requires a non-empty 'text' value"
        val result = UiAutomatorUtils.inputTextBySelector(fieldSelector, text, selectorType)
        if (result.startsWith("OK")) hideKeyboard()
        return result
    }

    @Tool
    @LLMDescription("Press the Android back button. Returns 'OK: ...' or 'ERROR: ...'.")
    fun goBack(): String {
        val result = AdbUtils.runAdb("shell", "input", "keyevent", "4")
        return if (result.contains("Error")) "ERROR: back failed: $result" else "OK: pressed back"
    }

    @Tool
    @LLMDescription(
        "Hide the on-screen keyboard. inputText auto-hides, so call this only after manual key events. " +
                "Returns 'OK: ...' or 'ERROR: ...'."
    )
    fun hideKeyboard(): String {
        // KEYCODE_BACK (4) is the only reliable way to dismiss the soft keyboard on this device.
        // KEYCODE_ESCAPE (111) leaves the keyboard showing. BACK dismisses keyboard on first press;
        // a second press would navigate back, but only if keyboard is already gone.
        val result = AdbUtils.runAdb("shell", "input", "keyevent", "4")
        Thread.sleep(500) // wait for keyboard animation to complete before next UI query
        return if (result.contains("Error")) "ERROR: hide keyboard failed: $result" else "OK: keyboard hidden"
    }

    @Tool
    @LLMDescription(
        "Launch an Android app by package name (e.g. com.android.settings). " +
                "Use mid-test when scenario switches apps. Returns 'OK: ...' or 'ERROR: ...'."
    )
    fun launchAppByPackage(
        @LLMDescription("Android package name, e.g. com.android.settings. Required.") packageName: String = ""
    ): String {
        if (packageName.isBlank()) return "ERROR: launchAppByPackage requires a non-empty 'packageName'"
        val result = AdbUtils.runAdb(
            "shell", "monkey", "-p", packageName, "-c", "android.intent.category.LAUNCHER", "1"
        )
        return if (result.contains("Error") || result.contains("No activities")) "ERROR: launch failed: $result"
        else "OK: launched $packageName"
    }

}
