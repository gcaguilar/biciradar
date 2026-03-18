# Release Checklist

## Android and Wear OS

- Define `GOOGLE_MAPS_API_KEY` if you want real Android map tiles.
- Add [androidApp/google-services.json](/Users/guillermo.castella/bizi/androidApp/google-services.json) to enable Firebase Crashlytics on Android.
- Add [wearApp/google-services.json](/Users/guillermo.castella/bizi/wearApp/google-services.json) to enable Firebase Crashlytics on Wear OS.
- Configure release signing in `androidApp` and `wearApp`.
- Generate builds:

```bash
./gradlew :androidApp:assembleRelease :wearApp:assembleRelease
```

## iOS and watchOS

- Add [apple/iosApp/GoogleService-Info.plist](/Users/guillermo.castella/bizi/apple/iosApp/GoogleService-Info.plist) to enable Firebase Crashlytics on iOS.
- Open [apple/BiciRadar.xcodeproj](/Users/guillermo.castella/bizi/apple/BiciRadar.xcodeproj).
- Configure `Team`, signing, and provisioning for `BiciRadar`, `BiciRadarWatch`, and their tests.
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
