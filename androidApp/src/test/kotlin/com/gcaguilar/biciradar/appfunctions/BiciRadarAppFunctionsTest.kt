package com.gcaguilar.biciradar.appfunctions

import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationRequirement
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BiciRadarAppFunctionsTest {
  private val errors = FakeAppFunctionErrors()

  @Test
  fun `bootstrap and city check run once across calls`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")))
      val functions = functions(data)

      functions.currentCity()
      functions.searchStations("plaza", limit = 5)

      assertEquals(1, data.bootstrapCalls)
      assertEquals(1, data.cityConfiguredChecks)
    }

  @Test
  fun `missing city fails with an actionable app unknown error`() {
    val data = FakeAppData(cityConfigured = false)
    val functions = functions(data)

    val error = assertThrows(FakeError::class.java) { runBlocking { functions.currentCity() } }

    assertEquals(FakeErrorKind.APP_UNKNOWN, error.kind)
    assertTrue(error.message.orEmpty().contains("open BiciRadar and select a city"))
  }

  @Test
  fun `current city maps the shared city model`() =
    runBlocking {
      val functions = functions(FakeAppData(city = City.BARCELONA))

      val city = functions.currentCity()

      assertEquals("barcelona", city.id)
      assertEquals("Barcelona", city.displayName)
      assertTrue(city.supportsEbikes)
    }

  @Test
  fun `search rejects a blank query`() {
    val functions = functions(FakeAppData())

    val error =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.searchStations("   ", limit = 5) }
      }

    assertEquals(FakeErrorKind.INVALID_ARGUMENT, error.kind)
  }

  @Test
  fun `search rejects a limit outside one to twenty`() {
    val functions = functions(FakeAppData())

    val tooSmall =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.searchStations("plaza", limit = 0) }
      }
    val tooLarge =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.searchStations("plaza", limit = 21) }
      }

    assertEquals(FakeErrorKind.INVALID_ARGUMENT, tooSmall.kind)
    assertEquals(FakeErrorKind.INVALID_ARGUMENT, tooLarge.kind)
  }

  @Test
  fun `search applies the limit`() =
    runBlocking {
      val data = FakeAppData(stations = (1..6).map { station("s$it") })

      val results = functions(data).searchStations("plaza", limit = 2)

      assertEquals(listOf("s1", "s2"), results.map { it.id })
    }

  @Test
  fun `search uses the default limit when the caller omits it`() =
    runBlocking {
      val data = FakeAppData(stations = (1..6).map { station("s$it") })

      val results = functions(data).searchStations("plaza", limit = null)

      assertEquals(5, results.size)
    }

  @Test
  fun `search resolves pinned aliases through the alias use case`() =
    runBlocking {
      val home = station("home")
      val data = FakeAppData(stations = listOf(home, station("other")), homeId = "home")
      data.aliasResult = home

      val results = functions(data).searchStations("Mi Casa", limit = 5)

      assertEquals(listOf("home"), results.map { it.id })
      assertEquals(listOf("Mi Casa"), data.aliasQueries)
      assertTrue(data.filterQueries.isEmpty())
    }

  @Test
  fun `search uses the free text filter for regular queries`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("plaza")))
      val functions = functions(data)

      functions.searchStations("Plaza España", limit = 5)

      assertEquals(listOf("Plaza España"), data.filterQueries)
      assertTrue(data.aliasQueries.isEmpty())
    }

  @Test
  fun `search result distances are unknown without a user location`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1", distance = 900)))

      val result = functions(data).searchStations("s1", limit = 5).single()

      assertEquals(-1, result.distanceMeters)
    }

  @Test
  fun `availability refreshes the requested station`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1", bikes = 3)))

      val result = functions(data).stationAvailability(" s1 ")

      assertEquals("s1", result.id)
      assertEquals(listOf("s1"), data.refreshedIds)
    }

  @Test
  fun `availability of an unknown station asks to search again`() {
    val data = FakeAppData(stations = listOf(station("s1")))
    val functions = functions(data)

    val error =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.stationAvailability("missing") }
      }

    assertEquals(FakeErrorKind.ELEMENT_NOT_FOUND, error.kind)
    assertTrue(error.message.orEmpty().contains("searchStations"))
  }

  @Test
  fun `availability with an empty repository reports missing data`() {
    val data = FakeAppData(stations = emptyList())
    val functions = functions(data)

    val error =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.stationAvailability("missing") }
      }

    assertEquals(FakeErrorKind.APP_UNKNOWN, error.kind)
    assertTrue(error.message.orEmpty().contains("try again later"))
  }

  @Test
  fun `nearest station accepts requirement in any case`() =
    runBlocking {
      val data = FakeAppData()
      data.nearestWithBikesResult = station("bikes")
      val functions = functions(data)

      val result = functions.findNearestStation(" bikes ", latitude = null, longitude = null)

      assertEquals("bikes", result?.id)
    }

  @Test
  fun `nearest station defaults to any when the requirement is omitted`() =
    runBlocking {
      val data = FakeAppData()
      data.nearestResult = station("any")
      val functions = functions(data)

      val result = functions.findNearestStation(null, latitude = null, longitude = null)

      assertEquals("any", result?.id)
    }

  @Test
  fun `nearest station rejects an unknown requirement`() {
    val functions = functions(FakeAppData())

    val error =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.findNearestStation("scooters", latitude = null, longitude = null) }
      }

    assertEquals(FakeErrorKind.INVALID_ARGUMENT, error.kind)
  }

  @Test
  fun `nearest station rejects a partial or out of range origin`() {
    val functions = functions(FakeAppData())

    val partial =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.findNearestStation("ANY", latitude = 41.6, longitude = null) }
      }
    val outOfRange =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.findNearestStation("ANY", latitude = 91.0, longitude = 0.0) }
      }

    assertEquals(FakeErrorKind.INVALID_ARGUMENT, partial.kind)
    assertEquals(FakeErrorKind.INVALID_ARGUMENT, outOfRange.kind)
  }

  @Test
  fun `nearest station uses the caller origin when coordinates are supplied`() =
    runBlocking {
      val data = FakeAppData()
      data.nearestFromResult = station("origin", distance = 42)
      val functions = functions(data)

      val result = functions.findNearestStation("SLOTS", latitude = 41.65, longitude = -0.88)

      assertEquals("origin", result?.id)
      assertEquals(42, result?.distanceMeters)
      assertEquals(
        listOf(GeoPoint(41.65, -0.88) to StationRequirement.SLOTS),
        data.nearestFromRequests,
      )
    }

  @Test
  fun `nearest ebike station picks the first station with ebikes`() =
    runBlocking {
      val data =
        FakeAppData(
          stations =
            listOf(
              station("no-ebikes", ebikes = 0),
              station("with-ebikes", ebikes = 2),
            ),
        )

      val result = functions(data).findNearestStation("EBIKES", latitude = null, longitude = null)

      assertEquals("with-ebikes", result?.id)
      assertEquals(-1, result?.distanceMeters)
    }

  @Test
  fun `nearest station returns null when nothing satisfies the requirement`() =
    runBlocking {
      val data = FakeAppData()
      data.nearestWithSlotsResult = null

      val result = functions(data).findNearestStation("SLOTS", latitude = null, longitude = null)

      assertNull(result)
    }

  @Test
  fun `nearby stations with coordinates are sorted from the caller origin`() =
    runBlocking {
      val data =
        FakeAppData(
          stations =
            listOf(
              station("far", distance = 5, latitude = 41.70, longitude = -0.88),
              station("near", distance = 900, latitude = 41.6501, longitude = -0.88),
            ),
        )
      val functions = functions(data)

      val results = functions.listNearbyStations(limit = 2, latitude = 41.65, longitude = -0.88)

      assertEquals(listOf("near", "far"), results.map { it.id })
      assertTrue(results.all { it.distanceMeters >= 0 })
    }

  @Test
  fun `nearby stations without coordinates delegate to the repository order`() =
    runBlocking {
      val data = FakeAppData(stations = (1..4).map { station("s$it") })

      val results = functions(data).listNearbyStations(limit = null, latitude = null, longitude = null)

      assertEquals(listOf(5), data.nearbyLimits)
      assertEquals(4, results.size)
      assertEquals(-1, results.first().distanceMeters)
    }

  @Test
  fun `nearby stations reject an invalid limit`() {
    val functions = functions(FakeAppData())

    val error =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.listNearbyStations(limit = 0, latitude = null, longitude = null) }
      }

    assertEquals(FakeErrorKind.INVALID_ARGUMENT, error.kind)
  }

  @Test
  fun `favorite stations carry home and work flags`() =
    runBlocking {
      val home = station("home")
      val work = station("work")
      val other = station("other")
      val data =
        FakeAppData(
          stations = listOf(home, work, other),
          favorites = setOf("home", "work", "other"),
          homeId = "home",
          workId = "work",
        )

      val results = functions(data).listFavoriteStations()

      assertEquals(3, results.size)
      assertTrue(results.single { it.id == "home" }.isHome)
      assertTrue(results.single { it.id == "work" }.isWork)
      assertTrue(results.single { it.id == "other" }.isFavorite)
      assertTrue(results.none { it.id == "other" && (it.isHome || it.isWork) })
    }

  @Test
  fun `adding an existing favorite does not remove it and does not notify`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")), favorites = setOf("s1"))
      var notifications = 0
      val functions = functions(data) { notifications += 1 }

      val result = functions.addFavoriteStation("s1")

      assertTrue(result.isFavorite)
      assertTrue(data.toggledIds.isEmpty())
      assertEquals(0, notifications)
      assertTrue(data.isFavorite("s1"))
    }

  @Test
  fun `adding a new favorite toggles once and notifies`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")))
      var notifications = 0
      val functions = functions(data) { notifications += 1 }

      val result = functions.addFavoriteStation("s1")

      assertTrue(result.isFavorite)
      assertEquals(listOf("s1"), data.toggledIds)
      assertEquals(1, notifications)
      assertTrue(data.isFavorite("s1"))
    }

  @Test
  fun `adding an unknown station asks to search again`() {
    val data = FakeAppData(stations = listOf(station("s1")))
    val functions = functions(data)

    val error =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.addFavoriteStation("missing") }
      }

    assertEquals(FakeErrorKind.ELEMENT_NOT_FOUND, error.kind)
    assertTrue(error.message.orEmpty().contains("searchStations"))
  }

  @Test
  fun `removing a non favorite is a silent no-op`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")))
      var notifications = 0
      val functions = functions(data) { notifications += 1 }

      val removed = functions.removeFavoriteStation("s1")

      assertFalse(removed)
      assertTrue(data.toggledIds.isEmpty())
      assertEquals(0, notifications)
    }

  @Test
  fun `removing a favorite toggles once and notifies`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")), favorites = setOf("s1"))
      var notifications = 0
      val functions = functions(data) { notifications += 1 }

      val removed = functions.removeFavoriteStation("s1")

      assertTrue(removed)
      assertEquals(listOf("s1"), data.toggledIds)
      assertEquals(1, notifications)
      assertFalse(data.isFavorite("s1"))
    }

  @Test
  fun `removing an unknown station asks to search again`() {
    val functions = functions(FakeAppData(stations = listOf(station("s1"))))

    val error =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.removeFavoriteStation("missing") }
      }

    assertEquals(FakeErrorKind.ELEMENT_NOT_FOUND, error.kind)
  }

  @Test
  fun `open station validates and trims the station id`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")))
      val functions = functions(data)

      assertEquals("s1", functions.validatedStationId(" s1 "))

      val error =
        assertThrows(FakeError::class.java) {
          runBlocking { functions.validatedStationId("missing") }
        }
      assertEquals(FakeErrorKind.ELEMENT_NOT_FOUND, error.kind)
      assertNotNull(error.message)
    }

  @Test
  fun `open station rejects a blank station id`() {
    val functions = functions(FakeAppData(stations = listOf(station("s1"))))

    val error =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.validatedStationId("  ") }
      }

    assertEquals(FakeErrorKind.INVALID_ARGUMENT, error.kind)
  }

  @Test
  fun `set home pins the station and reports home without favoriting it`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")))
      var notifications = 0
      val functions = functions(data) { notifications += 1 }

      val result = functions.setHomeStation(" s1 ")

      assertEquals(listOf("s1"), data.homePins)
      assertEquals("s1", data.homeId)
      assertTrue(result.isHome)
      assertFalse(result.isWork)
      assertFalse(result.isFavorite)
      assertEquals(1, notifications)
      assertTrue(data.toggledIds.isEmpty())
    }

  @Test
  fun `set home replaces the previous pin and notifies`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("old"), station("new")), homeId = "old")
      var notifications = 0
      val functions = functions(data) { notifications += 1 }

      val result = functions.setHomeStation("new")

      assertEquals("new", data.homeId)
      assertEquals(listOf("new"), data.homePins)
      assertTrue(result.isHome)
      assertEquals(1, notifications)
    }

  @Test
  fun `set home on the current pin does not notify`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")), homeId = "s1")
      var notifications = 0
      val functions = functions(data) { notifications += 1 }

      val result = functions.setHomeStation("s1")

      assertTrue(data.homePins.isEmpty())
      assertEquals(0, notifications)
      assertTrue(result.isHome)
    }

  @Test
  fun `set work pins the station and reports work without favoriting it`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")))
      val functions = functions(data)

      val result = functions.setWorkStation("s1")

      assertEquals(listOf("s1"), data.workPins)
      assertEquals("s1", data.workId)
      assertTrue(result.isWork)
      assertFalse(result.isHome)
      assertFalse(result.isFavorite)
    }

  @Test
  fun `set work replaces the previous pin and notifies`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("old"), station("new")), workId = "old")
      var notifications = 0
      val functions = functions(data) { notifications += 1 }

      functions.setWorkStation("new")

      assertEquals("new", data.workId)
      assertEquals(1, notifications)
    }

  @Test
  fun `set work with an unknown station asks to search again`() {
    val data = FakeAppData(stations = listOf(station("s1")))
    val functions = functions(data)

    val error =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.setWorkStation("missing") }
      }

    assertEquals(FakeErrorKind.ELEMENT_NOT_FOUND, error.kind)
    assertTrue(data.workPins.isEmpty())
  }

  @Test
  fun `monitoring defaults to docks for ten minutes`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")))
      val functions = functions(data)

      val info = functions.startStationMonitoring(" s1 ", kind = null, durationMinutes = null)

      assertEquals(listOf(Triple("s1", 600, SurfaceMonitoringKind.Docks)), data.startedMonitoring)
      assertEquals("s1", info.stationId)
      assertTrue(info.started)
      assertEquals(10, info.durationMinutes)
      assertEquals("DOCKS", info.monitors)
    }

  @Test
  fun `monitoring accepts a custom kind and duration in any case`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")))
      val functions = functions(data)

      val info = functions.startStationMonitoring("s1", kind = " bikes ", durationMinutes = 45)

      assertEquals(listOf(Triple("s1", 2700, SurfaceMonitoringKind.Bikes)), data.startedMonitoring)
      assertEquals("BIKES", info.monitors)
      assertEquals(45, info.durationMinutes)
    }

  @Test
  fun `monitoring rejects an unknown kind`() {
    val data = FakeAppData(stations = listOf(station("s1")))
    val functions = functions(data)

    val error =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.startStationMonitoring("s1", "SCOOTERS", 10) }
      }

    assertEquals(FakeErrorKind.INVALID_ARGUMENT, error.kind)
    assertTrue(data.startedMonitoring.isEmpty())
  }

  @Test
  fun `monitoring rejects a duration outside one to sixty`() {
    val data = FakeAppData(stations = listOf(station("s1")))
    val functions = functions(data)

    val tooShort =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.startStationMonitoring("s1", "DOCKS", 0) }
      }
    val tooLong =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.startStationMonitoring("s1", "DOCKS", 61) }
      }

    assertEquals(FakeErrorKind.INVALID_ARGUMENT, tooShort.kind)
    assertEquals(FakeErrorKind.INVALID_ARGUMENT, tooLong.kind)
    assertTrue(data.startedMonitoring.isEmpty())
  }

  @Test
  fun `monitoring reports honestly when the session does not start`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")))
      data.startMonitoringResult = false
      val functions = functions(data)

      val info = functions.startStationMonitoring("s1", kind = null, durationMinutes = null)

      assertFalse(info.started)
      assertTrue(info.message.contains("did not start"))
      assertTrue(info.message.contains("signed in"))
      assertTrue(info.message.contains("notifications"))
    }

  @Test
  fun `monitoring confirms an active session in its message`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")))
      val functions = functions(data)

      val info = functions.startStationMonitoring("s1", kind = null, durationMinutes = 15)

      assertTrue(info.started)
      assertTrue(info.message.contains("15 minutes"))
      assertTrue(info.message.contains("Alerts will be delivered"))
    }

  @Test
  fun `monitoring an unknown station asks to search again`() {
    val data = FakeAppData(stations = listOf(station("s1")))
    val functions = functions(data)

    val error =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.startStationMonitoring("missing", null, null) }
      }

    assertEquals(FakeErrorKind.ELEMENT_NOT_FOUND, error.kind)
    assertTrue(data.startedMonitoring.isEmpty())
  }

  @Test
  fun `stopping one active session leaves the others running`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1"), station("s2")))
      data.activeMonitoring += setOf("s1", "s2")
      val functions = functions(data)

      val stopped = functions.stopStationMonitoring(" s1 ")

      assertTrue(stopped)
      assertEquals(listOf("s1"), data.stoppedMonitoring)
      assertFalse(data.isMonitoring("s1"))
      assertTrue(data.isMonitoring("s2"))
    }

  @Test
  fun `stopping an inactive session is a no-op`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")))
      val functions = functions(data)

      val stopped = functions.stopStationMonitoring("s1")

      assertFalse(stopped)
      assertTrue(data.stoppedMonitoring.isEmpty())
    }

  @Test
  fun `stopping without a station stops all active sessions`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1"), station("s2")))
      data.activeMonitoring += setOf("s1", "s2")
      val functions = functions(data)

      val stopped = functions.stopStationMonitoring(null)

      assertTrue(stopped)
      assertEquals(listOf<String?>(null), data.stoppedMonitoring)
      assertFalse(data.isMonitoring(null))
    }

  @Test
  fun `stopping without a station returns false when nothing is active`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")))
      val functions = functions(data)

      val stopped = functions.stopStationMonitoring(null)

      assertFalse(stopped)
      assertTrue(data.stoppedMonitoring.isEmpty())
    }

  @Test
  fun `alerts map every condition kind`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")), favorites = setOf("s1"))
      val functions = functions(data)

      val atLeastBikes = functions.createSavedPlaceAlert("s1", "FAVORITE", "BIKES_AT_LEAST", 3)
      val atLeastDocks = functions.createSavedPlaceAlert("s1", "FAVORITE", "DOCKS_AT_LEAST", 2)
      val noBikes = functions.createSavedPlaceAlert("s1", "FAVORITE", "BIKES_EQUALS_ZERO", null)
      val noDocks = functions.createSavedPlaceAlert("s1", "FAVORITE", "DOCKS_EQUALS_ZERO", null)

      assertEquals("BIKES_AT_LEAST", atLeastBikes.condition)
      assertEquals(3, atLeastBikes.count)
      assertEquals("DOCKS_AT_LEAST", atLeastDocks.condition)
      assertEquals(2, atLeastDocks.count)
      assertEquals("BIKES_EQUALS_ZERO", noBikes.condition)
      assertEquals(0, noBikes.count)
      assertEquals("DOCKS_EQUALS_ZERO", noDocks.condition)
      assertEquals(0, noDocks.count)
      assertEquals(
        listOf(
          SavedPlaceAlertCondition.BikesAtLeast(3),
          SavedPlaceAlertCondition.DocksAtLeast(2),
          SavedPlaceAlertCondition.BikesEqualsZero,
          SavedPlaceAlertCondition.DocksEqualsZero,
        ),
        data.alertRuleRequests.map { it.second },
      )
    }

  @Test
  fun `at least alerts default the threshold to one`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")), favorites = setOf("s1"))
      val functions = functions(data)

      val info = functions.createSavedPlaceAlert("s1", null, "bikes_at_least", null)

      assertEquals(1, info.count)
      assertEquals("FAVORITE", info.target)
      assertEquals(
        SavedPlaceAlertCondition.BikesAtLeast(1),
        data.alertRuleRequests.single().second,
      )
    }

  @Test
  fun `at least alerts reject a threshold outside one to one hundred`() {
    val data = FakeAppData(stations = listOf(station("s1")), favorites = setOf("s1"))
    val functions = functions(data)

    val tooSmall =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.createSavedPlaceAlert("s1", "FAVORITE", "BIKES_AT_LEAST", 0) }
      }
    val tooLarge =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.createSavedPlaceAlert("s1", "FAVORITE", "DOCKS_AT_LEAST", 101) }
      }

    assertEquals(FakeErrorKind.INVALID_ARGUMENT, tooSmall.kind)
    assertEquals(FakeErrorKind.INVALID_ARGUMENT, tooLarge.kind)
    assertTrue(data.alertRuleRequests.isEmpty())
  }

  @Test
  fun `equals zero alerts ignore a supplied count`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")), favorites = setOf("s1"))
      val functions = functions(data)

      val info = functions.createSavedPlaceAlert("s1", "FAVORITE", "DOCKS_EQUALS_ZERO", 7)

      assertEquals(0, info.count)
      assertEquals(
        SavedPlaceAlertCondition.DocksEqualsZero,
        data.alertRuleRequests.single().second,
      )
    }

  @Test
  fun `an alert for an unknown station asks to search again`() {
    val data = FakeAppData(stations = listOf(station("s1")))
    val functions = functions(data)

    val error =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.createSavedPlaceAlert("missing", null, "BIKES_EQUALS_ZERO", null) }
      }

    assertEquals(FakeErrorKind.ELEMENT_NOT_FOUND, error.kind)
    assertTrue(data.alertRuleRequests.isEmpty())
  }

  @Test
  fun `favorite alerts require the station to be a favorite`() {
    val data = FakeAppData(stations = listOf(station("s1")))
    val functions = functions(data)

    val error =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.createSavedPlaceAlert("s1", "FAVORITE", "BIKES_AT_LEAST", 2) }
      }

    assertEquals(FakeErrorKind.INVALID_ARGUMENT, error.kind)
    assertTrue(error.message.orEmpty().contains("addFavoriteStation"))
    assertTrue(data.alertRuleRequests.isEmpty())
  }

  @Test
  fun `home alerts require the matching home pin`() {
    val data = FakeAppData(stations = listOf(station("s1")), homeId = "other")
    val functions = functions(data)

    val error =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.createSavedPlaceAlert("s1", "HOME", "BIKES_AT_LEAST", 2) }
      }

    assertEquals(FakeErrorKind.INVALID_ARGUMENT, error.kind)
    assertTrue(error.message.orEmpty().contains("setHomeStation"))
  }

  @Test
  fun `work alerts require the matching work pin`() {
    val data = FakeAppData(stations = listOf(station("s1")))
    val functions = functions(data)

    val error =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.createSavedPlaceAlert("s1", "WORK", "BIKES_AT_LEAST", 2) }
      }

    assertEquals(FakeErrorKind.INVALID_ARGUMENT, error.kind)
    assertTrue(error.message.orEmpty().contains("setWorkStation"))
  }

  @Test
  fun `alert info reports the rule identity and saved place`() =
    runBlocking {
      val data =
        FakeAppData(
          stations = listOf(station("s1", name = "Plaza España")),
          homeId = "s1",
        )
      val functions = functions(data)

      val info = functions.createSavedPlaceAlert("s1", "home", "DOCKS_AT_LEAST", 4)

      assertEquals("home:s1:zaragoza", info.id)
      assertEquals("s1", info.stationId)
      assertEquals("Plaza España", info.stationName)
      assertEquals("HOME", info.target)
      assertEquals("DOCKS_AT_LEAST", info.condition)
      assertEquals(4, info.count)
      assertTrue(info.isEnabled)
      assertEquals(
        SavedPlaceAlertTarget.Home("s1", "zaragoza", "Plaza España"),
        data.alertRuleRequests.single().first,
      )
    }

  @Test
  fun `work alert info carries the work target`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")), workId = "s1")
      val functions = functions(data)

      val info = functions.createSavedPlaceAlert("s1", "work", "BIKES_AT_LEAST", 2)

      assertEquals("WORK", info.target)
      assertEquals(
        SavedPlaceAlertTarget.Work("s1", "zaragoza", "Station s1"),
        data.alertRuleRequests.single().first,
      )
    }

  @Test
  fun `repeating an alert for the same station and target reuses its identity`() =
    runBlocking {
      val data = FakeAppData(stations = listOf(station("s1")), favorites = setOf("s1"))
      val functions = functions(data)

      val first = functions.createSavedPlaceAlert("s1", "FAVORITE", "BIKES_AT_LEAST", 3)
      val second = functions.createSavedPlaceAlert("s1", "FAVORITE", "DOCKS_EQUALS_ZERO", null)

      assertEquals(first.id, second.id)
      assertEquals("DOCKS_EQUALS_ZERO", second.condition)
    }

  @Test
  fun `alerts reject an unknown target or condition`() {
    val data = FakeAppData(stations = listOf(station("s1")), favorites = setOf("s1"))
    val functions = functions(data)

    val badTarget =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.createSavedPlaceAlert("s1", "CATEGORY", "BIKES_AT_LEAST", 1) }
      }
    val badCondition =
      assertThrows(FakeError::class.java) {
        runBlocking { functions.createSavedPlaceAlert("s1", "FAVORITE", "BIKES_ABOVE", 1) }
      }

    assertEquals(FakeErrorKind.INVALID_ARGUMENT, badTarget.kind)
    assertEquals(FakeErrorKind.INVALID_ARGUMENT, badCondition.kind)
    assertTrue(data.alertRuleRequests.isEmpty())
  }

  private fun functions(
    data: BiciRadarAppData,
    onFavoritesChanged: () -> Unit = {},
  ): BiciRadarAppFunctions = BiciRadarAppFunctions(data, onFavoritesChanged, errors)
}

private enum class FakeErrorKind {
  INVALID_ARGUMENT,
  ELEMENT_NOT_FOUND,
  APP_UNKNOWN,
}

private class FakeError(
  message: String,
  val kind: FakeErrorKind,
) : IllegalStateException(message)

private class FakeAppFunctionErrors : AppFunctionErrors {
  override fun invalidArgument(message: String): Exception = FakeError(message, FakeErrorKind.INVALID_ARGUMENT)

  override fun elementNotFound(message: String): Exception = FakeError(message, FakeErrorKind.ELEMENT_NOT_FOUND)

  override fun appUnknown(message: String): Exception = FakeError(message, FakeErrorKind.APP_UNKNOWN)
}

private class FakeAppData(
  var stations: List<Station> = emptyList(),
  var favorites: Set<String> = emptySet(),
  var homeId: String? = null,
  var workId: String? = null,
  var city: City = City.ZARAGOZA,
  var cityConfigured: Boolean = true,
  var userLocationKnown: Boolean = false,
  var lastUpdated: Long? = null,
) : BiciRadarAppData {
  var bootstrapCalls: Int = 0
  var cityConfiguredChecks: Int = 0
  var aliasResult: Station? = null
  var nearestResult: Station? = null
  var nearestWithBikesResult: Station? = null
  var nearestWithSlotsResult: Station? = null
  var nearestFromResult: Station? = null
  var startMonitoringResult: Boolean = true

  val aliasQueries = mutableListOf<String>()
  val filterQueries = mutableListOf<String>()
  val refreshedIds = mutableListOf<String>()
  val toggledIds = mutableListOf<String>()
  val nearbyLimits = mutableListOf<Int>()
  val nearestFromRequests = mutableListOf<Pair<GeoPoint, StationRequirement>>()
  val homePins = mutableListOf<String>()
  val workPins = mutableListOf<String>()
  val startedMonitoring = mutableListOf<Triple<String, Int, SurfaceMonitoringKind>>()
  val stoppedMonitoring = mutableListOf<String?>()
  val activeMonitoring = mutableSetOf<String>()
  val alertRuleRequests = mutableListOf<Pair<SavedPlaceAlertTarget, SavedPlaceAlertCondition>>()

  override suspend fun bootstrapSession() {
    bootstrapCalls += 1
  }

  override suspend fun isCityConfigured(): Boolean {
    cityConfiguredChecks += 1
    return cityConfigured
  }

  override suspend fun currentCity(): City = city

  override fun hasUserLocation(): Boolean = userLocationKnown

  override fun lastUpdatedEpoch(): Long? = lastUpdated

  override suspend fun searchStations(query: String): List<Station> {
    filterQueries += query
    return stations
  }

  override suspend fun findStationMatchingQuery(query: String): Station? {
    aliasQueries += query
    return aliasResult
  }

  override suspend fun allStations(): List<Station> = stations

  override suspend fun nearbyStations(limit: Int): List<Station> {
    nearbyLimits += limit
    return stations.take(limit)
  }

  override suspend fun stationById(stationId: String): Station? = stations.firstOrNull { it.id == stationId }

  override suspend fun refreshAvailability(stationId: String) {
    refreshedIds += stationId
  }

  override suspend fun nearestStation(): Station? = nearestResult

  override suspend fun nearestStationWithBikes(): Station? = nearestWithBikesResult

  override suspend fun nearestStationWithSlots(): Station? = nearestWithSlotsResult

  override suspend fun nearestStationFrom(
    origin: GeoPoint,
    requirement: StationRequirement,
  ): Station? {
    nearestFromRequests += origin to requirement
    return nearestFromResult
  }

  override suspend fun favoriteStations(): List<Station> = stations.filter { it.id in favorites }

  override fun isFavorite(stationId: String): Boolean = stationId in favorites

  override fun homeStationId(): String? = homeId

  override fun workStationId(): String? = workId

  override suspend fun setHomeStation(stationId: String) {
    homePins += stationId
    homeId = stationId
  }

  override suspend fun setWorkStation(stationId: String) {
    workPins += stationId
    workId = stationId
  }

  override suspend fun startMonitoring(
    stationId: String,
    durationSeconds: Int,
    kind: SurfaceMonitoringKind,
  ): Boolean {
    startedMonitoring += Triple(stationId, durationSeconds, kind)
    if (startMonitoringResult) activeMonitoring += stationId
    return startMonitoringResult
  }

  override suspend fun stopMonitoring(stationId: String?) {
    stoppedMonitoring += stationId
    if (stationId == null) {
      activeMonitoring.clear()
    } else {
      activeMonitoring -= stationId
    }
  }

  override fun isMonitoring(stationId: String?): Boolean =
    if (stationId == null) {
      activeMonitoring.isNotEmpty()
    } else {
      stationId in activeMonitoring
    }

  override suspend fun upsertSavedPlaceAlertRule(
    target: SavedPlaceAlertTarget,
    condition: SavedPlaceAlertCondition,
  ): SavedPlaceAlertRule {
    alertRuleRequests += target to condition
    return SavedPlaceAlertRule(
      id = target.identityKey(),
      target = target,
      condition = condition,
      isEnabled = true,
    )
  }

  override suspend fun toggleFavorite(stationId: String) {
    toggledIds += stationId
    favorites =
      if (stationId in favorites) {
        favorites - stationId
      } else {
        favorites + stationId
      }
  }
}
