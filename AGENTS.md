# BiciRadar Agent Context

## Project overview

BiciRadar is a multiplatform app for checking public bike availability across Spanish cities. The repository mixes shared Kotlin Multiplatform business logic with native shells for Apple platforms and app-specific Android/Wear integrations.

Primary stack:

- Kotlin Multiplatform for domain, repositories, contracts, DI, networking, and persistence.
- Compose Multiplatform for shared mobile UI.
- Native SwiftUI/App Intents for iPhone and watchOS shells.
- Compose for Wear OS.
- SQLDelight, Ktor, Kotlin serialization, and Metro DI.

## Repository map

- `shared/core`: core domain layer, repositories, platform contracts, SQLDelight database, and generated Apple frameworks as `BiziSharedCore`.
- `shared/mobile-ui`: shared Compose UI for mobile apps, exported to Apple as `BiziMobileUi`.
- `shared/test-utils`: common test helpers.
- `androidApp`: Android phone app. Has `playstore` and `fdroid` flavors.
- `wearApp`: Wear OS app. Has `playstore` and `fdroid` flavors.
- `apple`: SwiftUI/App Intents shell for iOS and watchOS, widgets, shortcuts, and watch-specific UI.
- `desktopApp`: desktop target present in Gradle settings; inspect before changing because it is not covered in the top-level README.
- `docs`: technical and release documentation.
- `docs/wiki`: end-user documentation.
- `tooling/project`: repo-specific helpers like smoke runs, hooks, icon generation, and version bumping.
- `tooling/generic-mobile-ci`: reusable mobile CI/release helpers.
- `landing` and `eminent-ellipse`: separate Node-based subprojects; treat them as independent work areas.

## Build and validation

Baseline validation:

```bash
./gradlew build
```

Fast targeted checks:

```bash
./gradlew :shared:core:jvmTest
./gradlew :shared:mobile-ui:compileKotlinIosSimulatorArm64
./gradlew :androidApp:compileDebugKotlinAndroid
./gradlew :wearApp:compileDebugKotlinAndroid
./gradlew ktlintCheckAll
```

Formatting:

```bash
./gradlew ktlintFormatAll
```

Smoke tests:

```bash
./tooling/project/run_smoke.sh
```

F-Droid-specific validation:

```bash
./gradlew :androidApp:testFdroidDebugUnitTest :wearApp:testFdroidDebugUnitTest
./gradlew :androidApp:assembleFdroidRelease :wearApp:assembleFdroidRelease
```

## Platform notes

- Android and Wear enable Firebase/Crashlytics only when the matching config files exist and the requested tasks target `playstore`.
- `fdroid` flavors must stay free of Google Play Services, Firebase, Google Maps SDK, and Garmin Connect IQ dependencies. Both app modules have explicit Gradle verification tasks for this.
- Android `fdroid` uses `osmdroid` instead of Google Maps.
- Apple code depends on generated KMP frameworks from `shared/core` and `shared/mobile-ui`.
- The checked-in Xcode project may need regeneration from `apple/project.yml`, but `xcodegen` is not guaranteed to be available.

## Working rules for agents

- Read the module you are changing before patching; this repo mixes shared and platform-specific implementations, so similar behavior may exist in Android, Wear, iOS, watchOS, and flavor-specific source sets.
- Prefer the smallest validation command that covers the edited area before running a full build.
- Be careful with flavors and source sets such as `src/fdroid`, `src/playstore`, `androidMain`, `appleMain`, and watchOS targets. A change that works on one target may silently break another.
- Do not assume secrets are present. The repo is designed to build with safe fallbacks when Firebase files or maps keys are absent.
- Keep F-Droid changes isolated from Play-specific integrations.
- Check `README.md`, `RELEASE.md`, `docs/fdroid/README.md`, and `apple/README.md` before making release or platform workflow changes.
- The worktree may already contain user edits. Do not revert unrelated modifications.

## Useful entry points

- `README.md`: overall architecture, setup, CI/CD, and common commands.
- `RELEASE.md`: release checklist.
- `docs/fdroid/README.md`: F-Droid packaging and submission notes.
- `apple/README.md`: Apple shell architecture.
- `tooling/README.md`: automation/helper layout.
