#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 2 ]]; then
  printf 'Usage: capture_android_screen.sh <adb-serial> <output.png>\n' >&2
  exit 1
fi

serial="$1"
output_path="$2"

command -v adb >/dev/null 2>&1 || {
  printf 'Missing required command: adb\n' >&2
  exit 1
}

mkdir -p "$(dirname "$output_path")"
adb -s "$serial" exec-out screencap -p >"$output_path"
printf 'Saved %s\n' "$output_path"
