# BiciRadar

Aplicacion multiplataforma para consultar disponibilidad de bicicletas publicas en ciudades de Espana.

Este README esta orientado a desarrolladores: arquitectura, setup local, comandos y CI/CD.
La documentacion orientada a usuarios finales vive en `docs/wiki/` (formato GitHub Wiki).

## Stack tecnico

- Kotlin Multiplatform para dominio, datos y contratos compartidos.
- Compose Multiplatform para UI movil compartida (Android + iOS).
- Compose para Wear OS.
- SwiftUI + App Intents para iOS y watchOS.
- Metro DI para inyeccion de dependencias en compilacion.
- Integracion de mapas y geolocalizacion en Android, iOS y watchOS.

## Estructura del repositorio

- `shared/core`: modelos, repositorios, contratos de plataforma, grafo Metro y cliente Bizi.
- `shared/mobile-ui`: UI compartida para apps moviles.
- `androidApp`: aplicacion Android principal.
- `wearApp`: aplicacion Wear OS.
- `apple`: shell SwiftUI/App Intents para iOS y watchOS.
- `docs`: planes tecnicos y documentacion interna.
- `docs/wiki`: documentacion funcional orientada a usuario final.

## Primer arranque local

1. Instala JDK y toolchains moviles (Android SDK y Xcode en macOS para targets Apple).
2. Clona el repositorio y abre la raiz del proyecto.
3. Ejecuta una compilacion basica para verificar toolchain.

```bash
./gradlew build
```

## Comandos de desarrollo

### Build y tests

```bash
./gradlew :shared:core:jvmTest
./gradlew :shared:mobile-ui:compileKotlinIosSimulatorArm64
./gradlew :androidApp:compileDebugKotlinAndroid
./gradlew :wearApp:compileDebugKotlinAndroid
./gradlew build
```

### Smokes rapidos

Script unificado:

```bash
./tooling/project/run_smoke.sh
```

Ejemplos:

```bash
./tooling/project/run_smoke.sh android-assistant emulator-5554
./tooling/project/run_smoke.sh ios "platform=iOS Simulator,name=iPhone 17 Pro,OS=26.2"
./tooling/project/run_smoke.sh watchos "platform=watchOS Simulator,name=Apple Watch Series 11 (46mm),OS=26.2"
```

## Configuracion local

- `GOOGLE_MAPS_API_KEY`: opcional en local; habilita tiles reales de mapas en Android. Tambien lo usan workflows iOS en CI.
- Android Crashlytics se habilita automaticamente si existe `androidApp/google-services.json`.
- Wear OS Crashlytics se habilita automaticamente si existe `wearApp/google-services.json`.
- iOS Crashlytics se habilita automaticamente si existe `apple/iosApp/GoogleService-Info.plist`.

### Firebase/Crashlytics

1. Registra apps en Firebase:
   - Android: `com.gcaguilar.biciradar`
   - Wear OS: `com.gcaguilar.biciradar.wear`
   - iOS: `com.gcaguilar.biciradar.ios`
2. Coloca los archivos de configuracion:
   - `androidApp/google-services.json`
   - `wearApp/google-services.json`
   - `apple/iosApp/GoogleService-Info.plist`
3. Configura clave local de mapas iOS en `apple/Config/LocalSecrets.xcconfig` con `GOOGLE_MAPS_IOS_API_KEY`.
4. Recompila.

El repositorio ya contempla fallback seguro cuando faltan archivos de Firebase, tanto en Android/Wear como en iOS.

## CI/CD

Workflow principal: `.github/workflows/build.yml`.

Se ejecuta en `push` a `main`, `pull_request` y ejecucion manual, con jobs en paralelo:

- `android`: tests JVM compartidos, tests unitarios Android y build de APKs debug (phone + wear).
- `ios`: tests iPhone y artefacto `.app` para simulador.
- `watchos`: tests Apple Watch y artefacto `.app` para simulador.

Artefactos publicados:

- `android-debug-apks`
- `ios-simulator-app`
- `watchos-simulator-app`
- `ios-device-ipa` (cuando hay secretos de firma Apple)

### Distribucion opcional con Firebase App Distribution

El mismo workflow distribuye builds internas si estan definidos los secretos/variables requeridos para Firebase y firma Apple.

Revisa y gestiona estos valores en GitHub Secrets/Variables:

- Firebase: `FIREBASE_SERVICE_ACCOUNT_JSON`, `FIREBASE_ANDROID_APP_ID`, `FIREBASE_WEAR_ANDROID_APP_ID`, `FIREBASE_IOS_APP_ID`.
- Apple signing: `APPLE_TEAM_ID`, `APPLE_SIGNING_CERTIFICATE_P12_BASE64`, `APPLE_SIGNING_CERTIFICATE_PASSWORD`, `APPLE_PROVISIONING_PROFILE_BASE64`, `APPLE_KEYCHAIN_PASSWORD`.
- App Store Connect: `APP_STORE_CONNECT_ISSUER_ID`, `APP_STORE_CONNECT_KEY_ID`, `APP_STORE_CONNECT_API_KEY_P8`.
- Maps/config review: `GOOGLE_MAPS_API_KEY`, `APP_REVIEW_CONTACT_FIRST_NAME`, `APP_REVIEW_CONTACT_LAST_NAME`, `APP_REVIEW_CONTACT_EMAIL`, `APP_REVIEW_CONTACT_PHONE`, `APP_REVIEW_NOTES`.

Workflow de publicacion App Store: `.github/workflows/publish-ios-store.yml` (manual).

## Release

Checklist de build y release en `RELEASE.md`.

## Wiki de usuario

Contenido orientado a personas usuarias:

- `docs/wiki/Home.md`: portada de la wiki (recomendada).
- `docs/wiki/GUIA_USUARIO.md`: guia consolidada en una sola pagina.
