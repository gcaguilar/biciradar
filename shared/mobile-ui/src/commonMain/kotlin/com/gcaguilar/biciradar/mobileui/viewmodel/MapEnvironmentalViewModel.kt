package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.DEFAULT_SEARCH_RADIUS_METERS
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.EnvironmentalRepository
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.NearbyStationSelection
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.filterStationsByQuery
import com.gcaguilar.biciradar.core.selectNearbyStation
import com.gcaguilar.biciradar.mobileui.MapEnvironmentalLayer
import com.gcaguilar.biciradar.mobileui.MapEnvironmentalZoneSnapshot
import com.gcaguilar.biciradar.mobileui.MapFilter
import com.gcaguilar.biciradar.mobileui.activeEnvironmentalLayerForFilters
import com.gcaguilar.biciradar.mobileui.applyMapFilters
import com.gcaguilar.biciradar.mobileui.availableMapFilters
import com.gcaguilar.biciradar.mobileui.buildMapEnvironmentalZoneSnapshots
import com.gcaguilar.biciradar.mobileui.clearEnvironmentalMapFilters
import com.gcaguilar.biciradar.mobileui.isEnvironmentalMapFilter
import com.gcaguilar.biciradar.mobileui.sanitizeActiveMapFilters
import com.gcaguilar.biciradar.mobileui.toggleMapFilterSelection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class MapEnvironmentalUiState(
  val stations: List<Station> = emptyList(),
  val favoriteIds: Set<String> = emptySet(),
  val loading: Boolean = false,
  val errorMessage: String? = null,
  val dataFreshness: DataFreshness = DataFreshness.Unavailable,
  val lastUpdatedEpoch: Long? = null,
  val nearestSelection: NearbyStationSelection =
    NearbyStationSelection(
      withinRadiusStation = null,
      fallbackStation = null,
      radiusMeters = DEFAULT_SEARCH_RADIUS_METERS,
    ),
  val searchQuery: String = "",
  val searchRadiusMeters: Int = DEFAULT_SEARCH_RADIUS_METERS,
  val userLocation: GeoPoint? = null,
  val availableFilters: Set<MapFilter> = emptySet(),
  val zones: List<MapEnvironmentalZoneSnapshot> = emptyList(),
  val persistedActiveFilters: Set<MapFilter> = emptySet(),
  val activeEnvironmentalLayer: MapEnvironmentalLayer? = null,
  val selectedMapStation: Station? = null,
  val selectedMapStationId: String? = null,
  val hasExplicitMapSelection: Boolean = false,
  val isShowingNearestSelection: Boolean = false,
  val isShowingNearestFallback: Boolean = false,
  val isCardDismissed: Boolean = false,
  val showEnvironmentalSheet: Boolean = false,
  val recenterRequestToken: Int = 0,
)

private data class MapEnvironmentalMutableState(
  val searchQuery: String = "",
  val persistedActiveFilters: Set<MapFilter> = emptySet(),
  val selectedMapStationId: String? = null,
  val hasExplicitMapSelection: Boolean = false,
  val isCardDismissed: Boolean = false,
  val showEnvironmentalSheet: Boolean = false,
  val recenterRequestToken: Int = 0,
)

private data class DerivedMapInputs(
  val stationsState: StationsState,
  val favoriteIds: Set<String>,
  val searchRadiusMeters: Int,
  val state: MapEnvironmentalMutableState,
)

private data class ResolvedMapSelection(
  val selectedId: String?,
  val selectedStation: Station?,
  val hasExplicitSelection: Boolean,
)

@OptIn(ExperimentalCoroutinesApi::class)
internal class MapEnvironmentalViewModel(
  private val environmentalRepository: EnvironmentalRepository,
  private val settingsRepository: SettingsRepository,
  private val stationsRepository: StationsRepository,
  private val favoritesRepository: FavoritesRepository,
) : ViewModel() {
  private val mutableState = MutableStateFlow(MapEnvironmentalMutableState())
  private val latestFilteredStations = MutableStateFlow(emptyList<Station>())
  private val latestLayer = MutableStateFlow<MapEnvironmentalLayer?>(null)

  private val zonesState: StateFlow<List<MapEnvironmentalZoneSnapshot>> =
    combine(
      latestFilteredStations,
      latestLayer,
    ) { stations, layer ->
      stations to layer
    }.mapLatest { (stations, layer) ->
      if (layer == null || stations.isEmpty()) {
        emptyList()
      } else {
        buildEnvironmentalZones(stations)
      }
    }.stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      emptyList(),
    )

  val uiState: StateFlow<MapEnvironmentalUiState> =
    combine(
      stationsRepository.state,
      favoritesRepository.favoriteIds,
      settingsRepository.searchRadiusMeters,
      mutableState,
      zonesState,
    ) { stationsState, favoriteIds, searchRadiusMeters, state, zones ->
      buildUiState(
        stationsState = stationsState,
        favoriteIds = favoriteIds,
        searchRadiusMeters = searchRadiusMeters,
        state = state,
        zones = zones,
      )
    }.stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      MapEnvironmentalUiState(),
    )

  init {
    viewModelScope.launch {
      settingsRepository.bootstrap()
      val persistedFilters =
        settingsRepository
          .persistedMapFilterNames()
          .mapNotNull { name -> MapFilter.entries.firstOrNull { it.name == name } }
          .toSet()
      mutableState.update { it.copy(persistedActiveFilters = persistedFilters) }
    }

    combine(
      stationsRepository.state,
      favoritesRepository.favoriteIds,
      settingsRepository.searchRadiusMeters,
      mutableState,
    ) { stationsState, favoriteIds, searchRadiusMeters, state ->
      DerivedMapInputs(
        stationsState = stationsState,
        favoriteIds = favoriteIds,
        searchRadiusMeters = searchRadiusMeters,
        state = state,
      )
    }.onEach(::reconcileDerivedState)
      .launchIn(viewModelScope)
  }

  fun onSearchQueryChange(query: String) {
    mutableState.update { it.copy(searchQuery = query) }
  }

  fun onPersistedMapFiltersChanged(filters: Set<MapFilter>) {
    mutableState.update { it.copy(persistedActiveFilters = filters) }
    persistFilters(filters)
  }

  fun onToggleFilter(filter: MapFilter) {
    val availableFilters = uiState.value.availableFilters
    val currentFilters = uiState.value.persistedActiveFilters
    val toggled = toggleMapFilterSelection(currentFilters, filter)
    val next = sanitizeActiveMapFilters(toggled, availableFilters)
    mutableState.update {
      it.copy(
        persistedActiveFilters = next,
        showEnvironmentalSheet = if (isEnvironmentalMapFilter(filter)) filter in next else it.showEnvironmentalSheet,
      )
    }
    persistFilters(next)
  }

  fun onStationSelected(stationId: String) {
    mutableState.update {
      it.copy(
        selectedMapStationId = stationId,
        hasExplicitMapSelection = true,
        isCardDismissed = false,
      )
    }
  }

  fun onStationCardDismissed() {
    mutableState.update { it.copy(isCardDismissed = true) }
  }

  fun onRecenterRequested() {
    mutableState.update {
      it.copy(
        recenterRequestToken = it.recenterRequestToken + 1,
        isCardDismissed = false,
      )
    }
  }

  fun onEnvironmentalSheetShown() {
    mutableState.update { it.copy(showEnvironmentalSheet = true) }
  }

  fun onEnvironmentalSheetDismissed() {
    mutableState.update { it.copy(showEnvironmentalSheet = false) }
  }

  fun onClearEnvironmentalFilters() {
    val next = clearEnvironmentalMapFilters(uiState.value.persistedActiveFilters)
    mutableState.update {
      it.copy(
        persistedActiveFilters = next,
        showEnvironmentalSheet = false,
      )
    }
    persistFilters(next)
  }

  private fun buildUiState(
    stationsState: StationsState,
    favoriteIds: Set<String>,
    searchRadiusMeters: Int,
    state: MapEnvironmentalMutableState,
    zones: List<MapEnvironmentalZoneSnapshot>,
  ): MapEnvironmentalUiState {
    val filteredStations = filterStationsByQuery(stationsState.stations, state.searchQuery)
    val availableFilters = availableMapFilters(filteredStations)
    val activeFilters = sanitizeActiveMapFilters(state.persistedActiveFilters, availableFilters)
    val mapStations = applyMapFilters(filteredStations, activeFilters)
    val nearestSelection = selectNearbyStation(stationsState.stations, searchRadiusMeters)
    val selection =
      resolveSelection(
        mapStations = mapStations,
        nearestSelection = nearestSelection,
        searchQuery = state.searchQuery,
        requestedSelectedId = state.selectedMapStationId,
        requestedExplicitSelection = state.hasExplicitMapSelection,
      )

    return MapEnvironmentalUiState(
      stations = mapStations,
      favoriteIds = favoriteIds,
      loading = stationsState.isLoading,
      errorMessage = stationsState.errorMessage,
      dataFreshness = stationsState.freshness,
      lastUpdatedEpoch = stationsState.lastUpdatedEpoch,
      nearestSelection = nearestSelection,
      searchQuery = state.searchQuery,
      searchRadiusMeters = searchRadiusMeters,
      userLocation = stationsState.userLocation,
      availableFilters = availableFilters,
      zones = zones,
      persistedActiveFilters = activeFilters,
      activeEnvironmentalLayer = activeEnvironmentalLayerForFilters(activeFilters),
      selectedMapStation = selection.selectedStation,
      selectedMapStationId = selection.selectedId,
      hasExplicitMapSelection = selection.hasExplicitSelection,
      isShowingNearestSelection =
        !selection.hasExplicitSelection && selection.selectedId == nearestSelection.highlightedStation?.id,
      isShowingNearestFallback =
        selection.selectedId == nearestSelection.highlightedStation?.id && nearestSelection.usesFallback,
      isCardDismissed = if (selection.selectedId != state.selectedMapStationId) false else state.isCardDismissed,
      showEnvironmentalSheet =
        if (activeEnvironmentalLayerForFilters(activeFilters) ==
          null
        ) {
          false
        } else {
          state.showEnvironmentalSheet
        },
      recenterRequestToken = state.recenterRequestToken,
    )
  }

  private fun reconcileDerivedState(inputs: DerivedMapInputs) {
    val filteredStations = filterStationsByQuery(inputs.stationsState.stations, inputs.state.searchQuery)
    val availableFilters = availableMapFilters(filteredStations)
    val sanitizedFilters = sanitizeActiveMapFilters(inputs.state.persistedActiveFilters, availableFilters)
    if (sanitizedFilters != inputs.state.persistedActiveFilters) {
      mutableState.update { it.copy(persistedActiveFilters = sanitizedFilters) }
      persistFilters(sanitizedFilters)
      return
    }

    val activeLayer = activeEnvironmentalLayerForFilters(sanitizedFilters)
    latestFilteredStations.update { filteredStations }
    latestLayer.update { activeLayer }
  }

  private fun persistFilters(filters: Set<MapFilter>) {
    viewModelScope.launch {
      settingsRepository.setPersistedMapFilterNames(filters.mapTo(linkedSetOf()) { it.name })
    }
  }

  private suspend fun buildEnvironmentalZones(stations: List<Station>): List<MapEnvironmentalZoneSnapshot> =
    buildMapEnvironmentalZoneSnapshots(stations).map { zone ->
      val reading = environmentalRepository.readingAt(zone.centerLatitude, zone.centerLongitude)
      zone.copy(
        airQualityScore = reading?.airQualityIndex,
        pollenScore = reading?.pollenIndex,
      )
    }
}

private fun resolveSelection(
  mapStations: List<Station>,
  nearestSelection: NearbyStationSelection,
  searchQuery: String,
  requestedSelectedId: String?,
  requestedExplicitSelection: Boolean,
): ResolvedMapSelection {
  var selectedId = requestedSelectedId
  var explicitSelection = requestedExplicitSelection

  val hasValidSelected = selectedId != null && mapStations.any { it.id == selectedId }
  if (!hasValidSelected) {
    selectedId =
      if (searchQuery.isNotBlank()) {
        mapStations.firstOrNull()?.id
      } else {
        val nearestId = nearestSelection.highlightedStation?.id
        nearestId?.takeIf { id -> mapStations.any { it.id == id } } ?: mapStations.firstOrNull()?.id
      }
    explicitSelection = false
  }

  if (searchQuery.isNotBlank()) {
    val firstMatchId = mapStations.firstOrNull()?.id
    if (firstMatchId != null && firstMatchId != selectedId) {
      selectedId = firstMatchId
      explicitSelection = false
    }
  }

  return ResolvedMapSelection(
    selectedId = selectedId,
    selectedStation = selectedId?.let { id -> mapStations.firstOrNull { it.id == id } },
    hasExplicitSelection = explicitSelection,
  )
}
