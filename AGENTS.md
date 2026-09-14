# BiciRadar Agent Guide

## Mission

BiciRadar is a public-bike availability client for Android, Wear OS, iOS,
watchOS, and an experimental desktop target. It reads official bike-sharing
data for multiple Spanish cities and helps users find nearby stations, inspect
bike and dock availability, save favorite stations, receive availability
alerts, and surface quick status through widgets, watch UI, tiles,
complications, and shortcuts.

The repository is a Kotlin Multiplatform monorepo. Domain and data behavior is
shared where practical; platform shells own capabilities that require Android,
Wear OS, SwiftUI, App Intents, or native maps and location APIs.

## Architecture

The main dependency direction is:

```text
platform apps -> shared/mobile-ui -> shared/core
platform apps --------------------> shared/core
```

- `shared/core`: shared models, repositories, networking, serialization,
  persistence, platform contracts, and Metro dependency-injection graph. It is
  exported to Apple code as `BiziSharedCore`.
- `shared/mobile-ui`: Compose Multiplatform screens and presentation logic
  shared by the phone apps. It is exported to Apple code as `BiziMobileUi`.
- `shared/test-utils`: reusable test fixtures and helpers.
- `androidApp`: Android phone shell and Android-only integrations. It has
  `playstore` and `fdroid` product flavors.
- `wearApp`: Compose for Wear OS, including watch-specific surfaces. It also
  contains `playstore` and `fdroid` source sets, although Wear is not currently
  part of the F-Droid release submission.
- `apple`: XcodeGen project definition plus SwiftUI/App Intents code for iOS,
  watchOS, widgets, shortcuts, and Apple-specific integrations.
- `desktopApp`: desktop target. Treat it as experimental and inspect its
  current Gradle configuration before assuming feature parity.
- `docs`: technical, release, and packaging documentation.
- `docs/wiki`: end-user documentation in GitHub Wiki format.
- `tooling/project`: repository-specific smoke, release, hook, icon, and
  versioning helpers.
- `tooling/generic-mobile-ci`: reusable CI/release helpers.
- `landing` and `eminent-ellipse`: independent Node-based work areas. Read each
  directory's manifest and documentation before changing it; do not apply the
  Gradle workflow to them.
- `fastlane`, `metadata`, `VERSION`, and `BUILD_NUMBER`: store-distribution and
  release inputs. Do not update them casually.

Core technologies include Kotlin Multiplatform, Compose Multiplatform, Compose
for Wear OS, SwiftUI, App Intents, Ktor, Kotlin serialization, SQLDelight, and
Metro DI.

## Before Editing

1. Read the nearest build file and the relevant source sets. Similar behavior
   may have separate implementations in `commonMain`, `androidMain`, Apple
   source sets, Android flavors, Swift, and watch code.
2. Search for all callers and platform implementations of any shared contract
   you change. A shared interface change is incomplete until every target
   compiles.
3. Check for a more specific `AGENTS.md` in the directory being edited and
   follow it if present.
4. Preserve existing user changes. Do not reformat or revert unrelated files.
5. Prefer a focused change over cross-platform cleanup that is not required by
   the task.

## Build and Validation

Use the Gradle wrapper from the repository root. Start with the smallest check
that exercises the changed code, then broaden validation when the change
crosses modules or platform boundaries.

```bash
./gradlew :shared:core:jvmTest
./gradlew :shared:mobile-ui:compileKotlinIosSimulatorArm64
./gradlew :androidApp:compileDebugKotlinAndroid
./gradlew :wearApp:compileDebugKotlinAndroid
./gradlew ktlintCheckAll
./gradlew build
```

Apply Kotlin formatting only when needed:

```bash
./gradlew ktlintFormatAll
```

Review the formatter's diff afterward; do not include unrelated formatting
churn.

For device and simulator smoke tests, use the repository helper:

```bash
./tooling/project/run_smoke.sh
./tooling/project/run_smoke.sh android-assistant emulator-5554
./tooling/project/run_smoke.sh ios "platform=iOS Simulator,name=iPhone 17 Pro,OS=26.2"
./tooling/project/run_smoke.sh watchos "platform=watchOS Simulator,name=Apple Watch Series 11 (46mm),OS=26.2"
```

Smoke-test destinations are examples and depend on locally installed runtimes.
Apple builds and smoke tests require macOS, Xcode, and the expected simulator.
If a platform cannot be tested in the current environment, run all portable
checks and report the unverified target explicitly.

For the current Android F-Droid submission flow:

```bash
./gradlew :androidApp:testFdroidDebugUnitTest
./gradlew :androidApp:assembleFdroidDebug
./gradlew :androidApp:assembleFdroidRelease
bash tooling/project/check_fdroid_submission.sh
```

Validation expectations by change type:

| Change area | Minimum useful validation |
| --- | --- |
| Shared core logic | `:shared:core:jvmTest` plus affected target compilation |
| Shared Compose UI | relevant shared UI compilation plus one consuming app build |
| Android phone | affected flavor unit tests or compilation |
| Wear OS | Wear compilation and a watch smoke test when available |
| Apple bridge/contracts | iOS and watchOS framework/app compilation on macOS |
| Formatting/build logic | `ktlintCheckAll` and the affected Gradle task |
| F-Droid code | F-Droid test, assembly, and submission checker |
| Release/CI | targeted workflow or helper validation and documentation review |

Add or update tests for behavior changes. Favor deterministic tests around
repository mapping, availability state, persistence, and presentation logic;
reserve device tests for actual platform integration.

## Cross-Platform Rules

- Put portable business rules, data transformations, and contracts in shared
  code. Keep permissions, notifications, maps, widgets, tiles, complications,
  shortcuts, and lifecycle wiring behind platform boundaries.
- Do not duplicate shared business logic in Kotlin platform source sets or
  Swift merely to avoid updating a shared contract.
- Preserve cancellation and lifecycle behavior in coroutines and flows. Avoid
  unscoped work and do not block UI threads with network or database calls.
- Treat bike/station data as live and fallible. Preserve explicit loading,
  empty, stale, offline, and error states rather than converting failures into
  plausible-looking availability.
- Keep identifiers and availability semantics stable across network models,
  persistence, shared UI, widgets, watches, and alerts.
- When changing user-visible behavior, check phone, watch, widget/tile,
  complication, shortcut, and notification surfaces for consistency.
- Update `docs/wiki` when a user-facing workflow changes. Update technical docs
  when architecture, setup, build, or release behavior changes.

## Flavor and Service Boundaries

- `playstore` may use Firebase/Crashlytics, Google Maps, Google Play Services,
  wearable sync, and Garmin integrations where configured.
- `fdroid` must remain free of Firebase, Google Play Services, Google Maps SDK,
  and Garmin Connect IQ runtime dependencies. Android F-Droid maps use
  OpenStreetMap through osmdroid.
- Keep Play-only imports and dependencies inside Play-specific source sets.
  Never make common or F-Droid code reference a class supplied only by a Play
  dependency.
- The Android F-Droid application ID is
  `com.gcaguilar.biciradar.fdroid`.
- Do not assume secrets or vendor configuration files exist. Android and Wear
  must retain their safe behavior without `google-services.json`; Apple must
  retain its fallback without `GoogleService-Info.plist`; local maps keys are
  optional.
- Never commit credentials, signing material, API keys, service-account JSON,
  provisioning profiles, or generated secret configuration.

## Apple Project Rules

- Read `apple/README.md` and `apple/project.yml` before structural Apple
  changes.
- The checked-in Xcode project may be generated from `apple/project.yml`.
  Keep the specification and project consistent; do not hand-edit generated
  project data without understanding the regeneration path.
- Changes to exported Kotlin declarations can affect Swift names and framework
  consumers even when JVM tests pass. Compile the relevant Apple framework and
  search Swift call sites.
- Preserve App Group, widget, watch connectivity, App Intent, entitlement, and
  bundle-identifier relationships when touching Apple targets.

## CI and Releases

The main workflow is `.github/workflows/build.yml`. It validates Android,
iPhone, and Apple Watch in parallel and publishes debug/simulator artifacts;
signed Apple artifacts and Firebase distribution are conditional on secrets.

Before changing CI or release behavior, read:

- `README.md`
- `RELEASE.md`
- `docs/fdroid/README.md`
- `apple/README.md`
- `tooling/README.md`
- the affected workflow and helper scripts

Keep local commands aligned with CI. Do not weaken a check simply because a
runner lacks optional secrets. Release changes may need coordinated updates to
store metadata, Fastlane configuration, `VERSION`, `BUILD_NUMBER`, Gradle
versioning, and Apple project settings; follow `RELEASE.md` as the authority.

## Completion Checklist

- The change lives in the correct shared, platform, or flavor-specific layer.
- All affected implementations and call sites were updated.
- Targeted tests or compilation passed.
- Ktlint passed for Kotlin changes.
- F-Droid isolation still holds when relevant.
- No secrets, local SDK paths, signing files, or generated build output were
  added.
- User and technical documentation were updated when behavior or workflow
  changed.
- The final report lists commands run, results, and any platform not verified.
