package com.gcaguilar.biciradar.mobileui.components.station

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StationAddressSubtitleTest {
  @Test
  fun `hides the address when it repeats the station name`() {
    assertNull(station(name = "Plaza España", address = "Plaza España").addressSubtitleOrNull())
    assertNull(station(name = "Plaza España", address = "  plaza españa  ").addressSubtitleOrNull())
  }

  @Test
  fun `keeps a distinct address`() {
    assertEquals(
      "Calle Mayor 1",
      station(name = "Plaza España", address = "Calle Mayor 1").addressSubtitleOrNull(),
    )
  }

  @Test
  fun `hides a blank address`() {
    assertNull(station(name = "Plaza España", address = "").addressSubtitleOrNull())
    assertNull(station(name = "Plaza España", address = "   ").addressSubtitleOrNull())
  }
}

private fun station(
  name: String,
  address: String,
): Station =
  Station(
    id = "s1",
    name = name,
    address = address,
    location = GeoPoint(41.65, -0.88),
    bikesAvailable = 1,
    slotsFree = 1,
    distanceMeters = 100,
  )
