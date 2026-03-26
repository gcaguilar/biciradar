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
    val favoriteStationId = favoriteStationId(stations)
    val favoriteStation = favoriteStationId?.let { stationId ->
      stations.firstOrNull { it.id == stationId }?.toSurfaceSnapshot(
        cityId = city.id,
        lastUpdatedEpoch = stationsState.lastUpdatedEpoch ?: currentTimeMs(),
        isFavorite = true,
      )
    }
    val nearbyStations = stations
      .take(3)
      .map { station ->
        station.toSurfaceSnapshot(
          cityId = city.id,
          lastUpdatedEpoch = stationsState.lastUpdatedEpoch ?: currentTimeMs(),
          isFavorite = station.id == favoriteStationId,
        )
      }

    val previousMonitoring = mutableBundle.value?.monitoringSession
    val mergedFavorite = if (previousMonitoring != null && favoriteStation?.id == previousMonitoring.stationId) {
      favoriteStation.copy(
        bikesAvailable = previousMonitoring.bikesAvailable,
        docksAvailable = previousMonitoring.docksAvailable,
        statusLevel = previousMonitoring.statusLevel,
        statusTextShort = previousMonitoring.status.surfaceTextShort(previousMonitoring.kind),
        lastUpdatedEpoch = previousMonitoring.lastUpdatedEpoch,
        alternativeStationId = previousMonitoring.alternativeStationId,
        alternativeStationName = previousMonitoring.alternativeStationName,
        alternativeDistanceMeters = previousMonitoring.alternativeDistanceMeters,
      )
    } else {
      favoriteStation
    }

    val snapshot = SurfaceSnapshotBundle(
      generatedAtEpoch = currentTimeMs(),
      favoriteStation = mergedFavorite,
      nearbyStations = nearbyStations,
      monitoringSession = previousMonitoring,
      state = SurfaceState(
        hasLocationPermission = stationsState.userLocation != null,
        hasNotificationPermission = true,
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
    val updatedFavorite = current.favoriteStation?.let { favorite ->
      if (session != null && favorite.id == session.stationId) {
        favorite.copy(
          bikesAvailable = session.bikesAvailable,
          docksAvailable = session.docksAvailable,
          statusLevel = session.statusLevel,
          statusTextShort = session.status.surfaceTextShort(session.kind),
          lastUpdatedEpoch = session.lastUpdatedEpoch,
          alternativeStationId = session.alternativeStationId,
          alternativeStationName = session.alternativeStationName,
          alternativeDistanceMeters = session.alternativeDistanceMeters,
        )
      } else {
        favorite
      }
    }
    persist(
      current.copy(
        generatedAtEpoch = currentTimeMs(),
        favoriteStation = updatedFavorite,
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
        hasNotificationPermission = true,
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

private fun SurfaceMonitoringStatus.surfaceTextShort(kind: SurfaceMonitoringKind): String = when (this) {
  SurfaceMonitoringStatus.Monitoring -> "Monitorizando"
  SurfaceMonitoringStatus.ChangedToEmpty -> if (kind == SurfaceMonitoringKind.Bikes) "Sin bicis" else "Cambio"
  SurfaceMonitoringStatus.ChangedToFull -> if (kind == SurfaceMonitoringKind.Docks) "Sin huecos" else "Cambio"
  SurfaceMonitoringStatus.AlternativeAvailable -> "Alternativa"
  SurfaceMonitoringStatus.Ended -> "Finalizada"
  SurfaceMonitoringStatus.Expired -> "Expirada"
}
