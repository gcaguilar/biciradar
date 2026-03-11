package com.gcaguilar.bizizaragoza

import com.gcaguilar.bizizaragoza.mobileui.MobileLaunchRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidLaunchRequestParserTest {
  @Test
  fun `parseLaunchRequest resolves stable action ids`() {
    assertEquals(
      MobileLaunchRequest.Favorites,
      parseLaunchRequest(assistantAction = FAVORITE_STATIONS_ACTION),
    )
    assertEquals(
      MobileLaunchRequest.NearestStation,
      parseLaunchRequest(feature = NEAREST_STATION_ACTION),
    )
    assertEquals(
      MobileLaunchRequest.OpenAssistant,
      parseLaunchRequest(assistantAction = OPEN_ASSISTANT_ACTION),
    )
    assertEquals(
      MobileLaunchRequest.StationStatus,
      parseLaunchRequest(feature = STATION_STATUS_ACTION),
    )
  }

  @Test
  fun `parseLaunchRequest keeps optional station id for route actions`() {
    val withStation = parseLaunchRequest(
      assistantAction = ROUTE_TO_STATION_ACTION,
      stationId = "station-42",
    )
    val withoutStation = parseLaunchRequest(
      assistantAction = ROUTE_TO_STATION_ACTION,
      stationId = "   ",
    )

    assertEquals(MobileLaunchRequest.RouteToStation("station-42"), withStation)
    assertEquals(MobileLaunchRequest.RouteToStation(), withoutStation)
  }

  @Test
  fun `parseLaunchRequest accepts legacy favorites and localized feature labels`() {
    assertEquals(
      MobileLaunchRequest.Favorites,
      parseLaunchRequest(feature = "favorites"),
    )
    assertEquals(
      MobileLaunchRequest.RouteToStation(),
      parseLaunchRequest(feature = "Ruta a estación"),
    )
    assertEquals(
      MobileLaunchRequest.StationStatus,
      parseLaunchRequest(feature = " Estado estación "),
    )
  }

  @Test
  fun `parseLaunchRequest requires station id to show a specific station`() {
    val request = parseLaunchRequest(
      assistantAction = SHOW_STATION_ACTION,
      stationId = "station-7",
    )

    assertTrue(request is MobileLaunchRequest.ShowStation)
    assertEquals("station-7", (request as MobileLaunchRequest.ShowStation).stationId)
    assertNull(parseLaunchRequest(assistantAction = SHOW_STATION_ACTION))
  }

  @Test
  fun `parseLaunchRequest ignores unknown actions`() {
    assertNull(parseLaunchRequest(assistantAction = "unknown_action"))
  }
}
