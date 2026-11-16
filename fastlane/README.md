fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## iOS

### ios build_signed_ipa

```sh
[bundle exec] fastlane ios build_signed_ipa
```

Build signed IPA for testing (no upload)

### ios upload_binary_to_app_store

```sh
[bundle exec] fastlane ios upload_binary_to_app_store
```

Build signed IPA and upload the binary to App Store Connect (no review submission)

### ios submit_to_app_store

```sh
[bundle exec] fastlane ios submit_to_app_store
```

Upload metadata and submit the already-uploaded build for App Store review

### ios release_to_app_store

```sh
[bundle exec] fastlane ios release_to_app_store
```

Build, upload and submit to App Store (local convenience: build+upload, then submit)

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
