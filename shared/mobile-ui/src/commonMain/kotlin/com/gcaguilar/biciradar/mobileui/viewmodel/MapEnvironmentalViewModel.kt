package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.EnvironmentalRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.NearbyStationSelection
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.mobileui.MapFilter
import com.gcaguilar.biciradar.mobileui.MapEnvironmentalLayer
import com.gcaguilar.biciradar.mobileui.MapEnvironmentalZoneSnapshot
import com.gcaguilar.biciradar.mobileui.buildMapEnvironmentalZoneSnapshots
import com.gcaguilar.biciradar.mobileui.clearEnvironmentalMapFilters
import com.gcaguilar.biciradar.mobileui.isEnvironmentalMapFilter
import com.gcaguilar.biciradar.mobileui.sanitizeActiveMapFilters
import com.gcaguilar.biciradar.mobileui.toggleMapFilterSelection
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

internal data class MapEnvironmentalUiState(
  val zones: List<MapEnvironmentalZoneSnapshot> = emptyList(),
  val persistedActiveFilters: Set<MapFilter> = emptySet(),
  val selectedMapStationId: String? = null,
  val hasExplicitMapSelection: Boolean = false,
  val isCardDismissed: Boolean = false,
  val showEnvironmentalSheet: Boolean = false,
  val recenterRequestToken: Int = 0,
)

@OptIn(ExperimentalCoroutinesApi::class)
internal class MapEnvironmentalViewModel(
  private val environmentalRepository: EnvironmentalRepository,
  private val settingsRepository: SettingsRepository,
) : ViewModel() {

  private val mutableUiState = MutableStateFlow(MapEnvironmentalUiState())

  private val latestStations = MutableStateFlow(emptyList<Station>())
  private val latestLayer = MutableStateFlow<MapEnvironmentalLayer?>(null)
  private val zonesState: StateFlow<List<MapEnvironmentalZoneSnapshot>> = combine(
    latestStations,
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
  val uiState: StateFlow<MapEnvironmentalUiState> = combine(
    mutableUiState,
    zonesState,
  ) { base, zones ->
    base.copy(zones = zones)
  }.stateIn(
    viewModelScope,
    SharingStarted.Eagerly,
    MapEnvironmentalUiState(),
  )

  init {
    viewModelScope.launch {
      settingsRepository.bootstrap()
      val persistedFilters = settingsRepository.persistedMapFilterNames()
        .mapNotNull { MapFilter.entries.firstOrNull { filter -> filter.name == it } }
        .toSet()
      mutableUiState.update { it.copy(persistedActiveFilters = persistedFilters) }
    }
  }

  fun onStationsChanged(stations: List<Station>) {
    latestStations.update { stations }
  }

  fun onEnvironmentalLayerChanged(layer: MapEnvironmentalLayer?) {
    latestLayer.update { layer }
  }

  fun onPersistedMapFiltersChanged(filters: Set<MapFilter>) {
    mutableUiState.update { it.copy(persistedActiveFilters = filters) }
    viewModelScope.launch {
      settingsRepository.setPersistedMapFilterNames(filters.mapTo(linkedSetOf()) { it.name })
    }
  }

  fun onToggleFilter(filter: MapFilter, availableFilters: Set<MapFilter>) {
    val current = uiState.value.persistedActiveFilters
    val toggled = toggleMapFilterSelection(current, filter)
    val next = sanitizeActiveMapFilters(toggled, availableFilters)
    mutableUiState.update {
      it.copy(
        persistedActiveFilters = next,
        showEnvironmentalSheet = if (isEnvironmentalMapFilter(filter)) filter in next else it.showEnvironmentalSheet,
      )
    }
    viewModelScope.launch {
      settingsRepository.setPersistedMapFilterNames(next.mapTo(linkedSetOf()) { it.name })
    }
  }

  fun onAvailableFiltersChanged(availableFilters: Set<MapFilter>) {
    val currentFilters = uiState.value.persistedActiveFilters
    val sanitized = sanitizeActiveMapFilters(currentFilters, availableFilters)
    if (sanitized == currentFilters) return
    onPersistedMapFiltersChanged(sanitized)
  }

  fun onStationSelected(stationId: String) {
    mutableUiState.update {
      it.copy(
      selectedMapStationId = stationId,
      hasExplicitMapSelection = true,
      isCardDismissed = false,
    )
    }
  }

  fun onStationCardDismissed() {
    mutableUiState.update { it.copy(isCardDismissed = true) }
  }

  fun onRecenterRequested() {
    mutableUiState.update {
      it.copy(
        recenterRequestToken = it.recenterRequestToken + 1,
        isCardDismissed = false,
      )
    }
  }

  fun onEnvironmentalSheetShown() {
    mutableUiState.update { it.copy(showEnvironmentalSheet = true) }
  }

  fun onEnvironmentalSheetDismissed() {
    mutableUiState.update { it.copy(showEnvironmentalSheet = false) }
  }

  fun onClearEnvironmentalFilters() {
    val next = clearEnvironmentalMapFilters(uiState.value.persistedActiveFilters)
    mutableUiState.update {
      it.copy(
      persistedActiveFilters = next,
      showEnvironmentalSheet = false,
    )
    }
    viewModelScope.launch {
      settingsRepository.setPersistedMapFilterNames(next.mapTo(linkedSetOf()) { it.name })
    }
  }

  fun reconcileSelection(
    mapStations: List<Station>,
    nearestSelection: NearbyStationSelection,
    searchQuery: String,
  ) {
    val current = uiState.value
    var selectedId = current.selectedMapStationId
    var explicitSelection = current.hasExplicitMapSelection

    val hasValidSelected = selectedId != null && mapStations.any { it.id == selectedId }
    if (!hasValidSelected) {
      selectedId = if (searchQuery.isNotBlank()) {
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

    val shouldResetDismissed = selectedId != current.selectedMapStationId
    mutableUiState.update {
      current.copy(
      selectedMapStationId = selectedId,
      hasExplicitMapSelection = explicitSelection,
      isCardDismissed = if (shouldResetDismissed) false else current.isCardDismissed,
    )
    }
  }

  private suspend fun buildEnvironmentalZones(stations: List<Station>): List<MapEnvironmentalZoneSnapshot> {
    return buildMapEnvironmentalZoneSnapshots(stations).map { zone ->
      val reading = environmentalRepository.readingAt(zone.centerLatitude, zone.centerLongitude)
      zone.copy(
        airQualityScore = reading?.airQualityIndex,
        pollenScore = reading?.pollenIndex,
      )
    }
  }
}
