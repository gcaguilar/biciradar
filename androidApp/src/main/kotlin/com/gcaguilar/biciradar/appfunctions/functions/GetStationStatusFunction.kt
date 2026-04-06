package com.gcaguilar.biciradar.appfunctions.functions

import androidx.appfunctions.AppFunction
import com.gcaguilar.biciradar.appfunctions.parameters.DetailLevel
import com.gcaguilar.biciradar.appfunctions.parameters.GetStationStatusParams
import com.gcaguilar.biciradar.appfunctions.results.StationStatusResult
import com.gcaguilar.biciradar.appfunctions.results.StationStatusCardUi
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.StationsRepository
import javax.inject.Inject

@AppFunction
class GetStationStatusFunction @Inject constructor(
    private val stationsRepository: StationsRepository,
    private val favoritesRepository: FavoritesRepository
) {
    suspend fun execute(params: GetStationStatusParams): StationStatusResult? {
        val station = stationsRepository.getStationById(params.stationId) ?: return null
        val isFavorite = favoritesRepository.favoriteIds.value.contains(station.id)

        return StationStatusResult(
            stationId = station.id,
            name = station.name,
            address = station.address,
            bikesAvailable = station.bikesFree,
            slotsAvailable = station.slotsFree,
            isOpen = station.isOpen,
            lastUpdated = station.lastUpdated,
            isFavorite = isFavorite,
            uiPresentation = if (params.detailLevel == DetailLevel.FULL) {
                StationStatusCardUi(
                    title = station.name,
                    statusText = if (station.isOpen) "Abierta" else "Cerrada",
                    bikesText = "${station.bikesFree} bicis",
                    slotsText = "${station.slotsFree} plazas",
                    primaryAction = "Ver en mapa"
                )
            } else null
        )
    }
}
