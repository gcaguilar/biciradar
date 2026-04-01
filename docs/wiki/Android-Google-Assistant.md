# Android - Google Assistant

Actions and shortcuts are implemented in:

- `androidApp/src/androidMain/res/xml/shortcuts.xml`
- `androidApp/src/androidMain/kotlin/com/gcaguilar/biciradar/AndroidLaunchRequestParser.kt`

## Supported actions

- `nearest_station`
- `nearest_station_with_bikes`
- `nearest_station_with_slots`
- `favorite_stations`
- `station_status`
- `station_bike_count`
- `station_slot_count`
- `route_to_station`
- `show_station`

## Saved place aliases

- `casa`, `mi casa`, `home`
- `trabajo`, `mi trabajo`, `work`
- `oficina`, `mi oficina`

## Example phrases

- "cual es la estacion mas cercana con Bici Radar"
- "donde hay bicis cerca con Bici Radar"
- "donde puedo dejar la bici con Bici Radar"
- "abre mis favoritas con Bici Radar"
- "como esta una estacion con Bici Radar"
- "cuantas bicis hay en una estacion con Bici Radar"
- "cuantos huecos hay en una estacion con Bici Radar"
- "llevame a una estacion con Bici Radar"
- "como esta casa con Bici Radar"
- "como esta trabajo con Bici Radar"
- "llevame a casa con Bici Radar"
- "llevame al trabajo con Bici Radar"

## Notes

- On Android emulator, the most reliable validation method is launching intents directly.
- Natural station-name recognition still depends on Google Assistant matching on the device.
