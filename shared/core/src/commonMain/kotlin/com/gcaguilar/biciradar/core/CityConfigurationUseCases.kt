package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.Inject

@Inject
class IsCityConfiguredUseCase(
  private val settingsRepository: SettingsRepository,
) {
  suspend fun execute(): Boolean = settingsRepository.isCityConfirmedPersisted()
}
