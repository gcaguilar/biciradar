#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 2 ]]; then
  printf 'Usage: capture_simulator_screen.sh <simulator-udid|booted> <output.png>\n' >&2
  exit 1
fi

device_id="$1"
output_path="$2"

command -v xcrun >/dev/null 2>&1 || {
  printf 'Missing required command: xcrun\n' >&2
  exit 1
}

mkdir -p "$(dirname "$output_path")"
xcrun simctl io "$device_id" screenshot "$output_path"
printf 'Saved %s\n' "$output_path"
