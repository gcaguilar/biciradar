# Native surfaces

This repo persists a compact surface snapshot for widgets, notifications, shortcuts, and watch surfaces.

## Shared snapshot

- Path: platform storage root + `surface_snapshot.json`
- Android / Wear OS root: app files directory under `/bizi`
- iOS / watchOS root: App Group container `group.com.gcaguilar.biciradar` under `/bizi`

The snapshot includes:

- favorite station summary
- nearby stations summary
- shared freshness and city state
- active monitoring session when one exists
- suggested alternative station data when monitoring finds one

## Shared use cases

- `GetFavoriteStations`
- `GetNearestStations`
- `GetStationStatus`
- `StartStationMonitoring`
- `StopStationMonitoring`
- `GetSuggestedAlternativeStation`
- `GetCachedStationSnapshot`
- `RefreshStationDataIfNeeded`

## Shared contracts and storage

- Snapshot repository: `shared/core/src/commonMain/kotlin/com/gcaguilar/biciradar/core/SurfaceRepositories.kt`
- Surface DTOs: `shared/core/src/commonMain/kotlin/com/gcaguilar/biciradar/core/SurfaceModels.kt`
- Monitoring repository: `shared/core/src/commonMain/kotlin/com/gcaguilar/biciradar/core/SurfaceMonitoringRepository.kt`
- Shared use cases: `shared/core/src/commonMain/kotlin/com/gcaguilar/biciradar/core/SurfaceUseCases.kt`

## Android

- Favorite home widget: `androidApp/src/main/kotlin/com/gcaguilar/biciradar/FavoriteStationWidgetProvider.kt`
- Nearby widget: `androidApp/src/main/kotlin/com/gcaguilar/biciradar/NearbyStationsWidgetProvider.kt`
- Quick actions widget: `androidApp/src/main/kotlin/com/gcaguilar/biciradar/QuickActionsWidgetProvider.kt`
- Commute widget: `androidApp/src/main/kotlin/com/gcaguilar/biciradar/CommuteWidgetProvider.kt`
- Ongoing monitoring notification: `androidApp/src/main/kotlin/com/gcaguilar/biciradar/TripMonitorService.kt`
- Monitoring notification rendering helpers: `androidApp/src/main/kotlin/com/gcaguilar/biciradar/AndroidSurfaceRendering.kt`
- Dynamic shortcuts: `androidApp/src/main/kotlin/com/gcaguilar/biciradar/AndroidDynamicShortcuts.kt`
- Launch / deep-link parsing: `androidApp/src/main/kotlin/com/gcaguilar/biciradar/AndroidLaunchRequestParser.kt`

Current Android shortcut coverage:

- dynamic launcher shortcuts for nearby, favorites, favorite monitoring, and saved places `Casa` / `Trabajo`
- shortcut ranking trims to the launcher limit while avoiding duplicate station-open entries when a saved place matches the favorite

Current Android deep links:

- `biciradar://home`
- `biciradar://map`
- `biciradar://station/{id}`
- `biciradar://favorites`
- `biciradar://monitor/{id}`
- `biciradar://city/{id}`

## Wear OS

- Main Wear dashboard: `wearApp/src/main/kotlin/com/gcaguilar/biciradar/wear/WearActivity.kt`
- Wear presentation helpers: `wearApp/src/main/kotlin/com/gcaguilar/biciradar/wear/WearStationPresentation.kt`
- Favorite Tile provider: `wearApp/src/main/kotlin/com/gcaguilar/biciradar/wear/FavoriteStationTileService.kt`

Current Wear OS coverage:

- nearby stations list
- favorites list with home/work ordering
- saved-place quick routes for `Casa` and `Trabajo`
- favorite quick-glance surface from the shared snapshot
- favorite Tile backed by the shared snapshot, with direct open into station detail
- active station monitoring card with countdown and alternative
- station detail actions for route, favorite toggle, start monitoring, and stop monitoring

## iOS

- Shared App Group helpers: `apple/sharedApple/BiziSharedStorage.swift`
- Snapshot reader: `apple/sharedApple/BiziSurfaceStore.swift`
- Live Activity controller: `apple/iosApp/SurfaceMonitoringActivityController.swift`
- Widget extension sources: `apple/iosWidgets`
- App Intents / Shortcuts: `apple/iosApp/BiziShortcuts.swift`

Current iOS surface coverage:

- Home Screen widgets for favorite, nearby, and home/work quick access
- Configurable quick-station widget on iOS 17 for favorita, casa, or trabajo
- Nearby widget supports medium and large layouts
- Lock Screen widgets
- Live Activity for active monitoring with alternative details, tap-through to the suggested station, and Dynamic Island support
- App Intents and Siri Shortcuts with station and city parameters

## Apple Watch

- Watch app: `apple/watchApp`
- Shared watch snapshot model: `apple/watchShared/WatchSurfaceSnapshot.swift`
- WidgetKit complication: `apple/watchWidgets/BiciRadarWatchWidgets.swift`
- Watch parity notes: `docs/WATCH_PARITY_CHECKLIST.md`

Current Apple Watch coverage:

- nearby and favorites lists
- station detail with routing
- synced monitoring handoff from iPhone via `WatchConnectivity`
- complication for favorite station bikes, docks, and stale fallback
- monitoring card with alternative distance and handoff to the alternative station
- offline fallback from the shared App Group snapshot

## Project generation

Apple widget, watch, and complication targets are defined in `apple/project.yml`.
If the checked-in Xcode project is out of sync, regenerate `apple/BiciRadar.xcodeproj` from `apple/project.yml` before shipping from macOS.
