package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.Inject

@Inject
class ChangeCityUseCase(
  private val settingsRepository: SettingsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val stationsRepository: StationsRepository,
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository? = null,
) {
  suspend fun execute(
    city: City,
    clearFavorites: Boolean = true,
  ) {
    val previousCityId = settingsRepository.currentSelectedCity().id
    settingsRepository.updateOnboardingChecklist { snapshot ->
      if (snapshot.cityConfirmed) snapshot else snapshot.copy(cityConfirmed = true)
    }
    settingsRepository.setSelectedCity(city)
    if (clearFavorites) {
      favoritesRepository.clearAll()
      if (previousCityId != city.id) {
        savedPlaceAlertsRepository?.removeRulesForCity(previousCityId)
      }
    }
    stationsRepository.forceRefresh()
  }
}
