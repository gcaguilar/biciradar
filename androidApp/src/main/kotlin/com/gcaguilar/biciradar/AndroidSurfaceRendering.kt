package com.gcaguilar.biciradar

import android.app.Application
import android.content.Context
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.SurfaceMonitoringStatus
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle
import com.gcaguilar.biciradar.core.SurfaceStationSnapshot
import com.gcaguilar.biciradar.core.formatDistance
import kotlinx.coroutines.runBlocking

internal data class AndroidSurfaceWidgetSnapshot(
  val favoriteStation: AndroidSurfaceFavoriteStation? = null,
  val homeStation: AndroidSurfaceSavedPlaceStation? = null,
  val workStation: AndroidSurfaceSavedPlaceStation? = null,
  val nearbyStations: List<AndroidSurfaceNearbyStation> = emptyList(),
  val hasFavoriteStation: Boolean? = null,
  val isDataFresh: Boolean? = null,
  val hasLocationPermission: Boolean? = null,
  val hasNotificationPermission: Boolean? = null,
)

internal data class AndroidSurfaceFavoriteStation(
  val id: String,
  val name: String,
  val bikesAvailable: Int,
  val docksAvailable: Int,
  val statusText: String,
  val lastUpdatedEpoch: Long?,
)

internal data class AndroidSurfaceNearbyStation(
  val id: String,
  val name: String,
  val bikesAvailable: Int,
  val docksAvailable: Int,
  val distanceMeters: Int?,
  val statusText: String,
)

internal data class AndroidSurfaceSavedPlaceStation(
  val id: String,
  val name: String,
  val bikesAvailable: Int,
  val docksAvailable: Int,
  val statusText: String,
)

internal object AndroidSurfaceSnapshotReader {
  fun read(context: Context): AndroidSurfaceWidgetSnapshot =
    runCatching {
      if (!BiziAppGraph.isInitialized()) {
        BiziAppGraph.initialize(context.applicationContext as Application)
      }
      val bundle =
        runBlocking {
          BiziAppGraph.graph.getCachedStationSnapshot.execute()
        }
      bundle?.toAndroidSurfaceWidgetSnapshot() ?: AndroidSurfaceWidgetSnapshot(isDataFresh = false)
    }.getOrElse {
      AndroidSurfaceWidgetSnapshot()
    }
}

internal fun SurfaceSnapshotBundle.toAndroidSurfaceWidgetSnapshot(): AndroidSurfaceWidgetSnapshot =
  AndroidSurfaceWidgetSnapshot(
    favoriteStation = favoriteStation?.toAndroidFavoriteStation(),
    homeStation = homeStation?.toAndroidSavedPlaceStation(),
    workStation = workStation?.toAndroidSavedPlaceStation(),
    nearbyStations = nearbyStations.map(SurfaceStationSnapshot::toAndroidNearbyStation),
    hasFavoriteStation = state.hasFavoriteStation,
    isDataFresh = state.isDataFresh,
    hasLocationPermission = state.hasLocationPermission,
    hasNotificationPermission = state.hasNotificationPermission,
  )

private fun SurfaceStationSnapshot.toAndroidFavoriteStation(): AndroidSurfaceFavoriteStation =
  AndroidSurfaceFavoriteStation(
    id = id,
    name = nameShort.ifBlank { nameFull },
    bikesAvailable = bikesAvailable,
    docksAvailable = docksAvailable,
    statusText = statusTextShort,
    lastUpdatedEpoch = lastUpdatedEpoch.takeIf { it > 0L },
  )

private fun SurfaceStationSnapshot.toAndroidNearbyStation(): AndroidSurfaceNearbyStation =
  AndroidSurfaceNearbyStation(
    id = id,
    name = nameShort.ifBlank { nameFull },
    bikesAvailable = bikesAvailable,
    docksAvailable = docksAvailable,
    distanceMeters = distanceMeters?.takeIf { it > 0 },
    statusText = statusTextShort,
  )

private fun SurfaceStationSnapshot.toAndroidSavedPlaceStation(): AndroidSurfaceSavedPlaceStation =
  AndroidSurfaceSavedPlaceStation(
    id = id,
    name = nameShort.ifBlank { nameFull },
    bikesAvailable = bikesAvailable,
    docksAvailable = docksAvailable,
    statusText = statusTextShort,
  )

internal data class AndroidAllFavoritesWidgetSnapshot(
  val stations: List<AndroidSurfaceNearbyStation> = emptyList(),
  val hasLocationPermission: Boolean? = null,
  val isDataFresh: Boolean? = null,
)

internal object AndroidAllFavoritesSnapshotReader {
  fun read(context: Context): AndroidAllFavoritesWidgetSnapshot =
    runCatching {
      if (!BiziAppGraph.isInitialized()) {
        BiziAppGraph.initialize(context.applicationContext as Application)
      }
      val getFavoriteStations = BiziAppGraph.graph.getFavoriteStations
      val bundle = runBlocking { BiziAppGraph.graph.getCachedStationSnapshot.execute() }
      val favoriteStations = runBlocking { getFavoriteStations.execute() }
      AndroidAllFavoritesWidgetSnapshot(
        stations = favoriteStations.map { it.toAndroidNearbyStation() },
        hasLocationPermission = bundle?.state?.hasLocationPermission,
        isDataFresh = bundle?.state?.isDataFresh,
      )
    }.getOrElse {
      AndroidAllFavoritesWidgetSnapshot()
    }
}

internal enum class AndroidWidgetEmptyState {
  ConfigureFavorite,
  NoLocationPermission,
  OpenAppToRefresh,
  DataUnavailable,
}

internal data class AndroidQuickActionsState(
  val monitorUri: String,
  val requiresConfiguration: Boolean,
  val requiresNotificationPermission: Boolean,
)

internal data class AndroidCommutePlaceState(
  val label: String,
  val title: String,
  val meta: String,
  val stationId: String? = null,
)

internal fun widgetEmptyState(snapshot: AndroidSurfaceWidgetSnapshot): AndroidWidgetEmptyState =
  when {
    snapshot.favoriteStation == null && snapshot.hasFavoriteStation == false ->
      AndroidWidgetEmptyState.ConfigureFavorite
    snapshot.isDataFresh == false -> AndroidWidgetEmptyState.OpenAppToRefresh
    else -> AndroidWidgetEmptyState.DataUnavailable
  }

internal fun nearbyWidgetEmptyState(snapshot: AndroidSurfaceWidgetSnapshot): AndroidWidgetEmptyState =
  when {
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
): String =
  when (state) {
    AndroidWidgetEmptyState.ConfigureFavorite -> configureFavorite
    AndroidWidgetEmptyState.NoLocationPermission -> noLocationPermission
    AndroidWidgetEmptyState.OpenAppToRefresh -> openAppToRefresh
    AndroidWidgetEmptyState.DataUnavailable -> dataUnavailable
  }

internal fun nearbyStationMeta(station: AndroidSurfaceNearbyStation): String =
  buildString {
    append("${station.bikesAvailable} bicis · ${station.docksAvailable} huecos")
    station.distanceMeters?.let { append(" · ${formatDistance(it)}") }
  }

internal fun savedPlaceMeta(station: AndroidSurfaceSavedPlaceStation): String =
  buildString {
    append("${station.bikesAvailable} bicis · ${station.docksAvailable} huecos")
    if (station.statusText.isNotBlank()) {
      append(" · ${station.statusText}")
    }
  }

internal fun commutePlaceState(
  label: String,
  station: AndroidSurfaceSavedPlaceStation?,
  snapshot: AndroidSurfaceWidgetSnapshot,
  configureSavedPlaces: String,
  openAppToRefresh: String,
  missingTitle: String,
): AndroidCommutePlaceState =
  if (station != null) {
    AndroidCommutePlaceState(
      label = label,
      title = station.name,
      meta = savedPlaceMeta(station),
      stationId = station.id,
    )
  } else {
    AndroidCommutePlaceState(
      label = label,
      title = missingTitle,
      meta = if (snapshot.isDataFresh == false) openAppToRefresh else configureSavedPlaces,
    )
  }

internal fun quickActionsState(snapshot: AndroidSurfaceWidgetSnapshot): AndroidQuickActionsState {
  val favoriteStationId = snapshot.favoriteStation?.id
  return if (favoriteStationId != null) {
    AndroidQuickActionsState(
      monitorUri = "biciradar://monitor/$favoriteStationId",
      requiresConfiguration = false,
      requiresNotificationPermission = snapshot.hasNotificationPermission == false,
    )
  } else {
    AndroidQuickActionsState(
      monitorUri = "biciradar://favorites",
      requiresConfiguration = true,
      requiresNotificationPermission = false,
    )
  }
}

internal fun monitoringNotificationTitle(session: SurfaceMonitoringSession): String =
  when (session.status) {
    SurfaceMonitoringStatus.Monitoring -> session.stationName
    SurfaceMonitoringStatus.ChangedToEmpty -> "${session.stationName} sin bicis"
    SurfaceMonitoringStatus.ChangedToFull -> "${session.stationName} sin huecos"
    SurfaceMonitoringStatus.AlternativeAvailable -> "Alternativa para ${session.stationName}"
    SurfaceMonitoringStatus.Ended -> "Monitorizacion detenida"
    SurfaceMonitoringStatus.Expired -> "Monitorizacion finalizada"
  }

internal fun monitoringNotificationBody(
  session: SurfaceMonitoringSession,
  remainingSeconds: Int,
): String =
  when (session.status) {
    SurfaceMonitoringStatus.Ended -> "${session.stationName} · Finalizada por el usuario"
    SurfaceMonitoringStatus.Expired -> "${session.stationName} · Tiempo agotado"
    else ->
      listOfNotNull(
        monitoringNotificationStatusText(session),
        "${session.bikesAvailable} bicis",
        "${session.docksAvailable} huecos",
        monitoringNotificationTimeText(remainingSeconds),
        monitoringNotificationAlternativeText(session),
      ).joinToString(" · ")
  }

private fun monitoringNotificationStatusText(session: SurfaceMonitoringSession): String =
  when (session.status) {
    SurfaceMonitoringStatus.Monitoring -> "Monitorizando"
    SurfaceMonitoringStatus.ChangedToEmpty -> "Sin bicis"
    SurfaceMonitoringStatus.ChangedToFull -> "Sin huecos"
    SurfaceMonitoringStatus.AlternativeAvailable -> "Alternativa sugerida"
    SurfaceMonitoringStatus.Ended -> "Finalizada"
    SurfaceMonitoringStatus.Expired -> "Finalizada"
  }

private fun monitoringNotificationTimeText(remainingSeconds: Int): String {
  val minutes = remainingSeconds / 60
  val seconds = remainingSeconds % 60
  return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
}

private fun monitoringNotificationAlternativeText(session: SurfaceMonitoringSession): String? {
  val alternativeName = session.alternativeStationName ?: return null
  val distance = session.alternativeDistanceMeters?.let { " (${formatDistance(it)})" }.orEmpty()
  return "Alt: $alternativeName$distance"
}
