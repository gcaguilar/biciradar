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
    assertEquals(listOf(0, 1), shortcuts.map { it.rank })
  }

  @Test
  fun `dynamic shortcuts add favorite station and monitoring when snapshot exists`() {
    val shortcuts = dynamicShortcutSpecs(
      snapshot = snapshot(
        favoriteStation = station(id = "station-42", name = "Plaza Espana"),
      ),
    )

    assertEquals(
      listOf("surface_nearby", "surface_favorite_station", "surface_monitor_favorite", "surface_favorites"),
      shortcuts.map { it.id },
    )
    assertTrue(shortcuts.any { it.uri == "biciradar://station/station-42" })
    assertTrue(shortcuts.any { it.uri == "biciradar://monitor/station-42" })
  }

  @Test
  fun `dynamic shortcuts add home and work saved places when available`() {
    val shortcuts = dynamicShortcutSpecs(
      snapshot = snapshot(
        homeStation = station(id = "station-home", name = "Puerta del Carmen"),
        workStation = station(id = "station-work", name = "Plaza Aragon"),
      ),
    )

    assertEquals(
      listOf("surface_nearby", "surface_home_station", "surface_work_station", "surface_favorites"),
      shortcuts.map { it.id },
    )
    assertTrue(shortcuts.any { it.uri == "biciradar://station/station-home" })
    assertTrue(shortcuts.any { it.uri == "biciradar://station/station-work" })
  }

  @Test
  fun `dynamic shortcuts prioritize places and monitoring within launcher limit`() {
    val shortcuts = dynamicShortcutSpecs(
      snapshot = snapshot(
        favoriteStation = station(id = "station-favorite", name = "Plaza Espana"),
        homeStation = station(id = "station-home", name = "Puerta del Carmen"),
        workStation = station(id = "station-work", name = "Plaza Aragon"),
      ),
      maxShortcutCount = 5,
    )

    assertEquals(
      listOf(
        "surface_nearby",
        "surface_home_station",
        "surface_work_station",
        "surface_favorite_station",
        "surface_monitor_favorite",
      ),
      shortcuts.map { it.id },
    )
    assertTrue(shortcuts.none { it.id == "surface_favorites" })
  }

  @Test
  fun `dynamic shortcuts avoid duplicate open-station entries when favorite matches a saved place`() {
    val shortcuts = dynamicShortcutSpecs(
      snapshot = snapshot(
        favoriteStation = station(id = "station-home", name = "Puerta del Carmen"),
        homeStation = station(id = "station-home", name = "Puerta del Carmen"),
        workStation = station(id = "station-work", name = "Plaza Aragon"),
      ),
    )

    assertEquals(
      listOf(
        "surface_nearby",
        "surface_home_station",
        "surface_work_station",
        "surface_monitor_favorite",
        "surface_favorites",
      ),
      shortcuts.map { it.id },
    )
    assertEquals(
      1,
      shortcuts.count { it.uri == "biciradar://station/station-home" },
    )
  }

  private fun snapshot(
    favoriteStation: SurfaceStationSnapshot? = null,
    homeStation: SurfaceStationSnapshot? = null,
    workStation: SurfaceStationSnapshot? = null,
  ): SurfaceSnapshotBundle {
    return SurfaceSnapshotBundle(
      generatedAtEpoch = 1L,
      favoriteStation = favoriteStation,
      homeStation = homeStation,
      workStation = workStation,
      state = SurfaceState(
        hasLocationPermission = true,
        hasNotificationPermission = true,
        hasFavoriteStation = favoriteStation != null,
        isDataFresh = true,
        cityId = "zaragoza",
        cityName = "Zaragoza",
      ),
    )
  }

  private fun station(id: String, name: String): SurfaceStationSnapshot {
    return SurfaceStationSnapshot(
      id = id,
      nameShort = name,
      nameFull = name,
      cityId = "zaragoza",
      latitude = 41.65,
      longitude = -0.88,
      bikesAvailable = 6,
      docksAvailable = 5,
      statusTextShort = "Disponible",
      statusLevel = SurfaceStatusLevel.Good,
      lastUpdatedEpoch = 1L,
      isFavorite = true,
    )
  }
}
