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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

  private val _uiState = MutableStateFlow(StationDetailUiState())
  val uiState: StateFlow<StationDetailUiState> = _uiState.asStateFlow()

  private var latestFavoriteIds: Set<String> = emptySet()
  private var latestHomeStationId: String? = null
  private var latestWorkStationId: String? = null
  private var latestSelectedCity: City = City.ZARAGOZA
  private var latestSavedPlaceAlertRules: List<SavedPlaceAlertRule> = emptyList()

  init {
    viewModelScope.launch {
      favoritesRepository.favoriteIds.collect { favoriteIds ->
        latestFavoriteIds = favoriteIds
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

    refreshPatterns()
  }

  private fun publishUiState(baseState: StationDetailUiState = _uiState.value) {
    _uiState.value = baseState.copy(
      isFavorite = stationId in latestFavoriteIds,
      isHomeStation = latestHomeStationId == stationId,
      isWorkStation = latestWorkStationId == stationId,
      supportsUsagePatterns = latestSelectedCity.supportsUsagePatterns,
      savedPlaceAlertsCityId = latestSelectedCity.id,
      savedPlaceAlertRules = latestSavedPlaceAlertRules,
    )
  }

  fun refreshPatterns() {
    viewModelScope.launch {
      publishUiState(_uiState.value.copy(patternsLoading = true, patternsError = false))
      val patterns = runCatching { datosBiziApi.fetchPatterns(stationId) }.getOrNull()
      publishUiState(
        _uiState.value.copy(
          patterns = patterns.orEmpty(),
          patternsLoading = false,
          patternsError = patterns == null,
        ),
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
        if (latestHomeStationId == stationId) null else stationId,
      )
    }
  }

  fun onToggleWork() {
    viewModelScope.launch {
      favoritesRepository.setWorkStationId(
        if (latestWorkStationId == stationId) null else stationId,
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
}
