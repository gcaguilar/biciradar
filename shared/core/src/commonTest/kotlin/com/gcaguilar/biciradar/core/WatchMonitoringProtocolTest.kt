package com.gcaguilar.biciradar.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WatchMonitoringProtocolTest {
  @Test
  fun `start command round trips`() {
    val payload =
      WatchMonitoringCommandPayload(
        action = WatchMonitoringProtocol.ACTION_START,
        stationId = "station-42",
        durationSeconds = 600,
        kind = SurfaceMonitoringKind.Docks.name,
      )

    val decoded = WatchMonitoringProtocol.decodeCommand(WatchMonitoringProtocol.encodeCommand(payload))

    assertEquals(payload, decoded)
  }

  @Test
  fun `stop command round trips without duration`() {
    val payload =
      WatchMonitoringCommandPayload(
        action = WatchMonitoringProtocol.ACTION_STOP,
        stationId = null,
      )

    val decoded = WatchMonitoringProtocol.decodeCommand(WatchMonitoringProtocol.encodeCommand(payload))

    assertEquals(payload, decoded)
  }

  @Test
  fun `result keeps the failure reason`() {
    val payload =
      WatchMonitoringResultPayload(
        stationId = "station-42",
        success = false,
        failure = TripMonitoringFailure.SignInRequired.name,
      )

    val decoded = WatchMonitoringProtocol.decodeResult(WatchMonitoringProtocol.encodeResult(payload))

    assertEquals(payload, decoded)
  }

  @Test
  fun `invalid payloads are ignored`() {
    assertNull(WatchMonitoringProtocol.decodeCommand("not json".encodeToByteArray()))
    assertNull(WatchMonitoringProtocol.decodeResult(byteArrayOf(0x00, 0x01)))
  }
}
