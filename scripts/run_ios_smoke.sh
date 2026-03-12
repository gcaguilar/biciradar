#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DESTINATION="${1:-$("$ROOT_DIR/scripts/resolve_xcode_destination.sh" ios)}"

cd "$ROOT_DIR"

xcodebuild \
  -project apple/BiziZaragoza.xcodeproj \
  -scheme BiziZaragoza \
  -sdk iphonesimulator \
  -destination "$DESTINATION" \
  test
