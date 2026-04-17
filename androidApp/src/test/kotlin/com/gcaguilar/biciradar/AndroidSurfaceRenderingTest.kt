package com.gcaguilar.biciradar

import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.SurfaceMonitoringStatus
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle
import com.gcaguilar.biciradar.core.SurfaceState
import com.gcaguilar.biciradar.core.SurfaceStationSnapshot
import com.gcaguilar.biciradar.core.SurfaceStatusLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidSurfaceRenderingTest {
  @Test
  fun `widget empty state prefers favorite setup before stale data`() {
    val snapshot =
      AndroidSurfaceWidgetSnapshot(
        hasFavoriteStation = false,
        isDataFresh = false,
      )

    assertEquals(AndroidWidgetEmptyState.ConfigureFavorite, widgetEmptyState(snapshot))
    assertEquals(
      "Configura una estación favorita",
      widgetEmptyMessage(
        state = widgetEmptyState(snapshot),
        configureFavorite = "Configura una estación favorita",
        noLocationPermission = "Sin permiso de ubicación",
        openAppToRefresh = "Abre la app para actualizar",
        dataUnavailable = "Datos no disponibles",
      ),
    )
  }

  @Test
  fun `nearby widget empty state prioritizes location permission`() {
    val snapshot =
      AndroidSurfaceWidgetSnapshot(
        hasLocationPermission = false,
        isDataFresh = false,
      )

    assertEquals(AndroidWidgetEmptyState.NoLocationPermission, nearbyWidgetEmptyState(snapshot))
    assertEquals(
      "Sin permiso de ubicación",
      widgetEmptyMessage(
        state = nearbyWidgetEmptyState(snapshot),
        configureFavorite = "Configura una estación favorita",
        noLocationPermission = "Sin permiso de ubicación",
        openAppToRefresh = "Abre la app para actualizar",
        dataUnavailable = "Datos no disponibles",
      ),
    )
  }

  @Test
  fun `nearby station meta includes distance when available`() {
    val station =
      AndroidSurfaceNearbyStation(
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
  fun `saved place meta includes counts and status`() {
    val station =
      AndroidSurfaceSavedPlaceStation(
        id = "station-1",
        name = "Plaza Espana",
        bikesAvailable = 5,
        docksAvailable = 7,
        statusText = "Disponible",
      )

    assertEquals("5 bicis · 7 huecos · Disponible", savedPlaceMeta(station))
  }

  @Test
  fun `commute place state falls back to refresh or setup copy when place is missing`() {
    val stale =
      commutePlaceState(
        label = "Casa",
        station = null,
        snapshot = AndroidSurfaceWidgetSnapshot(isDataFresh = false),
        configureSavedPlaces = "Elige tus estaciones",
        openAppToRefresh = "Abre la app para actualizar",
        missingTitle = "Sin configurar",
      )
    val configured =
      commutePlaceState(
        label = "Trabajo",
        station =
          AndroidSurfaceSavedPlaceStation(
            id = "station-9",
            name = "Delicias",
            bikesAvailable = 4,
            docksAvailable = 8,
            statusText = "Disponible",
          ),
        snapshot = AndroidSurfaceWidgetSnapshot(),
        configureSavedPlaces = "Elige tus estaciones",
        openAppToRefresh = "Abre la app para actualizar",
        missingTitle = "Sin configurar",
      )

    assertEquals("Sin configurar", stale.title)
    assertEquals("Abre la app para actualizar", stale.meta)
    assertEquals(null, stale.stationId)
    assertEquals("Delicias", configured.title)
    assertEquals("4 bicis · 8 huecos · Disponible", configured.meta)
    assertEquals("station-9", configured.stationId)
  }

  @Test
  fun `quick actions fall back to favorites when no favorite station exists`() {
    val configured =
      quickActionsState(
        AndroidSurfaceWidgetSnapshot(
          favoriteStation =
            AndroidSurfaceFavoriteStation(
              id = "station-9",
              name = "Delicias",
              bikesAvailable = 4,
              docksAvailable = 8,
              statusText = "Disponible",
              lastUpdatedEpoch = 100L,
            ),
          hasNotificationPermission = false,
        ),
      )
    val empty = quickActionsState(AndroidSurfaceWidgetSnapshot())

    assertEquals("biciradar://monitor/station-9", configured.monitorUri)
    assertFalse(configured.requiresConfiguration)
    assertTrue(configured.requiresNotificationPermission)
    assertEquals("biciradar://favorites", empty.monitorUri)
    assertTrue(empty.requiresConfiguration)
    assertFalse(empty.requiresNotificationPermission)
  }

  @Test
  fun `database surface bundle maps to widget snapshot`() {
    val snapshot =
      SurfaceSnapshotBundle(
        generatedAtEpoch = 1_000L,
        favoriteStation =
          surfaceStationSnapshot(
            id = "fav-1",
            nameShort = "Favorita",
            distanceMeters = 80,
          ),
        homeStation =
          surfaceStationSnapshot(
            id = "home-1",
            nameShort = "Casa",
          ),
        workStation =
          surfaceStationSnapshot(
            id = "work-1",
            nameShort = "Trabajo",
          ),
        nearbyStations =
          listOf(
            surfaceStationSnapshot(
              id = "near-1",
              nameShort = "Cerca",
              distanceMeters = 60,
            ),
          ),
        state =
          SurfaceState(
            hasLocationPermission = true,
            hasNotificationPermission = false,
            hasFavoriteStation = true,
            isDataFresh = true,
            lastSyncEpoch = 900L,
            cityId = "zaragoza",
            cityName = "Zaragoza",
          ),
      ).toAndroidSurfaceWidgetSnapshot()

    assertEquals("fav-1", snapshot.favoriteStation?.id)
    assertEquals("Casa", snapshot.homeStation?.name)
    assertEquals("Trabajo", snapshot.workStation?.name)
    assertEquals(listOf("near-1"), snapshot.nearbyStations.map { it.id })
    assertEquals(60, snapshot.nearbyStations.first().distanceMeters)
    assertTrue(snapshot.hasFavoriteStation == true)
    assertTrue(snapshot.isDataFresh == true)
    assertTrue(snapshot.hasLocationPermission == true)
    assertFalse(snapshot.hasNotificationPermission == true)
  }

  @Test
  fun `monitoring notification title reflects monitoring state`() {
    assertEquals(
      "Plaza Espana",
      monitoringNotificationTitle(monitoringSession(status = SurfaceMonitoringStatus.Monitoring)),
    )
    assertEquals(
      "Plaza Espana sin huecos",
      monitoringNotificationTitle(monitoringSession(status = SurfaceMonitoringStatus.ChangedToFull)),
    )
    assertEquals(
      "Alternativa para Plaza Espana",
      monitoringNotificationTitle(monitoringSession(status = SurfaceMonitoringStatus.AlternativeAvailable)),
    )
    assertEquals(
      "Monitorizacion finalizada",
      monitoringNotificationTitle(monitoringSession(status = SurfaceMonitoringStatus.Expired)),
    )
  }

  @Test
  fun `monitoring notification body includes state countdown and alternative distance`() {
    val session =
      monitoringSession(
        status = SurfaceMonitoringStatus.AlternativeAvailable,
        alternativeStationId = "station-2",
        alternativeStationName = "Paraninfo",
        alternativeDistanceMeters = 120,
      )

    assertEquals(
      "Alternativa sugerida · 3 bicis · 0 huecos · 1m 5s · Alt: Paraninfo (120 m)",
      monitoringNotificationBody(session, remainingSeconds = 65),
    )
  }

  @Test
  fun `monitoring notification body reports final states explicitly`() {
    assertEquals(
      "Plaza Espana · Finalizada por el usuario",
      monitoringNotificationBody(
        monitoringSession(status = SurfaceMonitoringStatus.Ended, isActive = false),
        remainingSeconds = 0,
      ),
    )
    assertEquals(
      "Plaza Espana · Tiempo agotado",
      monitoringNotificationBody(
        monitoringSession(status = SurfaceMonitoringStatus.Expired, isActive = false),
        remainingSeconds = 0,
      ),
    )
  }

  private fun monitoringSession(
    status: SurfaceMonitoringStatus,
    isActive: Boolean = true,
    alternativeStationId: String? = null,
    alternativeStationName: String? = null,
    alternativeDistanceMeters: Int? = null,
  ) = SurfaceMonitoringSession(
    stationId = "station-1",
    stationName = "Plaza Espana",
    cityId = "zaragoza",
    kind = SurfaceMonitoringKind.Docks,
    status = status,
    bikesAvailable = 3,
    docksAvailable = 0,
    statusLevel = SurfaceStatusLevel.Full,
    startedAtEpoch = 1L,
    expiresAtEpoch = 60_000L,
    lastUpdatedEpoch = 1L,
    isActive = isActive,
    alternativeStationId = alternativeStationId,
    alternativeStationName = alternativeStationName,
    alternativeDistanceMeters = alternativeDistanceMeters,
  )

  private fun surfaceStationSnapshot(
    id: String,
    nameShort: String,
    distanceMeters: Int? = null,
  ) = SurfaceStationSnapshot(
    id = id,
    nameShort = nameShort,
    nameFull = "$nameShort completa",
    cityId = "zaragoza",
    latitude = 41.65,
    longitude = -0.88,
    bikesAvailable = 3,
    docksAvailable = 7,
    statusTextShort = "Disponible",
    statusLevel = SurfaceStatusLevel.Good,
    lastUpdatedEpoch = 100L,
    distanceMeters = distanceMeters,
    isFavorite = id.startsWith("fav"),
  )
}
