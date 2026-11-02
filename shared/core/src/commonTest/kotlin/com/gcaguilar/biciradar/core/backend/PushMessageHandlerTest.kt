package com.gcaguilar.biciradar.core.backend

import com.gcaguilar.biciradar.core.NoOpLogger
import com.gcaguilar.biciradar.core.RefreshWidgetDataUseCase
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.SurfaceMonitoringStatus
import com.gcaguilar.biciradar.core.SurfaceStatusLevel
import com.gcaguilar.biciradar.testutils.FakeStationsRepository
import com.gcaguilar.biciradar.testutils.FakeSurfaceMonitoringRepository
import com.gcaguilar.biciradar.testutils.FakeSurfaceSnapshotRepository
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertNull

class PushMessageHandlerTest {
  @Test
  fun `terminal push stops the surface session for its station`() =
    runTest {
      val surfaceMonitoring =
        FakeSurfaceMonitoringRepository().apply {
          state.value = mapOf("station-1" to session("station-1"))
        }
      val handler =
        PushMessageHandler(
          pushMessageBus = PushMessageBus(),
          stationsRepository = FakeStationsRepository(),
          refreshWidgetDataUseCase = RefreshWidgetDataUseCase(FakeSurfaceSnapshotRepository(), NoOpLogger),
          surfaceMonitoringRepository = surfaceMonitoring,
          json = Json,
        )
      val payload =
        Json.encodeToString(
          PushMessage.serializer(),
          PushMessage(
            cityId = "zaragoza",
            stationId = "station-1",
            event = "DOCKS_EMPTY",
            routineId = "window-1",
          ),
        )

      handler.onDataMessage(mapOf("payload" to payload))

      assertNull(surfaceMonitoring.sessionFor("station-1"))
    }

  private fun session(stationId: String) =
    SurfaceMonitoringSession(
      stationId = stationId,
      stationName = "Plaza España",
      cityId = "zaragoza",
      kind = SurfaceMonitoringKind.Docks,
      status = SurfaceMonitoringStatus.Monitoring,
      bikesAvailable = 3,
      docksAvailable = 0,
      statusLevel = SurfaceStatusLevel.Full,
      startedAtEpoch = 0,
      expiresAtEpoch = 1,
      lastUpdatedEpoch = 0,
      isActive = true,
    )
}
