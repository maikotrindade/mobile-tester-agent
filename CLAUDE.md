# mobile-tester-agent

AI-powered mobile UI test automation for **Android, iOS, and React Native**. A Kotlin/Ktor backend exposes an HTTP API that hands natural-language scenarios to a [Koog](https://docs.koog.ai)-powered LLM agent, which drives a real device or emulator. A React/Vite dashboard under [web/](web/) lets users author scenarios and trigger runs.

## Where to find things

Full documentation lives in [docs/](docs/). Start there for anything non-trivial.

| Doc | What it covers |
|---|---|
| [docs/architecture.md](docs/architecture.md) | Runtime topology, request lifecycle, module map |
| [docs/getting-started.md](docs/getting-started.md) | Prerequisites, env, first run |
| [docs/api.md](docs/api.md) | `POST /run-test`, `POST /config` payloads + errors |
| [docs/ai-agent.md](docs/ai-agent.md) | `MobileTestAgent`, strategy graph, system prompt, executors |
| [docs/tools.md](docs/tools.md) | Tool catalog, status-prefix convention, utility layer |
| [docs/frontend.md](docs/frontend.md) | React/Vite dashboard internals |
| [docs/dependencies.md](docs/dependencies.md) | Every third-party library, by reason |

## Project layout

```
src/main/kotlin/
├── server/                    # Ktor: Application, Routing, HTTP, Monitoring
│   └── model/                 # AgentRequest, MobileTesterConfigAPI (wire DTOs)
└── agent/
    ├── MobileTestAgent.kt     # Singleton — builds & runs the Koog AIAgent
    ├── strategy/              # TestingStrategy.kt — Koog graph
    ├── executor/              # One file per LLM provider (ExecutorInfo impls)
    ├── model/                 # MobileTesterConfig, TestScenarioReport
    └── tool/
        ├── mobile/test/       # MobileTestTools, ReportingTools, utils/
        └── reporting/         # ReportingTools scaffolding (not yet wired)
web/                           # React 19 + Vite dashboard
docs/                          # Source of truth for design + API docs
```

## Backend stack

- **JDK 21** (Amazon Corretto), Kotlin **2.3.0**, Gradle wrapper (8.11+)
- **Ktor 3.1.3** (Netty) + `kotlinx.serialization`
- **Koog Agents 0.8.0** — `AIAgent`, strategy DSL, `@Tool` reflection
- **dotenv-kotlin** — loads `.env` from project root

## Frontend stack

- **React 19 + react-router-dom 7** on **Vite 7 + TypeScript 5.8**
- **axios** for `POST /run-test`, native `fetch` for `/config`
- **Firebase Firestore** for scenario persistence
- **mermaid** for architecture diagram on About page
- Vite dev server proxies `/api/*` → `http://localhost:8080`

## LLM executors

All implement `ExecutorInfo` in `agent/executor/`. Selected via `POST /config` (`executorInfoId` string).

| `executorInfoId` | Class | Env var |
|---|---|---|
| `deepseek` *(default)* | `DeepSeekExecutor` (DeepSeek V4 Flash) | `DEEP_SEEK_KEY` |
| `gemini` | `GeminiExecutor` (Gemini 2.5 Flash) | `GEMINI_API_KEY` |
| `haiku` | `HaikuExecutor` (Claude Haiku 4.5) | `CLAUDE_API_KEY` |
| `open_router` | `OpenRouterExecutor` (GPT-4) | `OPEN_ROUTER` |
| `ollama_llama` | `OllamaLlamaExecutor` (local) | — |
| `ollama_gwen` | `OllamaGwenExecutor` (local) | — |

## Critical conventions

These behaviors are load-bearing — see [docs/ai-agent.md](docs/ai-agent.md) and [docs/tools.md](docs/tools.md) for the full reasoning.

- **Status-prefixed tool returns.** Every `@Tool` returns a `String` starting with one of `OK | TAPPED | VISIBLE | NOT_VISIBLE | NOT_FOUND | AMBIGUOUS | ERROR | TIMEOUT`. The system prompt tells the LLM to pattern-match the prefix. Preserve this when adding tools — return strings, never throw.
- **Device serial pinning.** `AdbUtils.runAdb()` injects `-s <serial>` after `connectDevice()` picks a target. Don't bypass it with raw `ProcessBuilder("adb", …)` calls.
- **`MAX_TOKENS_THRESHOLD = 8000`** in `TestingStrategy.kt`. Lower values trigger compression too aggressively and the agent forgets which step it's on.
- **`startTestingScenario` once, first; `closeApp` once, last.** The system prompt forbids any "tap launcher to open the app" recovery — launch is handled programmatically.
- **`hideKeyboard` uses `KEYCODE_BACK` (4)**, not `KEYCODE_ESCAPE`. Empirically required on the target device; documented inline.

## Common commands

```bash
./gradlew run                       # Start backend on :8080
./gradlew compileKotlin             # Type-check Kotlin
adb devices                         # Confirm device/emulator visibility

cd web && npm install               # Frontend deps
cd web && npm run dev               # Vite dev server on :5173 (proxies /api → :8080)
cd web && npm run build             # tsc -b && vite build
cd web && npm run lint              # ESLint
```

## Environment

`.env` at project root (loaded by `dotenv-kotlin`, gitignored). See [.env.example](.env.example) for the template. At minimum one LLM key plus `HOME_PATH` (where screenshots/recordings are pulled to). For the dashboard, also `VITE_FIREBASE_*` keys.

## Adding things

- **New `@Tool`** — add a method to `MobileTestTools` with `@Tool` + `@LLMDescription`. Koog reflection picks it up; no registry edits needed. Return a status-prefixed string.
- **New executor** — implement `ExecutorInfo` in `agent/executor/`, then add a `when`-branch in `MobileTesterConfigAPI.toMobileConfig()` and (optionally) an `<option>` in `web/src/pages/settings/Settings.tsx`.

## Related repos

- Sample Android app under test: [mobile-tester-agent-sample-app](https://github.com/maikotrindade/mobile-tester-agent-sample-app)
- Background reading: [Building an Agentic AI Mobile Tester with Koog and Kotlin](https://maikotrindade.com/ai/kotlin/android/development/agents/2025/08/19/building-agentic-ai-mobile-tester-koog-kotlin.html)
