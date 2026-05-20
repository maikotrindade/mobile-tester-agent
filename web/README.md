# Mobile Tester Agent — Web Dashboard

The browser dashboard for the [Mobile Tester Agent](../README.md). Authors natural-language test scenarios, configures the LLM, and dispatches runs to the Ktor backend.

For the full design — pages, state, routing, theming, i18n, dev proxy — see [../docs/frontend.md](../docs/frontend.md).

---

## Stack

- **React 19** + **react-router-dom 7**
- **Vite 7** + **TypeScript 5.8**
- **axios** (`POST /run-test`) + native `fetch` (`POST /config`)
- **Firebase Firestore** — persists user-authored scenarios
- **mermaid** — renders the architecture diagram on the About page
- Custom lightweight i18n (English / French) and a `data-theme` light/dark switch

## Routes

| Path | Purpose |
|---|---|
| `/` | Author scenarios, list saved ones, run tests |
| `/settings` | LLM / agent configuration (model, temperature, iterations) |
| `/about` | System overview + mermaid architecture diagram |

## Getting started

```bash
npm install
npm run dev          # http://localhost:5173, proxies /api → :8080
npm run build        # tsc -b && vite build
npm run lint         # ESLint over the workspace
npm run preview      # Serve the built bundle locally
```

The Vite dev proxy forwards `/api/*` to `http://localhost:8080`, so the backend must be running for end-to-end tests. See [../docs/getting-started.md](../docs/getting-started.md) for the full setup.

## Environment

Create `web/.env.local` (Vite reads `VITE_*` automatically):

```dotenv
VITE_FIREBASE_API_KEY=...
VITE_FIREBASE_AUTH_DOMAIN=...
VITE_FIREBASE_PROJECT_ID=...
VITE_FIREBASE_STORAGE_BUCKET=...
VITE_FIREBASE_MESSAGING_SENDER_ID=...
VITE_FIREBASE_APP_ID=...
```

Without Firestore credentials the app still loads, but the Home page won't sync saved scenarios.

## Project structure

```
web/
├── index.html                       # Vite entry
├── vite.config.ts                   # /api → :8080 proxy
├── eslint.config.js                 # ESLint flat config
└── src/
    ├── main.tsx / App.tsx           # Router shell
    ├── firebase.ts                  # Firestore init from VITE_FIREBASE_*
    ├── useTheme.ts                  # light/dark hook
    ├── TopNav.tsx / Footer.tsx
    ├── i18n/                        # translations.ts + LanguageContext
    └── pages/
        ├── home/                    # Scenario CRUD + run
        ├── settings/                # LLM config (writes to /api/config + localStorage)
        └── about/                   # Project description + mermaid diagram
```

---

## Related

- Backend (this repo): [../README.md](../README.md)
- [Koog Documentation](https://docs.koog.ai)
