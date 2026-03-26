package com.gcaguilar.biciradar.wear

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WearStationPresentationTest {
  @Test
  fun `sort wear favorite stations prioritizes home then work`() {
    val sorted = sortWearFavoriteStations(
      stations = listOf(
        station(id = "work", distanceMeters = 300),
        station(id = "other", distanceMeters = 80),
        station(id = "home", distanceMeters = 200),
      ),
      homeStationId = "home",
      workStationId = "work",
    )

    assertEquals(listOf("home", "work", "other"), sorted.map { it.id })
  }

  @Test
  fun `wear saved place label maps home and work ids`() {
    assertEquals("Casa", wearSavedPlaceLabel("home", homeStationId = "home", workStationId = "work"))
    assertEquals("Trabajo", wearSavedPlaceLabel("work", homeStationId = "home", workStationId = "work"))
    assertNull(wearSavedPlaceLabel("other", homeStationId = "home", workStationId = "work"))
  }
}

private fun station(
  id: String,
  distanceMeters: Int,
): Station = Station(
  id = id,
  name = id,
  address = id,
  location = GeoPoint(41.65, -0.88),
  bikesAvailable = 4,
  slotsFree = 6,
  distanceMeters = distanceMeters,
)
