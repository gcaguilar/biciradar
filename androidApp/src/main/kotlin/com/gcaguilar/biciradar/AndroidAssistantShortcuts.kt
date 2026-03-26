package com.gcaguilar.biciradar

import android.content.Context
import androidx.core.content.pm.ShortcutManagerCompat
import com.gcaguilar.biciradar.mobileui.AssistantLaunchRequest
import com.gcaguilar.biciradar.mobileui.MobileLaunchRequest

internal object AndroidAssistantShortcuts {

  fun reportUsed(
    context: Context,
    launchRequest: MobileLaunchRequest?,
    assistantLaunchRequest: AssistantLaunchRequest?,
  ) {
    val shortcutId = shortcutIdFor(launchRequest, assistantLaunchRequest) ?: return
    ShortcutManagerCompat.reportShortcutUsed(context, shortcutId)
  }
}

internal fun shortcutIdFor(
  launchRequest: MobileLaunchRequest?,
  assistantLaunchRequest: AssistantLaunchRequest?,
): String? {
  return when {
    assistantLaunchRequest is AssistantLaunchRequest.StationStatus -> STATION_STATUS_ACTION
    assistantLaunchRequest is AssistantLaunchRequest.StationBikeCount -> STATION_BIKE_COUNT_ACTION
    assistantLaunchRequest is AssistantLaunchRequest.StationSlotCount -> STATION_SLOT_COUNT_ACTION
    assistantLaunchRequest is AssistantLaunchRequest.RouteToStation -> ROUTE_TO_STATION_ACTION
    assistantLaunchRequest is AssistantLaunchRequest.SearchStation -> SHOW_STATION_ACTION
    else -> when (launchRequest) {
      MobileLaunchRequest.Home -> HOME_ACTION
      MobileLaunchRequest.Map -> MAP_ACTION
      MobileLaunchRequest.Favorites -> FAVORITE_STATIONS_ACTION
      MobileLaunchRequest.NearestStation -> NEAREST_STATION_ACTION
      MobileLaunchRequest.NearestStationWithBikes -> NEAREST_STATION_WITH_BIKES_ACTION
      MobileLaunchRequest.NearestStationWithSlots -> NEAREST_STATION_WITH_SLOTS_ACTION
      MobileLaunchRequest.OpenAssistant -> OPEN_ASSISTANT_ACTION
      MobileLaunchRequest.StationStatus -> STATION_STATUS_ACTION
      is MobileLaunchRequest.MonitorStation -> MONITOR_STATION_ACTION
      is MobileLaunchRequest.SelectCity -> SELECT_CITY_ACTION
      is MobileLaunchRequest.RouteToStation -> ROUTE_TO_STATION_ACTION
      is MobileLaunchRequest.ShowStation -> SHOW_STATION_ACTION
      else -> null
    }
  }
}
