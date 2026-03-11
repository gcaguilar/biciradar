# Bizi Zaragoza

Scaffold greenfield para `com.gcaguilar.bizizaragoza` con:

- Kotlin Multiplatform para dominio, datos y contratos shared.
- Compose Multiplatform para móvil Android + iOS.
- Compose for Wear OS para Wear.
- SwiftUI + App Intents shell para iPhone/Apple Watch.
- Metro DI como contenedor compile-time.
- Proxy Gemini opcional con Ktor.
- Ubicación real en Android, iOS y watchOS con fallback a Zaragoza centro si no hay permiso o no hay fix.
- Sync de favoritos entre Android móvil y Wear OS con Data Layer.
- Mapa embebido en Compose móvil con wrapper multiplataforma.
- Feed oficial del Ayuntamiento de Zaragoza con fallback a CityBikes.

## Módulos

- `shared/core`: modelos, repositorios, contratos de plataforma, Metro graph y cliente Bizi.
- `shared/mobile-ui`: UI Compose compartida para móvil.
- `androidApp`: app Android principal.
- `wearApp`: app Wear OS.
- `backend/gemini-proxy`: backend mínimo para Gemini.
- `apple/`: fuentes SwiftUI/App Intents base para iOS/watchOS.

## Comandos

```bash
./gradlew :shared:core:jvmTest
./gradlew :backend:gemini-proxy:test
gradle :shared:mobile-ui:compileKotlinIosSimulatorArm64 :androidApp:compileDebugKotlinAndroid :wearApp:compileDebugKotlinAndroid
./gradlew build
```

## Gemini opcional

El proxy usa:

- `GEMINI_API_KEY`
- `GEMINI_MODEL` opcional, por defecto `gemini-2.5-flash`
- `BIZI_GEMINI_PROXY_BASE_URL` opcional para Android/Wear
- `GOOGLE_MAPS_API_KEY` opcional para habilitar tiles reales en Android

El endpoint expuesto es `POST /api/v1/gemini/prompt`.

Si no defines `BIZI_GEMINI_PROXY_BASE_URL`, la app sigue funcionando sin Gemini y muestra solo asistentes/atajos directos.

## Release

El checklist de build y publicación está en `RELEASE.md`.

## Apple

El proyecto Xcode generado está en `apple/BiziZaragoza.xcodeproj`. La base SwiftUI/App Intents está en `apple/` y los frameworks KMP consumidos son:

- `BiziSharedCore`
- `BiziMobileUi`
- `BiziMobileUi` exporta `MainViewController()` para integrarlo desde SwiftUI/UIKit
