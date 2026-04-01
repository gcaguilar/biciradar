package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.StationsRepository

class FavoritesViewModelFactory(
  private val favoritesRepository: FavoritesRepository,
  private val stationsRepository: StationsRepository,
  private val settingsRepository: SettingsRepository,
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
  private val routeLauncher: RouteLauncher,
) {
  fun create(): FavoritesViewModel = FavoritesViewModel(
    favoritesRepository = favoritesRepository,
    stationsRepository = stationsRepository,
    settingsRepository = settingsRepository,
    savedPlaceAlertsRepository = savedPlaceAlertsRepository,
    routeLauncher = routeLauncher,
  )
}
