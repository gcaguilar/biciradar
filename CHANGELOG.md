# Changelog

Notas de cada versión publicada. Las secciones **App** resumen lo que ve el
usuario (también se refleja en `ChangelogCatalog`, en las notas de Play y en las
notas de la App Store). Las secciones **Release** y **CI** documentan los cambios
internos de empaquetado, versionado y automatización que acompañan a la versión.

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
