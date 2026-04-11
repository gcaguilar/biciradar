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
import com.gcaguilar.biciradar.core.selectNearbyStationWithBikes
import com.gcaguilar.biciradar.core.selectNearbyStationWithSlots
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NearbyUiState(
  val stations: List<Station> = emptyList(),
  val favoriteIds: Set<String> = emptySet(),
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val refreshCountdownSeconds: Int = 0,
  val nearestSelection: NearbyStationSelection =
    NearbyStationSelection(
      withinRadiusStation = null,
      fallbackStation = null,
      radiusMeters = DEFAULT_SEARCH_RADIUS_METERS,
    ),
  val searchRadiusMeters: Int = DEFAULT_SEARCH_RADIUS_METERS,
  val dataFreshness: DataFreshness = DataFreshness.Unavailable,
  val lastUpdatedEpoch: Long? = null,
  val nearestWithBikesSelection: NearbyStationSelection =
    NearbyStationSelection(
      withinRadiusStation = null,
      fallbackStation = null,
      radiusMeters = DEFAULT_SEARCH_RADIUS_METERS,
    ),
  val nearestWithSlotsSelection: NearbyStationSelection =
    NearbyStationSelection(
      withinRadiusStation = null,
      fallbackStation = null,
      radiusMeters = DEFAULT_SEARCH_RADIUS_METERS,
    ),
)

class NearbyViewModel(
  private val stationsRepository: StationsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val settingsRepository: SettingsRepository,
  private val routeLauncher: RouteLauncher,
) : ViewModel() {
  private val _refreshCountdownSeconds = MutableStateFlow(0)

  val uiState: StateFlow<NearbyUiState> =
    combine(
      stationsRepository.state,
      favoritesRepository.favoriteIds,
      settingsRepository.searchRadiusMeters,
      _refreshCountdownSeconds,
    ) { stationsState, favoriteIds, radius, countdown ->
      NearbyUiState(
        stations = stationsState.stations,
        favoriteIds = favoriteIds,
        isLoading = stationsState.isLoading,
        errorMessage = stationsState.errorMessage,
        refreshCountdownSeconds = countdown,
        nearestSelection = selectNearbyStation(stationsState.stations, radius),
        searchRadiusMeters = radius,
        dataFreshness = stationsState.freshness,
        lastUpdatedEpoch = stationsState.lastUpdatedEpoch,
        nearestWithBikesSelection = selectNearbyStationWithBikes(stationsState.stations, radius),
        nearestWithSlotsSelection = selectNearbyStationWithSlots(stationsState.stations, radius),
      )
    }.stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      NearbyUiState(),
    )

  /** Set to true while the Nearby screen is visible; false when it goes off-screen. */
  private val _isActive = MutableStateFlow(false)

  init {
    viewModelScope.launch {
      val intervalSeconds = 300
      while (true) {
        _isActive.first { it }
        for (remaining in intervalSeconds downTo 1) {
          if (!_isActive.value) break
          _refreshCountdownSeconds.update { remaining }
          delay(1_000)
        }
        if (!_isActive.value) {
          _refreshCountdownSeconds.update { 0 }
          continue
        }
        _refreshCountdownSeconds.update { 0 }
        val ids =
          stationsRepository.state.value.stations
            .take(20)
            .map { it.id }
        stationsRepository.refreshAvailability(ids)
      }
    }
  }

  fun setActive(active: Boolean) {
    _isActive.update { active }
    if (active) {
      viewModelScope.launch {
        val snapshot = stationsRepository.state.value
        if (snapshot.stations.isEmpty() && !snapshot.isLoading && snapshot.errorMessage == null) {
          stationsRepository.loadIfNeeded()
        }
      }
    }
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
