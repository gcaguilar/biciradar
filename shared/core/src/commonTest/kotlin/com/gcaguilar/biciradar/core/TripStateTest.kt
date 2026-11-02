package com.gcaguilar.biciradar.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class TripStateTest {
  @Test
  fun `fresh availability replaces the selected station and keeps the trip distance`() {
    val state =
      TripState(
        nearestStationWithSlots = station(bikes = 1, slots = 0).copy(distanceMeters = 300),
        distanceToStation = 300,
      )

    val updated = state.withFreshStation(listOf(station(bikes = 4, slots = 6)))

    assertEquals(4, updated.nearestStationWithSlots?.bikesAvailable)
    assertEquals(6, updated.nearestStationWithSlots?.slotsFree)
    assertEquals(300, updated.nearestStationWithSlots?.distanceMeters)
    assertEquals(300, updated.distanceToStation)
  }

  @Test
  fun `missing station keeps the last known snapshot`() {
    val state = TripState(nearestStationWithSlots = station(bikes = 1, slots = 0))

    assertSame(state, state.withFreshStation(emptyList()))
  }

  @Test
  fun `unchanged availability keeps the same state`() {
    val selected = station(bikes = 3, slots = 5)
    val state = TripState(nearestStationWithSlots = selected)

    assertSame(state, state.withFreshStation(listOf(selected.copy(distanceMeters = 999))))
  }

  private fun station(
    bikes: Int,
    slots: Int,
  ) = Station(
    id = "station-1",
    name = "Plaza España",
    address = "Centro",
    location = GeoPoint(41.65, -0.88),
    bikesAvailable = bikes,
    slotsFree = slots,
    distanceMeters = 100,
  )
}
