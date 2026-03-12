#!/usr/bin/env bash

set -euo pipefail

DESTINATION="${1:-platform=iOS Simulator,name=iPhone 17 Pro,OS=26.2}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$ROOT_DIR"

xcodebuild \
  -project apple/BiziZaragoza.xcodeproj \
  -scheme BiziZaragoza \
  -sdk iphonesimulator \
  -destination "$DESTINATION" \
  test
