package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.StationsRepository

class ProfileViewModelFactory(
  private val settingsRepository: SettingsRepository,
  private val stationsRepository: StationsRepository,
  private val searchRadiusMeters: Int,
) {
  fun create(): ProfileViewModel = ProfileViewModel(
    settingsRepository = settingsRepository,
    stationsRepository = stationsRepository,
    searchRadiusMeters = searchRadiusMeters,
  )
}
