package com.gcaguilar.biciradar.appfunctions.parameters

import androidx.appfunctions.AppFunctionParameter

@AppFunctionParameter
data class FindNearbyStationParams(
    val preference: StationPreference = StationPreference.ANY,
    val maxDistance: Int? = null
)
