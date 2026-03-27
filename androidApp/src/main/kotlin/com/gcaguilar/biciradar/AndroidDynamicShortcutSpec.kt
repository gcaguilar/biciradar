package com.gcaguilar.biciradar

import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle

internal data class AndroidDynamicShortcutSpec(
  val id: String,
  val shortLabel: String,
  val longLabel: String,
  val iconRes: Int,
  val uri: String,
  val rank: Int,
)

internal const val DEFAULT_MAX_DYNAMIC_SHORTCUTS = 5

internal fun dynamicShortcutSpecs(
  snapshot: SurfaceSnapshotBundle?,
  maxShortcutCount: Int = DEFAULT_MAX_DYNAMIC_SHORTCUTS,
): List<AndroidDynamicShortcutSpec> {
  val favorite = snapshot?.favoriteStation
  val home = snapshot?.homeStation
  val work = snapshot?.workStation
  val prioritized = buildList {
    val seenStationUris = mutableSetOf<String>()

    fun addShortcut(
      id: String,
      shortLabel: String,
      longLabel: String,
      iconRes: Int,
      uri: String,
    ) {
      add(
        AndroidDynamicShortcutSpec(
          id = id,
          shortLabel = shortLabel,
          longLabel = longLabel,
          iconRes = iconRes,
          uri = uri,
          rank = size,
        ),
      )
    }

    fun addStationShortcut(
      id: String,
      shortLabel: String,
      longLabel: String,
      iconRes: Int,
      stationId: String?,
    ) {
      val resolvedStationId = stationId ?: return
      val uri = "biciradar://station/$resolvedStationId"
      if (!seenStationUris.add(uri)) return
      addShortcut(
        id = id,
        shortLabel = shortLabel,
        longLabel = longLabel,
        iconRes = iconRes,
        uri = uri,
      )
    }

    addShortcut(
      id = "surface_nearby",
      shortLabel = "Cerca",
      longLabel = "Ver estaciones cercanas",
      iconRes = android.R.drawable.ic_menu_mylocation,
      uri = "biciradar://home",
    )
    addStationShortcut(
      id = "surface_home_station",
      shortLabel = "Casa",
      longLabel = home?.nameFull?.let { "Casa: $it" } ?: "",
      iconRes = android.R.drawable.ic_menu_myplaces,
      stationId = home?.id,
    )
    addStationShortcut(
      id = "surface_work_station",
      shortLabel = "Trabajo",
      longLabel = work?.nameFull?.let { "Trabajo: $it" } ?: "",
      iconRes = android.R.drawable.ic_menu_manage,
      stationId = work?.id,
    )
    addStationShortcut(
      id = "surface_favorite_station",
      shortLabel = "Favorita",
      longLabel = favorite?.nameFull ?: "",
      iconRes = android.R.drawable.ic_menu_compass,
      stationId = favorite?.id,
    )
    if (favorite != null) {
      addShortcut(
        id = "surface_monitor_favorite",
        shortLabel = "Monitorizar",
        longLabel = "Monitorizar ${favorite.nameShort}",
        iconRes = android.R.drawable.ic_menu_recent_history,
        uri = "biciradar://monitor/${favorite.id}",
      )
    }
    addShortcut(
      id = "surface_favorites",
      shortLabel = "Favoritas",
      longLabel = "Abrir favoritas",
      iconRes = android.R.drawable.ic_menu_agenda,
      uri = "biciradar://favorites",
    )
  }
  return prioritized.take(maxShortcutCount.coerceAtLeast(0)).mapIndexed { index, spec ->
    spec.copy(rank = index)
  }
}
