package com.gcaguilar.biciradar.core

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
) {
  suspend fun execute(
    stationId: String,
    durationSeconds: Int = DEFAULT_SURFACE_MONITORING_DURATION_SECONDS,
    kind: SurfaceMonitoringKind = SurfaceMonitoringKind.Docks,
  ): Boolean =
    surfaceMonitoringRepository.startMonitoring(
      stationId = stationId,
      durationSeconds = durationSeconds,
      kind = kind,
    )
}

@Inject
class StopStationMonitoring(
  private val surfaceMonitoringRepository: SurfaceMonitoringRepository,
) {
  suspend fun execute(clear: Boolean = false) {
    if (clear) {
      surfaceMonitoringRepository.clearMonitoring()
    } else {
      surfaceMonitoringRepository.stopMonitoring()
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
