package com.gcaguilar.bizizaragoza.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.MONITORING_DURATION_OPTIONS_SECONDS
import com.gcaguilar.bizizaragoza.core.TripDestination
import com.gcaguilar.bizizaragoza.core.TripRepository
import com.gcaguilar.bizizaragoza.core.TripState
import com.gcaguilar.bizizaragoza.core.geo.GeoResult
import com.gcaguilar.bizizaragoza.core.geo.GeoSearchUseCase
import com.gcaguilar.bizizaragoza.core.geo.ReverseGeocodeUseCase
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
)

class TripViewModel(
  private val tripRepository: TripRepository,
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

      val results = runCatching { geoSearchUseCase.execute(newQuery) }

      results.exceptionOrNull()?.let { ex ->
        println("[GeoSearch] ERROR query='$newQuery' type=${ex::class.simpleName} message=${ex.message}")
        ex.cause?.let { println("[GeoSearch] CAUSE type=${it::class.simpleName} message=${it.message}") }
      }

      if (_uiState.value.query == newQuery) {
        val errorMsg = results.exceptionOrNull()?.let { ex ->
          "Error de búsqueda: ${ex.message ?: ex::class.simpleName ?: "desconocido"}"
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
      query = result.name,
      suggestions = emptyList(),
    )
    viewModelScope.launch {
      tripRepository.setDestination(
        destination = TripDestination(
          name = result.name,
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

  fun onStationPickedFromMap(station: com.gcaguilar.bizizaragoza.core.Station) {
    _uiState.value = _uiState.value.copy(mapPickerActive = false, pickedLocation = null)

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
      tripRepository.startMonitoring(_uiState.value.selectedDurationSeconds)
    }
  }

  fun onStopMonitoring() {
    tripRepository.stopMonitoring()
  }

  fun onClearTrip() {
    _uiState.value = TripUiState()
    tripRepository.clearTrip()
  }

  fun onDismissAlert() {
    tripRepository.dismissAlert()
  }

  fun onDestinationCleared() {
    _uiState.value = _uiState.value.copy(query = "", suggestions = emptyList())
  }
}
