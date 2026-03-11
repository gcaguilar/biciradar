# Bizi Zaragoza

Greenfield scaffold for `com.gcaguilar.bizizaragoza` with:

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

## Release

The build and release checklist is available in `RELEASE.md`.

## Shortcuts and Voice

### iPhone with Siri and Shortcuts

Shortcuts published in [apple/iosApp/BiziShortcuts.swift](/Users/guillermo.castella/bizi/apple/iosApp/BiziShortcuts.swift):

- `Muéstrame la estación más cercana en Bizi Zaragoza`
- `Muéstrame la estación más cercana con bicis en Bizi Zaragoza`
- `Muéstrame la estación más cercana con huecos en Bizi Zaragoza`
- `Abre mis favoritas en Bizi Zaragoza`
- `Enséñame el estado de una estación en Bizi Zaragoza`
- `Enséñame cuántas bicis tiene una estación en Bizi Zaragoza`
- `Enséñame cuántos huecos tiene una estación en Bizi Zaragoza`
- `Llévame a una estación con Bizi Zaragoza`

Supported queries:

- nearest station
- nearest station with available bikes
- nearest station with free docks
- saved favorites
- available bikes at a station by name or station number
- free docks at a station by name or station number
- full station status by name
- route to a station by name

Current behavior:

- `nearest station`, `with bikes`, `with free docks`, `favorites`, and `route` open the app in the corresponding state.
- `status`, `bikes at station`, and `free docks at station` respond directly in Siri/Shortcuts without opening the app.

### Apple Watch with Siri and Shortcuts

Shortcuts published in [apple/watchApp/BiziWatchShortcuts.swift](/Users/guillermo.castella/bizi/apple/watchApp/BiziWatchShortcuts.swift):

- `Muéstrame la estación más cercana en el reloj con Bizi Zaragoza`
- `Muéstrame la estación más cercana con bicis en el reloj con Bizi Zaragoza`
- `Muéstrame la estación más cercana con huecos en el reloj con Bizi Zaragoza`
- `Enséñame mis favoritas en el reloj con Bizi Zaragoza`
- `Enséñame cuántas bicis tiene una estación en el reloj con Bizi Zaragoza`
- `Enséñame cuántos huecos tiene una estación en el reloj con Bizi Zaragoza`
- `Abre una ruta en mi iPhone con Bizi Zaragoza`

Current behavior:

- the watch responds by voice and does not need to open the app for nearest-station, bikes, docks, or favorites queries.
- the route action asks the paired iPhone to open navigation to the requested station.

### Android with Google Assistant

Android exposes actions and shortcuts in [androidApp/src/androidMain/res/xml/shortcuts.xml](/Users/guillermo.castella/bizi/androidApp/src/androidMain/res/xml/shortcuts.xml) and parses launches in [androidApp/src/androidMain/kotlin/com/gcaguilar/bizizaragoza/AndroidLaunchRequestParser.kt](/Users/guillermo.castella/bizi/androidApp/src/androidMain/kotlin/com/gcaguilar/bizizaragoza/AndroidLaunchRequestParser.kt).

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

Example target phrases for Assistant:

- `abre Bizi Zaragoza y muéstrame la estación más cercana`
- `abre Bizi Zaragoza y muéstrame la estación más cercana con bicis`
- `abre Bizi Zaragoza y muéstrame la estación más cercana con huecos`
- `abre Bizi Zaragoza y abre mis favoritas`
- `abre Bizi Zaragoza y enséñame el estado de una estación`
- `abre Bizi Zaragoza y enséñame cuántas bicis tiene una estación`
- `abre Bizi Zaragoza y enséñame cuántos huecos tiene una estación`
- `abre Bizi Zaragoza y llévame a una estación`

Important note:

- on Android, the most reliable way to test this in the emulator is to launch the intent directly.
- natural phrase capture for station names still depends on Google Assistant matching on the device.

`adb` test commands:

```bash
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'bizi://assistant?action=nearest_station' com.gcaguilar.bizizaragoza
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'bizi://assistant?action=nearest_station_with_bikes' com.gcaguilar.bizizaragoza
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'bizi://assistant?action=nearest_station_with_slots' com.gcaguilar.bizizaragoza
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'bizi://assistant?action=favorite_stations' com.gcaguilar.bizizaragoza
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'bizi://assistant?action=station_status&station_query=Plaza%20Espa%C3%B1a' com.gcaguilar.bizizaragoza
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'bizi://assistant?action=station_bike_count&station_query=Plaza%20Espa%C3%B1a' com.gcaguilar.bizizaragoza
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'bizi://assistant?action=station_slot_count&station_query=48' com.gcaguilar.bizizaragoza
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'bizi://assistant?action=route_to_station&station_query=Plaza%20Espa%C3%B1a' com.gcaguilar.bizizaragoza
adb -s emulator-5554 shell am start -a android.intent.action.VIEW -d 'bizi://assistant?station_query=Plaza%20Espa%C3%B1a' com.gcaguilar.bizizaragoza
```

## Apple

The generated Xcode project lives at `apple/BiziZaragoza.xcodeproj`. The SwiftUI/App Intents base is in `apple/`, and the KMP frameworks consumed there are:

- `BiziSharedCore`
- `BiziMobileUi`
- `BiziMobileUi` exports `MainViewController()` so it can be integrated from SwiftUI/UIKit
