package com.gcaguilar.bizizaragoza

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.gcaguilar.bizizaragoza.mobileui.MobileLaunchRequest

internal object AndroidAssistantShortcuts {
  fun publish(context: Context) {
    ShortcutManagerCompat.setDynamicShortcuts(
      context,
      listOf(
        shortcut(
          context = context,
          shortcutId = NEAREST_STATION_ID,
          shortLabel = R.string.shortcut_nearest_station,
          iconRes = android.R.drawable.ic_menu_mylocation,
          action = "nearest_station",
        ),
        shortcut(
          context = context,
          shortcutId = FAVORITES_ID,
          shortLabel = R.string.shortcut_favorites,
          iconRes = android.R.drawable.ic_menu_agenda,
          action = "favorite_stations",
        ),
        shortcut(
          context = context,
          shortcutId = STATION_STATUS_ID,
          shortLabel = R.string.shortcut_station_status,
          iconRes = android.R.drawable.ic_dialog_info,
          action = "station_status",
        ),
        shortcut(
          context = context,
          shortcutId = ROUTE_ID,
          shortLabel = R.string.shortcut_route,
          iconRes = android.R.drawable.ic_dialog_map,
          action = "route_to_station",
        ),
      ),
    )
  }

  fun reportUsed(context: Context, launchRequest: MobileLaunchRequest?) {
    val shortcutId = when (launchRequest) {
      MobileLaunchRequest.Favorites -> FAVORITES_ID
      MobileLaunchRequest.NearestStation -> NEAREST_STATION_ID
      MobileLaunchRequest.StationStatus -> STATION_STATUS_ID
      is MobileLaunchRequest.RouteToStation -> ROUTE_ID
      else -> return
    }
    ShortcutManagerCompat.reportShortcutUsed(context, shortcutId)
  }

  private fun shortcut(
    context: Context,
    shortcutId: String,
    shortLabel: Int,
    iconRes: Int,
    action: String,
  ): ShortcutInfoCompat = ShortcutInfoCompat.Builder(context, shortcutId)
    .setShortLabel(context.getString(shortLabel))
    .setLongLabel(context.getString(shortLabel))
    .setIcon(IconCompat.createWithResource(context, iconRes))
    .setIntent(
      Intent(Intent.ACTION_VIEW).apply {
        setClass(context, MainActivity::class.java)
        data = Uri.parse("bizi://assistant?action=$action")
        putExtra("assistant_action", action)
      },
    )
    .setLongLived(true)
    .build()

  private const val NEAREST_STATION_ID = "nearest_station"
  private const val FAVORITES_ID = "favorite_stations"
  private const val STATION_STATUS_ID = "station_status"
  private const val ROUTE_ID = "route_to_station"
}
