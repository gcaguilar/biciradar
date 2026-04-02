package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.EnvironmentalRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.mobileui.MapEnvironmentalLayer
import com.gcaguilar.biciradar.mobileui.MapEnvironmentalZoneSnapshot
import com.gcaguilar.biciradar.mobileui.buildMapEnvironmentalZoneSnapshots
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

internal data class MapEnvironmentalUiState(
  val zones: List<MapEnvironmentalZoneSnapshot> = emptyList(),
)

internal class MapEnvironmentalViewModel(
  private val environmentalRepository: EnvironmentalRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(MapEnvironmentalUiState())
  val uiState: StateFlow<MapEnvironmentalUiState> = _uiState.asStateFlow()

  private val latestStations = MutableStateFlow(emptyList<Station>())
  private val latestLayer = MutableStateFlow<MapEnvironmentalLayer?>(null)

  init {
    viewModelScope.launch {
      combine(latestStations, latestLayer) { stations, layer -> stations to layer }
        .collect { (stations, layer) ->
          if (layer == null || stations.isEmpty()) {
            _uiState.value = MapEnvironmentalUiState()
            return@collect
          }
          _uiState.value = MapEnvironmentalUiState(
            zones = buildEnvironmentalZones(stations),
          )
        }
    }
  }

  fun onStationsChanged(stations: List<Station>) {
    latestStations.value = stations
  }

  fun onEnvironmentalLayerChanged(layer: MapEnvironmentalLayer?) {
    latestLayer.value = layer
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
