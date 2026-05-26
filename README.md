# Mobile Tester Agent

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![JDK](https://img.shields.io/badge/JDK-21-007396?logo=openjdk&logoColor=white)](https://adoptium.net)
[![Ktor](https://img.shields.io/badge/Ktor-3.1.3-087CFA?logo=ktor&logoColor=white)](https://ktor.io)
[![Koog](https://img.shields.io/badge/Koog-0.8.0-FF6F00)](https://docs.koog.ai)
[![Gradle](https://img.shields.io/badge/Gradle-8.11+-02303A?logo=gradle&logoColor=white)](https://gradle.org)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)](https://react.dev)
[![Vite](https://img.shields.io/badge/Vite-7-646CFF?logo=vite&logoColor=white)](https://vitejs.dev)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.8-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org)
[![Firebase](https://img.shields.io/badge/Firebase-Firestore-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com)
[![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![iOS](https://img.shields.io/badge/iOS-000000?logo=apple&logoColor=white)](https://developer.apple.com/ios)
[![React Native](https://img.shields.io/badge/React_Native-61DAFB?logo=react&logoColor=black)](https://reactnative.dev)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/maikotrindade/mobile-tester-agent/pulls)

AI-powered UI test automation for **Android, iOS, and React Native** — author scenarios in natural language, let a [Koog](https://docs.koog.ai)-powered LLM agent drive a real device or emulator.

![ai-agentic-mobile-tester (1)](https://github.com/user-attachments/assets/2042cf29-4207-4959-a6c1-e5d0c7d7e373)

---

## Get Started

**Prerequisites:** JDK 21, Android Platform Tools (`adb`), Node 18+ for the dashboard, one LLM API key (DeepSeek, Gemini, Claude, OpenRouter, or local Ollama).

```bash
# 1. Configure environment
cp .env.example .env          # then fill in at least one LLM key + HOME_PATH

# 2. One-shot: backend (:8080) + dashboard (:5173) + open browser
./start.sh

# — or run them separately —
./gradlew run                                  # backend only
cd web && npm install && npm run dev           # dashboard only
```

Trigger a test scenario:

```bash
curl -X POST http://localhost:8080/run-test \
  -H "Content-Type: application/json" \
  -d '{
    "goal": "Verify the login flow",
    "packageName": "com.example.myapp",
    "steps": [
      "Tap Login",
      "Enter username test@example.com",
      "Enter password hunter2",
      "Tap Submit",
      "Verify Profile screen is visible"
    ]
  }'
```

> `packageName` is the platform-native identifier of the app under test — Android package id, iOS bundle id, or the bundle of a React Native app. It is supplied by the caller (or set in the dashboard's **Settings → App Identifier** field); there is no hardcoded default.
Full walkthrough: [docs/getting-started.md](docs/getting-started.md).

---

## Architecture

A Kotlin/Ktor server receives a scenario, a Koog `AIAgent` reasons step-by-step, and a platform-aware tool layer drives the device under test.

```mermaid
flowchart LR
    U[User] -->|writes scenario| FE[Web Dashboard<br/>React + Vite]
    FE -->|POST /run-test| API[Ktor HTTP API]
    API --> AGENT[MobileTestAgent<br/>Koog AIAgent]
    AGENT <-->|reasoning| LLM[(LLM<br/>DeepSeek / Gemini /<br/>Claude / OpenRouter / Ollama)]
    AGENT -->|Tool calls| TOOLS[MobileTestTools<br/>platform-aware]
    TOOLS --> AND[Android<br/>device / emulator]
    TOOLS --> IOS[iOS<br/>device / simulator]
    TOOLS --> RN[React Native<br/>app on Android or iOS]
    AGENT -->|result| API
    API -->|JSON| FE
    FE -.->|scenarios| FS[(Firebase Firestore)]
```

Detailed design lives in [docs/](docs/):

| Doc | What it covers |
|---|---|
| [Architecture](docs/architecture.md) | Runtime topology, request lifecycle, module map |
| [Getting Started](docs/getting-started.md) | Prerequisites, env vars, first test run |
| [HTTP API](docs/api.md) | `POST /run-test`, `POST /stop-test`, `POST /config`, payloads and errors |
| [AI Agent](docs/ai-agent.md) | Strategy graph, system prompt, LLM executors |
| [Tools](docs/tools.md) | Full `MobileTestTools` catalog and ADB/UiAutomator utilities |
| [Frontend (web)](docs/frontend.md) | React 19 + Vite dashboard, theming, i18n, Firestore |
| [Dependencies](docs/dependencies.md) | Every third-party library used in backend and frontend |

---

## References

- [Koog Documentation](https://docs.koog.ai) — the agentic framework powering the LLM ↔ tool loop
- [Building an Agentic AI Mobile Tester with Koog and Kotlin](https://maikotrindade.com/ai/kotlin/android/development/agents/2025/08/19/building-agentic-ai-mobile-tester-koog-kotlin.html) — the blog post behind this project
- [mobile-tester-agent-sample-app](https://github.com/maikotrindade/mobile-tester-agent-sample-app) — sample Android app under test
- [How to Actually Test Autonomous AI Agents](https://maikotrindade.com/ai-agents/testing/2026/05/20/testing-autonomous-ai-agents.html)
---

## License

Released under the [MIT License](LICENSE). © 2026 Maiko Trindade.
