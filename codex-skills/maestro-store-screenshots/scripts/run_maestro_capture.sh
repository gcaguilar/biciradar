#!/usr/bin/env bash

set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  run_maestro_capture.sh --flow <flow.yaml> --output-dir <dir> [--device <id>] [--bootstrap-flow <flow.yaml>]

Notes:
  - The app must already be installed on the target device or simulator.
  - If --bootstrap-flow is provided, it runs first on the same device.
  - Screenshots created with takeScreenshot are written under --output-dir.
EOF
}

flow=""
bootstrap_flow=""
output_dir=""
device=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --flow)
      flow="${2:-}"
      shift 2
      ;;
    --bootstrap-flow)
      bootstrap_flow="${2:-}"
      shift 2
      ;;
    --output-dir)
      output_dir="${2:-}"
      shift 2
      ;;
    --device)
      device="${2:-}"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      printf 'Unknown argument: %s\n' "$1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

[[ -n "$flow" ]] || { usage >&2; exit 1; }
[[ -n "$output_dir" ]] || { usage >&2; exit 1; }

command -v maestro >/dev/null 2>&1 || {
  printf 'Missing required command: maestro\n' >&2
  exit 1
}

mkdir -p "$output_dir"

run_maestro_flow() {
  local selected_flow="$1"
  local args=(test --test-output-dir "$output_dir" "$selected_flow")
  if [[ -n "$device" ]]; then
    args=(--device "$device" "${args[@]}")
  fi
  maestro "${args[@]}"
}

if [[ -n "$bootstrap_flow" ]]; then
  run_maestro_flow "$bootstrap_flow"
fi

run_maestro_flow "$flow"
