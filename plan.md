# BiciRadar Plan

## Summary
- Greenfield project with base `package` `com.gcaguilar.biciradar`.
- `stitch` is used only as the initial visual inspiration.
- v1 scope: nearby stations, availability, station detail, favorites, map, routes delegated to native maps, Android shortcuts/App Actions, and iOS/watchOS App Intents/App Shortcuts.
- Targets: Android mobile with Compose Multiplatform, iOS mobile with Compose Multiplatform, Wear OS with Compose for Wear OS, and Apple Watch with SwiftUI plus shared KMP logic.
- Baseline: Android 26+, Wear OS 3+, iOS 16+, watchOS 9+.
- Out of current scope: system voice search.

## Implementation Changes
- Create KMP modules for domain, networking, local storage, favorites, assistant/intents, mobile-watch sync, and platform utilities.
- Consume public BiciRadar data directly from shared code with Ktor and normalize it into internal models for stations, availability, favorites, and nearby queries. The primary source is the official Zaragoza City Council feed, with CityBikes as fallback.
- Persist favorites locally and sync them between mobile and watch through pairing.
- Resolve routes with deep links/intents to Google Maps and Apple Maps.
- Implement Metro DI as the main container with shared bindings and native adapters.
- Build a native UI per platform inspired by `stitch`, without reproducing layouts literally.

## Public APIs and Interfaces
- `StationsRepository`
- `FavoritesRepository`
- `RouteLauncher`
- `AssistantIntentResolver`
- `WatchSyncBridge`

## Cross-Platform Intents and Actions
- `nearest_station`
- `station_status`
- `favorite_stations`
- `route_to_station`

## Test Plan
- KMP unit tests for feed parsing, nearby calculation, cache, favorites, and intent resolution.
- Integration tests for the Ktor client and public feed consumption.
- Platform tests for App Actions/App Shortcuts on Android.
- Platform tests for App Intents/App Shortcuts on iOS/watchOS.
- Favorite sync tests between mobile and watch.
- Native maps route launch tests.
- Visual QA based on design-system consistency, not pixel parity with `stitch`.

## Assumptions and Defaults
- `stitch` only inspires the design; it does not define functionality.
- A public feed/API with enough station and availability data is assumed to be available.
- Initial language: Spanish, while keeping the base ready for English later.
- Versions are pinned in `libs.versions.toml` using the latest stable versions compatible with the KMP/Compose/Gradle stack.
