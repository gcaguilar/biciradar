package com.gcaguilar.biciradar.appfunctions.results

import androidx.appfunctions.AppFunctionResult
import androidx.appfunctions.AppFunctionUiPresentation

@AppFunctionResult
data class FavoritesListResult(
    val favorites: List<StationResult>,
    val homeStation: StationResult?,
    val workStation: StationResult?,
    val totalCount: Int,
    @AppFunctionUiPresentation
    val uiPresentation: FavoritesListUi? = null
)

@AppFunctionUiPresentation
data class FavoritesListUi(
    val title: String,
    val subtitle: String,
    val emptyStateMessage: String? = null
)