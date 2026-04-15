package com.gcaguilar.biciradar.core

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.gcaguilar.biciradar.core.geo.currentTimeMs
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import com.gcaguilar.biciradar.core.local.loadSurfaceBundleFromDb
import com.gcaguilar.biciradar.core.local.persistSurfaceBundleRelational
import com.gcaguilar.biciradar.core.local.surfaceBundleFromRows
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
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

/**
 * Implementación de SurfaceSnapshotRepository.
 * Registrado automáticamente en el grafo vía @ContributesBinding.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class SurfaceSnapshotRepositoryImpl(
  private val fileSystem: FileSystem,
  private val json: Json,
  private val localNotifier: LocalNotifier,
  private val storageDirectoryProvider: StorageDirectoryProvider,
  private val settingsRepository: SettingsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val stationsRepository: StationsRepository,
  private val scope: CoroutineScope,
  private val database: BiciRadarDatabase? = null,
) : SurfaceSnapshotRepository {
  private val mutableBundle = MutableStateFlow<SurfaceSnapshotBundle?>(null)
  private var bootstrapped = false

  override val bundle: StateFlow<SurfaceSnapshotBundle?> = mutableBundle.asStateFlow()

  init {
    val db = database
    if (db != null) {
      scope.launch {
        combine(
          db.biciradarQueries
            .getSurfaceHeader()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default),
          db.biciradarQueries
            .getAllSurfaceStationRows()
            .asFlow()
            .mapToList(Dispatchers.Default),
          db.biciradarQueries
            .getSurfaceMonitoring()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default),
        ) { header, rows, mon ->
          if (header == null) {
            null
          } else {
            surfaceBundleFromRows(header, rows, mon)
          }
        }.collect { mutableBundle.value = it }
      }
    }
  }

  override suspend fun bootstrap() {
    if (bootstrapped) return
    mutableBundle.value = readPersistedBundle()
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

    fun stationSnapshot(stationId: String?): SurfaceStationSnapshot? =
      stationId?.let { id ->
        stations.firstOrNull { it.id == id }?.toSurfaceSnapshot(
          cityId = city.id,
          lastUpdatedEpoch = lastUpdatedEpoch,
          isFavorite = favoritesRepository.isFavorite(id),
        )
      }
    val favoriteStation = stationSnapshot(favoriteStationId)?.copy(isFavorite = true)
    val homeStation = stationSnapshot(homeStationId)
    val workStation =
      workStationId
        ?.takeIf { it != homeStationId }
        ?.let(::stationSnapshot)
    val hasLocationPermission = stationsState.userLocation != null
    val nearbyStations =
      if (hasLocationPermission) {
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

    val snapshot =
      SurfaceSnapshotBundle(
        generatedAtEpoch = currentTimeMs(),
        favoriteStation = mergedFavorite,
        homeStation = mergedHome,
        workStation = mergedWork,
        nearbyStations = nearbyStations,
        monitoringSession = previousMonitoring,
        state =
          SurfaceState(
            hasLocationPermission = hasLocationPermission,
            hasNotificationPermission = hasNotificationPermission,
            hasFavoriteStation = mergedFavorite != null,
            isDataFresh =
              stationsState.lastUpdatedEpoch?.let { currentTimeMs() - it < STATION_CACHE_REFRESH_INTERVAL_MS } == true,
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
    if (database != null) {
      val persisted = persistToDatabase(snapshot)
      if (persisted) {
        deleteLegacyFile()
        mutableBundle.value = snapshot
      } else {
        persistToFile(snapshot)
        mutableBundle.value = snapshot
      }
    } else {
      persistToFile(snapshot)
      mutableBundle.value = snapshot
    }
  }

  private fun emptyBundle(): SurfaceSnapshotBundle {
    val city = settingsRepository.currentSelectedCity()
    return SurfaceSnapshotBundle(
      generatedAtEpoch = currentTimeMs(),
      state =
        SurfaceState(
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

  private fun readPersistedBundle(): SurfaceSnapshotBundle? {
    if (database == null) return readFromFile()
    val dbBundle = readFromDatabase()
    val legacyBundle = readFromFile()
    if (legacyBundle == null) return dbBundle
    if (dbBundle != null) {
      deleteLegacyFile()
      return dbBundle
    }
    val migrated = persistToDatabase(legacyBundle)
    if (migrated) {
      deleteLegacyFile()
      return legacyBundle
    }
    return legacyBundle
  }

  private fun readFromDatabase(): SurfaceSnapshotBundle? {
    val db = database ?: return null
    return runCatching { loadSurfaceBundleFromDb(db) }.getOrNull()
  }

  private fun readFromFile(): SurfaceSnapshotBundle? {
    val path = snapshotPath()
    if (!fileSystem.exists(path)) return null
    return runCatching {
      json.decodeFromString<SurfaceSnapshotBundle>(fileSystem.read(path) { readUtf8() })
    }.getOrNull()
  }

  private fun persistToDatabase(snapshot: SurfaceSnapshotBundle): Boolean {
    val db = database ?: return false
    return runCatching {
      persistSurfaceBundleRelational(db, snapshot)
    }.isSuccess
  }

  private fun persistToFile(snapshot: SurfaceSnapshotBundle) {
    val path = snapshotPath()
    val parent = path.parent ?: return
    fileSystem.createDirectories(parent)
    fileSystem.write(path) {
      writeUtf8(json.encodeToString(snapshot))
    }
  }

  private fun deleteLegacyFile() {
    val path = snapshotPath()
    if (fileSystem.exists(path)) {
      runCatching { fileSystem.delete(path) }
    }
  }
}

private fun SurfaceStationSnapshot?.mergeMonitoring(session: SurfaceMonitoringSession?): SurfaceStationSnapshot? {
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

private fun SurfaceMonitoringStatus.surfaceTextShort(kind: SurfaceMonitoringKind): String =
  when (this) {
    SurfaceMonitoringStatus.Monitoring -> "Monitorizando"
    SurfaceMonitoringStatus.ChangedToEmpty -> if (kind == SurfaceMonitoringKind.Bikes) "Sin bicis" else "Cambio"
    SurfaceMonitoringStatus.ChangedToFull -> if (kind == SurfaceMonitoringKind.Docks) "Sin huecos" else "Cambio"
    SurfaceMonitoringStatus.AlternativeAvailable -> "Alternativa"
    SurfaceMonitoringStatus.Ended -> "Finalizada"
    SurfaceMonitoringStatus.Expired -> "Expirada"
  }
