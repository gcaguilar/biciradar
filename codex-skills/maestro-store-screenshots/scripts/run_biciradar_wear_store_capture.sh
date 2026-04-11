#!/usr/bin/env bash

set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  run_biciradar_wear_store_capture.sh <device-id> <output-dir>

This script:
  1. Grants Wear OS location permissions to BiciRadar.
  2. Launches deterministic screenshot surfaces directly.
  3. Captures dashboard, detail and monitoring screens.
EOF
}

device_id="${1:-}"
output_dir="${2:-}"
app_id="com.gcaguilar.biciradar"
activity_name=".wear.WearActivity"
surface_extra="com.gcaguilar.biciradar.wear.extra.SCREENSHOT_SURFACE"

if [[ -z "$device_id" || -z "$output_dir" ]]; then
  usage >&2
  exit 1
fi

command -v adb >/dev/null 2>&1 || {
  printf 'Missing required command: adb\n' >&2
  exit 1
}

mkdir -p "$output_dir/screenshots"

adb -s "$device_id" wait-for-device
adb -s "$device_id" shell pm grant "$app_id" android.permission.ACCESS_FINE_LOCATION >/dev/null 2>&1 || true
adb -s "$device_id" shell pm grant "$app_id" android.permission.ACCESS_COARSE_LOCATION >/dev/null 2>&1 || true

capture_surface() {
  local surface="$1"
  local output_path="$2"
  adb -s "$device_id" shell am force-stop "$app_id" >/dev/null 2>&1 || true
  adb -s "$device_id" shell am start -n "$app_id/$activity_name" --es "$surface_extra" "$surface" >/dev/null
  sleep 2
  adb -s "$device_id" exec-out screencap -p > "$output_path"
}

capture_surface "dashboard" "$output_dir/screenshots/01-dashboard.png"
capture_surface "station_detail" "$output_dir/screenshots/02-station-detail.png"
capture_surface "monitoring" "$output_dir/screenshots/03-monitoring.png"

printf 'Saved %s\n' "$output_dir/screenshots/01-dashboard.png"
printf 'Saved %s\n' "$output_dir/screenshots/02-station-detail.png"
printf 'Saved %s\n' "$output_dir/screenshots/03-monitoring.png"
