#!/usr/bin/env bash

set -euo pipefail

DEVICE_ID="${1:-emulator-5554}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$ROOT_DIR"

./gradlew :androidApp:installDebug
maestro --device "$DEVICE_ID" test maestro/android/assistant-smoke.yaml
