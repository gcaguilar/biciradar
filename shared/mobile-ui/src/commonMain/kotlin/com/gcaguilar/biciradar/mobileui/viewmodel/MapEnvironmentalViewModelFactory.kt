package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.EnvironmentalRepository
import com.gcaguilar.biciradar.core.SettingsRepository

internal class MapEnvironmentalViewModelFactory(
  private val environmentalRepository: EnvironmentalRepository,
  private val settingsRepository: SettingsRepository,
) {
  fun create(): MapEnvironmentalViewModel = MapEnvironmentalViewModel(
    environmentalRepository = environmentalRepository,
    settingsRepository = settingsRepository,
  )
}
