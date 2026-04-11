package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.EnvironmentalRepository
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.StationsRepository

internal class MapEnvironmentalViewModelFactory(
  private val environmentalRepository: EnvironmentalRepository,
  private val settingsRepository: SettingsRepository,
  private val stationsRepository: StationsRepository,
  private val favoritesRepository: FavoritesRepository,
) {
  fun create(): MapEnvironmentalViewModel =
    MapEnvironmentalViewModel(
      environmentalRepository = environmentalRepository,
      settingsRepository = settingsRepository,
      stationsRepository = stationsRepository,
      favoritesRepository = favoritesRepository,
    )
}
