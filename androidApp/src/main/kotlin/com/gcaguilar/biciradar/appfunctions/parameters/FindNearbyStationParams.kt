package com.gcaguilar.biciradar.appfunctions.parameters

/**
 * Parameters for the FindNearbyStation app function.
 *
 * @param preference Filter preference for stations (ANY, WITH_BIKES, WITH_SLOTS)
 * @param maxDistance Optional maximum distance in meters to search
 */
data class FindNearbyStationParams(
    val preference: StationPreference = StationPreference.ANY,
    val maxDistance: Int? = null
)