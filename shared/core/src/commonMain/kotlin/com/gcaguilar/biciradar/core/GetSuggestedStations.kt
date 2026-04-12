package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Returns a ranked list of suggested stations for the current user.
 *
 * Priority order:
 *  - Home station (400)
 *  - Work station (380)
 *  - Favorite stations (320)
 *  - Everything else (100)
 *
 * Within the same priority tier, stations are sorted by distance ASC then name A-Z.
 */
@SingleIn(AppScope::class)
@Inject
class GetSuggestedStations(
  private val stationsRepository: StationsRepository,
  private val favoritesRepository: FavoritesRepository,
) {
  suspend fun execute(limit: Int = 8): List<Station> {
    stationsRepository.loadIfNeeded()
    val stations = stationsRepository.state.value.stations
    val homeStationId = favoritesRepository.currentHomeStationId()
    val workStationId = favoritesRepository.currentWorkStationId()
    val favoriteIds: Set<String> = favoritesRepository.favoriteIds.value
    return stations
      .sortedWith(
        compareByDescending<Station> { it.suggestionPriority(favoriteIds, homeStationId, workStationId) }
          .thenBy { it.distanceMeters }
          .thenBy { it.name },
      ).take(limit)
  }
}

private fun Station.suggestionPriority(
  favoriteIds: Set<String>,
  homeStationId: String?,
  workStationId: String?,
): Int =
  when {
    id == homeStationId -> 400
    id == workStationId -> 380
    favoriteIds.contains(id) -> 320
    else -> 100
  }
