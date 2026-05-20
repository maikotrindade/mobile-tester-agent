# Dependencies

Every third-party library shipped in this project, with a one-line description of why it's here. The stack supports test runs across **Android, iOS, and React Native**. Sources of truth:

* Backend: [build.gradle.kts](../build.gradle.kts) + [gradle.properties](../gradle.properties)
* Frontend: [web/package.json](../web/package.json)

---

## Backend (JVM)

Built on **JDK 21** (Kotlin `jvmToolchain(21)`) with **Gradle 8.11+** and **Kotlin 2.3.0**.

### Production dependencies

| Library | Version | Role |
|---|---|---|
| **[Koog Agents](https://docs.koog.ai)** (`ai.koog:koog-agents`) | `0.8.0` | The agentic framework. Provides `AIAgent`, strategy DSL, `@Tool` reflection, executors for major LLM providers (Google, Anthropic, DeepSeek, OpenRouter, Ollama), and event handlers. The single most important dependency in this project |
| **[Ktor Server Core](https://ktor.io)** | `3.1.3` | HTTP framework — application lifecycle, routing, request/response primitives |
| **Ktor Server Netty** | `3.1.3` | Netty-backed engine that actually accepts connections on `:8080` |
| **Ktor Server YAML Config** | `3.1.3` | Loads [application.yaml](../src/main/resources/application.yaml) (modules, deployment port) |
| **Ktor Server Content Negotiation** | `3.1.3` | Plumbs the JSON serializer into request/response bodies |
| **Ktor Serialization kotlinx-json** | `3.1.3` | `kotlinx.serialization` adapter — pretty-prints, lenient, ignores unknown keys |
| **Ktor Server Call Logging** | `3.1.3` | Logs every incoming HTTP call at `INFO` |
| **[Logback Classic](https://logback.qos.ch/)** | `1.4.14` | The SLF4J implementation; configured via [logback.xml](../src/main/resources/logback.xml) |
| **[dotenv-kotlin](https://github.com/cdimascio/dotenv-kotlin)** | `6.4.1` | Loads `.env` from the project root for API keys (`GEMINI_API_KEY`, `CLAUDE_API_KEY`, `DEEP_SEEK_KEY`, `OPEN_ROUTER`, `HOME_PATH`) |

### Kotlin plugins

| Plugin | Version | Role |
|---|---|---|
| `kotlin("jvm")` | `2.3.0` | Kotlin JVM compilation |
| `kotlin("plugin.serialization")` | `2.3.0` | Generates serializers for `@Serializable` data classes (`AgentRequest`, `MobileTesterConfigAPI`, `UiMatchResult`, `TestScenarioReport`) |
| `io.ktor.plugin` | `3.1.3` | Ktor Gradle plugin — fat-jar packaging and the `./gradlew run` task |

### Runtime tooling (not a dependency, but required)

* **`adb` (Android Debug Bridge)** — every device action is a child process of `adb`. The host machine must have Android Platform Tools installed and an emulator/device discoverable via `adb devices`. See [AdbUtils.kt](../src/main/kotlin/agent/tool/mobile/test/utils/AdbUtils.kt).
* **Android `uiautomator`** — invoked over `adb shell uiautomator dump` to extract the UI hierarchy XML. Ships on every Android device.
* **`screencap` / `screenrecord`** — standard Android binaries used by [MediaUtils.kt](../src/main/kotlin/agent/tool/mobile/test/utils/MediaUtils.kt).

---

## Frontend (web)

Built with **Node 18+** and **TypeScript 5.8**.

### Production dependencies

| Library | Version | Role |
|---|---|---|
| **[React](https://react.dev/)** | `^19.1.1` | UI library |
| **react-dom** | `^19.1.1` | DOM renderer for React |
| **[react-router-dom](https://reactrouter.com/)** | `^7.8.0` | Client-side routing (`/`, `/settings`, `/about`) |
| **[axios](https://axios-http.com/)** | `^1.11.0` | HTTP client used to POST `/api/run-test` with a 3-min timeout and Axios-aware error handling |
| **[Firebase](https://firebase.google.com/)** | `^12.1.0` | Firestore — persists user-authored test scenarios for live sync across reloads. Initialised in [firebase.ts](../web/src/firebase.ts) |
| **[mermaid](https://mermaid.js.org/)** | `^11.9.0` | Renders the architecture diagram on the About page; re-initialised with theme-aware palette colours |

### Dev dependencies

| Library | Version | Role |
|---|---|---|
| **[Vite](https://vite.dev/)** | `^7.1.0` | Dev server (with the `/api` → `:8080` proxy) and production bundler |
| **@vitejs/plugin-react** | `^4.7.0` | React Fast Refresh + JSX transform |
| **TypeScript** | `~5.8.3` | Type checking (`tsc -b` runs before `vite build`) |
| **@types/react / @types/react-dom** | `^19.1.x` | Type definitions |
| **ESLint** | `^9.32.0` | Linting via the new flat-config format |
| **@eslint/js** | `^9.32.0` | Bundled ESLint recommended rules |
| **typescript-eslint** | `^8.39.0` | TS-aware lint rules |
| **eslint-plugin-react-hooks** | `^5.2.0` | Catches Hook-rule violations |
| **eslint-plugin-react-refresh** | `^0.4.20` | Warns on non-component exports that break Fast Refresh |
| **globals** | `^16.3.0` | Standard global variable definitions for ESLint envs |

---

## Where versions are pinned

* **JVM versions** live in [gradle.properties](../gradle.properties):
  ```
  kotlin_version=2.1.10
  ktor_version=3.1.3
  logback_version=1.4.14
  koog_agents_version=0.8.0
  dotenv_kotlin_version=6.4.1
  ```
  > Note: the `kotlin_version` property exists for legacy reasons; the actual Kotlin plugin in [build.gradle.kts](../build.gradle.kts) is `2.3.0`.
* **Frontend versions** are pinned in [web/package.json](../web/package.json) and locked by [web/package-lock.json](../web/package-lock.json).

---

## Why Koog?

Koog ships with everything we needed off the shelf:

* `AIAgent` + strategy DSL → no need to hand-roll the LLM ↔ tool loop.
* `@Tool` / `@LLMDescription` reflection → tools are auto-described to the LLM from their Kotlin signatures.
* Built-in executors for Google (Gemini), Anthropic (Claude), DeepSeek, OpenRouter (GPT-4 and others), and Ollama → swapping providers is a one-line change.
* `nodeLLMCompressHistory` → context-window management as a node, not a special case.
* Event handlers → simple `onAgentStarting` / `onToolCallStarting` / `onAgentCompleted` callbacks for logging and result capture.

The trade-off is coupling: anything strange about Koog's behaviour is also strange about ours. So far that has been a net win.
