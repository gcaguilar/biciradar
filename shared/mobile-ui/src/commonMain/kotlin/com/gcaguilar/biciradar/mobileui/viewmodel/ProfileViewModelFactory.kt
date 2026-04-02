package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.ChangeCityUseCase
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.core.PermissionPrompter
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.StationsRepository

class ProfileViewModelFactory(
  private val settingsRepository: SettingsRepository,
  private val stationsRepository: StationsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val permissionPrompter: PermissionPrompter,
  private val localNotifier: LocalNotifier,
) {
  fun create(): ProfileViewModel = ProfileViewModel(
    settingsRepository = settingsRepository,
    favoritesRepository = favoritesRepository,
    changeCityUseCase = ChangeCityUseCase(
      settingsRepository = settingsRepository,
      favoritesRepository = favoritesRepository,
      stationsRepository = stationsRepository,
    ),
    permissionPrompter = permissionPrompter,
    localNotifier = localNotifier,
  )
}
