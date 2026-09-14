package com.gcaguilar.biciradar.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteLauncherTripModeTest {
  @Test
  fun `pedestrian launches walking directions to the station`() {
    val launcher = RecordingRouteLauncher()

    launcher.launchForTripMode(station(), TripMode.Pedestrian)

    assertEquals(listOf("s1"), launcher.launchedStations)
    assertTrue(launcher.bikeDestinations.isEmpty())
  }

  @Test
  fun `cyclist launches bike directions to the station location`() {
    val launcher = RecordingRouteLauncher()
    val station = station()

    launcher.launchForTripMode(station, TripMode.Cyclist)

    assertEquals(listOf(station.location), launcher.bikeDestinations)
    assertTrue(launcher.launchedStations.isEmpty())
  }
}

private class RecordingRouteLauncher : RouteLauncher {
  val launchedStations = mutableListOf<String>()
  val bikeDestinations = mutableListOf<GeoPoint>()

  override fun launch(station: Station) {
    launchedStations += station.id
  }

  override fun launchWalkToLocation(destination: GeoPoint) = Unit

  override fun launchBikeToLocation(destination: GeoPoint) {
    bikeDestinations += destination
  }
}

private fun station(): Station =
  Station(
    id = "s1",
    name = "Station",
    address = "Address",
    location = GeoPoint(41.65, -0.88),
    bikesAvailable = 1,
    slotsFree = 1,
    distanceMeters = 100,
  )
