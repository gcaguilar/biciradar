# Release Checklist

## Android and Wear OS

- Define `GOOGLE_MAPS_API_KEY` if you want real Android map tiles.
- Configure release signing in `androidApp` and `wearApp`.
- Generate builds:

```bash
./gradlew :androidApp:assembleRelease :wearApp:assembleRelease
```

## iOS and watchOS

- Open [apple/BiziZaragoza.xcodeproj](/Users/guillermo.castella/bizi/apple/BiziZaragoza.xcodeproj).
- Configure `Team`, signing, and provisioning for `BiziZaragoza`, `BiziZaragozaWatch`, and their tests.
- Generate archives:

```bash
xcodebuild -project apple/BiziZaragoza.xcodeproj -scheme BiziZaragoza -configuration Release archive
xcodebuild -project apple/BiziZaragoza.xcodeproj -scheme BiziZaragozaWatch -configuration Release archive
```

## Minimum QA

- Real location on Android, iPhone, Wear OS, and Apple Watch.
- Favorites synced between mobile and watch.
- Siri/App Shortcuts on Apple platforms.
- Android shortcuts and station lookup by name or number.
- Native routing to a station.
- Validate the configurable-radius fallback when there are no stations within the threshold.
