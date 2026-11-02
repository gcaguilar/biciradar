package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Availability filter applied when looking for the nearest station relative to an
 * explicit origin.
 */
enum class StationRequirement {
  /** Any station, regardless of how many bikes or free docks it reports. */
  ANY,

  /** Stations with at least one bike available. */
  BIKES,

  /** Stations with at least one electric bike available. */
  EBIKES,

  /** Stations with at least one free dock, so a bike can be returned. */
  SLOTS,
}

/**
 * Returns the station that satisfies [requirement] and is closest to [origin].
 *
 * Distances are always recomputed from [origin]; the `distanceMeters` values stored in
 * the repository snapshot (relative to the app's last known location) are ignored. The
 * selection keeps the same semantics as [FindNearestStation],
 * [FindNearestStationWithBikes] and [FindNearestStationWithSlots]: a matching station
 * inside the user's configured search radius wins, and when nothing matches inside the
 * radius the absolute nearest matching station is returned as a fallback. `null` means
 * the repository has no station satisfying [requirement] at all.
 *
 * [origin] must be a real location. An unknown origin is not this use case's concern:
 * the caller decides what to do when the calling agent does not supply coordinates
 * (reject the request, fall back to [FindNearestStation], and so on).
 */
@SingleIn(AppScope::class)
@Inject
class FindNearestStationFrom(
  private val stationsRepository: StationsRepository,
  private val settingsRepository: SettingsRepository,
) {
  suspend fun execute(
    origin: GeoPoint,
    requirement: StationRequirement = StationRequirement.ANY,
  ): Station? {
    stationsRepository.loadIfNeeded()
    val stations =
      stationsSortedByDistanceFrom(
        stations = stationsRepository.state.value.stations,
        origin = origin,
      )
    return selectNearbyStation(
      stations = stations,
      searchRadiusMeters = settingsRepository.currentSearchRadiusMeters(),
    ) { station -> requirement.isSatisfiedBy(station) }
      .highlightedStation
  }
}

private fun StationRequirement.isSatisfiedBy(station: Station): Boolean =
  when (this) {
    StationRequirement.ANY -> true
    StationRequirement.BIKES -> station.bikesAvailable > 0
    StationRequirement.EBIKES -> station.ebikesAvailable > 0
    StationRequirement.SLOTS -> station.slotsFree > 0
  }
