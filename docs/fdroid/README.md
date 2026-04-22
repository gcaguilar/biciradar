# F-Droid release notes

This repository includes a dedicated `fdroid` flavor for the Android phone app and the Wear OS app.

## Package IDs

- Android phone: `com.gcaguilar.biciradar.fdroid`
- Wear OS: `com.gcaguilar.biciradar.wear.fdroid`

Both packages are meant to be submitted as separate entries in `fdroiddata`.

## Functional differences in the F-Droid flavor

- No Firebase, Crashlytics, Play Review, Play In-App Updates, Garmin Connect IQ, or Play Services wearable sync.
- Location resolves through the Android framework `LocationManager`.
- Embedded Android map content uses OpenStreetMap tiles via `osmdroid` instead of Google Maps.
- External routing uses generic `geo:` intents so free navigation apps can handle them.

## Validation commands

```bash
./gradlew :androidApp:assembleFdroidRelease
./gradlew :wearApp:assembleFdroidRelease
```

The app modules also run `verifyFdroidReleaseDependencies` during `assembleFdroidRelease` and fail if the F-Droid runtime classpath includes:

- `com.google.android.gms`
- `com.google.firebase`
- `com.garmin.connectiq`
- `com.google.maps.android`

## Submission assets

Prepared assets live in:

- `androidApp/src/fdroid/fastlane/metadata/android/en-US/`
- `wearApp/src/fdroid/fastlane/metadata/android/en-US/`
- `metadata/com.gcaguilar.biciradar.fdroid.yml`
- `metadata/com.gcaguilar.biciradar.wear.fdroid.yml`

Before opening the merge request to `fdroiddata`, replace the placeholder screenshot directories with real captures and confirm the build commit/tag in the metadata YAML files.
