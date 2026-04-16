#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_FILE="$ROOT_DIR/source/BiciRadarApp.mc"
ORIGINAL_CONTENT="$(mktemp)"

cp "$APP_FILE" "$ORIGINAL_CONTENT"

cleanup() {
  cp "$ORIGINAL_CONTENT" "$APP_FILE"
  rm -f "$ORIGINAL_CONTENT"
}

trap cleanup EXIT

perl -0pi -e 's/const ENABLE_DEMO_DATA = false;/const ENABLE_DEMO_DATA = true;/' "$APP_FILE"

"$ROOT_DIR/build_store_iq.sh" "$@"
