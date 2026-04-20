# Generic Mobile CI Scripts

These scripts are the reusable part of this repository's release automation. They are written so you can copy them into another Android/iOS project and adapt paths, secret names, and workflow steps.

## Included scripts

- `install_apple_signing_assets.sh`: imports an Apple signing certificate and provisioning profile into the CI keychain.
- `export_ios_ipa.sh`: archives and exports a signed iOS IPA with manual signing.
- `resolve_xcode_destination.sh`: picks a usable iOS or watchOS simulator destination for CI.
- `print_ios_store_ci_values.sh`: prints GitHub `secrets` and `vars` values for an App Store workflow.
- `firebase_app_distribution.sh`: uploads an APK/IPA/AAB to Firebase App Distribution.
- `generate_android_upload_key.sh`: generates an Android upload keystore and a copy-pasteable secrets file.
- `upload_android_github_secrets.sh`: uploads `KEY=VALUE` pairs to GitHub Actions secrets with `gh`.
- `write_release_notes.sh`: writes a simple release notes file from git and CI metadata.

## Generic usage patterns

### iOS signing in CI

```bash
./tooling/generic-mobile-ci/install_apple_signing_assets.sh
./tooling/generic-mobile-ci/export_ios_ipa.sh "$RUNNER_TEMP/ios-export" "$RUNNER_TEMP/App.xcarchive"
```

Required environment variables:

- `APPLE_SIGNING_CERTIFICATE_P12_BASE64`
- `APPLE_SIGNING_CERTIFICATE_PASSWORD`
- `APPLE_PROVISIONING_PROFILE_BASE64`
- `APPLE_TEAM_ID`
- `APPLE_PROFILE_NAME`

Optional environment variables:

- `APPLE_KEYCHAIN_PASSWORD`
- `APPLE_XCODE_PROJECT_PATH`
- `APPLE_XCODE_SCHEME`
- `APPLE_BUNDLE_ID`
- `APPLE_EXPORT_METHOD`
- `APPLE_SIGNING_CERTIFICATE_TYPE`

### Simulator selection for tests

```bash
./tooling/generic-mobile-ci/resolve_xcode_destination.sh ios
./tooling/generic-mobile-ci/resolve_xcode_destination.sh watchos
```

Use the returned value as the `xcodebuild -destination` argument.

### App Store secret preparation

```bash
./tooling/generic-mobile-ci/print_ios_store_ci_values.sh
```

The script is opinionated toward GitHub Actions and App Store Connect. By default it looks for assets in this repository layout, but you can override paths with:

- `APPLE_CERT_DIR`
- `IOS_APP_DIR`
- `APPLE_P12_PATH`
- `APPLE_PROFILE_PATH`
- `APPLE_APP_STORE_CONNECT_P8_PATH`
- `GOOGLE_SERVICE_INFO_PLIST_PATH`

### Firebase App Distribution

```bash
PROJECT_NAME="Example App" ./tooling/generic-mobile-ci/write_release_notes.sh build/release-notes.txt beta
./tooling/generic-mobile-ci/firebase_app_distribution.sh app-release.apk "$FIREBASE_APP_ID" build/release-notes.txt apk
```

Optional distribution environment variables:

- `FIREBASE_APP_DIST_TESTERS`
- `FIREBASE_APP_DIST_GROUPS`

### Android upload key bootstrap

```bash
./tooling/generic-mobile-ci/generate_android_upload_key.sh
./tooling/generic-mobile-ci/upload_android_github_secrets.sh github-secrets.txt owner/repo
```

The generated secret names mirror this repository's workflow conventions. If your target project uses different names, edit the output file before uploading it.
