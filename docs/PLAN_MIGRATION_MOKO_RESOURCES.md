# Plan de Migración a moko-resources

## Situación Actual

El proyecto utiliza un sistema de localización híbrido:

- **Strings en 2 lugares**: `Localization.kt` (código Kotlin) + `composeResources/values*/strings.xml`
- **224 usages** de `sharedString(SharedString.XYZ)` en el código
- **200+ strings** en enum `SharedString`
- **5 idiomas**: ES (default), EN, CA, EU, GL
- **Interpolación**: `%s` (sin soporte plurals nativo)
- **iOS**: Lenguaje hardcodeado a español
- **Siri/Shortcuts**: Strings en `.lproj/Localizable.strings` separados

## Objetivo

Sistema unificado con **moko-resources** para todos los recursos:

- Un solo lugar para definir strings (XML)
- Soporte plurals nativo
- Detección automática de idioma en iOS
- Tipo-seguro con clase `MR` generada

## Estructura Actual vs Estructura Target

### Actual

```
shared/core/src/commonMain/
├── kotlin/com/.../core/
│   ├── Localization.kt           # ~200 strings + enum SharedString
│   ├── Localization.android.kt  # Detección idioma
│   ├── Localization.apple.kt    # Hardcodeado ES
│   └── Localization.jvm.kt     # Detección idioma
└── composeResources/
    └── values*/strings.xml     # Duplicado de strings (para Compose)
```

### Target

```
shared/core/src/commonMain/
├── resMR/                      # moko-resources
│   ├── base/strings/
│   │   ├── strings.xml         # ES (default)
│   │   └── plurals.xml        # ES plurals
│   ├── en/strings/
│   │   ├── strings.xml
│   │   └── plurals.xml
│   ├── ca/strings/
│   │   ├── strings.xml
│   │   └── plurals.xml
│   ├── eu/strings/
│   │   ├── strings.xml
│   │   └── plurals.xml
│   └── gl/strings/
│       ├── strings.xml
│       └── plurals.xml
└── kotlin/com/.../core/
    └── (Localization.kt eliminado)
```

## Fases de Implementación

### Fase 1: Configuración

**Archivos a modificar:**

1. `gradle/libs.versions.toml`
   ```toml
   [versions]
   mokoResources = "0.26.1"

   [plugins]
   moko-resources = { id = "dev.icerock.moko.resources", version.ref = "mokoResources" }

   [libraries]
   moko-resources-compose = { module = "dev.icerock.moko:resources-compose", version.ref = "mokoResources" }
   ```

2. `shared/core/build.gradle.kts` - Añadir plugin:
   ```kotlin
   plugins {
       id("dev.icerock.moko.resources")
   }

   mokoResources {
       sourceSet.set("commonMain")
   }
   ```

3. `shared/mobile-ui/build.gradle.kts` - Añadir dependencia:
   ```kotlin
   dependencies {
       implementation(moko.resources.compose)
   }
   ```

**Directorios a crear:**
- `shared/core/src/commonMain/resMR/base/strings/`
- `shared/core/src/commonMain/resMR/base/plurals/`
- `shared/core/src/commonMain/resMR/en/strings/`
- `shared/core/src/commonMain/resMR/en/plurals/`
- `shared/core/src/commonMain/resMR/ca/strings/`
- `shared/core/src/commonMain/resMR/ca/plurals/`
- `shared/core/src/commonMain/resMR/eu/strings/`
- `shared/core/src/commonMain/resMR/eu/plurals/`
- `shared/core/src/commonMain/resMR/gl/strings/`
- `shared/core/src/commonMain/resMR/gl/plurals/`

**Tiempo estimado:** 30 minutos

---

### Fase 2: Migración de Strings

**Proceso:**

1. Exportar todas las strings de `Localization.kt`:
   - `localizedTextByKey` (strings de mapa)
   - `SharedString` enum (strings de shortcuts)

2. Crear archivos `strings.xml` por idioma con formato Android:
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <resources>
       <string name="appName">Bici Radar</string>
       <string name="mapNoStations">No hay estaciones para esa búsqueda</string>
       <string name="nearestStation">La estación más cercana es %1$s con %2$s bicis y %3$s anclajes.</string>
       <!-- ... -->
   </resources>
   ```

3. Crear archivos `plurals.xml` donde aplique:
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <resources>
       <plurals name="bikesCount">
           <item quantity="one">%d bici</item>
           <item quantity="other">%d bicis</item>
       </plurals>
   </resources>
   ```

**Strings de Siri/Shortcuts identificadas:**
- `nearestStation` - "La estación más cercana es %s con %s bicis y %s anclajes."
- `nearestStationFallback` - "No he encontrado ninguna estación dentro de %s m..."
- `noNearbyBikes` - "No he encontrado estaciones cercanas con bicis..."
- `nearestWithBikes` - "La estación más cercana con bicis disponibles es..."
- `stationBikes` - "%s tiene %s bicis disponibles."
- `stationStatus` - "%s tiene %s bicis disponibles y %s huecos libres."
- Y ~30+ más (ver `Localization.kt` líneas 235-443)

**Tiempo estimado:** 2-3 horas

---

### Fase 3: Actualización de Código

**Reemplazar usages (~224 ocurrencias):**

```kotlin
// ANTES
text = sharedString(SharedString.MAP_NO_STATIONS)
snippet = sharedString(SharedString.MAP_STATION_BIKES_FREE, bikes, slots)
dialog = await runner.stationStatusDialog(stationId: station.id)

// DESPUÉS
text = stringResource(MR.strings.mapNoStations)
snippet = stringResource(MR.strings.mapStationBikesFree, bikes, slots)
dialog = await runner.stationStatusDialog(stationId: station.id, MR.strings)
```

**Archivos a modificar:**
- `shared/mobile-ui/src/commonMain/kotlin/.../BiziMobileApp.kt` (~100 usages)
- `shared/mobile-ui/src/androidMain/kotlin/.../PlatformStationMap.android.kt` (~10 usages)
- `apple/iosApp/AppleShortcutRunner.swift` (~20 usages)
- `apple/watchApp/WatchShortcutRunner.swift` (~10 usages)
- Y más según grep `sharedString\(SharedString\.`

**Tiempo estimado:** 2-3 horas

---

### Fase 4: Configuración iOS

**1. Actualizar `Localization.apple.kt`:**

```kotlin
actual fun currentAppLanguage(): AppLanguage {
    val code = NSLocale.current.languageCode?.identifier?.uppercase() ?: "ES"
    return AppLanguage.entries.find { it.name == code } ?: AppLanguage.ES
}
```

**2. Añadir MR.bundle al proyecto Xcode:**

moko-resources genera un `MR.bundle` que debe añadirse a:
- `BiciRadar.xcodeproj` (app principal)
- `BiciRadarWatch.xcodeproj` (watch app)

**3. Acceso desde Swift:**

```swift
import BiciMobileUi  // Módulo KMP con MR

// En lugar de hardcoded strings
let dialog = await runner.stationStatusDialog(
    stationId: station.id,
    strings: MR.strings // Pasado desde KMP
)
```

**Tiempo estimado:** 1 hora

---

### Fase 5: Limpieza

**Archivos a eliminar:**

- `shared/core/src/commonMain/kotlin/com/gcaguilar/biciradar/core/Localization.kt`
- `shared/core/src/commonMain/kotlin/com/gcaguilar/biciradar/core/Localization.android.kt`
- `shared/core/src/commonMain/kotlin/com/gcaguilar/biciradar/core/Localization.apple.kt`
- `shared/core/src/commonMain/kotlin/com/gcaguilar/biciradar/core/Localization.jvm.kt`
- `shared/core/src/commonMain/composeResources/values/strings.xml`
- `shared/core/src/commonMain/composeResources/values-en/strings.xml`
- `shared/core/src/commonMain/composeResources/values-ca/strings.xml`
- `shared/core/src/commonMain/composeResources/values-eu/strings.xml`
- `shared/core/src/commonMain/composeResources/values-gl/strings.xml`
- `apple/iosApp/*.lproj/Localizable.strings` (migrados a moko)

**Limpiar imports en:**
- Todos los archivos que importaban `Localization` o `SharedString`

**Tiempo estimado:** 30 minutos

---

### Fase 6: Testing

**Verificaciones:**

1. **Compilación:**
   ```bash
   ./gradlew :shared:core:generateMR
   ./gradlew :androidApp:assembleDebug
   ./gradlew :apple:iosApp:build  # o según nombre del target
   ```

2. **Test de idiomas (manual):**
   - iOS Simulator: Cambiar idioma del sistema → Español, Inglés, Catalán, Euskera, Gallego
   - Android Emulator: Cambiar idioma del sistema → Español, Inglés, Catalán, Euskera, Gallego

3. **Test de shortcuts:**
   - Invocar Siri: "Dónde hay bicis cerca con Bici Radar"
   - Verificar respuesta en el idioma correcto

4. **Test de watchOS:**
   - Verificar que el reloj muestra strings correctas

**Tiempo estimado:** 1-2 horas

---

## Resumen de Cambios por Archivo

| Archivo/Directorio | Acción |
|---------------------|--------|
| `gradle/libs.versions.toml` | Modificar - Añadir moko |
| `shared/core/build.gradle.kts` | Modificar - Añadir plugin |
| `shared/mobile-ui/build.gradle.kts` | Modificar - Añadir dependencia |
| `shared/core/src/commonMain/resMR/**` | **CREAR** - Estructura strings |
| `shared/core/src/commonMain/kotlin/.../Localization.kt` | **ELIMINAR** |
| `shared/core/src/commonMain/kotlin/.../Localization.*.kt` | **ELIMINAR** (3 archivos) |
| `shared/core/src/commonMain/composeResources/**` | **ELIMINAR** (5 directorios) |
| `shared/mobile-ui/src/**/*.kt` | **MODIFICAR** - Actualizar usages |
| `apple/iosApp/**/*.lproj/*.strings` | **ELIMINAR** - Migrados |
| `apple/BiciRadar.xcodeproj/` | **MODIFICAR** - Añadir MR.bundle |

---

## Tiempo Total Estimado

| Fase | Tiempo |
|------|--------|
| 1. Configuración | 30 min |
| 2. Migración strings | 2-3 horas |
| 3. Actualización código | 2-3 horas |
| 4. iOS/wearOS | 1 hora |
| 5. Limpieza | 30 min |
| 6. Testing | 1-2 horas |
| **Total** | **~6-8 horas** |

---

## Riesgos y Mitigaciones

| Riesgo | Probabilidad | Mitigación |
|--------|--------------|------------|
| iOS no detecta idioma | Media | Verificar NSLocale API en simulator |
| watchOS no tiene acceso a MR | Baja | moko-resources soporta watchOS |
| Breakage en shortcuts Siri | Baja | Testear tras migración |
| Conflictos con plugin gradle | Baja | Verificar versiones compatibles |

---

## Recursos

- [moko-resources GitHub](https://github.com/icerockdev/moko-resources)
- [moko-resources Documentation](https://github.com/icerockdev/moko-resources?tab=readme-ov-file#readme)
- [Compose Multiplatform Resources](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-resources.html)
