#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

expect_file() {
  local path="$1"
  [[ -f "$path" ]] || fail "missing file: $path"
}

expect_dir() {
  local path="$1"
  [[ -d "$path" ]] || fail "missing directory: $path"
}

read_gradle_value() {
  local file="$1"
  local key="$2"
  sed -n "s/^[[:space:]]*$key = \"\\([^\"]*\\)\"/\\1/p; s/^[[:space:]]*$key = \\([0-9][0-9]*\\)/\\1/p" "$file" | head -n 1
}

check_metadata_entry() {
  local label="$1"
  local gradle_file="$2"
  local metadata_file="$3"
  local apk_path="$4"
  local screenshots_dir="$5"

  local base_version version_code expected_version metadata_version metadata_code metadata_commit
  base_version="$(read_gradle_value "$gradle_file" "versionName")"
  version_code="$(read_gradle_value "$gradle_file" "versionCode")"
  expected_version="${base_version}-fdroid"

  [[ -n "$base_version" ]] || fail "could not read versionName from $gradle_file"
  [[ -n "$version_code" ]] || fail "could not read versionCode from $gradle_file"

  expect_file "$metadata_file"
  expect_file "$apk_path"
  expect_dir "$screenshots_dir"

  metadata_version="$(sed -n 's/^[[:space:]-]*versionName: //p' "$metadata_file" | head -n 1)"
  metadata_code="$(sed -n 's/^[[:space:]-]*versionCode: //p' "$metadata_file" | head -n 1)"
  metadata_commit="$(sed -n 's/^[[:space:]-]*commit: //p' "$metadata_file" | head -n 1)"

  [[ "$metadata_version" == "$expected_version" ]] || fail "$label metadata versionName is $metadata_version but expected $expected_version"
  [[ "$metadata_code" == "$version_code" ]] || fail "$label metadata versionCode is $metadata_code but expected $version_code"
  [[ "$metadata_commit" =~ ^[0-9a-f]{7,40}$|^[0-9]+\.[0-9].* ]] || fail "$label metadata commit must be a git hash or release tag"
  grep -q '^      - fdroid$' "$metadata_file" || fail "$label metadata must build the fdroid Gradle flavor"

  if ! find "$screenshots_dir" -type f ! -name '.gitkeep' | grep -q .; then
    fail "$label screenshots are still placeholders in $screenshots_dir"
  fi
}

check_metadata_entry \
  "androidApp" \
  "androidApp/build.gradle.kts" \
  "metadata/com.gcaguilar.biciradar.fdroid.yml" \
  "androidApp/build/outputs/apk/fdroid/release/androidApp-fdroid-release-unsigned.apk" \
  "androidApp/src/fdroid/fastlane/metadata/android/en-US/images/phoneScreenshots"

echo "F-Droid submission files look consistent."
