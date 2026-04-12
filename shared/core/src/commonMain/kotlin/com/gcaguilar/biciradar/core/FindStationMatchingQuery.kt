package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Finds the best single station matching a free-text query.
 * Supports pinned home/work aliases ("casa", "trabajo", etc.).
 * If [query] is null or empty, returns the closest station.
 *
 * Delegates to [findStationMatchingQueryOrPinnedAlias].
 */
@SingleIn(AppScope::class)
@Inject
class FindStationMatchingQuery(
  private val stationsRepository: StationsRepository,
  private val favoritesRepository: FavoritesRepository,
) {
  suspend fun execute(query: String?): Station? {
    stationsRepository.loadIfNeeded()
    val stations = stationsRepository.state.value.stations
    return findStationMatchingQueryOrPinnedAlias(
      stations = stations,
      query = query,
      homeStationId = favoritesRepository.currentHomeStationId(),
      workStationId = favoritesRepository.currentWorkStationId(),
    )
  }
}
