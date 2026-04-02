package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.AssistantIntentResolver
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class ShortcutsUiState(
  val latestAnswer: String? = null,
)

internal class ShortcutsViewModel(
  private val assistantIntentResolver: AssistantIntentResolver,
) : ViewModel() {

  private val _uiState = MutableStateFlow(ShortcutsUiState())
  val uiState: StateFlow<ShortcutsUiState> = _uiState.asStateFlow()

  private var resolveJob: Job? = null

  fun resolveInitialAction(
    action: AssistantAction,
    stations: List<Station>,
    favoriteIds: Set<String>,
    searchRadiusMeters: Int,
  ) {
    resolveJob?.cancel()
    resolveJob = viewModelScope.launch {
      val resolution = assistantIntentResolver.resolve(
        action = action,
        stationsState = StationsState(stations = stations),
        favoriteIds = favoriteIds,
        searchRadiusMeters = searchRadiusMeters,
      )
      _uiState.value = ShortcutsUiState(latestAnswer = resolution.spokenResponse)
    }
  }
}
