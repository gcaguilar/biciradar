package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.currentTimeMs
import com.gcaguilar.biciradar.core.geo.distanceBetween
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Gestiona una o varias sesiones de monitorización concurrentes, indexadas por
 * `stationId`. Una rutina puede arrancar varias estaciones a la vez.
 *
 * Desde la migración al backend la comprobación real de estaciones vive en el
 * servidor y los avisos llegan por push: aquí sólo queda el estado de
 * visualización (cuenta atrás, caducidad y persistencia para widgets/reloj).
 */
interface SurfaceMonitoringRepository {
  /** Sesiones conocidas por estación (activas y finalizadas recientes). */
  val state: StateFlow<Map<String, SurfaceMonitoringSession>>

  suspend fun bootstrap()

  suspend fun startMonitoring(
    stationId: String,
    durationSeconds: Int = DEFAULT_SURFACE_MONITORING_DURATION_SECONDS,
    kind: SurfaceMonitoringKind = SurfaceMonitoringKind.Docks,
  ): Boolean

  suspend fun startMonitoringFavoriteStation(
    durationSeconds: Int = DEFAULT_SURFACE_MONITORING_DURATION_SECONDS,
    kind: SurfaceMonitoringKind = SurfaceMonitoringKind.Docks,
  ): Boolean

  /** Detiene la sesión de [stationId], o todas las sesiones activas si es `null`. */
  fun stopMonitoring(stationId: String? = null)

  suspend fun clearMonitoring()

  fun sessionFor(stationId: String): SurfaceMonitoringSession?

  fun activeSessions(): List<SurfaceMonitoringSession>
}

/**
 * Implementación de SurfaceMonitoringRepository.
 * Registrado automáticamente en el grafo vía @ContributesBinding.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class SurfaceMonitoringRepositoryImpl(
  private val favoritesRepository: FavoritesRepository,
  private val engagementRepository: EngagementRepository,
  private val scope: CoroutineScope,
  private val settingsRepository: SettingsRepository,
  private val stationsRepository: StationsRepository,
  private val surfaceSnapshotRepository: SurfaceSnapshotRepository,
) : SurfaceMonitoringRepository {
  private val mutableState = MutableStateFlow<Map<String, SurfaceMonitoringSession>>(emptyMap())

  @kotlin.concurrent.Volatile private var bootstrapped = false
  private val countdownJobs = mutableMapOf<String, Job>()
  private val finishMutex = Mutex()

  override val state: StateFlow<Map<String, SurfaceMonitoringSession>> = mutableState.asStateFlow()

  override suspend fun bootstrap() {
    if (bootstrapped) return
    surfaceSnapshotRepository.bootstrap()
    val bundle = surfaceSnapshotRepository.currentBundle()
    val persisted =
      bundle?.monitoringSessions?.takeIf { it.isNotEmpty() }
        ?: listOfNotNull(bundle?.monitoringSession)
    val nowEpoch = currentTimeMs()
    val restored = mutableMapOf<String, SurfaceMonitoringSession>()
    persisted.forEach { session ->
      when {
        session.isActive && session.expiresAtEpoch > nowEpoch -> {
          restored[session.stationId] = session
          startWorkers(session.stationId)
        }

        session.isActive -> {
          restored[session.stationId] =
            session.copy(
              isActive = false,
              status = SurfaceMonitoringStatus.Expired,
              lastUpdatedEpoch = nowEpoch,
            )
        }

        else -> {
          restored[session.stationId] = session
        }
      }
    }
    mutableState.value = restored
    if (persisted.any { it.isActive && it.expiresAtEpoch <= nowEpoch }) {
      saveSessions()
    }
    bootstrapped = true
  }

  override suspend fun startMonitoring(
    stationId: String,
    durationSeconds: Int,
    kind: SurfaceMonitoringKind,
  ): Boolean {
    if (!bootstrapped) bootstrap()
    stationsRepository.loadIfNeeded()
    val station = stationsRepository.stationById(stationId) ?: return false
    val nowEpoch = currentTimeMs()
    val session =
      SurfaceMonitoringSession(
        stationId = station.id,
        stationName = station.name,
        cityId = settingsRepository.currentSelectedCity().id,
        kind = kind,
        status = SurfaceMonitoringStatus.Monitoring,
        bikesAvailable = station.bikesAvailable,
        docksAvailable = station.slotsFree,
        statusLevel = station.surfaceStatusLevel(),
        startedAtEpoch = nowEpoch,
        expiresAtEpoch = nowEpoch + (durationSeconds.coerceAtLeast(60) * 1000L),
        lastUpdatedEpoch = nowEpoch,
        isActive = true,
      )
    cancelWorkers(stationId)
    mutableState.value = mutableState.value + (station.id to session)
    saveSessions()
    startWorkers(station.id)
    return true
  }

  override suspend fun startMonitoringFavoriteStation(
    durationSeconds: Int,
    kind: SurfaceMonitoringKind,
  ): Boolean {
    if (!bootstrapped) bootstrap()
    stationsRepository.loadIfNeeded()
    val stations = stationsRepository.state.value.stations
    val homeStationId = favoritesRepository.currentHomeStationId()
    val favoriteStationId =
      when {
        homeStationId != null && stations.any { it.id == homeStationId } -> homeStationId
        else -> stations.firstOrNull { favoritesRepository.isFavorite(it.id) }?.id
      } ?: return false
    return startMonitoring(favoriteStationId, durationSeconds, kind)
  }

  override fun stopMonitoring(stationId: String?) {
    val current = mutableState.value
    val targets = if (stationId == null) current.values.toList() else listOfNotNull(current[stationId])
    if (targets.isEmpty()) return
    val nowEpoch = currentTimeMs()
    val updated = current.toMutableMap()
    targets.forEach { session ->
      cancelWorkers(session.stationId)
      updated[session.stationId] =
        session.copy(
          isActive = false,
          status = SurfaceMonitoringStatus.Ended,
          lastUpdatedEpoch = nowEpoch,
        )
    }
    mutableState.value = updated
    scope.launch { saveSessions() }
  }

  override suspend fun clearMonitoring() {
    mutableState.value.keys
      .toList()
      .forEach(::cancelWorkers)
    mutableState.value = emptyMap()
    surfaceSnapshotRepository.saveMonitoringSessions(emptyList())
  }

  override fun sessionFor(stationId: String): SurfaceMonitoringSession? = mutableState.value[stationId]

  override fun activeSessions(): List<SurfaceMonitoringSession> = mutableState.value.values.filter { it.isActive }

  private fun startWorkers(stationId: String) {
    cancelWorkers(stationId)
    countdownJobs[stationId] =
      scope.launch {
        while (true) {
          delay(1_000L)
          val current = mutableState.value[stationId] ?: break
          if (!current.isActive) break
          if (current.remainingSeconds() <= 0) {
            finishMonitoring(stationId, status = SurfaceMonitoringStatus.Expired)
            break
          }
        }
      }
  }

  private suspend fun finishMonitoring(
    stationId: String,
    status: SurfaceMonitoringStatus,
    alternative: Station? = null,
  ) {
    finishMutex.withLock {
      if (mutableState.value[stationId]?.isActive != true) return
    }
    val current = mutableState.value[stationId] ?: return
    cancelWorkers(stationId)
    val finished =
      current.copy(
        isActive = false,
        status = status,
        lastUpdatedEpoch = currentTimeMs(),
        alternativeStationId = alternative?.id ?: current.alternativeStationId,
        alternativeStationName = alternative?.name ?: current.alternativeStationName,
        alternativeDistanceMeters = alternative?.distanceMeters ?: current.alternativeDistanceMeters,
      )
    mutableState.value = mutableState.value + (stationId to finished)
    saveSessions()
    engagementRepository.markMonitoringCompleted()
  }

  private suspend fun saveSessions() {
    surfaceSnapshotRepository.saveMonitoringSessions(mutableState.value.values.toList())
  }

  private fun cancelWorkers(stationId: String) {
    countdownJobs.remove(stationId)?.cancel()
  }
}

internal fun selectAlternativeStation(
  monitoredStation: Station,
  candidates: List<Station>,
  kind: SurfaceMonitoringKind,
  maxRadiusMeters: Int,
): Station? =
  candidates
    .asSequence()
    .filter { it.id != monitoredStation.id }
    .map { candidate ->
      candidate.copy(distanceMeters = distanceBetween(monitoredStation.location, candidate.location))
    }.filter { candidate ->
      candidate.distanceMeters <= maxRadiusMeters &&
        when (kind) {
          SurfaceMonitoringKind.Bikes -> candidate.bikesAvailable > 0
          SurfaceMonitoringKind.Docks -> candidate.slotsFree > 0
        }
    }.sortedWith(
      compareByDescending<Station> {
        when (kind) {
          SurfaceMonitoringKind.Bikes -> it.bikesAvailable
          SurfaceMonitoringKind.Docks -> it.slotsFree
        }
      }.thenBy { it.distanceMeters },
    ).firstOrNull()
