package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.FavoriteCategory
import com.gcaguilar.biciradar.core.FavoriteCategoryIds
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.findStationMatchingQuery
import com.gcaguilar.biciradar.mobileui.usecases.FavoritesManagementUseCase
import com.gcaguilar.biciradar.mobileui.usecases.RouteLaunchUseCase
import com.gcaguilar.biciradar.mobileui.usecases.SavedPlaceAlertsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
  val categories: List<FavoriteCategory> = emptyList(),
  val stationCategory: Map<String, String> = emptyMap(),
)

class FavoritesViewModel(
  private val favoritesManagementUseCase: FavoritesManagementUseCase,
  private val savedPlaceAlertsUseCase: SavedPlaceAlertsUseCase,
  private val routeLaunchUseCase: RouteLaunchUseCase,
) : ViewModel() {

  private val searchQuery = MutableStateFlow("")
  private val cityRulesState: StateFlow<FavoritesCityRulesState> = combine(
    favoritesManagementUseCase.selectedCity,
    savedPlaceAlertsUseCase.rules,
  ) { selectedCity, alertRules ->
    FavoritesCityRulesState(
      selectedCity = selectedCity,
      savedPlaceAlertRules = alertRules,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = FavoritesCityRulesState(
      selectedCity = favoritesManagementUseCase.selectedCity.value,
      savedPlaceAlertRules = savedPlaceAlertsUseCase.rules.value,
    ),
  )

  private val relationBaseState: StateFlow<FavoritesRelationState> = combine(
    favoritesManagementUseCase.stationsState,
    favoritesManagementUseCase.favoriteIds,
    favoritesManagementUseCase.homeStationId,
    favoritesManagementUseCase.workStationId,
    cityRulesState,
  ) { stationsState, favoriteIds, homeStationId, workStationId, cityRulesState ->
    FavoritesRelationState(
      stations = stationsState.stations,
      favoriteIds = favoriteIds,
      homeStationId = homeStationId,
      workStationId = workStationId,
      categories = emptyList(),
      stationCategory = emptyMap(),
      selectedCity = cityRulesState.selectedCity,
      savedPlaceAlertRules = cityRulesState.savedPlaceAlertRules,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = buildRelationState(
      stations = favoritesManagementUseCase.stationsState.value.stations,
      favoriteIds = favoritesManagementUseCase.favoriteIds.value,
      homeStationId = favoritesManagementUseCase.homeStationId.value,
      workStationId = favoritesManagementUseCase.workStationId.value,
      categories = emptyList(),
      stationCategory = emptyMap(),
      cityRulesState = cityRulesState.value,
    ),
  )

  private val relationState: StateFlow<FavoritesRelationState> = combine(
    relationBaseState,
    favoritesManagementUseCase.categories,
    favoritesManagementUseCase.stationCategory,
  ) { base, categories, stationCategory ->
    base.copy(categories = categories, stationCategory = stationCategory)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = buildRelationState(
      stations = favoritesManagementUseCase.stationsState.value.stations,
      favoriteIds = favoritesManagementUseCase.favoriteIds.value,
      homeStationId = favoritesManagementUseCase.homeStationId.value,
      workStationId = favoritesManagementUseCase.workStationId.value,
      categories = favoritesManagementUseCase.categories.value,
      stationCategory = favoritesManagementUseCase.stationCategory.value,
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
    searchQuery.update { query }
  }

  fun onAssignHomeStation(station: Station) {
    viewModelScope.launch {
      favoritesManagementUseCase.setHomeStationId(station.id)
    }
  }

  fun onAssignWorkStation(station: Station) {
    viewModelScope.launch {
      favoritesManagementUseCase.setWorkStationId(station.id)
    }
  }

  fun onClearHomeStation() {
    viewModelScope.launch {
      if (homeStationId() == null) return@launch
      favoritesManagementUseCase.setHomeStationId(null)
    }
  }

  fun onClearWorkStation() {
    viewModelScope.launch {
      if (workStationId() == null) return@launch
      favoritesManagementUseCase.setWorkStationId(null)
    }
  }

  fun onRemoveFavorite(station: Station) {
    viewModelScope.launch {
      favoritesManagementUseCase.assignStationToCategory(station.id, null)
    }
  }

  fun onToggleFavorite(station: Station) {
    viewModelScope.launch {
      favoritesManagementUseCase.toggleFavorite(station.id)
    }
  }

  fun onCreateCustomCategory(label: String) {
    viewModelScope.launch {
      val id = "custom:${label.lowercase().replace("\\s+".toRegex(), "_")}:${kotlin.random.Random.nextInt(1_000_000)}"
      favoritesManagementUseCase.upsertCategory(id = id, label = label, isSystem = false)
    }
  }

  fun onAssignCandidateToCategory(categoryId: String) {
    val station = uiState.value.assignmentCandidate ?: return
    viewModelScope.launch {
      favoritesManagementUseCase.assignStationToCategory(station.id, categoryId)
    }
  }

  fun onAssignStationToCategory(station: Station, categoryId: String) {
    viewModelScope.launch {
      favoritesManagementUseCase.assignStationToCategory(station.id, categoryId)
    }
  }

  fun onRemoveCustomCategory(categoryId: String) {
    viewModelScope.launch {
      favoritesManagementUseCase.removeCategory(categoryId)
    }
  }

  fun onClearCategoryAssignment(categoryId: String) {
    viewModelScope.launch {
      when (categoryId) {
        FavoriteCategoryIds.HOME -> favoritesManagementUseCase.setHomeStationId(null)
        FavoriteCategoryIds.WORK -> favoritesManagementUseCase.setWorkStationId(null)
        else -> {
          uiState.value.stationCategory
            .filterValues { it == categoryId }
            .keys
            .forEach { stationId ->
              favoritesManagementUseCase.assignStationToCategory(stationId, null)
            }
        }
      }
    }
  }

  fun onQuickRoute(station: Station) {
    routeLaunchUseCase.launchRoute(station)
  }

  fun onUpsertSavedPlaceAlert(target: SavedPlaceAlertTarget, condition: SavedPlaceAlertCondition) {
    viewModelScope.launch {
      savedPlaceAlertsUseCase.upsertRule(target, condition)
    }
  }

  fun onRemoveSavedPlaceAlertForTarget(target: SavedPlaceAlertTarget) {
    viewModelScope.launch {
      savedPlaceAlertsUseCase.removeRuleForTarget(target)
    }
  }

  private fun buildRelationState(
    stations: List<Station>,
    favoriteIds: Set<String>,
    homeStationId: String?,
    workStationId: String?,
    categories: List<FavoriteCategory>,
    stationCategory: Map<String, String>,
    cityRulesState: FavoritesCityRulesState,
  ): FavoritesRelationState = FavoritesRelationState(
    stations = stations,
    favoriteIds = favoriteIds,
    homeStationId = homeStationId,
    workStationId = workStationId,
    categories = categories,
    stationCategory = stationCategory,
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
    categories = categories,
    stationCategory = stationCategory,
  )

  private fun homeStationId(): String? = favoritesManagementUseCase.homeStationId.value
  private fun workStationId(): String? = favoritesManagementUseCase.workStationId.value
}

internal data class FavoritesRelationState(
  val stations: List<Station>,
  val favoriteIds: Set<String>,
  val homeStationId: String?,
  val workStationId: String?,
  val categories: List<FavoriteCategory>,
  val stationCategory: Map<String, String>,
  val selectedCity: City,
  val savedPlaceAlertRules: List<SavedPlaceAlertRule>,
)

internal data class FavoritesCityRulesState(
  val selectedCity: City,
  val savedPlaceAlertRules: List<SavedPlaceAlertRule>,
)
