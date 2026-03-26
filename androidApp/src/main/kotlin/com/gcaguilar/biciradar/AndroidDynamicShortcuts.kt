package com.gcaguilar.biciradar

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle

internal object AndroidDynamicShortcuts {
  fun publish(context: Context, snapshot: SurfaceSnapshotBundle?) {
    val shortcuts = buildList {
      add(
        shortcut(
          context = context,
          id = "surface_map",
          shortLabel = "Mapa",
          longLabel = "Abrir mapa",
          iconRes = android.R.drawable.ic_dialog_map,
          uri = Uri.parse("biciradar://map"),
        ),
      )

      val favorite = snapshot?.favoriteStation
      if (favorite != null) {
        add(
          shortcut(
            context = context,
            id = "surface_favorite_station",
            shortLabel = "Favorita",
            longLabel = favorite.nameFull,
            iconRes = android.R.drawable.ic_menu_mylocation,
            uri = Uri.parse("biciradar://station/${favorite.id}"),
          ),
        )
        add(
          shortcut(
            context = context,
            id = "surface_monitor_favorite",
            shortLabel = "Monitorizar",
            longLabel = "Monitorizar ${favorite.nameShort}",
            iconRes = android.R.drawable.ic_menu_recent_history,
            uri = Uri.parse("biciradar://monitor/${favorite.id}"),
          ),
        )
      }
    }
    ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
  }

  private fun shortcut(
    context: Context,
    id: String,
    shortLabel: String,
    longLabel: String,
    iconRes: Int,
    uri: Uri,
  ): ShortcutInfoCompat {
    val intent = Intent(Intent.ACTION_VIEW, uri, context, MainActivity::class.java)
    return ShortcutInfoCompat.Builder(context, id)
      .setShortLabel(shortLabel)
      .setLongLabel(longLabel)
      .setIcon(IconCompat.createWithResource(context, iconRes))
      .setIntent(intent)
      .build()
  }
}
