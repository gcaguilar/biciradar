package com.gcaguilar.biciradar

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GarminPayloadBuilderTest {
  private val builder = GarminPayloadBuilder()

  @Test
  fun `build returns nearest and four backups max`() {
    val stations =
      (1..10).map { index ->
        Station(
          id = index.toString(),
          name = "Station $index",
          address = "Address $index",
          location = GeoPoint(41.65 + index, -0.88),
          bikesAvailable = index,
          slotsFree = 10 - index,
          distanceMeters = index * 100,
        )
      }

    val payload = builder.build(stations, lastUpdatedEpochMs = 123_000L)

    val nearest = payload["nearest"] as Map<*, *>
    val backup = payload["backup"] as List<*>

    assertEquals("1", nearest["id"])
    assertEquals(4, backup.size)
    assertEquals(123, payload["timestamp"])
  }

  @Test
  fun `build keeps null nearest when no stations`() {
    val payload = builder.build(emptyList(), lastUpdatedEpochMs = 5_000L)

    assertNull(payload["nearest"])
    assertEquals(emptyList<Any>(), payload["backup"])
    assertEquals(5, payload["timestamp"])
  }

  @Test
  fun `build truncates long names with ascii ellipsis`() {
    val payload =
      builder.build(
        listOf(
          Station(
            id = "1",
            name = "Estacion de la Plaza del Pilar con nombre muy largo",
            address = "Address",
            location = GeoPoint(41.65, -0.88),
            bikesAvailable = 5,
            slotsFree = 5,
            distanceMeters = 100,
            ebikesAvailable = 2,
          ),
        ),
      )

    val nearest = payload["nearest"] as Map<*, *>
    val name = nearest["name"] as String

    assertNotNull(nearest)
    assertEquals(20, name.length)
    assertEquals("...", name.takeLast(3))
    assertEquals(2, nearest["ebikes"])
  }
}
