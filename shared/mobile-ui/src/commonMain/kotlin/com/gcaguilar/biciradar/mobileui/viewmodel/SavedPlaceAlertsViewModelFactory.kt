package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository

class SavedPlaceAlertsViewModelFactory(
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
) {
  fun create(): SavedPlaceAlertsViewModel =
    SavedPlaceAlertsViewModel(
      savedPlaceAlertsRepository = savedPlaceAlertsRepository,
    )
}
