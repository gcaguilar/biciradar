package com.gcaguilar.biciradar.core

import kotlin.test.Test
import kotlin.test.assertEquals

class TripModeTest {
  private fun station(
    id: String,
    bikes: Int,
    slots: Int,
    distanceMeters: Int,
  ): Station =
    Station(
      id = id,
      name = id,
      address = "address-$id",
      location = GeoPoint(41.65, -0.88),
      bikesAvailable = bikes,
      slotsFree = slots,
      distanceMeters = distanceMeters,
    )

  @Test
  fun `pedestrian mode keeps only stations with bikes ordered by distance`() {
    val stations =
      listOf(
        station(id = "far-with-bikes", bikes = 3, slots = 0, distanceMeters = 400),
        station(id = "near-without-bikes", bikes = 0, slots = 4, distanceMeters = 50),
        station(id = "near-with-bikes", bikes = 2, slots = 1, distanceMeters = 120),
      )

    val result = stationsForTripMode(stations, TripMode.Pedestrian)

    assertEquals(listOf("near-with-bikes", "far-with-bikes"), result.map { it.id })
  }

  @Test
  fun `cyclist mode keeps only stations with free slots ordered by distance`() {
    val stations =
      listOf(
        station(id = "far-with-slots", bikes = 0, slots = 2, distanceMeters = 600),
        station(id = "near-without-slots", bikes = 5, slots = 0, distanceMeters = 30),
        station(id = "near-with-slots", bikes = 1, slots = 3, distanceMeters = 200),
      )

    val result = stationsForTripMode(stations, TripMode.Cyclist)

    assertEquals(listOf("near-with-slots", "far-with-slots"), result.map { it.id })
  }

  @Test
  fun `stations for trip mode keep the original order for equal distances`() {
    val stations =
      listOf(
        station(id = "first", bikes = 1, slots = 1, distanceMeters = 100),
        station(id = "second", bikes = 1, slots = 1, distanceMeters = 100),
      )

    assertEquals(
      listOf("first", "second"),
      stationsForTripMode(stations, TripMode.Pedestrian).map { it.id },
    )
  }

  @Test
  fun `pedestrian selection prefers the nearest station with bikes within radius`() {
    val stations =
      listOf(
        station(id = "near-without-bikes", bikes = 0, slots = 4, distanceMeters = 50),
        station(id = "near-with-bikes", bikes = 2, slots = 0, distanceMeters = 200),
        station(id = "far-with-bikes", bikes = 5, slots = 0, distanceMeters = 900),
      )

    val selection = selectNearbyStationForTripMode(stations, 500, TripMode.Pedestrian)

    assertEquals("near-with-bikes", selection.withinRadiusStation?.id)
    assertEquals("near-with-bikes", selection.highlightedStation?.id)
  }

  @Test
  fun `selection falls back to the nearest matching station outside the radius`() {
    val stations =
      listOf(
        station(id = "near-without-bikes", bikes = 0, slots = 4, distanceMeters = 50),
        station(id = "far-with-bikes", bikes = 5, slots = 0, distanceMeters = 900),
      )

    val selection = selectNearbyStationForTripMode(stations, 500, TripMode.Pedestrian)

    assertEquals(null, selection.withinRadiusStation)
    assertEquals("far-with-bikes", selection.fallbackStation?.id)
    assertEquals("far-with-bikes", selection.highlightedStation?.id)
  }
}
