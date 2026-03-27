# Apple Watch parity checklist

## Shared data and naming

- [x] Watch favorites use the same synced favorite ids as iPhone surfaces
- [x] Watch saved-place priority recognizes home/work ordering
- [x] Watch status labels follow the same short states as surfaces: `Disponible`, `Pocas`, `Sin bicis`, `Sin huecos`

## Current watch coverage

- [x] Nearby list
- [x] Favorite stations list
- [x] Station detail with bikes, docks and route actions
- [x] Siri/App Shortcuts on watch for nearest, favorites, status and routing

## Pending or explicitly deferred

- [x] WidgetKit complication for favorite station status
- [x] Dedicated watch surface snapshot reader from App Group storage
- [x] Live Activity handoff from iPhone to watch surface

## Notes

- The existing watch app already covers quick lookup and routing, and now also includes a favorite-status complication for at-a-glance checks.
- The watch dashboard now persists a compact local snapshot in the shared App Group container and reuses it as an offline fallback.
- The watch complication reads that same App Group snapshot to show favorite bikes, docks and stale-state fallback without extra network calls.
- Active monitoring from iPhone is mirrored onto the watch dashboard through `WatchConnectivity`, including a quick route handoff back to the phone.
- Home/work ordering and short status text now match the native surface rules used on iPhone and Android.
