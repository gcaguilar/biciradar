#!/usr/bin/env bash
# install_git_hooks.sh — Installs the project's git hooks into .git/hooks/.
# Run this once after cloning:  ./scripts/install_git_hooks.sh

set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
HOOKS_DIR="${REPO_ROOT}/.git/hooks"
SCRIPTS_DIR="${REPO_ROOT}/scripts"

install_hook() {
  local hook_name="$1"
  local source="${SCRIPTS_DIR}/${hook_name}"
  local target="${HOOKS_DIR}/${hook_name}"

  if [[ ! -f "$source" ]]; then
    echo "Source not found, skipping: ${source}"
    return
  fi

  cp "$source" "$target"
  chmod +x "$target"
  echo "Installed: ${hook_name}"
}

install_hook "post-commit"

echo "Done. Git hooks installed."
