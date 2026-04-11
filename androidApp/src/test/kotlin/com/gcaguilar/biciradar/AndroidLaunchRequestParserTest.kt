package com.gcaguilar.biciradar

import com.gcaguilar.biciradar.mobileui.navigation.AssistantLaunchRequest
import com.gcaguilar.biciradar.mobileui.navigation.MobileLaunchRequest
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
    val withStation =
      parseLaunchRequest(
        assistantAction = ROUTE_TO_STATION_ACTION,
        stationId = "station-42",
      )
    val withoutStation =
      parseLaunchRequest(
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
    val request =
      parseLaunchRequest(
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
    val statusPayload =
      parseLaunchPayload(
        assistantAction = STATION_STATUS_ACTION,
        stationQuery = "42",
      )
    val bikeCountPayload =
      parseLaunchPayload(
        assistantAction = STATION_BIKE_COUNT_ACTION,
        stationQuery = "Universidad",
      )
    val routePayload =
      parseLaunchPayload(
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
    val routePayload =
      parseLaunchPayload(
        assistantAction = ROUTE_TO_STATION_ACTION,
        stationId = "station-42",
      )
    val bikeCountPayload =
      parseLaunchPayload(
        assistantAction = STATION_BIKE_COUNT_ACTION,
        stationId = "station-7",
      )
    val slotCountPayload =
      parseLaunchPayload(
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

  @Test
  fun `parseLaunchPayload extracts station query from natural voice phrases`() {
    val statusPayload =
      parseLaunchPayload(
        assistantAction = "Cómo está Plaza España en Bici Radar",
      )
    val bikeCountPayload =
      parseLaunchPayload(
        feature = "Cuántas bicis hay en la estación 48 con Bici Radar",
      )
    val slotCountPayload =
      parseLaunchPayload(
        feature = "Cuántos huecos hay en Plaza Aragón",
      )
    val routePayload =
      parseLaunchPayload(
        assistantAction = "Llévame a trabajo con Bici Radar",
      )

    assertEquals(
      AssistantLaunchRequest.StationStatus(stationQuery = "plaza espana"),
      statusPayload?.assistantLaunchRequest,
    )
    assertEquals(
      AssistantLaunchRequest.StationBikeCount(stationQuery = "48"),
      bikeCountPayload?.assistantLaunchRequest,
    )
    assertEquals(
      AssistantLaunchRequest.StationSlotCount(stationQuery = "plaza aragon"),
      slotCountPayload?.assistantLaunchRequest,
    )
    assertEquals(
      AssistantLaunchRequest.RouteToStation(stationQuery = "trabajo"),
      routePayload?.assistantLaunchRequest,
    )
  }

  @Test
  fun `parseLaunchPayload understands broader natural route and availability phrases`() {
    val routePayload =
      parseLaunchPayload(
        assistantAction = "Cómo llego a Plaza España con Bici Radar",
      )
    val bikesPayload =
      parseLaunchPayload(
        assistantAction = "Hay bicis en la estación 42 de Bici Radar",
      )
    val slotsPayload =
      parseLaunchPayload(
        assistantAction = "Hay huecos en Plaza Aragón con Bici Radar",
      )

    assertEquals(
      AssistantLaunchRequest.RouteToStation(stationQuery = "plaza espana"),
      routePayload?.assistantLaunchRequest,
    )
    assertEquals(
      AssistantLaunchRequest.StationBikeCount(stationQuery = "42"),
      bikesPayload?.assistantLaunchRequest,
    )
    assertEquals(
      AssistantLaunchRequest.StationSlotCount(stationQuery = "plaza aragon"),
      slotsPayload?.assistantLaunchRequest,
    )
  }

  @Test
  fun `parseLaunchPayload understands conversational prompts and filler words`() {
    val bikesPayload =
      parseLaunchPayload(
        assistantAction = "Dime cuántas bicis hay en la estación 48 con Bici Radar por favor",
      )
    val slotsPayload =
      parseLaunchPayload(
        assistantAction = "Quiero saber cuántos huecos hay en Plaza Aragón con Bici Radar ahora mismo",
      )
    val nearbySlotsPayload =
      parseLaunchPayload(
        assistantAction = "Dónde puedo anclar la bici con Bici Radar",
      )

    assertEquals(
      AssistantLaunchRequest.StationBikeCount(stationQuery = "48"),
      bikesPayload?.assistantLaunchRequest,
    )
    assertEquals(
      AssistantLaunchRequest.StationSlotCount(stationQuery = "plaza aragon"),
      slotsPayload?.assistantLaunchRequest,
    )
    assertEquals(
      MobileLaunchRequest.NearestStationWithSlots,
      nearbySlotsPayload?.launchRequest,
    )
  }

  @Test
  fun `parseLaunchPayload supports route phrases with al contraction`() {
    val payload =
      parseLaunchPayload(
        assistantAction = "Llévame al trabajo con Bici Radar",
      )

    assertEquals(
      AssistantLaunchRequest.RouteToStation(stationQuery = "trabajo"),
      payload?.assistantLaunchRequest,
    )
  }

  @Test
  fun `parseLaunchPayload resolves native deep link hosts`() {
    val stationPayload =
      parseLaunchPayload(
        source =
          AndroidLaunchSource(
            deepLinkHost = "station",
            deepLinkPathSegment = "station-7",
          ),
      )
    val monitorPayload =
      parseLaunchPayload(
        source =
          AndroidLaunchSource(
            deepLinkHost = "monitor",
            deepLinkPathSegment = "station-9",
          ),
      )
    val cityPayload =
      parseLaunchPayload(
        source =
          AndroidLaunchSource(
            deepLinkHost = "city",
            deepLinkPathSegment = "zaragoza",
          ),
      )

    assertEquals(MobileLaunchRequest.ShowStation("station-7"), stationPayload?.launchRequest)
    assertEquals(MobileLaunchRequest.MonitorStation("station-9"), monitorPayload?.launchRequest)
    assertEquals(MobileLaunchRequest.SelectCity("zaragoza"), cityPayload?.launchRequest)
    val alertsPayload =
      parseLaunchPayload(
        source =
          AndroidLaunchSource(
            deepLinkHost = "alerts",
          ),
      )
    assertEquals(MobileLaunchRequest.SavedPlaceAlerts, alertsPayload?.launchRequest)
  }

  @Test
  fun `parseLaunchRequest resolves saved place alerts action`() {
    assertEquals(
      MobileLaunchRequest.SavedPlaceAlerts,
      parseLaunchRequest(feature = SAVED_PLACE_ALERTS_ACTION),
    )
  }
}
