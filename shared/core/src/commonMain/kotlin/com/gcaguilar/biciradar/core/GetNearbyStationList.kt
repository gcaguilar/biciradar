package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Returns the first [limit] stations sorted by distance from the current
 * user location (i.e. the stations already ordered by the repository).
 *
 * Unlike [GetNearestStations] this returns plain [Station] objects and
 * does not apply a search radius filter.
 */
@SingleIn(AppScope::class)
@Inject
class GetNearbyStationList(
    private val stationsRepository: StationsRepository,
) {
    suspend fun execute(limit: Int = 5): List<Station> {
        stationsRepository.loadIfNeeded()
        return stationsRepository.state.value.stations.take(limit)
    }
}
