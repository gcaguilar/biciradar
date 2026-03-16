package com.gcaguilar.bizizaragoza.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.GooglePlacesApi
import com.gcaguilar.bizizaragoza.core.MONITORING_DURATION_OPTIONS_SECONDS
import com.gcaguilar.bizizaragoza.core.PlaceDetails
import com.gcaguilar.bizizaragoza.core.PlacePrediction
import com.gcaguilar.bizizaragoza.core.TripDestination
import com.gcaguilar.bizizaragoza.core.TripRepository
import com.gcaguilar.bizizaragoza.core.TripState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TripUiState(
  val query: String = "",
  val suggestions: List<PlacePrediction> = emptyList(),
  val isLoadingSuggestions: Boolean = false,
  val suggestionsError: String? = null,
  val selectedDurationSeconds: Int = MONITORING_DURATION_OPTIONS_SECONDS[0],
  val mapPickerActive: Boolean = false,
  val isReverseGeocoding: Boolean = false,
  val pickedLocation: GeoPoint? = null,
)

class TripViewModel(
  private val tripRepository: TripRepository,
  private val googlePlacesApi: GooglePlacesApi,
  private val googleMapsApiKey: String?,
  private val searchRadiusMeters: Int,
) : ViewModel() {

  private val _uiState = MutableStateFlow(TripUiState())
  val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

  val tripState: StateFlow<TripState> = tripRepository.state

  private var debounceJob: Job? = null

  fun onQueryChange(newQuery: String) {
    _uiState.value = _uiState.value.copy(query = newQuery)
    
    debounceJob?.cancel()
    
    if (newQuery.isBlank() || googleMapsApiKey == null) {
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
      
      val result = googlePlacesApi.autocompleteWithStatus(newQuery, userLocation, googleMapsApiKey)
      
      if (result.error != null) {
        println("[TripVM] autocomplete EXCEPTION: ${result.error}")
      } else if (result.status != "OK" && result.status != "ZERO_RESULTS") {
        println("[TripVM] autocomplete status=${result.status} query=$newQuery")
      }

      if (_uiState.value.query == newQuery) {
        val errorMsg = when {
          result.error != null -> "Error de red: ${result.error?.message?.take(80)}"
          result.status == "REQUEST_DENIED" -> "API key no autorizada para Places (status: REQUEST_DENIED)"
          result.status == "OVER_QUERY_LIMIT" -> "Límite de consultas superado"
          result.status != "OK" && result.status != "ZERO_RESULTS" -> "Error Places API: ${result.status}"
          else -> null
        }
        _uiState.value = _uiState.value.copy(
          suggestions = result.predictions,
          isLoadingSuggestions = false,
          suggestionsError = errorMsg,
        )
      }
    }
  }

  fun onClearQuery() {
    _uiState.value = _uiState.value.copy(query = "", suggestions = emptyList(), suggestionsError = null)
  }

  fun onSuggestionSelected(prediction: PlacePrediction) {
    if (googleMapsApiKey == null) return

    viewModelScope.launch {
      val details = googlePlacesApi.placeDetails(prediction.placeId, googleMapsApiKey)
      if (details != null) {
        _uiState.value = _uiState.value.copy(
          query = details.name,
          suggestions = emptyList(),
        )
        tripRepository.setDestination(
          destination = TripDestination(
            name = details.name,
            location = details.location,
          ),
          searchRadiusMeters = searchRadiusMeters,
        )
      }
    }
  }

  fun onLocationPicked(location: GeoPoint) {
    if (googleMapsApiKey == null) return

    _uiState.value = _uiState.value.copy(
      isReverseGeocoding = true,
      pickedLocation = location,
    )

    viewModelScope.launch {
      val name = googlePlacesApi.reverseGeocode(location, googleMapsApiKey)
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
