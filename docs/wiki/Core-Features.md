# Core features

With BiciRadar you can:

- See the nearest station.
- Find stations with available bikes.
- Find stations with free docks.
- Open favorites.
- Check station status by name or number.
- Get directions to a station.

## Trip mode: on foot or cycling

Use the **On foot / Cycling** selector at the top of Nearby to tell BiciRadar
what you are about to do:

- **On foot**: only stations with bikes available are listed, ordered by
  distance, and the map recommends the "With bikes" filter.
- **Cycling**: only stations with free slots are listed, ordered by distance,
  and the map recommends the "With slots" filter.

The mode is saved as a preference. Changing it on Nearby also reorders the map
filters so the recommended one comes first. Any availability filter you pick
manually on the map takes priority over the one suggested by the mode.

On Nearby, the **Route** action follows the mode too: it opens walking
directions when you are on foot and cycling directions when you are riding.

## Filtering and sorting Nearby

The Nearby list has a **Filters** button that opens a panel where you can:

- **Bike type**: both, e-bikes, or mechanical bikes. This option only appears in
  cities whose data distinguishes both types.
- **Max distance**: 500 m, 1 km, or 2 km.
- **Favorites only**: show only your saved stations.
- **Sort by**: nearest, most bikes, most free slots, name, or best predicted
  availability.

**Best predicted availability** uses each station's hourly usage pattern for the
current hour; otherwise it falls back to the availability reported right now.
Patterns are built from a rolling week of observations, so a city that was just
added falls back to live availability until it has collected samples for that
hour. Filters and sorting apply for the current session and reset when you
switch city.

## Usage patterns

The station detail includes a **Usage pattern** card that summarises how busy a
station usually is at each hour: average bikes, average free docks and
occupancy, split into weekdays (Mon–Fri) and weekends (Sat–Sun). Like the
prediction above, it covers a rolling week; until there is enough history the
card shows "No usage pattern data available." instead of an invented value.
A few operators don't publish how many free docks each station has, so those
cities always show the empty card; BiciRadar never estimates the missing counts.

## Routines (recurring monitoring from the Trip tab)

The **Trip** tab has a **Routines** section to monitor your stations on a
schedule, so you find out before leaving that your regular station has bikes
(or free docks).

- A **routine** has one shared schedule: the days of the week and the time.
- Each routine can include **several stations**; every station keeps its own
  monitoring duration (5/10/15/20 min) and what to watch (bikes or free docks).
- Monitoring starts **10 minutes before** the configured time. From this
  release, the watch runs **on the BiciRadar backend**, not on the device: the
  app syncs your routines when you sign in and the server opens a monitoring
  window around the scheduled time, then sends a **push notification** (with a
  suggested nearby alternative) if the station runs out of bikes or docks.
- Stations are added from **Favorites**, a **manual search**, or by picking a
  station **on the map**.
- Routines can be enabled/disabled, edited and deleted from the routines list.

Because monitoring is server-side:

- It works even with the app closed, and consistently on **Android and iPhone /
  Apple Watch** (the same push reaches your phone and is reflected on the watch).
- The app keeps a **local copy** of routines and works offline; changes sync
  when you are signed in. Without a backend/Auth0 configuration the app stays in
  **local-only mode** ("On this device only").
- Your **saved places and alerts** are also watched by the server, which is the
  single source of availability notifications. Widgets and watch complications
  are refreshed when you open the app and when a push arrives.

## Trip monitoring

Trip monitoring requires a BiciRadar account, because the watch runs on the
server. If you are not signed in, the Trip tab asks you to **sign in or create
an account** before you can start monitoring.

When you start a trip, the app asks the backend to watch your destination
station for the chosen duration (5/10/15/20 min) and cancels the watch early
when you arrive or when the alert with the alternative is delivered. The backend
stops on its own when the time is up, so nothing is left monitoring forever even
if the app is closed. From the trip you can also **create a routine from this
trip**: it opens the editor with the destination station pre-filled as an
_arrival_ step (watch free docks) and a suggested time based on how far it is.

## Next

- [iPhone - Siri and Shortcuts](iPhone-Siri-and-Shortcuts)
- [Apple Watch - Siri and Shortcuts](Apple-Watch-Siri-and-Shortcuts)
- [Android - Google Assistant](Android-Google-Assistant)
