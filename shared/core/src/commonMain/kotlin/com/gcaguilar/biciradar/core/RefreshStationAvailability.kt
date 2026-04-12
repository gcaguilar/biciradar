package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Refreshes real-time availability (bikes / docks) for a specific subset of stations
 * without triggering a full station list reload.
 *
 * Used by wearOS and widgets to keep nearby stations fresh efficiently.
 */
@SingleIn(AppScope::class)
@Inject
class RefreshStationAvailability(
    private val stationsRepository: StationsRepository,
) {
    suspend fun execute(stationIds: List<String>) {
        stationsRepository.refreshAvailability(stationIds)
    }
}
