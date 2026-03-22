#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TARGET="${1:-}"
OVERRIDE="${2:-}"
RESOLVE_DESTINATION="$ROOT_DIR/tooling/generic-mobile-ci/resolve_xcode_destination.sh"

usage() {
  cat <<'EOF'
Usage:
  run_smoke.sh android-assistant [device-id]
  run_smoke.sh ios [destination]
  run_smoke.sh watchos [destination]
EOF
}

if [[ -z "$TARGET" ]]; then
  usage >&2
  exit 1
fi

cd "$ROOT_DIR"

case "$TARGET" in
  android-assistant)
    device_id="${OVERRIDE:-emulator-5554}"
    ./gradlew :androidApp:installDebug
    maestro --device "$device_id" test maestro/android/assistant-smoke.yaml
    ;;
  ios)
    destination="${OVERRIDE:-$($RESOLVE_DESTINATION ios)}"
    xcodebuild \
      -project apple/BiciRadar.xcodeproj \
      -scheme BiciRadar \
      -sdk iphonesimulator \
      -destination "$destination" \
      test
    ;;
  watchos)
    destination="${OVERRIDE:-$($RESOLVE_DESTINATION watchos)}"
    xcodebuild \
      -project apple/BiciRadar.xcodeproj \
      -scheme BiciRadarWatch \
      -sdk watchsimulator \
      -destination "$destination" \
      test
    ;;
  *)
    usage >&2
    exit 1
    ;;
esac
