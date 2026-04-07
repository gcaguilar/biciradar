package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.AssistantIntentResolver
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.StationsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class ShortcutsUiState(
  val latestAnswer: String? = null,
  val searchRadiusMeters: Int = 0,
)

private data class ShortcutsRuntimeState(
  val resolveRequestVersion: Long = 0L,
)

internal class ShortcutsViewModel(
  private val assistantIntentResolver: AssistantIntentResolver,
  private val stationsRepository: StationsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val settingsRepository: SettingsRepository,
) : ViewModel() {

  private val latestAnswer = MutableStateFlow<String?>(null)
  val uiState: StateFlow<ShortcutsUiState> = combine(
    latestAnswer,
    settingsRepository.searchRadiusMeters,
  ) { answer, radius ->
    ShortcutsUiState(
      latestAnswer = answer,
      searchRadiusMeters = radius,
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.Eagerly,
    ShortcutsUiState(searchRadiusMeters = settingsRepository.currentSearchRadiusMeters()),
  )
  private val runtimeState = MutableStateFlow(ShortcutsRuntimeState())

  fun resolveInitialAction(action: AssistantAction) {
    val requestVersion = runtimeState.value.resolveRequestVersion + 1
    runtimeState.update { it.copy(resolveRequestVersion = requestVersion) }
    viewModelScope.launch {
      val resolution = assistantIntentResolver.resolve(
        action = action,
        stationsState = stationsRepository.state.value,
        favoriteIds = favoritesRepository.favoriteIds.value,
        searchRadiusMeters = settingsRepository.currentSearchRadiusMeters(),
      )
      if (runtimeState.value.resolveRequestVersion == requestVersion) {
        latestAnswer.update { resolution.spokenResponse }
      }
    }
  }
}
