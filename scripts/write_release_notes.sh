#!/usr/bin/env bash

set -euo pipefail

output_path="${1:?usage: write_release_notes.sh <output-path> [label]}"
label="${2:-build}"

mkdir -p "$(dirname "$output_path")"

commit_sha="${GITHUB_SHA:-$(git rev-parse HEAD)}"
commit_subject="$(git log --format=%s -n 1 "$commit_sha")"
ref_name="${GITHUB_REF_NAME:-$(git rev-parse --abbrev-ref HEAD)}"

{
  echo "BiciRadar ${label}"
  echo
  echo "Commit: ${commit_sha}"
  echo "Branch: ${ref_name}"
  echo "Summary: ${commit_subject}"

  if [[ -n "${GITHUB_SERVER_URL:-}" && -n "${GITHUB_REPOSITORY:-}" && -n "${GITHUB_RUN_ID:-}" ]]; then
    echo
    echo "CI run: ${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/actions/runs/${GITHUB_RUN_ID}"
  fi
} > "$output_path"
