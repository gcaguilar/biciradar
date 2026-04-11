package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.AssistantIntentResolver
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.StationsRepository

internal class ShortcutsViewModelFactory(
  private val assistantIntentResolver: AssistantIntentResolver,
  private val stationsRepository: StationsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val settingsRepository: SettingsRepository,
) {
  fun create(): ShortcutsViewModel =
    ShortcutsViewModel(
      assistantIntentResolver = assistantIntentResolver,
      stationsRepository = stationsRepository,
      favoritesRepository = favoritesRepository,
      settingsRepository = settingsRepository,
    )
}
