package com.gcaguilar.biciradar

import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle

internal data class AndroidDynamicShortcutSpec(
  val id: String,
  val shortLabel: String,
  val longLabel: String,
  val iconRes: Int,
  val uri: String,
)

internal fun dynamicShortcutSpecs(snapshot: SurfaceSnapshotBundle?): List<AndroidDynamicShortcutSpec> {
  val favorite = snapshot?.favoriteStation
  return buildList {
    add(
      AndroidDynamicShortcutSpec(
        id = "surface_nearby",
        shortLabel = "Cerca",
        longLabel = "Ver estaciones cercanas",
        iconRes = android.R.drawable.ic_menu_mylocation,
        uri = "biciradar://home",
      ),
    )
    add(
      AndroidDynamicShortcutSpec(
        id = "surface_favorites",
        shortLabel = "Favoritas",
        longLabel = "Abrir favoritas",
        iconRes = android.R.drawable.ic_menu_agenda,
        uri = "biciradar://favorites",
      ),
    )
    if (favorite != null) {
      add(
        AndroidDynamicShortcutSpec(
          id = "surface_favorite_station",
          shortLabel = "Favorita",
          longLabel = favorite.nameFull,
          iconRes = android.R.drawable.ic_menu_compass,
          uri = "biciradar://station/${favorite.id}",
        ),
      )
      add(
        AndroidDynamicShortcutSpec(
          id = "surface_monitor_favorite",
          shortLabel = "Monitorizar",
          longLabel = "Monitorizar ${favorite.nameShort}",
          iconRes = android.R.drawable.ic_menu_recent_history,
          uri = "biciradar://monitor/${favorite.id}",
        ),
      )
    }
  }
}
