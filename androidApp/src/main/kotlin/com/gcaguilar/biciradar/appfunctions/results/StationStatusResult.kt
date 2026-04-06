package com.gcaguilar.biciradar.appfunctions.results

import androidx.appfunctions.AppFunctionResult
import androidx.appfunctions.AppFunctionUiPresentation

@AppFunctionResult
data class StationStatusResult(
    val stationId: String,
    val name: String,
    val address: String,
    val bikesAvailable: Int,
    val slotsAvailable: Int,
    val isOpen: Boolean,
    val lastUpdated: Long,
    val isFavorite: Boolean,
    @AppFunctionUiPresentation
    val uiPresentation: StationStatusCardUi? = null
)

@AppFunctionUiPresentation
data class StationStatusCardUi(
    val title: String,
    val statusText: String,
    val bikesText: String,
    val slotsText: String,
    val primaryAction: String
)
