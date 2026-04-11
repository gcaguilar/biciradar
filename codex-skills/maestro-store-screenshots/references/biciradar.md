# BiciRadar Repo Notes

Use this file only when the task is for this repository.

## App IDs and Bundle IDs

- Android / Wear OS application id: `com.gcaguilar.biciradar`
- iPhone bundle id: `com.gcaguilar.biciradar.ios`
- Apple Watch bundle id: `com.gcaguilar.biciradar.ios.watch`

## Existing Helpers

- Existing Maestro smoke flow: `/Users/guillermo.castella/biciradar/maestro/android/assistant-smoke.yaml`
- Smoke wrapper: `/Users/guillermo.castella/biciradar/tooling/project/run_smoke.sh`
- iOS/watchOS simulator destination resolver: `/Users/guillermo.castella/biciradar/tooling/generic-mobile-ci/resolve_xcode_destination.sh`
- Release/store notes: `/Users/guillermo.castella/biciradar/RELEASE.md`

## Useful Build / Install Commands

### Android phone

```bash
./gradlew :androidApp:installDebug
```

### Wear OS

```bash
./gradlew :wearApp:installDebug
```

### iPhone simulator build

```bash
xcodebuild \
  -project apple/BiciRadar.xcodeproj \
  -scheme BiciRadar \
  -sdk iphonesimulator \
  -destination "$(/Users/guillermo.castella/biciradar/tooling/generic-mobile-ci/resolve_xcode_destination.sh ios)" \
  build
```

### Apple Watch simulator build

```bash
xcodebuild \
  -project apple/BiciRadar.xcodeproj \
  -scheme BiciRadarWatch \
  -sdk watchsimulator \
  -destination "$(/Users/guillermo.castella/biciradar/tooling/generic-mobile-ci/resolve_xcode_destination.sh watchos)" \
  build
```

### Apple Watch capture helper

```bash
bash /Users/guillermo.castella/biciradar/codex-skills/maestro-store-screenshots/scripts/run_biciradar_watch_store_capture.sh \
  BB46A5C0-BC43-4AD7-9E0F-46EECD968EE3 \
  /tmp/biciradar-store/apple-watch
```

## Existing Deep Links Confirmed In Tests

These already appear in the Android Maestro smoke flow and are the safest starting points for deterministic screenshots:

- `biciradar://city/zaragoza`
- `biciradar://favorites`
- `biciradar://shortcuts`
- `biciradar://station/5a8156a1697a8ad877e70472210e4b91`
- `biciradar://assistant?action=station_slot_count&station_query=48`

## Good First Screenshot Set

- `01-home-map`
- `02-favorites`
- `03-nearby`
- `04-station-detail`

## Known Constraints

- The repo currently has unit tests for iOS and watchOS, but no existing Apple UI-test screenshot harness.
- If watchOS screenshots must show synced or dynamic state, seed the paired phone/watch state before capturing.
- The default visible strings in current flows are Spanish. If you boot an English simulator, duplicate the Flow and update selectors to the visible locale.
