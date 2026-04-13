package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.DEFAULT_SEARCH_RADIUS_METERS
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.NearbyStationSelection
import com.gcaguilar.biciradar.core.PermissionPrompter
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.selectNearbyStation
import com.gcaguilar.biciradar.core.selectNearbyStationWithBikes
import com.gcaguilar.biciradar.core.selectNearbyStationWithSlots
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
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
  val locationPermissionGranted: Boolean = true,
)

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class NearbyViewModel(
  private val stationsRepository: StationsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val settingsRepository: SettingsRepository,
  private val routeLauncher: RouteLauncher,
  private val permissionPrompter: PermissionPrompter,
) : ViewModel() {
  private val refreshCountdownSeconds = MutableStateFlow(0)
  private val locationPermissionGranted = MutableStateFlow(true)

  val uiState: StateFlow<NearbyUiState> =
    combine(
      stationsRepository.state,
      favoritesRepository.favoriteIds,
      settingsRepository.searchRadiusMeters,
      refreshCountdownSeconds,
      locationPermissionGranted,
    ) { stationsState, favoriteIds, radius, countdown, locationGranted ->
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
        locationPermissionGranted = locationGranted,
      )
    }.stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      NearbyUiState(),
    )

  /** Set to true while the Nearby screen is visible; false when it goes off-screen. */
  private val isActive = MutableStateFlow(false)

  init {
    viewModelScope.launch {
      val intervalSeconds = 300
      while (true) {
        isActive.first { it }
        for (remaining in intervalSeconds downTo 1) {
          if (!isActive.value) break
          refreshCountdownSeconds.update { remaining }
          delay(1_000)
        }
        if (!isActive.value) {
          refreshCountdownSeconds.update { 0 }
          continue
        }
        refreshCountdownSeconds.update { 0 }
        val ids =
          stationsRepository.state.value.stations
            .take(20)
            .map { it.id }
        stationsRepository.refreshAvailability(ids)
      }
    }
  }

  fun setActive(active: Boolean) {
    isActive.update { active }
    if (active) {
      viewModelScope.launch {
        locationPermissionGranted.update { permissionPrompter.hasLocationPermission() }
        val snapshot = stationsRepository.state.value
        if (snapshot.stations.isEmpty() && !snapshot.isLoading && snapshot.errorMessage == null) {
          stationsRepository.loadIfNeeded()
        }
      }
    }
  }

  fun onRequestLocationPermission() {
    viewModelScope.launch {
      permissionPrompter.requestLocationPermission()
      locationPermissionGranted.update { permissionPrompter.hasLocationPermission() }
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
