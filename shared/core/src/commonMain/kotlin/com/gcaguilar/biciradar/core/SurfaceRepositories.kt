package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.currentTimeMs
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

interface SurfaceSnapshotRepository {
  val bundle: StateFlow<SurfaceSnapshotBundle?>
  suspend fun bootstrap()
  suspend fun refreshSnapshot()
  suspend fun saveMonitoringSession(session: SurfaceMonitoringSession?)
  fun currentBundle(): SurfaceSnapshotBundle?
}

@Inject
class SurfaceSnapshotRepositoryImpl(
  private val fileSystem: FileSystem,
  private val json: Json,
  private val localNotifier: LocalNotifier,
  private val storageDirectoryProvider: StorageDirectoryProvider,
  private val settingsRepository: SettingsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val stationsRepository: StationsRepository,
) : SurfaceSnapshotRepository {
  private val mutableBundle = MutableStateFlow<SurfaceSnapshotBundle?>(null)
  private var bootstrapped = false

  override val bundle: StateFlow<SurfaceSnapshotBundle?> = mutableBundle.asStateFlow()

  override suspend fun bootstrap() {
    if (bootstrapped) return
    val path = snapshotPath()
    mutableBundle.value = if (fileSystem.exists(path)) {
      runCatching {
        json.decodeFromString<SurfaceSnapshotBundle>(fileSystem.read(path) { readUtf8() })
      }.getOrNull()
    } else {
      null
    }
    bootstrapped = true
  }

  override suspend fun refreshSnapshot() {
    if (!bootstrapped) bootstrap()
    val city = settingsRepository.currentSelectedCity()
    val stationsState = stationsRepository.state.value
    val stations = stationsState.stations
    val lastUpdatedEpoch = stationsState.lastUpdatedEpoch ?: currentTimeMs()
    val favoriteStationId = favoriteStationId(stations)
    val homeStationId = favoritesRepository.currentHomeStationId()
    val workStationId = favoritesRepository.currentWorkStationId()
    fun stationSnapshot(stationId: String?): SurfaceStationSnapshot? = stationId?.let { id ->
      stations.firstOrNull { it.id == id }?.toSurfaceSnapshot(
        cityId = city.id,
        lastUpdatedEpoch = lastUpdatedEpoch,
        isFavorite = favoritesRepository.isFavorite(id),
      )
    }
    val favoriteStation = stationSnapshot(favoriteStationId)?.copy(isFavorite = true)
    val homeStation = stationSnapshot(homeStationId)
    val workStation = workStationId
      ?.takeIf { it != homeStationId }
      ?.let(::stationSnapshot)
    val hasLocationPermission = stationsState.userLocation != null
    val nearbyStations = if (hasLocationPermission) {
      stations
        .sortedBy { it.distanceMeters }
        .take(3)
        .map { station ->
          station.toSurfaceSnapshot(
            cityId = city.id,
            lastUpdatedEpoch = lastUpdatedEpoch,
            isFavorite = station.id == favoriteStationId,
          )
        }
    } else {
      emptyList()
    }
    val hasNotificationPermission = localNotifier.hasPermission()

    val previousMonitoring = mutableBundle.value?.monitoringSession
    val mergedFavorite = favoriteStation.mergeMonitoring(previousMonitoring)
    val mergedHome = homeStation.mergeMonitoring(previousMonitoring)
    val mergedWork = workStation.mergeMonitoring(previousMonitoring)

    val snapshot = SurfaceSnapshotBundle(
      generatedAtEpoch = currentTimeMs(),
      favoriteStation = mergedFavorite,
      homeStation = mergedHome,
      workStation = mergedWork,
      nearbyStations = nearbyStations,
      monitoringSession = previousMonitoring,
      state = SurfaceState(
        hasLocationPermission = hasLocationPermission,
        hasNotificationPermission = hasNotificationPermission,
        hasFavoriteStation = mergedFavorite != null,
        isDataFresh = stationsState.lastUpdatedEpoch?.let { currentTimeMs() - it < STATION_CACHE_REFRESH_INTERVAL_MS } == true,
        lastSyncEpoch = stationsState.lastUpdatedEpoch,
        cityId = city.id,
        cityName = city.displayName,
        userLatitude = stationsState.userLocation?.latitude,
        userLongitude = stationsState.userLocation?.longitude,
      ),
    )
    persist(snapshot)
  }

  override suspend fun saveMonitoringSession(session: SurfaceMonitoringSession?) {
    if (!bootstrapped) bootstrap()
    val current = mutableBundle.value ?: emptyBundle()
    val updatedFavorite = current.favoriteStation.mergeMonitoring(session)
    val updatedHome = current.homeStation.mergeMonitoring(session)
    val updatedWork = current.workStation.mergeMonitoring(session)
    persist(
      current.copy(
        generatedAtEpoch = currentTimeMs(),
        favoriteStation = updatedFavorite,
        homeStation = updatedHome,
        workStation = updatedWork,
        monitoringSession = session,
      ),
    )
  }

  override fun currentBundle(): SurfaceSnapshotBundle? = mutableBundle.value

  private fun favoriteStationId(stations: List<Station>): String? {
    val homeStationId = favoritesRepository.currentHomeStationId()
    if (homeStationId != null && stations.any { it.id == homeStationId }) return homeStationId
    return stations.firstOrNull { favoritesRepository.isFavorite(it.id) }?.id
  }

  private fun persist(snapshot: SurfaceSnapshotBundle) {
    val path = snapshotPath()
    val parent = path.parent ?: return
    fileSystem.createDirectories(parent)
    fileSystem.write(path) {
      writeUtf8(json.encodeToString(snapshot))
    }
    mutableBundle.value = snapshot
  }

  private fun emptyBundle(): SurfaceSnapshotBundle {
    val city = settingsRepository.currentSelectedCity()
    return SurfaceSnapshotBundle(
      generatedAtEpoch = currentTimeMs(),
      state = SurfaceState(
        hasLocationPermission = false,
        hasNotificationPermission = false,
        hasFavoriteStation = false,
        isDataFresh = false,
        lastSyncEpoch = null,
        cityId = city.id,
        cityName = city.displayName,
      ),
    )
  }

  private fun snapshotPath() = "${storageDirectoryProvider.rootPath}/surface_snapshot.json".toPath()
}

private fun SurfaceStationSnapshot?.mergeMonitoring(
  session: SurfaceMonitoringSession?,
): SurfaceStationSnapshot? {
  val station = this ?: return null
  if (session == null || station.id != session.stationId) return station
  return station.copy(
    bikesAvailable = session.bikesAvailable,
    docksAvailable = session.docksAvailable,
    statusLevel = session.statusLevel,
    statusTextShort = session.status.surfaceTextShort(session.kind),
    lastUpdatedEpoch = session.lastUpdatedEpoch,
    alternativeStationId = session.alternativeStationId,
    alternativeStationName = session.alternativeStationName,
    alternativeDistanceMeters = session.alternativeDistanceMeters,
  )
}

private fun SurfaceMonitoringStatus.surfaceTextShort(kind: SurfaceMonitoringKind): String = when (this) {
  SurfaceMonitoringStatus.Monitoring -> "Monitorizando"
  SurfaceMonitoringStatus.ChangedToEmpty -> if (kind == SurfaceMonitoringKind.Bikes) "Sin bicis" else "Cambio"
  SurfaceMonitoringStatus.ChangedToFull -> if (kind == SurfaceMonitoringKind.Docks) "Sin huecos" else "Cambio"
  SurfaceMonitoringStatus.AlternativeAvailable -> "Alternativa"
  SurfaceMonitoringStatus.Ended -> "Finalizada"
  SurfaceMonitoringStatus.Expired -> "Expirada"
}
