package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.AssistantIntentResolver
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.StationsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal data class ShortcutsUiState(
  val latestAnswer: String? = null,
  val searchRadiusMeters: Int = 0,
)

internal class ShortcutsViewModel(
  private val assistantIntentResolver: AssistantIntentResolver,
  private val stationsRepository: StationsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val settingsRepository: SettingsRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(
    ShortcutsUiState(searchRadiusMeters = settingsRepository.currentSearchRadiusMeters()),
  )
  val uiState: StateFlow<ShortcutsUiState> = _uiState.asStateFlow()

  private var resolveJob: Job? = null

  init {
    viewModelScope.launch {
      settingsRepository.searchRadiusMeters.collect { radius ->
        _uiState.value = _uiState.value.copy(searchRadiusMeters = radius)
      }
    }
  }

  fun resolveInitialAction(action: AssistantAction) {
    resolveJob?.cancel()
    resolveJob = viewModelScope.launch {
      val resolution = assistantIntentResolver.resolve(
        action = action,
        stationsState = stationsRepository.state.value,
        favoriteIds = favoritesRepository.favoriteIds.value,
        searchRadiusMeters = settingsRepository.currentSearchRadiusMeters(),
      )
      _uiState.value = _uiState.value.copy(latestAnswer = resolution.spokenResponse)
    }
  }
}
