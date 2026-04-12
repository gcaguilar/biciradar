package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Returns the nearest station with at least one bike available,
 * within the user's configured search radius (falls back to the
 * absolute nearest with bikes if none is within radius).
 */
@SingleIn(AppScope::class)
@Inject
class FindNearestStationWithBikes(
    private val stationsRepository: StationsRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun execute(): Station? {
        stationsRepository.loadIfNeeded()
        val stations = stationsRepository.state.value.stations
        return selectNearbyStationWithBikes(
            stations = stations,
            searchRadiusMeters = settingsRepository.currentSearchRadiusMeters(),
        ).highlightedStation
    }
}
