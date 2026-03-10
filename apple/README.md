# Apple shell

Este directorio contiene la base SwiftUI/App Intents para iPhone y Apple Watch.

- `shared/core` genera el framework `BiziSharedCore`.
- `shared/mobile-ui` genera el framework `BiziMobileUi`.
- `shared/mobile-ui` exporta `MainViewController()` para montar la UI Compose desde Swift.
- Falta crear o importar el proyecto Xcode que consuma esos frameworks; `xcodegen` no está instalado en este entorno.

La intención es:

- iPhone: usar `BiziMobileUi` para la UI Compose compartida y `FavoritesSyncBridge` para el enlace con WatchConnectivity.
- Apple Watch: usar `BiziSharedCore` para dominio/datos y SwiftUI nativo para la UI circular.
- Apple Watch: `WatchFavoritesSyncBridge` deja preparada la recepción de favoritas desde el iPhone.
- Siri/Shortcuts: usar App Intents y App Shortcuts definidos aquí.
