package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.backend.MonitoringWindowService
import com.gcaguilar.biciradar.core.backend.MonitoringWindowStart
import com.gcaguilar.biciradar.core.geo.currentTimeMs
import dev.zacsweers.metro.Inject

@Inject
class GetFavoriteStations(
  private val favoritesRepository: FavoritesRepository,
  private val settingsRepository: SettingsRepository,
  private val stationsRepository: StationsRepository,
) {
  suspend fun execute(limit: Int? = null): List<SurfaceStationSnapshot> {
    stationsRepository.loadIfNeeded()
    val state = stationsRepository.state.value
    val cityId = settingsRepository.currentSelectedCity().id
    val lastUpdatedEpoch = state.lastUpdatedEpoch ?: currentTimeMs()
    val homeStationId = favoritesRepository.currentHomeStationId()
    val snapshots =
      state.stations
        .asSequence()
        .filter { favoritesRepository.isFavorite(it.id) }
        .sortedWith(
          compareByDescending<Station> { it.id == homeStationId }
            .thenBy { it.distanceMeters }
            .thenBy { it.name },
        ).map { station ->
          station.toSurfaceSnapshot(
            cityId = cityId,
            lastUpdatedEpoch = lastUpdatedEpoch,
            isFavorite = true,
          )
        }.toList()
    return limit?.let(snapshots::take) ?: snapshots
  }
}

@Inject
class GetNearestStations(
  private val favoritesRepository: FavoritesRepository,
  private val settingsRepository: SettingsRepository,
  private val stationsRepository: StationsRepository,
) {
  suspend fun execute(limit: Int = 3): List<SurfaceStationSnapshot> {
    stationsRepository.loadIfNeeded()
    val state = stationsRepository.state.value
    val cityId = settingsRepository.currentSelectedCity().id
    val lastUpdatedEpoch = state.lastUpdatedEpoch ?: currentTimeMs()
    return state.stations
      .sortedBy { it.distanceMeters }
      .take(limit)
      .map { station ->
        station.toSurfaceSnapshot(
          cityId = cityId,
          lastUpdatedEpoch = lastUpdatedEpoch,
          isFavorite = favoritesRepository.isFavorite(station.id),
        )
      }
  }
}

@Inject
class GetStationStatus(
  private val settingsRepository: SettingsRepository,
  private val stationsRepository: StationsRepository,
  private val surfaceSnapshotRepository: SurfaceSnapshotRepository,
) {
  suspend fun execute(stationId: String): SurfaceStationSnapshot? {
    surfaceSnapshotRepository.bootstrap()
    val cachedBundle = surfaceSnapshotRepository.currentBundle()
    cachedBundle?.favoriteStation?.takeIf { it.id == stationId }?.let { return it }
    cachedBundle?.homeStation?.takeIf { it.id == stationId }?.let { return it }
    cachedBundle?.workStation?.takeIf { it.id == stationId }?.let { return it }
    cachedBundle?.nearbyStations?.firstOrNull { it.id == stationId }?.let { return it }

    stationsRepository.loadIfNeeded()
    val state = stationsRepository.state.value
    val cityId = settingsRepository.currentSelectedCity().id
    val lastUpdatedEpoch = state.lastUpdatedEpoch ?: currentTimeMs()
    return state.stations
      .firstOrNull { it.id == stationId }
      ?.toSurfaceSnapshot(cityId = cityId, lastUpdatedEpoch = lastUpdatedEpoch)
  }
}

@Inject
class StartStationMonitoring(
  private val surfaceMonitoringRepository: SurfaceMonitoringRepository,
  private val monitoringWindowService: MonitoringWindowService,
) {
  /**
   * Abre la ventana del backend y, solo si se ha abierto, arranca la sesión de
   * superficie. Sin ventana no llegarían avisos, así que devolver `false` es
   * preferible a mostrar una cuenta atrás que no vigila nada.
   *
   * [openWindow] se pone a `false` cuando el dispositivo que pide la sesión no
   * puede recibir los avisos (el reloj no tiene sesión ni token push propios):
   * en ese caso la sesión es solo visual y los avisos los gestiona el teléfono.
   */
  suspend fun execute(
    stationId: String,
    durationSeconds: Int = DEFAULT_SURFACE_MONITORING_DURATION_SECONDS,
    kind: SurfaceMonitoringKind = SurfaceMonitoringKind.Docks,
    openWindow: Boolean = true,
  ): Boolean {
    if (!openWindow) {
      return surfaceMonitoringRepository.startMonitoring(
        stationId = stationId,
        durationSeconds = durationSeconds,
        kind = kind,
      )
    }
    // Reemplazar una sesión viva dejaría su ventana huérfana hasta caducar.
    surfaceMonitoringRepository.sessionFor(stationId)?.windowId?.let { previous ->
      monitoringWindowService.cancel(previous)
    }
    val start = monitoringWindowService.start(stationId, durationSeconds, kind)
    val windowId =
      when (start) {
        is MonitoringWindowStart.Started -> start.windowId
        is MonitoringWindowStart.Failed -> return false
      }
    val started =
      surfaceMonitoringRepository.startMonitoring(
        stationId = stationId,
        durationSeconds = durationSeconds,
        kind = kind,
        windowId = windowId,
      )
    if (!started) {
      monitoringWindowService.cancel(windowId)
    }
    return started
  }
}

@Inject
class StopStationMonitoring(
  private val surfaceMonitoringRepository: SurfaceMonitoringRepository,
  private val monitoringWindowService: MonitoringWindowService,
) {
  suspend fun execute(
    clear: Boolean = false,
    stationId: String? = null,
  ) {
    val sessions =
      if (clear) {
        surfaceMonitoringRepository.activeSessions()
      } else if (stationId == null) {
        surfaceMonitoringRepository.activeSessions()
      } else {
        listOfNotNull(surfaceMonitoringRepository.sessionFor(stationId))
      }
    sessions.forEach { session ->
      session.windowId?.let { monitoringWindowService.cancel(it) }
    }
    if (clear) {
      surfaceMonitoringRepository.clearMonitoring()
    } else {
      surfaceMonitoringRepository.stopMonitoring(stationId)
    }
  }
}

@Inject
class GetSuggestedAlternativeStation(
  private val settingsRepository: SettingsRepository,
  private val stationsRepository: StationsRepository,
) {
  suspend fun execute(
    stationId: String,
    kind: SurfaceMonitoringKind,
  ): SurfaceStationSnapshot? {
    stationsRepository.loadIfNeeded()
    val state = stationsRepository.state.value
    val monitoredStation = state.stations.firstOrNull { it.id == stationId } ?: return null
    val alternative =
      selectAlternativeStation(
        monitoredStation = monitoredStation,
        candidates = state.stations,
        kind = kind,
        maxRadiusMeters = settingsRepository.currentSearchRadiusMeters(),
      ) ?: return null
    return alternative.toSurfaceSnapshot(
      cityId = settingsRepository.currentSelectedCity().id,
      lastUpdatedEpoch = state.lastUpdatedEpoch ?: currentTimeMs(),
    )
  }
}

@Inject
class GetCachedStationSnapshot(
  private val surfaceSnapshotRepository: SurfaceSnapshotRepository,
) {
  suspend fun execute(): SurfaceSnapshotBundle? {
    surfaceSnapshotRepository.bootstrap()
    return surfaceSnapshotRepository.currentBundle()
  }
}

@Inject
class RefreshStationDataIfNeeded(
  private val stationsRepository: StationsRepository,
  private val surfaceSnapshotRepository: SurfaceSnapshotRepository,
) {
  suspend fun execute(forceRefresh: Boolean = false): SurfaceSnapshotBundle? {
    surfaceSnapshotRepository.bootstrap()
    if (forceRefresh) {
      stationsRepository.forceRefresh()
    } else {
      stationsRepository.loadIfNeeded()
    }
    surfaceSnapshotRepository.refreshSnapshot()
    return surfaceSnapshotRepository.currentBundle()
  }
}
