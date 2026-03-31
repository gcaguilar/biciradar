package com.gcaguilar.biciradar.core

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

class SavedPlaceAlertsCoreTest {
  @Test
  fun `saved place alerts repository persists rules across instances`() = runTest {
    val root = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/biciradar-alerts-${Random.nextInt()}"
    val json = Json { ignoreUnknownKeys = true }
    val storage = object : StorageDirectoryProvider {
      override val rootPath: String = root
    }
    val repo = SavedPlaceAlertsRepositoryImpl(
      fileSystem = FileSystem.SYSTEM,
      json = json,
      storageDirectoryProvider = storage,
    )
    repo.bootstrap()
    val target = SavedPlaceAlertTarget.Home("station-home", "zaragoza", "Casa")
    repo.upsertRule(target, SavedPlaceAlertCondition.BikesAtLeast(1))
    assertEquals(1, repo.currentRules().size)
    assertEquals(SavedPlaceAlertCondition.BikesAtLeast(1), repo.ruleForTarget(target)?.condition)
    assertTrue(FileSystem.SYSTEM.exists("$root/saved_place_alerts.json".toPath()))

    val repoReloaded = SavedPlaceAlertsRepositoryImpl(
      fileSystem = FileSystem.SYSTEM,
      json = json,
      storageDirectoryProvider = storage,
    )
    repoReloaded.bootstrap()
    assertEquals(1, repoReloaded.currentRules().size)
    assertEquals(SavedPlaceAlertCondition.BikesAtLeast(1), repoReloaded.ruleForTarget(target)?.condition)
  }

  @Test
  fun `saved place alerts evaluator triggers when condition newly matches`() {
    val target = SavedPlaceAlertTarget.FavoriteStation("station-1", "zaragoza", "Plaza")
    val rule = SavedPlaceAlertRule(
      id = target.identityKey(),
      target = target,
      condition = SavedPlaceAlertCondition.BikesAtLeast(1),
      isEnabled = true,
      lastConditionMatched = false,
    )
    val station = Station(
      id = "station-1",
      name = "Plaza",
      address = "Centro",
      location = GeoPoint(41.65, -0.88),
      bikesAvailable = 2,
      slotsFree = 5,
      distanceMeters = 120,
    )
    val evaluation = SavedPlaceAlertsEvaluator().evaluate(
      rules = listOf(rule),
      stationsState = StationsState(stations = listOf(station)),
      nowEpoch = 9_000_000L,
    )
    assertEquals(1, evaluation.triggers.size)
    assertEquals("station-1", evaluation.triggers.first().stationId)
    assertEquals(9_000_000L, evaluation.updatedRules.single().lastTriggeredEpoch)
    assertEquals(true, evaluation.updatedRules.single().lastConditionMatched)
  }
}
