package com.gcaguilar.biciradar.appfunctions.parameters

import androidx.appfunctions.AppFunctionParameter

@AppFunctionParameter
data class GetStationStatusParams(
    val stationId: String,
    val detailLevel: DetailLevel = DetailLevel.BASIC
)
