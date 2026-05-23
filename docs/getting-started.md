# Getting Started

A practical walkthrough: from a fresh clone to running your first AI-driven mobile test. The agent runs scenarios for **Android, iOS, and React Native** apps from the same backend and dashboard. The setup below walks through running an Android scenario as a first example.

---

## 1. Prerequisites

| Tool | Version | Why |
|---|---|---|
| **JDK** | 21 (Amazon Corretto recommended) | The Kotlin toolchain targets JDK 21 — see `kotlin { jvmToolchain(21) }` in [build.gradle.kts](../build.gradle.kts) |
| **Gradle Wrapper** | shipped (`./gradlew`) | Don't install Gradle globally |
| **Android Platform Tools** | latest | Provides `adb`. On Linux: `sudo apt install adb`, on macOS: `brew install --cask android-platform-tools` |
| **Android device or emulator** | API 26+ | Must be visible to `adb devices`. The agent prefers emulators when multiple are attached |
| **Node.js** | 18+ | Required only if you want to run the frontend |
| **An LLM API key** | — | At least one of: `GEMINI_API_KEY`, `CLAUDE_API_KEY`, `DEEP_SEEK_KEY`, `OPEN_ROUTER`. Or run Ollama locally |

### Verify Android tooling

```bash
adb devices
# List of devices attached
# emulator-5554   device
```

If the device shows `offline`, run `adb kill-server && adb start-server`. The backend does this automatically on `connectDevice()` too.

---

## 2. Clone & configure environment

```bash
git clone https://github.com/maikotrindade/mobile-tester-agent.git
cd mobile-tester-agent
```

Create a `.env` at the project root (loaded automatically by [dotenv-kotlin](https://github.com/cdimascio/dotenv-kotlin)):

```dotenv
# At least one LLM key
GEMINI_API_KEY=...
CLAUDE_API_KEY=...
DEEP_SEEK_KEY=...
OPEN_ROUTER=...

# Where screenshots / recordings are pulled to
HOME_PATH=/home/<you>/mobile-tester-artifacts
```

`HOME_PATH` must exist and be writable — [MediaUtils.kt](../src/main/kotlin/agent/tool/mobile/test/utils/MediaUtils.kt) pulls files into it.

---

## 3. Run the backend

```bash
./gradlew run
```

The Ktor server binds to `http://localhost:8080` (see [application.yaml](../src/main/resources/application.yaml)). Expect:

```
... Application started in X.XX seconds.
... Responding at http://0.0.0.0:8080
```

In IntelliJ, use the `mobile-tester-agent` run configuration which runs the same Gradle `run` task.

### Sanity-check the API

```bash
curl -X POST http://localhost:8080/config \
  -H "Content-Type: application/json" \
  -d '{"executorInfoId":"gemini","llmTemperature":0.0,"maxAgentIterations":50,"logTokensConsumption":false}'
# → Configuration updated successfully
```

---

## 4. Run the frontend (optional)

The dashboard lives in [web/](../web/).

```bash
cd web
npm install
```

Create `web/.env.local` with your Firebase project credentials (the Home page persists scenarios in Firestore):

```dotenv
VITE_FIREBASE_API_KEY=...
VITE_FIREBASE_AUTH_DOMAIN=...
VITE_FIREBASE_PROJECT_ID=...
VITE_FIREBASE_STORAGE_BUCKET=...
VITE_FIREBASE_MESSAGING_SENDER_ID=...
VITE_FIREBASE_APP_ID=...
```

> If you don't have a Firestore project, you can still run the app — the Home page will just fail to subscribe to `testScenarios`. You can author and run scenarios in-memory.

Start the dev server:

```bash
npm run dev
# → http://localhost:5173
```

The Vite dev proxy forwards `/api/*` to `http://localhost:8080` (see [vite.config.ts](../web/vite.config.ts)) — no CORS configuration needed for local dev.

---

## 5. First test run

You have two ways to trigger a run.

### Via the dashboard

1. Open `http://localhost:5173`.
2. Settings → pick a model (e.g. `Gemini 2.0 Flash`), set **App Identifier** to the package id / bundle id of the app under test (this is required — there is no default), **Save**.
3. Home → write a goal (e.g. *"Verify the create-post flow"*), add steps (one line each), **Run Test**.
4. Wait for the PASS/FAIL summary.

### Via `curl`

```bash
curl -X POST http://localhost:8080/run-test \
  -H "Content-Type: application/json" \
  -d '{
    "goal": "Open the app and verify the home screen loads",
    "packageName": "com.example.myapp",
    "steps": [
      "Verify the home screen heading is visible"
    ]
  }'
```

The backend will:

1. Connect to the device (`AdbUtils.connectDevice`).
2. Wake the screen, dismiss the keyguard, force-stop any stale instance, `monkey`-launch the package, verify foreground.
3. Run the LLM ↔ tool loop until each step is verified.
4. `closeApp`.
5. Return a PASS/FAIL summary as the HTTP response body.

---

## 6. Where things show up

* **Logs** stream to stdout (and `app.log` for some runs). Look for `Tool called: tool <name>, args ...` lines to follow the agent's reasoning.
* **Screenshots** land in `$HOME_PATH/<slug-of-goal>-<index>.png`.
* **Screen recordings** land in `$HOME_PATH/video.mp4` when `stopScreenRecording` is called.

---

## 7. Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `Configuration updated successfully` but `POST /run-test` returns `ERROR: No devices connected.` | No `adb devices` entries | Start your emulator or plug in a phone with USB debugging on |
| `ERROR: launched '<pkg>' but foreground is '<other>' after retry` | The package id is wrong, or the device launcher intercepted the launch | Verify with `adb shell pm list packages \| grep <fragment>` |
| Agent loops `NOT_FOUND` on a button you can see | Selector mismatch (case, partial text, content-desc vs text) | Add the actual selector to the step description; consider exposing `getScreenDump` to the LLM |
| `Error running adb …: Cannot run program "adb"` | `adb` not on PATH | Install Android Platform Tools and ensure `adb` resolves |
| Frontend can't reach backend | Vite proxy points elsewhere or backend not running | `curl http://localhost:8080/config` to confirm; otherwise restart `./gradlew run` |
| Settings page can't save | Backend not running | The save uses `fetch('/api/config')` — same proxy path |

For more on what each tool returns and how the agent recovers, see [tools.md](tools.md) and [ai-agent.md](ai-agent.md).
