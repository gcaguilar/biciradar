#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
OUTPUT_PATH="$ROOT_DIR/bin/BiciRadar.iq"
DEVELOPER_KEY_PATH="${DEVELOPER_KEY_PATH:-$ROOT_DIR/developer_key}"
SDK_ROOT="${CONNECTIQ_SDK_ROOT:-}"
REFRESH_ICONS="false"
ICON_SIZES=(30 40 50 60 70 90 100 110 140 150 180 210 220)

usage() {
  cat <<'EOF'
Usage: ./garminApp/build_store_iq.sh [--refresh-icons]

Options:
  --refresh-icons   Regenerate launcher icons from the Android source icon.
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --refresh-icons)
      REFRESH_ICONS="true"
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
  shift
done

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

icons_need_generation() {
  if [[ "$REFRESH_ICONS" == "true" ]]; then
    return 0
  fi

  local size
  for size in "${ICON_SIZES[@]}"; do
    if [[ ! -f "$ROOT_DIR/resources/drawables/${size}x${size}/ic_launcher.png" ]]; then
      return 0
    fi
  done

  return 1
}

if [[ ! -f "$DEVELOPER_KEY_PATH" ]]; then
  echo "Developer key not found: $DEVELOPER_KEY_PATH" >&2
  echo "Set DEVELOPER_KEY_PATH or place the file at garminApp/developer_key" >&2
  exit 1
fi

if ! command -v python3 >/dev/null 2>&1; then
  echo "python3 is required" >&2
  exit 1
fi

SDK_ROOT="$(find_sdk_root)"
if [[ -z "$SDK_ROOT" || ! -x "$SDK_ROOT/bin/monkeyc" ]]; then
  echo "Could not find a usable Connect IQ SDK." >&2
  echo "Set CONNECTIQ_SDK_ROOT to your SDK path, for example:" >&2
  echo '  export CONNECTIQ_SDK_ROOT="$HOME/Library/Application Support/Garmin/ConnectIQ/Sdks/connectiq-sdk-mac-9.1.0-..."' >&2
  exit 1
fi

echo "Using SDK: $SDK_ROOT"

if icons_need_generation; then
  if ! python3 -c 'from PIL import Image' >/dev/null 2>&1; then
    echo "Pillow is required to generate icons. Install it with: python3 -m pip install pillow" >&2
    exit 1
  fi

  echo "Generating launcher icons..."
  python3 "$ROOT_DIR/generate_icons.py"
else
  echo "Using existing launcher icons..."
fi

mkdir -p "$ROOT_DIR/bin"

echo "Exporting store package..."
"$SDK_ROOT/bin/monkeyc" \
  -e \
  -f "$ROOT_DIR/monkey.jungle" \
  -o "$OUTPUT_PATH" \
  -y "$DEVELOPER_KEY_PATH" \
  -w

echo
echo "Created IQ package: $OUTPUT_PATH"
