# Native surfaces

This repo now persists a compact surface snapshot for widgets, live activities and shortcuts.

## Shared snapshot

- Path: platform storage root + `surface_snapshot.json`
- Android root: app files directory `/bizi`
- iOS root: App Group container `group.com.gcaguilar.biciradar` under `/bizi`

The snapshot includes:

- favorite station summary
- nearby stations summary
- shared freshness and city state
- active monitoring session when one exists

## Android

- Home widget: `androidApp/src/main/kotlin/com/gcaguilar/biciradar/FavoriteStationWidgetProvider.kt`
- Ongoing monitoring notification: `androidApp/src/main/kotlin/com/gcaguilar/biciradar/TripMonitorService.kt`
- Dynamic shortcuts: `androidApp/src/main/kotlin/com/gcaguilar/biciradar/AndroidDynamicShortcuts.kt`

## iOS

- Shared App Group helpers: `apple/sharedApple/BiziSharedStorage.swift`
- Snapshot reader: `apple/sharedApple/BiziSurfaceStore.swift`
- Live Activity controller: `apple/iosApp/SurfaceMonitoringActivityController.swift`
- Widget extension sources: `apple/iosWidgets`

## Project generation

The widget extension target was added to `apple/project.yml`.
If the checked-in Xcode project is out of sync, regenerate `apple/BiciRadar.xcodeproj` from `apple/project.yml` before shipping from macOS.
