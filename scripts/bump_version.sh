#!/usr/bin/env bash
# bump_version.sh — Updates version strings across iOS and Android project files.
# Version format: yyyy.MM.dd.HHmm  (e.g. 2026.03.13.1530)
# Build number:   yyyyMMddHHmm     (e.g. 202603131530)
#
# Reads the timestamp of the latest git commit so the version is stable
# even when the script runs multiple times for the same commit.

set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"

# Use the author date of HEAD so re-runs produce the same value.
commit_timestamp="$(git log -1 --format='%ad' --date='format:%Y.%m.%d.%H%M')"
build_number="$(git log -1 --format='%ad' --date='format:%Y%m%d%H%M')"

version_name="$commit_timestamp"   # e.g. 2026.03.13.1530
version_code="$build_number"       # e.g. 202603131530

echo "Bumping version → name=${version_name}  code=${version_code}"

# ── iOS: apple/project.yml ────────────────────────────────────────────────────
project_yml="${REPO_ROOT}/apple/project.yml"
if [[ -f "$project_yml" ]]; then
  sed -i '' \
    "s/MARKETING_VERSION:.*/MARKETING_VERSION: ${version_name}/" \
    "$project_yml"
  sed -i '' \
    "s/CURRENT_PROJECT_VERSION:.*/CURRENT_PROJECT_VERSION: ${version_code}/" \
    "$project_yml"
  echo "  ✓ apple/project.yml"
fi

# ── Android phone: androidApp/build.gradle.kts ────────────────────────────────
android_gradle="${REPO_ROOT}/androidApp/build.gradle.kts"
if [[ -f "$android_gradle" ]]; then
  sed -i '' \
    "s/versionCode = .*/versionCode = ${version_code}/" \
    "$android_gradle"
  sed -i '' \
    "s/versionName = \".*\"/versionName = \"${version_name}\"/" \
    "$android_gradle"
  echo "  ✓ androidApp/build.gradle.kts"
fi

# ── Wear OS: wearApp/build.gradle.kts ─────────────────────────────────────────
wear_gradle="${REPO_ROOT}/wearApp/build.gradle.kts"
if [[ -f "$wear_gradle" ]]; then
  sed -i '' \
    "s/versionCode = .*/versionCode = ${version_code}/" \
    "$wear_gradle"
  sed -i '' \
    "s/versionName = \".*\"/versionName = \"${version_name}\"/" \
    "$wear_gradle"
  echo "  ✓ wearApp/build.gradle.kts"
fi
