# Bizi Zaragoza

Scaffold greenfield para `com.gcaguilar.bizizaragoza` con:

- Kotlin Multiplatform para dominio, datos y contratos shared.
- Compose Multiplatform para móvil Android + iOS.
- Compose for Wear OS para Wear.
- SwiftUI + App Intents shell para iPhone/Apple Watch.
- Metro DI como contenedor compile-time.
- Ubicación real en Android, iOS y watchOS con fallback a Zaragoza centro si no hay permiso o no hay fix.
- Sync de favoritos entre Android móvil y Wear OS con Data Layer.
- Mapa embebido en Compose móvil con wrapper multiplataforma.
- Feed oficial del Ayuntamiento de Zaragoza con fallback a CityBikes.

## Módulos

- `shared/core`: modelos, repositorios, contratos de plataforma, Metro graph y cliente Bizi.
- `shared/mobile-ui`: UI Compose compartida para móvil.
- `androidApp`: app Android principal.
- `wearApp`: app Wear OS.
- `apple/`: fuentes SwiftUI/App Intents base para iOS/watchOS.

## Comandos

```bash
./gradlew :shared:core:jvmTest
gradle :shared:mobile-ui:compileKotlinIosSimulatorArm64 :androidApp:compileDebugKotlinAndroid :wearApp:compileDebugKotlinAndroid
./gradlew build
```

## Configuración

- `GOOGLE_MAPS_API_KEY` es opcional para habilitar tiles reales en Android.

## Release

El checklist de build y publicación está en `RELEASE.md`.

## Atajos y voz

### iPhone con Siri y Atajos

Atajos publicados en [apple/iosApp/BiziShortcuts.swift](/Users/guillermo.castella/bizi/apple/iosApp/BiziShortcuts.swift):

- `Muéstrame la estación más cercana en Bizi Zaragoza`
- `Muéstrame la estación más cercana con bicis en Bizi Zaragoza`
- `Muéstrame la estación más cercana con huecos en Bizi Zaragoza`
- `Abre mis favoritas en Bizi Zaragoza`
- `Enséñame el estado de una estación en Bizi Zaragoza`
- `Enséñame cuántas bicis tiene una estación en Bizi Zaragoza`
- `Enséñame cuántos huecos tiene una estación en Bizi Zaragoza`
- `Llévame a una estación con Bizi Zaragoza`

Consultas soportadas:

- estación más cercana
- estación más cercana con bicis disponibles
- estación más cercana con huecos libres
- favoritas guardadas
- bicis disponibles en una estación por nombre o número
- huecos libres en una estación por nombre o número
- estado completo de una estación por nombre
- ruta a una estación por nombre

Comportamiento actual:

- `estación más cercana`, `con bicis`, `con huecos`, `favoritas` y `ruta` abren la app en el estado correspondiente.
- `estado`, `bicis en estación` y `huecos en estación` responden directamente en Siri/Atajos sin abrir la app.

### Apple Watch con Siri y Atajos

Atajos publicados en [apple/watchApp/BiziWatchShortcuts.swift](/Users/guillermo.castella/bizi/apple/watchApp/BiziWatchShortcuts.swift):

- `Muéstrame la estación más cercana en el reloj con Bizi Zaragoza`
- `Muéstrame la estación más cercana con bicis en el reloj con Bizi Zaragoza`
- `Muéstrame la estación más cercana con huecos en el reloj con Bizi Zaragoza`
- `Enséñame mis favoritas en el reloj con Bizi Zaragoza`
- `Enséñame cuántas bicis tiene una estación en el reloj con Bizi Zaragoza`
- `Enséñame cuántos huecos tiene una estación en el reloj con Bizi Zaragoza`
- `Abre una ruta en mi iPhone con Bizi Zaragoza`

Comportamiento actual:

- el reloj responde por voz y no necesita abrir la app para las consultas de cercanía, bicis, huecos o favoritas.
- la acción de ruta pide al iPhone emparejado que abra la navegación hacia la estación pedida.

### Android con Google Assistant

Android expone acciones y shortcuts en [androidApp/src/androidMain/res/xml/shortcuts.xml](/Users/guillermo.castella/bizi/androidApp/src/androidMain/res/xml/shortcuts.xml) y parsea los lanzamientos en [androidApp/src/androidMain/kotlin/com/gcaguilar/bizizaragoza/AndroidLaunchRequestParser.kt](/Users/guillermo.castella/bizi/androidApp/src/androidMain/kotlin/com/gcaguilar/bizizaragoza/AndroidLaunchRequestParser.kt).

Acciones soportadas:

- `nearest_station`
- `nearest_station_with_bikes`
- `nearest_station_with_slots`
- `favorite_stations`
- `station_status`
- `station_bike_count`
- `station_slot_count`
- `route_to_station`
- `show_station`

Ejemplos de frases objetivo para Assistant:

- `abre Bizi Zaragoza y muéstrame la estación más cercana`
- `abre Bizi Zaragoza y muéstrame la estación más cercana con bicis`
- `abre Bizi Zaragoza y muéstrame la estación más cercana con huecos`
- `abre Bizi Zaragoza y abre mis favoritas`
- `abre Bizi Zaragoza y enséñame el estado de una estación`
- `abre Bizi Zaragoza y enséñame cuántas bicis tiene una estación`
- `abre Bizi Zaragoza y enséñame cuántos huecos tiene una estación`
- `abre Bizi Zaragoza y llévame a una estación`

Matiz importante:

- en Android, la forma más fiable de probarlo en emulador es lanzar el intent directamente.
- la captura natural de frases con nombre de estación depende del matching real de Google Assistant en el dispositivo.

Comandos de prueba con `adb`:

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

El proyecto Xcode generado está en `apple/BiziZaragoza.xcodeproj`. La base SwiftUI/App Intents está en `apple/` y los frameworks KMP consumidos son:

- `BiziSharedCore`
- `BiziMobileUi`
- `BiziMobileUi` exporta `MainViewController()` para integrarlo desde SwiftUI/UIKit
