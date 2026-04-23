# F-Droid submission guide

This repository includes a dedicated `fdroid` flavor for the Android phone app. The Wear OS app still keeps its `fdroid` flavor in the codebase for future work, but it is not part of the current F-Droid submission.

- Android phone: `com.gcaguilar.biciradar.fdroid`

The current F-Droid package metadata lives in [metadata/](/Users/guillermo.castella/biciradar/metadata).

## What ships from this repo

F-Droid's current `fdroiddata` contribution guide recommends keeping app text and screenshots in the upstream source repository whenever possible. In this project that content lives here:

- Android phone listing text and screenshots: [fastlane/metadata/android/en-US/](/Users/guillermo.castella/biciradar/fastlane/metadata/android/en-US)
- F-Droid metadata YAML for the phone app: [metadata/com.gcaguilar.biciradar.fdroid.yml](/Users/guillermo.castella/biciradar/metadata/com.gcaguilar.biciradar.fdroid.yml)

The YAML files are the pieces that get added to `fdroiddata`. The fastlane metadata stays in this repository at the repository-root `fastlane/metadata` path so F-Droid tooling can detect it directly from the tagged source release.

## F-Droid-specific behavior

The `fdroid` flavors intentionally remove proprietary integrations:

- no Firebase or Crashlytics
- no Play Review or in-app updates
- no Garmin Connect IQ
- no Play Services wearable sync
- Android map support switches to OpenStreetMap via `osmdroid`
- external routing falls back to generic `geo:` intents

Both Android modules still run `verifyFdroidReleaseDependencies` during `assembleFdroidRelease`, but the submission flow documented here only covers the phone app.

To keep the Android phone APK friendlier to reproducible builds, the `fdroidRelease` variant also disables:

- code minification
- resource shrinking
- embedded VCS info
- Android baseline profile / `.dm` sidecar generation

## Local validation

Build the exact F-Droid release APKs:

```bash
./gradlew :androidApp:assembleFdroidRelease
```

Run the repository-level submission check:

```bash
bash tooling/project/check_fdroid_submission.sh
```

This validation checks the package metadata, the expected APK outputs, the F-Droid flavor selection in the YAML, and whether real screenshots are present instead of placeholder directories.

For the current `fdroidRelease` configuration, repeated clean local builds of the same commit produce the same APK SHA-256:

```text
248b405afd207cb38fe2a814a48cb077f6b28f1e5abc66c2e5e26dc09611e219
```

That does not replace F-Droid's official reproducibility verification, but it is a useful local sanity check before wiring upstream signing metadata.

## Submission checklist

1. Build the F-Droid APKs locally.
2. Add real screenshots under:
   - `fastlane/metadata/android/en-US/images/phoneScreenshots/`
3. Tag the exact release commit you want F-Droid to build, or at minimum confirm the commit hash in each YAML file.
4. Update the `commit`, `versionName`, and `versionCode` fields in the corresponding YAML file when preparing a new release.
5. Open a merge request to `fdroiddata` adding:
   - `metadata/com.gcaguilar.biciradar.fdroid.yml`

## Current status in this repo

- The Android F-Droid metadata already targets the `fdroid` Gradle flavor.
- The release APK output path in the YAML file matches the current Gradle output.
- The Wear OS `fdroid` flavor remains in the repo but is intentionally out of scope for this submission.
