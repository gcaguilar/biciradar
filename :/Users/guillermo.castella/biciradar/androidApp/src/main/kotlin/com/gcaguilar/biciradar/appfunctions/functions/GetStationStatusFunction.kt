package com.gcaguilar.biciradar.appfunctions.functions

import com.gcaguilar.biciradar.appfunctions.results.StationStatusResult
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.StationsRepository
import javax.inject.Inject

class GetStationStatusFunction @Inject constructor(
    private val stationsRepository: StationsRepository,
    private val favoritesRepository: FavoritesRepository
) {
    suspend fun execute(stationId: String): StationStatusResult? {
        val station = stationsRepository.stationById(stationId) ?: return null
        val isFavorite = favoritesRepository.favoriteIds.value.contains(station.id)

        return StationStatusResult(
            stationId = station.id,
            name = station.name,
            address = station.address,
            bikesAvailable = station.bikesAvailable,
            slotsAvailable = station.slotsFree,
            isOpen = true,
            lastUpdated = System.currentTimeMillis(),
            isFavorite = isFavorite
        )
    }
}