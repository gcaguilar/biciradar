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
          shortcutId = NEAREST_STATION_ACTION,
          shortLabel = R.string.shortcut_nearest_station,
          iconRes = android.R.drawable.ic_menu_mylocation,
          action = NEAREST_STATION_ACTION,
        ),
        shortcut(
          context = context,
          shortcutId = FAVORITE_STATIONS_ACTION,
          shortLabel = R.string.shortcut_favorites,
          iconRes = android.R.drawable.ic_menu_agenda,
          action = FAVORITE_STATIONS_ACTION,
        ),
        shortcut(
          context = context,
          shortcutId = STATION_STATUS_ACTION,
          shortLabel = R.string.shortcut_station_status,
          iconRes = android.R.drawable.ic_dialog_info,
          action = STATION_STATUS_ACTION,
        ),
        shortcut(
          context = context,
          shortcutId = ROUTE_TO_STATION_ACTION,
          shortLabel = R.string.shortcut_route,
          iconRes = android.R.drawable.ic_dialog_map,
          action = ROUTE_TO_STATION_ACTION,
        ),
      ),
    )
  }

  fun reportUsed(context: Context, launchRequest: MobileLaunchRequest?) {
    val shortcutId = when (launchRequest) {
      MobileLaunchRequest.Favorites -> FAVORITE_STATIONS_ACTION
      MobileLaunchRequest.NearestStation -> NEAREST_STATION_ACTION
      MobileLaunchRequest.OpenAssistant -> OPEN_ASSISTANT_ACTION
      MobileLaunchRequest.StationStatus -> STATION_STATUS_ACTION
      is MobileLaunchRequest.RouteToStation -> ROUTE_TO_STATION_ACTION
      is MobileLaunchRequest.ShowStation -> SHOW_STATION_ACTION
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
        data = Uri.parse("bizi://assistant?action=$action&feature=$action")
        putExtra(ASSISTANT_ACTION_EXTRA, action)
        putExtra(FEATURE_EXTRA, action)
      },
    )
    .setLongLived(true)
    .build()
}
