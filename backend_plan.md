# BiciRadar — Migración de la lógica de monitorización a backend + push

> Estado: **propuesta / plan**. Documento vivo para iterar antes de implementar.

## 1. Problema

Hoy las apps (Android, Wear OS, iOS, watchOS) sondean directamente las APIs de cada
ciudad desde el dispositivo:

- `SurfaceMonitoringRepository` sondea cada 5 min las estaciones vigiladas.
- `SavedPlaceAlertsWorker` (Android) evalúa alertas cada 15 min.
- `RoutineMonitorWorker` (Android) dispara la vigilancia de rutinas; en iOS es un
  recordatorio local *best-effort* (imposible vigilar a hora exacta en background).

Problemas:

1. **Rate limits**: las APIs de ciudad son por estación (N llamadas por ciclo); si
   muchos usuarios vigilan, la presión crece.
2. **iOS no puede ejecutar en background a hora exacta**: no hay vigilancia real sin
   abrir la app.
3. **Lógica duplicada y dependiente del dispositivo**: cada instalación hace su propio
   sondeo, gastando batería y cuota.
4. **Las rutinas solo avisan al arrancar**: no vigilan durante la duración real del
   trayecto y no sugieren alternativas cuando la estación falla.

**Objetivo**: mover la detección de cambios al backend, notificar por **push** durante
la **ventana activa** de cada rutina (estación sin bicis, nivel crítico de bicis, sin
huecos, nivel crítico de huecos) y **sugerir una estación alternativa** (definida por
el usuario o la cercana que cumpla la condición).

## 2. Decisiones tomadas

| Tema | Decisión |
| --- | --- |
| Stack backend | **Kotlin + Ktor** (JVM), **standalone**: no reutiliza `shared/core`; el acoplamiento es el contrato (OpenAPI + push) |
| Cobertura de sondeo | **Solo estaciones con suscripción** (interest-driven); con GBFS puede ampliarse a cobertura completa |
| Forma de los endpoints | **Solo por estación** en las APIs propietarias; **GBFS** da `station_status.json` batch |
| Push | **Android (FCM) + iOS (APNs, vía FCM)** |
| Autenticación | **Auth0** (OIDC, Authorization Code + PKCE); el backend valida JWT con JWKS |
| Propiedad de datos | **User-scoped**: rutinas/favoritos/suscripciones pertenecen al usuario (Auth0 `sub`), sincronizan entre dispositivos |
| Composición del push | **El backend compone** título/cuerpo localizados (según `locale` del device) + data payload para deep link |
| Contenido del data payload | Limitado a conceptos que la app ya puede renderizar (estación, evento, alternativa con bicis/huecos/distancia) |
| Horario de rutinas | **Backend**: ventana activa desde `HH:MM − 10 min` hasta `fin de la rutina`; elimina el recordatorio best-effort de iOS |
| Fallback offline | **Push como fuente principal**; la app muestra caché local si no hay push |
| Base de datos | **SQLite**: una BD por ciudad (estaciones, caché) + una BD principal (usuarios, rutinas, suscripciones, outbox) |
| Caché en caliente | **En memoria** (apenas hay usuarios); sin Redis por ahora |
| Textos de notificación | **Catálogo en el backend** (locales soportados + fallback a inglés) |
| Catálogo de ciudades | **Fijo**, portado de la app (`City.kt`, 51 ciudades; ver §11); sin alta dinámica |
| Despliegue | **Dockerizado**: imagen del backend + **docker compose preparado para Coolify** (§13) |
| Alcance | **Multiciudad**: un backend atiende todas las ciudades a la vez; poller, presupuesto y BD propios por ciudad; todo se referencia como `(cityId, stationId)` |
| Radio de la alternativa | **Lo define el usuario** (por paso), **acotado por el backend** (default, mín y máx) |
| Reloj | **No es cliente**: solo el teléfono inicia sesión, se registra y recibe push; el reloj es superficie pasiva |
| Historial de uso | **Agregación incremental** en buckets `(estación, día de semana, hora)` con rotación por `epoch_day` (7 días); sin muestras crudas; endpoint **público**; cobertura completa opt-in (`COVERAGE_ENABLED`); **Zaragoza excluida** (sigue con datosbizi) |

## 3. Arquitectura propuesta

```text
[App] --Auth0 (OIDC/PKCE)--> [Auth0] --JWT--> [App]
[App] --REST /v1 (Bearer JWT): register device / sync subscriptions / CRUD routines--> [Backend API]
[Backend] pollers por ciudad (cola + rate limit + backoff) --> [APIs de ciudad / feeds GBFS]
[Cache + diff por estación] --> eventos de transición --> [Outbox] --> [Pusher FCM/APNs] --> [App]
[Cron de rutinas] abre "ventanas de vigilancia" server-side --> mismos eventos + resolución de alternativa
```

### 3.1 Componentes del backend

1. **`auth`** — validación de JWT de Auth0 contra JWKS (issuer + audience). Sin
   gestión de credenciales propia. Endpoints de ciclo de cuenta (borrado de datos).
2. **`ingestion`** — adaptadores por ciudad. Presupuesto por ciudad, backoff ante 429,
   caché por estación. Con GBFS: 1–2 descargas por ciclo en vez de N. Persiste también
   `station_information.json` (geo/capacidad) para la resolución de alternativas.
3. **`subscriptions`** — usuario → estaciones + tipo (favorito / alerta / rutina) +
   condición/umbral. Sustituye a lo que hoy persisten `SavedPlaceAlertsRepository` y
   `RoutineRepository`.
4. **`diff`** — compara estado nuevo vs caché; emite los eventos de §6.2.
5. **`scheduler`** — cron de rutinas (días + hora − 10 min) que abre **ventanas de
   vigilancia** server-side (ver §6.1).
6. **`alternatives`** — resuelve la estación alternativa: fallback del usuario si
   cumple la condición, o la más cercana que la cumpla dentro de un radio (ver §6.3).
7. **`push`** — FCM/APNs: compone título/cuerpo según `locale`, dedup por token,
   cooldown por usuario+estación+evento y borrado de tokens inválidos
   (`410 Gone` / `Unregistered`).
8. **`api`** — REST `/v1` versionado, descrito con **OpenAPI** (fuente de verdad del
   contrato), para registro de device, sincronía de intereses y CRUD de rutinas.

### 3.2 Modelo de datos (BD del backend)

```text
-- BD principal (SQLite): datos de usuario y reparto de push
users(id, auth0_sub, created_at)
devices(id, user_id, token, platform, app_id, locale, push_environment, last_seen)
subscriptions(id, user_id, station_id, city_id, kind, condition, threshold, enabled)
routines(id, user_id, name, days_bitmap, hour, minute, enabled)
routine_steps(id, routine_id, position, station_id, city_id, kind, duration_seconds, threshold, fallback_station_id, alternative_radius_meters)
routine_windows(id, routine_id, opens_at, closes_at)   -- materializado por el cron; estado runtime
event_outbox(id, user_id, station_id, city_id, event, payload, locale, created_at, retries)

-- BD por ciudad (SQLite, una por ciudad, p. ej. biciradar_zaragoza.db)
stations(city_id, station_id, name, lat, lon, capacity)
station_cache(city_id, station_id, bikes, docks, status, reported_at)
```

Notas:

- **Multiciudad**: una rutina puede cruzar ciudades; cada paso referencia su
  `(city_id, station_id)`. La alternativa se busca siempre **dentro de la misma
  ciudad** que el paso.
- `routine_steps.kind`: `DEPARTURE` (el usuario coge bici → vigilar **bicis**) o
  `ARRIVAL` (el usuario deja bici → vigilar **huecos**).
- `routine_steps.threshold`: opcional; si es null se usa el umbral crítico por defecto.
- `routine_steps.fallback_station_id`: opcional; alternativa definida por el usuario.
- `routine_steps.alternative_radius_meters`: radio de búsqueda elegido por el usuario;
  el backend lo acota (default/mín/máx) antes de persistirlo.
- `push_environment`: sandbox vs producción (APNs vía FCM).

### 3.3 Contrato app ↔ backend

- **REST (app → backend)**, versionado `/v1`, con OpenAPI como especificación:
  - `POST /v1/devices` — registrar/rotar token push (+ plataforma, locale).
  - `GET/PUT /v1/subscriptions` — alta/baja/reconcilio de intereses.
  - `GET/POST/PUT/DELETE /v1/routines` — CRUD de rutinas (con pasos, umbrales,
    fallbacks y radios de alternativa).
- **Push (backend → app)**: *alert push* con título/cuerpo localizados por el backend +
  data payload limitado a lo que la app puede renderizar:

```json
{
  "cityId": "zaragoza",
  "stationId": "42",
  "event": "BIKES_EMPTY",
  "routineId": "…",
  "alternative": {
    "stationId": "57",
    "bikesAvailable": 4,
    "slotsFree": 9,
    "distanceMeters": 210
  }
}
```

- **App**: nuevo módulo **`api-client`** (shared) con DTOs que mapean a los tipos que
  la app ya soporta; la lógica de diff se duplica app/backend y el contrato es el único
  punto de acoplamiento. Tests de contrato backend ↔ DTOs para evitar deriva.

## 4. GBFS (cómo funciona y por qué cambia el problema)

**GBFS** (*General Bikeshare Feed Specification*) es un estándar abierto que publica
ficheros JSON sobre HTTP, normalmente sin API key.

### 4.1 Ficheros relevantes

| Fichero | Contenido | Frecuencia |
| --- | --- | --- |
| `gbfs.json` | Descubrimiento (feeds, idiomas, versiones) | Raro |
| `system_information.json` | Sistema/operador/zona horaria | Raro |
| `station_information.json` | Datos estáticos: `station_id`, nombre, `lat`/`lon`, capacidad | Raro |
| `station_status.json` | Datos dinámicos: bicis y huecos por estación | ~30 s–5 min |
| `vehicle_types.json` | Tipos de vehículo (bici normal / eléctrica) | Raro |
| `system_alerts.json` | Avisos (estación cerrada, averías) | Ocasional |

### 4.2 Mapeo al modelo del backend

El backend tiene sus propios modelos (no comparte `shared/core`), pero el contrato debe
ser **adaptado a lo que la app puede representar**: identificador de estación y ciudad,
bicis/huecos disponibles, estado operativo y marca temporal — conceptos que las
pantallas, widgets, watch y notificaciones de la app ya saben mostrar.

| GBFS | Concepto del contrato |
| --- | --- |
| `station_id` + system | `stationId` + `cityId` |
| `name` | nombre de estación (para texto del push) |
| `lat` / `lon` | geo (solo backend: resolución de alternativa) |
| `num_bikes_available` | `bikesAvailable` |
| `num_docks_available` | `slotsFree` |
| `num_ebikes_available` | e-bikes vs mecánicas |
| `last_reported` / `last_updated` | marca temporal de frescura |
| `is_renting` / `is_returning` / `is_installed` | operativa o no (`STATION_UNAVAILABLE`) |

### 4.3 Por qué resuelve los rate limits

1. `station_status.json` devuelve **todas las estaciones en 1–2 descargas** por ciclo.
2. Suele ser **público y sin key** → sin (o con muy laxo) rate limit.
3. **`ttl`** indica cuándo volver a pedir.
4. **ETag / If-Modified-Since** → `304 Not Modified` cuando no hay cambios (casi gratis).

Con GBFS, la cobertura completa es barata; la suscripción pasa a ser solo un filtro de
**a quién avisar**, no de **qué sondear**.

## 5. Caché (ttl + ETag)

La caché es obligatoria y cumple tres roles:

1. **Respetar `ttl`** (no pedir antes de que expire).
2. **Un solo fetch para todos** (push, alternativa y pull leen del mismo estado).
3. **Línea base del diff** (guardar el snapshot previo para detectar transiciones).

Forma por ciudad:

```text
city:zaragoza {
  fetchedAt, ttl, lastUpdated, etag,
  stations: { "42": { bikes, docks, status, reportedAt }, ... }
}
```

Flujo:

```text
si ahora - fetchedAt < ttl  -> servir caché
si no -> GET con If-None-Match: etag
          si 304 -> actualizar fetchedAt, registrar muestra de historial con el
                    snapshot cacheado (los valores siguen vigentes), servir caché
          si 200 -> parsear, diff(caché, nuevo), guardar nuevo + etag
```

Notas:

- El `ttl` es una garantía mínima; se puede cachear más si se tolera staleness.
- La cadencia de revalidación no tiene por qué ser igual al `ttl`.
- Un `304` **no** se salta el muestreo de historial: si se ignorara, las medias se
  sesgarían hacia los momentos en que el feed cambia. Hoy publican `ETag` y responden
  `304` `reus` (ganxeta) y `sevilla`/`valencia` (cyclocity); el resto no.
- Ubicación: **en memoria** (decisión: apenas hay usuarios; se revisará si escala). La
  línea base del diff y el snapshot persisten en la BD SQLite por ciudad.
- La app mantiene su propia caché local (SQLDelight) para el display offline.

## 6. Rutinas: ventanas, eventos y estación alternativa

### 6.1 Ventana activa de vigilancia

Una rutina no se vigila "para siempre": el cron abre una **ventana** y al cerrarse dejan
de emitirse push de esa rutina.

```text
apertura = HH:MM − 10 min          (pre-aviso de la rutina)
cierre   = apertura + Σ duration_seconds de los pasos + margen
```

Cada paso vigila su estación **durante su tramo** de la ventana:

```text
paso k activo en [ apertura + Σ duraciones(0..k-1), apertura + Σ duraciones(0..k) ]
```

Fuera de la ventana (y de su tramo), el paso no genera eventos. El cron materializa
`routine_windows` y el pipeline de diff filtra por ventanas abiertas.

### 6.2 Dirección del paso y taxonomía de eventos

El tipo de paso determina qué se vigila:

- `DEPARTURE` (salida): el usuario necesita **bicis**.
- `ARRIVAL` (llegada): el usuario necesita **huecos**.

| Evento | Condición | Paso que lo dispara |
| --- | --- | --- |
| `BIKES_EMPTY` | bicis = 0 | `DEPARTURE` |
| `BIKES_LOW` | bicis < umbral crítico (por defecto 2) | `DEPARTURE` |
| `DOCKS_EMPTY` | huecos = 0 | `ARRIVAL` |
| `DOCKS_LOW` | huecos < umbral crítico | `ARRIVAL` |
| `STATION_UNAVAILABLE` | `is_renting`/`is_returning` = false | ambos |
| `AVAILABLE` | recuperación tras `*_EMPTY`/`*_LOW` (opcional) | ambos |

Reglas de emisión:

- **Cooldown por usuario + estación + evento**: sin repetir el mismo evento en la misma
  ventana, salvo recuperación (`AVAILABLE`) y re-caída posterior.
- Un evento puede ser relevante para varias ventanas/suscripciones a la vez: el outbox
  registra un ítem por (usuario, evento) con dedup.
- `AVAILABLE` se emite como notificación discreta (o solo data push) para no spamear.

### 6.3 Estación alternativa

Cuando se emite `BIKES_EMPTY`/`BIKES_LOW`/`DOCKS_EMPTY`/`DOCKS_LOW`, el componente
`alternatives` resuelve una sugerencia:

1. **Fallback del usuario**: si el paso define `fallback_station_id` y esa estación
   **cumple la condición que falló** (p. ej. bicis ≥ umbral), se sugiere esa.
2. **Cercana automática**: si no hay fallback (o no cumple), la estación más próxima
   que cumpla la condición, dentro del **radio elegido por el usuario** para ese paso
   (`alternative_radius_meters`, con default y máximo impuestos por el backend),
   usando `stations` (geo) + `station_cache` (estado vivo) **de la misma ciudad del
   paso** (multiciudad: nunca se cruza de ciudad para sugerir).
3. Si no hay ninguna que cumpla → push sin `alternative` (la app muestra solo el aviso).

El payload de la alternativa se limita a lo que la app puede renderizar hoy:
`stationId`, `bikesAvailable`, `slotsFree`, `distanceMeters` — deep link al detalle de
estación, bicis/huecos y distancia.

## 7. Autenticación con Auth0

### 7.1 Flujo en la app

- **OIDC Authorization Code + PKCE** contra Auth0 Universal Login.
- Android: Custom Tabs (`auth0-android`); iOS: `ASWebAuthenticationSession`
  (`Auth0.swift`).
- El access token (JWT RS256) viaja como `Authorization: Bearer` en todas las llamadas
  a `/v1`. Refresh token guardado en Keystore/Keychain.
- **Modo anónimo**: las apps siguen siendo funcionales sin login (comportamiento local
  actual); push y sincronía requieren iniciar sesión.

### 7.2 En el backend

- Ktor valida el JWT con las **JWKS** de Auth0 (issuer + audience configurados).
- `users.auth0_sub` es la clave de identidad; cero credenciales propias.
- **User-scoped**: rutinas/favoritos/suscripciones cuelgan de `users`, no del device:
  sincronizan entre móvil y reloj, la rotación de token push no pierde datos.
- Ciclo de cuenta: `DELETE /v1/me` purga todos los datos del usuario (RGPD).
- **Migración**: en el primer login, la app sube las rutinas/favoritos locales
  existentes y reconcilia con `GET /v1/subscriptions` / `GET /v1/routines`.

### 7.3 Solo el teléfono es cliente

- Solo el teléfono inicia sesión en Auth0, registra su token push y habla con el
  backend. Los relojes (Wear OS/watchOS) **no tienen sesión propia ni token push**.
- Las superficies del reloj (watch UI, complicaciones) consumen el estado que el
  teléfono sincroniza en su caché local, igual que hoy.

## 8. iOS y push (APNs)

### 8.1 Por qué APNs resuelve iOS

```text
[Backend detecta cambio] --> [APNs] --> [iPhone/watch, app cerrada]
```

El backend corre 24/7 y detecta el cambio; Apple entrega el push aunque la app esté
cerrada. Se elimina la limitación de "iOS no ejecuta en background a hora exacta" y el
`IosRoutineScheduler` (recordatorio local) deja de ser necesario.

### 8.2 Qué montar en iOS

1. Entitlement **Push Notifications** (en `apple/project.yml` → capability/entitlement).
2. Credencial APNs: key **`.p8`** (token-based) + Key ID + Team ID + Bundle ID.
3. Registrar `deviceToken` (`registerForRemoteNotifications`) y enviarlo al backend.
4. **Topic**: bundle ID de la app (y del watch si se quiere push directo al reloj).
5. El backend compone el `aps` (título/cuerpo según locale + data payload con
   `stationId`/`alternative` para deep link).
6. `UNUserNotificationCenterDelegate` para mostrar en foreground y navegar al tocar.

### 8.3 Puntos que dan guerra

- **Permiso del usuario**: si se deniega, no se muestra (el backend envía igual).
- **Alert push vs silent push**:
  - *Alert* (banner + sonido): es lo que se usa; se entrega aunque la app esté cerrada
    (incluso tras force-quit, desde iOS 10). El texto ya viene compuesto del backend.
  - *Silent* (`content-available`): iOS lo regula/throttlea y no garantiza entrega; **no
    usar para avisar al usuario**.
- **Sandbox vs producción**: desarrollo → APNs sandbox; TestFlight/App Store →
  producción (`push_environment` en `devices`).
- **Rotación de tokens**: actualizar; ante `410 Gone`/`Unregistered`, borrar el token.
- **Apple Watch**: el push va al iPhone y el reloj lo refleja; sin push directo al
  reloj (decisión: solo el teléfono es cliente del backend).
- **APNs es best-effort**: suficiente para "estación sin bicis"; no es transaccional.
- **No spamear**: avisar solo en transiciones, con cooldown por usuario+estación+evento
  (equivalente server-side del actual `ALERT_COOLDOWN_MILLIS`).

### 8.4 FCM vs APNs directo

- **FCM para ambos (recomendado)**: ya hay Firebase en iOS y FCM en Android; un solo
  proveedor, un solo token, una sola integración. FCM en iOS usa APNs por debajo.
- APNs directo + FCM: más control, dos integraciones.

## 9. Cambios en la app (KMP)

1. **`api-client`** (shared, nuevo): DTOs del contrato `/v1` + mapeo a los tipos que la
   app ya soporta. Sin compartir modelos con el backend.
2. **Auth0 login**: flujo OIDC/PKCE en Android e iOS; guardado de tokens; migración de
   rutinas/favoritos locales al primer login.
3. `PushSubscriptionRepository`: registra device (token + locale) y sincroniza
   intereses con el backend.
4. `PushMessageHandler`: recibe el push (texto ya compuesto por el backend), muestra y
   hace deep link a la estación (o a la alternativa sugerida).
5. `SurfaceMonitoringRepository` y `SavedPlaceAlertsWorker` dejan de sondear (pasan a
   sincronizar intereses).
6. Rutinas: CRUD contra el backend con pasos (`DEPARTURE`/`ARRIVAL`, duración, umbral,
   fallback opcional y radio de alternativa); se eliminan `AndroidRoutineScheduler`,
   `RoutineMonitorWorker` e `IosRoutineScheduler`.
7. Reloj: sin cambios de autenticación; consume los datos que el teléfono sincroniza.

## 10. Fases de migración

**Estado (28 oct 2026)**: fases 0 y 2–4 implementadas. Backend con 60 tests y
smoke real sobre los 48 feeds vivos (§11.5); la app ya tiene `api-client`, login
Auth0, registro de device, sincronización de intereses/rutinas y los workers
locales retirados (`SavedPlaceAlertsWorker`, `AndroidRoutineScheduler`,
`RoutineMonitorWorker`, `IosRoutineScheduler` ya no existen). La fase 1
(paridad de eventos en sombra) no se puede cerrar sin un despliegue real con
usuarios. Lo que queda del plan son las decisiones abiertas de §12.

0. **Fundamento**: Auth0 (login en apps + validación JWKS en backend), esqueleto de la
   API `/v1` con OpenAPI, `api-client` y registro de devices.
1. **Sombra**: backend ingiere + cachea + detecta en paralelo; apps siguen sondeando
   (verificar paridad de eventos).
2. **Corte de alertas/favoritos**: registrar token + intereses; recibir push; apagar
   `SavedPlaceAlertsWorker`.
3. **Corte de rutinas**: backend hace el cron + ventanas + resolución de alternativa;
   migración de rutinas locales en el primer login; apagar los schedulers de
   dispositivo.
4. **Limpieza**: retirar el sondeo local de `SurfaceMonitoringRepository` (queda el
   estado de visualización al abrir).

## 11. Catálogo de ciudades (fijo) y URLs

Catálogo **fijo** portado desde `shared/core/src/commonMain/kotlin/com/gcaguilar/biciradar/core/City.kt`
de la app: **51 ciudades**, todas con feed GBFS 2.x (URL de descubrimiento
`gbfs.json`). Sin alta dinámica: añadir una ciudad es cambiar la configuración y
desplegar. El catálogo del backend replica `id`, `displayName`,
`gbfsDiscoveryUrl` (más geo por defecto si hace falta para el display).

**Estado verificado (28 oct 2026)**: 48 de 51 feeds responden con
`station_status` y `station_information` utilizables. Los 3 restantes
(`getafe`, `ontinyent`, `urdaibai`) devuelven **404** en el espejo citybik y no
aparecen en el catálogo oficial de sistemas GBFS (MobilityData `systems.csv`),
así que no hay feed alternativo conocido: esas ciudades quedan en el catálogo
(la app las sigue mostrando con su estado de error) pero **no acumulan
historial**. Ver §11.5 para el detalle de la auditoría.

### 11.1 Zaragoza (ciudad por defecto; APIs propietarias además de GBFS)

Extraídas de `AppConfiguration.kt`, `BiziApi.kt` e
`InstallationIdentityRepository.kt`:

| Uso | URL |
| --- | --- |
| Estaciones (API oficial del ayuntamiento) | `https://www.zaragoza.es/sede/servicio/urbanismo-infraestructuras/estacion-bicicleta.json` |
| Fallback estaciones | `https://api.citybik.es/v2/networks/bizi-zaragoza` |
| GBFS (oficial, v3.0) | `https://zaragoza.publicbikesystem.net/customer/gbfs/v3.0/gbfs.json` |
| Patrones de uso | `https://datosbizi.com/api/patterns?stationId={id}` (la app mantiene datosbizi.com para Zaragoza; el backend **no** acumula historial propio aquí, `usageHistoryEnabled = false`) |

### 11.2 Feeds GBFS de primera parte (16 ciudades)

El catálogo ya no depende del espejo citybik.es donde el operador publica GBFS
propio. Migración verificada por coordenadas (§11.5): mismas estaciones, ids
nuevos (ver §11.4).

**publicbikesystem.net v3.0** — `https://{sub}.publicbikesystem.net/customer/gbfs/v3.0/gbfs.json`:

```text
a_coruna→acoruna · barcelona→barcelona · donostia→sansebastian · madrid→madrid ·
rivas→rivas · valladolid→valladolid · zaragoza→zaragoza
```

**jCDecaux (cyclocity) v3** — `https://api.cyclocity.fr/contracts/{contract}/gbfs/v3/gbfs.json`:

```text
sevilla→seville · valencia→valence
```

**nextbike directo v2** — `https://gbfs.nextbike.net/maps/gbfs/v2/{id}/gbfs.json`:

```text
arteixo→nextbike_aa · bilbao→nextbike_bw · bizkaia→nextbike_bn ·
boadilla_del_monte→nextbike_by · las_palmas→nextbike_el · leon→nextbike_le ·
logrono→nextbike_ej · mislata→nextbike_ad · palma→nextbike_ea ·
santander→nextbike_ek · torrent→nextbike_en
```

**Otros operadores**:

```text
vitoria_gasteiz→https://gbfs.partners.fifteen.eu/gbfs/2.2/vitoria/en/gbfs.json
reus→https://gbfs.ganxeta.cat/v3/gbfs.json
```

### 11.3 Resto de ciudades: espejo citybik.es (29)

`https://api.citybik.es/gbfs/2/{network}/gbfs.json`. Solo para operadores sin
endpoint propio (3 de ellos con feed caído: `getafe`, `ontinyent`, `urdaibai`):

```text
albacete→albabici · alcazar_de_san_juan→bicialcazar · aller→bealler ·
aviles→aviles-en-bici · badajoz→biba · burgos→bicibur · caldas_de_reis→verdeciclos ·
campoo_de_enmedio→bikenmedio · castellon→bicicas · castro_urdiales→bicicastro ·
cenicientos→cenibici · elx→bicielx · ferrol→bici-ferrol-terra ·
fuerteventura→lovesharing-canary-islands-fuerteventura · getafe→gbici ·
gijon→gijon · girona→girocleta · gran_canaria→lovesharing-canary-islands-gran-canaria ·
ibiza→dott-ibiza · illa_de_arousa→biciarousa · lanzarote→lovesharing-canary-islands-lanzarote ·
murcia→dott-murcia · ontinyent→ontibici · pamplona→nbici ·
sant_vicent_del_raspeig→bicisanvi · tarragona→dott-tarragona ·
urdaibai→nextbike-urdaibai · vilagarcia_de_arousa→vaibike · villarcayo→vive-villarcayo
```

`bilbao` y `leon` se quedan en nextbike directo porque el feed municipal de
Bilbao (67 estaciones) y `nextbike_sl` (51) son **sistemas distintos** a los que
hoy ve el usuario (108 y 153 estaciones, 0 % de coincidencia por coordenadas).

### 11.4 Compatibilidad de formatos y de ids

- El parser (app y backend) acepta GBFS 2.x y 3.0:
  - descubrimiento en `data.<idioma>.feeds` (2.x) o `data.feeds` (3.0);
  - `num_bikes_available` (2.x) o `num_vehicles_available` (3.0);
  - `name`/`address` como texto plano o lista de `{text, language}` (se prefiere
    `es`, luego `en`, luego el primero);
  - `last_updated`/`last_reported` como epoch (2.x) o ISO-8601 (3.0).
- **E-bikes**: los feeds 3.0 usan ids de operador (`EFIT`, `BOOST`, `FIT`), no
  `ebike`, y su `count` puede incluir vehículos *overflow*. La app resuelve
  `vehicle_types.json` (`propulsion_type`) y acota las e-bikes al total de la
  estación; el backend aplica el mismo tope con heurística de id (no lee
  `vehicle_types`, no afecta a patrones ni alertas).
- **Los `station_id` cambian**: el espejo citybik reescribe los ids del operador
  con hashes (`Bicing 1 → 1c3b749e…`). Los feeds de primera parte usan los ids
  nativos (`1`, `1406`, `st_d4sl…`). Decisión tomada: **no** se migran los sitios
  guardados (favoritos, casa/trabajo, categorías, alertas) de las ciudades
  afectadas; hay que volver a guardarlos. Zaragoza no se ve afectada porque la
  app la resuelve con la API del ayuntamiento.

### 11.5 Verificación de feeds (auditoría del 28 oct 2026)

Auditoría de las 51 URLs de descubrimiento: 48 OK, 3 en 404. Total **5.039
estaciones** (coinciden `station_information` y `station_status` en todas las
ciudades que responden).

- **`ttl` declarado**: `0` en 28 feeds; el resto publica un `ttl` dinámico de
  entre 1 y 60 s (`sevilla`/`valencia` 1, `madrid` 10, `rivas` 16, `donostia` 18,
  `valladolid` 21, `a_coruna` 29 y 13 feeds nextbike/`reus`/`vitoria` en 60).
  Con `ttl = 0` no hay ninguna señal de cadencia: el
  `MIN_POLL_INTERVAL_SECONDS` es lo único que protege a los feeds. Para cobertura
  completa se recomienda **300 s** en producción (`COVERAGE_ENABLED=true`):
  ~580 req/h frente a ~2.900 req/h con el default de 60 s.
- **ETag/304**: solo `reus` (ganxeta) y `sevilla`/`valencia` (cyclocity) publican
  `ETag`, y los tres responden `304`. La petición condicional del cliente
  funciona ahí; en los otros 45 feeds no hay `ETag` y no se puede recortar
  tráfico.
- **`num_docks_available` ausente** en `ibiza` (28 estaciones), `murcia` (401) y
  `tarragona` (101). Esos feeds tampoco publican `capacity` en
  `station_information`, así que los anclajes no se pueden derivar: el ingestor
  los mapea a `-1`, no generan muestra y esas 530 estaciones **nunca tendrán
  patrones** (la UI muestra "No usage pattern data available"). Se descarta
  inventar `anchorsAvg`/`occupancyAvg` a partir de datos ausentes.
- **`capacity` ausente** (no lo usa el historial, pero afecta a cálculos de
  ocupación/alternativas) en `barcelona`, `elx`, `gijon`, `girona`,
  `valladolid`.
- **Feeds caídos**: `getafe` (`gbici`), `ontinyent` (`ontibici`) y `urdaibai`
  (`nextbike-urdaibai`) devuelven 404 en citybik; no existen en el catálogo
  oficial de GBFS ni responden feeds propios probados. Sin reemplazo conocido:
  se quedan en el catálogo sin historial y aparecerán como error en el poller.
- **Feeds oficiales**: migrados los 18 endpoints de primera parte que describen
  el mismo sistema (publicbikesystem v3.0, cyclocity, fifteen, ganxeta y los
  nextbike directos de `santander`, `palma`, `las_palmas`, `logrono`, `torrent`,
  `boadilla_del_monte`, `arteixo`), verificados por coordenadas (≥95 % de
  estaciones coincidentes, en la práctica 100 %). Quedan en citybik las 29
  ciudades sin endpoint propio: sus operadores no publican GBFS.
- **Rate limits**: ningún feed declara límites; se asume que el `ttl` (o su
  ausencia) es la única guía y que el presupuesto debe ser conservador.

## 12. Notas de decisión aún abiertas

- Si el display (mapa/nearby) se proxyficará también por el backend en el futuro o se
  mantiene client-side.
- Valores por defecto y límites (mín/máx) del umbral crítico y del radio de alternativa
  que el backend impone sobre la elección del usuario.
- Emisión de `AVAILABLE` (recuperación): notificación discreta, solo data push, o nada.
- ~~Sustituir los espejos de citybik.es por feeds GBFS oficiales donde existan~~:
  hecho (§11.2). Los ids de estación cambiaron y **no** se migran los sitios
  guardados (decisión tomada, §11.4).

## 13. Despliegue: Docker + Coolify

- El backend se empaqueta como **imagen Docker** (Ktor/JVM) y se despliega con un
  **docker-compose** pensado para **Coolify**.
- Requisitos del compose para Coolify:
  - **Sin build en el servidor**: Coolify construye desde el repo (Dockerfile del
    backend) o consume una imagen de registro; el compose solo declara el servicio.
  - **Configuración por entorno**: todo vía variables de entorno (puerto, JDBC/SQLite,
    JWKS de Auth0: `issuer`/`audience`, credenciales FCM, `.p8` de APNs vía secret),
    sin valores hardcodeados. Coolify inyecta el `.env`.
  - **Volúmenes persistentes** para las BD SQLite (principal + una por ciudad): si el
    despliegue es single-node, volumen local; evitar réplicas concurrentes escribiendo
    el mismo SQLite.
  - **Healthcheck** (`/health`) y puerto expuesto según lo que espere Coolify
    (por defecto escucha el puerto que declara el compose).
  - **Logs a stdout/stderr** (formato texto o JSON), sin ficheros de log.
- Fuera de alcance del compose: Auth0 (SaaS externo) y las APIs de ciudad (solo
  saliente). No hay más servicios que el propio backend; si en el futuro se añade
  Redis u otro estado compartido, entra como servicio adicional del mismo compose.

## 14. Historial de uso de estaciones (implementado en el backend)

El backend ofrece el equivalente a los "patrones de uso" de datosbizi.com para
todas las ciudades, agregando las muestras GBFS que ya recibe.

### 14.1 Diseño

- **Muestreo**: en cada poll exitoso (`CityIngestor.poll`), cada estación con
  contadores usables (`bikes`/`docks != -1`) aporta una muestra.
- **Almacenamiento**: tabla `station_hourly_usage` en la BD de cada ciudad,
  `WITHOUT ROWID` con PK `(station_id, day_of_week, hour)`; columnas
  `epoch_day`, `sample_count`, `sum_bikes`, `sum_docks`. Upsert batch en la
  misma transacción que `updateCache`.
- **Hora local**: los buckets usan la zona horaria de la ciudad (`City.timezoneId`;
  `Atlantic/Canary` para las ciudades canarias), no UTC.
- **Rotación**: al escribir un bucket cuyo `epoch_day` difiere del almacenado,
  se resetea (`sample_count = 1`, sumas = muestra actual). Cada bucket contiene
  siempre la ocurrencia más reciente de ese día de la semana → ventana rodante
  de 7 días sin jobs de purga ni fragmentación (todo son `UPDATE` in-place).
- **Lectura**: `toPatterns` ignora buckets con `epoch_day` fuera de la ventana
  (protege ante muestreo interrumpido) y agrupa `WEEKDAY` (lun–vie) y
  `WEEKEND` (sáb–dom). `occupancyAvg` se deriva en lectura
  (`bikesAvg / (bikesAvg + anchorsAvg)`).

### 14.2 API

- `GET /v1/cities/{cityId}/stations/{stationId}/patterns` — **público** (datos
  agregados no personales), devuelve el array con el contrato exacto de
  `StationHourlyPattern` de la app (`stationId`, `dayType`, `hour`, `bikesAvg`,
  `anchorsAvg`, `occupancyAvg`, `sampleCount`, camelCase). Responde **404**
  `unknown_city` si la ciudad no está en el catálogo y **404**
  `patterns_not_available` si `usageHistoryEnabled = false` (feed muerto o sin
  anclajes): la app trata ese 404 como "sin patrones" y **oculta la card**.
- **App (implementado)**: `DatosBiziApi` recibe `cityId`, y `RoutingDatosBiziApi`
  decide la fuente por ciudad: **Zaragoza → datosbizi.com**; resto de ciudades
  → backend **solo si `backendBaseUrl` está configurado** (modo anónimo/local en
  caso contrario, sin llamadas). El gate de UI pasa a ser
  `datosBiziApi.supports(cityId)`, no el flag estático `supportsUsagePatterns`
  del catálogo, de modo que no hace falta remote config para encenderlo.

### 14.3 Cobertura y coste

- **`COVERAGE_ENABLED`** (default `false`): con `false` solo se sondean ciudades
  con interés (comportamiento previo); con `true` se sondean las 51 ciudades
  del catálogo, necesario para acumular historial donde nadie está suscrito.
- **`MIN_POLL_INTERVAL_SECONDS`** (default 60): clamp inferior del intervalo
  efectivo por feed, sea cual sea el `ttl` que declare el espejo. Con
  `COVERAGE_ENABLED=true`, producción debe usar **300 s** (§11.5).
- **Resolución de los patrones**: con 300 s se acumulan ~12 muestras por hora y
  por estación; suficiente para medias estables sin disparar el tráfico.
- **Espacio** (medido sobre los feeds reales, oct 2026: 4.233 estaciones en las
  44 ciudades que acumulan historial; Bizkaia 1.079, Madrid 678, Barcelona 543):
  ~168 filas por estación × ~50 B ≈ **711.000 filas / ~36 MB en total**, tamaño
  estable desde la primera semana. Frente a muestras crudas (~9,5 M filas ≈
  430 MB) es ~10× menos.
- **Ciudades sin patrones**: `getafe`, `ontinyent` y `urdaibai` (feed 404) e
  `ibiza`, `murcia`, `tarragona` (feed sin `num_docks_available` ni `capacity`)
  están marcadas con `usageHistoryEnabled = false`: el endpoint responde 404
  `patterns_not_available` y la app no muestra la card en vez de dejarla vacía
  para siempre. No se inventan medias.
- Los patrones tardan ~1 semana en cubrir las 168 horas; `sampleCount` indica
  la madurez de cada media.
