# Release Checklist

## Android and Wear OS

- Define `GOOGLE_MAPS_API_KEY` if you want real Android map tiles.
- Add [androidApp/google-services.json](androidApp/google-services.json) to enable Firebase Crashlytics on Android.
- Add [wearApp/google-services.json](wearApp/google-services.json) to enable Firebase Crashlytics on Wear OS.
- Configure release signing in `androidApp` and `wearApp`.
- Both apps share the same `applicationId` (com.gcaguilar.biciradar) and are published to the same Play Store listing using different form factor tracks.
- Generate builds:

```bash
./gradlew :androidApp:assemblePlaystoreRelease :wearApp:assemblePlaystoreRelease
```

### Play Console Form Factor Configuration

To publish both mobile and WearOS versions under the same app listing:

1. Go to Play Console → **Test and release** → **Setup** → **Advanced settings**
2. Select the **Form factors** tab
3. Click **Add form factor** → Select **Wear OS**
4. Upload Wear OS screenshots (minimum 384x384px, 1:1 aspect ratio)
5. Upload your Wear OS-enabled app bundle to a test track first
6. Return to Advanced settings and click **Manage** next to "Wear OS"
7. Opt-in to distribution and review the terms of service

The CI workflow publishes:
- Mobile app to the `alpha` track
- WearOS app to the `wear:alpha` dedicated form factor track

Users will see one app listing, but Google Play serves the appropriate version based on their device.

## F-Droid

- F-Droid package IDs:
  - Android: `com.gcaguilar.biciradar.fdroid`
- Generate the release APKs:

```bash
./gradlew :androidApp:assembleFdroidRelease
```

- Each `assembleFdroidRelease` run also executes a dependency gate that fails when the runtime classpath includes Google Play Services, Firebase, Garmin Connect IQ, or Google Maps Compose artifacts.
- Run the repository-level submission check:

```bash
bash tooling/project/check_fdroid_submission.sh
```

- Submission-ready metadata lives in:
  - [docs/fdroid/README.md](docs/fdroid/README.md)
  - [metadata/com.gcaguilar.biciradar.fdroid.yml](metadata/com.gcaguilar.biciradar.fdroid.yml)
  - `fastlane/metadata/android/en-US/`
- The YAML files are the parts that go into `fdroiddata`; the fastlane text and screenshots stay in this repository.
- Before sending the MR to `fdroiddata`, replace the placeholder screenshot directories with real captures, update the metadata `commit` to the full 40-character hash of the release commit, run `.github/workflows/publish-fdroid.yml` for the matching tag so `Binaries` resolves to a real signed APK, and keep `AllowedAPKSigningKeys` aligned with `android-cert/bici-upload-v2.pem`. The GitHub Release title can vary, but the tag and asset name must still match the `Binaries` pattern.
- Keep `UpdateCheckMode` restricted to F-Droid tags and `UpdateCheckData` aligned with the Android Gradle file so F-Droid discovers `versionCode` from source and `versionName` from tags like `0.22.9-fdroid`.
- The Wear OS `fdroid` flavor remains in the codebase for future work, but it is not part of the current submission checklist.

## iOS and watchOS

- Add [apple/iosApp/GoogleService-Info.plist](apple/iosApp/GoogleService-Info.plist) to enable Firebase Crashlytics on iOS.
- Open [apple/BiciRadar.xcodeproj](apple/BiciRadar.xcodeproj).
- Configure `Team`, signing, and provisioning for `BiciRadar`, `BiciRadarWatch`, and their tests.
- For App Store publication from CI, configure the dedicated workflow in `.github/workflows/publish-ios-store.yml`.
- Add App Store Connect secrets `APP_STORE_CONNECT_ISSUER_ID`, `APP_STORE_CONNECT_KEY_ID`, and `APP_STORE_CONNECT_API_KEY_P8`.
- Add `GOOGLE_MAPS_API_KEY` if you want iOS CI builds to bundle the Google Maps key.
- Add review-compliance secrets `APP_REVIEW_CONTACT_FIRST_NAME`, `APP_REVIEW_CONTACT_LAST_NAME`, `APP_REVIEW_CONTACT_EMAIL`, `APP_REVIEW_CONTACT_PHONE`, and optional `APP_REVIEW_NOTES`.
- Add the repository variable `APP_USES_ENCRYPTION` and keep the App Store workflow secrets in a protected environment such as `app-store` for public repositories.
- Set `APPLE_EXPORT_METHOD=app-store` and `APPLE_SIGNING_CERTIFICATE_TYPE=Apple Distribution` for the store workflow.
- Run `tooling/generic-mobile-ci/print_ios_store_ci_values.sh` locally to print the exact secret and variable values to paste into GitHub.
- Generate archives:

```bash
xcodebuild -project apple/BiciRadar.xcodeproj -scheme BiciRadar -configuration Release archive
xcodebuild -project apple/BiciRadar.xcodeproj -scheme BiciRadarWatch -configuration Release archive
```

## Firebase App Distribution

GitHub Actions can distribute installable internal builds directly to Firebase App Distribution:

- Android phone: debug APK
- Wear OS: debug APK when `FIREBASE_WEAR_ANDROID_APP_ID` is configured
- iOS: signed IPA for device installs

GitHub Actions does not upload simulator bundles to Firebase. Those remain GitHub artifacts:

- iOS Simulator: zipped `.app`
- watchOS Simulator: zipped `.app`

Repository secrets for CI distribution:

- `FIREBASE_SERVICE_ACCOUNT_JSON`
- `FIREBASE_ANDROID_APP_ID`
- `FIREBASE_WEAR_ANDROID_APP_ID` optional
- `FIREBASE_IOS_APP_ID`
- `APPLE_TEAM_ID`
- `APPLE_SIGNING_CERTIFICATE_P12_BASE64`
- `APPLE_SIGNING_CERTIFICATE_PASSWORD`
- `APPLE_PROVISIONING_PROFILE_BASE64`
- `APPLE_KEYCHAIN_PASSWORD` optional

Repository variables:

- `FIREBASE_APP_DIST_GROUPS`
- `FIREBASE_APP_DIST_TESTERS`
- `APPLE_EXPORT_METHOD`
- `APPLE_SIGNING_CERTIFICATE_TYPE`

Recommended Apple export methods:

- `debugging` for development provisioning profiles
- `release-testing` for ad hoc style provisioning profiles

## Minimum QA

- Real location on Android, iPhone, Wear OS, and Apple Watch.
- Favorites synced between mobile and watch.
- Siri/App Shortcuts on Apple platforms.
- Android shortcuts and station lookup by name or number.
- Native routing to a station.
- Validate the configurable-radius fallback when there are no stations within the threshold.
