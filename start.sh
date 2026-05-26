#!/usr/bin/env bash
# Start both the Ktor backend (:8080) and the Vite web dashboard (:5173).
# Usage: ./start.sh

set -e
cd "$(dirname "$0")"

cleanup() {
  echo ""
  echo "Stopping services..."
  kill 0 2>/dev/null || true
}
trap cleanup EXIT INT TERM

if [ ! -d "web/node_modules" ]; then
  echo "Installing web dependencies..."
  (cd web && npm install)
fi

echo "Starting Ktor backend on http://localhost:8080 ..."
./gradlew run --console=plain &
BACKEND_PID=$!

echo "Starting web dashboard on http://localhost:5173 ..."
(cd web && npm run dev) &
WEB_PID=$!

open_browser() {
  local url="http://localhost:5173"
  until curl -sf "$url" >/dev/null 2>&1; do sleep 1; done
  if command -v xdg-open >/dev/null 2>&1; then xdg-open "$url" >/dev/null 2>&1
  elif command -v open >/dev/null 2>&1; then open "$url" >/dev/null 2>&1
  elif command -v start >/dev/null 2>&1; then start "$url" >/dev/null 2>&1
  fi
}
open_browser &

echo ""
echo "Backend PID: $BACKEND_PID | Web PID: $WEB_PID"
echo "Press Ctrl+C to stop both."
wait
