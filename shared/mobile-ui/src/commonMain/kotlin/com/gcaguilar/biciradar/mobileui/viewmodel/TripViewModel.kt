package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.DEFAULT_SEARCH_RADIUS_METERS
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.MONITORING_DURATION_OPTIONS_SECONDS
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.TripDestination
import com.gcaguilar.biciradar.core.TripState
import com.gcaguilar.biciradar.core.geo.GeoResult
import com.gcaguilar.biciradar.mobileui.usecases.GeoLocationUseCase
import com.gcaguilar.biciradar.mobileui.usecases.SurfaceMonitoringUseCase
import com.gcaguilar.biciradar.mobileui.usecases.TripManagementUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TripUiState(
  val query: String = "",
  val suggestions: List<GeoResult> = emptyList(),
  val isLoadingSuggestions: Boolean = false,
  val suggestionsError: String? = null,
  val selectedDurationSeconds: Int = MONITORING_DURATION_OPTIONS_SECONDS[0],
  val mapPickerActive: Boolean = false,
  val isReverseGeocoding: Boolean = false,
  val pickedLocation: GeoPoint? = null,
  val searchRadiusMeters: Int = DEFAULT_SEARCH_RADIUS_METERS,
)

class TripViewModel(
  private val tripManagementUseCase: TripManagementUseCase,
  private val surfaceMonitoringUseCase: SurfaceMonitoringUseCase,
  private val geoLocationUseCase: GeoLocationUseCase,
) : ViewModel() {

  private val _uiState = MutableStateFlow(TripUiState())
  val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

  val tripState: StateFlow<TripState> = tripManagementUseCase.tripState

  private var debounceJob: Job? = null

  init {
    viewModelScope.launch {
      tripManagementUseCase.searchRadiusMeters.collect { radius ->
        _uiState.value = _uiState.value.copy(searchRadiusMeters = radius)
      }
    }
  }

  fun onQueryChange(newQuery: String) {
    _uiState.value = _uiState.value.copy(query = newQuery)

    debounceJob?.cancel()

    if (newQuery.isBlank()) {
      _uiState.value = _uiState.value.copy(
        suggestions = emptyList(),
        isLoadingSuggestions = false,
        suggestionsError = null,
      )
      return
    }

    debounceJob = viewModelScope.launch {
      delay(400)
      if (_uiState.value.query != newQuery) return@launch

      _uiState.value = _uiState.value.copy(isLoadingSuggestions = true, suggestionsError = null)

      val result = try {
        geoLocationUseCase.search(newQuery)
      } catch (cancelled: CancellationException) {
        return@launch
      }

      if (_uiState.value.query == newQuery) {
        val errorMsg: String? = when (result) {
          is GeoLocationUseCase.GeoSearchResult.Error.Server -> "Location service unavailable"
          is GeoLocationUseCase.GeoSearchResult.Error.Network -> "Network error"
          is GeoLocationUseCase.GeoSearchResult.Error.Unknown -> "Search error: ${result.message}"
          else -> null
        }
        val suggestions = when (result) {
          is GeoLocationUseCase.GeoSearchResult.Success -> result.results
          else -> emptyList()
        }
        _uiState.value = _uiState.value.copy(
          suggestions = suggestions,
          isLoadingSuggestions = false,
          suggestionsError = errorMsg,
        )
      }
    }
  }

  fun onClearQuery() {
    _uiState.value = _uiState.value.copy(query = "", suggestions = emptyList(), suggestionsError = null)
  }

  fun onSuggestionSelected(result: GeoResult) {
    _uiState.value = _uiState.value.copy(
      isLoadingSuggestions = false,
      suggestionsError = null,
      mapPickerActive = false,
      pickedLocation = null,
    )
    viewModelScope.launch {
      tripManagementUseCase.setDestination(
        destination = TripDestination(
          name = result.tripDisplayLabel(),
          location = GeoPoint(result.latitude, result.longitude),
        ),
        searchRadiusMeters = _uiState.value.searchRadiusMeters,
      )
    }
  }

  fun onLocationPicked(location: GeoPoint) {
    _uiState.value = _uiState.value.copy(
      isReverseGeocoding = true,
      pickedLocation = location,
      suggestionsError = null,
    )

    viewModelScope.launch {
      val name = when (val result = geoLocationUseCase.reverseGeocode(location)) {
        is GeoLocationUseCase.ReverseGeocodeResult.Success -> result.name
        is GeoLocationUseCase.ReverseGeocodeResult.Fallback -> result.coordinates
      }

      _uiState.value = _uiState.value.copy(
        mapPickerActive = false,
        isReverseGeocoding = false,
        pickedLocation = null,
      )

      tripManagementUseCase.setDestination(
        destination = TripDestination(name = name, location = location),
        searchRadiusMeters = _uiState.value.searchRadiusMeters,
      )
    }
  }

  fun onStationPickedFromMap(station: Station) {
    _uiState.value = _uiState.value.copy(
      mapPickerActive = false,
      pickedLocation = null,
      isLoadingSuggestions = false,
      suggestionsError = null,
    )

    viewModelScope.launch {
      tripManagementUseCase.setDestination(
        destination = TripDestination(name = station.name, location = station.location),
        searchRadiusMeters = _uiState.value.searchRadiusMeters,
      )
    }
  }

  fun onMapPickerToggle() {
    _uiState.value = _uiState.value.copy(
      mapPickerActive = !_uiState.value.mapPickerActive,
      pickedLocation = null,
    )
  }

  fun onDurationSelected(durationSeconds: Int) {
    _uiState.value = _uiState.value.copy(selectedDurationSeconds = durationSeconds)
  }

  fun onStartMonitoring() {
    viewModelScope.launch {
      tripManagementUseCase.nearestStationWithSlots?.let { stationId ->
        surfaceMonitoringUseCase.startMonitoring(
          stationId = stationId,
          durationSeconds = _uiState.value.selectedDurationSeconds,
          kind = SurfaceMonitoringKind.Docks,
        )
      }
      tripManagementUseCase.startMonitoring(_uiState.value.selectedDurationSeconds)
    }
  }

  fun onStopMonitoring() {
    tripManagementUseCase.stopMonitoring()
    surfaceMonitoringUseCase.stopMonitoring()
  }

  fun onClearTrip() {
    _uiState.value = _uiState.value.copy(
      isLoadingSuggestions = false,
      suggestionsError = null,
      mapPickerActive = false,
      isReverseGeocoding = false,
      pickedLocation = null,
    )
    tripManagementUseCase.clearTrip()
    viewModelScope.launch {
      surfaceMonitoringUseCase.clearMonitoring()
    }
  }

  fun onDismissAlert() {
    tripManagementUseCase.dismissAlert()
  }

  fun onDestinationCleared() {
    _uiState.value = _uiState.value.copy(
      isLoadingSuggestions = false,
      suggestionsError = null,
      mapPickerActive = false,
      isReverseGeocoding = false,
      pickedLocation = null,
    )
  }
}

private fun GeoResult.tripDisplayLabel(): String {
  val primaryName = name.trim()
  if (primaryName.isBlank()) return address.trim()

  val addressParts = address
    .split(',')
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .filterNot { it.equals(primaryName, ignoreCase = true) }
    .fold(mutableListOf<String>()) { acc, part ->
      if (acc.lastOrNull()?.equals(part, ignoreCase = true) != true) acc += part
      acc
    }

  if (addressParts.isEmpty()) return primaryName

  val context = addressParts
    .take(3)
    .joinToString(", ")

  return "$primaryName, $context"
}
