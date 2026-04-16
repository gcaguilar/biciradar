#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_FILE="$ROOT_DIR/source/BiciRadarApp.mc"
SDK_ROOT="${CONNECTIQ_SDK_ROOT:-}"
DEVICE_ID="${1:-venu3s}"
OUTPUT_PATH="$ROOT_DIR/bin/demo-${DEVICE_ID}.prg"
DEVELOPER_KEY_PATH="${DEVELOPER_KEY_PATH:-$ROOT_DIR/developer_key}"
ORIGINAL_CONTENT="$(mktemp)"

find_sdk_root() {
  if [[ -n "$SDK_ROOT" ]]; then
    echo "$SDK_ROOT"
    return 0
  fi

  local default_sdk_dir="$HOME/Library/Application Support/Garmin/ConnectIQ/Sdks"
  if [[ -d "$default_sdk_dir" ]]; then
    ls -td "$default_sdk_dir"/* 2>/dev/null | head -n1
    return 0
  fi

  return 1
}

cp "$APP_FILE" "$ORIGINAL_CONTENT"

cleanup() {
  cp "$ORIGINAL_CONTENT" "$APP_FILE"
  rm -f "$ORIGINAL_CONTENT"
}

trap cleanup EXIT

perl -0pi -e 's/const ENABLE_DEMO_DATA = false;/const ENABLE_DEMO_DATA = true;/' "$APP_FILE"

SDK_ROOT="$(find_sdk_root)"
if [[ -z "$SDK_ROOT" || ! -x "$SDK_ROOT/bin/monkeyc" ]]; then
  echo "Could not find a usable Connect IQ SDK." >&2
  echo 'Set CONNECTIQ_SDK_ROOT to your SDK path, for example:' >&2
  echo '  export CONNECTIQ_SDK_ROOT="$HOME/Library/Application Support/Garmin/ConnectIQ/Sdks/connectiq-sdk-mac-9.1.0-..."' >&2
  exit 1
fi

if [[ ! -f "$DEVELOPER_KEY_PATH" ]]; then
  echo "Developer key not found: $DEVELOPER_KEY_PATH" >&2
  echo "Set DEVELOPER_KEY_PATH or place the file at garminApp/developer_key" >&2
  exit 1
fi

mkdir -p "$ROOT_DIR/bin"

"$SDK_ROOT/bin/monkeyc" \
  -d "$DEVICE_ID" \
  -f "$ROOT_DIR/monkey.jungle" \
  -o "$OUTPUT_PATH" \
  -y "$DEVELOPER_KEY_PATH" \
  -w

echo "Created demo PRG: $OUTPUT_PATH"
