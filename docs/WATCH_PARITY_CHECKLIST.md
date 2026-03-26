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

- [ ] WidgetKit complication for favorite station status
- [ ] Dedicated watch surface snapshot reader from App Group storage
- [ ] Live Activity handoff from iPhone to watch surface

## Notes

- The existing watch app already covers quick lookup and routing, so the optional complication remains deferred.
- Home/work ordering and short status text now match the native surface rules used on iPhone and Android.
