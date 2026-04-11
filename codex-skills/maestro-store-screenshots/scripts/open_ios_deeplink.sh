#!/usr/bin/env bash

set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  open_ios_deeplink.sh <device-udid> <deeplink> [app-id]

Notes:
  - This helper opens an iOS deep link with simctl and then foregrounds the app.
  - It is useful on Simulator when SpringBoard confirmation makes Maestro's openLink flaky.
EOF
}

device_udid="${1:-}"
deeplink="${2:-}"
app_id="${3:-com.gcaguilar.biciradar.ios}"

if [[ -z "$device_udid" || -z "$deeplink" ]]; then
  usage >&2
  exit 1
fi

xcrun simctl openurl "$device_udid" "$deeplink"
xcrun simctl launch "$device_udid" "$app_id" >/dev/null
