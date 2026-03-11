package com.gcaguilar.bizizaragoza

import com.gcaguilar.bizizaragoza.mobileui.MobileLaunchRequest
import com.gcaguilar.bizizaragoza.mobileui.AssistantLaunchRequest
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
      MobileLaunchRequest.NearestStationWithBikes,
      parseLaunchRequest(assistantAction = NEAREST_STATION_WITH_BIKES_ACTION),
    )
    assertEquals(
      MobileLaunchRequest.NearestStationWithSlots,
      parseLaunchRequest(feature = NEAREST_STATION_WITH_SLOTS_ACTION),
    )
    assertEquals(
      MobileLaunchRequest.OpenAssistant,
      parseLaunchRequest(assistantAction = OPEN_ASSISTANT_ACTION),
    )
    assertEquals(
      MobileLaunchRequest.StationStatus,
      parseLaunchRequest(feature = STATION_STATUS_ACTION),
    )
    assertNull(parseLaunchRequest(feature = STATION_BIKE_COUNT_ACTION))
    assertNull(parseLaunchRequest(feature = STATION_SLOT_COUNT_ACTION))
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
      MobileLaunchRequest.NearestStationWithBikes,
      parseLaunchRequest(feature = "Estación cercana con bicis"),
    )
    assertEquals(
      MobileLaunchRequest.NearestStationWithSlots,
      parseLaunchRequest(feature = "Estación cercana con huecos"),
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

  @Test
  fun `parseLaunchPayload resolves station queries for search and assistant actions`() {
    val searchPayload = parseLaunchPayload(stationQuery = "Plaza España")
    val statusPayload = parseLaunchPayload(
      assistantAction = STATION_STATUS_ACTION,
      stationQuery = "42",
    )
    val bikeCountPayload = parseLaunchPayload(
      assistantAction = STATION_BIKE_COUNT_ACTION,
      stationQuery = "Universidad",
    )
    val routePayload = parseLaunchPayload(
      assistantAction = ROUTE_TO_STATION_ACTION,
      stationQuery = "Plaza Aragón",
    )

    assertEquals(
      AssistantLaunchRequest.SearchStation("Plaza España"),
      searchPayload?.assistantLaunchRequest,
    )
    assertEquals(
      AssistantLaunchRequest.StationStatus(stationQuery = "42"),
      statusPayload?.assistantLaunchRequest,
    )
    assertEquals(
      AssistantLaunchRequest.StationBikeCount(stationQuery = "Universidad"),
      bikeCountPayload?.assistantLaunchRequest,
    )
    assertEquals(
      AssistantLaunchRequest.RouteToStation(stationQuery = "Plaza Aragón"),
      routePayload?.assistantLaunchRequest,
    )
  }

  @Test
  fun `parseLaunchPayload keeps route station id and supports bike slot count station ids`() {
    val routePayload = parseLaunchPayload(
      assistantAction = ROUTE_TO_STATION_ACTION,
      stationId = "station-42",
    )
    val bikeCountPayload = parseLaunchPayload(
      assistantAction = STATION_BIKE_COUNT_ACTION,
      stationId = "station-7",
    )
    val slotCountPayload = parseLaunchPayload(
      assistantAction = STATION_SLOT_COUNT_ACTION,
      stationId = "station-9",
    )

    assertEquals(MobileLaunchRequest.RouteToStation("station-42"), routePayload?.launchRequest)
    assertEquals(
      AssistantLaunchRequest.StationBikeCount(stationId = "station-7"),
      bikeCountPayload?.assistantLaunchRequest,
    )
    assertEquals(
      AssistantLaunchRequest.StationSlotCount(stationId = "station-9"),
      slotCountPayload?.assistantLaunchRequest,
    )
  }
}
