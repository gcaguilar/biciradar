package com.gcaguilar.biciradar

import com.gcaguilar.biciradar.core.SurfaceMonitoringSession

internal enum class AndroidWidgetEmptyState {
  ConfigureFavorite,
  NoLocationPermission,
  OpenAppToRefresh,
  DataUnavailable,
}

internal data class AndroidQuickActionsState(
  val monitorUri: String,
  val requiresConfiguration: Boolean,
)

internal fun widgetEmptyState(snapshot: AndroidSurfaceWidgetSnapshot): AndroidWidgetEmptyState = when {
  snapshot.favoriteStation == null && snapshot.hasFavoriteStation == false -> AndroidWidgetEmptyState.ConfigureFavorite
  snapshot.isDataFresh == false -> AndroidWidgetEmptyState.OpenAppToRefresh
  else -> AndroidWidgetEmptyState.DataUnavailable
}

internal fun nearbyWidgetEmptyState(snapshot: AndroidSurfaceWidgetSnapshot): AndroidWidgetEmptyState = when {
  snapshot.hasLocationPermission == false -> AndroidWidgetEmptyState.NoLocationPermission
  snapshot.isDataFresh == false -> AndroidWidgetEmptyState.OpenAppToRefresh
  else -> AndroidWidgetEmptyState.DataUnavailable
}

internal fun widgetEmptyMessage(
  state: AndroidWidgetEmptyState,
  configureFavorite: String,
  noLocationPermission: String,
  openAppToRefresh: String,
  dataUnavailable: String,
): String = when (state) {
  AndroidWidgetEmptyState.ConfigureFavorite -> configureFavorite
  AndroidWidgetEmptyState.NoLocationPermission -> noLocationPermission
  AndroidWidgetEmptyState.OpenAppToRefresh -> openAppToRefresh
  AndroidWidgetEmptyState.DataUnavailable -> dataUnavailable
}

internal fun nearbyStationMeta(station: AndroidSurfaceNearbyStation): String = buildString {
  append("${station.bikesAvailable} bicis · ${station.docksAvailable} huecos")
  station.distanceMeters?.let { append(" · ${it} m") }
}

internal fun quickActionsState(snapshot: AndroidSurfaceWidgetSnapshot): AndroidQuickActionsState {
  val favoriteStationId = snapshot.favoriteStation?.id
  return if (favoriteStationId != null) {
    AndroidQuickActionsState(
      monitorUri = "biciradar://monitor/$favoriteStationId",
      requiresConfiguration = false,
    )
  } else {
    AndroidQuickActionsState(
      monitorUri = "biciradar://favorites",
      requiresConfiguration = true,
    )
  }
}

internal fun monitoringNotificationBody(
  session: SurfaceMonitoringSession,
  remainingSeconds: Int,
): String {
  val minutes = remainingSeconds / 60
  val seconds = remainingSeconds % 60
  val timeText = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
  val base = "${session.bikesAvailable} bicis · ${session.docksAvailable} huecos · $timeText"
  return session.alternativeStationName?.let { alternativeName ->
    "$base · Alt: $alternativeName"
  } ?: base
}
