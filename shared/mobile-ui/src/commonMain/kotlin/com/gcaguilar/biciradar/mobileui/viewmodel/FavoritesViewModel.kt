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
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.findStationMatchingQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

  private val searchQuery = MutableStateFlow("")
  private val cityRulesState: StateFlow<FavoritesCityRulesState> = combine(
    settingsRepository.selectedCity,
    savedPlaceAlertsRepository.rules,
  ) { selectedCity, alertRules ->
    FavoritesCityRulesState(
      selectedCity = selectedCity,
      savedPlaceAlertRules = alertRules,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = FavoritesCityRulesState(
      selectedCity = settingsRepository.selectedCity.value,
      savedPlaceAlertRules = savedPlaceAlertsRepository.rules.value,
    ),
  )

  private val relationState: StateFlow<FavoritesRelationState> = combine(
    stationsRepository.state,
    favoritesRepository.favoriteIds,
    favoritesRepository.homeStationId,
    favoritesRepository.workStationId,
    cityRulesState,
  ) { stationsState: StationsState,
      favoriteIds: Set<String>,
      homeStationId: String?,
      workStationId: String?,
      cityRulesState: FavoritesCityRulesState ->
    FavoritesRelationState(
      stations = stationsState.stations,
      favoriteIds = favoriteIds,
      homeStationId = homeStationId,
      workStationId = workStationId,
      selectedCity = cityRulesState.selectedCity,
      savedPlaceAlertRules = cityRulesState.savedPlaceAlertRules,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = buildRelationState(
      stations = stationsRepository.state.value.stations,
      favoriteIds = favoritesRepository.favoriteIds.value,
      homeStationId = favoritesRepository.homeStationId.value,
      workStationId = favoritesRepository.workStationId.value,
      cityRulesState = cityRulesState.value,
    ),
  )

  val uiState: StateFlow<FavoritesUiState> = combine(relationState, searchQuery) { relation, query ->
    relation.toUiState(query)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = relationState.value.toUiState(searchQuery.value),
  )

  fun onSearchQueryChange(query: String) {
    searchQuery.value = query
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

  private fun buildRelationState(
    stations: List<Station>,
    favoriteIds: Set<String>,
    homeStationId: String?,
    workStationId: String?,
    cityRulesState: FavoritesCityRulesState,
  ): FavoritesRelationState = FavoritesRelationState(
    stations = stations,
    favoriteIds = favoriteIds,
    homeStationId = homeStationId,
    workStationId = workStationId,
    selectedCity = cityRulesState.selectedCity,
    savedPlaceAlertRules = cityRulesState.savedPlaceAlertRules,
  )

  private fun FavoritesRelationState.toUiState(query: String): FavoritesUiState = FavoritesUiState(
    allStations = stations,
    favoriteStations = visibleFavoriteStations(
      stations = stations,
      favoriteIds = favoriteIds,
      homeStationId = homeStationId,
      workStationId = workStationId,
    ),
    homeStation = stations.find { it.id == homeStationId },
    workStation = stations.find { it.id == workStationId },
    searchQuery = query,
    assignmentCandidate = findStationMatchingQuery(stations, query),
    savedPlaceAlertsCityId = selectedCity.id,
    savedPlaceAlertRules = savedPlaceAlertRules,
  )
}

internal data class FavoritesRelationState(
  val stations: List<Station>,
  val favoriteIds: Set<String>,
  val homeStationId: String?,
  val workStationId: String?,
  val selectedCity: City,
  val savedPlaceAlertRules: List<SavedPlaceAlertRule>,
)

internal data class FavoritesCityRulesState(
  val selectedCity: City,
  val savedPlaceAlertRules: List<SavedPlaceAlertRule>,
)
