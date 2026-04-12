package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Changes the selected city and triggers a full station refresh.
 *
 * If [city] is already selected, this is a no-op.
 * Does NOT clear favorites — use [ChangeCityUseCase] for the onboarding
 * flow that also clears user data.
 */
@SingleIn(AppScope::class)
@Inject
class UpdateSelectedCity(
  private val settingsRepository: SettingsRepository,
  private val refreshStationDataIfNeeded: RefreshStationDataIfNeeded,
) {
  suspend fun execute(city: City) {
    if (settingsRepository.currentSelectedCity().id == city.id) return
    settingsRepository.setSelectedCity(city)
    refreshStationDataIfNeeded.execute(forceRefresh = true)
  }
}
