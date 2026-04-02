package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.EnvironmentalRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.mobileui.MapEnvironmentalLayer
import com.gcaguilar.biciradar.mobileui.MapEnvironmentalZoneSnapshot
import com.gcaguilar.biciradar.mobileui.buildMapEnvironmentalZoneSnapshots
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class MapEnvironmentalUiState(
  val zones: List<MapEnvironmentalZoneSnapshot> = emptyList(),
)

internal class MapEnvironmentalViewModel(
  private val environmentalRepository: EnvironmentalRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(MapEnvironmentalUiState())
  val uiState: StateFlow<MapEnvironmentalUiState> = _uiState.asStateFlow()

  private var latestStations: List<Station> = emptyList()
  private var latestLayer: MapEnvironmentalLayer? = null
  private var refreshJob: Job? = null

  fun onStationsChanged(stations: List<Station>) {
    latestStations = stations
    refreshSnapshots()
  }

  fun onEnvironmentalLayerChanged(layer: MapEnvironmentalLayer?) {
    latestLayer = layer
    refreshSnapshots()
  }

  private fun refreshSnapshots() {
    refreshJob?.cancel()
    val layer = latestLayer
    val stations = latestStations
    refreshJob = viewModelScope.launch {
      if (layer == null || stations.isEmpty()) {
        _uiState.value = MapEnvironmentalUiState()
        return@launch
      }

      val zones = buildMapEnvironmentalZoneSnapshots(stations).map { zone ->
        val reading = environmentalRepository.readingAt(zone.centerLatitude, zone.centerLongitude)
        zone.copy(
          airQualityScore = reading?.airQualityIndex,
          pollenScore = reading?.pollenIndex,
        )
      }
      _uiState.value = MapEnvironmentalUiState(zones = zones)
    }
  }
}
