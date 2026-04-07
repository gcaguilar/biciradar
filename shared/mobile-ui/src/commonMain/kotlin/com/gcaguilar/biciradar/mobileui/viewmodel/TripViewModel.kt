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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
  private data class TripTransientUiState(
    val query: String = "",
    val suggestions: List<GeoResult> = emptyList(),
    val isLoadingSuggestions: Boolean = false,
    val suggestionsError: String? = null,
    val selectedDurationSeconds: Int = MONITORING_DURATION_OPTIONS_SECONDS[0],
    val mapPickerActive: Boolean = false,
    val isReverseGeocoding: Boolean = false,
    val pickedLocation: GeoPoint? = null,
  )


  private val transientUiState = MutableStateFlow(TripTransientUiState())

  val uiState: StateFlow<TripUiState> = combine(
    transientUiState,
    tripManagementUseCase.searchRadiusMeters,
  ) { transient, radius ->
    TripUiState(
      query = transient.query,
      suggestions = transient.suggestions,
      isLoadingSuggestions = transient.isLoadingSuggestions,
      suggestionsError = transient.suggestionsError,
      selectedDurationSeconds = transient.selectedDurationSeconds,
      mapPickerActive = transient.mapPickerActive,
      isReverseGeocoding = transient.isReverseGeocoding,
      pickedLocation = transient.pickedLocation,
      searchRadiusMeters = radius,
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.Eagerly,
    TripUiState(searchRadiusMeters = tripManagementUseCase.searchRadiusMeters.value),
  )

  val tripState: StateFlow<TripState> = tripManagementUseCase.tripState

  private val queryInput = MutableStateFlow("")

  init {
    viewModelScope.launch {
      val preferredDuration = tripManagementUseCase.preferredMonitoringDurationSeconds()
      if (preferredDuration != null && preferredDuration in MONITORING_DURATION_OPTIONS_SECONDS) {
        transientUiState.update { it.copy(selectedDurationSeconds = preferredDuration) }
      }
    }
    queryInput
      .debounce(400)
      .distinctUntilChanged()
      .filter { it.isNotBlank() }
      .onEach { query ->
        if (transientUiState.value.query != query) return@onEach
        transientUiState.update {
          it.copy(
          isLoadingSuggestions = true,
          suggestionsError = null,
        )
        }
        val result = try {
          geoLocationUseCase.search(query)
        } catch (cancelled: CancellationException) {
          return@onEach
        }
        if (transientUiState.value.query == query) {
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
          transientUiState.update {
            it.copy(
            suggestions = suggestions,
            isLoadingSuggestions = false,
            suggestionsError = errorMsg,
          )
          }
        }
      }
      .launchIn(viewModelScope)
  }

  fun onQueryChange(newQuery: String) {
    transientUiState.update { it.copy(query = newQuery) }
    if (newQuery.isBlank()) {
      transientUiState.update {
        it.copy(
        suggestions = emptyList(),
        isLoadingSuggestions = false,
        suggestionsError = null,
      )
      }
      return
    }
    queryInput.update { newQuery }
  }

  fun onClearQuery() {
    transientUiState.update {
      it.copy(
      query = "",
      suggestions = emptyList(),
      suggestionsError = null,
      isLoadingSuggestions = false,
    )
    }
  }

  fun onSuggestionSelected(result: GeoResult) {
    transientUiState.update {
      it.copy(
      isLoadingSuggestions = false,
      suggestionsError = null,
      mapPickerActive = false,
      pickedLocation = null,
    )
    }
    viewModelScope.launch {
      tripManagementUseCase.setDestination(
        destination = TripDestination(
          name = result.tripDisplayLabel(),
          location = GeoPoint(result.latitude, result.longitude),
        ),
        searchRadiusMeters = uiState.value.searchRadiusMeters,
      )
    }
  }

  fun onLocationPicked(location: GeoPoint) {
    transientUiState.update {
      it.copy(
      isReverseGeocoding = true,
      pickedLocation = location,
      suggestionsError = null,
    )
    }

    viewModelScope.launch {
      val name = when (val result = geoLocationUseCase.reverseGeocode(location)) {
        is GeoLocationUseCase.ReverseGeocodeResult.Success -> result.name
        is GeoLocationUseCase.ReverseGeocodeResult.Fallback -> result.coordinates
      }

      transientUiState.update {
        it.copy(
        mapPickerActive = false,
        isReverseGeocoding = false,
        pickedLocation = null,
      )
      }

      tripManagementUseCase.setDestination(
        destination = TripDestination(name = name, location = location),
        searchRadiusMeters = uiState.value.searchRadiusMeters,
      )
    }
  }

  fun onStationPickedFromMap(station: Station) {
    transientUiState.update {
      it.copy(
        mapPickerActive = false,
        pickedLocation = null,
        isLoadingSuggestions = false,
        suggestionsError = null,
      )
    }

    viewModelScope.launch {
      tripManagementUseCase.setDestination(
        destination = TripDestination(name = station.name, location = station.location),
        searchRadiusMeters = uiState.value.searchRadiusMeters,
      )
    }
  }

  fun onMapPickerToggle() {
    transientUiState.update {
      it.copy(
        mapPickerActive = !it.mapPickerActive,
        pickedLocation = null,
      )
    }
  }

  fun onDurationSelected(durationSeconds: Int) {
    transientUiState.update { it.copy(selectedDurationSeconds = durationSeconds) }
    viewModelScope.launch {
      tripManagementUseCase.setPreferredMonitoringDurationSeconds(durationSeconds)
    }
  }

  fun onStartMonitoring() {
    viewModelScope.launch {
      tripManagementUseCase.nearestStationWithSlots?.let { stationId ->
        surfaceMonitoringUseCase.startMonitoring(
          stationId = stationId,
          durationSeconds = uiState.value.selectedDurationSeconds,
          kind = SurfaceMonitoringKind.Docks,
        )
      }
      tripManagementUseCase.startMonitoring(uiState.value.selectedDurationSeconds)
    }
  }

  fun onStopMonitoring() {
    tripManagementUseCase.stopMonitoring()
    surfaceMonitoringUseCase.stopMonitoring()
  }

  fun onClearTrip() {
    transientUiState.update {
      it.copy(
      isLoadingSuggestions = false,
      suggestionsError = null,
      mapPickerActive = false,
      isReverseGeocoding = false,
      pickedLocation = null,
    )
    }
    tripManagementUseCase.clearTrip()
    viewModelScope.launch {
      surfaceMonitoringUseCase.clearMonitoring()
    }
  }

  fun onDismissAlert() {
    tripManagementUseCase.dismissAlert()
  }

  fun onDestinationCleared() {
    transientUiState.update {
      it.copy(
      isLoadingSuggestions = false,
      suggestionsError = null,
      mapPickerActive = false,
      isReverseGeocoding = false,
      pickedLocation = null,
    )
    }
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
