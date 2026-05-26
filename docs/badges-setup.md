# README Badges — Setup Guide

The badges at the top of [README.md](../README.md) are a mix of **static** (always render) and **dynamic** (need infrastructure). Static badges work immediately. Dynamic badges (Build, Tests) require GitHub Actions workflows in this repo.

Repo: `maikotrindade/mobile-tester-agent`

---

## 1. Static badges — already working

These render from [shields.io](https://shields.io) with hard-coded values. Bump versions when dependencies change.

| Badge | Source | What to update |
|---|---|---|
| Kotlin | `img.shields.io/badge/Kotlin-2.3.0-...` | Bump version in URL when `build.gradle.kts` changes |
| JDK | `img.shields.io/badge/JDK-21-...` | Bump if you change `jvmToolchain` |
| Ktor | `img.shields.io/badge/Ktor-3.1.3-...` | Track `build.gradle.kts` |
| Koog | `img.shields.io/badge/Koog-0.8.0-...` | Track `build.gradle.kts` |
| Gradle | `img.shields.io/badge/Gradle-8.11+-...` | Bump if wrapper changes |
| React / Vite / TypeScript | `img.shields.io/badge/...` | Track `web/package.json` |
| Firebase, Android, iOS, React Native | Platform tags | No update needed |
| License: MIT | `img.shields.io/badge/License-MIT-...` | Confirm `LICENSE` exists at repo root |
| PRs Welcome | `img.shields.io/badge/PRs-welcome-...` | Static |

---

## 2. Dynamic badges — require GitHub Actions

Two badges currently point to workflows that don't exist yet:

- `…/actions/workflows/build.yml/badge.svg`
- `…/actions/workflows/test.yml/badge.svg`

Until the workflows are created, the badges show **"no status"**. Steps to make them green:

### Step 1 — Create `.github/workflows/build.yml`

```yaml
name: Build

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: corretto
          java-version: '21'
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew compileKotlin

  build-web:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: web
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: npm
          cache-dependency-path: web/package-lock.json
      - run: npm ci
      - run: npm run build
```

### Step 2 — Create `.github/workflows/test.yml`

```yaml
name: Tests

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: corretto
          java-version: '21'
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew test

  lint-web:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: web
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: npm
          cache-dependency-path: web/package-lock.json
      - run: npm ci
      - run: npm run lint
```

### Step 3 — Commit, push, verify

```bash
git add .github/workflows/build.yml .github/workflows/test.yml
git commit -m "ci: add build and test workflows"
git push
```

Open the **Actions** tab on GitHub. After the first run, the badges go green/red.

> File names matter: badge URLs reference `build.yml` and `test.yml` literally. If you rename a workflow file, update the badge URL in [README.md](../README.md) to match.

---

## 3. Optional extra badges

Drop these in if useful:

```markdown
[![codecov](https://codecov.io/gh/maikotrindade/mobile-tester-agent/branch/main/graph/badge.svg)](https://codecov.io/gh/maikotrindade/mobile-tester-agent)
[![Last commit](https://img.shields.io/github/last-commit/maikotrindade/mobile-tester-agent)](https://github.com/maikotrindade/mobile-tester-agent/commits/main)
[![Issues](https://img.shields.io/github/issues/maikotrindade/mobile-tester-agent)](https://github.com/maikotrindade/mobile-tester-agent/issues)
[![Stars](https://img.shields.io/github/stars/maikotrindade/mobile-tester-agent?style=social)](https://github.com/maikotrindade/mobile-tester-agent/stargazers)
[![Release](https://img.shields.io/github/v/release/maikotrindade/mobile-tester-agent)](https://github.com/maikotrindade/mobile-tester-agent/releases)
```

- **codecov** also requires CI: run coverage in the test workflow and upload via `codecov/codecov-action@v4`.
- **Last commit / Issues / Stars / Release** are pulled from the GitHub API — no setup needed.

---

## 4. Verification checklist

- [ ] `LICENSE` file exists at the repo root (for the MIT badge to be accurate).
- [ ] `build.yml` and `test.yml` workflows pushed to `main` at least once.
- [ ] Workflow runs succeed (Actions tab shows green).
- [ ] Badge images load when opening the README on GitHub.
- [ ] Versions in static badges match `build.gradle.kts` and `web/package.json`.
