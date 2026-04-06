package com.gcaguilar.biciradar.appfunctions.parameters

import androidx.appfunctions.AppFunctionParameter

/**
 * Represents the user's preference for filtering nearby stations.
 * Used as a parameter in FindNearbyStationFunction to filter stations
 * based on bike or slot availability.
 */
@AppFunctionParameter
enum class StationPreference {
    /** No preference - return any nearby station */
    ANY,

    /** Only return stations that have bikes available */
    WITH_BIKES,

    /** Only return stations that have free slots available */
    WITH_SLOTS
}
