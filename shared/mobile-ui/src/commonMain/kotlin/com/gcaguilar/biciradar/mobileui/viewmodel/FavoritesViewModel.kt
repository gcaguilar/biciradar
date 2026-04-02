package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.findStationMatchingQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FavoritesUiState(
  val allStations: List<Station> = emptyList(),
  val favoriteStations: List<Station> = emptyList(),
  val homeStation: Station? = null,
  val workStation: Station? = null,
  val searchQuery: String = "",
  val assignmentCandidate: Station? = null,
  val savedPlaceAlertsCityId: String = City.ZARAGOZA.id,
  val savedPlaceAlertRules: List<SavedPlaceAlertRule> = emptyList(),
)

class FavoritesViewModel(
  private val favoritesRepository: FavoritesRepository,
  private val stationsRepository: StationsRepository,
  private val settingsRepository: SettingsRepository,
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
  private val routeLauncher: RouteLauncher,
) : ViewModel() {

  private val _uiState = MutableStateFlow(FavoritesUiState())
  val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

  private var latestAllStations: List<Station> = emptyList()
  private var latestFavoriteIds: Set<String> = emptySet()
  private var latestHomeStationId: String? = null
  private var latestWorkStationId: String? = null
  private var latestSelectedCity: City = City.ZARAGOZA
  private var latestSavedPlaceAlertRules: List<SavedPlaceAlertRule> = emptyList()

  init {
    viewModelScope.launch {
      stationsRepository.state.collect { stationsState ->
        latestAllStations = stationsState.stations
        publishUiState()
      }
    }

    viewModelScope.launch {
      favoritesRepository.favoriteIds.collect { ids ->
        latestFavoriteIds = ids
        publishUiState()
      }
    }

    viewModelScope.launch {
      favoritesRepository.homeStationId.collect { homeStationId ->
        latestHomeStationId = homeStationId
        publishUiState()
      }
    }

    viewModelScope.launch {
      favoritesRepository.workStationId.collect { workStationId ->
        latestWorkStationId = workStationId
        publishUiState()
      }
    }

    viewModelScope.launch {
      settingsRepository.selectedCity.collect { city ->
        latestSelectedCity = city
        publishUiState()
      }
    }

    viewModelScope.launch {
      savedPlaceAlertsRepository.rules.collect { rules ->
        latestSavedPlaceAlertRules = rules
        publishUiState()
      }
    }
  }

  private fun publishUiState() {
    val query = _uiState.value.searchQuery
    _uiState.value = FavoritesUiState(
      allStations = latestAllStations,
      favoriteStations = visibleFavoriteStations(
        stations = latestAllStations,
        favoriteIds = latestFavoriteIds,
        homeStationId = latestHomeStationId,
        workStationId = latestWorkStationId,
      ),
      homeStation = latestAllStations.find { it.id == latestHomeStationId },
      workStation = latestAllStations.find { it.id == latestWorkStationId },
      searchQuery = query,
      assignmentCandidate = findStationMatchingQuery(latestAllStations, query),
      savedPlaceAlertsCityId = latestSelectedCity.id,
      savedPlaceAlertRules = latestSavedPlaceAlertRules,
    )
  }

  fun onSearchQueryChange(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
    publishUiState()
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

  fun onUpsertSavedPlaceAlert(target: SavedPlaceAlertTarget, condition: SavedPlaceAlertCondition) {
    viewModelScope.launch {
      savedPlaceAlertsRepository.upsertRule(target, condition)
    }
  }

  fun onRemoveSavedPlaceAlertForTarget(target: SavedPlaceAlertTarget) {
    viewModelScope.launch {
      savedPlaceAlertsRepository.removeRuleForTarget(target)
    }
  }
}
