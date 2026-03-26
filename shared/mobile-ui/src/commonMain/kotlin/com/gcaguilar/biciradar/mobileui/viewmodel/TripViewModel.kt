package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.MONITORING_DURATION_OPTIONS_SECONDS
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.TripDestination


import com.gcaguilar.biciradar.core.TripRepository
import com.gcaguilar.biciradar.core.TripState
import com.gcaguilar.biciradar.core.geo.GeoError
import com.gcaguilar.biciradar.core.geo.GeoResult
import com.gcaguilar.biciradar.core.geo.GeoSearchUseCase
import com.gcaguilar.biciradar.core.geo.ReverseGeocodeUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
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
)

class TripViewModel(
  private val tripRepository: TripRepository,
  private val surfaceMonitoringRepository: SurfaceMonitoringRepository,
  private val geoSearchUseCase: GeoSearchUseCase,
  private val reverseGeocodeUseCase: ReverseGeocodeUseCase,
  private val searchRadiusMeters: Int,
) : ViewModel() {

  private val _uiState = MutableStateFlow(TripUiState())
  val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

  val tripState: StateFlow<TripState> = tripRepository.state

  private var debounceJob: Job? = null

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

      val userLocation: GeoPoint? = null

      val results = try {
        Result.success(geoSearchUseCase.execute(newQuery))
      } catch (cancelled: CancellationException) {
        return@launch
      } catch (error: Throwable) {
        Result.failure(error)
      }

      results.exceptionOrNull()?.let { ex ->
        if (ex !is CancellationException) {
          println("[GeoSearch] ERROR query='$newQuery' type=${ex::class.simpleName} message=${ex.message}")
          ex.cause?.let { println("[GeoSearch] CAUSE type=${it::class.simpleName} message=${it.message}") }
        }
      }

      if (_uiState.value.query == newQuery) {
        val errorMsg: String? = results.exceptionOrNull()?.let { ex ->
          when (ex) {
            is GeoError.Server -> "Location service unavailable"
            is GeoError.Network -> "Network error"
            else -> "Search error: ${ex.message ?: ex::class.simpleName ?: ""}"
          }
        }
        _uiState.value = _uiState.value.copy(
          suggestions = results.getOrElse { emptyList() },
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
      tripRepository.setDestination(
        destination = TripDestination(
          name = result.tripDisplayLabel(),
          location = GeoPoint(result.latitude, result.longitude),
        ),
        searchRadiusMeters = searchRadiusMeters,
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
      val name = runCatching { reverseGeocodeUseCase.execute(location) }
        .getOrNull()
        ?.name
        ?: "${location.latitude}, ${location.longitude}"

      _uiState.value = _uiState.value.copy(
        mapPickerActive = false,
        isReverseGeocoding = false,
        pickedLocation = null,
      )

      tripRepository.setDestination(
        destination = TripDestination(name = name, location = location),
        searchRadiusMeters = searchRadiusMeters,
      )
    }
  }

  fun onStationPickedFromMap(station: com.gcaguilar.biciradar.core.Station) {
    _uiState.value = _uiState.value.copy(
      mapPickerActive = false,
      pickedLocation = null,
      isLoadingSuggestions = false,
      suggestionsError = null,
    )

    viewModelScope.launch {
      tripRepository.setDestination(
        destination = TripDestination(name = station.name, location = station.location),
        searchRadiusMeters = searchRadiusMeters,
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
      tripState.value.nearestStationWithSlots?.id?.let { stationId ->
        surfaceMonitoringRepository.startMonitoring(
          stationId = stationId,
          durationSeconds = _uiState.value.selectedDurationSeconds,
          kind = SurfaceMonitoringKind.Docks,
        )
      }
      tripRepository.startMonitoring(_uiState.value.selectedDurationSeconds)
    }
  }

  fun onStopMonitoring() {
    tripRepository.stopMonitoring()
    viewModelScope.launch {
      surfaceMonitoringRepository.clearMonitoring()
    }
  }

  fun onClearTrip() {
    _uiState.value = _uiState.value.copy(
      isLoadingSuggestions = false,
      suggestionsError = null,
      mapPickerActive = false,
      isReverseGeocoding = false,
      pickedLocation = null,
    )
    tripRepository.clearTrip()
    viewModelScope.launch {
      surfaceMonitoringRepository.clearMonitoring()
    }
  }

  fun onDismissAlert() {
    tripRepository.dismissAlert()
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
