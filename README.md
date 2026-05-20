# Mobile Tester Agent

An **AI-powered mobile test automation** system covering **Android, iOS, and React Native**. A Kotlin/Ktor backend exposes an HTTP API that hands natural-language test scenarios to a [Koog](https://docs.koog.ai)-powered LLM agent. The agent reasons step-by-step and drives a real device or emulator on the target platform. A React/Vite dashboard lets users author scenarios, configure the LLM, and trigger runs.

![ai-agentic-mobile-tester (1)](https://github.com/user-attachments/assets/2042cf29-4207-4959-a6c1-e5d0c7d7e373)

> Background reading: [Building an Agentic AI Mobile Tester with Koog and Kotlin](https://maikotrindade.com/ai/kotlin/android/development/agents/2025/08/19/building-agentic-ai-mobile-tester-koog-kotlin.html) — the blog post behind this project.

---

## Documentation

Full documentation lives in [docs/](docs/):

| Doc | What it covers |
|---|---|
| [docs/README.md](docs/README.md) | Index + 30-second overview |
| [Architecture](docs/architecture.md) | System map, request lifecycle, mermaid diagrams |
| [Getting Started](docs/getting-started.md) | Setup, env vars, first test run |
| [HTTP API](docs/api.md) | `POST /run-test`, `POST /config`, payloads and errors |
| [AI Agent](docs/ai-agent.md) | Koog `AIAgent`, strategy graph, system prompt, executors |
| [Tools](docs/tools.md) | Full `MobileTestTools` catalog + the ADB/UiAutomator utilities |
| [Frontend (web)](docs/frontend.md) | React 19 + Vite dashboard, theming, i18n, Firestore |
| [Dependencies](docs/dependencies.md) | Every third-party library used in backend and frontend |

---

## Quick start

```bash
# 1. Set up .env (LLM key + HOME_PATH for artifacts)
cp .env.example .env

# 2. Start the backend
./gradlew run

# 3. (Optional) start the dashboard in another terminal
cd web && npm install && npm run dev
```

Then trigger a test:

```bash
curl -X POST http://localhost:8080/run-test \
  -H "Content-Type: application/json" \
  -d '{
    "goal": "Verify the login flow",
    "packageName": "io.githib.maikotrindade.appfortesting",
    "steps": ["Tap Login", "Enter username test@example.com", "Enter password hunter2", "Tap Submit", "Verify Profile screen is visible"]
  }'
```

> `packageName` is the platform-native app identifier — Android package id, iOS bundle id, or the bundle/package of a React Native app.

See [docs/getting-started.md](docs/getting-started.md) for the full walkthrough.

---

## Platforms

The Mobile Tester Agent is designed to drive UI tests across the three dominant mobile stacks:

| Platform | Target |
|---|---|
| **Android** | Physical devices and emulators |
| **iOS** | Physical devices and simulators |
| **React Native** | The same scenarios run against the native shell on either OS |

A single test scenario is authored in natural language and the agent picks the right tool calls for the platform under test.

---

## Related repositories

* Sample Android app under test: [mobile-tester-agent-sample-app](https://github.com/maikotrindade/mobile-tester-agent-sample-app)
