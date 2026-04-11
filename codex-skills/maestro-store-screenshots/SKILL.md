---
name: maestro-store-screenshots
description: Use when you need to capture Google Play or App Store screenshots with Maestro and related device tools. This skill plans screenshot sets, creates or updates Maestro flows, runs capture commands, and validates Android phone, Wear OS, iPhone, and Apple Watch assets against current store requirements.
---

# Maestro Store Screenshots

Use this skill to produce store-ready screenshots for Android phone, Wear OS, iPhone, and Apple Watch.

## Read This First

1. Read `references/store-requirements.md` before choosing devices or output sizes.
2. Read `references/biciradar.md` when working in this repo.
3. Prefer raw screenshots from accepted Apple simulator sizes. Do not resize Apple screenshots unless the source already matches the target aspect ratio.

## Platform Matrix

- Android phone: Maestro is the default. Use `takeScreenshot` in a Flow and validate the output against Play Console rules. Raw modern phone screenshots are valid if they stay within Google's bounds; exact `1080x1920` is only needed if marketing wants the recommended `9:16` canvas.
- Wear OS: Try Maestro first only if the watch/emulator is visible and selectors are reliable. If that is flaky, navigate with `adb` or manually, then capture with `adb exec-out screencap -p`.
- iPhone: Use Maestro on an accepted simulator size, ideally the newest available Pro Max or Plus simulator. Keep the raw screenshot size if it matches one of Apple's accepted dimensions.
- Apple Watch: Maestro does not document official watchOS support. Put the watch app into the desired state with the simulator, app fixtures, or native tests, then capture with `xcrun simctl io <udid> screenshot`.

## Workflow

### 1. Pick the exact targets

- Android phone: target Play Console-compatible PNG/JPEG. Use the validator to confirm minimum and maximum bounds.
- Wear OS: target a square screenshot with minimum `384x384`.
- iPhone: prefer `6.9"` or `6.5"` accepted sizes when the app runs on iPhone.
- Apple Watch: pick one accepted watch size and keep it consistent across all localizations.

### 2. Build and install the app

- Android and Wear OS: use the repo Gradle install tasks from `references/biciradar.md`.
- iPhone and Apple Watch: use the Xcode project and simulator destinations from `references/biciradar.md`.
- If Maestro has no running target, use `maestro start-device` or boot the simulator/emulator yourself.

### 3. Capture phone screenshots with Maestro

- Start from the platform-specific flow:
  - Android bootstrap: `assets/flows/biciradar-android-bootstrap.yaml`
  - Android capture: `assets/flows/biciradar-phone-store.yaml`
  - iPhone deep-link prep: `scripts/open_ios_deeplink.sh <udid> biciradar://city/zaragoza`
  - iPhone bootstrap: `assets/flows/biciradar-ios-bootstrap.yaml`
  - iPhone per-screen capture:
    - `assets/flows/biciradar-ios-home-capture.yaml`
    - `assets/flows/biciradar-ios-favorites-capture.yaml`
    - `assets/flows/biciradar-ios-nearby-capture.yaml`
    - `assets/flows/biciradar-ios-station-detail-capture.yaml`
  - iPhone full set helper: `scripts/run_biciradar_ios_store_set.sh`
- Keep the Flow deterministic:
  - Prefer deep links, seeded data, or fixed search queries.
  - Use one `takeScreenshot` step per final store asset.
  - Assert the target UI before every screenshot.
- Run the flow with `scripts/run_maestro_capture.sh`.

Example:

```bash
bash scripts/run_maestro_capture.sh \
  --bootstrap-flow assets/flows/biciradar-android-bootstrap.yaml \
  --flow assets/flows/biciradar-phone-store.yaml \
  --output-dir /tmp/biciradar-store/android-phone \
  --device emulator-5554
```

```bash
bash scripts/open_ios_deeplink.sh \
  10346F56-822D-4688-8DC2-D631BF9C50C8 \
  biciradar://city/zaragoza
```

```bash
bash scripts/run_biciradar_ios_store_set.sh \
  10346F56-822D-4688-8DC2-D631BF9C50C8 \
  /tmp/biciradar-store/iphone
```

If you need just one screen:

```bash
bash scripts/open_ios_deeplink.sh \
  10346F56-822D-4688-8DC2-D631BF9C50C8 \
  "biciradar://assistant?action=open_assistant"

maestro --device 10346F56-822D-4688-8DC2-D631BF9C50C8 test \
  --test-output-dir /tmp/biciradar-store/iphone \
  assets/flows/biciradar-ios-assistant-capture.yaml
```

### 4. Capture watch screenshots with native tools

- Wear OS:

```bash
bash scripts/capture_android_screen.sh emulator-5556 /tmp/biciradar-store/wearos/01-home.png
```

For BiciRadar on the local round Wear emulator, use the dedicated helper:

```bash
bash scripts/run_biciradar_wear_store_capture.sh \
  emulator-5556 \
  /tmp/biciradar-store/wearos
```

This helper now captures a small Wear OS set in one go:
- `01-dashboard.png`
- `02-station-detail.png`
- `03-monitoring.png`

  - Apple Watch:

```bash
bash scripts/capture_simulator_screen.sh <watch-udid> /tmp/biciradar-store/apple-watch/01-home.png
```

For BiciRadar watch, use the dedicated helper so the simulator reboots cleanly and the app launches with a fixed Zaragoza location for repeatable captures:

```bash
bash scripts/run_biciradar_watch_store_capture.sh \
  BB46A5C0-BC43-4AD7-9E0F-46EECD968EE3 \
  /tmp/biciradar-store/apple-watch
```

This helper cycles through the built-in screenshot surfaces and writes:
- `01-watch-dashboard.png`
- `02-watch-station-detail.png`
- `03-watch-monitoring.png`

### 5. Resize only when it is safe

- `scripts/fit_image_to_canvas.py` is conservative on purpose.
- Default behavior only resamples when the source aspect ratio already matches the target.
- `--mode pad` exists for emergency marketing use, but adding bars or background pixels is usually a bad idea for store uploads.

### 6. Validate before upload

Run the validator on the final folder, not the raw capture folder.

```bash
python3 scripts/check_store_screenshots.py --platform android-phone /tmp/biciradar-store/android-phone
python3 scripts/check_store_screenshots.py --platform wearos /tmp/biciradar-store/wearos
python3 scripts/check_store_screenshots.py --platform iphone /tmp/biciradar-store/iphone
python3 scripts/check_store_screenshots.py --platform apple-watch /tmp/biciradar-store/apple-watch
```

## BiciRadar Notes

- The existing Android Maestro smoke flow is `maestro/android/assistant-smoke.yaml`.
- This repo already exposes useful deep links for deterministic capture. Reuse those before adding fragile tap chains.
- The current phone store set that is easiest to automate is:
  - Android: Home map, Favorites, Trip, Station detail
  - iPhone: Home map, Favorites, Nearby, Station detail

## When To Stop And Re-plan

- The simulator locale changed and visible text selectors no longer match.
- The Apple target device is not one of the accepted screenshot sizes.
- The watch UI depends on live state that cannot be reproduced deterministically.
- The app requires data fixtures or permissions that are not yet scripted.

In those cases, update the Flow or seed state first, then retry the capture.
