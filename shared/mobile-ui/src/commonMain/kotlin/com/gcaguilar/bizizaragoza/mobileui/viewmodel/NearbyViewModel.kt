package com.gcaguilar.bizizaragoza.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.bizizaragoza.core.FavoritesRepository
import com.gcaguilar.bizizaragoza.core.NearbyStationSelection
import com.gcaguilar.bizizaragoza.core.RouteLauncher
import com.gcaguilar.bizizaragoza.core.Station
import com.gcaguilar.bizizaragoza.core.StationsRepository
import com.gcaguilar.bizizaragoza.core.StationsState
import com.gcaguilar.bizizaragoza.core.selectNearbyStation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class NearbyUiState(
  val stations: List<Station> = emptyList(),
  val favoriteIds: Set<String> = emptySet(),
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val refreshCountdownSeconds: Int = 0,
  val nearestSelection: NearbyStationSelection? = null,
  val searchRadiusMeters: Int = 500,
)

class NearbyViewModel(
  private val stationsRepository: StationsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val routeLauncher: RouteLauncher,
  private val searchRadiusMeters: Int,
) : ViewModel() {

  private val _uiState = MutableStateFlow(NearbyUiState())
  val uiState: StateFlow<NearbyUiState> = _uiState.asStateFlow()

  val stationsState: StateFlow<StationsState> = stationsRepository.state

  /** Set to true while the Nearby screen is visible; false when it goes off-screen. */
  private val _isActive = MutableStateFlow(false)

  init {
    viewModelScope.launch {
      stationsRepository.state.collect { state ->
        _uiState.value = _uiState.value.copy(
          stations = state.stations,
          isLoading = state.isLoading,
          errorMessage = state.errorMessage,
          nearestSelection = selectNearbyStation(state.stations, searchRadiusMeters),
        )
      }
    }

    viewModelScope.launch {
      favoritesRepository.favoriteIds.collect { ids ->
        _uiState.value = _uiState.value.copy(favoriteIds = ids)
      }
    }

    viewModelScope.launch {
      val intervalSeconds = 300
      while (true) {
        // Suspend until the screen becomes active
        _isActive.first { it }
        for (remaining in intervalSeconds downTo 1) {
          // If screen goes inactive mid-countdown, break and wait again
          if (!_isActive.value) break
          _uiState.value = _uiState.value.copy(refreshCountdownSeconds = remaining)
          delay(1_000)
        }
        if (!_isActive.value) {
          _uiState.value = _uiState.value.copy(refreshCountdownSeconds = 0)
          continue
        }
        _uiState.value = _uiState.value.copy(refreshCountdownSeconds = 0)
        val ids = stationsRepository.state.value.stations.take(20).map { it.id }
        stationsRepository.refreshAvailability(ids)
      }
    }
  }

  /** Called by the composable when the Nearby screen enters/leaves the composition. */
  fun setActive(active: Boolean) {
    _isActive.value = active
  }

  fun onStationSelected(station: Station) {
  }

  fun onRetry() {
    viewModelScope.launch {
      stationsRepository.loadIfNeeded()
    }
  }

  fun onRefresh() {
    viewModelScope.launch {
      stationsRepository.forceRefresh()
    }
  }

  fun onFavoriteToggle(station: Station) {
    viewModelScope.launch {
      favoritesRepository.toggle(station.id)
    }
  }

  fun onQuickRoute(station: Station) {
    routeLauncher.launch(station)
  }
}

