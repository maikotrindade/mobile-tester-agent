# Tools

Tools are the agent's *hands*. Every method on `MobileTestTools` and `ReportingTools` is annotated with Koog's `@Tool` + `@LLMDescription` so it is auto-registered in the `ToolRegistry` and surfaced to the LLM as a callable function. The tool catalog covers **Android, iOS, and React Native** under one uniform surface — taps, swipes, text input, and verification work the same way regardless of platform.

This page documents:

1. The full tool catalog (signatures, status prefixes, when to use)
2. The status-prefix convention every tool follows
3. The utility layer underneath (`AdbUtils`, `UiAutomatorUtils`, `MediaUtils`)
4. How to add a new tool

---

## 1. Status-prefix convention

Every tool returns a `String` that begins with one of these tokens:

| Prefix | Meaning |
|---|---|
| `OK` | Action succeeded; ready for the next step |
| `TAPPED: (x,y)` | A tap was issued at the given screen coordinate |
| `VISIBLE: <n> match(es)` | The element queried is on screen |
| `NOT_VISIBLE` | Element not on screen — try scrolling / waiting |
| `NOT_FOUND` | No element matches the selector — try variations |
| `AMBIGUOUS` | Multiple matches; retry with a `position` index |
| `ERROR` | The tool itself failed (ADB/IO) |
| `TIMEOUT` | The operation exceeded its bound |

The LLM is instructed (in the [system prompt](ai-agent.md#system-prompt)) to pattern-match this prefix to decide its next move. Tool authors **must** preserve this convention.

---

## 2. `MobileTestTools` — the catalog

Defined in [agent/tool/mobile/test/MobileTestTools.kt](../src/main/kotlin/agent/tool/mobile/test/MobileTestTools.kt).

### Lifecycle

#### `startTestingScenario(appPackage: String): String`
First action in every run. Connects ADB, wakes screen, dismisses keyguard, force-stops any stale instance, launches the package via `monkey`, and **verifies** it reached the foreground (retrying once). On success the target app is already on screen — the model is forbidden from "manually" tapping a launcher icon.

> Returns `OK: launched <package> (foreground confirmed)` or `ERROR: ...`.

#### `closeApp(): String`
Final action. Reads `dumpsys activity top` to find the current foreground package and `am force-stop`s it.

#### `launchAppByPackage(packageName: String): String`
Mid-run app switch (e.g. open Settings to toggle airplane mode, return to the app under test).

---

### Discovery

#### `findUiElementsByText(text: String, selectorType: String = "any"): List<UiMatchResult>`
Searches the dumped UI hierarchy for nodes whose `text`, `content-desc`, or `resource-id` contains the given fragment (case-insensitive, substring). `selectorType` ∈ `{"text", "content-desc", "resource-id", "any"}`. Returns a list of `UiMatchResult(value, cx, cy)` — `cx,cy` is the computed tap center.

#### `getScreenDump(): String`
Returns the raw UI hierarchy XML (truncated to ~2KB). Use as a last resort when `findUiElementsByText` keeps coming back empty — the model is told to prefer the targeted query.

#### `getScreenSize(): String`
Returns `OK: <width>x<height>` from `adb shell wm size`. Useful before computing scroll distances on tablets.

#### `deviceInformation()` *(internal, not yet exposed as @Tool)*
Helper in [AdbUtils](../src/main/kotlin/agent/tool/mobile/test/utils/AdbUtils.kt) that returns manufacturer/model/OS/SDK/platform/memory/battery/IP. Could be promoted to a `@Tool` if you want the agent to branch on device characteristics.

---

### Interaction

#### `tap(text: String, selectorType: String = "any", position: Int = 0): String`
Preferred tap. Looks up matches, then taps the center of the `position`-th one. Returns:
* `TAPPED: (x,y)` on success
* `NOT_FOUND: no element matches '<text>' (selectorType=<…>)`
* `AMBIGUOUS: <n> matches for '<text>' — retry with position in 0..<n-1>` when multiple matches and `position` is out of range
* `ERROR: ...` on tap failure

#### `tapByCoordinates(x: Int, y: Int): String`
Fallback when selectors can't find an element. Sends `adb shell input tap`.

#### `inputText(fieldSelector: String, text: String, selectorType: String = "any"): String`
Taps the input field (using the same selector logic as `tap`), encodes spaces as `%s`, then `adb shell input text`. Auto-calls `hideKeyboard` on success so the next UI query isn't obstructed.

#### `goBack(): String`
`adb shell input keyevent 4` (KEYCODE_BACK). Returns `OK: pressed back` or `ERROR: ...`.

#### `hideKeyboard(): String`
Also `KEYCODE_BACK`. **Why not `KEYCODE_ESCAPE`?** On the target device, ESC (111) leaves the keyboard up; BACK (4) dismisses it on first press. Waits 500ms for the keyboard animation to complete before returning, so the next UI query reads a stable layout. Documented inline in [MobileTestTools.kt:230](../src/main/kotlin/agent/tool/mobile/test/MobileTestTools.kt).

---

### Scrolling

#### `swipeUp(): String`
Convenience: swipe up ~60% of screen height (reveals content below). Adaptive to device size — falls back to 1080×1920 if size can't be read.

#### `swipeDown(): String`
Mirror of `swipeUp`.

#### `scrollVertically(distance: Int = 1000, durationMs: Int = 300): String`
Precise vertical scroll. **Positive distance = swipe up** (content scrolls up); negative = down. Start coords adapt to screen size (75% / 25% of height).

#### `scrollHorizontally(distance: Int = 1000, durationMs: Int = 300): String`
Same idea for the X axis. Positive = right; negative = left.

---

### Verification

#### `verifyElementVisible(text: String, selectorType: String = "any"): String`
After an action, confirm the expected element is on screen. Returns `VISIBLE: <n> match(es)` or `NOT_VISIBLE: '<text>' not on screen`.

#### `verifyElementNotVisible(text: String, selectorType: String = "any"): String`
Confirms a screen transition: the element you expected to leave is gone. Returns `OK: '<text>' is not visible (as expected)` or `NOT_VISIBLE: '<text>' is still visible (...)`.

---

### Timing

#### `wait(ms: Int): String`
`suspend` function that `kotlinx.coroutines.delay`s for the bounded value in `50..10000` ms. Use after navigation, animations, or async loads (typically 300–1500ms). Returns `OK: waited <ms>ms`.

---

## 3. `ReportingTools` — screenshots & recording

Defined in [agent/tool/mobile/test/ReportingTools.kt](../src/main/kotlin/agent/tool/mobile/test/ReportingTools.kt). All three methods delegate to [MediaUtils](../src/main/kotlin/agent/tool/mobile/test/utils/MediaUtils.kt).

| Tool | What it does |
|---|---|
| `takeScreenshot(goalName: String)` | `screencap` → pull to `$HOME_PATH/<slug>-<index>.png`. Returns local file path |
| `startScreenRecording()` | Spawns `adb shell screenrecord` in the background |
| `stopScreenRecording()` | Destroys the recording process, sleeps 1s for the file to finalize, pulls `/sdcard/video.mp4` to `$HOME_PATH/video.mp4`. Returns local path |

> Note: there is a second `agent/tool/reporting/ReportingTools.kt` containing `initializeTestScenarioReport`, `updateTestScenarioReport`, and `generateTestScenarioReport`. These are **not** currently wired into the `ToolRegistry` in [MobileTestAgent.kt](../src/main/kotlin/agent/MobileTestAgent.kt) — the agent's PASS/FAIL summary is produced by the LLM directly in the final assistant message. The class is kept as scaffolding for a future "structured report" feature.

---

## 4. The utility layer

The `@Tool` methods are thin wrappers — the real work lives in three utility objects.

### `AdbUtils` — raw ADB execution

[agent/tool/mobile/test/utils/AdbUtils.kt](../src/main/kotlin/agent/tool/mobile/test/utils/AdbUtils.kt) holds:

* **`targetSerial: String?`** — populated by `connectDevice()`. Every `runAdb()` call injects `-s <serial>` so commands route to the right device when multiple are attached.
* **`runAdb(vararg args)`** — `ProcessBuilder("adb", "-s", serial, …args).redirectErrorStream(true).start()`, returns trimmed stdout. Swallows exceptions into an `Error running adb …` string.
* **`getDevices()`** — parses `adb devices` output into a list, dropping the header.
* **`connectDevice()`** — restarts the adb server if any devices are offline, then picks a target: emulator preferred, otherwise the first online device.
* **`closeCurrentApp()`** — regex-extracts `ACTIVITY <pkg>/...` from `dumpsys activity top` and `am force-stop`s it.
* **`foregroundPackage()`** — same regex without the stop.
* **`launchAndVerify(packageName)`** — `monkey -p <pkg> -c android.intent.category.LAUNCHER 1`, sleeps 1.5s, re-reads `foregroundPackage()`, retries once after 2.5s if it doesn't match.
* **`deviceInformation()`** — multi-prop dump for diagnostics.

### `UiAutomatorUtils` — hierarchy + interaction

[agent/tool/mobile/test/utils/UiAutomatorUtils.kt](../src/main/kotlin/agent/tool/mobile/test/utils/UiAutomatorUtils.kt) handles:

* **`dumpUiHierarchy()`** — `uiautomator dump` to `/sdcard/window_dump.xml`, `cat`s it back, and strips noisy attributes (`index`, `package`, `class`, `checkable`, …) to slim the LLM's context.
* **`findUiElementsByText(text, selectorType)`** — builds a regex against the requested attribute(s) plus `bounds="[x1,y1][x2,y2]"`, finds all matches, computes the tap center `( (x1+x2)/2, (y1+y2)/2 )`, returns `List<UiMatchResult>`.
* **`tapByText(matches, position)`** — `adb shell input tap <cx> <cy>` on the chosen match.
* **`tapByCoordinates(x, y)`** — same, raw coordinates.
* **`inputTextBySelector(selector, text, selectorType)`** — tap the field, then `adb shell input text` with spaces encoded as `%s` (a quirk of `input text`).
* **`scrollScreenVertically(distance, durationMs)`** / **`scrollScreenHorizontally(...)`** — adaptive swipe: start coords are 75% or 25% of the screen dimension depending on direction sign, end coords are clamped to the screen.
* **`getScreenSize()`** — parses `WxH` from `wm size`.

### `MediaUtils` — screenshots & screen recording

[agent/tool/mobile/test/utils/MediaUtils.kt](../src/main/kotlin/agent/tool/mobile/test/utils/MediaUtils.kt):

* Reads `HOME_PATH` from `.env`.
* Keeps a monotonic `screenshotIndex` per process so filenames don't collide.
* **`takeScreenshot(goalName)`** — `screencap -p /sdcard/screen-<i>.png` → `adb pull` to `$HOME_PATH/<slug-goalName>-<i>.png`.
* **`startScreenRecording()`** — spawns `adb shell screenrecord /sdcard/video.mp4`, keeps the `Process` reference.
* **`stopScreenRecording()`** — `process.destroy()`, sleeps 1s, pulls the video.

### `Formatter`

Single extension function `String.formatToSlug()` — lowercases, strips non-alphanumerics (except spaces), trims, and replaces runs of whitespace with a single hyphen. Used by `MediaUtils` to build screenshot filenames.

### `UiMatchResult`

```kotlin
@Serializable
data class UiMatchResult(
    val value: String, // matched attribute value (text / content-desc / resource-id)
    val cx: Int,       // x of tap center
    val cy: Int        // y of tap center
)
```

`@Serializable` so it can be JSON-encoded for the LLM to read.

---

## 5. Adding a new tool

1. Add a method to `MobileTestTools` (or a new `ToolSet` class) with both annotations:
   ```kotlin
   @Tool
   @LLMDescription(
       "One-sentence summary of what this does, followed by return-value conventions. " +
       "Mention status prefixes (OK / NOT_FOUND / ...) so the LLM knows how to parse the result."
   )
   fun myTool(
       @LLMDescription("What this argument means.") arg: String
   ): String { ... }
   ```
2. Return a `String` starting with one of the [status prefixes](#1-status-prefix-convention).
3. **No registry changes** — `MobileTestAgent` builds the registry with `tools(MobileTestTools())`, and Koog's reflection-based `ToolSet` picks up all `@Tool` methods on the class.

### Guidelines

* **Keep descriptions short but explicit.** The `@LLMDescription` text is the LLM's *only* documentation for the tool. Mention the return-value contract.
* **Return strings, not exceptions.** If something fails, return `"ERROR: <reason>"`. Throwing crashes the agent run.
* **One responsibility per tool.** It's tempting to bundle "tap and verify" — don't. The act/verify split is what makes the per-step loop work.
* **Bound timing.** Use `coerceIn` for any ms/iteration argument (like `wait`).
