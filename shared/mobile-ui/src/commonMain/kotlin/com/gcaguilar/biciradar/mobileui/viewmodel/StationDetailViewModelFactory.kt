package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.DatosBiziApi
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository
import com.gcaguilar.biciradar.core.SettingsRepository

class StationDetailViewModelFactory(
  private val favoritesRepository: FavoritesRepository,
  private val settingsRepository: SettingsRepository,
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
  private val datosBiziApi: DatosBiziApi,
  private val routeLauncher: RouteLauncher,
) {
  fun create(stationId: String): StationDetailViewModel = StationDetailViewModel(
    stationId = stationId,
    favoritesRepository = favoritesRepository,
    settingsRepository = settingsRepository,
    savedPlaceAlertsRepository = savedPlaceAlertsRepository,
    datosBiziApi = datosBiziApi,
    routeLauncher = routeLauncher,
  )
}
