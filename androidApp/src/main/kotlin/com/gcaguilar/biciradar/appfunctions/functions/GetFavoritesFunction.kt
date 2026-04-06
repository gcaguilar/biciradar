package com.gcaguilar.biciradar.appfunctions.functions

import com.gcaguilar.biciradar.appfunctions.results.FavoritesListResult
import com.gcaguilar.biciradar.appfunctions.results.StationResult
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.StationsRepository
import javax.inject.Inject

/**
 * App Function that retrieves the user's favorite stations.
 */
class GetFavoritesFunction @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    private val stationsRepository: StationsRepository
) {
    suspend fun execute(): FavoritesListResult {
        val favoriteIds = favoritesRepository.favoriteIds.value
        val homeId = favoritesRepository.homeStationId.value
        val workId = favoritesRepository.workStationId.value
        
        val favoriteStations = favoriteIds.mapNotNull { id ->
            stationsRepository.stationById(id)?.toResult(isFavorite = true)
        }
        
        val homeStation = homeId?.let { 
            stationsRepository.stationById(it)?.toResult(isFavorite = true) 
        }
        
        val workStation = workId?.let { 
            stationsRepository.stationById(it)?.toResult(isFavorite = true) 
        }
        
        return FavoritesListResult(
            favorites = favoriteStations,
            homeStation = homeStation,
            workStation = workStation,
            totalCount = favoriteStations.size
        )
    }
    
    private fun com.gcaguilar.biciradar.core.Station.toResult(isFavorite: Boolean): StationResult {
        return StationResult(
            stationId = id,
            name = name,
            bikesAvailable = bikesAvailable,
            slotsAvailable = slotsFree,
            distance = distanceMeters.toDouble(),
            isFavorite = isFavorite
        )
    }
}