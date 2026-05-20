# HTTP API

The backend exposes a tiny Ktor HTTP surface — two endpoints. JSON in, JSON/text out. The same endpoints drive **Android, iOS, and React Native** test runs; the platform is implied by the `packageName` you submit.

Defined in [server/Routing.kt](../src/main/kotlin/server/Routing.kt) and bound to port `8080` by [src/main/resources/application.yaml](../src/main/resources/application.yaml).

Content negotiation uses `kotlinx.serialization` (pretty-printed, lenient, ignores unknown keys) — see [server/HTTP.kt](../src/main/kotlin/server/HTTP.kt).

---

## `POST /run-test`

Runs a single test scenario on the connected device. **Synchronous** — the response returns only after the agent's `closeApp` + final summary, so set a generous client timeout (the frontend uses 3 minutes).

### Request body

| Field | Type | Required | Description |
|---|---|---|---|
| `goal` | string | yes | Human-readable objective of the test |
| `packageName` | string | yes | Platform-native app identifier — Android package id, iOS bundle id, or the bundle/package of a React Native app (e.g. `io.githib.maikotrindade.appfortesting`) |
| `steps` | string[] | yes | Ordered list of natural-language steps the agent will execute |

Defined in [server/model/AgentRequest.kt](../src/main/kotlin/server/model/AgentRequest.kt).

### Example

```bash
curl -X POST http://localhost:8080/run-test \
  -H "Content-Type: application/json" \
  -d '{
    "goal": "Verify login works with valid credentials",
    "packageName": "io.githib.maikotrindade.appfortesting",
    "steps": [
      "Tap the Login button on the home screen",
      "Enter username test@example.com",
      "Enter password hunter2",
      "Tap Submit",
      "Verify the Profile screen is visible"
    ]
  }'
```

### Successful response

`200 OK`, `text/plain` — the body is the agent's final assistant message, prefixed with `onAgentFinished: `:

```
onAgentFinished: Step 1: PASS — Login button tapped, login screen shown.
Step 2: PASS — username entered.
Step 3: PASS — password entered.
Step 4: PASS — Submit tapped, profile screen appeared.
Step 5: PASS — Profile heading visible.
```

The format is enforced by the system prompt — see [ai-agent.md](ai-agent.md#system-prompt).

### Error responses

| Status | When | Body |
|---|---|---|
| `400 Bad Request` | `goal` blank | `Missing goal` |
| `400 Bad Request` | `packageName` blank | `Missing packageName` |
| `400 Bad Request` | `steps` empty | `Missing steps` |
| `500 Internal Server Error` | Agent throws | `Error: <ExceptionClass>: <message>` |

---

## `POST /config`

Updates the agent's runtime configuration. Survives until the JVM restarts.

### Request body

| Field | Type | Default | Description |
|---|---|---|---|
| `executorInfoId` | string | `"gemini"` | One of: `gemini`, `deepseek`, `haiku`, `open_router`, `ollama_gwen`, `ollama_llama` |
| `llmTemperature` | double | `0.0` | Forwarded to `LLMParams.temperature`. `0` = deterministic |
| `maxAgentIterations` | int | `50` | Hard cap on agent reasoning steps before it gives up |
| `logTokensConsumption` | boolean | `false` | Currently a config flag only — wire-up TBD |

Defined in [server/model/MobileTesterConfigAPI.kt](../src/main/kotlin/server/model/MobileTesterConfigAPI.kt). The `toMobileConfig()` extension function maps `executorInfoId` strings to concrete `ExecutorInfo` instances:

```kotlin
"haiku"         -> HaikuExecutor()
"deepseek"      -> DeepSeekExecutor()
"gemini"        -> GeminiExecutor()
"ollama_gwen"   -> OllamaGwenExecutor()
"ollama_llama"  -> OllamaLlamaExecutor()
"open_router"   -> OpenRouterExecutor()
```

An unknown id triggers `400 Bad Request` with `Invalid configuration: Unknown executorInfoId: <id>`.

### Example

```bash
curl -X POST http://localhost:8080/config \
  -H "Content-Type: application/json" \
  -d '{
    "executorInfoId": "haiku",
    "llmTemperature": 0.0,
    "maxAgentIterations": 80,
    "logTokensConsumption": true
  }'
```

### Successful response

`200 OK`, body: `Configuration updated successfully`

### Error response

| Status | When | Body |
|---|---|---|
| `400 Bad Request` | Deserialization failure or unknown executor | `Invalid configuration: <reason>` |

---

## Calling the API from the frontend

In development, the frontend hits `/api/run-test` and `/api/config`. Vite proxies them to `http://localhost:8080` and rewrites the `/api` prefix — see [web/vite.config.ts](../web/vite.config.ts):

```ts
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

This avoids CORS configuration on the backend during local dev. For deployments, point `apiBaseUrl` in the Settings page at the real backend URL.
