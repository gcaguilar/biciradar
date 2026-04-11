package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.StationsRepository

class NearbyViewModelFactory(
  private val stationsRepository: StationsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val settingsRepository: SettingsRepository,
  private val routeLauncher: RouteLauncher,
) {
  fun create(): NearbyViewModel =
    NearbyViewModel(
      stationsRepository = stationsRepository,
      favoritesRepository = favoritesRepository,
      settingsRepository = settingsRepository,
      routeLauncher = routeLauncher,
    )
}
