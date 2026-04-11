#!/usr/bin/env bash

set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  run_biciradar_watch_store_capture.sh <watch-udid> <output-dir> [watch-app-path]

This script:
  1. Reboots the Apple Watch simulator to clear sticky launch UI.
  2. Installs the built watch app.
  3. Enables the watch screenshot mode with a fixed Zaragoza location.
  4. Cycles through the supported watch screenshot surfaces.
  5. Captures store-ready screenshots into <output-dir>/screenshots.
EOF
}

watch_udid="${1:-}"
output_dir="${2:-}"
watch_app_path="${3:-$PWD/build/maestro-watch/Build/Products/Debug-watchsimulator/BiciRadar Watch.app}"
watch_app_id="com.gcaguilar.biciradar.ios.watch"

if [[ -z "$watch_udid" || -z "$output_dir" ]]; then
  usage >&2
  exit 1
fi

if [[ ! -d "$watch_app_path" ]]; then
  printf 'Watch app bundle not found at %s\n' "$watch_app_path" >&2
  exit 1
fi

command -v xcrun >/dev/null 2>&1 || {
  printf 'Missing required command: xcrun\n' >&2
  exit 1
}

mkdir -p "$output_dir/screenshots"

# A clean reboot avoids the simulator getting stuck on a stale permission surface.
xcrun simctl shutdown "$watch_udid" >/dev/null 2>&1 || true
xcrun simctl boot "$watch_udid"
xcrun simctl bootstatus "$watch_udid" -b

xcrun simctl install "$watch_udid" "$watch_app_path"
xcrun simctl privacy "$watch_udid" grant location "$watch_app_id" >/dev/null 2>&1 || true
xcrun simctl spawn "$watch_udid" defaults write "$watch_app_id" bizizaragoza.watch.screenshot_mode -bool YES
xcrun simctl spawn "$watch_udid" defaults write "$watch_app_id" bizizaragoza.watch.screenshot_latitude -float 41.6488
xcrun simctl spawn "$watch_udid" defaults write "$watch_app_id" bizizaragoza.watch.screenshot_longitude -float -0.8891

capture_surface() {
  local surface="$1"
  local output_path="$2"
  xcrun simctl spawn "$watch_udid" defaults write "$watch_app_id" bizizaragoza.watch.screenshot_surface -string "$surface"
  xcrun simctl launch --terminate-running-process "$watch_udid" "$watch_app_id" >/dev/null
  sleep 25
  xcrun simctl io "$watch_udid" screenshot "$output_path"
}

capture_surface "dashboard" "$output_dir/screenshots/01-watch-dashboard.png"
capture_surface "station_detail" "$output_dir/screenshots/02-watch-station-detail.png"
capture_surface "monitoring" "$output_dir/screenshots/03-watch-monitoring.png"

printf 'Saved %s\n' "$output_dir/screenshots/01-watch-dashboard.png"
printf 'Saved %s\n' "$output_dir/screenshots/02-watch-station-detail.png"
printf 'Saved %s\n' "$output_dir/screenshots/03-watch-monitoring.png"
