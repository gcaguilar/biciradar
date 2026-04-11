#!/usr/bin/env bash

set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  run_biciradar_ios_store_set.sh <device-udid> <output-dir>

This script:
  1. Seeds Zaragoza via iOS deep link.
  2. Completes pending onboarding with Maestro.
  3. Captures home, favorites, nearby, and station detail screenshots.
EOF
}

device_udid="${1:-}"
output_dir="${2:-}"
app_id="com.gcaguilar.biciradar.ios"
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
flow_dir="${script_dir%/scripts}/assets/flows"

if [[ -z "$device_udid" || -z "$output_dir" ]]; then
  usage >&2
  exit 1
fi

mkdir -p "$output_dir"

run_flow() {
  local flow_file="$1"
  maestro --device "$device_udid" test --test-output-dir "$output_dir" "$flow_file"
}

open_deeplink() {
  local deeplink="$1"
  bash "$script_dir/open_ios_deeplink.sh" "$device_udid" "$deeplink" "$app_id"
}

open_deeplink "biciradar://city/zaragoza"
run_flow "$flow_dir/biciradar-ios-bootstrap.yaml"

run_flow "$flow_dir/biciradar-ios-home-capture.yaml"

open_deeplink "biciradar://favorites"
run_flow "$flow_dir/biciradar-ios-favorites-capture.yaml"

open_deeplink "biciradar://home"
run_flow "$flow_dir/biciradar-ios-nearby-capture.yaml"

open_deeplink "biciradar://station/5a8156a1697a8ad877e70472210e4b91"
run_flow "$flow_dir/biciradar-ios-station-detail-capture.yaml"
