# BiciRadar

Multiplatform app to check public bike availability across Spanish cities.

This README is developer-oriented: architecture, local setup, commands, and CI/CD.
End-user documentation lives in `docs/wiki/` (GitHub Wiki format).

## Technical stack

- Kotlin Multiplatform for domain, data, and shared contracts.
- Compose Multiplatform for shared mobile UI (Android + iOS).
- Compose for Wear OS.
- SwiftUI + App Intents for iOS and watchOS.
- Metro DI for compile-time dependency injection.
- Maps and geolocation integration on Android, iOS, and watchOS.

## Repository structure

- `shared/core`: models, repositories, platform contracts, Metro graph, and Bizi client.
- `shared/mobile-ui`: shared UI for mobile apps.
- `androidApp`: main Android app.
- `wearApp`: Wear OS app.
- `apple`: SwiftUI/App Intents shell for iOS and watchOS.
- `docs`: technical plans and internal documentation.
- `docs/wiki`: end-user functional documentation.

## Local setup

1. Install JDK and mobile toolchains (Android SDK and Xcode on macOS for Apple targets).
2. Clone the repository and open the project root.
3. Run a baseline build to verify toolchains.

```bash
./gradlew build
```

## Development commands

### Build and tests

```bash
./gradlew :shared:core:jvmTest
./gradlew :shared:mobile-ui:compileKotlinIosSimulatorArm64
./gradlew :androidApp:compileDebugKotlinAndroid
./gradlew :wearApp:compileDebugKotlinAndroid
./gradlew build
```

### Quick smoke tests

Unified script:

```bash
./tooling/project/run_smoke.sh
```

Examples:

```bash
./tooling/project/run_smoke.sh android-assistant emulator-5554
./tooling/project/run_smoke.sh ios "platform=iOS Simulator,name=iPhone 17 Pro,OS=26.2"
./tooling/project/run_smoke.sh watchos "platform=watchOS Simulator,name=Apple Watch Series 11 (46mm),OS=26.2"
```

## Local configuration

- `GOOGLE_MAPS_API_KEY`: optional in local development; enables real map tiles on Android. iOS CI workflows also use it.
- Android Crashlytics is enabled automatically when `androidApp/google-services.json` exists.
- Wear OS Crashlytics is enabled automatically when `wearApp/google-services.json` exists.
- iOS Crashlytics is enabled automatically when `apple/iosApp/GoogleService-Info.plist` exists.

### Firebase/Crashlytics

1. Register apps in Firebase:
   - Android: `com.gcaguilar.biciradar`
   - Wear OS: `com.gcaguilar.biciradar.wear`
   - iOS: `com.gcaguilar.biciradar.ios`
2. Place configuration files:
   - `androidApp/google-services.json`
   - `wearApp/google-services.json`
   - `apple/iosApp/GoogleService-Info.plist`
3. Configure local iOS maps key in `apple/Config/LocalSecrets.xcconfig` using `GOOGLE_MAPS_IOS_API_KEY`.
4. Rebuild.

The repository already includes safe fallbacks when Firebase files are missing on Android/Wear and iOS.

## CI/CD

Main workflow: `.github/workflows/build.yml`.

It runs on `push` to `main`, `pull_request`, and manual dispatch, with parallel jobs:

- `android`: shared JVM tests, Android unit tests, and debug APK builds (phone + wear).
- `ios`: iPhone tests and simulator `.app` artifact.
- `watchos`: Apple Watch tests and simulator `.app` artifact.

Published artifacts:

- `android-debug-apks`
- `ios-simulator-app`
- `watchos-simulator-app`
- `ios-device-ipa` (when Apple signing secrets are configured)

### Optional Firebase App Distribution

The same workflow can distribute internal builds when required Firebase and Apple signing secrets/variables are present.

Review and manage these values in GitHub Secrets/Variables:

- Firebase: `FIREBASE_SERVICE_ACCOUNT_JSON`, `FIREBASE_ANDROID_APP_ID`, `FIREBASE_WEAR_ANDROID_APP_ID`, `FIREBASE_IOS_APP_ID`.
- Apple signing: `APPLE_TEAM_ID`, `APPLE_SIGNING_CERTIFICATE_P12_BASE64`, `APPLE_SIGNING_CERTIFICATE_PASSWORD`, `APPLE_PROVISIONING_PROFILE_BASE64`, `APPLE_KEYCHAIN_PASSWORD`.
- App Store Connect: `APP_STORE_CONNECT_ISSUER_ID`, `APP_STORE_CONNECT_KEY_ID`, `APP_STORE_CONNECT_API_KEY_P8`.
- Maps/review config: `GOOGLE_MAPS_API_KEY`, `APP_REVIEW_CONTACT_FIRST_NAME`, `APP_REVIEW_CONTACT_LAST_NAME`, `APP_REVIEW_CONTACT_EMAIL`, `APP_REVIEW_CONTACT_PHONE`, `APP_REVIEW_NOTES`.

App Store publishing workflow: `.github/workflows/publish-ios-store.yml` (manual).

## Release

Build and release checklist: `RELEASE.md`.

## User wiki

End-user content:

- `docs/wiki/Home.md`: wiki homepage (recommended).
- `docs/wiki/GUIA_USUARIO.md`: consolidated one-page guide.
