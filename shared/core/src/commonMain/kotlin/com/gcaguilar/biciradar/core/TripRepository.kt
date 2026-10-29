package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.auth.AuthProvider
import com.gcaguilar.biciradar.core.auth.AuthState
import com.gcaguilar.biciradar.core.backend.BiciRadarApiClient
import com.gcaguilar.biciradar.core.backend.PushMessage
import com.gcaguilar.biciradar.core.backend.PushMessageBus
import com.gcaguilar.biciradar.core.backend.RegisterDeviceRequest
import com.gcaguilar.biciradar.core.backend.StartMonitoringWindowRequest
import com.gcaguilar.biciradar.core.backend.TripWindowStore
import com.gcaguilar.biciradar.core.geo.currentTimeMs
import com.gcaguilar.biciradar.core.geo.distanceBetween
import com.gcaguilar.biciradar.core.push.DeviceTokenProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

/** Why [TripRepository.startMonitoring] could not open a backend window. */
enum class TripMonitoringFailure {
  SignInRequired,

  /** No push token: the backend would have nowhere to deliver the alert. */
  NotificationsUnavailable,
  BackendUnavailable,
}

data class TripState(
  val destination: TripDestination? = null,
  val nearestStationWithSlots: Station? = null,
  val distanceToStation: Int? = null,
  val monitoring: TripMonitoringState = TripMonitoringState(),
  val alert: TripAlert? = null,
  val isSearchingStation: Boolean = false,
  val searchError: String? = null,
  /** Backend monitoring-window id while a trip is being watched. */
  val activeWindowId: String? = null,
  /** Set when the last attempt to start watching failed; nothing is being watched. */
  val monitoringFailure: TripMonitoringFailure? = null,
)

interface TripRepository {
  val state: StateFlow<TripState>

  suspend fun setDestination(
    destination: TripDestination,
    searchRadiusMeters: Int,
  )

  suspend fun selectStation(station: Station) = Unit

  suspend fun startMonitoring(durationSeconds: Int)

  fun stopMonitoring()

  fun clearTrip()

  fun dismissAlert()

  /** Called by platform (e.g., iOS background task expiry) to do a final check and notify. */
  suspend fun doFinalBackgroundCheck()
}

/**
 * Implementación de TripRepository.
 * Registrado automáticamente en el grafo vía @ContributesBinding.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class TripRepositoryImpl(
  private val stationsRepository: StationsRepository,
  private val engagementRepository: EngagementRepository,
  private val scope: CoroutineScope,
  private val apiClient: BiciRadarApiClient,
  private val authProvider: AuthProvider,
  private val settingsRepository: SettingsRepository,
  private val pushMessageBus: PushMessageBus,
  private val tripWindowStore: TripWindowStore,
  private val deviceTokenProvider: DeviceTokenProvider,
) : TripRepository {
  private val mutableState = MutableStateFlow(TripState())
  override val state: StateFlow<TripState> = mutableState.asStateFlow()

  private var countdownJob: Job? = null

  init {
    // A trip does not survive process death: any persisted window is orphaned.
    val orphanWindow = tripWindowStore.current()
    if (orphanWindow != null) {
      scope.launch {
        if (apiClient.isConfigured) {
          runCatching { apiClient.cancelMonitoringWindow(orphanWindow.windowId) }
        }
        if (tripWindowStore.current()?.windowId == orphanWindow.windowId) {
          tripWindowStore.clear()
        }
      }
    }
    scope.launch {
      pushMessageBus.messages.collect(::handlePush)
    }
  }

  override suspend fun setDestination(
    destination: TripDestination,
    searchRadiusMeters: Int,
  ) {
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

  override suspend fun selectStation(station: Station) {
    stopMonitoring()
    val destination = mutableState.value.destination
    val distanceMeters =
      destination
        ?.let { distanceBetween(it.location, station.location) }
        ?: station.distanceMeters

    mutableState.update {
      it.copy(
        nearestStationWithSlots = station.copy(distanceMeters = distanceMeters),
        distanceToStation = distanceMeters,
        alert = null,
        isSearchingStation = false,
        searchError = null,
      )
    }
  }

  private suspend fun findNearestStation(
    location: GeoPoint,
    searchRadiusMeters: Int,
  ) {
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
    val stationsWithDistance =
      stations
        .map { station ->
          station.copy(distanceMeters = distanceBetween(location, station.location))
        }.sortedBy { it.distanceMeters }

    val selection =
      selectNearbyStationWithSlots(
        stations = stationsWithDistance,
        searchRadiusMeters = searchRadiusMeters,
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

    // The backend watches the station during an ephemeral window. `threshold = 1`
    // makes the detector emit only `DOCKS_EMPTY`, matching the previous
    // "alert when the station fills up" behaviour (plan §H). Without a window
    // nothing would ever notify the user, so a failure is reported instead of
    // showing a countdown that watches nothing.
    if (authProvider.credentialsState.value != AuthState.Authenticated) {
      mutableState.update { it.copy(monitoringFailure = TripMonitoringFailure.SignInRequired) }
      return
    }
    // The backend drops alerts for users without a registered device, so bind
    // this device right before opening the window instead of trusting that an
    // earlier subscription sync managed to do it.
    val pushToken = deviceTokenProvider.currentToken()?.takeIf { it.isNotBlank() }
    if (pushToken == null) {
      mutableState.update { it.copy(monitoringFailure = TripMonitoringFailure.NotificationsUnavailable) }
      return
    }
    val windowId =
      if (apiClient.isConfigured) {
        runCatching {
          apiClient.registerDevice(
            RegisterDeviceRequest(
              token = pushToken,
              platform = deviceTokenProvider.platform,
              appId = deviceTokenProvider.appId,
              locale = deviceTokenProvider.locale,
              pushEnvironment = deviceTokenProvider.pushEnvironment,
            ),
          )
          apiClient.startMonitoringWindow(
            StartMonitoringWindowRequest(
              cityId = settingsRepository.currentSelectedCity().id,
              stationId = station.id,
              kind = "ARRIVAL",
              durationSeconds = durationSeconds,
              threshold = 1,
            ),
          )
        }.onFailure { if (it is CancellationException) throw it }
          .getOrNull()
      } else {
        null
      }
    if (windowId == null) {
      mutableState.update { it.copy(monitoringFailure = TripMonitoringFailure.BackendUnavailable) }
      return
    }

    mutableState.update {
      it.copy(
        alert = null,
        activeWindowId = windowId,
        monitoringFailure = null,
        monitoring =
          TripMonitoringState(
            isActive = true,
            remainingSeconds = durationSeconds,
            totalSeconds = durationSeconds,
          ),
      )
    }
    tripWindowStore.save(windowId = windowId, stationId = station.id, startedAt = currentTimeMs())

    countdownJob =
      scope.launch {
        var remaining = durationSeconds
        while (remaining > 0 && mutableState.value.monitoring.isActive) {
          delay(1_000L)
          remaining--
          mutableState.update { it.copy(monitoring = it.monitoring.copy(remainingSeconds = remaining)) }
        }
        if (mutableState.value.monitoring.isActive) {
          if (remaining == 0) {
            scope.launch { engagementRepository.markMonitoringCompleted() }
          }
          stopMonitoring()
        }
      }
  }

  /** Terminal push for the active trip window: surface the alternative and cancel early. */
  private suspend fun handlePush(message: PushMessage) {
    val current = mutableState.value
    val windowId = current.activeWindowId ?: return
    if (message.routineId != windowId) return
    if (message.event !in TERMINAL_TRIP_EVENTS) return

    val fullStation =
      current.nearestStationWithSlots
        ?.copy(slotsFree = 0)
        ?: stationFor(message.stationId)
        ?: return
    val alternative = message.alternative?.let(::stationFor)
    cancelActiveWindow()
    mutableState.update {
      it.copy(
        monitoring = TripMonitoringState(isActive = false),
        activeWindowId = null,
        alert =
          TripAlert(
            fullStation = fullStation,
            alternativeStation = alternative,
            alternativeDistanceMeters = message.alternative?.distanceMeters,
          ),
      )
    }
    engagementRepository.markMonitoringCompleted()
  }

  private fun stationFor(stationId: String): Station? =
    stationsRepository.state.value.stations
      .firstOrNull { it.id == stationId }

  private fun stationFor(alternative: com.gcaguilar.biciradar.core.backend.PushAlternative): Station? {
    val existing = stationFor(alternative.stationId)
    return if (existing != null) {
      existing.copy(
        bikesAvailable = alternative.bikesAvailable,
        slotsFree = alternative.slotsFree,
        distanceMeters = alternative.distanceMeters,
      )
    } else {
      null
    }
  }

  private fun cancelActiveWindow() {
    val windowId = mutableState.value.activeWindowId
    tripWindowStore.clear()
    if (windowId == null) return
    scope.launch { runCatching { apiClient.cancelMonitoringWindow(windowId) } }
  }

  override fun stopMonitoring() {
    countdownJob?.cancel()
    countdownJob = null
    cancelActiveWindow()
    mutableState.update {
      it.copy(monitoring = TripMonitoringState(isActive = false), activeWindowId = null, monitoringFailure = null)
    }
  }

  override fun clearTrip() {
    stopMonitoring()
    mutableState.value = TripState()
  }

  override fun dismissAlert() {
    mutableState.update { it.copy(alert = null) }
  }

  /**
   * The backend guarantees window expiry, so there is no device-side final
   * check anymore; kept for API compatibility with the iOS background task.
   */
  override suspend fun doFinalBackgroundCheck() = Unit

  private companion object {
    val TERMINAL_TRIP_EVENTS = setOf("DOCKS_EMPTY", "DOCKS_FULL", "STATION_UNAVAILABLE")
  }
}
