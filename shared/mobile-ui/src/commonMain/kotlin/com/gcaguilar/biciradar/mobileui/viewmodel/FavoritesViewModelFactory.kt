package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.mobileui.usecases.FavoritesManagementUseCase
import com.gcaguilar.biciradar.mobileui.usecases.RouteLaunchUseCase
import com.gcaguilar.biciradar.mobileui.usecases.SavedPlaceAlertsUseCase

class FavoritesViewModelFactory(
  private val favoritesRepository: FavoritesRepository,
  private val stationsRepository: StationsRepository,
  private val settingsRepository: SettingsRepository,
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
  private val routeLauncher: RouteLauncher,
) {
  fun create(): FavoritesViewModel {
    val favoritesManagementUseCase = FavoritesManagementUseCase(
      favoritesRepository = favoritesRepository,
      stationsRepository = stationsRepository,
      settingsRepository = settingsRepository,
    )
    val savedPlaceAlertsUseCase = SavedPlaceAlertsUseCase(
      savedPlaceAlertsRepository = savedPlaceAlertsRepository,
    )
    val routeLaunchUseCase = RouteLaunchUseCase(
      routeLauncher = routeLauncher,
    )

    return FavoritesViewModel(
      favoritesManagementUseCase = favoritesManagementUseCase,
      savedPlaceAlertsUseCase = savedPlaceAlertsUseCase,
      routeLaunchUseCase = routeLaunchUseCase,
    )
  }
}
