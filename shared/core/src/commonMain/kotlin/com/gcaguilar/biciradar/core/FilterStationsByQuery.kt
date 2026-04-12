package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Filters stations by a free-text query, returning ranked results.
 * If the query is blank, returns all stations ordered by distance.
 *
 * Delegates to the shared [filterStationsByQuery] pure function.
 */
@SingleIn(AppScope::class)
@Inject
class FilterStationsByQuery(
  private val stationsRepository: StationsRepository,
) {
  suspend fun execute(query: String): List<Station> {
    stationsRepository.loadIfNeeded()
    val stations = stationsRepository.state.value.stations
    return filterStationsByQuery(stations = stations, query = query)
  }
}
