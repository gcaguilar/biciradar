# Changelog

Notas de cada versión publicada. Las secciones **App** resumen lo que ve el
usuario (también se refleja en `ChangelogCatalog`, en las notas de Play y en las
notas de la App Store). Las secciones **Release** y **CI** documentan los cambios
internos de empaquetado, versionado y automatización que acompañan a la versión.

## 0.23.0

`versionName` 0.23.0 · Android `29570215` · Wear OS `29570216` · Apple
`29570215`.

### App

- **Modo viaje**: elegir una estación en el mapa también entra en modo viaje. Las
  rutinas y el bloque para elegir destino dejan de aparecer y una X en la tarjeta
  de estación permite volver a planificar. "Crear rutina desde este viaje" pasa a
  la tarjeta de estación, donde la estación ya está elegida.
- **Viaje**: la estación elegida se actualiza con cada refresco de disponibilidad,
  se puede cambiar la estación aunque ya haya destino, el selector de destino en
  el mapa es accesible desde el buscador y el aviso distingue una estación no
  disponible de una estación llena.
- **Rutinas**: se pueden gestionar también desde Perfil, sin tener que limpiar el
  viaje activo.
- **Buscador de destino**: limpiar el viaje reinicia la búsqueda, y borrar y
  volver a escribir el mismo texto lanza la búsqueda otra vez.
- **Vigilancia**: los avisos push cierran la sesión también en el reloj y los
  widgets; detener o limpiar un viaje solo termina la sesión de esa estación, sin
  cortar las rutinas que vigilan otras. Si se deniegan las notificaciones, se
  muestra el motivo en lugar de no hacer nada.
- **Cuenta**: Perfil permite eliminar la cuenta y sus datos (rutinas, avisos y
  dispositivos) y cierra la sesión, tal como exigen App Store y Google Play.

### Release

- Se elimina la variante `fdroid` de `androidApp` y `wearApp`, junto con su
  implementación de mapas OpenStreetMap (`osmdroid`), el workflow de
  publicación, los metadatos de F-Droid y las notas de envío. El flavor
  `playstore` se mantiene sin cambios.
- `VERSION` y `BUILD_NUMBER` suben a 0.23.0 y 2141. `tooling/project/bump_version.sh`
  deja Android e iOS en `29570215` y Wear OS en `29570216`.
- Notas de Play para `29570215` (móvil) y `29570216` (Wear OS) y notas de la
  App Store para iPhone y Apple Watch.

## 0.22.21

`versionName` 0.22.21 · Android `29570213` · Wear OS `29570214` · Apple
`29570213`.

### App

- **Inicio de sesión corregido en producción**: el AAB de Play apunta otra vez
  al tenant de Auth0 correcto (`dev-8nf57og7xa3urr1i`), que ya tiene
  registradas las callback URLs de Android e iOS.

### Release

- `VERSION` y `BUILD_NUMBER` suben a 0.22.21 y 2139.
- Los secretos `BICI_AUTH0_DOMAIN` y `BICI_AUTH0_CLIENT_ID_ANDROID` dejan de
  apuntar al tenant `dev-tbwmrj5wi0ecsgfy`; `BICI_AUTH0_AUDIENCE` vuelve a
  `https://api.datosbizi.com`.
- `publish-ios-store.yml` escribe backend, Auth0 y esquema en
  `apple/Config/LocalSecrets.xcconfig` y falla si faltan, en lugar de generar un
  archive sin login.

## 0.22.20

`versionName` 0.22.20 · Android `29570211` · Wear OS `29570212` · Apple
`29570211`.

### App

- **Inicio de sesión corregido**: el AAB de Play vuelve a incluir la
  configuración del backend y de Auth0, por lo que el login y la sincronización
  de rutinas y avisos funcionan en la versión publicada.

### Release

- `VERSION` y `BUILD_NUMBER` suben a 0.22.20 y 2137.
- El workflow de Play pasa `BICI_BACKEND_URL`, `BICI_AUTH0_DOMAIN`,
  `BICI_AUTH0_AUDIENCE` y `BICI_AUTH0_CLIENT_ID_ANDROID`, y falla antes de
  construir si alguno falta. `bundlePlaystoreRelease` también rechaza un AAB
  sin esa configuración. Corrige el build 0.22.19, que llegó a Play sin Auth0.

## 0.22.19

`versionName` 0.22.19 · Android `29570209` · Wear OS `29570210` · Apple
`29570209`.

### App

- **Cuenta y rutinas de vigilancia**: la vigilancia de estaciones pasa a
  requerir cuenta y admite rutinas programadas (días, hora y estaciones). La
  vigilancia arranca 10 minutos antes de la hora indicada y las rutinas se
  sincronizan con el backend, con estado `Sincronizado` o `Solo en este
  dispositivo` según haya sesión.
- **Inicio de sesión con Auth0** en Android, iPhone, Wear OS y Apple Watch,
  compartido con el backend de rutinas y avisos.
- **Avisos push**: las alertas de estaciones favoritas y de rutinas llegan como
  notificaciones push del backend, no solo como avisos locales, también con la
  app cerrada. Android crea el canal `biciradar_alerts` que esperan los
  mensajes FCM.
- **Modo viaje**: los favoritos se ofrecen como destino rápido, los bloques de
  destino y rutinas se ocultan una vez elegido el destino y, si la vigilancia no
  puede arrancar, se muestra el error en lugar de una cuenta atrás falsa.
- **Apple sobre Kotlin Swift export**: iPhone y Apple Watch consumen `Core` y
  `MobileUi` mediante Swift export, con `CURRENT_PROJECT_VERSION` y
  `MARKETING_VERSION` alineados con Android.
- **Android 17 (API 37/37.1)** en móvil y reloj, con Compose Multiplatform
  1.13.0-beta01, Kotlin 2.5.0-Beta2, Metro 1.5.0, Firebase 13.1.1 y el resto de
  dependencias actualizadas.
- **Correcciones**: los deep links de Android solo se resuelven para los hosts
  propios de la app, y la hora local en Apple se lee con `NSDateFormatter`.

### Release

- `tooling/project/bump_version.sh` calcula los `versionCode` a partir del mayor
  código ya presente en `androidApp`, `wearApp` y el proyecto Xcode (más uno),
  en lugar de confiar en `BUILD_NUMBER`, para no reutilizar nunca un código que
  Play ya haya aceptado. `BUILD_NUMBER` se recalcula desde el código resultante.
- El script usa `sed -i` portable (GNU y BSD) y actualiza también
  `apple/BiciRadar.xcodeproj/project.pbxproj`, que antes solo se sincronizaba a
  mano.
- `VERSION` y `BUILD_NUMBER` suben a 0.22.19 y 2135.

### CI

- `build.yml` se reorganiza: `paths-ignore` para documentación, metadatos de
  Play, Fastlane, landing y Garmin; `concurrency` que cancela ejecuciones
  antiguas solo en PRs; job `lint` con `ktlintCheckAll`; job `changes` que
  decide si hace falta validar watchOS; artefactos solo cuando falla un job y
  con retención de 14 días.
- El aprovisionamiento de JDK, Android SDK, Gradle y permisos de scripts se
  extrae a la acción compuesta `.github/actions/setup-build`, reutilizada por
  los workflows de build y publicación.
- Los bundles de Play solo se ensamblan en `main`; en PRs se validan tests de
  Android, Wear y shared.
- `publish-play.yml` añade el input `target` (`all`, `android`, `wear`), separa
  `closed_testing` de `production`, soporta `release_status`
  (`completed`/`draft`/`staged`) y valida la combinación de entradas en un job
  `prepare` que publica la matriz y escribe las pistas resueltas en el resumen.
- `publish-ios-store.yml` y `publish-fdroid.yml` adoptan la acción compartida y
  Xcode 26.6 queda fijado en los jobs de Apple.

## 0.22.18

Ver `git show f4867892` y las notas publicadas en `fastlane/metadata`.
