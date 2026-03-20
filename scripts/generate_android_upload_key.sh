#!/usr/bin/env bash

set -euo pipefail

KEYSTORE_PATH="${1:-bici-upload-v2.keystore}"
KEY_ALIAS="${2:-bici-upload-v2}"
CERT_PATH="${3:-bici-upload-v2.pem}"
SECRETS_FILE="${4:-github-secrets.txt}"
VALIDITY_DAYS="${VALIDITY_DAYS:-9125}"
KEY_SIZE="${KEY_SIZE:-4096}"

if ! command -v keytool >/dev/null 2>&1; then
  echo "Error: keytool is required" >&2
  exit 1
fi

if ! command -v base64 >/dev/null 2>&1; then
  echo "Error: base64 is required" >&2
  exit 1
fi

read -rsp "Keystore password: " KEYSTORE_PASSWORD
echo
read -rsp "Key password [press Enter to reuse keystore password]: " KEY_PASSWORD
echo
if [[ -z "$KEY_PASSWORD" ]]; then
  KEY_PASSWORD="$KEYSTORE_PASSWORD"
fi

read -rp "Distinguished name [CN=BiciRadar Upload, OU=Mobile, O=BiciRadar, L=Barcelona, ST=Barcelona, C=ES]: " DNAME
if [[ -z "$DNAME" ]]; then
  DNAME="CN=BiciRadar Upload, OU=Mobile, O=BiciRadar, L=Barcelona, ST=Barcelona, C=ES"
fi

for path in "$KEYSTORE_PATH" "$CERT_PATH" "$SECRETS_FILE"; do
  if [[ -e "$path" ]]; then
    echo "Error: $path already exists. Remove it or pass different output paths." >&2
    exit 1
  fi
done

keytool -genkeypair \
  -v \
  -keystore "$KEYSTORE_PATH" \
  -storepass "$KEYSTORE_PASSWORD" \
  -alias "$KEY_ALIAS" \
  -keypass "$KEY_PASSWORD" \
  -keyalg RSA \
  -keysize "$KEY_SIZE" \
  -validity "$VALIDITY_DAYS" \
  -dname "$DNAME"

keytool -export -rfc \
  -keystore "$KEYSTORE_PATH" \
  -storepass "$KEYSTORE_PASSWORD" \
  -alias "$KEY_ALIAS" \
  -file "$CERT_PATH"

KEYSTORE_BASE64="$(base64 < "$KEYSTORE_PATH" | tr -d '\n')"

umask 177
cat > "$SECRETS_FILE" <<EOF
BIZI_CI_KEYSTORE_BASE64=$KEYSTORE_BASE64
BIZI_CI_KEYSTORE_PASSWORD=$KEYSTORE_PASSWORD
BIZI_CI_KEY_ALIAS=$KEY_ALIAS
BIZI_CI_KEY_PASSWORD=$KEY_PASSWORD
EOF

echo
echo "Done."
echo
echo "Files created:"
echo "- Keystore: $KEYSTORE_PATH"
echo "- Public cert for Play Console: $CERT_PATH"
echo "- GitHub secrets file: $SECRETS_FILE"
echo
echo "Upload these values from $SECRETS_FILE into GitHub Actions secrets:"
echo "- BIZI_CI_KEYSTORE_BASE64"
echo "- BIZI_CI_KEYSTORE_PASSWORD"
echo "- BIZI_CI_KEY_ALIAS"
echo "- BIZI_CI_KEY_PASSWORD"
echo
echo "Then in Google Play Console:"
echo "- Go to Setup > App integrity"
echo "- Start the upload key reset/replacement flow"
echo "- Upload the certificate file: $CERT_PATH"
