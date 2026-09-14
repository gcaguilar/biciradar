package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.DEFAULT_SEARCH_RADIUS_METERS
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.DatosBiziApi
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.NearbyStationSelection
import com.gcaguilar.biciradar.core.PermissionPrompter
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationHourlyPattern
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.TripMode
import com.gcaguilar.biciradar.core.geo.currentLocalTimeSnapshot
import com.gcaguilar.biciradar.core.launchForTripMode
import com.gcaguilar.biciradar.core.selectNearbyStationForTripMode
import com.gcaguilar.biciradar.core.selectNearbyStationWithBikes
import com.gcaguilar.biciradar.core.selectNearbyStationWithSlots
import com.gcaguilar.biciradar.core.stationsForTripMode
import com.gcaguilar.biciradar.mobileui.NearbyBikeType
import com.gcaguilar.biciradar.mobileui.NearbyFilters
import com.gcaguilar.biciradar.mobileui.NearbyMaxDistance
import com.gcaguilar.biciradar.mobileui.NearbySort
import com.gcaguilar.biciradar.mobileui.filterNearbyStations
import com.gcaguilar.biciradar.mobileui.sortNearbyStations
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class NearbyUiState(
  val stations: List<Station> = emptyList(),
  val favoriteIds: Set<String> = emptySet(),
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
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
  val tripMode: TripMode = TripMode.Pedestrian,
  val filters: NearbyFilters = NearbyFilters(),
  /** `true` cuando la ciudad distingue bicicletas eléctricas y mecánicas. */
  val supportsBikeTypeFilter: Boolean = false,
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
  private val datosBiziApi: DatosBiziApi,
) : ViewModel() {
  private val _refreshCountdownSeconds = MutableStateFlow(0)
  val refreshCountdownSeconds: StateFlow<Int> = _refreshCountdownSeconds.asStateFlow()
  private val locationPermissionGranted = MutableStateFlow(true)
  private val filters = MutableStateFlow(NearbyFilters())
  private val patternCache = MutableStateFlow<Map<String, List<StationHourlyPattern>>>(emptyMap())

  val uiState: StateFlow<NearbyUiState> =
    combine(
      combine(
        stationsRepository.state,
        favoritesRepository.favoriteIds,
        settingsRepository.searchRadiusMeters,
        settingsRepository.tripMode,
        locationPermissionGranted,
      ) { stationsState, favoriteIds, radius, tripMode, locationGranted ->
        NearbyBaseState(
          stationsState = stationsState,
          favoriteIds = favoriteIds,
          radiusMeters = radius,
          tripMode = tripMode,
          locationPermissionGranted = locationGranted,
        )
      },
      filters,
      patternCache,
      settingsRepository.selectedCity,
    ) { base, activeFilters, patterns, city ->
      buildNearbyUiState(base, activeFilters, patterns, city)
    }.stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      NearbyUiState(),
    )

  /** Set to true while the Nearby screen is visible; false when it goes off-screen. */
  private val isActive = MutableStateFlow(false)

  init {
    observeRefreshCountdown()
    observeSelectedCity()
    observePredictedAvailabilitySort()
  }

  private fun observeRefreshCountdown() {
    viewModelScope.launch {
      val intervalSeconds = 300
      while (true) {
        isActive.first { it }
        for (remaining in intervalSeconds downTo 1) {
          if (!isActive.value) break
          _refreshCountdownSeconds.update { remaining }
          delay(1_000)
        }
        if (!isActive.value) {
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

  /** Al cambiar de ciudad se descartan los filtros activos. */
  private fun observeSelectedCity() {
    viewModelScope.launch {
      settingsRepository.selectedCity.collect {
        filters.value = NearbyFilters()
      }
    }
  }

  /** Precarga los patrones horarios cuando se ordena por disponibilidad prevista. */
  private fun observePredictedAvailabilitySort() {
    viewModelScope.launch {
      var lastCityId: String? = null
      combine(
        stationsRepository.state,
        filters,
        settingsRepository.selectedCity,
      ) { stationsState, activeFilters, city ->
        Triple(stationsState.stations, activeFilters.sort, city)
      }.distinctUntilChanged().collect { (stations, sort, city) ->
        if (city.id != lastCityId) {
          lastCityId = city.id
          patternCache.value = emptyMap()
        }
        if (sort == NearbySort.BEST_PREDICTED_AVAILABILITY && city.supportsUsagePatterns) {
          loadMissingPatterns(stations)
        }
      }
    }
  }

  private suspend fun loadMissingPatterns(stations: List<Station>) {
    val missing =
      stations
        .take(PATTERN_STATION_LIMIT)
        .map(Station::id)
        .filterNot { it in patternCache.value }
    if (missing.isEmpty()) return

    val loaded = mutableMapOf<String, List<StationHourlyPattern>>()
    for (id in missing) {
      val patterns =
        try {
          datosBiziApi.fetchPatterns(id)
        } catch (cancellation: CancellationException) {
          throw cancellation
        } catch (_: Exception) {
          null
        } ?: continue
      loaded[id] = patterns
    }
    if (loaded.isNotEmpty()) {
      patternCache.update { it + loaded }
    }
  }

  private fun buildNearbyUiState(
    base: NearbyBaseState,
    activeFilters: NearbyFilters,
    patterns: Map<String, List<StationHourlyPattern>>,
    city: City,
  ): NearbyUiState {
    val localTime = currentLocalTimeSnapshot()
    val effectiveFilters =
      if (city.supportsEbikes) activeFilters else activeFilters.copy(bikeType = NearbyBikeType.BOTH)
    val modeStations = stationsForTripMode(base.stationsState.stations, base.tripMode)
    val visibleStations =
      sortNearbyStations(
        stations = filterNearbyStations(modeStations, effectiveFilters, base.favoriteIds),
        sort = effectiveFilters.sort,
        tripMode = base.tripMode,
        patterns = patterns,
        hour = localTime.hour,
        isWeekend = localTime.isWeekend,
      )
    return NearbyUiState(
      stations = visibleStations,
      favoriteIds = base.favoriteIds,
      isLoading = base.stationsState.isLoading,
      errorMessage = base.stationsState.errorMessage,
      nearestSelection =
        selectNearbyStationForTripMode(
          base.stationsState.stations,
          base.radiusMeters,
          base.tripMode,
        ),
      searchRadiusMeters = base.radiusMeters,
      dataFreshness = base.stationsState.freshness,
      lastUpdatedEpoch = base.stationsState.lastUpdatedEpoch,
      nearestWithBikesSelection =
        selectNearbyStationWithBikes(base.stationsState.stations, base.radiusMeters),
      nearestWithSlotsSelection =
        selectNearbyStationWithSlots(base.stationsState.stations, base.radiusMeters),
      locationPermissionGranted = base.locationPermissionGranted,
      tripMode = base.tripMode,
      filters = effectiveFilters,
      supportsBikeTypeFilter = city.supportsEbikes,
    )
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
      val wasGranted = locationPermissionGranted.value
      permissionPrompter.requestLocationPermission()
      val isGranted = permissionPrompter.hasLocationPermission()
      locationPermissionGranted.update { isGranted }
      if (!wasGranted && isGranted) {
        stationsRepository.forceRefresh()
      }
    }
  }

  fun onRetry() {
    viewModelScope.launch {
      stationsRepository.forceRefresh()
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
    routeLauncher.launchForTripMode(station, settingsRepository.tripMode.value)
  }

  fun onTripModeChanged(mode: TripMode) {
    viewModelScope.launch {
      settingsRepository.setTripMode(mode)
    }
  }

  fun onBikeTypeSelected(bikeType: NearbyBikeType) {
    filters.update { it.copy(bikeType = bikeType) }
  }

  fun onMaxDistanceSelected(maxDistance: NearbyMaxDistance) {
    filters.update { it.copy(maxDistance = maxDistance) }
  }

  fun onFavoritesOnlyToggled() {
    filters.update { it.copy(favoritesOnly = !it.favoritesOnly) }
  }

  fun onSortSelected(sort: NearbySort) {
    filters.update { it.copy(sort = sort) }
  }

  fun onClearFilters() {
    filters.update { NearbyFilters() }
  }

  private data class NearbyBaseState(
    val stationsState: StationsState,
    val favoriteIds: Set<String>,
    val radiusMeters: Int,
    val tripMode: TripMode,
    val locationPermissionGranted: Boolean,
  )

  private companion object {
    const val PATTERN_STATION_LIMIT = 20
  }
}
