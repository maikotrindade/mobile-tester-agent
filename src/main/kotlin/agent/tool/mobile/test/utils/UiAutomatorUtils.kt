package agent.tool.mobile.test.utils

import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

object UiAutomatorUtils {

    private val BOUNDS_RE = Regex("\\[(\\d+),(\\d+)\\]\\[(\\d+),(\\d+)\\]")

    private val NOISE_ATTRS = Regex(
        " (?:index|package|class|checkable|checked|focusable|focused|scrollable|" +
                "long-clickable|password|selected|NAF|instance|rotation)=\"[^\"]*\""
    )

    fun dumpUiHierarchy(): String {
        val raw = dumpUiHierarchyRaw()
        if (raw.startsWith("ERROR:")) return raw
        return NOISE_ATTRS.replace(raw, "")
    }

    /** Raw uiautomator XML — keeps every attribute. Used by the page-model parser. */
    fun dumpUiHierarchyRaw(): String {
        val dumpResult = AdbUtils.runAdb("shell", "uiautomator", "dump")
        if (dumpResult.contains("Error")) {
            return "ERROR: Failed to dump UI hierarchy: $dumpResult"
        }
        val xmlPath = "/sdcard/window_dump.xml"
        val xml = AdbUtils.runAdb("shell", "cat", xmlPath)
        if (xml.isBlank() || xml.contains("Error") || xml.startsWith("Failed"))
            return "ERROR: Failed to read UI hierarchy XML."
        return xml
    }

    fun getScreenSize(): Pair<Int, Int>? {
        val output = AdbUtils.runAdb("shell", "wm", "size")
        val match = Regex("(\\d+)x(\\d+)").find(output) ?: return null
        return match.groupValues[1].toInt() to match.groupValues[2].toInt()
    }

    /**
     * Finds UI nodes whose chosen attribute contains [text] (case-insensitive substring).
     * The text passed in MAY differ slightly from on-screen text (synonym, case, partial phrase).
     *
     * @param selectorType one of "text" | "content-desc" | "resource-id" | "any" (default).
     * @return list of matches; empty if nothing found.
     */
    fun findUiElementsByText(text: String, selectorType: String = "any"): List<UiMatchResult> {
        val xml = dumpUiHierarchy()
        if (xml.startsWith("ERROR:")) return emptyList()
        val escaped = Regex.escape(text)
        val attrClause = when (selectorType.lowercase()) {
            "text" -> "text=\"([^\"]*$escaped[^\"]*)\""
            "content-desc" -> "content-desc=\"([^\"]*$escaped[^\"]*)\""
            "resource-id" -> "resource-id=\"([^\"]*$escaped[^\"]*)\""
            else -> "(?:text=\"([^\"]*$escaped[^\"]*)\"|content-desc=\"([^\"]*$escaped[^\"]*)\"|resource-id=\"([^\"]*$escaped[^\"]*)\")"
        }
        val regex = Regex(
            "<node[^>]*$attrClause[^>]*bounds=\"\\[(\\d+),(\\d+)\\]\\[(\\d+),(\\d+)\\]\"",
            RegexOption.IGNORE_CASE
        )
        return regex.findAll(xml).toList().map { match ->
            val groups = match.groupValues
            val value = groups.drop(1).dropLast(4).firstOrNull { it.isNotEmpty() } ?: ""
            val n = groups.size
            val cx = (groups[n - 4].toInt() + groups[n - 2].toInt()) / 2
            val cy = (groups[n - 3].toInt() + groups[n - 1].toInt()) / 2
            UiMatchResult(value, cx, cy)
        }
    }

    fun tapByText(matches: List<UiMatchResult>, position: Int): String {
        if (matches.isEmpty()) return "NOT_FOUND: no UI elements to tap"
        if (position !in matches.indices) {
            return "ERROR: position $position out of bounds (${matches.size} matches)"
        }
        val m = matches[position]
        val tapResult = AdbUtils.runAdb("shell", "input", "tap", m.cx.toString(), m.cy.toString())
        return when {
            tapResult.isBlank() || tapResult == "\n" -> "TAPPED: (${m.cx},${m.cy})"
            tapResult.contains("Error") -> "ERROR: tap failed: $tapResult"
            else -> "TAPPED: (${m.cx},${m.cy}) output=$tapResult"
        }
    }

    fun inputTextBySelector(selector: String, text: String, selectorType: String = "any"): String {
        return try {
            val matches = findUiElementsByText(selector, selectorType)
            if (matches.isEmpty()) return "NOT_FOUND: no input field matches selector '$selector'"
            val tapResult = tapByText(matches, 0)
            if (tapResult.startsWith("ERROR") || tapResult.startsWith("NOT_FOUND")) {
                return tapResult
            }
            val encodedText = text.replace(" ", "%s")
            val inputResult = AdbUtils.runAdb("shell", "input", "text", encodedText)
            if (inputResult.contains("Error")) "ERROR: input text failed: $inputResult"
            else "OK: typed '$text' into '$selector'"
        } catch (e: Exception) {
            "ERROR: ${e.message}"
        }
    }

    fun tapByCoordinates(x: Int, y: Int): String {
        val tapResult = AdbUtils.runAdb("shell", "input", "tap", x.toString(), y.toString())
        return when {
            tapResult.isBlank() -> "TAPPED: ($x,$y)"
            tapResult.contains("Error") -> "ERROR: tap failed: $tapResult"
            else -> "TAPPED: ($x,$y) output=$tapResult"
        }
    }

    private fun swipeScreen(startX: Int, startY: Int, endX: Int, endY: Int, durationMs: Int): String {
        return AdbUtils.runAdb(
            "shell", "input", "swipe",
            startX.toString(), startY.toString(), endX.toString(), endY.toString(), durationMs.toString()
        )
    }

    /**
     * Vertical scroll. Adapts start coordinates to actual screen size.
     * Positive distance = scroll up (swipe up); negative = scroll down.
     */
    fun scrollScreenVertically(distance: Int = 1000, durationMs: Int = 300): String {
        val (width, height) = getScreenSize() ?: (1080 to 1920)
        val startX = width / 2
        val startY = if (distance > 0) (height * 0.75).toInt() else (height * 0.25).toInt()
        val endY = (startY - distance).coerceIn(0, height)
        val result = swipeScreen(startX, startY, startX, endY, durationMs)
        return if (result.contains("Error")) "ERROR: vertical scroll failed: $result"
        else "OK: scrolled vertically distance=$distance"
    }

    /**
     * Horizontal scroll. Adapts start coordinates to actual screen size.
     * Positive distance = scroll right; negative = scroll left.
     */
    fun scrollScreenHorizontally(distance: Int = 1000, durationMs: Int = 300): String {
        val (width, height) = getScreenSize() ?: (1080 to 1920)
        val startY = height / 2
        val startX = if (distance > 0) (width * 0.25).toInt() else (width * 0.75).toInt()
        val endX = (startX + distance).coerceIn(0, width)
        val result = swipeScreen(startX, startY, endX, startY, durationMs)
        return if (result.contains("Error")) "ERROR: horizontal scroll failed: $result"
        else "OK: scrolled horizontally distance=$distance"
    }

    /**
     * Build a generic page model from the current uiautomator dump.
     *
     * Keeps any node that is interactive (clickable/long-clickable/focusable) OR carries a
     * non-empty text / content-desc. Each kept node is annotated with a coarse semantic [role]
     * derived from its Android class, plus a [parent] index pointing to the nearest interactive
     * ancestor so the LLM can see grouping (list ↔ list items, dialog ↔ buttons, etc.).
     *
     * Sorted top-to-bottom, left-to-right. Zero-area nodes are dropped. Returns empty on parse
     * failure — never throws.
     */
    fun buildPageModel(maxItems: Int = 60): List<PageNode> {
        origCounter = 0
        val xml = dumpUiHierarchyRaw()
        if (xml.startsWith("ERROR:")) return emptyList()
        val doc = try {
            val factory = DocumentBuilderFactory.newInstance().apply {
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                isNamespaceAware = false
            }
            factory.newDocumentBuilder().parse(ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)))
        } catch (_: Exception) {
            return emptyList()
        }

        val raw = mutableListOf<RawNode>()
        walk(doc.documentElement, parentInteractiveIdx = -1, out = raw)

        // Re-index in original-doc order, then resolve parent indices, then sort visually.
        val byOriginalIdx = raw.withIndex().associate { (idx, r) -> r.origId to idx }
        val resolved = raw.mapIndexed { idx, r ->
            val parentResolved = if (r.parentOrigId < 0) -1 else byOriginalIdx[r.parentOrigId] ?: -1
            r.copy(resolvedIdx = idx, resolvedParent = parentResolved)
        }
        val sorted = resolved.sortedWith(compareBy({ it.boundsTop }, { it.boundsLeft }))
            .take(maxItems)

        // After sorting, rebuild indices and remap parent references.
        val origToNew = sorted.withIndex().associate { (newIdx, r) -> r.resolvedIdx to newIdx }
        return sorted.mapIndexed { newIdx, r ->
            PageNode(
                i = newIdx,
                role = r.role,
                text = r.text,
                desc = r.desc,
                id = r.id,
                childTexts = r.childTexts,
                clickable = r.clickable,
                long = r.longClickable,
                enabled = r.enabled,
                selected = r.selected,
                checked = r.checked,
                boundsLeft = r.boundsLeft,
                boundsTop = r.boundsTop,
                boundsRight = r.boundsRight,
                boundsBottom = r.boundsBottom,
                cx = (r.boundsLeft + r.boundsRight) / 2,
                cy = (r.boundsTop + r.boundsBottom) / 2,
                parent = origToNew[r.resolvedParent] ?: -1
            )
        }
    }

    private data class RawNode(
        val origId: Int,
        val parentOrigId: Int,
        val role: String,
        val text: String,
        val desc: String,
        val id: String,
        val childTexts: List<String>,
        val clickable: Boolean,
        val longClickable: Boolean,
        val enabled: Boolean,
        val selected: Boolean,
        val checked: Boolean,
        val boundsLeft: Int,
        val boundsTop: Int,
        val boundsRight: Int,
        val boundsBottom: Int,
        val resolvedIdx: Int = -1,
        val resolvedParent: Int = -1
    )

    private var origCounter = 0

    private fun walk(node: Node, parentInteractiveIdx: Int, out: MutableList<RawNode>) {
        if (node.nodeType != Node.ELEMENT_NODE) return
        val el = node as Element
        if (el.tagName != "node") {
            // Root <hierarchy> wrapper — recurse into children.
            val kids = el.childNodes
            for (i in 0 until kids.length) walk(kids.item(i), parentInteractiveIdx, out)
            return
        }

        val clickable = el.getAttribute("clickable") == "true"
        val longClickable = el.getAttribute("long-clickable") == "true"
        val focusable = el.getAttribute("focusable") == "true"
        val text = el.getAttribute("text").orEmpty()
        val desc = el.getAttribute("content-desc").orEmpty()
        val cls = el.getAttribute("class").orEmpty()
        val resourceId = el.getAttribute("resource-id").orEmpty()
        val boundsRaw = el.getAttribute("bounds").orEmpty()
        val bounds = BOUNDS_RE.matchEntire(boundsRaw)?.groupValues?.drop(1)?.map { it.toInt() }

        val keep = (clickable || longClickable || focusable || text.isNotEmpty() || desc.isNotEmpty()) &&
                bounds != null && (bounds[2] - bounds[0]) > 0 && (bounds[3] - bounds[1]) > 0

        var thisOrigId = -1
        if (keep) {
            thisOrigId = origCounter++
            val childTexts = collectChildTexts(el, includeSelf = false)
            out.add(
                RawNode(
                    origId = thisOrigId,
                    parentOrigId = parentInteractiveIdx,
                    role = classify(cls, clickable, longClickable),
                    text = text,
                    desc = desc,
                    id = resourceId.substringAfterLast('/', missingDelimiterValue = resourceId),
                    childTexts = childTexts,
                    clickable = clickable,
                    longClickable = longClickable,
                    enabled = el.getAttribute("enabled") == "true",
                    selected = el.getAttribute("selected") == "true",
                    checked = el.getAttribute("checked") == "true",
                    boundsLeft = bounds[0],
                    boundsTop = bounds[1],
                    boundsRight = bounds[2],
                    boundsBottom = bounds[3]
                )
            )
        }

        val nextParent = if (keep && (clickable || longClickable)) thisOrigId else parentInteractiveIdx
        val kids = el.childNodes
        for (i in 0 until kids.length) walk(kids.item(i), nextParent, out)
    }

    private fun collectChildTexts(el: Element, includeSelf: Boolean): List<String> {
        val acc = mutableListOf<String>()
        fun visit(n: Node, isRoot: Boolean) {
            if (n.nodeType != Node.ELEMENT_NODE) return
            val e = n as Element
            if (e.tagName == "node" && !(isRoot && !includeSelf)) {
                e.getAttribute("text").takeIf { it.isNotEmpty() }?.let { acc.add(it) }
                e.getAttribute("content-desc").takeIf { it.isNotEmpty() }?.let { acc.add(it) }
            }
            val kids = e.childNodes
            for (i in 0 until kids.length) visit(kids.item(i), isRoot = false)
        }
        visit(el, isRoot = true)
        return acc.distinct()
    }

    private fun classify(cls: String, clickable: Boolean, longClickable: Boolean): String {
        val c = cls.substringAfterLast('.')
        return when {
            c.equals("EditText", ignoreCase = true) || c.contains("AutoComplete", ignoreCase = true) -> "input"
            c.contains("Switch", ignoreCase = true) || c.contains("CheckBox", ignoreCase = true) ||
                    c.contains("RadioButton", ignoreCase = true) || c.contains("ToggleButton", ignoreCase = true) -> "toggle"
            c.contains("Button", ignoreCase = true) -> "button"
            c.contains("TabItem", ignoreCase = true) || c.contains("Tab", ignoreCase = true) &&
                    !c.contains("Layout", ignoreCase = true) -> "tab"
            c.contains("RecyclerView", ignoreCase = true) || c.equals("ListView", ignoreCase = true) ||
                    c.contains("GridView", ignoreCase = true) -> "list"
            c.contains("ScrollView", ignoreCase = true) -> "scroll"
            c.contains("Dialog", ignoreCase = true) || c.contains("AlertDialog", ignoreCase = true) -> "dialog"
            c.contains("ImageView", ignoreCase = true) -> if (clickable || longClickable) "button" else "image"
            c.contains("TextView", ignoreCase = true) -> if (clickable || longClickable) "button" else "text"
            (clickable || longClickable) -> "listItem"
            else -> "container"
        }
    }

}
