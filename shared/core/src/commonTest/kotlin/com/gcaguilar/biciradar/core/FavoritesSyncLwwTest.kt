package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.testutils.testCoroutineScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Last-writer-wins sync between the phone and the paired watch.
 *
 * Timestamps are injected through the persisted snapshot/meta files so the
 * tests never depend on the wall clock.
 */
class FavoritesSyncLwwTest {
  @Test
  fun `codec roundtrips every field including updatedAt`() {
    val snapshot =
      FavoritesSyncSnapshot(
        categories = listOf(FavoriteCategory(id = "custom:uni", label = "Universidad")),
        stationCategory = mapOf("station-1" to "custom:uni", "station-2" to FavoriteCategoryIds.HOME),
        favoriteIds = setOf("station-1"),
        homeStationId = "station-2",
        workStationId = "station-3",
        updatedAt = 1_234L,
      )

    assertEquals(snapshot, FavoritesSyncCodec.decode(FavoritesSyncCodec.encode(snapshot)))
  }

  @Test
  fun `codec returns null for invalid json`() {
    assertNull(FavoritesSyncCodec.decode("{ definitely-not-valid-json"))
    assertNull(FavoritesSyncCodec.decode("[]"))
  }

  @Test
  fun `codec defaults updatedAt to zero when the payload omits it`() {
    val decoded = FavoritesSyncCodec.decode("""{"favoriteIds":["station-1"]}""")

    assertEquals(setOf("station-1"), decoded?.favoriteIds)
    assertEquals(0L, decoded?.updatedAt)
  }

  @Test
  fun `newer remote removes a local favorite`() =
    runTest {
      val dir = testFavoritesDir()
      dir.writeSnapshot(FavoritesSyncSnapshot(favoriteIds = setOf("station-a", "station-b"), updatedAt = 1_000L))
      dir.writeMeta(localUpdatedAt = 1_000L, lastPushedUpdatedAt = 0L)
      val bridge =
        TestWatchSyncBridge(
          remote = FavoritesSyncSnapshot(favoriteIds = setOf("station-a"), updatedAt = 2_000L),
        )

      val repository = createRepository(dir, bridge)
      repository.bootstrap()

      assertEquals(setOf("station-a"), repository.favoriteIds.value)
      assertTrue(bridge.pushed.isEmpty())

      // A second sync must not resurrect the removal nor push anything.
      repository.syncFromPeer()
      assertEquals(setOf("station-a"), repository.favoriteIds.value)
      assertTrue(bridge.pushed.isEmpty())
      assertEquals(setOf("station-a"), dir.readSnapshot().favoriteIds)
      assertEquals(2_000L, dir.readMeta()["localUpdatedAt"])
      assertEquals(2_000L, dir.readMeta()["lastPushedUpdatedAt"])
    }

  @Test
  fun `newer remote wins for home and work stations`() =
    runTest {
      val dir = testFavoritesDir()
      dir.writeSnapshot(
        FavoritesSyncSnapshot(
          favoriteIds = setOf("station-a"),
          homeStationId = "station-home-old",
          updatedAt = 1_000L,
        ),
      )
      dir.writeMeta(localUpdatedAt = 1_000L, lastPushedUpdatedAt = 0L)
      val bridge =
        TestWatchSyncBridge(
          remote =
            FavoritesSyncSnapshot(
              homeStationId = "station-home-new",
              workStationId = "station-work-new",
              updatedAt = 2_000L,
            ),
        )

      val repository = createRepository(dir, bridge)
      repository.bootstrap()

      assertEquals("station-home-new", repository.currentHomeStationId())
      assertEquals("station-work-new", repository.currentWorkStationId())
      assertEquals(setOf("station-home-new", "station-work-new"), repository.favoriteIds.value)
      assertTrue(bridge.pushed.isEmpty())
    }

  @Test
  fun `newer empty remote clears local favorites`() =
    runTest {
      val dir = testFavoritesDir()
      dir.writeSnapshot(
        FavoritesSyncSnapshot(
          favoriteIds = setOf("station-a", "station-b"),
          homeStationId = "station-home",
          updatedAt = 1_000L,
        ),
      )
      dir.writeMeta(localUpdatedAt = 1_000L, lastPushedUpdatedAt = 0L)
      val bridge = TestWatchSyncBridge(remote = FavoritesSyncSnapshot(updatedAt = 2_000L))

      val repository = createRepository(dir, bridge)
      repository.bootstrap()

      assertTrue(repository.favoriteIds.value.isEmpty())
      assertNull(repository.currentHomeStationId())
      assertNull(repository.currentWorkStationId())
      assertTrue(bridge.pushed.isEmpty())
      assertTrue(dir.readSnapshot().favoriteIds.isEmpty())
    }

  @Test
  fun `newer local snapshot is not overwritten and is pushed only once`() =
    runTest {
      val dir = testFavoritesDir()
      dir.writeSnapshot(FavoritesSyncSnapshot(favoriteIds = setOf("station-local"), updatedAt = 2_000L))
      dir.writeMeta(localUpdatedAt = 2_000L, lastPushedUpdatedAt = 1_000L)
      val bridge =
        TestWatchSyncBridge(
          remote = FavoritesSyncSnapshot(favoriteIds = setOf("station-remote"), updatedAt = 1_000L),
        )

      val repository = createRepository(dir, bridge)
      repository.bootstrap()

      assertEquals(setOf("station-local"), repository.favoriteIds.value)
      assertEquals(1, bridge.pushed.size)
      assertEquals(setOf("station-local"), bridge.pushed.single().favoriteIds)
      assertEquals(2_000L, bridge.pushed.single().updatedAt)

      repository.syncFromPeer()

      // Anti ping-pong: the peer already got revision 2000, so no second push.
      assertEquals(1, bridge.pushed.size)
      assertEquals(setOf("station-local"), repository.favoriteIds.value)
      assertEquals(2_000L, dir.readMeta()["lastPushedUpdatedAt"])
    }

  @Test
  fun `legacy peer without timestamps keeps the union merge`() =
    runTest {
      val dir = testFavoritesDir()
      dir.writeSnapshot(FavoritesSyncSnapshot(favoriteIds = setOf("station-local")))
      val bridge =
        TestWatchSyncBridge(
          remote = FavoritesSyncSnapshot(favoriteIds = setOf("station-watch"), homeStationId = "station-home"),
        )

      val repository = createRepository(dir, bridge)
      repository.bootstrap()

      assertEquals(setOf("station-local", "station-watch", "station-home"), repository.favoriteIds.value)
      assertEquals("station-home", repository.currentHomeStationId())
      assertEquals(1, bridge.pushed.size)
      assertEquals(0L, bridge.pushed.single().updatedAt)
    }

  @Test
  fun `local mutations seal updatedAt and persist sync meta`() =
    runTest {
      val dir = testFavoritesDir()
      val bridge = TestWatchSyncBridge(remote = null)
      val repository = createRepository(dir, bridge)

      repository.bootstrap()
      repository.toggle("station-1")

      val pushed = bridge.pushed.single()
      assertTrue(pushed.updatedAt > 0L)
      assertEquals(setOf("station-1"), pushed.favoriteIds)

      val persisted = dir.readSnapshot()
      assertEquals(pushed.updatedAt, persisted.updatedAt)
      assertEquals(setOf("station-1"), persisted.favoriteIds)

      val meta = dir.readMeta()
      assertEquals(pushed.updatedAt, meta["localUpdatedAt"])
      assertEquals(pushed.updatedAt, meta["lastPushedUpdatedAt"])
    }

  private fun createRepository(
    dir: TestFavoritesDir,
    bridge: WatchSyncBridge,
  ) = FavoritesRepositoryImpl(
    fileSystem = FileSystem.SYSTEM,
    json = Json,
    storageDirectoryProvider =
      object : StorageDirectoryProvider {
        override val rootPath: String = dir.rootPath
      },
    watchSyncBridge = bridge,
    scope = testCoroutineScope(),
  )

  private fun testFavoritesDir(): TestFavoritesDir {
    val rootPath = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/biciradar-lww-${Random.nextInt()}"
    FileSystem.SYSTEM.createDirectories(rootPath.toPath())
    return TestFavoritesDir(rootPath)
  }
}

private class TestFavoritesDir(
  val rootPath: String,
) {
  private val fileSystem = FileSystem.SYSTEM

  fun writeSnapshot(snapshot: FavoritesSyncSnapshot) {
    fileSystem.write(snapshotPath()) {
      writeUtf8(FavoritesSyncCodec.encode(snapshot))
    }
  }

  fun readSnapshot(): FavoritesSyncSnapshot =
    FavoritesSyncCodec.decode(fileSystem.read(snapshotPath()) { readUtf8() })!!

  fun writeMeta(
    localUpdatedAt: Long,
    lastPushedUpdatedAt: Long,
  ) {
    fileSystem.write(metaPath()) {
      writeUtf8("""{"localUpdatedAt":$localUpdatedAt,"lastPushedUpdatedAt":$lastPushedUpdatedAt}""")
    }
  }

  fun readMeta(): Map<String, Long> {
    val payload = Json.parseToJsonElement(fileSystem.read(metaPath()) { readUtf8() }).jsonObject
    return payload.mapValues { (_, value) -> value.jsonPrimitive.long }
  }

  private fun snapshotPath() = "$rootPath/favorites.json".toPath()

  private fun metaPath() = "$rootPath/favorites-sync-meta.json".toPath()
}

private class TestWatchSyncBridge(
  var remote: FavoritesSyncSnapshot?,
) : WatchSyncBridge {
  val pushed = mutableListOf<FavoritesSyncSnapshot>()

  override suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot) {
    pushed += snapshot
  }

  override suspend fun latestFavorites(): FavoritesSyncSnapshot? = remote
}
