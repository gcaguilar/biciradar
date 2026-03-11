# Apple shell

This directory contains the SwiftUI/App Intents base for iPhone and Apple Watch.

- `shared/core` generates the `BiziSharedCore` framework.
- `shared/mobile-ui` generates the `BiziMobileUi` framework.
- `shared/mobile-ui` exports `MainViewController()` to mount the Compose UI from Swift.
- The Xcode project that consumes those frameworks still needs to be created or imported; `xcodegen` is not installed in this environment.

The intended setup is:

- iPhone: use `BiziMobileUi` for the shared Compose UI and `FavoritesSyncBridge` for the WatchConnectivity bridge.
- Apple Watch: use `BiziSharedCore` for domain/data and native SwiftUI for the circular UI.
- Apple Watch: `WatchFavoritesSyncBridge` prepares favorite reception from the iPhone.
- Siri/Shortcuts: use the App Intents and App Shortcuts defined here.
