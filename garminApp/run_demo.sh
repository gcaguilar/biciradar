#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
SDK_ROOT="${CONNECTIQ_SDK_ROOT:-}"
DEVICE_ID="${1:-venu3s}"
DEMO_PRG="$ROOT_DIR/bin/demo-${DEVICE_ID}.prg"

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

SDK_ROOT="$(find_sdk_root)"
if [[ -z "$SDK_ROOT" || ! -x "$SDK_ROOT/bin/monkeydo" ]]; then
  echo "Could not find a usable Connect IQ SDK." >&2
  exit 1
fi

"$ROOT_DIR/build_demo.sh" "$DEVICE_ID"

"$SDK_ROOT/bin/connectiq" &
sleep 5
"$SDK_ROOT/bin/monkeydo" "$DEMO_PRG" "$DEVICE_ID"
