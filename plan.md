# Plan: integrar Android AppFunctions en BiciRadar

Objetivo: exponer a agentes del sistema (Gemini vía Android MCP, AppFunction
Testing Agent, asistentes on-device) las capacidades clave de BiciRadar sin
abrir la UI: consultar disponibilidad de estaciones, encontrar la estación más
cercana con bicis/huecos y gestionar favoritas.

Referencias:
- https://developer.android.com/ai/appfunctions
- https://johnoreilly.dev/posts/confetti-appfunctions/ (Confetti, KMP + Koin, `1.0.0-beta01`)
- Skill `android-skills:appfunctions` (discovery → implementación → KDoc → adb)

---

## 0. Estado actual relevante

| Aspecto | Estado en el repo | Implicación |
|---|---|---|
| `compileSdk` / `targetSdk` | `release(37) { minorApiLevel = 1 }` / `37` (`androidApp/build.gradle.kts`) | Cumple el requisito (compileSdk ≥ 37, targetSdk ≥ 36). |
| `minSdk` | 29 | El servicio se anota `@RequiresApi(36)` + `tools:targetApi="36"`; en < 36 no se registra. |
| Kotlin / AGP | Kotlin `2.5.0-Beta2`, AGP `9.5.0`, `androidApp` usa Kotlin integrado en AGP (no aplica `kotlin-android`) | Hay que añadir **KSP** (no existe hoy en el proyecto). Riesgo de compatibilidad → spike (Fase 1). |
| DI | Metro (compiler plugin), grafo global `BiziAppGraph.graph: SharedGraph` | El servicio obtiene dependencias vía `BiziAppGraph.graph` (patrón "service locator" de la skill), sin Hilt. |
| Lógica reutilizable | Use cases en `shared/core` expuestos por `StationsQueryUseCases`, `FavoritesFeatureDeps`, `SessionFeatureDeps` | Las AppFunctions solo orquestan use cases existentes; no se crea capa nueva. |
| Integraciones análogas | Shortcuts/assistant Android (`AndroidAssistantShortcuts.kt`, `res/xml/shortcuts.xml`), App Intents iOS (`apple/iosApp/BiziShortcuts.swift`) | El catálogo de funciones se alinea con los intents ya existentes en iOS. |
| Deep links | `biciradar://station`, `nearby`, `favorites`, `map`, `monitor`, `city` | Se pueden devolver `PendingIntent` para "abrir en la app". |

Librería: `androidx.appfunctions:appfunctions{,-compiler}` **`1.0.0-beta01`**
(última en maven.google.com a 2026‑11‑02). En beta01 la anotación de función es
`@AppFunctionDeclaration` y el metadata usa `appfn:instructions` (en alphas
era `@AppFunction` / `appfn:description`); verificar en el spike.

---

## 1. Discovery: funciones propuestas

Criterio: "rich queries" (resolver entidades: estaciones) y mutaciones seguras.
Nada de credenciales Auth0, tokens push ni acciones destructivas masivas
(`clearAll`, borrar todas las alertas).

### MVP (Fase 2)

| Función | Tipo | Input | Output | Use case existente | Valor para el agente |
|---|---|---|---|---|---|
| `getCurrentCity` | query | — | `CityInfo` (id, nombre, soporta e-bikes) | `GetCurrentCity` | Contexto: todas las funciones operan sobre la ciudad seleccionada. |
| `searchStations` | rich query | `query: String`, `limit: Int = 5` | `List<StationInfo>` | `FilterStationsByQuery` / `FindStationMatchingQuery` (soporta alias "casa"/"trabajo") | Resuelve "la estación de Plaza España" → `stationId`. Prerrequisito del resto. |
| `getStationAvailability` | rich query | `stationId: String` | `StationInfo` (bicis, mecánicas, eléctricas, huecos, actualizado) | `GetStationStatus` / `FindStationById` + `RefreshStationAvailability` | "¿Cuántas bicis quedan en mi estación de casa?" |
| `findNearestStation` | rich query | `requirement: String = "ANY"` (`ANY`/`BIKES`/`EBIKES`/`SLOTS`), `latitude: Double?`, `longitude: Double?` | `StationInfo?` | `FindNearestStation`, `FindNearestStationWithBikes`, `FindNearestStationWithSlots` | Manos libres: "¿Dónde dejo la bici?" |
| `listNearbyStations` | rich query | `limit: Int = 5`, `latitude/longitude` opcionales | `List<StationInfo>` | `GetNearbyStationList` | Comparar opciones cercanas. |
| `listFavoriteStations` | rich query | — | `List<StationInfo>` (+ flags `isHome`/`isWork`) | `GetFavoriteStationList` + `FavoritesRepository.current{Home,Work}StationId` | "¿Cómo están mis favoritas?" |
| `addFavoriteStation` | mutación | `stationId` | `StationInfo` | `ToggleFavoriteStation` (solo si no es favorita) | Idempotente: no usar toggle "a ciegas". |
| `removeFavoriteStation` | mutación reversible | `stationId` | `Boolean` | `ToggleFavoriteStation` (solo si es favorita) | Reversible, una sola estación → sin confirmación extra. |
| `openStationInApp` | acción UI | `stationId` | `PendingIntent` (deep link `biciradar://station?...`) | `AndroidLaunchRequestParser` | El agente puede ofrecer "abrir en BiciRadar" / ruta. |

### Fase 3 (tras validar MVP)

| Función | Notas / riesgo |
|---|---|
| `setHomeStation` / `setWorkStation` | `FavoritesRepository.setHome/WorkStationId`. Sobrescribe un valor → documentar en KDoc que reemplaza el anterior. |
| `startStationMonitoring` / `stopStationMonitoring` | `StartStationMonitoring`/`TripRepository.startMonitoring` arrancan `TripMonitorService` (FGS `dataSync`). Desde un servicio enlazado en background Android puede bloquear el arranque de FGS → evaluar; si falla, devolver `PendingIntent` a `biciradar://monitor`. |
| `createSavedPlaceAlert` | `SavedPlaceAlertsRepository.upsertRule(target, condition)`. Requiere modelar `SavedPlaceAlertCondition` serializable. |
| `changeCity` | `ChangeCityUseCase`. Cambia todo el contexto y borra estado por ciudad (`removeRulesForCity`?) → confirmar efectos antes de exponer. |

Descartadas: login/logout Auth0, sincronización de suscripciones push,
`clearAll` de favoritos, ajustes internos, onboarding.

---

## 2. Implementación y configuración

### 2.1 Gradle (Fase 1 – spike)

`gradle/libs.versions.toml`:

```toml
[versions]
androidx-appfunctions = "1.0.0-beta01"
ksp = "<versión KSP2 compatible con Kotlin 2.5.0-Beta2 / AGP 9 built-in Kotlin>"

[libraries]
androidx-appfunctions = { module = "androidx.appfunctions:appfunctions", version.ref = "androidx-appfunctions" }
androidx-appfunctions-compiler = { module = "androidx.appfunctions:appfunctions-compiler", version.ref = "androidx-appfunctions" }

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

`androidApp/build.gradle.kts`:

```kotlin
plugins { alias(libs.plugins.ksp) }

dependencies {
  implementation(libs.androidx.appfunctions)
  ksp(libs.androidx.appfunctions.compiler)
}

ksp { arg("appfunctions:aggregateAppFunctions", "true") }
```

Spike — criterios de salida:
1. `./gradlew :androidApp:assemblePlaystoreDebug` compila con KSP + Metro + Compose.
2. El APK contiene `assets/biciradar_app_function_service.xml` y la clase
   generada `BiciRadarAppFunctionService`.
3. Si KSP no soporta aún Kotlin 2.5.0-Beta2 / built-in Kotlin: (a) probar la
   última KSP 2.3.x (KSP2 ya está desacoplado de la versión de Kotlin),
   (b) aislar AppFunctions en un módulo Android library `:androidApp:appfunctions`
   y aplicar KSP solo ahí. Documentar la decisión.
4. R8: comprobar que `release` (minify + shrinkResources) conserva el servicio
   generado y los `@AppFunctionSerializable`; añadir keep rules en
   `proguard-rules.pro` solo si hace falta (la librería debería traer consumer rules).

Recordatorio de alcance (memoria del proyecto): verificar con tareas concretas
de `playstore`, sin `PlayStoreRelease` signing, sin wearApp.

### 2.2 Código (Fase 2)

Ubicación: `androidApp/src/main/kotlin/com/gcaguilar/biciradar/appfunctions/`
(AppFunctions es integración de plataforma Android → no va en `shared/core`).

Ficheros:
- `BaseBiciRadarAppFunctionService.kt` – entry point.
- `AppFunctionModels.kt` – `StationInfo`, `CityInfo` (`@AppFunctionSerializable`).
- `AppFunctionMappers.kt` – `Station.toStationInfo(...)`, `City.toCityInfo()`.

Esqueleto:

```kotlin
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
  serviceName = "BiciRadarAppFunctionService",
  appFunctionXmlFileName = "biciradar_app_function_service",
)
abstract class BaseBiciRadarAppFunctionService : AppFunctionService() {
  // BiziAppGraph se inicializa en BiciRadarApplication.onCreate(), que corre
  // antes de enlazar cualquier Service.
  private val graph get() = BiziAppGraph.graph

  /**
   * Search bike-sharing stations in the selected city by name, street or alias.
   * Required workflow: call this before "getStationAvailability",
   * "addFavoriteStation" or "openStationInApp" to obtain a valid stationId.
   *
   * @param query Station name, address fragment, or the aliases "home"/"casa" and "work"/"trabajo".
   * @param limit Maximum number of results, 1–20. Defaults to 5.
   * @return Matching stations ordered by relevance, then distance.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun searchStations(query: String, limit: Int = 5): List<StationInfo> =
    withContext(Dispatchers.IO) { /* graph.filterStationsByQuery … */ }

  // getCurrentCity, getStationAvailability, findNearestStation,
  // listNearbyStations, listFavoriteStations, add/removeFavoriteStation,
  // openStationInApp
}
```

```kotlin
/** A public bike-sharing station with its current availability. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class StationInfo(
  /** Unique station ID; pass it to other BiciRadar functions. */
  val id: String,
  /** Station display name. */
  val name: String,
  /** Street address of the station. */
  val address: String,
  /** Total bikes available to rent right now. */
  val bikesAvailable: Int,
  /** Electric bikes available (0 if the city has no e-bikes). */
  val ebikesAvailable: Int,
  /** Mechanical bikes available. */
  val regularBikesAvailable: Int,
  /** Free docks to return a bike. */
  val freeDocks: Int,
  /** Distance from the user in meters, or -1 if location is unknown. */
  val distanceMeters: Int,
  /** Latitude in degrees. */
  val latitude: Double,
  /** Longitude in degrees. */
  val longitude: Double,
  /** Whether the user marked this station as favorite. */
  val isFavorite: Boolean,
  /** When the availability data was last refreshed. */
  val updatedAt: Instant?,
)
```

Reglas (skill):
- `suspend` + `withContext(Dispatchers.IO)` en todas.
- Parámetros opcionales con default vacío (`0`, `null`, `emptyList()`).
- Enums como `String` acotado y documentado (`requirement`), validando y
  lanzando `AppFunctionInvalidArgumentException` si no es válido.
- Errores: `AppFunctionElementNotFoundException` (stationId inexistente),
  `AppFunctionInvalidArgumentException` (query vacía, limit fuera de rango),
  `AppFunctionAppUnknownException`/`…UnavailableException` (sin red y sin caché).
- KDoc de serializables **inline por propiedad** (KSP ignora `@property`).
- Fechas como `Instant` (convertir `lastUpdatedEpoch`).

### 2.3 Puntos delicados de comportamiento

1. **Ubicación en background.** `StationsRepository.loadIfNeeded()` llama a
   `fetchCurrentLocation()` (`LocationManager.getCurrentLocation`). Cuando el
   sistema enlaza el servicio, la app no está en primer plano y la app no
   declara `ACCESS_BACKGROUND_LOCATION` → la ubicación puede ser `null` o
   imprecisa. Mitigación:
   - Aceptar `latitude`/`longitude` opcionales (el agente suele conocer la
     ubicación del usuario) y recalcular distancias con ellos.
   - Si no hay ubicación: usar la última conocida / fallback de ciudad y
     devolver `distanceMeters = -1`; documentarlo en KDoc.
   - **No** añadir `ACCESS_BACKGROUND_LOCATION` (impacto en política de Play).
   - Puede requerir un use case nuevo pequeño en `shared/core`
     (p. ej. `FindNearestStationFrom(origin: GeoPoint, requirement)`) reutilizando
     `selectNearbyStationWithBikes` etc.; añadir tests en `commonTest`.
2. **Frescura.** `loadIfNeeded()` es no-op si ya está cargado; para
   `getStationAvailability` llamar a `RefreshStationAvailability` del id
   pedido (los datos de bicis caducan en minutos).
3. **Bootstrap.** Comprobar que `SettingsRepository`/`FavoritesRepository`
   están bootstrapped en un proceso arrancado en frío por el servicio
   (sin `MainActivity`). Si no, llamar a `graph.bootstrapSession` en un helper
   `ensureReady()` con `Mutex`.
4. **Ciudad no configurada.** Si `IsCityConfiguredUseCase` es falso, lanzar
   error con mensaje accionable: "Open BiciRadar and select a city first".
5. **Widgets.** Tras `add/removeFavoriteStation`, disparar
   `WidgetRefreshWorker.scheduleImmediate` y la sincronización con el reloj
   igual que hace la UI (revisar qué invoca hoy el toggle desde la UI).
6. **Idioma.** KDoc/metadata en inglés (lo consume el LLM); los nombres de
   estación se devuelven tal cual.

### 2.4 Manifest y metadata

`androidApp/src/main/AndroidManifest.xml` (dentro de `<application>`):

```xml
<service
    android:name="com.gcaguilar.biciradar.appfunctions.BiciRadarAppFunctionService"
    android:permission="android.permission.BIND_APP_FUNCTION_SERVICE"
    android:exported="true"
    tools:targetApi="36">
    <property
        android:name="android.app.appfunctions.schema"
        android:value="app_functions_schema.xsd" />
    <property
        android:name="android.app.appfunctions.v2"
        android:value="biciradar_app_function_service.xml" />
    <intent-filter>
        <action android:name="android.app.appfunctions.AppFunctionService" />
    </intent-filter>
</service>
<property
    android:name="android.app.appfunctions.app_metadata"
    android:resource="@xml/app_metadata" />
```

`androidApp/src/main/res/xml/app_metadata.xml` (borrador):

```xml
<AppFunctionAppMetadata xmlns:appfn="http://schemas.android.com/apk/androidx.appfunctions"
    appfn:instructions="BiciRadar shows real-time availability of public bike-sharing stations in Spanish cities and manages the user's favorite stations.
Operational patterns:
- All functions act on the city currently selected in the app; 'getCurrentCity' returns it.
- Resolve station names to IDs with 'searchStations' (or 'findNearestStation' / 'listFavoriteStations') before calling functions that take a stationId.
- Prefer 'listFavoriteStations' over multiple 'getStationAvailability' calls when the user asks about their saved stations.
- Pass the user's latitude/longitude when known; otherwise distances may be unavailable (-1).
Constraints:
- Availability changes every few minutes; do not cache results across conversations.
- Switching city is not supported through these functions.
- 'home'/'casa' and 'work'/'trabajo' are aliases for the user's pinned stations."
    appfn:displayDescription="@string/appfunctions_display_description" />
```

Añadir `appfunctions_display_description` a `androidApp/src/main/res/values*/strings.xml`
(es, en, ca, eu, gl — mismos idiomas que el resto de strings).

---

## 3. Refinamiento de KDoc (obligatorio tras implementar)

Checklist por función:
- Empieza con verbo imperativo y beneficio ("Find the nearest station with free docks…").
- Dependencias con la frase estándar: `Required workflow: Call "searchStations" first to "obtain a stationId"`.
- `@param` con validación y default; `@return` concreto; `@throws` con acción
  de recuperación para el agente ("If no city is configured, ask the user to open BiciRadar and pick a city").
- Sin relleno ("This method is used to…"), descriptivo, no imperativo hacia el LLM.
- Serializables: KDoc inline en cada propiedad.
- Metadata: relaciones entre funciones y restricciones, sin repetir descripciones.

---

## 4. Testing y validación

### 4.1 Unit tests
- Mappers `Station → StationInfo` (e-bikes, distancia desconocida, `Instant`).
- Lógica de `requirement` y validaciones (`limit`, `query` vacía).
- Use case nuevo de "nearest from origin" en `shared/core/commonTest`.
- Favoritos idempotentes (`add` sobre favorita no la desmarca).
- Extraer la lógica del servicio a una clase `internal` testeable
  (`BiciRadarAppFunctions(graph)`) y dejar el servicio como delegación fina,
  para testear sin `AppFunctionService` real.

### 4.2 Build
```bash
./gradlew :androidApp:assemblePlaystoreDebug
./gradlew :androidApp:testPlaystoreDebugUnitTest
./gradlew :androidApp:assemblePlaystoreRelease   # sin firma de Play: solo verificar R8
unzip -l androidApp/build/outputs/apk/playstore/debug/*.apk | grep app_function
```

### 4.3 En dispositivo (Android 16+ / emulador API 36+)
```bash
adb shell cmd app_function help
adb shell cmd app_function list-app-functions --package com.gcaguilar.biciradar
adb shell "cmd app_function execute-app-function --package com.gcaguilar.biciradar \
  --function 'com.gcaguilar.biciradar.appfunctions.BaseBiciRadarAppFunctionService#searchStations' \
  --parameters '{\"query\": \"plaza españa\"}' --brief-yaml"
adb shell "cmd app_function execute-app-function --package com.gcaguilar.biciradar \
  --function 'com.gcaguilar.biciradar.appfunctions.BaseBiciRadarAppFunctionService#findNearestStation' \
  --parameters '{\"requirement\": \"BIKES\", \"latitude\": 41.6488, \"longitude\": -0.8751}' --brief-yaml"
```
Casos: arranque en frío (force-stop antes de invocar), sin red (caché),
ciudad no configurada, stationId inválido, sin permiso de ubicación.

### 4.4 Con agente
- App **AppFunction Testing Agent** (repo `android/appfunctions`): pestaña Debug
  (descubrimiento/ejecución) y Agent Demo (Gemini). Prompts de prueba:
  - "¿Cuántas bicis hay en mi estación de casa?"
  - "Busca la estación más cercana con huecos para dejar la bici."
  - "Añade la estación de Plaza Aragón a favoritas."
- Cuando esté disponible, probar con Gemini vía Android MCP.

---

## 5. Fases y entregables

| Fase | Contenido | Entregable / criterio de hecho |
|---|---|---|
| 1. Spike | KSP + appfunctions beta01, servicio con 1 función (`getCurrentCity`), manifest + metadata mínimos | Compila debug y release; `list-app-functions` muestra la función; se invoca en frío. Decisión documentada sobre KSP (módulo app vs módulo aislado). |
| 2. MVP | 9 funciones de la tabla MVP, modelos, mappers, manejo de ubicación/frescura/bootstrap, refresco de widgets | Tests unitarios verdes, validación adb de todos los casos de 4.3. |
| 3. KDoc + metadata | Refinamiento según sección 3, strings localizados | Revisión de descripciones en `list-app-functions`; prueba con Testing Agent (prompts de 4.4). |
| 4. Ampliación | Funciones de Fase 3 (home/work, monitorización, alertas) | Cada una con análisis de efectos y tests. |
| 5. Release | Entrada en `CHANGELOG.md`, doc en `docs/` (y `docs/wiki` para usuario), actualizar `AGENTS.md` con la nueva superficie Android | PR con Conventional Commits (`feat(android): …`). |

---

## 6. Riesgos y preguntas abiertas

1. **KSP con Kotlin 2.5.0-Beta2 + AGP 9 built-in Kotlin + Metro**: principal
   riesgo técnico; mitigado por el spike y la opción de módulo aislado.
2. **API en beta**: nombres de anotaciones/atributos han cambiado entre alphas
   y beta01 (`@AppFunction` → `@AppFunctionDeclaration`, `description` →
   `instructions`). Fijar versión y revisar release notes al subir.
3. **Ubicación en background**: ver 2.3.1; decidir si basta con lat/lng del agente.
4. **FGS desde servicio enlazado** para monitorización (Fase 3).
5. **Privacidad**: las favoritas/home/work revelan patrones de movimiento del
   usuario. El sistema controla qué agentes pueden enlazar
   (`BIND_APP_FUNCTION_SERVICE`), pero valorar un ajuste "Permitir a asistentes
   acceder a mis favoritas" y revisar la declaración de Data Safety en Play.
6. **Paridad iOS**: el catálogo replica los App Intents de `BiziShortcuts.swift`;
   mantener nombres y semántica alineados para no divergir.
7. **A2UI** (respuestas con UI rica, experimental): fuera de alcance; reevaluar
   tras el MVP.
