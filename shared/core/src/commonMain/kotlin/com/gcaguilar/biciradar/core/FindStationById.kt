package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Returns a single station by its ID from the in-memory station state.
 * Returns null if no station with that ID is currently loaded.
 */
@SingleIn(AppScope::class)
@Inject
class FindStationById(
    private val stationsRepository: StationsRepository,
) {
    fun execute(stationId: String): Station? = stationsRepository.stationById(stationId)
}
