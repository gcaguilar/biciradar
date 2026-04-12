package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Toggles the favorite status of a station.
 */
@SingleIn(AppScope::class)
@Inject
class ToggleFavoriteStation(
  private val favoritesRepository: FavoritesRepository,
) {
  suspend fun execute(stationId: String) {
    favoritesRepository.toggle(stationId)
  }
}
