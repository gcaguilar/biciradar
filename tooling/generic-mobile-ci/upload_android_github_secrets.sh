#!/usr/bin/env bash

set -euo pipefail

SECRETS_FILE="${1:-github-secrets.txt}"
REPO="${2:-}"

if ! command -v gh >/dev/null 2>&1; then
  echo "Error: gh is required" >&2
  exit 1
fi

if [[ ! -f "$SECRETS_FILE" ]]; then
  echo "Error: secrets file not found: $SECRETS_FILE" >&2
  exit 1
fi

if ! gh auth status >/dev/null 2>&1; then
  echo "Error: gh is not authenticated. Run 'gh auth login' first." >&2
  exit 1
fi

set_secret() {
  local name="$1"
  local value="$2"
  if [[ -n "$REPO" ]]; then
    gh secret set "$name" --repo "$REPO" --body "$value" >/dev/null
  else
    gh secret set "$name" --body "$value" >/dev/null
  fi
  echo "Uploaded $name"
}

while IFS='=' read -r name value; do
  [[ -z "$name" ]] && continue
  set_secret "$name" "$value"
done < "$SECRETS_FILE"

echo
echo "Done uploading GitHub Actions secrets from $SECRETS_FILE"
