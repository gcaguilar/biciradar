#!/usr/bin/env bash

set -euo pipefail

: "${APPLE_SIGNING_CERTIFICATE_P12_BASE64:?APPLE_SIGNING_CERTIFICATE_P12_BASE64 is required}"
: "${APPLE_SIGNING_CERTIFICATE_PASSWORD:?APPLE_SIGNING_CERTIFICATE_PASSWORD is required}"
: "${APPLE_PROVISIONING_PROFILE_BASE64:?APPLE_PROVISIONING_PROFILE_BASE64 is required}"
: "${GITHUB_ENV:?GITHUB_ENV is required}"
: "${RUNNER_TEMP:?RUNNER_TEMP is required}"

keychain_password="${APPLE_KEYCHAIN_PASSWORD:-bizi-temporary-keychain-password}"
keychain_path="${RUNNER_TEMP}/bizi-signing.keychain-db"
certificate_path="${RUNNER_TEMP}/signing-certificate.p12"
profile_path="${RUNNER_TEMP}/signing-profile.mobileprovision"
profile_plist_path="${RUNNER_TEMP}/signing-profile.plist"

decode_base64_to_file() {
  local input_value="$1"
  local output_path="$2"

  BASE64_INPUT="$input_value" OUTPUT_PATH="$output_path" python3 <<'PY'
import base64
import os
from pathlib import Path

data = os.environ["BASE64_INPUT"]
print(f"DEBUG: Decoding {len(data)} bytes of base64 data", file=sys.stderr)
try:
    decoded = base64.b64decode(data)
    print(f"DEBUG: Decoded to {len(decoded)} bytes", file=sys.stderr)
    Path(os.environ["OUTPUT_PATH"]).write_bytes(decoded)
    print(f"DEBUG: Written to {os.environ['OUTPUT_PATH']}", file=sys.stderr)
except Exception as e:
    print(f"ERROR: Decode failed: {e}", file=sys.stderr)
    raise
PY
}

decode_base64_to_file "$APPLE_SIGNING_CERTIFICATE_P12_BASE64" "$certificate_path"
decode_base64_to_file "$APPLE_PROVISIONING_PROFILE_BASE64" "$profile_path"

echo "DEBUG: Checking P12 file..."
ls -la "$certificate_path"
file "$certificate_path"
openssl pkcs12 -info -in "$certificate_path" -passin "pass:$APPLE_SIGNING_CERTIFICATE_PASSWORD" -nokeys -noout 2>&1 || echo "P12 verification failed!"

security create-keychain -p "$keychain_password" "$keychain_path"
security set-keychain-settings -lut 21600 "$keychain_path"
security unlock-keychain -p "$keychain_password" "$keychain_path"
security import "$certificate_path" \
  -k "$keychain_path" \
  -P "$APPLE_SIGNING_CERTIFICATE_PASSWORD" \
  -T /usr/bin/codesign \
  -T /usr/bin/security \
  -A
security set-key-partition-list -S apple-tool:,apple: -s -k "$keychain_password" "$keychain_path"
security list-keychains -d user -s "$keychain_path" $(security list-keychains -d user | tr -d '"')
security default-keychain -s "$keychain_path"

mkdir -p "$HOME/Library/MobileDevice/Provisioning Profiles"
security cms -D -i "$profile_path" > "$profile_plist_path"

profile_uuid="$(
  /usr/libexec/PlistBuddy -c 'Print UUID' "$profile_plist_path"
)"
profile_name="$(
  /usr/libexec/PlistBuddy -c 'Print Name' "$profile_plist_path"
)"

cp "$profile_path" "$HOME/Library/MobileDevice/Provisioning Profiles/${profile_uuid}.mobileprovision"

{
  echo "APPLE_KEYCHAIN_PATH=${keychain_path}"
  echo "APPLE_KEYCHAIN_PASSWORD=${keychain_password}"
  echo "APPLE_PROFILE_NAME=${profile_name}"
  echo "APPLE_PROFILE_UUID=${profile_uuid}"
} >> "$GITHUB_ENV"
