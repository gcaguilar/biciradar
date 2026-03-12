#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DESTINATION="${1:-$("$ROOT_DIR/scripts/resolve_xcode_destination.sh" watchos)}"

cd "$ROOT_DIR"

xcodebuild \
  -project apple/BiziZaragoza.xcodeproj \
  -scheme BiziZaragozaWatch \
  -sdk watchsimulator \
  -destination "$DESTINATION" \
  test
