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
# Version code: the highest code already used by Android, Wear OS or Apple,
#               plus one. Wear OS gets Android + 1, Apple stays aligned with
#               Android. BUILD_NUMBER is recomputed as code - BASE_VERSION_CODE
#               so it stays in sync with the derived codes.
#               Base: 29568074 (last published on Google Play)
#               e.g. last used 29570208 → Android 29570209, Wear 29570210

set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
VERSION_FILE="${REPO_ROOT}/VERSION"
BUILD_NUMBER_FILE="${REPO_ROOT}/BUILD_NUMBER"
BASE_VERSION_CODE=29568074
ANDROID_GRADLE_FILE="${REPO_ROOT}/androidApp/build.gradle.kts"
WEAR_GRADLE_FILE="${REPO_ROOT}/wearApp/build.gradle.kts"
PROJECT_YML_FILE="${REPO_ROOT}/apple/project.yml"
XCODEPROJ_FILE="${REPO_ROOT}/apple/BiciRadar.xcodeproj/project.pbxproj"

# Portable in-place sed: GNU sed uses `-i`, BSD/macOS sed needs an empty backup
# suffix, so `sed -i ''` fails on Linux and vice versa.
sed_in_place() {
  if sed --version >/dev/null 2>&1; then
    sed -i "$@"
  else
    sed -i '' "$@"
  fi
}

# Highest versionCode/current project version already checked in.
highest_existing_code() {
  local codes=()
  local value

  for file in "$ANDROID_GRADLE_FILE" "$WEAR_GRADLE_FILE"; do
    [[ -f "$file" ]] || continue
    value=$(grep -oE '^[[:space:]]*versionCode = [0-9]+' "$file" | grep -oE '[0-9]+' | head -1 || true)
    [[ -n "$value" ]] && codes+=("$value")
  done

  if [[ -f "$XCODEPROJ_FILE" ]]; then
    while read -r value; do
      [[ -n "$value" ]] && codes+=("$value")
    done < <(grep -oE 'CURRENT_PROJECT_VERSION = [0-9]+;' "$XCODEPROJ_FILE" | grep -oE '[0-9]+' || true)
  elif [[ -f "$PROJECT_YML_FILE" ]]; then
    value=$(grep -oE 'CURRENT_PROJECT_VERSION: [0-9]+' "$PROJECT_YML_FILE" | grep -oE '[0-9]+' | head -1 || true)
    [[ -n "$value" ]] && codes+=("$value")
  fi

  local highest="$BASE_VERSION_CODE"
  for value in "${codes[@]}"; do
    (( value > highest )) && highest="$value"
  done

  echo "$highest"
}

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

# Determine version name
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
      ;;
    *)
      # Use provided version
      version_name="$1"
      ;;
  esac
else
  # Read from VERSION file, keep the version name
  if [[ -f "$VERSION_FILE" ]]; then
    version_name=$(cat "$VERSION_FILE")
  else
    version_name="0.1.0"
  fi
fi

# Validate version format
if ! [[ "$version_name" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Error: Invalid version format '$version_name'. Expected: X.Y.Z (e.g., 1.2.3)"
  exit 1
fi

# Codes always continue from the highest code already checked in, so a stale
# BUILD_NUMBER can never make us reuse a code Play already accepted.
last_version_code=$(highest_existing_code)
version_code=$((last_version_code + 1))
wear_version_code=$((version_code + 1))
build_number=$((version_code - BASE_VERSION_CODE))

echo "Bumping version → name=${version_name}  code=${version_code} (last used ${last_version_code}, build ${build_number})  wear_code=${wear_version_code}"

# Update VERSION file
echo "$version_name" > "$VERSION_FILE"
echo "  ✓ VERSION file"

# Update BUILD_NUMBER file
echo "$build_number" > "$BUILD_NUMBER_FILE"
echo "  ✓ BUILD_NUMBER file"

# ── iOS: apple/project.yml ────────────────────────────────────────────────────
project_yml="${PROJECT_YML_FILE}"
if [[ -f "$project_yml" ]]; then
  sed_in_place \
    "s/MARKETING_VERSION:.*/MARKETING_VERSION: ${version_name}/" \
    "$project_yml"
  sed_in_place \
    "s/CURRENT_PROJECT_VERSION:.*/CURRENT_PROJECT_VERSION: ${version_code}/" \
    "$project_yml"
  echo "  ✓ apple/project.yml"
fi

# ── iOS: checked-in Xcode project (kept in sync with project.yml) ─────────────
xcodeproj="${XCODEPROJ_FILE}"
if [[ -f "$xcodeproj" ]]; then
  sed_in_place \
    "s/MARKETING_VERSION = .*/MARKETING_VERSION = ${version_name};/" \
    "$xcodeproj"
  sed_in_place \
    "s/CURRENT_PROJECT_VERSION = .*/CURRENT_PROJECT_VERSION = ${version_code};/" \
    "$xcodeproj"
  echo "  ✓ apple/BiciRadar.xcodeproj/project.pbxproj"
fi

# ── Android phone: androidApp/build.gradle.kts ────────────────────────────────
android_gradle="${ANDROID_GRADLE_FILE}"
if [[ -f "$android_gradle" ]]; then
  sed_in_place \
    "s/versionCode = .*/versionCode = ${version_code}/" \
    "$android_gradle"
  sed_in_place \
    "s/versionName = \".*\"/versionName = \"${version_name}\"/" \
    "$android_gradle"
  echo "  ✓ androidApp/build.gradle.kts"
fi

# ── Wear OS: wearApp/build.gradle.kts ─────────────────────────────────────────
wear_gradle="${WEAR_GRADLE_FILE}"
if [[ -f "$wear_gradle" ]]; then
  sed_in_place \
    "s/versionCode = .*/versionCode = ${wear_version_code}/" \
    "$wear_gradle"
  sed_in_place \
    "s/versionName = \".*\"/versionName = \"${version_name}\"/" \
    "$wear_gradle"
  echo "  ✓ wearApp/build.gradle.kts"
fi

echo ""
echo "Version updated successfully!"
echo "  versionName:      ${version_name}"
echo "  versionCode:      ${version_code} (androidApp, apple)"
echo "  wear versionCode: ${wear_version_code} (wearApp)"
echo "  last used code:   ${last_version_code}"
echo "  base: ${BASE_VERSION_CODE} + build: ${build_number}"
echo ""
echo "Remaining version codes available: $(( 2100000000 - wear_version_code ))"
