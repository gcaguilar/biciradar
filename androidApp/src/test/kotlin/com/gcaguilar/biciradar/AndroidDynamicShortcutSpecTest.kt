package com.gcaguilar.biciradar

import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle
import com.gcaguilar.biciradar.core.SurfaceState
import com.gcaguilar.biciradar.core.SurfaceStationSnapshot
import com.gcaguilar.biciradar.core.SurfaceStatusLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidDynamicShortcutSpecTest {
  @Test
  fun `dynamic shortcuts always expose nearby and favorites`() {
    val shortcuts = dynamicShortcutSpecs(snapshot = null)

    assertEquals(listOf("surface_nearby", "surface_favorites"), shortcuts.map { it.id })
    assertEquals("biciradar://home", shortcuts.first().uri)
  }

  @Test
  fun `dynamic shortcuts add favorite station and monitoring when snapshot exists`() {
    val shortcuts = dynamicShortcutSpecs(
      snapshot = SurfaceSnapshotBundle(
        generatedAtEpoch = 1L,
        favoriteStation = SurfaceStationSnapshot(
          id = "station-42",
          nameShort = "Plaza Espana",
          nameFull = "Plaza Espana",
          cityId = "zaragoza",
          latitude = 41.65,
          longitude = -0.88,
          bikesAvailable = 6,
          docksAvailable = 5,
          statusTextShort = "Disponible",
          statusLevel = SurfaceStatusLevel.Good,
          lastUpdatedEpoch = 1L,
          isFavorite = true,
        ),
        state = SurfaceState(
          hasLocationPermission = true,
          hasNotificationPermission = true,
          hasFavoriteStation = true,
          isDataFresh = true,
          cityId = "zaragoza",
          cityName = "Zaragoza",
        ),
      ),
    )

    assertEquals(
      listOf("surface_nearby", "surface_favorites", "surface_favorite_station", "surface_monitor_favorite"),
      shortcuts.map { it.id },
    )
    assertTrue(shortcuts.any { it.uri == "biciradar://station/station-42" })
    assertTrue(shortcuts.any { it.uri == "biciradar://monitor/station-42" })
  }
}
