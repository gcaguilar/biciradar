package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationHourlyPattern
import com.gcaguilar.biciradar.mobileui.usecases.StationDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StationDetailUiState(
  val isFavorite: Boolean = false,
  val isHomeStation: Boolean = false,
  val isWorkStation: Boolean = false,
  val supportsUsagePatterns: Boolean = false,
  val savedPlaceAlertsCityId: String = City.ZARAGOZA.id,
  val savedPlaceAlertRules: List<SavedPlaceAlertRule> = emptyList(),
  val patterns: List<StationHourlyPattern> = emptyList(),
  val patternsLoading: Boolean = true,
  val patternsError: Boolean = false,
)

class StationDetailViewModel(
  private val stationId: String,
  private val stationDetailUseCase: StationDetailUseCase,
) : ViewModel() {

  private val patternState = MutableStateFlow(PatternState())
  private val cityRulesState: StateFlow<StationDetailCityRulesState> = combine(
    stationDetailUseCase.selectedCity,
    stationDetailUseCase.alertRules,
  ) { selectedCity, alertRules ->
    StationDetailCityRulesState(
      selectedCity = selectedCity,
      savedPlaceAlertRules = alertRules,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = StationDetailCityRulesState(
      selectedCity = stationDetailUseCase.selectedCity.value,
      savedPlaceAlertRules = stationDetailUseCase.alertRules.value,
    ),
  )

  private val relationState: StateFlow<StationDetailRelationState> = combine(
    stationDetailUseCase.favoriteIds,
    stationDetailUseCase.homeStationId,
    stationDetailUseCase.workStationId,
    cityRulesState,
  ) { favoriteIds, homeStationId, workStationId, cityRulesState ->
    StationDetailRelationState(
      isFavorite = stationId in favoriteIds,
      isHomeStation = homeStationId == stationId,
      isWorkStation = workStationId == stationId,
      supportsUsagePatterns = cityRulesState.selectedCity.supportsUsagePatterns,
      savedPlaceAlertsCityId = cityRulesState.selectedCity.id,
      savedPlaceAlertRules = cityRulesState.savedPlaceAlertRules,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = buildRelationState(
      favoriteIds = stationDetailUseCase.favoriteIds.value,
      homeStationId = stationDetailUseCase.homeStationId.value,
      workStationId = stationDetailUseCase.workStationId.value,
      cityRulesState = cityRulesState.value,
    ),
  )

  val uiState: StateFlow<StationDetailUiState> = combine(relationState, patternState) { relation, patterns ->
    relation.toUiState(patterns)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = relationState.value.toUiState(patternState.value),
  )

  init {
    refreshPatterns()
  }

  fun refreshPatterns() {
    viewModelScope.launch {
      patternState.update { it.copy(loading = true, error = false) }
      val result = stationDetailUseCase.fetchStationPatterns(stationId)
      val patterns = result.getOrNull()
      patternState.update {
        it.copy(
          patterns = patterns.orEmpty(),
          loading = false,
          error = patterns == null,
        )
      }
    }
  }

  fun onToggleFavorite() {
    viewModelScope.launch {
      stationDetailUseCase.toggleFavorite(stationId)
    }
  }

  fun onToggleHome() {
    viewModelScope.launch {
      stationDetailUseCase.toggleHomeStation(stationId)
    }
  }

  fun onToggleWork() {
    viewModelScope.launch {
      stationDetailUseCase.toggleWorkStation(stationId)
    }
  }

  fun onRoute(station: Station) {
    stationDetailUseCase.launchRoute(station)
  }

  fun onUpsertSavedPlaceAlert(target: SavedPlaceAlertTarget, condition: SavedPlaceAlertCondition) {
    viewModelScope.launch {
      stationDetailUseCase.upsertAlertRule(target, condition)
    }
  }

  fun onRemoveSavedPlaceAlertForTarget(target: SavedPlaceAlertTarget) {
    viewModelScope.launch {
      stationDetailUseCase.removeAlertRuleForTarget(target)
    }
  }

  private fun buildRelationState(
    favoriteIds: Set<String>,
    homeStationId: String?,
    workStationId: String?,
    cityRulesState: StationDetailCityRulesState,
  ): StationDetailRelationState = StationDetailRelationState(
    isFavorite = stationId in favoriteIds,
    isHomeStation = homeStationId == stationId,
    isWorkStation = workStationId == stationId,
    supportsUsagePatterns = cityRulesState.selectedCity.supportsUsagePatterns,
    savedPlaceAlertsCityId = cityRulesState.selectedCity.id,
    savedPlaceAlertRules = cityRulesState.savedPlaceAlertRules,
  )

  private fun StationDetailRelationState.toUiState(patternsState: PatternState): StationDetailUiState = StationDetailUiState(
    isFavorite = isFavorite,
    isHomeStation = isHomeStation,
    isWorkStation = isWorkStation,
    supportsUsagePatterns = supportsUsagePatterns,
    savedPlaceAlertsCityId = savedPlaceAlertsCityId,
    savedPlaceAlertRules = savedPlaceAlertRules,
    patterns = patternsState.patterns,
    patternsLoading = patternsState.loading,
    patternsError = patternsState.error,
  )
}

internal data class PatternState(
  val patterns: List<StationHourlyPattern> = emptyList(),
  val loading: Boolean = true,
  val error: Boolean = false,
)

internal data class StationDetailRelationState(
  val isFavorite: Boolean,
  val isHomeStation: Boolean,
  val isWorkStation: Boolean,
  val supportsUsagePatterns: Boolean,
  val savedPlaceAlertsCityId: String,
  val savedPlaceAlertRules: List<SavedPlaceAlertRule>,
)

internal data class StationDetailCityRulesState(
  val selectedCity: City,
  val savedPlaceAlertRules: List<SavedPlaceAlertRule>,
)
