#!/usr/bin/env bash

set -euo pipefail

DESTINATION="${1:-platform=watchOS Simulator,name=Apple Watch Series 11 (46mm),OS=26.2}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$ROOT_DIR"

xcodebuild \
  -project apple/BiziZaragoza.xcodeproj \
  -scheme BiziZaragozaWatch \
  -sdk watchsimulator \
  -destination "$DESTINATION" \
  test
