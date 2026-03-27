package com.gcaguilar.biciradar.wear

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.SurfaceMonitoringStatus
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle
import com.gcaguilar.biciradar.core.SurfaceState
import com.gcaguilar.biciradar.core.SurfaceStationSnapshot
import com.gcaguilar.biciradar.core.SurfaceStatusLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WearStationPresentationTest {
  @Test
  fun `sort wear favorite stations prioritizes home then work`() {
    val sorted = sortWearFavoriteStations(
      stations = listOf(
        station(id = "work", distanceMeters = 300),
        station(id = "other", distanceMeters = 80),
        station(id = "home", distanceMeters = 200),
      ),
      homeStationId = "home",
      workStationId = "work",
    )

    assertEquals(listOf("home", "work", "other"), sorted.map { it.id })
  }

  @Test
  fun `wear saved place label maps home and work ids`() {
    assertEquals("Casa", wearSavedPlaceLabel("home", homeStationId = "home", workStationId = "work"))
    assertEquals("Trabajo", wearSavedPlaceLabel("work", homeStationId = "home", workStationId = "work"))
    assertNull(wearSavedPlaceLabel("other", homeStationId = "home", workStationId = "work"))
  }

  @Test
  fun `wear favorite surface state exposes favorite snapshot summary`() {
    val state = wearFavoriteSurfaceState(
      snapshot = SurfaceSnapshotBundle(
        generatedAtEpoch = 61_000L,
        favoriteStation = SurfaceStationSnapshot(
          id = "home",
          nameShort = "Plaza Espana",
          nameFull = "Plaza Espana 1",
          cityId = "zaragoza",
          latitude = 41.65,
          longitude = -0.88,
          bikesAvailable = 4,
          docksAvailable = 7,
          statusTextShort = "Disponible",
          statusLevel = SurfaceStatusLevel.Good,
          lastUpdatedEpoch = 1_000L,
          isFavorite = true,
        ),
        state = surfaceState(),
      ),
      nowEpoch = 61_000L,
    )

    assertEquals(WearFavoriteSurfaceKind.Favorite, state.kind)
    assertEquals("Plaza Espana", state.title)
    assertEquals("Disponible", state.statusText)
    assertEquals("Hace 1 min", state.updatedText)
    assertEquals("🚲 4", state.bikesLabel)
    assertEquals("🅿 7", state.docksLabel)
  }

  @Test
  fun `wear favorite surface state prefers configure message when no favorite exists`() {
    val state = wearFavoriteSurfaceState(
      snapshot = SurfaceSnapshotBundle(
        generatedAtEpoch = 2_000L,
        state = surfaceState(hasFavoriteStation = false, isDataFresh = false),
      ),
    )

    assertEquals(WearFavoriteSurfaceKind.ConfigureFavorite, state.kind)
    assertEquals("Sin favorita", state.title)
    assertEquals("Configúrala en la app", state.supportingText)
  }

  @Test
  fun `wear favorite tile state exposes compact tile content and alternative`() {
    val state = wearFavoriteTileState(
      snapshot = SurfaceSnapshotBundle(
        generatedAtEpoch = 61_000L,
        favoriteStation = SurfaceStationSnapshot(
          id = "home",
          nameShort = "Plaza Espana",
          nameFull = "Plaza Espana 1",
          cityId = "zaragoza",
          latitude = 41.65,
          longitude = -0.88,
          bikesAvailable = 4,
          docksAvailable = 0,
          statusTextShort = "Sin huecos",
          statusLevel = SurfaceStatusLevel.Full,
          lastUpdatedEpoch = 1_000L,
          isFavorite = true,
          alternativeStationId = "alt",
          alternativeStationName = "Paraninfo",
          alternativeDistanceMeters = 120,
        ),
        state = surfaceState(),
      ),
      nowEpoch = 61_000L,
    )

    assertEquals("Plaza Espana", state.title)
    assertEquals("Sin huecos", state.label)
    assertEquals("Hace 1 min", state.updatedText)
    assertEquals("🚲 4  🅿 0 · Alt: Paraninfo (120 m)", state.body)
    assertEquals("home", state.stationId)
  }

  @Test
  fun `wear favorite tile state reuses city fallback when no favorite exists`() {
    val state = wearFavoriteTileState(
      snapshot = SurfaceSnapshotBundle(
        generatedAtEpoch = 2_000L,
        state = surfaceState(hasFavoriteStation = false, isDataFresh = false),
      ),
    )

    assertEquals("Sin favorita", state.title)
    assertEquals("Zaragoza", state.label)
    assertEquals("Configúrala en la app", state.body)
    assertNull(state.stationId)
  }

  @Test
  fun `wear monitoring surface state includes countdown and alternative`() {
    val state = wearMonitoringSurfaceState(
      session = SurfaceMonitoringSession(
        stationId = "home",
        stationName = "Plaza Espana",
        cityId = "zaragoza",
        kind = SurfaceMonitoringKind.Docks,
        status = SurfaceMonitoringStatus.AlternativeAvailable,
        bikesAvailable = 3,
        docksAvailable = 0,
        statusLevel = SurfaceStatusLevel.Full,
        startedAtEpoch = 1L,
        expiresAtEpoch = 61_000L,
        lastUpdatedEpoch = 1_000L,
        isActive = true,
        alternativeStationId = "alt",
        alternativeStationName = "Paraninfo",
        alternativeDistanceMeters = 120,
      ),
      remainingSeconds = 65,
    )

    assertEquals("Alternativa sugerida", state.statusText)
    assertEquals("1m 5s", state.countdownText)
    assertEquals("🚲 3", state.bikesLabel)
    assertEquals("🅿 0", state.docksLabel)
    assertEquals("Alt: Paraninfo (120 m)", state.alternativeText)
  }

  @Test
  fun `wear saved place surface states keep home work order and omit missing stations`() {
    val states = wearSavedPlaceSurfaceStates(
      stations = listOf(
        station(id = "other", distanceMeters = 80),
        station(id = "work", distanceMeters = 300),
        station(id = "home", distanceMeters = 200),
      ),
      homeStationId = "home",
      workStationId = "work",
    )

    assertEquals(listOf("Casa", "Trabajo"), states.map { it.label })
    assertEquals(listOf("home", "work"), states.map { it.stationId })
  }
}

private fun station(
  id: String,
  distanceMeters: Int,
): Station = Station(
  id = id,
  name = id,
  address = id,
  location = GeoPoint(41.65, -0.88),
  bikesAvailable = 4,
  slotsFree = 6,
  distanceMeters = distanceMeters,
)

private fun surfaceState(
  hasFavoriteStation: Boolean = true,
  isDataFresh: Boolean = true,
): SurfaceState = SurfaceState(
  hasLocationPermission = true,
  hasNotificationPermission = true,
  hasFavoriteStation = hasFavoriteStation,
  isDataFresh = isDataFresh,
  lastSyncEpoch = 1_000L,
  cityId = "zaragoza",
  cityName = "Zaragoza",
)
