package com.gcaguilar.biciradar.wear

import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.SurfaceMonitoringStatus
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle
import com.gcaguilar.biciradar.core.SurfaceStatusLevel
import com.gcaguilar.biciradar.core.formatRelativeMinutes
import com.gcaguilar.biciradar.core.surfaceStatusLevel
import com.gcaguilar.biciradar.core.surfaceStatusTextShort

internal fun sortWearFavoriteStations(
  stations: List<Station>,
  homeStationId: String?,
  workStationId: String?,
): List<Station> = stations.sortedWith(
  compareByDescending<Station> { it.id == homeStationId }
    .thenByDescending { it.id == workStationId }
    .thenBy { it.distanceMeters }
    .thenBy { it.name },
)

internal fun wearSavedPlaceLabel(
  stationId: String,
  homeStationId: String?,
  workStationId: String?,
): String? = when (stationId) {
  homeStationId -> "Casa"
  workStationId -> "Trabajo"
  else -> null
}

internal enum class WearFavoriteSurfaceKind {
  Favorite,
  ConfigureFavorite,
  OpenAppToRefresh,
  DataUnavailable,
}

internal data class WearFavoriteSurfaceState(
  val kind: WearFavoriteSurfaceKind,
  val title: String,
  val supportingText: String,
  val stationId: String? = null,
  val statusText: String? = null,
  val statusLevel: SurfaceStatusLevel? = null,
  val updatedText: String? = null,
  val bikesLabel: String? = null,
  val docksLabel: String? = null,
)

internal data class WearFavoriteTileState(
  val title: String,
  val body: String,
  val label: String? = null,
  val updatedText: String? = null,
  val stationId: String? = null,
)

internal data class WearMonitoringSurfaceState(
  val stationId: String,
  val title: String,
  val statusText: String,
  val statusLevel: SurfaceStatusLevel,
  val countdownText: String,
  val bikesLabel: String,
  val docksLabel: String,
  val alternativeText: String? = null,
)

internal data class WearSavedPlaceSurfaceState(
  val stationId: String,
  val label: String,
  val title: String,
  val statusText: String,
  val statusLevel: SurfaceStatusLevel,
  val bikesLabel: String,
  val docksLabel: String,
)

internal fun wearFavoriteSurfaceState(
  snapshot: SurfaceSnapshotBundle?,
  nowEpoch: Long = System.currentTimeMillis(),
): WearFavoriteSurfaceState {
  val favorite = snapshot?.favoriteStation
  if (favorite != null) {
    return WearFavoriteSurfaceState(
      kind = WearFavoriteSurfaceKind.Favorite,
      title = favorite.nameShort,
      supportingText = favorite.nameFull,
      stationId = favorite.id,
      statusText = favorite.statusTextShort,
      statusLevel = favorite.statusLevel,
      updatedText = formatRelativeMinutes(favorite.lastUpdatedEpoch, nowEpoch),
      bikesLabel = "🚲 ${favorite.bikesAvailable}",
      docksLabel = "🅿 ${favorite.docksAvailable}",
    )
  }
  return when {
    snapshot?.state?.hasFavoriteStation == false -> WearFavoriteSurfaceState(
      kind = WearFavoriteSurfaceKind.ConfigureFavorite,
      title = "Sin favorita",
      supportingText = "Configúrala en la app",
    )

    snapshot?.state?.isDataFresh == false -> WearFavoriteSurfaceState(
      kind = WearFavoriteSurfaceKind.OpenAppToRefresh,
      title = "Datos stale",
      supportingText = "Abre la app para actualizar",
    )

    else -> WearFavoriteSurfaceState(
      kind = WearFavoriteSurfaceKind.DataUnavailable,
      title = "Datos no disponibles",
      supportingText = snapshot?.state?.cityName ?: "BiciRadar",
    )
  }
}

internal fun wearFavoriteTileState(
  snapshot: SurfaceSnapshotBundle?,
  nowEpoch: Long = System.currentTimeMillis(),
): WearFavoriteTileState {
  val surface = wearFavoriteSurfaceState(snapshot, nowEpoch)
  return when (surface.kind) {
    WearFavoriteSurfaceKind.Favorite -> {
      val favorite = snapshot?.favoriteStation
      val metrics = listOfNotNull(surface.bikesLabel, surface.docksLabel).joinToString("  ")
      val alternative = favorite?.alternativeStationName?.let { name ->
        val distance = favorite.alternativeDistanceMeters?.let { " (${it} m)" }.orEmpty()
        "Alt: $name$distance"
      }
      WearFavoriteTileState(
        title = surface.title,
        body = listOfNotNull(metrics.takeIf { it.isNotBlank() }, alternative).joinToString(" · "),
        label = surface.statusText,
        updatedText = surface.updatedText,
        stationId = surface.stationId,
      )
    }

    WearFavoriteSurfaceKind.ConfigureFavorite,
    WearFavoriteSurfaceKind.OpenAppToRefresh,
    WearFavoriteSurfaceKind.DataUnavailable -> WearFavoriteTileState(
      title = surface.title,
      body = surface.supportingText,
      label = snapshot?.state?.cityName,
      stationId = surface.stationId,
    )
  }
}

internal fun wearMonitoringSurfaceState(
  session: SurfaceMonitoringSession,
  remainingSeconds: Int,
): WearMonitoringSurfaceState = WearMonitoringSurfaceState(
  stationId = session.stationId,
  title = session.stationName,
  statusText = wearMonitoringStatusText(session.status, session.kind),
  statusLevel = session.statusLevel,
  countdownText = wearMonitoringCountdownText(remainingSeconds),
  bikesLabel = "🚲 ${session.bikesAvailable}",
  docksLabel = "🅿 ${session.docksAvailable}",
  alternativeText = session.alternativeStationName?.let { name ->
    val distance = session.alternativeDistanceMeters?.let { " (${it} m)" }.orEmpty()
    "Alt: $name$distance"
  },
)

internal fun wearSavedPlaceSurfaceStates(
  stations: List<Station>,
  homeStationId: String?,
  workStationId: String?,
): List<WearSavedPlaceSurfaceState> = buildList {
  homeStationId
    ?.let { stationId -> stations.firstOrNull { it.id == stationId } }
    ?.let { station ->
      add(station.toWearSavedPlaceSurfaceState(label = "Casa"))
    }
  workStationId
    ?.takeIf { it != homeStationId }
    ?.let { stationId -> stations.firstOrNull { it.id == stationId } }
    ?.let { station ->
      add(station.toWearSavedPlaceSurfaceState(label = "Trabajo"))
    }
}

private fun wearMonitoringStatusText(
  status: SurfaceMonitoringStatus,
  kind: SurfaceMonitoringKind,
): String = when (status) {
  SurfaceMonitoringStatus.Monitoring -> if (kind == SurfaceMonitoringKind.Docks) "Monitorizando huecos" else "Monitorizando bicis"
  SurfaceMonitoringStatus.ChangedToEmpty -> "Sin bicis"
  SurfaceMonitoringStatus.ChangedToFull -> "Sin huecos"
  SurfaceMonitoringStatus.AlternativeAvailable -> "Alternativa sugerida"
  SurfaceMonitoringStatus.Ended -> "Finalizada"
  SurfaceMonitoringStatus.Expired -> "Expirada"
}

private fun wearMonitoringCountdownText(remainingSeconds: Int): String {
  val minutes = remainingSeconds / 60
  val seconds = remainingSeconds % 60
  return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
}

private fun Station.toWearSavedPlaceSurfaceState(label: String): WearSavedPlaceSurfaceState =
  WearSavedPlaceSurfaceState(
    stationId = id,
    label = label,
    title = name,
    statusText = surfaceStatusTextShort(),
    statusLevel = surfaceStatusLevel(),
    bikesLabel = "🚲 $bikesAvailable",
    docksLabel = "🅿 $slotsFree",
  )
