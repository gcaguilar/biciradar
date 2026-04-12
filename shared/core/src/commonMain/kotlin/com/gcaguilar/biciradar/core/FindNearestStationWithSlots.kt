package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Returns the nearest station with at least one free dock available,
 * within the user's configured search radius (falls back to the
 * absolute nearest with free docks if none is within radius).
 */
@SingleIn(AppScope::class)
@Inject
class FindNearestStationWithSlots(
    private val stationsRepository: StationsRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun execute(): Station? {
        stationsRepository.loadIfNeeded()
        val stations = stationsRepository.state.value.stations
        return selectNearbyStationWithSlots(
            stations = stations,
            searchRadiusMeters = settingsRepository.currentSearchRadiusMeters(),
        ).highlightedStation
    }
}
