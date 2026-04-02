package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.Inject

@Inject
class ChangeCityUseCase(
  private val settingsRepository: SettingsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val stationsRepository: StationsRepository,
) {
  suspend fun execute(
    city: City,
    clearFavorites: Boolean = true,
    markCityConfirmed: Boolean = false,
  ) {
    settingsRepository.setSelectedCity(city)
    if (markCityConfirmed) {
      settingsRepository.updateOnboardingChecklist { snapshot ->
        if (snapshot.cityConfirmed) snapshot else snapshot.copy(cityConfirmed = true)
      }
    }
    if (clearFavorites) {
      favoritesRepository.clearAll()
    }
    stationsRepository.forceRefresh()
  }
}
