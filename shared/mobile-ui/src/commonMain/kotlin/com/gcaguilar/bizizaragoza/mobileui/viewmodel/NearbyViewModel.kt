package com.gcaguilar.bizizaragoza.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.bizizaragoza.core.FavoritesRepository
import com.gcaguilar.bizizaragoza.core.NearbyStationSelection
import com.gcaguilar.bizizaragoza.core.RouteLauncher
import com.gcaguilar.bizizaragoza.core.Station
import com.gcaguilar.bizizaragoza.core.StationsRepository
import com.gcaguilar.bizizaragoza.core.StationsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

  init {
    viewModelScope.launch {
      stationsRepository.state.collect { state ->
        _uiState.value = _uiState.value.copy(
          stations = state.stations,
          isLoading = state.isLoading,
          errorMessage = state.errorMessage,
        )
      }
    }

    viewModelScope.launch {
      favoritesRepository.favoriteIds.collect { ids ->
        _uiState.value = _uiState.value.copy(favoriteIds = ids)
      }
    }
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
