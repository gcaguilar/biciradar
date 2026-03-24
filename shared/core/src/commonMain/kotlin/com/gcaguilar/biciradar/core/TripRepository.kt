package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val POLLING_INTERVAL_MILLIS = 30_000L

val MONITORING_DURATION_OPTIONS_SECONDS = listOf(5 * 60, 10 * 60, 15 * 60, 20 * 60)

data class TripDestination(
  val name: String,
  val location: GeoPoint,
)

data class TripAlert(
  val fullStation: Station,
  val alternativeStation: Station?,
  val alternativeDistanceMeters: Int?,
)

data class TripMonitoringState(
  val isActive: Boolean = false,
  val remainingSeconds: Int = 0,
  val totalSeconds: Int = 0,
)

data class TripState(
  val destination: TripDestination? = null,
  val nearestStationWithSlots: Station? = null,
  val distanceToStation: Int? = null,
  val monitoring: TripMonitoringState = TripMonitoringState(),
  val alert: TripAlert? = null,
  val isSearchingStation: Boolean = false,
  val searchError: String? = null,
)

interface TripRepository {
  val state: StateFlow<TripState>
  suspend fun setDestination(destination: TripDestination, searchRadiusMeters: Int)
  suspend fun startMonitoring(durationSeconds: Int)
  fun stopMonitoring()
  fun clearTrip()
  fun dismissAlert()
  /** Called by platform (e.g., iOS background task expiry) to do a final check and notify. */
  suspend fun doFinalBackgroundCheck()
}

@Inject
class TripRepositoryImpl(
  private val biziApi: BiziApi,
  private val stationsRepository: StationsRepository,
  private val localNotifier: LocalNotifier,
  private val appConfiguration: AppConfiguration,
  private val scope: CoroutineScope,
) : TripRepository {
  private val mutableState = MutableStateFlow(TripState())
  override val state: StateFlow<TripState> = mutableState.asStateFlow()

  private var monitoringJob: Job? = null
  private var countdownJob: Job? = null

  override suspend fun setDestination(destination: TripDestination, searchRadiusMeters: Int) {
    stopMonitoring()
    mutableState.update {
      it.copy(
        destination = destination,
        nearestStationWithSlots = null,
        distanceToStation = null,
        alert = null,
        isSearchingStation = true,
        searchError = null,
      )
    }
    findNearestStation(destination.location, searchRadiusMeters)
  }

  private suspend fun findNearestStation(location: GeoPoint, searchRadiusMeters: Int) {
    val stations = stationsRepository.state.value.stations
    if (stations.isEmpty()) {
      mutableState.update {
        it.copy(
          isSearchingStation = false,
          searchError = "No station data available",
        )
      }
      return
    }

    // Recalculate distances relative to the destination
    val stationsWithDistance = stations.map { station ->
      station.copy(distanceMeters = distanceBetween(location, station.location))
    }.sortedBy { it.distanceMeters }

    val selection = selectNearbyStation(
      stations = stationsWithDistance,
      searchRadiusMeters = searchRadiusMeters,
      predicate = { it.slotsFree > 0 },
    )
    val chosen = selection.highlightedStation

    if (chosen == null) {
      mutableState.update {
        it.copy(
          isSearchingStation = false,
          searchError = "No stations with free slots nearby",
        )
      }
      return
    }

    mutableState.update {
      it.copy(
        nearestStationWithSlots = chosen,
        distanceToStation = chosen.distanceMeters,
        isSearchingStation = false,
        searchError = null,
      )
    }
  }

  override suspend fun startMonitoring(durationSeconds: Int) {
    val station = mutableState.value.nearestStationWithSlots ?: return
    stopMonitoring()

    mutableState.update {
      it.copy(
        alert = null,
        monitoring = TripMonitoringState(
          isActive = true,
          remainingSeconds = durationSeconds,
          totalSeconds = durationSeconds,
        ),
      )
    }

    countdownJob = scope.launch {
      var remaining = durationSeconds
      while (remaining > 0 && mutableState.value.monitoring.isActive) {
        delay(1_000L)
        remaining--
        mutableState.update { it.copy(monitoring = it.monitoring.copy(remainingSeconds = remaining)) }
      }
      if (mutableState.value.monitoring.isActive) {
        stopMonitoring()
      }
    }

    monitoringJob = scope.launch {
      var firstCheck = true
      while (mutableState.value.monitoring.isActive) {
        if (!firstCheck) delay(POLLING_INTERVAL_MILLIS)
        firstCheck = false

        val currentStation = mutableState.value.nearestStationWithSlots ?: break
        val availability = runCatching {
          biziApi.fetchAvailability(listOf(currentStation.id))
        }.getOrNull() ?: continue

        val updated = availability[currentStation.id] ?: continue
        val updatedStation = currentStation.copy(
          bikesAvailable = updated.bikesAvailable,
          slotsFree = updated.slotsFree,
        )
        mutableState.update { it.copy(nearestStationWithSlots = updatedStation) }

        if (updatedStation.slotsFree == 0) {
          val destination = mutableState.value.destination
          val alternative = if (destination != null) findAlternativeStation(updatedStation, destination.location) else null
          val alert = TripAlert(
            fullStation = updatedStation,
            alternativeStation = alternative,
            alternativeDistanceMeters = alternative?.distanceMeters,
          )
          stopMonitoring()
          mutableState.update { it.copy(alert = alert) }
          val altText = if (alternative != null) " Alternativa: ${alternative.name} (${alternative.distanceMeters}m)." else ""
          localNotifier.notify(
            title = "Estación llena: ${updatedStation.name}",
            body = "No quedan plazas libres.$altText",
          )
          break
        }
      }
    }
  }

  private suspend fun findAlternativeStation(fullStation: Station, destination: GeoPoint): Station? {
    val stations = stationsRepository.state.value.stations
    return stations
      .filter { it.id != fullStation.id }
      .map { it.copy(distanceMeters = distanceBetween(destination, it.location)) }
      .sortedBy { it.distanceMeters }
      .firstOrNull { it.slotsFree > 0 }
  }

  override fun stopMonitoring() {
    monitoringJob?.cancel()
    monitoringJob = null
    countdownJob?.cancel()
    countdownJob = null
    mutableState.update { it.copy(monitoring = TripMonitoringState(isActive = false)) }
  }

  override fun clearTrip() {
    stopMonitoring()
    mutableState.value = TripState()
  }

  override fun dismissAlert() {
    mutableState.update { it.copy(alert = null) }
  }

  override suspend fun doFinalBackgroundCheck() {
    val station = mutableState.value.nearestStationWithSlots ?: return
    if (!mutableState.value.monitoring.isActive) return

    val availability = runCatching {
      biziApi.fetchAvailability(listOf(station.id))
    }.getOrNull()

    val updated = availability?.get(station.id)
    if (updated != null && updated.slotsFree == 0) {
      val destination = mutableState.value.destination
      val alternative = if (destination != null) findAlternativeStation(station, destination.location) else null
      val altText = if (alternative != null) " Alternativa: ${alternative.name} (${alternative.distanceMeters}m)." else ""
      localNotifier.notify(
        title = "Estación llena: ${station.name}",
        body = "No quedan plazas libres.$altText",
      )
    } else {
      localNotifier.notify(
        title = "Monitorización pausada",
        body = "Abre la app para continuar vigilando ${station.name}.",
      )
    }
    stopMonitoring()
  }
}
