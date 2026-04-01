# BiciRadar User Guide

This guide is intended for end users of the app (not for development).

## What you can do with BiciRadar

- See the nearest station.
- Find stations with available bikes.
- Find stations with free docks.
- Open favorites.
- Check station status by name or number.
- Get directions to a station.

## iPhone: Siri and Shortcuts

Shortcuts are defined in `apple/iosApp/BiziShortcuts.swift`.

Example phrases:

- "Cual es la estacion mas cercana con Bici Radar"
- "Donde hay bicis cerca con Bici Radar"
- "Donde puedo dejar la bici con Bici Radar"
- "Abre mis favoritas con Bici Radar"
- "Como esta una estacion con Bici Radar"
- "Cuantas bicis hay en una estacion con Bici Radar"
- "Cuantos huecos hay en una estacion con Bici Radar"
- "Llevame a una estacion con Bici Radar"
- "Como esta casa con Bici Radar"
- "Como esta trabajo con Bici Radar"
- "Llevame a casa con Bici Radar"
- "Llevame a trabajo con Bici Radar"

Current behavior:

- Nearest station, bike availability, free docks, favorites, and routing requests open the app in the corresponding state.
- Status, bike count, and dock count requests can respond directly in Siri/Shortcuts without opening the app.

## Apple Watch: Siri and Shortcuts

Shortcuts are defined in `apple/watchApp/BiziWatchShortcuts.swift`.

Example phrases:

- "Cual es la estacion mas cercana con Bici Radar"
- "Donde hay bicis cerca con Bici Radar"
- "Donde puedo dejar la bici con Bici Radar"
- "Abre mis favoritas con Bici Radar"
- "Cuantas bicis hay en una estacion con Bici Radar"
- "Cuantos huecos hay en una estacion con Bici Radar"
- "Llevame a una estacion con Bici Radar"
- "Como esta casa con Bici Radar"
- "Como esta trabajo con Bici Radar"
- "Llevame a casa con Bici Radar"
- "Llevame a trabajo con Bici Radar"

Current behavior:

- The watch replies by voice for nearest station, bikes, docks, and favorites queries.
- The route action asks the paired iPhone to open navigation.

## Android: Google Assistant

Actions and shortcuts are implemented in:

- `androidApp/src/androidMain/res/xml/shortcuts.xml`
- `androidApp/src/androidMain/kotlin/com/gcaguilar/biciradar/AndroidLaunchRequestParser.kt`

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

Supported saved place aliases:

- `casa`, `mi casa`, `home`
- `trabajo`, `mi trabajo`, `work`
- `oficina`, `mi oficina`

Example phrases:

- "cual es la estacion mas cercana con Bici Radar"
- "donde hay bicis cerca con Bici Radar"
- "donde puedo dejar la bici con Bici Radar"
- "abre mis favoritas con Bici Radar"
- "como esta una estacion con Bici Radar"
- "cuantas bicis hay en una estacion con Bici Radar"
- "cuantos huecos hay en una estacion con Bici Radar"
- "llevame a una estacion con Bici Radar"
- "como esta casa con Bici Radar"
- "como esta trabajo con Bici Radar"
- "llevame a casa con Bici Radar"
- "llevame al trabajo con Bici Radar"

Notes:

- On Android emulator, the most reliable validation method is launching intents directly.
- Natural station-name recognition still depends on Google Assistant matching on the device.
