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

## Shared use cases

- `GetFavoriteStations`
- `GetNearestStations`
- `GetStationStatus`
- `StartStationMonitoring`
- `StopStationMonitoring`
- `GetSuggestedAlternativeStation`
- `GetCachedStationSnapshot`
- `RefreshStationDataIfNeeded`

## Android

- Home widget: `androidApp/src/main/kotlin/com/gcaguilar/biciradar/FavoriteStationWidgetProvider.kt`
- Nearby widget: `androidApp/src/main/kotlin/com/gcaguilar/biciradar/NearbyStationsWidgetProvider.kt`
- Quick actions widget: `androidApp/src/main/kotlin/com/gcaguilar/biciradar/QuickActionsWidgetProvider.kt`
- Ongoing monitoring notification: `androidApp/src/main/kotlin/com/gcaguilar/biciradar/TripMonitorService.kt`
- Dynamic shortcuts: `androidApp/src/main/kotlin/com/gcaguilar/biciradar/AndroidDynamicShortcuts.kt`

## iOS

- Shared App Group helpers: `apple/sharedApple/BiziSharedStorage.swift`
- Snapshot reader: `apple/sharedApple/BiziSurfaceStore.swift`
- Live Activity controller: `apple/iosApp/SurfaceMonitoringActivityController.swift`
- Widget extension sources: `apple/iosWidgets`
- Watch parity checklist: `docs/WATCH_PARITY_CHECKLIST.md`

## Project generation

The widget extension target was added to `apple/project.yml`.
If the checked-in Xcode project is out of sync, regenerate `apple/BiciRadar.xcodeproj` from `apple/project.yml` before shipping from macOS.
