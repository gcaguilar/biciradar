package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.testutils.FakeFavoritesRepository
import com.gcaguilar.biciradar.testutils.FakeSavedPlaceAlertsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PinnedStationUseCasesTest {
  @Test
  fun `set home replaces the previous pin`() =
    runTest {
      val favorites = FakeFavoritesRepository()
      val useCase = SetHomeStation(favorites)

      useCase.execute("station-a")
      useCase.execute("station-b")

      assertEquals("station-b", favorites.currentHomeStationId())
    }

  @Test
  fun `set work replaces the previous pin and leaves home untouched`() =
    runTest {
      val favorites = FakeFavoritesRepository()
      val setHome = SetHomeStation(favorites)
      val setWork = SetWorkStation(favorites)

      setHome.execute("station-home")
      setWork.execute("station-work")
      setWork.execute("station-work-2")

      assertEquals("station-home", favorites.currentHomeStationId())
      assertEquals("station-work-2", favorites.currentWorkStationId())
    }

  @Test
  fun `setting the same pin twice is idempotent`() =
    runTest {
      val favorites = FakeFavoritesRepository()
      val useCase = SetHomeStation(favorites)

      useCase.execute("station-a")
      useCase.execute("station-a")

      assertEquals("station-a", favorites.currentHomeStationId())
    }
}

class UpsertSavedPlaceAlertRuleTest {
  private class RecordingRepository : FakeSavedPlaceAlertsRepository() {
    var upsertCount = 0
    var lastTarget: SavedPlaceAlertTarget? = null
    var lastCondition: SavedPlaceAlertCondition? = null

    override suspend fun upsertRule(
      target: SavedPlaceAlertTarget,
      condition: SavedPlaceAlertCondition,
      enabled: Boolean,
    ) {
      upsertCount++
      lastTarget = target
      lastCondition = condition
    }
  }

  @Test
  fun `builds an enabled rule keyed by the target identity`() =
    runTest {
      val repository = RecordingRepository()
      val target =
        SavedPlaceAlertTarget.Home(
          stationId = "station-1",
          cityId = "zaragoza",
          stationName = "Plaza España",
        )
      val condition = SavedPlaceAlertCondition.DocksAtLeast(count = 2)

      val rule = UpsertSavedPlaceAlertRule(repository).execute(target, condition)

      assertEquals("home:station-1:zaragoza", rule.id)
      assertEquals(target, rule.target)
      assertEquals(condition, rule.condition)
      assertTrue(rule.isEnabled)
      assertEquals(1, repository.upsertCount)
      assertEquals(target, repository.lastTarget)
      assertEquals(condition, repository.lastCondition)
    }

  @Test
  fun `different targets produce different rule ids`() =
    runTest {
      val repository = RecordingRepository()
      val useCase = UpsertSavedPlaceAlertRule(repository)
      val condition = SavedPlaceAlertCondition.BikesEqualsZero

      val favorite =
        useCase.execute(
          SavedPlaceAlertTarget.FavoriteStation(
            stationId = "station-1",
            cityId = "zaragoza",
          ),
          condition,
        )
      val work =
        useCase.execute(
          SavedPlaceAlertTarget.Work(
            stationId = "station-1",
            cityId = "zaragoza",
          ),
          condition,
        )

      assertNotEquals(favorite.id, work.id)
      assertEquals(2, repository.upsertCount)
    }
}
