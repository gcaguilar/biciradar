# Bizi Zaragoza Plan

## Resumen
- Proyecto greenfield con `package` base `com.gcaguilar.bizizaragoza`.
- `stitch` se usa solo como inspiración visual inicial.
- Alcance v1: estaciones cercanas, disponibilidad, detalle de estación, favoritos, mapa, rutas delegadas a mapas nativos, shortcuts/App Actions en Android y App Intents/App Shortcuts en iOS/watchOS.
- Targets: Android móvil con Compose Multiplatform, iOS móvil con Compose Multiplatform, Wear OS con Compose for Wear OS y Apple Watch con SwiftUI + lógica compartida KMP.
- Baseline: Android 26+, Wear OS 3+, iOS 16+, watchOS 9+.
- Fuera del alcance actual: búsqueda por voz del sistema.

## Cambios de implementación
- Crear módulos KMP para dominio, red, almacenamiento local, favoritos, assistant/intents, sincronización móvil-reloj y utilidades de plataforma.
- Consumir datos públicos de Bizi Zaragoza directamente desde shared con Ktor y normalizarlos a modelos internos de estaciones, disponibilidad, favoritos y consultas cercanas. El origen primario es el feed oficial del Ayuntamiento de Zaragoza con fallback a CityBikes.
- Persistir favoritos localmente y sincronizarlos entre móvil y reloj por emparejado.
- Resolver rutas con deep links/intents a Google Maps y Apple Maps.
- Implementar Metro DI como contenedor principal con bindings compartidos y adaptadores nativos.
- Construir una UI nativa por plataforma inspirada en `stitch`, sin replicar layouts de forma literal.

## APIs e interfaces públicas
- `StationsRepository`
- `FavoritesRepository`
- `RouteLauncher`
- `AssistantIntentResolver`
- `WatchSyncBridge`

## Intents y acciones cross-platform
- `nearest_station`
- `station_status`
- `favorite_stations`
- `route_to_station`

## Plan de pruebas
- Unit tests KMP para parsing del feed, cálculo de cercanía, caché, favoritos y resolución de intents.
- Integration tests para cliente Ktor y consumo del feed público.
- Tests de plataforma para App Actions/App Shortcuts en Android.
- Tests de plataforma para App Intents/App Shortcuts en iOS/watchOS.
- Tests de sync de favoritos entre móvil y reloj.
- Tests de lanzamiento de rutas a mapas nativos.
- QA visual basada en consistencia del sistema visual, no en pixel parity con `stitch`.

## Suposiciones y defaults
- `stitch` solo inspira diseño; no define funcionalidad.
- Se asume disponibilidad de un feed/API pública suficiente para estaciones y disponibilidad.
- Idioma inicial: español, dejando la base preparada para inglés.
- Versiones fijadas en `libs.versions.toml` con últimas estables compatibles del stack KMP/Compose/Gradle.
