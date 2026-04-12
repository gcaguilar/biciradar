package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Returns the nearest station within the user's configured search radius,
 * falling back to the absolute nearest station if none is within radius.
 */
@SingleIn(AppScope::class)
@Inject
class FindNearestStation(
  private val stationsRepository: StationsRepository,
  private val settingsRepository: SettingsRepository,
) {
  suspend fun execute(): Station? {
    stationsRepository.loadIfNeeded()
    val stations = stationsRepository.state.value.stations
    return selectNearbyStation(
      stations = stations,
      searchRadiusMeters = settingsRepository.currentSearchRadiusMeters(),
    ).highlightedStation
  }
}
