# Android AppFunctions

## 1. Resumen

BiciRadar expone sus capacidades principales a agentes del sistema mediante
Android AppFunctions: consultar la ciudad activa, buscar estaciones, leer
disponibilidad en tiempo real, localizar la estación más cercana y gestionar
favoritas sin abrir la interfaz. El catálogo está pensado para asistentes
on-device como Gemini (a través de Android MCP), el AppFunction Testing Agent y
cualquier agente autorizado por el sistema.

La integración vive en `:androidApp` y requiere Android 16 (API 36) o superior
para ejecutarse. La app mantiene `minSdk = 29`; en versiones anteriores la
plataforma no descubre el servicio y el resto de la app funciona igual.

- Módulo: `androidApp`
- Paquete: `com.gcaguilar.biciradar.appfunctions`
- Entrada: `BaseBiciRadarAppFunctionService`
- Librería: `androidx.appfunctions:appfunctions` y
  `androidx.appfunctions:appfunctions-compiler` `1.0.0-beta01`

## 2. Catálogo de funciones

Todas las funciones son `suspend`, se ejecutan en `Dispatchers.IO` y operan
sobre la ciudad seleccionada en la app. Ninguna cambia de ciudad.

| Función | Entrada | Salida | Para qué sirve |
| --- | --- | --- | --- |
| `getCurrentCity` | — | `CityInfo` (`id`, `displayName`, `supportsEbikes`) | Devuelve la ciudad activa. Es el contexto de todas las demás funciones. |
| `searchStations` | `query: String`, `limit: Int? = null` (5 si se omite) | `List<StationInfo>` | Resuelve un nombre, calle o alias (`home`/`casa`, `work`/`trabajo`) a un `stationId`. Prerrequisito del resto. |
| `getStationAvailability` | `stationId: String` | `StationInfo` | Bicis, mecánicas, eléctricas, huecos y marca temporal de una estación concreta. |
| `findNearestStation` | `requirement: String? = null` (`ANY` si se omite), `latitude: Double?`, `longitude: Double?` | `StationInfo?` | Estación más cercana que cumple `ANY`, `BIKES`, `EBIKES` o `SLOTS`. |
| `listNearbyStations` | `limit: Int? = null` (5 si se omite), `latitude: Double?`, `longitude: Double?` | `List<StationInfo>` | Varias estaciones cercanas para comparar opciones. |
| `listFavoriteStations` | — | `List<StationInfo>` | Favoritas del usuario, con los indicadores de casa y trabajo. |
| `addFavoriteStation` | `stationId: String` | `StationInfo` | Añade una estación a favoritas. Solo añade. |
| `removeFavoriteStation` | `stationId: String` | `Boolean` | Quita una estación de favoritas. Solo quita. |
| `openStationInApp` | `stationId: String` | `PendingIntent` | Abre la estación en BiciRadar mediante el deep link `biciradar://station/<id>`. |
| `setHomeStation` | `stationId: String` | `StationInfo` | Fija la estación como casa. Reemplaza la anterior. No la marca como favorita. |
| `setWorkStation` | `stationId: String` | `StationInfo` | Fija la estación como trabajo. Reemplaza la anterior. No la marca como favorita. |
| `startStationMonitoring` | `stationId: String`, `kind: String? = null` (`BIKES`/`DOCKS`, por defecto `DOCKS`), `durationMinutes: Int? = null` (1–60, por defecto 10) | `MonitoringInfo` | Abre una ventana de avisos temporal para una estación. |
| `stopStationMonitoring` | `stationId: String? = null` | `Boolean` | Detiene una estación o, sin `stationId`, todas las sesiones. |
| `createSavedPlaceAlert` | `stationId: String`, `target: String? = null` (`FAVORITE`/`HOME`/`WORK`), `condition: String`, `count: Int? = null` | `SavedPlaceAlertInfo` | Crea o reemplaza una alerta de lugar guardado. |

Los parámetros opcionales se declaran `null`-ables porque la API beta01 rellena
un `Int` omitido con `0` en lugar del valor por defecto de Kotlin. `null` se
normaliza al valor documentado (5 resultados, `ANY`, 10 minutos, `FAVORITE`) y
un `0` o `21` explícito sigue siendo un error.

Notas de flujo obligatorio:

- Las funciones que reciben `stationId` no aceptan nombres. El agente debe
  obtener un identificador válido con `searchStations`, `findNearestStation` o
  `listFavoriteStations` antes de llamarlas.
- `addFavoriteStation` y `removeFavoriteStation` son idempotentes: añadir una
  favorita existente no la desmarca y quitar una que no lo es no falla. No son
  un toggle, a diferencia del botón de la interfaz.
- `setHomeStation` y `setWorkStation` son categorías independientes de
  favoritas: fijar casa no añade ni quita la estación de favoritas. Solo hay una
  casa y un trabajo; cada llamada reemplaza el pin anterior.
- `startStationMonitoring` es temporal y devuelve `started = false` cuando no se
  pudo abrir la ventana (sin sesión, sin token push o backend no disponible).
  Ese `false` debe comunicarse tal cual: no se han programado avisos. El
  servicio en primer plano es opcional; los avisos llegan por push.
- `stopStationMonitoring` sin `stationId` detiene todas las sesiones activas;
  con `stationId` solo detiene esa y deja el resto.
- `createSavedPlaceAlert` exige que la estación sea favorita para `FAVORITE`, o
  la casa/trabajo fijada para `HOME`/`WORK`. La regla se identifica por estación
  y tipo, así que repetir la llamada reemplaza la condición anterior.
- Para preguntas sobre las estaciones guardadas, `listFavoriteStations` es
  preferible a encadenar varias llamadas a `getStationAvailability`.
- La disponibilidad cambia cada pocos minutos. No debe reutilizarse entre
  conversaciones ni presentarse como un dato permanente.

## 3. Arquitectura

La integración es una superficie de plataforma Android, no lógica de dominio:
vive en `androidApp` y solo orquesta casos de uso que ya existen en
`shared/core`.

El paquete
`androidApp/src/main/kotlin/com/gcaguilar/biciradar/appfunctions/` contiene el
servicio base con las declaraciones, los modelos serializables (`StationInfo`,
`CityInfo`) y los mappers desde los modelos de `shared/core`.

El servicio es abstracto y declara el punto de entrada; KSP genera la clase
concreta y el inventario XML:

```kotlin
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
  serviceName = "BiciRadarAppFunctionService",
  appFunctionXmlFileName = "biciradar_app_function_service",
)
abstract class BaseBiciRadarAppFunctionService : AppFunctionService() {
  private val graph get() = BiziAppGraph.graph

  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun getCurrentCity(): CityInfo =
    withContext(Dispatchers.IO) {
      graph.getCurrentCity.execute().let { city ->
        CityInfo(
          id = city.id,
          displayName = city.displayName,
          supportsEbikes = city.supportsEbikes,
        )
      }
    }
}
```

- Generación: KSP produce `BiciRadarAppFunctionService` y
  `assets/biciradar_app_function_service.xml`. El manifest apunta al XML y al
  esquema; no se escribe a mano.
- Orquestación: el servicio es una capa fina. Toda la lógica vive en
  `BiciRadarAppFunctions`, que depende de la interfaz estrecha
  `BiciRadarAppData` (sin tipos de Android) y de `AppFunctionErrors`. Así los
  tests de JVM cubren validaciones, idempotencia de favoritos y errores sin
  Robolectric. El servicio solo conserva `Intent`, `PendingIntent` y el refresco
  de widgets.
- Dependencias: `BiziAppGraph.graph` (`SharedGraph` de Metro), inicializado en
  `BiciRadarApplication.onCreate()` antes de que el sistema pueda enlazar
  ningún servicio. No se usa Hilt.
- Modelo de datos: `StationInfo`, `CityInfo`, `MonitoringInfo` y
  `SavedPlaceAlertInfo` están anotados con
  `@AppFunctionSerializable(isDescribedByKDoc = true)` y documentan cada
  propiedad inline, porque KSP ignora el KDoc de nivel `@property`.

Campos de `StationInfo`:

| Campo | Tipo | Significado |
| --- | --- | --- |
| `id` | `String` | Identificador estable; se pasa al resto de funciones. |
| `name` | `String` | Nombre visible de la estación. |
| `address` | `String` | Dirección postal. |
| `bikesAvailable` | `Int` | Bicis disponibles ahora mismo. |
| `ebikesAvailable` | `Int` | Bicis eléctricas; 0 si la ciudad no tiene. |
| `regularBikesAvailable` | `Int` | Bicis mecánicas cuando el proveedor reporta tipos; `0` si no los reporta. El total está en `bikesAvailable`. |
| `freeDocks` | `Int` | Huecos libres para devolver la bici. |
| `distanceMeters` | `Int` | Distancia al usuario; `-1` si se desconoce. |
| `latitude` / `longitude` | `Double` | Coordenadas de la estación. |
| `isFavorite` | `Boolean` | Si está marcada como favorita. |
| `isHome` / `isWork` | `Boolean` | Si es la estación fijada como casa o trabajo. |
| `updatedAt` | `Instant?` | Última actualización de la disponibilidad. |

Campos de `MonitoringInfo`:

| Campo | Tipo | Significado |
| --- | --- | --- |
| `stationId` | `String` | Estación vigilada. |
| `started` | `Boolean` | Si la ventana de avisos se abrió y se entregarán avisos. |
| `durationMinutes` | `Int` | Duración total de la sesión. |
| `monitors` | `String` | `BIKES` o `DOCKS`. |
| `message` | `String` | Explicación accionable para el agente. |

Campos de `SavedPlaceAlertInfo`:

| Campo | Tipo | Significado |
| --- | --- | --- |
| `id` | `String` | Identificador estable de la regla. |
| `stationId` / `stationName` | `String` | Estación vigilada. |
| `target` | `String` | `FAVORITE`, `HOME` o `WORK`. |
| `condition` | `String` | `BIKES_AT_LEAST`, `DOCKS_AT_LEAST`, `BIKES_EQUALS_ZERO` o `DOCKS_EQUALS_ZERO`. |
| `count` | `Int` | Umbral para `*_AT_LEAST`; `0` para `*_EQUALS_ZERO`. |
| `isEnabled` | `Boolean` | Una regla nueva queda habilitada. |

## 4. Configuración

### 4.1 Gradle

Versiones en `gradle/libs.versions.toml`:

```toml
[versions]
androidx-appfunctions = "1.0.0-beta01"
ksp = "2.3.13"

[libraries]
androidx-appfunctions = { module = "androidx.appfunctions:appfunctions", version.ref = "androidx-appfunctions" }
androidx-appfunctions-compiler = { module = "androidx.appfunctions:appfunctions-compiler", version.ref = "androidx-appfunctions" }

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

`androidApp/build.gradle.kts`:

```kotlin
plugins {
  alias(libs.plugins.ksp)
}

android {
  compileSdk {
    version = release(37) { minorApiLevel = 1 }
  }
  defaultConfig {
    minSdk = 29
    targetSdk = 37
  }
}

ksp {
  arg("appfunctions:aggregateAppFunctions", "true")
}

dependencies {
  implementation(libs.androidx.appfunctions)
  ksp(libs.androidx.appfunctions.compiler)
}
```

El compilador se aplica con KSP, no como procesador de anotaciones de Kotlin.
`aggregateAppFunctions` emite un único inventario para todo el módulo.

Decisión del spike: **KSP `2.3.13` funciona con Kotlin `2.5.0-Beta2`, AGP
`9.5.0` (Kotlin integrado, sin plugin `kotlin-android`), Metro y Compose**. No
hizo falta bajar a `2.3.12`/`2.3.10` ni aislar un módulo `:androidApp:appfunctions`.
Si una futura actualización de Kotlin o AGP rompe KSP, ese módulo aislado sigue
siendo el plan B.

### 4.2 Manifest

El servicio generado se declara en `androidApp/src/main/AndroidManifest.xml`
dentro de `<application>`:

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

`BIND_APP_FUNCTION_SERVICE` lo concede el sistema: solo los agentes que Android
autoriza pueden enlazar el servicio.

### 4.3 Metadata e instrucciones

`androidApp/src/main/res/xml/app_metadata.xml` describe el conjunto para el
modelo. En `1.0.0-beta01` el atributo es `appfn:instructions` (en las alphas se
llamaba `appfn:description`). Extracto:

```xml
<AppFunctionAppMetadata
    xmlns:appfn="http://schemas.android.com/apk/androidx.appfunctions"
    appfn:instructions="BiciRadar exposes real-time availability of public bike-sharing stations in Spanish cities and manages the user's favorite stations.
Operational patterns:
- Every function acts on the city currently selected in the app; 'getCurrentCity' identifies it.
- Resolve station names to station IDs with 'searchStations', 'findNearestStation' or 'listFavoriteStations' before calling a function that takes a stationId. Never invent or reuse a station ID from a different city or conversation.
- 'setHomeStation' and 'setWorkStation' replace the previous pin for their category and are independent of favorites: pinning does not make a station favorite.
- Monitoring is time-boxed (10 minutes by default, 60 at most). A 'started' value of false means no alert was scheduled; report that honestly.
- 'stopStationMonitoring' with no stationId stops every active session.
- 'createSavedPlaceAlert' requires a favorite or the matching pinned home/work station, and replaces the rule per station and target.
Constraints:
- Availability changes every few minutes. Never cache a station or availability result across conversations.
- 'addFavoriteStation' and 'removeFavoriteStation' are not toggles.
- A null result from 'findNearestStation' or an empty list is a valid answer.
- Changing the selected city is not available through these functions."
    appfn:displayDescription="@string/appfunctions_display_description" />
```

`appfunctions_display_description` está traducido en `values`, `values-en`,
`values-ca`, `values-eu` y `values-gl`. El texto de `appfn:instructions` va en
inglés porque lo consume el modelo, igual que el KDoc de las funciones.

## 5. Estados y errores

Las funciones no convierten un fallo en una disponibilidad plausible. Los
errores usan las excepciones de AppFunctions con mensajes que indican al agente
cómo recuperarse.

| Situación | Comportamiento | Recuperación esperada |
| --- | --- | --- |
| Ciudad no configurada | Todas las funciones, incluida `getCurrentCity`, lanzan `AppFunctionAppUnknownException` con un mensaje accionable. | Pedir al usuario que abra BiciRadar y seleccione una ciudad. No cambiar de ciudad desde una función. |
| `stationId` inexistente | `AppFunctionElementNotFoundException`. | Volver a resolver el identificador con `searchStations`, `findNearestStation` o `listFavoriteStations`. |
| Argumento inválido | `AppFunctionInvalidArgumentException`: `query` vacía, `limit` fuera de rango o `requirement` distinto de `ANY`/`BIKES`/`EBIKES`/`SLOTS`. | Corregir el argumento; no reintentar con el mismo valor. |
| Sin red y sin datos utilizables | `AppFunctionAppUnknownException` con mensaje accionable. | Informar de que no hay datos en este momento; no inventar disponibilidad. |
| Ubicación desconocida | `distanceMeters = -1`; no se falla por ello. | Pasar `latitude`/`longitude` si el agente los conoce. |
| Disponibilidad leída | Se refresca al leer `getStationAvailability`. | No cachear entre conversaciones. |
| Monitorización no disponible | `startStationMonitoring` devuelve `MonitoringInfo(started = false)` con un mensaje accionable; no lanza. | Informar de que no hay avisos programados y pedir al usuario que abra BiciRadar, revise la sesión y los permisos de notificaciones. |
| Alerta sobre un lugar no guardado | `AppFunctionInvalidArgumentException` si `FAVORITE` no es favorita o `HOME`/`WORK` no coincide con el pin actual. | Llamar antes a `addFavoriteStation`, `setHomeStation` o `setWorkStation`, o cambiar el `target`. |
| Condición o umbral inválidos | `AppFunctionInvalidArgumentException` si `condition` no es una de las cuatro, o `count` queda fuera de 1..100 en `*_AT_LEAST`. | Corregir el argumento; `*_EQUALS_ZERO` ignora `count`. |

Detalles de comportamiento:

- **Ubicación en background.** Cuando el sistema enlaza el servicio, la app no
  está en primer plano y no declara `ACCESS_BACKGROUND_LOCATION`, así que la
  ubicación propia puede ser nula o imprecisa. Por eso
  `findNearestStation` y `listNearbyStations` aceptan `latitude`/`longitude`
  opcionales del llamante y recalculan distancias con ellos. Si no hay origen,
  la función sigue respondiendo con `distanceMeters = -1`.
- **Frescura.** La disponibilidad se refresca al leerla; `loadIfNeeded` por sí
  solo no basta porque los datos de bicis caducan en minutos.
- **Arranque en frío.** El grafo se inicializa en `Application.onCreate()`, que
  corre antes de que el sistema enlace el servicio, de modo que las funciones
  no dependen de que `MainActivity` se haya abierto.
- **Favoritas.** Las mutaciones se hacen con `ToggleFavoriteStation`, el mismo
  camino que la interfaz, para que la sincronización con el reloj se dispare
  dentro del repositorio; además el servicio programa un refresco inmediato de
  widgets. `addFavoriteStation` solo añade y `removeFavoriteStation` solo quita,
  así que no hay riesgo de desmarcar una favorita por accidente.

## 6. Validación

### 6.1 Local

```bash
./gradlew :androidApp:compilePlaystoreDebugKotlin
./gradlew :androidApp:testPlaystoreDebugUnitTest
./gradlew :androidApp:assemblePlaystoreDebug
unzip -l androidApp/build/outputs/apk/playstore/debug/*.apk | grep app_function
```

`assemblePlaystoreRelease` (R8 + `shrinkResources`) necesita la firma de Play,
que el repo exige para cualquier release. Para comprobar solo R8 en local se
puede relajar esa puerta con el keystore de debug, sin tocar el repo:

```bash
BIZI_CI_KEYSTORE_PATH="$HOME/.android/debug.keystore" \
BIZI_CI_KEYSTORE_PASSWORD=android \
BIZI_CI_KEY_ALIAS=androiddebugkey \
BIZI_CI_KEY_PASSWORD=android \
./gradlew :androidApp:assemblePlaystoreRelease
```

El APK de release debe seguir conteniendo
`assets/biciradar_app_function_service.xml`, y `mapping.txt` debe conservar
`BiciRadarAppFunctionService`, `CityInfo`, `StationInfo` y sus factories. Las
reglas consumer del AAR son suficientes: no hace falta tocar
`proguard-rules.pro`.

### 6.2 En dispositivo o emulador

Requiere una build Android 16 (API 36) o superior:

```bash
adb shell cmd app_function list-app-functions --package com.gcaguilar.biciradar

adb shell "cmd app_function execute-app-function --package com.gcaguilar.biciradar \
  --function 'com.gcaguilar.biciradar.appfunctions.BaseBiciRadarAppFunctionService#searchStations' \
  --parameters '{\"query\": \"plaza españa\"}' --brief-yaml"

adb shell "cmd app_function execute-app-function --package com.gcaguilar.biciradar \
  --function 'com.gcaguilar.biciradar.appfunctions.BaseBiciRadarAppFunctionService#findNearestStation' \
  --parameters '{\"requirement\": \"BIKES\", \"latitude\": 41.6488, \"longitude\": -0.8751}' --brief-yaml"
```

Casos que conviene cubrir: arranque en frío (hacer `force-stop` antes de
invocar), sin red, ciudad no configurada, `stationId` inválido y sin permiso de
ubicación.

Estado verificado: el APK de debug se instala en API 36 y `dumpsys package`
muestra el servicio con `BIND_APP_FUNCTION_SERVICE` y la acción
`android.app.appfunctions.AppFunctionService`; el inventario generado contiene
todas las funciones del catálogo y los campos de `StationInfo` con su
descripción. La ejecución end-to-end por
`adb` **no** está verificada en esta máquina porque la imagen del emulador
devuelve `No shell command implementation` para `cmd app_function`. En ese caso
hay que usar una imagen que incluya el comando o probar con el agente.

### 6.3 Con agentes

- App **AppFunction Testing Agent**: pestaña Debug para descubrir y ejecutar
  funciones, y Agent Demo con Gemini.
- Gemini mediante Android MCP cuando el acceso esté disponible.

Prompts útiles:

- "¿Cuántas bicis hay en mi estación de casa?"
- "Busca la estación más cercana con huecos para dejar la bici."
- "Añade la estación de Plaza Aragón a favoritas."

## 7. Privacidad y limitaciones

- **API experimental.** AppFunctions `1.0.0-beta01` puede cambiar entre
  versiones: anotaciones, atributos de metadata y nombres de comandos `adb` ya
  han cambiado entre alphas y beta. Fijar la versión y revisar las release notes
  antes de actualizar.
- **Acceso controlado por el sistema.** El servicio está protegido por
  `BIND_APP_FUNCTION_SERVICE`; solo los agentes autorizados por Android pueden
  enlazarlo. Aun así, las favoritas y las estaciones de casa y trabajo revelan
  patrones de movimiento del usuario, así que deben tratarse como datos
  sensibles. Todavía no hay un ajuste para desactivar el acceso de asistentes a
  favoritas; si se añade, debe documentarse aquí y revisarse la declaración de
  Data Safety de Play.
- **Sin ubicación en background.** No se declara `ACCESS_BACKGROUND_LOCATION`.
  La precisión depende de la última ubicación conocida o de las coordenadas que
  aporte el agente, y la distancia puede ser desconocida (`-1`).
- **Sin cambio de ciudad.** Las funciones no modifican la ciudad seleccionada.
  `ChangeCityUseCase` borra todas las favoritas y las alertas de la ciudad
  anterior y no hay un canal de confirmación interactiva en AppFunctions, así
  que el cambio de ciudad se hace solo desde la app.
- **Monitorización temporal.** `startStationMonitoring` abre una ventana de
  avisos (10 minutos por defecto, máximo 60). El servicio en primer plano es
  *best-effort*: si Android bloquea su arranque en segundo plano, los avisos
  siguen llegando por push. Un `started = false` significa que no se programó
  ningún aviso y debe comunicarse así.
- **Alertas de lugar guardado.** Se crean y reemplazan por estación y tipo;
  desactivarlas o borrarlas sigue siendo una acción de la app.
- **Gemini en preview privada.** El acceso de Gemini a AppFunctions depende de
  un programa en preview; no debe asumirse disponible para todos los usuarios.
- **Paridad con iOS.** El catálogo se alinea con los App Intents de
  `apple/iosApp/BiziShortcuts.swift`, pero no es idéntico: en Android el cambio
  de ciudad queda deliberadamente fuera y las alertas de lugar guardado se
  exponen como función. La paridad se mantiene por separado: un cambio aquí no
  actualiza iOS automáticamente.

## 8. Referencias

- [Android AppFunctions](https://developer.android.com/ai/appfunctions)
- [Release notes de androidx.appfunctions](https://developer.android.com/jetpack/androidx/releases/appfunctions)
- [Sample oficial `android/appfunctions`](https://github.com/android/appfunctions)
- [Diseño y decisiones en `plan.md`](../plan.md)
