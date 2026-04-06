package com.gcaguilar.biciradar.appfunctions.functions

import androidx.appfunctions.AppFunction
import com.gcaguilar.biciradar.appfunctions.results.FavoritesListResult
import com.gcaguilar.biciradar.appfunctions.results.FavoritesListUi
import com.gcaguilar.biciradar.appfunctions.results.StationResult
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.StationsRepository
import javax.inject.Inject

@AppFunction
class GetFavoritesFunction @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    private val stationsRepository: StationsRepository
) {
    suspend fun execute(): FavoritesListResult {
        val favoriteIds = favoritesRepository.favoriteIds.value
        val homeId = favoritesRepository.homeStationId.value
        val workId = favoritesRepository.workStationId.value
        
        val favoriteStations = favoriteIds.mapNotNull { id ->
            stationsRepository.getStationById(id)?.toResult(isFavorite = true)
        }
        
        val homeStation = homeId?.let { 
            stationsRepository.getStationById(it)?.toResult(isFavorite = true) 
        }
        
        val workStation = workId?.let { 
            stationsRepository.getStationById(it)?.toResult(isFavorite = true) 
        }
        
        return FavoritesListResult(
            favorites = favoriteStations,
            homeStation = homeStation,
            workStation = workStation,
            totalCount = favoriteStations.size,
            uiPresentation = FavoritesListUi(
                title = "Mis Estaciones Favoritas",
                subtitle = "${favoriteStations.size} estaciones guardadas",
                emptyStateMessage = if (favoriteStations.isEmpty()) "No tienes estaciones favoritas" else null
            )
        )
    }
    
    private fun com.gcaguilar.biciradar.core.Station.toResult(isFavorite: Boolean): StationResult {
        return StationResult(
            stationId = id,
            name = name,
            bikesAvailable = bikesFree,
            slotsAvailable = slotsFree,
            distance = 0.0,
            isFavorite = isFavorite
        )
    }
}