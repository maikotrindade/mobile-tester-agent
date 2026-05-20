# Mobile Tester Agent — Documentation

The **Mobile Tester Agent** is an AI-powered mobile test automation system covering **Android, iOS, and React Native**. A Kotlin/Ktor backend exposes an HTTP API that delegates natural-language test scenarios to a [Koog](https://docs.koog.ai)-powered LLM agent. The agent reasons step-by-step and drives a real device or simulator on the target platform, while a React/Vite web dashboard lets users author scenarios, configure the LLM, and trigger runs.

### Supported platforms

| Platform | Targets |
|---|---|
| **Android** | Physical devices and emulators |
| **iOS** | Physical devices and simulators |
| **React Native** | Cross-platform RN apps running on either Android or iOS |

> Reference write-up: *[Building an Agentic AI Mobile Tester with Koog and Kotlin](https://maikotrindade.com/ai/kotlin/android/development/agents/2025/08/19/building-agentic-ai-mobile-tester-koog-kotlin.html)* — the original blog post that inspired this project.

---

## Documentation index

| Document | What it covers |
|---|---|
| [Architecture](architecture.md) | High-level system map, request lifecycle, runtime topology, mermaid diagrams |
| [Getting Started](getting-started.md) | Prerequisites, environment setup, running backend + frontend, first test run |
| [HTTP API](api.md) | `POST /run-test`, `POST /config`, payload shapes, status codes, examples |
| [AI Agent](ai-agent.md) | `MobileTestAgent`, the Koog strategy graph, system prompt design, LLM executors |
| [Tools](tools.md) | The full `MobileTestTools` & `ReportingTools` catalog and the ADB / UiAutomator / Media utilities behind them |
| [Frontend (web)](frontend.md) | React 19 + Vite dashboard, pages, theming, i18n, Firestore persistence, dev proxy |
| [Dependencies](dependencies.md) | Every third-party library used in backend and frontend, with one-line descriptions |

---

## TL;DR

```mermaid
flowchart LR
    U[User] -->|writes scenario| FE[Web Dashboard<br/>React + Vite]
    FE -->|POST /run-test| API[Ktor HTTP API]
    API --> AGENT[MobileTestAgent<br/>Koog AIAgent]
    AGENT <-->|reasoning| LLM[(LLM<br/>Gemini / DeepSeek /<br/>Claude / OpenRouter / Ollama)]
    AGENT -->|@Tool calls| TOOLS[MobileTestTools<br/>platform-aware]
    TOOLS --> AND[Android<br/>device / emulator]
    TOOLS --> IOS[iOS<br/>device / simulator]
    TOOLS --> RN[React Native<br/>app on Android or iOS]
    AGENT -->|result| API
    API -->|JSON| FE
    FE -.->|scenarios| FS[(Firebase Firestore)]
```

1. The user defines a **goal** and a list of **natural-language steps** in the dashboard.
2. The frontend POSTs the scenario to the Ktor server.
3. `MobileTestAgent` (a Koog `AIAgent`) interprets each step, calls `@Tool`-annotated functions, and verifies the result before continuing.
4. The tools layer drives the device on the target platform — **Android, iOS, or React Native** — and reads UI state back.
5. After `closeApp`, the agent emits a PASS/FAIL summary per step, which is returned to the dashboard.

For the deeper "how" — see [architecture.md](architecture.md) and [ai-agent.md](ai-agent.md).
