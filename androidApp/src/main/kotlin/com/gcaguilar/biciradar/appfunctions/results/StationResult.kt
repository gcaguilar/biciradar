package com.gcaguilar.biciradar.appfunctions.results

import androidx.appfunctions.AppFunctionResult
import androidx.appfunctions.AppFunctionUiPresentation

@AppFunctionResult
data class StationResult(
    val stationId: String,
    val name: String,
    val bikesAvailable: Int,
    val slotsAvailable: Int,
    val distance: Double,
    val isFavorite: Boolean,
    @AppFunctionUiPresentation
    val uiPresentation: StationCardUi? = null
)

@AppFunctionUiPresentation
data class StationCardUi(
    val title: String,
    val subtitle: String,
    val primaryAction: String,
    val imageUrl: String? = null
)
