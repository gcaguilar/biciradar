package com.gcaguilar.biciradar

import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.SurfaceMonitoringStatus
import com.gcaguilar.biciradar.core.SurfaceStatusLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidSurfaceRenderingTest {
  @Test
  fun `widget empty state prefers favorite setup before stale data`() {
    val snapshot = AndroidSurfaceWidgetSnapshot(
      hasFavoriteStation = false,
      isDataFresh = false,
    )

    assertEquals(AndroidWidgetEmptyState.ConfigureFavorite, widgetEmptyState(snapshot))
    assertEquals(
      "Configura una estación favorita",
      widgetEmptyMessage(
        state = widgetEmptyState(snapshot),
        configureFavorite = "Configura una estación favorita",
        openAppToRefresh = "Abre la app para actualizar",
        dataUnavailable = "Datos no disponibles",
      ),
    )
  }

  @Test
  fun `nearby station meta includes distance when available`() {
    val station = AndroidSurfaceNearbyStation(
      id = "station-1",
      name = "Plaza Espana",
      bikesAvailable = 5,
      docksAvailable = 7,
      distanceMeters = 140,
      statusText = "Disponible",
    )

    assertEquals("5 bicis · 7 huecos · 140 m", nearbyStationMeta(station))
  }

  @Test
  fun `quick actions fall back to favorites when no favorite station exists`() {
    val configured = quickActionsState(
      AndroidSurfaceWidgetSnapshot(
        favoriteStation = AndroidSurfaceFavoriteStation(
          id = "station-9",
          name = "Delicias",
          bikesAvailable = 4,
          docksAvailable = 8,
          statusText = "Disponible",
          lastUpdatedEpoch = 100L,
        ),
      ),
    )
    val empty = quickActionsState(AndroidSurfaceWidgetSnapshot())

    assertEquals("biciradar://monitor/station-9", configured.monitorUri)
    assertFalse(configured.requiresConfiguration)
    assertEquals("biciradar://favorites", empty.monitorUri)
    assertTrue(empty.requiresConfiguration)
  }

  @Test
  fun `monitoring notification body appends alternative when present`() {
    val session = SurfaceMonitoringSession(
      stationId = "station-1",
      stationName = "Plaza Espana",
      cityId = "zaragoza",
      kind = SurfaceMonitoringKind.Docks,
      status = SurfaceMonitoringStatus.AlternativeAvailable,
      bikesAvailable = 3,
      docksAvailable = 0,
      statusLevel = SurfaceStatusLevel.Full,
      startedAtEpoch = 1L,
      expiresAtEpoch = 60_000L,
      lastUpdatedEpoch = 1L,
      isActive = true,
      alternativeStationId = "station-2",
      alternativeStationName = "Paraninfo",
      alternativeDistanceMeters = 120,
    )

    assertEquals(
      "3 bicis · 0 huecos · 1m 5s · Alt: Paraninfo",
      monitoringNotificationBody(session, remainingSeconds = 65),
    )
  }
}
