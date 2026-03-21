# BiciRadar

Greenfield scaffold for `com.gcaguilar.biciradar` with:

- Kotlin Multiplatform for domain, data, and shared contracts.
- Compose Multiplatform for Android + iOS mobile.
- Compose for Wear OS for Wear.
- SwiftUI + App Intents shell for iPhone and Apple Watch.
- Metro DI as the compile-time container.
- Real location on Android, iOS, and watchOS, with Zaragoza city center as a fallback when permission is denied or no fix is available.
- Favorites sync between Android mobile and Wear OS through Data Layer.
- Embedded map in mobile Compose with a multiplatform wrapper.
- Official Zaragoza City Council feed with fallback to CityBikes.

## Modules

- `shared/core`: models, repositories, platform contracts, Metro graph, and Bizi client.
- `shared/mobile-ui`: shared Compose UI for mobile.
- `androidApp`: main Android app.
- `wearApp`: Wear OS app.
- `apple/`: base SwiftUI/App Intents sources for iOS/watchOS.

## Commands

```bash
./gradlew :shared:core:jvmTest
gradle :shared:mobile-ui:compileKotlinIosSimulatorArm64 :androidApp:compileDebugKotlinAndroid :wearApp:compileDebugKotlinAndroid
./gradlew build
```

## Configuration

- `GOOGLE_MAPS_API_KEY` is optional and enables real map tiles on Android.
- Android Crashlytics is enabled automatically when `androidApp/google-services.json` exists.
- Wear OS Crashlytics is enabled automatically when `wearApp/google-services.json` exists.
- iOS Crashlytics is enabled automatically when `apple/iosApp/GoogleService-Info.plist` exists and gets bundled into the app.

### Crashlytics setup

1. In Firebase, register the Android app `com.gcaguilar.biciradar` and download `google-services.json`.
2. Save that file at [androidApp/google-services.json](/Users/guillermo.castella/bizi/androidApp/google-services.json).
3. In Firebase, register the Wear OS app `com.gcaguilar.biciradar.wear` and download its `google-services.json`.
4. Save that file at [wearApp/google-services.json](/Users/guillermo.castella/bizi/wearApp/google-services.json).
5. In Firebase, register the iOS app `com.gcaguilar.biciradar.ios` and download `GoogleService-Info.plist`.
6. Save that file at [apple/iosApp/GoogleService-Info.plist](/Users/guillermo.castella/bizi/apple/iosApp/GoogleService-Info.plist).
7. Save your local iPhone Google Maps key in [apple/Config/LocalSecrets.xcconfig](/Users/guillermo.castella/bizi/apple/Config/LocalSecrets.xcconfig) using the `GOOGLE_MAPS_IOS_API_KEY` setting.
8. Rebuild the apps.

What the repository already does for you:

- Android applies `com.google.gms.google-services` and `com.google.firebase.crashlytics` only when the Firebase config file is present.
- Wear OS applies `com.google.gms.google-services` and `com.google.firebase.crashlytics` only when the Firebase config file is present.
- iOS configures `FirebaseApp` on startup only when `GoogleService-Info.plist` is bundled.
- The iOS target includes the Crashlytics package and uploads dSYMs during build when the plist is available.
- The repository only tracks [apple/Config/MapsConfig.xcconfig](/Users/guillermo.castella/bizi/apple/Config/MapsConfig.xcconfig). Your real iOS maps key lives in the ignored local file `apple/Config/LocalSecrets.xcconfig`.

Quick verification:

- Android: trigger a test crash and check that the event appears in Firebase Crashlytics.
- iOS: launch the app once with the plist bundled, trigger a test crash, and verify the event plus symbol upload.

## Release

The build and release checklist is available in `RELEASE.md`.

## Continuous Integration

GitHub Actions runs the workflow at `.github/workflows/build.yml` on pushes to `main`, on pull requests, and on manual dispatches.

It runs three jobs in parallel:

- `android`: runs shared JVM tests, Android unit tests, and builds the Android phone and Wear OS debug APKs.
- `ios`: runs iPhone tests and uploads a zipped `.app` bundle that can be installed on an iOS Simulator.
- `watchos`: runs Apple Watch tests and uploads a zipped `.app` bundle that can be installed on a watchOS Simulator.

Published artifacts:

- `android-debug-apks`
- `ios-simulator-app`
- `watchos-simulator-app`
- `ios-device-ipa` when Apple signing secrets are configured

Important note:

- Apple simulator installs use `.app` bundles, not `.ipa` files, so the workflow packages those bundles as `.zip` artifacts.
- Firebase App Distribution only receives installable device builds. In this repository that means Android APKs and, when signing is configured, a signed iOS IPA. Simulator `.app` bundles stay as GitHub Actions artifacts.

### Optional Firebase App Distribution

The same workflow can also distribute internal builds to Firebase App Distribution when the required GitHub secrets and variables are present.

Required secrets:

- `FIREBASE_SERVICE_ACCOUNT_JSON`: raw JSON of a Firebase service account with App Distribution access.
- `FIREBASE_ANDROID_APP_ID`: Firebase app id for the Android phone app.
- `FIREBASE_WEAR_ANDROID_APP_ID`: optional Firebase app id for the Wear OS app.
- `FIREBASE_IOS_APP_ID`: Firebase app id for the iOS app.
- `APPLE_TEAM_ID`: Apple Developer team id for signing the iOS IPA.
- `APPLE_SIGNING_CERTIFICATE_P12_BASE64`: base64-encoded `.p12` signing certificate for iOS distribution.
- `APPLE_SIGNING_CERTIFICATE_PASSWORD`: password for the `.p12`.
- `APPLE_PROVISIONING_PROFILE_BASE64`: base64-encoded provisioning profile for `com.gcaguilar.biciradar.ios`.
- `APPLE_KEYCHAIN_PASSWORD`: optional custom password for the temporary CI keychain.
- `APP_STORE_CONNECT_ISSUER_ID`: issuer id for the App Store Connect API key.
- `APP_STORE_CONNECT_KEY_ID`: key id for the App Store Connect API key.
- `APP_STORE_CONNECT_API_KEY_P8`: raw `.p8` App Store Connect API key contents.
- `APP_REVIEW_CONTACT_FIRST_NAME`, `APP_REVIEW_CONTACT_LAST_NAME`, `APP_REVIEW_CONTACT_EMAIL`, `APP_REVIEW_CONTACT_PHONE`: App Review contact data for App Store submission.
- `APP_REVIEW_NOTES`: optional notes for Apple App Review.

Optional GitHub repository variables:

- `FIREBASE_APP_DIST_GROUPS`: comma-separated Firebase tester groups.
- `FIREBASE_APP_DIST_TESTERS`: comma-separated tester emails.
- `APPLE_EXPORT_METHOD`: Xcode export method for the IPA. Recommended values are `debugging` for development-style builds, `release-testing` for ad hoc style builds, and `app-store` for App Store submissions.
- `APPLE_SIGNING_CERTIFICATE_TYPE`: optional explicit Xcode certificate selector such as `Apple Development` or `Apple Distribution`.
- `APPLE_BUNDLE_ID`: optional bundle id override for iOS release workflows. Defaults to `com.gcaguilar.biciradar.ios`.
- `APP_USES_ENCRYPTION`: `true` or `false` export-compliance value passed to App Store submission.

Behavior:

- `pull_request` runs keep building and testing, but skip Firebase distribution.
- `push` to `main` and manual runs distribute Android APKs when Firebase secrets are present.
- `push` to `main` and manual runs also export and distribute a signed iOS IPA when both Firebase and Apple signing secrets are present.
- watchOS simulator builds continue to upload as GitHub artifacts only.
- `.github/workflows/publish-ios-store.yml` is a separate manual workflow that builds a signed App Store IPA, uploads it to App Store Connect, submits it for review, and enables automatic release after approval.
- For public repositories, keep App Store secrets in a protected GitHub environment such as `app-store`; `scripts/print_ios_store_ci_values.sh` prints the current local values in a copy-pasteable format.

## Shortcuts and Voice

### iPhone with Siri and Shortcuts

Shortcuts published in [apple/iosApp/BiziShortcuts.swift](/Users/guillermo.castella/bizi/apple/iosApp/BiziShortcuts.swift):

- `Cuál es la estación más cercana con Bici Radar`
- `Dónde hay bicis cerca con Bici Radar`
- `Dónde puedo dejar la bici con Bici Radar`
- `Abre mis favoritas con Bici Radar`
- `Cómo está una estación con Bici Radar`
- `Cuántas bicis hay en una estación con Bici Radar`
- `Cuántos huecos hay en una estación con Bici Radar`
- `Llévame a una estación con Bici Radar`
- `Cómo está casa con Bici Radar`
- `Cómo está trabajo con Bici Radar`
- `Llévame a casa con Bici Radar`
- `Llévame a trabajo con Bici Radar`

Supported queries:

- nearest station
- nearest station with available bikes
- nearest station with free docks
- saved favorites
- available bikes at a station by name or station number
- free docks at a station by name or station number
- full station status by name
- route to a station by name
- full station status for the saved `Casa` and `Trabajo` stations
- route to the saved `Casa` and `Trabajo` stations

Current behavior:

- `nearest station`, `with bikes`, `with free docks`, `favorites`, and `route` open the app in the corresponding state.
- `status`, `bikes at station`, and `free docks at station` respond directly in Siri/Shortcuts without opening the app.

### Apple Watch with Siri and Shortcuts

Shortcuts published in [apple/watchApp/BiziWatchShortcuts.swift](/Users/guillermo.castella/bizi/apple/watchApp/BiziWatchShortcuts.swift):

- `Cuál es la estación más cercana con Bici Radar`
- `Dónde hay bicis cerca con Bici Radar`
- `Dónde puedo dejar la bici con Bici Radar`
- `Abre mis favoritas con Bici Radar`
- `Cuántas bicis hay en una estación con Bici Radar`
- `Cuántos huecos hay en una estación con Bici Radar`
- `Llévame a una estación con Bici Radar`
- `Cómo está casa con Bici Radar`
- `Cómo está trabajo con Bici Radar`
- `Llévame a casa con Bici Radar`
- `Llévame a trabajo con Bici Radar`

Current behavior:

- the watch responds by voice and does not need to open the app for nearest-station, bikes, docks, or favorites queries.
- the route action asks the paired iPhone to open navigation to the requested station.

### Android with Google Assistant

Android exposes actions and shortcuts in [androidApp/src/androidMain/res/xml/shortcuts.xml](/Users/guillermo.castella/bizi/androidApp/src/androidMain/res/xml/shortcuts.xml) and parses launches in [androidApp/src/androidMain/kotlin/com/gcaguilar/biciradar/AndroidLaunchRequestParser.kt](/Users/guillermo.castella/bizi/androidApp/src/androidMain/kotlin/com/gcaguilar/biciradar/AndroidLaunchRequestParser.kt).

Supported actions:

- `nearest_station`
- `nearest_station_with_bikes`
- `nearest_station_with_slots`
- `favorite_stations`
- `station_status`
- `station_bike_count`
- `station_slot_count`
- `route_to_station`
- `show_station`

Saved place aliases supported by Android launch resolution:

- `casa`
- `mi casa`
- `home`
- `trabajo`
- `mi trabajo`
- `work`
- `oficina`
- `mi oficina`

Example target phrases for Assistant:

- `cuál es la estación más cercana con Bici Radar`
- `dónde hay bicis cerca con Bici Radar`
- `dónde puedo dejar la bici con Bici Radar`
- `abre mis favoritas con Bici Radar`
- `cómo está una estación con Bici Radar`
- `cuántas bicis hay en una estación con Bici Radar`
- `cuántos huecos hay en una estación con Bici Radar`
- `llévame a una estación con Bici Radar`
- `cómo está casa con Bici Radar`
- `cómo está trabajo con Bici Radar`
- `llévame a casa con Bici Radar`
- `llévame al trabajo con Bici Radar`

Important note:

- on Android, the most reliable way to test this in the emulator is to launch the intent directly.
- natural phrase capture for station names still depends on Google Assistant matching on the device.

`adb` test commands:

```bash
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'biciradar://assistant?action=nearest_station' com.gcaguilar.biciradar
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'biciradar://assistant?action=nearest_station_with_bikes' com.gcaguilar.biciradar
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'biciradar://assistant?action=nearest_station_with_slots' com.gcaguilar.biciradar
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'biciradar://assistant?action=favorite_stations' com.gcaguilar.biciradar
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'biciradar://assistant?action=station_status&station_query=Plaza%20Espa%C3%B1a' com.gcaguilar.biciradar
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'biciradar://assistant?action=station_bike_count&station_query=Plaza%20Espa%C3%B1a' com.gcaguilar.biciradar
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'biciradar://assistant?action=station_slot_count&station_query=48' com.gcaguilar.biciradar
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'biciradar://assistant?action=route_to_station&station_query=Plaza%20Espa%C3%B1a' com.gcaguilar.biciradar
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'biciradar://assistant?station_query=Plaza%20Espa%C3%B1a' com.gcaguilar.biciradar
```

Android smoke validation:

- Maestro flow: [maestro/android/assistant-smoke.yaml](/Users/guillermo.castella/bizi/maestro/android/assistant-smoke.yaml)
- Convenience script: [scripts/run_android_assistant_smoke.sh](/Users/guillermo.castella/bizi/scripts/run_android_assistant_smoke.sh)

Example:

```bash
./scripts/run_android_assistant_smoke.sh emulator-5554
```

## Apple

The generated Xcode project lives at `apple/BiciRadar.xcodeproj`. The SwiftUI/App Intents base is in `apple/`, and the KMP frameworks consumed there are:

- `BiziSharedCore`
- `BiziMobileUi`
- `BiziMobileUi` exports `MainViewController()` so it can be integrated from SwiftUI/UIKit

Apple smoke validation:

- Shortcut logic tests: [apple/iosAppTests/AppleShortcutRunnerTests.swift](/Users/guillermo.castella/bizi/apple/iosAppTests/AppleShortcutRunnerTests.swift)
- Launch-request store tests: [apple/iosAppTests/AppleLaunchRequestStoreTests.swift](/Users/guillermo.castella/bizi/apple/iosAppTests/AppleLaunchRequestStoreTests.swift)
- UI smoke tests: [apple/iosAppUITests/BiciRadarUITests.swift](/Users/guillermo.castella/bizi/apple/iosAppUITests/BiciRadarUITests.swift)
- Convenience script: [scripts/run_ios_smoke.sh](/Users/guillermo.castella/bizi/scripts/run_ios_smoke.sh)

Example:

```bash
./scripts/run_ios_smoke.sh "platform=iOS Simulator,name=iPhone 17 Pro,OS=26.2"
```

Apple Watch smoke validation:

- Watch shortcut tests: [apple/watchAppTests/WatchShortcutRunnerTests.swift](/Users/guillermo.castella/bizi/apple/watchAppTests/WatchShortcutRunnerTests.swift)
- Watch dashboard tests: [apple/watchAppTests/WatchDashboardModelTests.swift](/Users/guillermo.castella/bizi/apple/watchAppTests/WatchDashboardModelTests.swift)
- Convenience script: [scripts/run_watch_smoke.sh](/Users/guillermo.castella/bizi/scripts/run_watch_smoke.sh)

Example:

```bash
./scripts/run_watch_smoke.sh "platform=watchOS Simulator,name=Apple Watch Series 11 (46mm),OS=26.2"
```
