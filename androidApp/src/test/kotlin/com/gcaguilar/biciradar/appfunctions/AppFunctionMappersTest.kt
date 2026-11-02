package com.gcaguilar.biciradar.appfunctions

import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class AppFunctionMappersTest {
  @Test
  fun `station mapper copies availability and location`() {
    val station =
      station(
        id = "s1",
        name = "Plaza España",
        address = "Calle 1",
        bikes = 7,
        ebikes = 2,
        regular = 5,
        freeDocks = 4,
        distance = 320,
        latitude = 41.65,
        longitude = -0.88,
      )

    val info =
      station.toStationInfo(
        isFavorite = true,
        isHome = true,
        isWork = false,
        lastUpdatedEpoch = null,
        distanceIsUserRelative = true,
      )

    assertEquals("s1", info.id)
    assertEquals("Plaza España", info.name)
    assertEquals("Calle 1", info.address)
    assertEquals(7, info.bikesAvailable)
    assertEquals(2, info.ebikesAvailable)
    assertEquals(5, info.regularBikesAvailable)
    assertEquals(4, info.freeDocks)
    assertEquals(320, info.distanceMeters)
    assertEquals(41.65, info.latitude, 0.0)
    assertEquals(-0.88, info.longitude, 0.0)
  }

  @Test
  fun `station mapper propagates favorite home and work flags`() {
    val info =
      station("s1").toStationInfo(
        isFavorite = true,
        isHome = false,
        isWork = true,
        lastUpdatedEpoch = null,
        distanceIsUserRelative = true,
      )

    assertTrue(info.isFavorite)
    assertFalse(info.isHome)
    assertTrue(info.isWork)
  }

  @Test
  fun `station mapper reports unknown distance as minus one`() {
    val info =
      station("s1", distance = 1_500).toStationInfo(
        isFavorite = false,
        isHome = false,
        isWork = false,
        lastUpdatedEpoch = null,
        distanceIsUserRelative = false,
      )

    assertEquals(-1, info.distanceMeters)
  }

  @Test
  fun `station mapper keeps distance when it is user relative`() {
    val info =
      station("s1", distance = 1_500).toStationInfo(
        isFavorite = false,
        isHome = false,
        isWork = false,
        lastUpdatedEpoch = null,
        distanceIsUserRelative = true,
      )

    assertEquals(1_500, info.distanceMeters)
  }

  @Test
  fun `station mapper converts the repository timestamp to Instant`() {
    val epochMillis = 1_700_000_000_000L

    val info =
      station("s1").toStationInfo(
        isFavorite = false,
        isHome = false,
        isWork = false,
        lastUpdatedEpoch = epochMillis,
        distanceIsUserRelative = true,
      )

    assertEquals(Instant.ofEpochMilli(epochMillis), info.updatedAt)
  }

  @Test
  fun `station mapper leaves updatedAt null when the repository has no timestamp`() {
    val info =
      station("s1").toStationInfo(
        isFavorite = false,
        isHome = false,
        isWork = false,
        lastUpdatedEpoch = null,
        distanceIsUserRelative = true,
      )

    assertNull(info.updatedAt)
  }

  @Test
  fun `city mapper copies identity display name and ebike support`() {
    val info = City.ZARAGOZA.toCityInfo()

    assertEquals("zaragoza", info.id)
    assertEquals("Zaragoza", info.displayName)
    assertTrue(info.supportsEbikes)
  }
}

internal fun station(
  id: String,
  name: String = "Station $id",
  address: String = "Address $id",
  bikes: Int = 5,
  ebikes: Int = 0,
  regular: Int = bikes - ebikes,
  freeDocks: Int = 3,
  distance: Int = 100,
  latitude: Double = 41.65,
  longitude: Double = -0.88,
): Station =
  Station(
    id = id,
    name = name,
    address = address,
    location = GeoPoint(latitude = latitude, longitude = longitude),
    bikesAvailable = bikes,
    slotsFree = freeDocks,
    distanceMeters = distance,
    ebikesAvailable = ebikes,
    regularBikesAvailable = regular,
  )
