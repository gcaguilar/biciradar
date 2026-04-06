package com.gcaguilar.biciradar

import com.gcaguilar.biciradar.mobileui.navigation.AssistantLaunchRequest
import com.gcaguilar.biciradar.mobileui.navigation.MobileLaunchRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AndroidLaunchSourceTest {
  @Test
  fun `parseLaunchPayload resolves deep link action and station query`() {
    val payload = parseLaunchPayload(
      AndroidLaunchSource(
        deepLinkAction = STATION_BIKE_COUNT_ACTION,
        deepLinkStationQuery = "Plaza España",
      ),
    )

    assertNull(payload?.launchRequest)
    assertEquals(
      AssistantLaunchRequest.StationBikeCount(stationQuery = "Plaza España"),
      payload?.assistantLaunchRequest,
    )
  }

  @Test
  fun `parseLaunchPayload resolves generic station query from deep link aliases`() {
    val payload = parseLaunchPayload(
      AndroidLaunchSource(
        deepLinkQuery = "Plaza Aragón",
      ),
    )

    assertEquals(
      AssistantLaunchRequest.SearchStation("Plaza Aragón"),
      payload?.assistantLaunchRequest,
    )
  }

  @Test
  fun `parseLaunchPayload resolves route station id aliases`() {
    val payload = parseLaunchPayload(
      AndroidLaunchSource(
        deepLinkAction = ROUTE_TO_STATION_ACTION,
        deepLinkStationIdAlias = "station-48",
      ),
    )

    assertEquals(
      MobileLaunchRequest.RouteToStation("station-48"),
      payload?.launchRequest,
    )
  }

  @Test
  fun `parseLaunchPayload prefers explicit extras over deep link parameters`() {
    val payload = parseLaunchPayload(
      AndroidLaunchSource(
        assistantAction = ROUTE_TO_STATION_ACTION,
        deepLinkAction = NEAREST_STATION_ACTION,
        stationQuery = "Plaza del Pilar",
        deepLinkStationQuery = "Ignored",
      ),
    )

    assertEquals(
      AssistantLaunchRequest.RouteToStation(stationQuery = "Plaza del Pilar"),
      payload?.assistantLaunchRequest,
    )
  }

  @Test
  fun `parseLaunchPayload falls back to platform text queries when assistant extras are missing`() {
    val payload = parseLaunchPayload(
      AndroidLaunchSource(
        textQuery = "Cuántas bicis hay en Plaza España con Bici Radar",
      ),
    )

    assertEquals(
      AssistantLaunchRequest.StationBikeCount(stationQuery = "plaza espana"),
      payload?.assistantLaunchRequest,
    )
  }

  @Test
  fun `show station shortcut without query leaves payload empty`() {
    val payload = parseLaunchPayload(
      AndroidLaunchSource(
        assistantAction = SHOW_STATION_ACTION,
      ),
    )

    assertEquals(AndroidLaunchPayload(), payload)
  }
}
