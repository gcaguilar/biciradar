package com.gcaguilar.biciradar.appfunctions.parameters

/**
 * Parameters for the GetStationStatus app function.
 *
 * @param stationId The unique identifier of the station
 * @param detailLevel Level of detail requested (BASIC or FULL)
 */
data class GetStationStatusParams(
    val stationId: String,
    val detailLevel: DetailLevel = DetailLevel.BASIC
)