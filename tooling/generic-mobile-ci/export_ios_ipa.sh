#!/usr/bin/env bash

set -euo pipefail

: "${APPLE_TEAM_ID:?APPLE_TEAM_ID is required}"
: "${APPLE_PROFILE_NAME:?APPLE_PROFILE_NAME is required}"

export_path="${1:?usage: export_ios_ipa.sh <export-path> [archive-path]}"
archive_path="${2:-${RUNNER_TEMP:-/tmp}/BiciRadar.xcarchive}"
project_path="${APPLE_XCODE_PROJECT_PATH:-apple/BiciRadar.xcodeproj}"
scheme_name="${APPLE_XCODE_SCHEME:-BiciRadar}"
bundle_identifier="${APPLE_BUNDLE_ID:-com.gcaguilar.biciradar.ios}"
export_method="${APPLE_EXPORT_METHOD:-debugging}"
signing_certificate="${APPLE_SIGNING_CERTIFICATE_TYPE:-}"
export_options_plist="${RUNNER_TEMP:-/tmp}/BiciRadar-export-options.plist"

mkdir -p "$export_path"

cat > "$export_options_plist" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>destination</key>
  <string>export</string>
  <key>method</key>
  <string>${export_method}</string>
  <key>provisioningProfiles</key>
  <dict>
    <key>${bundle_identifier}</key>
    <string>${APPLE_PROFILE_NAME}</string>
  </dict>
  <key>signingStyle</key>
  <string>manual</string>
  <key>stripSwiftSymbols</key>
  <true/>
  <key>teamID</key>
  <string>${APPLE_TEAM_ID}</string>
</dict>
</plist>
EOF

if [[ -n "$signing_certificate" ]]; then
  /usr/libexec/PlistBuddy -c "Add :signingCertificate string ${signing_certificate}" "$export_options_plist"
fi

xcodebuild \
  -project "$project_path" \
  -scheme "$scheme_name" \
  -configuration Release \
  -destination 'generic/platform=iOS' \
  -archivePath "$archive_path" \
  DEVELOPMENT_TEAM="$APPLE_TEAM_ID" \
  CODE_SIGN_STYLE=Manual \
  PROVISIONING_PROFILE_SPECIFIER="$APPLE_PROFILE_NAME" \
  archive

xcodebuild \
  -exportArchive \
  -archivePath "$archive_path" \
  -exportPath "$export_path" \
  -exportOptionsPlist "$export_options_plist"
