#!/usr/bin/env bash

set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
apple_cert_dir="${APPLE_CERT_DIR:-$repo_root/apple-cert}"
ios_app_dir="${IOS_APP_DIR:-$repo_root/apple/iosApp}"

p12_path="${APPLE_P12_PATH:-$apple_cert_dir/ios_distribution.p12}"
profile_path="${APPLE_PROFILE_PATH:-$apple_cert_dir/Profile.mobileprovision}"
google_service_info_path="${GOOGLE_SERVICE_INFO_PLIST_PATH:-$ios_app_dir/GoogleService-Info.plist}"

shopt -s nullglob
api_key_candidates=("$apple_cert_dir"/AuthKey_*.p8)
shopt -u nullglob

if [[ ! -f "$p12_path" ]]; then
  printf 'Missing signing certificate: %s\n' "$p12_path" >&2
  exit 1
fi

if [[ ! -f "$profile_path" ]]; then
  printf 'Missing provisioning profile: %s\n' "$profile_path" >&2
  exit 1
fi

if (( ${#api_key_candidates[@]} != 1 )); then
  printf 'Expected exactly one AuthKey_*.p8 file in %s\n' "$apple_cert_dir" >&2
  exit 1
fi

api_key_path="${APPLE_APP_STORE_CONNECT_P8_PATH:-${api_key_candidates[0]}}"
api_key_filename="$(basename "$api_key_path")"
api_key_id_default="${api_key_filename#AuthKey_}"
api_key_id_default="${api_key_id_default%.p8}"
app_store_connect_key_id="${APP_STORE_CONNECT_KEY_ID:-$api_key_id_default}"

profile_values="$({ PROFILE_PATH="$profile_path" python3 <<'PY'
import os
import plistlib
from pathlib import Path

raw = Path(os.environ["PROFILE_PATH"]).read_bytes()
start = raw.find(b"<?xml")
end = raw.find(b"</plist>")
if start < 0 or end < 0:
    raise SystemExit("Could not decode provisioning profile")

data = plistlib.loads(raw[start:end + len(b"</plist>")])
team_id = data["TeamIdentifier"][0]
app_id = data["Entitlements"]["application-identifier"]
bundle_id = app_id.split(".", 1)[1]

print(team_id)
print(bundle_id)
PY
})"

apple_team_id_default="${profile_values%%$'\n'*}"
apple_bundle_id_default="${profile_values#*$'\n'}"

apple_team_id="${APPLE_TEAM_ID:-$apple_team_id_default}"
apple_bundle_id="${APPLE_BUNDLE_ID:-$apple_bundle_id_default}"
apple_export_method="${APPLE_EXPORT_METHOD:-app-store}"
apple_signing_certificate_type="${APPLE_SIGNING_CERTIFICATE_TYPE:-Apple Distribution}"

base64_file() {
  python3 - "$1" <<'PY'
import base64
import sys
from pathlib import Path

print(base64.b64encode(Path(sys.argv[1]).read_bytes()).decode("ascii"), end="")
PY
}

read_required() {
  local var_name="$1"
  local prompt="$2"
  local secret_input="${3:-false}"
  local current_value="${!var_name:-}"

  while [[ -z "$current_value" ]]; do
    if [[ "$secret_input" == "true" ]]; then
      read -r -s -p "$prompt: " current_value
      printf '\n' >&2
    else
      read -r -p "$prompt: " current_value
    fi
  done

  printf -v "$var_name" '%s' "$current_value"
}

read_optional() {
  local var_name="$1"
  local prompt="$2"
  local current_value="${!var_name:-}"

  if [[ -z "$current_value" ]]; then
    read -r -p "$prompt: " current_value
    printf -v "$var_name" '%s' "$current_value"
  fi
}

read_required APPLE_SIGNING_CERTIFICATE_PASSWORD "APPLE_SIGNING_CERTIFICATE_PASSWORD" true

if [[ -z "${APPLE_KEYCHAIN_PASSWORD:-}" ]]; then
  APPLE_KEYCHAIN_PASSWORD="$(python3 - <<'PY'
import secrets
print(secrets.token_urlsafe(24), end="")
PY
)"
fi

read_required APP_STORE_CONNECT_ISSUER_ID "APP_STORE_CONNECT_ISSUER_ID"
read_required GOOGLE_MAPS_API_KEY "GOOGLE_MAPS_API_KEY"
read_required APP_REVIEW_CONTACT_FIRST_NAME "APP_REVIEW_CONTACT_FIRST_NAME"
read_required APP_REVIEW_CONTACT_LAST_NAME "APP_REVIEW_CONTACT_LAST_NAME"
read_required APP_REVIEW_CONTACT_EMAIL "APP_REVIEW_CONTACT_EMAIL"
read_required APP_REVIEW_CONTACT_PHONE "APP_REVIEW_CONTACT_PHONE"
read_optional APP_REVIEW_NOTES "APP_REVIEW_NOTES (optional)"

app_uses_encryption="${APP_USES_ENCRYPTION:-}"
while [[ "$app_uses_encryption" != "true" && "$app_uses_encryption" != "false" ]]; do
  read -r -p 'APP_USES_ENCRYPTION (true/false): ' app_uses_encryption
done
APP_USES_ENCRYPTION="$app_uses_encryption"

p12_base64="$(base64_file "$p12_path")"
profile_base64="$(base64_file "$profile_path")"
google_service_info_contents=""
if [[ -f "$google_service_info_path" ]]; then
  google_service_info_contents="$(python3 - "$google_service_info_path" <<'PY'
import sys
from pathlib import Path

print(Path(sys.argv[1]).read_text(), end="")
PY
)"
fi

printf 'Recommended GitHub environment: app-store\n'
printf '\n[secrets]\n'
printf 'APPLE_TEAM_ID=%s\n' "$apple_team_id"
printf 'APPLE_SIGNING_CERTIFICATE_P12_BASE64=%s\n' "$p12_base64"
printf 'APPLE_SIGNING_CERTIFICATE_PASSWORD=%s\n' "$APPLE_SIGNING_CERTIFICATE_PASSWORD"
printf 'APPLE_PROVISIONING_PROFILE_BASE64=%s\n' "$profile_base64"
printf 'APPLE_KEYCHAIN_PASSWORD=%s\n' "$APPLE_KEYCHAIN_PASSWORD"
printf 'APP_STORE_CONNECT_ISSUER_ID=%s\n' "$APP_STORE_CONNECT_ISSUER_ID"
printf 'APP_STORE_CONNECT_KEY_ID=%s\n' "$app_store_connect_key_id"
printf 'APP_REVIEW_CONTACT_FIRST_NAME=%s\n' "$APP_REVIEW_CONTACT_FIRST_NAME"
printf 'APP_REVIEW_CONTACT_LAST_NAME=%s\n' "$APP_REVIEW_CONTACT_LAST_NAME"
printf 'APP_REVIEW_CONTACT_EMAIL=%s\n' "$APP_REVIEW_CONTACT_EMAIL"
printf 'APP_REVIEW_CONTACT_PHONE=%s\n' "$APP_REVIEW_CONTACT_PHONE"

if [[ -n "$APP_REVIEW_NOTES" ]]; then
  printf 'APP_REVIEW_NOTES=%s\n' "$APP_REVIEW_NOTES"
fi

printf 'GOOGLE_MAPS_API_KEY=%s\n' "$GOOGLE_MAPS_API_KEY"

printf '\nAPP_STORE_CONNECT_API_KEY_P8<<EOF\n'
python3 - "$api_key_path" <<'PY'
import sys
from pathlib import Path

print(Path(sys.argv[1]).read_text(), end="")
PY
printf '\nEOF\n'

if [[ -n "$google_service_info_contents" ]]; then
  printf '\nGOOGLE_SERVICE_INFO_PLIST_IOS<<EOF\n%s\nEOF\n' "$google_service_info_contents"
else
  printf '\n# GOOGLE_SERVICE_INFO_PLIST_IOS not found at %s\n' "$google_service_info_path"
fi

printf '\n[vars]\n'
printf 'APPLE_EXPORT_METHOD=%s\n' "$apple_export_method"
printf 'APPLE_SIGNING_CERTIFICATE_TYPE=%s\n' "$apple_signing_certificate_type"
printf 'APPLE_BUNDLE_ID=%s\n' "$apple_bundle_id"
printf 'APP_USES_ENCRYPTION=%s\n' "$APP_USES_ENCRYPTION"
