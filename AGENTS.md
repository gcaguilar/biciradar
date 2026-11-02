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
  exported to Apple code through Kotlin Swift export as the `Core` module.
- `shared/mobile-ui`: Compose Multiplatform screens and presentation logic
  shared by the phone apps. It is exported to Apple code through Kotlin Swift
  export as the `MobileUi` module.
- `shared/ios-export` and `shared/watch-export`: code-less umbrella modules that
  run Swift export for the iPhone (`Core` + `MobileUi`) and the Apple Watch
  (`Core` only). Xcode builds them through `embedSwiftExportForXcode`.
- `shared/test-utils`: reusable test fixtures and helpers.
- `androidApp`: Android phone shell and Android-only integrations. Its only
  product flavor is `playstore`.
- `androidApp/.../appfunctions`: Android 16+ AppFunctions surface. KSP generates
  the service and schema; the functions only orchestrate existing shared use
  cases through `BiziAppGraph`.
- `wearApp`: Compose for Wear OS, including watch-specific surfaces. Its only
  product flavor is `playstore`.
- `apple`: XcodeGen project definition plus SwiftUI/App Intents code for iOS,
  watchOS, widgets, shortcuts, and Apple-specific integrations.
- `desktopApp`: desktop target. Treat it as experimental and inspect its
  current Gradle configuration before assuming feature parity.
- `backend`: standalone Kotlin + Ktor push-monitoring backend. It is no longer
  part of this monorepo: it lives in its own private repository
  (`git@github.com:gcaguilar/biciradar-back.git`) with its own Gradle build and
  wrapper. A local `backend/` checkout is git-ignored here. See that repository's
  `README.md`; the plan lives in `backend_plan.md`. Do not apply the app Gradle
  workflow to it.
- `docs`: technical, release, and packaging documentation.
- `docs/wiki`: end-user documentation in GitHub Wiki format.
- `tooling/project`: repository-specific smoke, release, hook, icon, and
  versioning helpers.
- `tooling/generic-mobile-ci`: reusable CI/release helpers.
- `landing` and `eminent-ellipse`: independent Node-based work areas. Read each
  directory's manifest and documentation before changing it; do not apply the
  Gradle workflow to them.
- `fastlane`, `VERSION`, and `BUILD_NUMBER`: store-distribution and release
  inputs. Do not update them casually.

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
./gradlew :androidApp:compilePlaystoreDebugKotlin
./gradlew :wearApp:compilePlaystoreDebugKotlin
./gradlew ktlintCheckAll
./gradlew build
```

Android and Wear tasks are flavor-qualified because `playstore` is the only
flavor: compilation is `:androidApp:compilePlaystoreDebugKotlin` and unit tests
are `:androidApp:testPlaystoreDebugUnitTest` (same pattern for `:wearApp:`).
CI installs Temurin JDK 21 through `.github/actions/setup-build`; match that
version locally to reproduce a CI failure.

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
./tooling/project/run_smoke.sh ios
./tooling/project/run_smoke.sh watchos
```

For Apple targets the destination argument is optional: the script falls back to
`tooling/generic-mobile-ci/resolve_xcode_destination.sh`, which lists the
available simulators and picks the newest runtime and preferred device. Only
pass an explicit destination when you need to pin one:

```bash
./tooling/project/run_smoke.sh ios "platform=iOS Simulator,name=iPhone 18 Pro,OS=27.0"
./tooling/project/run_smoke.sh watchos "platform=watchOS Simulator,name=Apple Watch Series 12 (46mm),OS=27.0"
```

Take names from `xcrun simctl list devices available` on the machine you are
using. The CI image (`xcode-27`) ships iOS 27 and watchOS 27 simulators, so
device names from older Xcode versions (for example `iPhone 17 Pro`,
`OS=26.2`) do not exist there.

Smoke-test destinations are examples and depend on locally installed runtimes.
Apple builds and smoke tests require macOS, Xcode, and the expected simulator.
If a platform cannot be tested in the current environment, run all portable
checks and report the unverified target explicitly.

Validation expectations by change type:

| Change area | Minimum useful validation |
| --- | --- |
| Shared core logic | `:shared:core:jvmTest` plus affected target compilation |
| Shared Compose UI | relevant shared UI compilation plus one consuming app build |
| Android phone | `:androidApp:testPlaystoreDebugUnitTest` or `:androidApp:compilePlaystoreDebugKotlin` |
| Wear OS | `:wearApp:compilePlaystoreDebugKotlin` and a watch smoke test when available |
| Apple bridge/contracts | iOS and watchOS framework/app compilation on macOS |
| Formatting/build logic | `ktlintCheckAll` and the affected Gradle task |
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

- `playstore` uses Firebase/Crashlytics, Google Maps, Google Play Services,
  and wearable sync.
- Keep Play-only imports and dependencies inside the `playstore` source set.
  Never make common code reference a class supplied only by a Play dependency.
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

The workflows live in `.github/workflows`:

- `build.yml` is the main validation pipeline. It runs Android unit tests (plus
  a signed Play Store build on `main`), builds iOS tests on pull requests and
  runs them on `main`, and runs watchOS tests when Apple, shared, Wear, Gradle,
  or workflow inputs change (the `changes` job decides). Failed runs upload
  test reports as artifacts; no debug or simulator builds are published.
- `publish-play.yml` (manual dispatch) builds signed phone and Wear AABs,
  uploads them as artifacts, and publishes them to Play.
- `publish-ios-store.yml` (manual dispatch) builds, signs, and uploads the IPA
  to App Store Connect, keeping the IPA as an artifact.
- `deploy-landing.yml` builds `landing/` and deploys it to GitHub Pages.

Shared toolchain setup for the Gradle jobs lives in `.github/actions/setup-build`
(Temurin JDK, Android SDK, Gradle caching). Change it there instead of
duplicating steps per job.

Runner policy:

| Runner | Use it for |
| --- | --- |
| `ubuntu-slim` | Lightweight jobs only: diffing, shell/`jq` validation, the landing Node build, and the Pages deploy. It has 1 CPU, 5 GB RAM, an unprivileged container (no Docker-in-Docker, no privileged mounts) and a **hard 15-minute job limit**, so keep `timeout-minutes` at 10 or below. |
| `ubuntu-24.04` | Anything that runs Gradle, needs the Android SDK, or can exceed ~10 minutes: Android tests and release build, `ktlintCheckAll`, Play publishing. |
| `xcode-27` | Every Apple job (iOS, watchOS, App Store publishing). It deliberately uses the image's default Xcode instead of pinning one with `xcode-select`, so the compiler changes when GitHub updates the image; log it with `xcodebuild -version` rather than pinning it. |

Do not move a Gradle or Android job to `ubuntu-slim` to save capacity: the
15-minute cap and 5 GB of RAM make those builds fail, not merely slower. Apple
DerivedData cache keys include the Xcode generation (`xcode27`) so artifacts
from an older Xcode image are never reused.

Before changing CI or release behavior, read:

- `README.md`
- `RELEASE.md`
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
- Play-only code stays inside the `playstore` source set.
- New or changed CI jobs follow the runner policy in "CI and Releases".
- No secrets, local SDK paths, signing files, or generated build output were
  added.
- User and technical documentation were updated when behavior or workflow
  changed.
- The final report lists commands run, results, and any platform not verified.
