package com.gcaguilar.bizizaragoza.mobileui.viewmodel

import com.gcaguilar.bizizaragoza.core.SettingsRepository
import com.gcaguilar.bizizaragoza.core.StationsRepository

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
