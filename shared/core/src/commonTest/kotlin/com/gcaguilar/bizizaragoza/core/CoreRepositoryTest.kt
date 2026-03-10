package com.gcaguilar.bizizaragoza.core

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import okio.FileSystem
import okio.Path.Companion.toPath

class CoreRepositoryTest {
  @Test
  fun `distanceBetween returns stable Zaragoza distance ordering`() {
    val origin = GeoPoint(41.6488, -0.8891)
    val near = GeoPoint(41.6492, -0.8888)
    val far = GeoPoint(41.6592, -0.9001)

    assertTrue(distanceBetween(origin, near) < distanceBetween(origin, far))
  }

  @Test
  fun `favorites repository persists ids to disk`() = runTest {
    val temporaryRoot = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/bizizaragoza-test-${Random.nextInt()}"
    val repository = FavoritesRepositoryImpl(
      fileSystem = FileSystem.SYSTEM,
      json = Json,
      storageDirectoryProvider = object : StorageDirectoryProvider {
        override val rootPath: String = temporaryRoot
      },
      watchSyncBridge = object : WatchSyncBridge {
        override suspend fun pushFavoriteIds(favoriteIds: Set<String>) = Unit

        override suspend fun latestFavoriteIds(): Set<String>? = null
      },
    )

    repository.bootstrap()
    repository.toggle("station-1")

    assertEquals(setOf("station-1"), repository.favoriteIds.value)
    assertTrue(FileSystem.SYSTEM.exists("$temporaryRoot/favorites.json".toPath()))
  }

  @Test
  fun `favorites repository merges local and wearable ids on bootstrap`() = runTest {
    val temporaryRoot = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/bizizaragoza-sync-${Random.nextInt()}"
    val fileSystem = FileSystem.SYSTEM
    val rootPath = "$temporaryRoot/favorites.json".toPath()
    fileSystem.createDirectories(rootPath.parent!!)
    fileSystem.write(rootPath) {
      writeUtf8("""{"favoriteIds":["station-local"]}""")
    }

    val repository = FavoritesRepositoryImpl(
      fileSystem = fileSystem,
      json = Json,
      storageDirectoryProvider = object : StorageDirectoryProvider {
        override val rootPath: String = temporaryRoot
      },
      watchSyncBridge = object : WatchSyncBridge {
        override suspend fun pushFavoriteIds(favoriteIds: Set<String>) = Unit

        override suspend fun latestFavoriteIds(): Set<String> = setOf("station-watch")
      },
    )

    repository.bootstrap()

    assertEquals(setOf("station-local", "station-watch"), repository.favoriteIds.value)
  }

  @Test
  fun `stations repository refresh uses current user location when available`() = runTest {
    var requestedOrigin: GeoPoint? = null
    val repository = StationsRepositoryImpl(
      biziApi = object : BiziApi {
        override suspend fun fetchStations(origin: GeoPoint): List<Station> {
          requestedOrigin = origin
          return listOf(
            Station(
              id = "station-1",
              name = "Plaza Espana",
              address = "Centro",
              location = origin,
              bikesAvailable = 10,
              slotsFree = 5,
              distanceMeters = 0,
            ),
          )
        }
      },
      appConfiguration = AppConfiguration(),
      locationProvider = object : LocationProvider {
        override suspend fun currentLocation(): GeoPoint = GeoPoint(41.65, -0.88)
      },
    )

    repository.refresh()

    assertEquals(GeoPoint(41.65, -0.88), requestedOrigin)
    assertEquals(GeoPoint(41.65, -0.88), repository.state.value.userLocation)
  }
}
