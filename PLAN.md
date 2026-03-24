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

---

# Plan de Migración: moko-resources → compose.components.resources

## BLOQUEADO

**Razón:** Compose Multiplatform Resources no soporta watchOS en la versión actual (1.10.3).

**Error encontrado:**
```
Couldn't resolve dependency 'org.jetbrains.compose.components:components-resources'
Unresolved platforms: [watchosArm64, watchosSimulatorArm64]
```

**Posibles soluciones futuras:**
1. Esperar a una versión de Compose Multiplatform que soporte watchOS
2. Excluir watchOS del proyecto (no deseado)
3. Mantener moko-resources indefinidamente

**Decisión:** Mantener moko-resources por ahora. Revisitar cuando Compose Multiplatform 1.11+ esté disponible con soporte watchOS completo.

## Estado Actual

- **moko-resources versión:** 0.26.1
- **Ubicación recursos:** `shared/core/src/commonMain/moko-resources/`
- **Idiomas:** base (castellano), ca (catalán), eu (euskera), gl (gallego), en (inglés)
- **Referencias en código:** 255+ usos de `MR.strings.*`

## Release

Release dedicada para esta migración (no mezclar con otras features).

---

## Fases de la Migración

### Fase 1: Preparación (Pre-migración)

#### 1.1 Agregar dependencia compose.components.resources

**Archivo:** `shared/core/build.gradle.kts`

```kotlin
commonMain.dependencies {
  // Agregar esta línea
  implementation(libs.compose.components.resources)
}
```

**Archivo:** `gradle/libs.versions.toml` (agregar si no existe)

```toml
[libraries]
compose-components-resources = { module = "org.jetbrains.compose:components:components-resources", version.ref = "composeMultiplatform" }
```

---

### Fase 2: Mover Recursos

#### 2.1 Estructura de directorios

Crear: `shared/core/src/commonMain/composeResources/`

```
composeResources/
└── values/
    └── strings.xml        (contenido de moko-resources/base/strings.xml)
└── values-ca/
    └── strings.xml        (contenido de moko-resources/ca/strings.xml)
└── values-eu/
    └── strings.xml        (contenido de moko-resources/eu/strings.xml)
└── values-gl/
    └── strings.xml        (contenido de moko-resources/gl/strings.xml)
└── values-en/
    └── strings.xml        (contenido de moko-resources/en/strings.xml)
```

#### 2.2 Acción

- Copiar archivos de `moko-resources/` a `composeResources/`
- **NO eliminar** moko-resources todavía (hasta Fase 5)

---

### Fase 3: Actualizar Código

#### 3.1 Actualizar referencias de strings

Buscar y reemplazar todas las ocurrencias (255+):

```kotlin
// ANTES
stringResource(MR.strings.nearby)

// DESPUÉS
stringResource(SharedRes.strings.nearby)
```

**Scripts de búsqueda:**
```bash
# Encontrar todos los usages de MR.strings
grep -r "MR\.strings\." --include="*.kt" shared/mobile-ui/
```

---

### Fase 4: Validación

#### 4.1 Compilar todos los targets

```bash
# Android
./gradlew :shared:core:compileDebugKotlinAndroid

# iOS
./gradlew :shared:core:compileKotlinIosArm64

# JVM
./gradlew :shared:core:compileKotlinJvm
```

#### 4.2 Test manual

- Verificar que las strings aparecen correctamente en Android
- Verificar que las strings aparecen correctamente en iOS
- Verificar cambio de idioma funciona

---

### Fase 5: Limpieza

#### 5.1 Eliminar plugin moko-resources

**Archivo:** `shared/core/build.gradle.kts`

```kotlin
// ELIMINAR esta línea del bloque plugins
alias(libs.plugins.moko.resources)

// ELIMINAR el bloque completo
multiplatformResources {
  resourcesPackage.set("com.gcaguilar.biciradar.core")
}
```

#### 5.2 Eliminar dependencias moko

**Archivo:** `gradle/libs.versions.toml`

```toml
# ELIMINAR esta línea
mokoResources = "0.26.1"
```

#### 5.3 Eliminar directorio moko-resources

```bash
rm -rf shared/core/src/commonMain/moko-resources/
```

---

## Tiempo Estimado

- **Preparación:** 15 min
- **Mover recursos:** 10 min
- **Actualizar código:** 1-2 horas
- **Validación:** 30 min
- **Limpieza:** 15 min

**Total:** ~2-3 horas

---

## Checklist de Ejecución

- [ ] Fase 1: Agregar dependencia compose.components.resources
- [ ] Fase 2: Mover archivos de moko-resources a composeResources
- [ ] Fase 3: Actualizar referencias (255+ cambios)
- [ ] Fase 4: Compilar Android, iOS, JVM
- [ ] Fase 5: Eliminar plugin moko-resources
- [ ] Fase 5: Eliminar directorio moko-resources/
