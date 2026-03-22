#!/usr/bin/env bash

set -euo pipefail

p12_path="${1:-apple-cert/ios_distribution.p12}"

if [[ ! -f "$p12_path" ]]; then
  printf 'P12 file not found: %s\n' "$p12_path" >&2
  exit 1
fi

if [[ -z "${P12_PASS:-}" ]]; then
  read -r -s -p "P12 password: " P12_PASS
  printf '\n'
fi

tmp_cert="$(mktemp)"
tmp_err="$(mktemp)"
trap 'rm -f "$tmp_cert" "$tmp_err"' EXIT

if openssl pkcs12 \
  -in "$p12_path" \
  -nokeys \
  -clcerts \
  -nodes \
  -passin env:P12_PASS \
  >"$tmp_cert" 2>"$tmp_err"; then
  printf 'OK: password correcta para %s\n' "$p12_path"
  exit 0
fi

printf 'ERROR: password incorrecta o P12 invalido para %s\n' "$p12_path" >&2
if [[ -s "$tmp_err" ]]; then
  cat "$tmp_err" >&2
fi
exit 1
