#!/usr/bin/env bash

set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$repo_root"

secrets_file="${LOCAL_RELEASE_SECRETS_FILE:-$repo_root/github-secrets.txt}"
if [[ -f "$secrets_file" ]]; then
  # shellcheck disable=SC1090
  source "$secrets_file"
fi

output_root="${LOCAL_BUILD_OUTPUT_DIR:-build/local-release}"
output_root="${output_root/#\~/$HOME}"
case "$output_root" in
  /*) ;;
  *) output_root="$repo_root/$output_root" ;;
esac

mkdir -p "$output_root"

usage() {
  cat <<'EOF'
Usage: ./local-release-build.sh [all|android|wearos|ios|watchos|apple]

Targets:
  all      Build Android, Wear OS, iOS and Apple Watch release artifacts.
  android  Build the Android phone release APK/AAB.
  wearos   Build the Wear OS release APK/AAB.
  ios      Build the signed iOS archive and IPA.
  watchos  Build the signed Apple Watch archive.
  apple    Build both iOS and Apple Watch artifacts.
EOF
}

log() {
  printf '\n==> %s\n' "$*"
}

fail() {
  printf 'Error: %s\n' "$*" >&2
  exit 1
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || fail "Missing required command: $1"
}

decode_base64_to_file() {
  local input_value="$1"
  local output_path="$2"

  python3 - "$output_path" <<'PY' <<<"$input_value"
import base64
import sys
from pathlib import Path

payload = sys.stdin.read()
Path(sys.argv[1]).write_bytes(base64.b64decode(payload))
PY
}

resolve_path() {
  local path="$1"
  path="${path/#\~/$HOME}"
  case "$path" in
    /*) printf '%s\n' "$path" ;;
    *) printf '%s/%s\n' "$repo_root" "$path" ;;
  esac
}

first_existing_path() {
  local candidate
  for candidate in "$@"; do
    [[ -z "$candidate" ]] && continue
    candidate="$(resolve_path "$candidate")"
    if [[ -f "$candidate" ]]; then
      printf '%s\n' "$candidate"
      return 0
    fi
  done
  return 1
}

android_requested=false
wear_requested=false
ios_requested=false
watch_requested=false

if [[ $# -eq 0 ]]; then
  set -- all
fi

for target in "$@"; do
  case "$target" in
    all)
      android_requested=true
      wear_requested=true
      ios_requested=true
      watch_requested=true
      ;;
    android)
      android_requested=true
      ;;
    wearos|wear)
      wear_requested=true
      ;;
    ios)
      ios_requested=true
      ;;
    watchos|watch)
      watch_requested=true
      ;;
    apple)
      ios_requested=true
      watch_requested=true
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      usage >&2
      fail "Unknown target: $target"
      ;;
  esac
done

artifacts=()

add_artifact() {
  local path="$1"
  artifacts+=("$path")
}

ios_profile_name=""
ios_profile_uuid=""
ios_profile_team=""
ios_profile_bundle=""

read_profile_metadata() {
  local profile_path="$1"
  local output

  output="$(python3 - "$profile_path" <<'PY'
import plistlib
import sys
from pathlib import Path

profile_path = Path(sys.argv[1])
raw = profile_path.read_bytes()
start = raw.find(b"<?xml")
end = raw.find(b"</plist>")
if start < 0 or end < 0:
    raise SystemExit(f"Could not decode provisioning profile: {profile_path}")

data = plistlib.loads(raw[start:end + len(b"</plist>")])
application_identifier = data["Entitlements"]["application-identifier"]
bundle_id = application_identifier.split(".", 1)[1]

print(data["Name"])
print(data["UUID"])
print(data["TeamIdentifier"][0])
print(bundle_id)
PY
)"

  local old_ifs="$IFS"
  IFS=$'\n'
  set -- $output
  IFS="$old_ifs"

  ios_profile_name="${1:-}"
  ios_profile_uuid="${2:-}"
  ios_profile_team="${3:-}"
  ios_profile_bundle="${4:-}"
}

setup_android_inputs() {
  local android_cert_dir="${LOCAL_ANDROID_CERT_DIR:-android-cert}"
  android_cert_dir="$(resolve_path "$android_cert_dir")"

  android_keystore_path="$(first_existing_path \
    "${BIZI_CI_KEYSTORE_PATH:-}" \
    "$android_cert_dir/bici-upload-v2.keystore")" || android_keystore_path=""

  if [[ -z "$android_keystore_path" ]]; then
    if [[ -z "${BIZI_CI_KEYSTORE_BASE64:-}" ]]; then
      fail "Missing Android keystore file and BIZI_CI_KEYSTORE_BASE64"
    fi
    android_keystore_path="$output_root/tmp/bizi-ci.keystore"
    mkdir -p "$(dirname "$android_keystore_path")"
    decode_base64_to_file "$BIZI_CI_KEYSTORE_BASE64" "$android_keystore_path"
  fi

  [[ -n "${BIZI_CI_KEYSTORE_PASSWORD:-}" ]] || fail "BIZI_CI_KEYSTORE_PASSWORD is required"
  [[ -n "${BIZI_CI_KEY_ALIAS:-}" ]] || fail "BIZI_CI_KEY_ALIAS is required"
  [[ -n "${BIZI_CI_KEY_PASSWORD:-}" ]] || fail "BIZI_CI_KEY_PASSWORD is required"

  android_google_services_path="$(first_existing_path \
    "${GOOGLE_SERVICES_JSON_ANDROID_PATH:-}" \
    "$android_cert_dir/google-services.json")" || android_google_services_path=""

  wear_google_services_path="$(first_existing_path \
    "${GOOGLE_SERVICES_JSON_WEAR_PATH:-}" \
    "${GOOGLE_SERVICES_JSON_ANDROID_PATH:-}" \
    "$android_cert_dir/google-services.json")" || wear_google_services_path=""

  if [[ -n "$android_google_services_path" ]]; then
    cp "$android_google_services_path" "$repo_root/androidApp/google-services.json"
  fi

  if [[ -n "$wear_google_services_path" ]]; then
    cp "$wear_google_services_path" "$repo_root/wearApp/google-services.json"
  fi
}

build_android_targets() {
  require_command ./gradlew
  setup_android_inputs

  local maps_api_key="${GOOGLE_MAPS_API_KEY_ANDROID:-${GOOGLE_MAPS_API_KEY:-}}"
  local gradle_tasks=()

  if [[ "$android_requested" == true ]]; then
    gradle_tasks+=(":androidApp:assembleRelease" ":androidApp:bundleRelease")
  fi

  if [[ "$wear_requested" == true ]]; then
    gradle_tasks+=(":wearApp:assembleRelease" ":wearApp:bundleRelease")
  fi

  log "Building Android release artifacts"
  GOOGLE_MAPS_API_KEY="$maps_api_key" ./gradlew \
    "${gradle_tasks[@]}" \
    --no-build-cache \
    -PBIZI_CI_KEYSTORE_PATH="$android_keystore_path" \
    -PBIZI_CI_KEYSTORE_PASSWORD="$BIZI_CI_KEYSTORE_PASSWORD" \
    -PBIZI_CI_KEY_ALIAS="$BIZI_CI_KEY_ALIAS" \
    -PBIZI_CI_KEY_PASSWORD="$BIZI_CI_KEY_PASSWORD"

  if [[ "$android_requested" == true ]]; then
    mkdir -p "$output_root/android"
    cp "$repo_root"/androidApp/build/outputs/apk/release/*.apk "$output_root/android/" 2>/dev/null || true
    cp "$repo_root"/androidApp/build/outputs/bundle/release/*.aab "$output_root/android/" 2>/dev/null || true
    cp "$repo_root/androidApp/build/outputs/mapping/release/mapping.txt" "$output_root/android/androidApp-release-mapping.txt" 2>/dev/null || true
    add_artifact "$output_root/android"
  fi

  if [[ "$wear_requested" == true ]]; then
    mkdir -p "$output_root/wearos"
    cp "$repo_root"/wearApp/build/outputs/apk/release/*.apk "$output_root/wearos/" 2>/dev/null || true
    cp "$repo_root"/wearApp/build/outputs/bundle/release/*.aab "$output_root/wearos/" 2>/dev/null || true
    cp "$repo_root/wearApp/build/outputs/mapping/release/mapping.txt" "$output_root/wearos/wearApp-release-mapping.txt" 2>/dev/null || true
    add_artifact "$output_root/wearos"
  fi
}

original_default_keychain=""
original_keychains=()
temp_keychain_path=""
temp_keychain_password=""
apple_inputs_ready=false

capture_keychain_state() {
  original_default_keychain=""
  if default_keychain_output="$(security default-keychain -d user 2>/dev/null)"; then
    original_default_keychain="$(printf '%s' "$default_keychain_output" | sed 's/^[[:space:]]*//; s/[[:space:]]*$//' | tr -d '"')"
  fi
  original_keychains=()
  while IFS= read -r line; do
    line="$(printf '%s' "$line" | sed 's/^[[:space:]]*//; s/[[:space:]]*$//' | tr -d '"')"
    [[ "$line" == /* ]] && original_keychains+=("$line")
  done < <(security list-keychains -d user 2>/dev/null || true)

  if [[ -z "$original_default_keychain" && -f "$HOME/Library/Keychains/login.keychain-db" ]]; then
    original_default_keychain="$HOME/Library/Keychains/login.keychain-db"
  fi
}

restore_keychain_state() {
  if [[ ${#original_keychains[@]} -gt 0 ]]; then
    security list-keychains -d user -s "${original_keychains[@]}" >/dev/null 2>&1 || true
  fi

  if [[ -n "$original_default_keychain" ]]; then
    security default-keychain -s "$original_default_keychain" >/dev/null 2>&1 || true
  fi

  if [[ -n "$temp_keychain_path" && -f "$temp_keychain_path" ]]; then
    security delete-keychain "$temp_keychain_path" >/dev/null 2>&1 || true
  fi
}

cleanup() {
  restore_keychain_state
}

trap cleanup EXIT

setup_apple_inputs() {
  if [[ "$apple_inputs_ready" == true ]]; then
    return
  fi

  require_command security
  require_command xcodebuild
  require_command python3

  local apple_cert_dir="${LOCAL_APPLE_CERT_DIR:-apple-cert}"
  apple_cert_dir="$(resolve_path "$apple_cert_dir")"

  apple_certificate_path="$(first_existing_path \
    "${APPLE_SIGNING_CERTIFICATE_PATH:-}" \
    "$apple_cert_dir/ios-distribution.p12" \
    "$apple_cert_dir/ios_distribution.p12")" || apple_certificate_path=""
  [[ -n "$apple_certificate_path" ]] || fail "Missing Apple signing certificate (.p12)"
  [[ -n "${APPLE_SIGNING_CERTIFICATE_PASSWORD:-}" ]] || fail "APPLE_SIGNING_CERTIFICATE_PASSWORD is required"

  apple_profile_path="$(first_existing_path \
    "${APPLE_PROVISIONING_PROFILE_PATH:-}" \
    "$apple_cert_dir/BiciRadarProfile.mobileprovision" \
    "$apple_cert_dir/Profile.mobileprovision")" || apple_profile_path=""
  apple_widget_profile_path="$(first_existing_path \
    "${APPLE_WIDGET_PROVISIONING_PROFILE_PATH:-}" \
    "$apple_cert_dir/BiciRadarWidgetProfile.mobileprovision")" || apple_widget_profile_path=""
  apple_watch_profile_path="$(first_existing_path \
    "${APPLE_WATCH_PROVISIONING_PROFILE_PATH:-}" \
    "$apple_cert_dir/BiciRadarWatchProfile.mobileprovision")" || apple_watch_profile_path=""
  apple_watch_widget_profile_path="$(first_existing_path \
    "${APPLE_WATCH_WIDGET_PROVISIONING_PROFILE_PATH:-}" \
    "$apple_cert_dir/BiciRadarWatchWidgetProfile.mobileprovision")" || apple_watch_widget_profile_path=""

  [[ -n "$apple_profile_path" ]] || fail "Missing iOS provisioning profile"
  [[ -n "$apple_widget_profile_path" ]] || fail "Missing iOS widget provisioning profile"
  [[ -n "$apple_watch_profile_path" ]] || fail "Missing watchOS provisioning profile"
  [[ -n "$apple_watch_widget_profile_path" ]] || fail "Missing watchOS widget provisioning profile"

  google_service_info_ios_path="$(first_existing_path \
    "${GOOGLE_SERVICE_INFO_PLIST_IOS_PATH:-}" \
    "$apple_cert_dir/GoogleService-Info.plist")" || google_service_info_ios_path=""

  local ios_maps_key="${GOOGLE_MAPS_IOS_API_KEY:-${GOOGLE_MAPS_API_KEY:-}}"
  printf 'GOOGLE_MAPS_IOS_API_KEY = %s\n' "$ios_maps_key" > "$repo_root/apple/Config/LocalSecrets.xcconfig"

  if [[ -n "$google_service_info_ios_path" ]]; then
    cp "$google_service_info_ios_path" "$repo_root/apple/iosApp/GoogleService-Info.plist"
  fi

  read_profile_metadata "$apple_profile_path"
  main_profile_name="$ios_profile_name"
  main_profile_uuid="$ios_profile_uuid"
  main_profile_team="$ios_profile_team"
  main_profile_bundle="$ios_profile_bundle"

  read_profile_metadata "$apple_widget_profile_path"
  widget_profile_name="$ios_profile_name"
  widget_profile_uuid="$ios_profile_uuid"
  widget_profile_team="$ios_profile_team"
  widget_profile_bundle="$ios_profile_bundle"

  read_profile_metadata "$apple_watch_profile_path"
  watch_profile_name="$ios_profile_name"
  watch_profile_uuid="$ios_profile_uuid"
  watch_profile_team="$ios_profile_team"
  watch_profile_bundle="$ios_profile_bundle"

  read_profile_metadata "$apple_watch_widget_profile_path"
  watch_widget_profile_name="$ios_profile_name"
  watch_widget_profile_uuid="$ios_profile_uuid"
  watch_widget_profile_team="$ios_profile_team"
  watch_widget_profile_bundle="$ios_profile_bundle"

  apple_team_id="${APPLE_TEAM_ID:-$main_profile_team}"
  apple_bundle_id="${APPLE_BUNDLE_ID:-$main_profile_bundle}"
  apple_export_method="${APPLE_EXPORT_METHOD:-app-store}"
  apple_signing_certificate_type="${APPLE_SIGNING_CERTIFICATE_TYPE:-Apple Distribution}"

  mkdir -p "$HOME/Library/MobileDevice/Provisioning Profiles"
  cp "$apple_profile_path" "$HOME/Library/MobileDevice/Provisioning Profiles/${main_profile_uuid}.mobileprovision"
  cp "$apple_widget_profile_path" "$HOME/Library/MobileDevice/Provisioning Profiles/${widget_profile_uuid}.mobileprovision"
  cp "$apple_watch_profile_path" "$HOME/Library/MobileDevice/Provisioning Profiles/${watch_profile_uuid}.mobileprovision"
  cp "$apple_watch_widget_profile_path" "$HOME/Library/MobileDevice/Provisioning Profiles/${watch_widget_profile_uuid}.mobileprovision"

  capture_keychain_state

  temp_keychain_password="${APPLE_KEYCHAIN_PASSWORD:-local-release-signing-keychain}"
  temp_keychain_path="$output_root/tmp/local-release-signing.keychain-db"
  mkdir -p "$output_root/tmp"

  if [[ -f "$temp_keychain_path" ]]; then
    security delete-keychain "$temp_keychain_path" >/dev/null 2>&1 || true
  fi

  security create-keychain -p "$temp_keychain_password" "$temp_keychain_path"
  security set-keychain-settings -lut 21600 "$temp_keychain_path"
  security unlock-keychain -p "$temp_keychain_password" "$temp_keychain_path"
  security import "$apple_certificate_path" \
    -k "$temp_keychain_path" \
    -P "$APPLE_SIGNING_CERTIFICATE_PASSWORD" \
    -T /usr/bin/codesign \
    -T /usr/bin/security \
    -A
  security set-key-partition-list -S apple-tool:,apple: -s -k "$temp_keychain_password" "$temp_keychain_path"
  security list-keychains -d user -s "$temp_keychain_path" "${original_keychains[@]}"
  security default-keychain -s "$temp_keychain_path"
  apple_inputs_ready=true
}

build_ios_release() {
  setup_apple_inputs

  local ios_output_dir="$output_root/ios"
  local ios_archive_path="$ios_output_dir/BiciRadar.xcarchive"
  local export_options_plist="$output_root/tmp/BiciRadar-export-options.plist"
  mkdir -p "$ios_output_dir"
  rm -rf "$ios_archive_path" "$ios_output_dir/export"

  cat > "$export_options_plist" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>destination</key>
  <string>export</string>
  <key>method</key>
  <string>${apple_export_method}</string>
  <key>provisioningProfiles</key>
  <dict>
    <key>${main_profile_bundle}</key>
    <string>${main_profile_name}</string>
    <key>${widget_profile_bundle}</key>
    <string>${widget_profile_name}</string>
    <key>${watch_profile_bundle}</key>
    <string>${watch_profile_name}</string>
    <key>${watch_widget_profile_bundle}</key>
    <string>${watch_widget_profile_name}</string>
  </dict>
  <key>signingCertificate</key>
  <string>${apple_signing_certificate_type}</string>
  <key>signingStyle</key>
  <string>manual</string>
  <key>stripSwiftSymbols</key>
  <true/>
  <key>teamID</key>
  <string>${apple_team_id}</string>
</dict>
</plist>
EOF

  log "Building signed iOS archive"
  xcodebuild \
    -project "$repo_root/apple/BiciRadar.xcodeproj" \
    -scheme BiciRadar \
    -configuration Release \
    -destination 'generic/platform=iOS' \
    -archivePath "$ios_archive_path" \
    CODE_SIGN_STYLE=Manual \
    CODE_SIGN_IDENTITY="$apple_signing_certificate_type" \
    DEVELOPMENT_TEAM="$apple_team_id" \
    OTHER_CODE_SIGN_FLAGS="--keychain $temp_keychain_path" \
    archive

  log "Exporting iOS IPA"
  xcodebuild \
    -exportArchive \
    -archivePath "$ios_archive_path" \
    -exportPath "$ios_output_dir/export" \
    -exportOptionsPlist "$export_options_plist"

  if compgen -G "$ios_output_dir/export/*.ipa" >/dev/null; then
    cp "$ios_output_dir/export/"*.ipa "$ios_output_dir/"
  fi

  add_artifact "$ios_output_dir"
}

build_watch_release() {
  setup_apple_inputs

  local watch_output_dir="$output_root/watchos"
  local watch_archive_path="$watch_output_dir/BiciRadarWatch.xcarchive"
  mkdir -p "$watch_output_dir"
  rm -rf "$watch_archive_path"

  log "Building signed Apple Watch archive"
  xcodebuild \
    -project "$repo_root/apple/BiciRadar.xcodeproj" \
    -scheme BiciRadarWatch \
    -configuration Release \
    -destination 'generic/platform=watchOS' \
    -archivePath "$watch_archive_path" \
    CODE_SIGN_STYLE=Manual \
    CODE_SIGN_IDENTITY="$apple_signing_certificate_type" \
    DEVELOPMENT_TEAM="$apple_team_id" \
    OTHER_CODE_SIGN_FLAGS="--keychain $temp_keychain_path" \
    archive

  add_artifact "$watch_output_dir"
}

if [[ "$android_requested" == true || "$wear_requested" == true ]]; then
  build_android_targets
fi

if [[ "$ios_requested" == true ]]; then
  build_ios_release
fi

if [[ "$watch_requested" == true ]]; then
  build_watch_release
fi

log "Artifacts ready"
for artifact in "${artifacts[@]}"; do
  printf '%s\n' "$artifact"
done
