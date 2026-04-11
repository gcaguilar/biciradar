# Store Screenshot Requirements

Verified on April 8, 2026 from official Google Play, Apple, and Maestro docs.

## Official Sources

- Google Play Console Help: https://support.google.com/googleplay/android-developer/answer/9866151
- Apple App Store Connect screenshot specs: https://developer.apple.com/help/app-store-connect/reference/app-information/screenshot-specifications/
- Maestro QuickStart (`takeScreenshot`, Android+iOS setup): https://docs.maestro.dev/get-started/quickstart
- Maestro device targeting and `start-device`: https://docs.maestro.dev/advanced/specify-a-device

## Google Play

### Android phone

- Format: JPEG or 24-bit PNG with no alpha.
- Minimum dimension: `320px`.
- Maximum dimension: `3840px`.
- The longest side cannot be more than 2x the shortest side.
- To stay eligible for recommendation formats, provide at least 4 screenshots with minimum `1080px` resolution:
  - Portrait: `1080x1920` (`9:16`)
  - Landscape: `1920x1080` (`16:9`)

Practical rule:

- Raw phone screenshots are valid if they pass the bounds above.
- Use an exact `1080x1920` marketing crop only when the team explicitly wants the recommended portrait canvas.

### Wear OS

- At least 1 screenshot.
- Show only the app interface.
- No device frames, extra text, graphics, or backgrounds outside the app UI.
- `1:1` aspect ratio.
- Minimum size: `384x384`.
- If the app offers Tiles, Google recommends also showing a Tile screenshot.

Practical rule:

- Prefer raw square captures from the watch/emulator.
- Only resample if the screenshot is below `384x384` or if you need consistent export dimensions.

## Apple App Store Connect

Apple accepts specific screenshot sizes. Prefer capturing on a simulator that already matches one of these exact outputs.

### iPhone sizes worth targeting first

- `6.9"`:
  - `1260x2736`
  - `1290x2796`
  - `1320x2868`
- `6.5"`:
  - `1242x2688`
  - `1284x2778`
  - Required if the app runs on iPhone and `6.9"` screenshots are not provided.
- `6.3"`:
  - `1179x2556`
  - `1206x2622`
- `6.1"`:
  - `1170x2532`
  - `1125x2436`
  - `1080x2340`
- `5.5"`:
  - `1242x2208`

Landscape inversions of the same sizes are also accepted.

Practical rule:

- Prefer the newest available Pro Max or Plus simulator so the raw capture already lands on an accepted `6.9"` or `6.5"` size.
- Do not pad iPhone screenshots with fake backgrounds.

### Apple Watch

Accepted sizes:

- `422x514` for Ultra 3
- `410x502` for Ultra 2 and Ultra
- `416x496` for Series 11 and Series 10
- `396x484` for Series 9, Series 8, and Series 7
- `368x448` for Series 6, Series 5, Series 4, SE 3, and SE
- `312x390` for Series 3

Practical rule:

- Pick one watch simulator family and keep that exact size for every localization.

## Maestro Capability Notes

- Official Maestro docs cover Android and iOS as supported mobile platforms.
- Official docs show `takeScreenshot` in Android and iOS Flows.
- Maestro docs do not document a dedicated watchOS or Apple Watch automation mode.

Practical rule:

- Use Maestro directly for Android phone and iPhone.
- Treat Wear OS as best-effort through Android tooling.
- Treat Apple Watch capture as simulator-native rather than Maestro-native.
