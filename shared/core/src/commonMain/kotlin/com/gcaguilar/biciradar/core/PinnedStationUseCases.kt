package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Pins [stationId] as the user's home station.
 *
 * There is only one home station: this replaces any previous pin. Home is a
 * category of its own, so pinning does not mark the station as a favorite;
 * `StationInfo.isHome` reports it independently from `isFavorite`.
 */
@SingleIn(AppScope::class)
@Inject
class SetHomeStation(
  private val favoritesRepository: FavoritesRepository,
) {
  suspend fun execute(stationId: String) {
    favoritesRepository.setHomeStationId(stationId)
  }
}

/**
 * Pins [stationId] as the user's work station.
 *
 * There is only one work station: this replaces any previous pin. Work is a
 * category of its own, so pinning does not mark the station as a favorite;
 * `StationInfo.isWork` reports it independently from `isFavorite`.
 */
@SingleIn(AppScope::class)
@Inject
class SetWorkStation(
  private val favoritesRepository: FavoritesRepository,
) {
  suspend fun execute(stationId: String) {
    favoritesRepository.setWorkStationId(stationId)
  }
}
