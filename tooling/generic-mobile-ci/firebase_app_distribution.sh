#!/usr/bin/env bash

set -euo pipefail

binary_path="${1:?usage: firebase_app_distribution.sh <binary> <firebase-app-id> <release-notes-file> [display-name]}"
firebase_app_id="${2:?usage: firebase_app_distribution.sh <binary> <firebase-app-id> <release-notes-file> [display-name]}"
release_notes_file="${3:?usage: firebase_app_distribution.sh <binary> <firebase-app-id> <release-notes-file> [display-name]}"
display_name="${4:-artifact}"

if [[ ! -f "$binary_path" ]]; then
  echo "Binary not found: $binary_path" >&2
  exit 1
fi

if [[ ! -f "$release_notes_file" ]]; then
  echo "Release notes file not found: $release_notes_file" >&2
  exit 1
fi

command=(
  firebase
  appdistribution:distribute
  "$binary_path"
  --app
  "$firebase_app_id"
  --release-notes-file
  "$release_notes_file"
)

if [[ -n "${FIREBASE_APP_DIST_TESTERS:-}" ]]; then
  command+=(--testers "$FIREBASE_APP_DIST_TESTERS")
fi

if [[ -n "${FIREBASE_APP_DIST_GROUPS:-}" ]]; then
  command+=(--groups "$FIREBASE_APP_DIST_GROUPS")
fi

echo "Uploading ${display_name} to Firebase App Distribution"
"${command[@]}"
