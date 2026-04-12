package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Returns the list of stations marked as favorites, sorted by distance then name.
 *
 * Unlike [GetFavoriteStations], this returns plain [Station] objects
 * rather than [SurfaceStationSnapshot], making it suitable for widget
 * extensions and other contexts that do not need Surface metadata.
 */
@SingleIn(AppScope::class)
@Inject
class GetFavoriteStationList(
    private val stationsRepository: StationsRepository,
    private val favoritesRepository: FavoritesRepository,
) {
    suspend fun execute(): List<Station> {
        stationsRepository.loadIfNeeded()
        return stationsRepository.state.value.stations
            .filter { favoritesRepository.isFavorite(it.id) }
            .sortedWith(
                compareBy({ it.distanceMeters }, { it.name }),
            )
    }
}
