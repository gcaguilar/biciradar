package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class FavoritesUiState(
  val allStations: List<Station> = emptyList(),
  val favoriteStations: List<Station> = emptyList(),
  val homeStation: Station? = null,
  val workStation: Station? = null,
  val searchQuery: String = "",
  val assignmentCandidate: Station? = null,
)

class FavoritesViewModel(
  private val favoritesRepository: FavoritesRepository,
  private val stationsRepository: StationsRepository,
  private val routeLauncher: RouteLauncher,
) : ViewModel() {

  private val _uiState = MutableStateFlow(FavoritesUiState())
  val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      combine(
        stationsRepository.state,
        favoritesRepository.favoriteIds,
        favoritesRepository.homeStationId,
        favoritesRepository.workStationId,
      ) { stationsState, favoriteIds, homeId, workId ->
        val allStations = stationsState.stations
        val favoriteStations = visibleFavoriteStations(
          stations = allStations,
          favoriteIds = favoriteIds,
          homeStationId = homeId,
          workStationId = workId,
        )
        val homeStation = allStations.find { it.id == homeId }
        val workStation = allStations.find { it.id == workId }

        _uiState.value.copy(
          allStations = allStations,
          favoriteStations = favoriteStations,
          homeStation = homeStation,
          workStation = workStation,
        )
      }.collect { newState ->
        _uiState.value = newState
      }
    }
  }

  fun onSearchQueryChange(query: String) {
    val candidate = _uiState.value.allStations.find { 
      it.name.contains(query, ignoreCase = true) 
    }
    _uiState.value = _uiState.value.copy(
      searchQuery = query,
      assignmentCandidate = candidate,
    )
  }

  fun onStationSelected(station: Station) {
  }

  fun onAssignHomeStation(station: Station) {
    viewModelScope.launch {
      favoritesRepository.setHomeStationId(station.id)
    }
  }

  fun onAssignWorkStation(station: Station) {
    viewModelScope.launch {
      favoritesRepository.setWorkStationId(station.id)
    }
  }

  fun onClearHomeStation() {
    viewModelScope.launch {
      favoritesRepository.setHomeStationId(null)
    }
  }

  fun onClearWorkStation() {
    viewModelScope.launch {
      favoritesRepository.setWorkStationId(null)
    }
  }

  fun onRemoveFavorite(station: Station) {
    viewModelScope.launch {
      favoritesRepository.toggle(station.id)
    }
  }

  fun onQuickRoute(station: Station) {
    routeLauncher.launch(station)
  }
}
