package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.DEFAULT_SEARCH_RADIUS_METERS
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.NearbyStationSelection
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.selectNearbyStation
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
  val nearestSelection: NearbyStationSelection = NearbyStationSelection(
    withinRadiusStation = null,
    fallbackStation = null,
    radiusMeters = DEFAULT_SEARCH_RADIUS_METERS,
  ),
  val searchRadiusMeters: Int = DEFAULT_SEARCH_RADIUS_METERS,
  val dataFreshness: DataFreshness = DataFreshness.Unavailable,
  val lastUpdatedEpoch: Long? = null,
)

class NearbyViewModel(
  private val stationsRepository: StationsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val settingsRepository: SettingsRepository,
  private val routeLauncher: RouteLauncher,
) : ViewModel() {

  private val _uiState = MutableStateFlow(NearbyUiState())
  val uiState: StateFlow<NearbyUiState> = _uiState.asStateFlow()

  /** Set to true while the Nearby screen is visible; false when it goes off-screen. */
  private val _isActive = MutableStateFlow(false)
  private val _refreshCountdownSeconds = MutableStateFlow(0)

  private var latestStations: List<Station> = emptyList()
  private var latestFavoriteIds: Set<String> = emptySet()
  private var latestSearchRadiusMeters: Int = DEFAULT_SEARCH_RADIUS_METERS
  private var latestIsLoading: Boolean = false
  private var latestErrorMessage: String? = null
  private var latestDataFreshness: DataFreshness = DataFreshness.Unavailable
  private var latestLastUpdatedEpoch: Long? = null

  init {
    viewModelScope.launch {
      stationsRepository.state.collect { state ->
        latestStations = state.stations
        latestIsLoading = state.isLoading
        latestErrorMessage = state.errorMessage
        latestDataFreshness = state.freshness
        latestLastUpdatedEpoch = state.lastUpdatedEpoch
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
      settingsRepository.searchRadiusMeters.collect { radius ->
        latestSearchRadiusMeters = radius
        publishUiState()
      }
    }

    viewModelScope.launch {
      _refreshCountdownSeconds.collect {
        publishUiState()
      }
    }

    viewModelScope.launch {
      val intervalSeconds = 300
      while (true) {
        _isActive.first { it }
        for (remaining in intervalSeconds downTo 1) {
          if (!_isActive.value) break
          _refreshCountdownSeconds.value = remaining
          delay(1_000)
        }
        if (!_isActive.value) {
          _refreshCountdownSeconds.value = 0
          continue
        }
        _refreshCountdownSeconds.value = 0
        val ids = stationsRepository.state.value.stations.take(20).map { it.id }
        stationsRepository.refreshAvailability(ids)
      }
    }
  }

  private fun publishUiState() {
    _uiState.value = NearbyUiState(
      stations = latestStations,
      favoriteIds = latestFavoriteIds,
      isLoading = latestIsLoading,
      errorMessage = latestErrorMessage,
      refreshCountdownSeconds = _refreshCountdownSeconds.value,
      nearestSelection = selectNearbyStation(latestStations, latestSearchRadiusMeters),
      searchRadiusMeters = latestSearchRadiusMeters,
      dataFreshness = latestDataFreshness,
      lastUpdatedEpoch = latestLastUpdatedEpoch,
    )
  }

  fun setActive(active: Boolean) {
    _isActive.value = active
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
