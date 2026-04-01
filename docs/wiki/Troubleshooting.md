# Troubleshooting

## Voice command is not recognized

- Confirm the app is installed and updated.
- Say the full phrase including "Bici Radar".
- Retry with a simpler request first (for example nearest station).
- On Android, Assistant matching quality depends on the device and language model.

## Nearest station looks wrong

- Check location permission status.
- Disable battery saver for the app if background location refresh is restricted.
- Move to an open-sky area and retry.

## Favorites are missing

- Verify you are on the same account/session/device setup.
- Re-save Home/Work stations.
- On watch, ensure pairing/sync is active with iPhone.

## Data seems stale

- Pull to refresh (if available) or reopen the app.
- Network/provider feed may be delayed temporarily.

## Android emulator voice testing

The most reliable method is launching intents directly instead of natural voice capture.

## Website analytics (Umami) are not showing visits

If you are validating the landing analytics:

- Confirm the Umami script is present in page source:
  - `https://cloud.umami.is/script.js`
  - expected `data-website-id` value.
- Verify your deployed domain is allowed in Umami settings.
- Disable ad-blockers/privacy shields during testing.
- Check browser console and network tab for blocked script/CSP errors.
- If you use CSP, allow:
  - `script-src https://cloud.umami.is`
  - `connect-src https://cloud.umami.is`
