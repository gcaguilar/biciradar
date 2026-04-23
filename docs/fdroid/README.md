# F-Droid submission guide

This repository includes dedicated `fdroid` flavors for the Android phone app and the Wear OS app:

- Android phone: `com.gcaguilar.biciradar.fdroid`
- Wear OS: `com.gcaguilar.biciradar.wear.fdroid`

They are separate F-Droid packages, each with its own metadata file in [metadata/](/Users/guillermo.castella/biciradar/metadata).

## What ships from this repo

F-Droid's current `fdroiddata` contribution guide recommends keeping app text and screenshots in the upstream source repository whenever possible. In this project that content lives here:

- Android phone listing text and screenshots: [androidApp/src/fdroid/fastlane/metadata/android/en-US/](/Users/guillermo.castella/biciradar/androidApp/src/fdroid/fastlane/metadata/android/en-US)
- Wear OS listing text and screenshots: [wearApp/src/fdroid/fastlane/metadata/android/en-US/](/Users/guillermo.castella/biciradar/wearApp/src/fdroid/fastlane/metadata/android/en-US)
- F-Droid metadata YAML for the phone app: [metadata/com.gcaguilar.biciradar.fdroid.yml](/Users/guillermo.castella/biciradar/metadata/com.gcaguilar.biciradar.fdroid.yml)
- F-Droid metadata YAML for the Wear app: [metadata/com.gcaguilar.biciradar.wear.fdroid.yml](/Users/guillermo.castella/biciradar/metadata/com.gcaguilar.biciradar.wear.fdroid.yml)

The YAML files are the pieces that get added to `fdroiddata`. The fastlane metadata stays in this repository and is fetched by F-Droid from the tagged source release.

## F-Droid-specific behavior

The `fdroid` flavors intentionally remove proprietary integrations:

- no Firebase or Crashlytics
- no Play Review or in-app updates
- no Garmin Connect IQ
- no Play Services wearable sync
- Android map support switches to OpenStreetMap via `osmdroid`
- external routing falls back to generic `geo:` intents

Both Android modules also run `verifyFdroidReleaseDependencies` during `assembleFdroidRelease` and fail if the runtime classpath contains forbidden proprietary SDKs.

## Local validation

Build the exact F-Droid release APKs:

```bash
./gradlew :androidApp:assembleFdroidRelease
./gradlew :wearApp:assembleFdroidRelease
```

Run the repository-level submission check:

```bash
bash tooling/project/check_fdroid_submission.sh
```

This validation checks the package metadata, the expected APK outputs, the F-Droid flavor selection in the YAML, and whether real screenshots are present instead of placeholder directories.

## Submission checklist

1. Build the F-Droid APKs locally.
2. Add real screenshots under:
   - `androidApp/src/fdroid/fastlane/metadata/android/en-US/images/phoneScreenshots/`
   - `wearApp/src/fdroid/fastlane/metadata/android/en-US/images/wearScreenshots/`
3. Tag the exact release commit you want F-Droid to build, or at minimum confirm the commit hash in each YAML file.
4. Update the `commit`, `versionName`, and `versionCode` fields in the corresponding YAML file when preparing a new release.
5. Open a merge request to `fdroiddata` adding:
   - `metadata/com.gcaguilar.biciradar.fdroid.yml`
   - `metadata/com.gcaguilar.biciradar.wear.fdroid.yml` if you are also submitting the Wear package

## Current status in this repo

- The Android and Wear F-Droid metadata already target the `fdroid` Gradle flavor.
- The release APK output paths in the YAML files match the current Gradle outputs.
- The remaining manual submission blocker is adding real screenshots before the merge request.
