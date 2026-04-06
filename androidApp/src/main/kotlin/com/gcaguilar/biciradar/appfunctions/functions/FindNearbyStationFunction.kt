package com.gcaguilar.biciradar.appfunctions.functions

import com.gcaguilar.biciradar.appfunctions.parameters.FindNearbyStationParams
import com.gcaguilar.biciradar.appfunctions.parameters.StationPreference
import com.gcaguilar.biciradar.appfunctions.results.StationResult
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.LocationProvider
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * App Function that finds nearby stations based on user location and preferences.
 */
class FindNearbyStationFunction @Inject constructor(
    private val stationsRepository: StationsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val locationProvider: LocationProvider
) {
    suspend fun execute(params: FindNearbyStationParams): List<StationResult> {
        val userLocation = locationProvider.getCurrentLocation()
            ?: return emptyList()
        
        val favoriteIds = favoritesRepository.favoriteIds.value
        
        val stations = stationsRepository.state.value.stations
            .ifEmpty { return emptyList() }
            .map { it.copyWithDistance(userLocation) }
            .filter { it.matchesPreference(params.preference) }
            .let { list ->
                params.maxDistance?.let { maxDist ->
                    list.filter { it.distanceMeters <= maxDist }
                } ?: list
            }
            .sortedBy { it.distanceMeters }
        
        return stations.map { it.toResult(favoriteIds.contains(it.id)) }
    }
    
    private fun Station.matchesPreference(preference: StationPreference): Boolean {
        return when (preference) {
            StationPreference.ANY -> true
            StationPreference.WITH_BIKES -> bikesAvailable > 0
            StationPreference.WITH_SLOTS -> slotsFree > 0
        }
    }
    
    private fun Station.copyWithDistance(userLocation: GeoPoint): Station {
        return this.copy(
            distanceMeters = calculateDistance(userLocation, location)
        )
    }
    
    private fun Station.toResult(isFavorite: Boolean): StationResult {
        return StationResult(
            stationId = id,
            name = name,
            bikesAvailable = bikesAvailable,
            slotsAvailable = slotsFree,
            distance = distanceMeters.toDouble(),
            isFavorite = isFavorite
        )
    }
    
    private fun calculateDistance(origin: GeoPoint, destination: GeoPoint): Int {
        val earthRadiusM = 6_371_000.0
        val dLat = Math.toRadians(destination.latitude - origin.latitude)
        val dLon = Math.toRadians(destination.longitude - origin.longitude)
        val lat1 = Math.toRadians(origin.latitude)
        val lat2 = Math.toRadians(destination.latitude)
        
        val a = sin(dLat / 2).pow(2.0) +
                sin(dLon / 2).pow(2.0) * cos(lat1) * cos(lat2)
        val c = 2 * asin(sqrt(a))
        
        return (earthRadiusM * c).roundToInt()
    }
}