package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.currentTimeMs
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

private const val SURFACE_MONITORING_POLLING_INTERVAL_MILLIS = 30_000L

interface SurfaceMonitoringRepository {
  val state: StateFlow<SurfaceMonitoringSession?>

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

  fun stopMonitoring()

  suspend fun clearMonitoring()
}

/**
 * Implementación de SurfaceMonitoringRepository.
 * Registrado automáticamente en el grafo vía @ContributesBinding.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class SurfaceMonitoringRepositoryImpl(
  private val biziApi: BiziApi,
  private val favoritesRepository: FavoritesRepository,
  private val localNotifier: LocalNotifier,
  private val engagementRepository: EngagementRepository,
  private val scope: CoroutineScope,
  private val settingsRepository: SettingsRepository,
  private val stationsRepository: StationsRepository,
  private val surfaceSnapshotRepository: SurfaceSnapshotRepository,
) : SurfaceMonitoringRepository {
  private val mutableState = MutableStateFlow<SurfaceMonitoringSession?>(null)
  private var bootstrapped = false
  private var countdownJob: Job? = null
  private var monitoringJob: Job? = null

  override val state: StateFlow<SurfaceMonitoringSession?> = mutableState.asStateFlow()

  override suspend fun bootstrap() {
    if (bootstrapped) return
    surfaceSnapshotRepository.bootstrap()
    val persisted = surfaceSnapshotRepository.currentBundle()?.monitoringSession
    if (persisted != null) {
      val nowEpoch = currentTimeMs()
      if (persisted.isActive && persisted.expiresAtEpoch > nowEpoch) {
        mutableState.value = persisted
        startWorkers(persisted.stationId, persisted.kind)
      } else if (persisted.isActive) {
        val expired =
          persisted.copy(
            isActive = false,
            status = SurfaceMonitoringStatus.Expired,
            lastUpdatedEpoch = nowEpoch,
          )
        mutableState.value = expired
        surfaceSnapshotRepository.saveMonitoringSession(expired)
      } else {
        mutableState.value = persisted
      }
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
    stopInternal(updateState = false)
    mutableState.value = session
    surfaceSnapshotRepository.saveMonitoringSession(session)
    startWorkers(station.id, kind)
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

  override fun stopMonitoring() {
    val current = mutableState.value ?: return
    scope.launch {
      val ended =
        current.copy(
          isActive = false,
          status = SurfaceMonitoringStatus.Ended,
          lastUpdatedEpoch = currentTimeMs(),
        )
      stopInternal(updateState = false)
      mutableState.value = ended
      surfaceSnapshotRepository.saveMonitoringSession(ended)
    }
  }

  override suspend fun clearMonitoring() {
    stopInternal(updateState = true)
    surfaceSnapshotRepository.saveMonitoringSession(null)
  }

  private fun startWorkers(
    stationId: String,
    kind: SurfaceMonitoringKind,
  ) {
    countdownJob?.cancel()
    monitoringJob?.cancel()
    countdownJob =
      scope.launch {
        while (true) {
          delay(1_000L)
          val current = mutableState.value ?: break
          if (!current.isActive) break
          if (current.remainingSeconds() <= 0) {
            finishMonitoring(status = SurfaceMonitoringStatus.Expired)
            break
          }
        }
      }
    monitoringJob =
      scope.launch {
        stationsRepository.loadIfNeeded()
        var firstCheck = true
        while (true) {
          val current = mutableState.value ?: break
          if (!current.isActive) break
          if (!firstCheck) delay(SURFACE_MONITORING_POLLING_INTERVAL_MILLIS)
          firstCheck = false

          val availability =
            runCatching {
              biziApi.fetchAvailability(listOf(stationId))
            }.getOrNull() ?: continue

          val update = availability[stationId] ?: continue
          val nowEpoch = currentTimeMs()
          val currentStation = stationsRepository.stationById(stationId)
          val updatedStation =
            (
              currentStation ?: Station(
                id = current.stationId,
                name = current.stationName,
                address = current.stationName,
                location = GeoPoint(0.0, 0.0),
                bikesAvailable = current.bikesAvailable,
                slotsFree = current.docksAvailable,
                distanceMeters = 0,
              )
            ).copy(
              bikesAvailable = update.bikesAvailable,
              slotsFree = update.slotsFree,
            )

          val alternative =
            when {
              kind == SurfaceMonitoringKind.Bikes && updatedStation.bikesAvailable == 0 ->
                findAlternativeStation(
                  updatedStation,
                  kind,
                )
              kind == SurfaceMonitoringKind.Docks && updatedStation.slotsFree == 0 ->
                findAlternativeStation(
                  updatedStation,
                  kind,
                )
              else -> null
            }

          val status =
            when {
              kind == SurfaceMonitoringKind.Bikes && updatedStation.bikesAvailable == 0 && alternative != null -> SurfaceMonitoringStatus.AlternativeAvailable
              kind == SurfaceMonitoringKind.Docks && updatedStation.slotsFree == 0 && alternative != null -> SurfaceMonitoringStatus.AlternativeAvailable
              kind == SurfaceMonitoringKind.Bikes && updatedStation.bikesAvailable == 0 -> SurfaceMonitoringStatus.ChangedToEmpty
              kind == SurfaceMonitoringKind.Docks && updatedStation.slotsFree == 0 -> SurfaceMonitoringStatus.ChangedToFull
              else -> SurfaceMonitoringStatus.Monitoring
            }

          val session =
            current.copy(
              bikesAvailable = updatedStation.bikesAvailable,
              docksAvailable = updatedStation.slotsFree,
              status = status,
              statusLevel = updatedStation.surfaceStatusLevel(),
              lastUpdatedEpoch = nowEpoch,
              alternativeStationId = alternative?.id,
              alternativeStationName = alternative?.name,
              alternativeDistanceMeters = alternative?.distanceMeters,
            )
          mutableState.value = session
          surfaceSnapshotRepository.saveMonitoringSession(session)

          if (status != SurfaceMonitoringStatus.Monitoring) {
            localNotifier.notify(
              title = monitoringTitle(session),
              body = monitoringBody(session),
            )
            finishMonitoring(status = status, alternative = alternative)
            break
          }
        }
      }
  }

  private suspend fun finishMonitoring(
    status: SurfaceMonitoringStatus,
    alternative: Station? = null,
  ) {
    val current = mutableState.value ?: return
    stopInternal(updateState = false)
    val finished =
      current.copy(
        isActive = false,
        status = status,
        lastUpdatedEpoch = currentTimeMs(),
        alternativeStationId = alternative?.id ?: current.alternativeStationId,
        alternativeStationName = alternative?.name ?: current.alternativeStationName,
        alternativeDistanceMeters = alternative?.distanceMeters ?: current.alternativeDistanceMeters,
      )
    mutableState.value = finished
    surfaceSnapshotRepository.saveMonitoringSession(finished)
    engagementRepository.markMonitoringCompleted()
  }

  private fun stopInternal(updateState: Boolean) {
    countdownJob?.cancel()
    countdownJob = null
    monitoringJob?.cancel()
    monitoringJob = null
    if (updateState) {
      mutableState.value = null
    }
  }

  private fun findAlternativeStation(
    monitoredStation: Station,
    kind: SurfaceMonitoringKind,
  ): Station? =
    selectAlternativeStation(
      monitoredStation = monitoredStation,
      candidates = stationsRepository.state.value.stations,
      kind = kind,
      maxRadiusMeters = settingsRepository.currentSearchRadiusMeters(),
    )

  private fun monitoringTitle(session: SurfaceMonitoringSession): String =
    when (session.status) {
      SurfaceMonitoringStatus.ChangedToEmpty -> "Estacion vacia: ${session.stationName}"
      SurfaceMonitoringStatus.ChangedToFull -> "Estacion llena: ${session.stationName}"
      SurfaceMonitoringStatus.AlternativeAvailable -> "Alternativa disponible"
      SurfaceMonitoringStatus.Ended -> "Monitorizacion detenida"
      SurfaceMonitoringStatus.Expired -> "Monitorizacion expirada"
      SurfaceMonitoringStatus.Monitoring -> session.stationName
    }

  private fun monitoringBody(session: SurfaceMonitoringSession): String {
    val alternative =
      if (session.alternativeStationName != null) {
        " Alternativa: ${session.alternativeStationName}" +
          session.alternativeDistanceMeters?.let { " ($it m)." }.orEmpty()
      } else {
        ""
      }
    return when (session.status) {
      SurfaceMonitoringStatus.ChangedToEmpty -> "Ya no quedan bicis en ${session.stationName}.$alternative"
      SurfaceMonitoringStatus.ChangedToFull -> "Ya no quedan huecos libres en ${session.stationName}.$alternative"
      SurfaceMonitoringStatus.AlternativeAvailable -> "${session.stationName} ya no sirve. Tienes una alternativa cercana.$alternative"
      SurfaceMonitoringStatus.Ended -> "Has detenido la monitorizacion de ${session.stationName}."
      SurfaceMonitoringStatus.Expired -> "La monitorizacion de ${session.stationName} ha terminado."
      SurfaceMonitoringStatus.Monitoring -> "${session.stationName}: ${session.bikesAvailable} bicis y ${session.docksAvailable} huecos."
    }
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
