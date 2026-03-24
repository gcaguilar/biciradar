#!/usr/bin/env bash
# bump_version.sh — Updates version strings across iOS and Android project files.
# 
# Usage:
#   ./bump_version.sh                    # Read version from VERSION file, bump build
#   ./bump_version.sh 1.2.3              # Use specific version (bumps build)
#   ./bump_version.sh --patch            # Bump patch version (0.1.0 → 0.1.1)
#   ./bump_version.sh --minor            # Bump minor version (0.1.0 → 0.2.0)
#   ./bump_version.sh --major            # Bump major version (0.1.0 → 1.0.0)
#
# Version code: BASE_VERSION_CODE + build_number
#               Base: 29568074 (last published on Google Play)
#               Build number stored in BUILD_NUMBER file
#               e.g. build 1 → 29568075, build 10 → 29568084

set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
VERSION_FILE="${REPO_ROOT}/VERSION"
BUILD_NUMBER_FILE="${REPO_ROOT}/BUILD_NUMBER"
BASE_VERSION_CODE=29568074

# Function to bump version
bump_version() {
  local current="$1"
  local bump_type="$2"
  local major minor patch
  
  IFS='.' read -r major minor patch <<< "$current"
  
  case "$bump_type" in
    --major)
      major=$((major + 1))
      minor=0
      patch=0
      ;;
    --minor)
      minor=$((minor + 1))
      patch=0
      ;;
    --patch)
      patch=$((patch + 1))
      ;;
  esac
  
  echo "${major}.${minor}.${patch}"
}

# Read or initialize build number
if [[ -f "$BUILD_NUMBER_FILE" ]]; then
  build_number=$(cat "$BUILD_NUMBER_FILE")
else
  build_number=0
fi

# Determine version
if [[ $# -gt 0 ]]; then
  case "$1" in
    --major|--minor|--patch)
      # Read current version and bump
      if [[ -f "$VERSION_FILE" ]]; then
        current_version=$(cat "$VERSION_FILE")
      else
        current_version="0.1.0"
      fi
      version_name=$(bump_version "$current_version" "$1")
      build_number=$((build_number + 1))
      ;;
    *)
      # Use provided version
      version_name="$1"
      build_number=$((build_number + 1))
      ;;
  esac
else
  # Read from VERSION file, just bump build number
  if [[ -f "$VERSION_FILE" ]]; then
    version_name=$(cat "$VERSION_FILE")
  else
    version_name="0.1.0"
  fi
  build_number=$((build_number + 1))
fi

# Validate version format
if ! [[ "$version_name" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Error: Invalid version format '$version_name'. Expected: X.Y.Z (e.g., 1.2.3)"
  exit 1
fi

version_code=$((BASE_VERSION_CODE + build_number))

echo "Bumping version → name=${version_name}  code=${version_code} (build ${build_number})"

# Update VERSION file
echo "$version_name" > "$VERSION_FILE"
echo "  ✓ VERSION file"

# Update BUILD_NUMBER file
echo "$build_number" > "$BUILD_NUMBER_FILE"
echo "  ✓ BUILD_NUMBER file"

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

echo ""
echo "Version updated successfully!"
echo "  versionName: ${version_name}"
echo "  versionCode: ${version_code} (base: ${BASE_VERSION_CODE} + build: ${build_number})"
echo ""
echo "Remaining version codes available: $(( 2100000000 - version_code ))"