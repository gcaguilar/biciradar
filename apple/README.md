# Apple shell

This directory contains the SwiftUI/App Intents base for iPhone and Apple Watch.

- Kotlin is consumed through Swift export (no Objective-C framework). `shared/ios-export`
  exposes the `Core` and `MobileUi` Swift modules to the iPhone app and
  `shared/watch-export` exposes `Core` to the Apple Watch app. Xcode runs
  `./gradlew :shared:<ios|watch>-export:embedSwiftExportForXcode` in a pre-build script and links
  `libShared.a` / `libSharedWatch.a`. Swift export requires iOS 18 / watchOS 11.
- `shared/mobile-ui` exports `MainViewControllerWrapper(...)` to mount the Compose UI from Swift.
- `iosWidgets` contains the WidgetKit / ActivityKit extension sources for iPhone surfaces.
- `iosAppTests` and `watchAppTests` cover launch requests, shortcuts and watch-specific surface formatting.
- The checked-in Xcode project may need regeneration from `project.yml`; `xcodegen` is not installed in this environment.

The intended setup is:

- iPhone: use the `MobileUi` module for the shared Compose UI and `FavoritesSyncBridge` for the WatchConnectivity bridge.
- iPhone widgets/live activities: use `BiziSurfaceStore` + `SurfaceMonitoringActivityController` with the App Group container.
- Apple Watch: use the `Core` module for domain/data and native SwiftUI for the circular UI.
- Apple Watch: `WatchFavoritesSyncBridge` prepares favorite/home/work reception from the iPhone.
- Siri/Shortcuts: use the App Intents and App Shortcuts defined here.

## Backend, Auth0 and push configuration

Both platforms are functional without configuration (anonymous/local mode). To
enable login, sync and push you need to fill the same values on the app and on
the backend.

### 1. Auth0 dashboard

- Application type **Native** → copy the **Domain** and **Client ID**.
- Create an **API** → its **Identifier** is the **Audience** (must produce a JWT
  access token).
- Register the callbacks/logouts:
  - iOS: `biciradar://TU_DOMAIN/ios/com.gcaguilar.biciradar.ios/callback`
  - Android: `biciradar://TU_DOMAIN/android/com.gcaguilar.biciradar/callback`
    and `biciradar://TU_DOMAIN/android/com.gcaguilar.biciradar.fdroid/callback`
- Grant types: Authorization Code + Refresh Token (rotation on).
- Scope: `openid profile email offline_access`.

### 2. Backend (`backend/.env`)

```dotenv
AUTH0_ISSUER=https://TU_DOMAIN/
AUTH0_AUDIENCE=TU_AUDIENCE
AUTH_DISABLED=false
# push (FCM)
PUSH_ENABLED=true
FCM_PROJECT_ID=tu-proyecto-firebase
FCM_SERVICE_ACCOUNT_PATH=/ruta/service-account.json
```

### 3. iPhone / Apple Watch (`apple/Config/LocalSecrets.xcconfig`, git-ignored)

```xcconfig
// OJO: en xcconfig `//` inicia un comentario. Usa el truco `$()` para URLs.
BICI_BACKEND_URL = https:/$()/api.tu-servidor.com
BICI_AUTH0_DOMAIN = dev-xxxx.us.auth0.com
BICI_AUTH0_CLIENT_ID = TU_CLIENT_ID
BICI_AUTH0_AUDIENCE = https:/$()/api.biciradar.app
BICI_AUTH0_SCHEME = biciradar
```

In Debug you can instead set `BICI_*` environment variables in the Xcode scheme,
which avoids the `//` escaping. Then run `xcodegen generate` from `apple/` after
editing `project.yml` (the checked-in project is generated).

### 4. Push notifications (APNs + FCM)

Two different credentials are involved and they do not live in the same place:

- **APNs auth key (`.p8`)** — a *server* credential. Upload it in the Firebase
  console (Project settings → Cloud Messaging → Apple app configuration) with
  its Key ID and the Team ID. The backend never sees it, and it must never be a
  GitHub secret or a committed file.
- **Provisioning profile** — the app needs the **Push Notifications** capability
  enabled on the App ID `com.gcaguilar.biciradar.ios`, and the App Store profile
  regenerated afterwards. A profile created before the capability was enabled has
  no `aps-environment` entitlement, so TestFlight builds cannot register a token.

`aps-environment` is not hardcoded: the entitlements file expands
`$(APS_ENVIRONMENT)`, which is `development` in Debug and `production` in
Release (`project.yml`). That lets one entitlements file sign both.

After enabling the capability, update the `APPLE_PROVISIONING_PROFILE_BASE64`
secret with the new profile. CI fails early if the profile lacks the entitlement
(`Verify push entitlement in the iOS profile`), instead of producing an app that
silently receives nothing.

### 5. Android (`~/.gradle/gradle.properties` or env vars)

```properties
bizi.backendBaseUrl=https://api.tu-servidor.com
bizi.auth0Domain=dev-xxxx.us.auth0.com
bizi.auth0ClientId=TU_CLIENT_ID
bizi.auth0Audience=https://api.biciradar.app
```

Env equivalents: `BICI_BACKEND_URL`, `BICI_AUTH0_DOMAIN`, `BICI_AUTH0_CLIENT_ID`,
`BICI_AUTH0_AUDIENCE`.

In CI the Play Store release reads them from the repository secrets
`BICI_BACKEND_URL`, `BICI_AUTH0_DOMAIN`, `BICI_AUTH0_AUDIENCE`, and
`BICI_AUTH0_CLIENT_ID_ANDROID`. `:androidApp:bundlePlaystoreRelease` fails when
any of them is empty, so a production AAB cannot silently ship without sign-in.

### 6. Local smoke testing without Auth0

If the backend runs with `AUTH_DISABLED=true` and the app has a backend URL but
no Auth0 values, Debug builds wire `DevAuthProvider` so routines/subscriptions
sync against the local backend (device registration is skipped because there is
no push token). Never ship a release with this state.

