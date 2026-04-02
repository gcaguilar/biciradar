package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.DatosBiziApi
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationHourlyPattern
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
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
  private val favoritesRepository: FavoritesRepository,
  private val settingsRepository: SettingsRepository,
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
  private val datosBiziApi: DatosBiziApi,
  private val routeLauncher: RouteLauncher,
) : ViewModel() {

  private val patternState = MutableStateFlow(PatternState())
  private val cityRulesState: StateFlow<StationDetailCityRulesState> = combine(
    settingsRepository.selectedCity,
    savedPlaceAlertsRepository.rules,
  ) { selectedCity, alertRules ->
    StationDetailCityRulesState(
      selectedCity = selectedCity,
      savedPlaceAlertRules = alertRules,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = StationDetailCityRulesState(
      selectedCity = settingsRepository.selectedCity.value,
      savedPlaceAlertRules = savedPlaceAlertsRepository.rules.value,
    ),
  )

  private val relationState: StateFlow<StationDetailRelationState> = combine(
    favoritesRepository.favoriteIds,
    favoritesRepository.homeStationId,
    favoritesRepository.workStationId,
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
      favoriteIds = favoritesRepository.favoriteIds.value,
      homeStationId = favoritesRepository.homeStationId.value,
      workStationId = favoritesRepository.workStationId.value,
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
      patternState.value = patternState.value.copy(loading = true, error = false)
      val patterns = runCatching { datosBiziApi.fetchPatterns(stationId) }.getOrNull()
      patternState.value = patternState.value.copy(
        patterns = patterns.orEmpty(),
        loading = false,
        error = patterns == null,
      )
    }
  }

  fun onToggleFavorite() {
    viewModelScope.launch {
      favoritesRepository.toggle(stationId)
    }
  }

  fun onToggleHome() {
    viewModelScope.launch {
      favoritesRepository.setHomeStationId(
        if (favoritesRepository.homeStationId.value == stationId) null else stationId,
      )
    }
  }

  fun onToggleWork() {
    viewModelScope.launch {
      favoritesRepository.setWorkStationId(
        if (favoritesRepository.workStationId.value == stationId) null else stationId,
      )
    }
  }

  fun onRoute(station: Station) {
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
