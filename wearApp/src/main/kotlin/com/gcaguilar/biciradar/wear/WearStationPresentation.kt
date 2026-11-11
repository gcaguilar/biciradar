package com.gcaguilar.biciradar.wear

import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle
import com.gcaguilar.biciradar.core.SurfaceStationSnapshot
import com.gcaguilar.biciradar.core.SurfaceStatusLevel
import com.gcaguilar.biciradar.core.formatDistance
import com.gcaguilar.biciradar.core.formatMonitoringCountdown
import com.gcaguilar.biciradar.core.surfaceStatusLevel

internal fun sortWearFavoriteStations(
  stations: List<Station>,
  homeStationId: String?,
  workStationId: String?,
): List<Station> =
  stations.sortedWith(
    compareByDescending<Station> { it.id == homeStationId }
      .thenByDescending { it.id == workStationId }
      .thenBy { it.distanceMeters }
      .thenBy { it.name },
  )

internal fun wearSavedPlaceLabel(
  text: WearText,
  stationId: String,
  homeStationId: String?,
  workStationId: String?,
): String? =
  when (stationId) {
    homeStationId -> text.get(R.string.wear_place_home)
    workStationId -> text.get(R.string.wear_place_work)
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
  val bikesAvailable: Int = 0,
  val docksAvailable: Int = 0,
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

/**
 * [fallbackStation] se usa cuando el usuario aún no tiene favorita (p. ej. un reloj sin
 * móvil emparejado): así tiles y complications muestran datos reales de la estación
 * más cercana en lugar de un estado vacío.
 */
internal fun wearFavoriteSurfaceState(
  text: WearText,
  snapshot: SurfaceSnapshotBundle?,
  nowEpoch: Long = System.currentTimeMillis(),
  fallbackStation: SurfaceStationSnapshot? = null,
): WearFavoriteSurfaceState {
  val favorite = snapshot?.favoriteStation ?: fallbackStation
  if (favorite != null) {
    return WearFavoriteSurfaceState(
      kind = WearFavoriteSurfaceKind.Favorite,
      title = favorite.nameShort,
      supportingText = favorite.nameFull,
      stationId = favorite.id,
      statusText = text.statusLabel(favorite.statusLevel),
      statusLevel = favorite.statusLevel,
      updatedText = text.relativeMinutes(favorite.lastUpdatedEpoch, nowEpoch),
      bikesLabel = "🚲 ${favorite.bikesAvailable}",
      docksLabel = "🅿 ${favorite.docksAvailable}",
      bikesAvailable = favorite.bikesAvailable,
      docksAvailable = favorite.docksAvailable,
    )
  }
  return when {
    snapshot?.state?.hasFavoriteStation == false -> {
      WearFavoriteSurfaceState(
        kind = WearFavoriteSurfaceKind.ConfigureFavorite,
        title = text.get(R.string.wear_no_favorite_title),
        supportingText = text.get(R.string.wear_no_favorite_hint),
      )
    }

    snapshot?.state?.isDataFresh == false -> {
      WearFavoriteSurfaceState(
        kind = WearFavoriteSurfaceKind.OpenAppToRefresh,
        title = text.get(R.string.wear_refresh_title),
        supportingText = text.get(R.string.wear_refresh_hint),
      )
    }

    else -> {
      WearFavoriteSurfaceState(
        kind = WearFavoriteSurfaceKind.DataUnavailable,
        title = text.get(R.string.wear_data_unavailable),
        supportingText = snapshot?.state?.cityName ?: "BiciRadar",
      )
    }
  }
}

internal fun wearFavoriteTileState(
  text: WearText,
  snapshot: SurfaceSnapshotBundle?,
  nowEpoch: Long = System.currentTimeMillis(),
  fallbackStation: SurfaceStationSnapshot? = null,
): WearFavoriteTileState {
  val surface = wearFavoriteSurfaceState(text, snapshot, nowEpoch, fallbackStation)
  return when (surface.kind) {
    WearFavoriteSurfaceKind.Favorite -> {
      val favorite = snapshot?.favoriteStation ?: fallbackStation
      val metrics = listOfNotNull(surface.bikesLabel, surface.docksLabel).joinToString("  ")
      val alternative =
        favorite?.alternativeStationName?.let { name ->
          text.alternativeLabel(name, favorite.alternativeDistanceMeters?.let(::formatDistance))
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
    WearFavoriteSurfaceKind.DataUnavailable,
    -> {
      WearFavoriteTileState(
        title = surface.title,
        body = surface.supportingText,
        label = snapshot?.state?.cityName,
        stationId = surface.stationId,
      )
    }
  }
}

internal fun wearMonitoringSurfaceState(
  text: WearText,
  session: SurfaceMonitoringSession,
  remainingSeconds: Int,
): WearMonitoringSurfaceState =
  WearMonitoringSurfaceState(
    stationId = session.stationId,
    title = session.stationName,
    statusText = text.monitoringStatusLabel(session.status, session.kind),
    statusLevel = session.statusLevel,
    countdownText = wearMonitoringCountdownText(remainingSeconds),
    bikesLabel = "🚲 ${session.bikesAvailable}",
    docksLabel = "🅿 ${session.docksAvailable}",
    alternativeText =
      session.alternativeStationName?.let { name ->
        text.alternativeLabel(name, session.alternativeDistanceMeters?.let(::formatDistance))
      },
  )

internal fun wearSavedPlaceSurfaceStates(
  text: WearText,
  stations: List<Station>,
  homeStationId: String?,
  workStationId: String?,
): List<WearSavedPlaceSurfaceState> =
  buildList {
    homeStationId
      ?.let { stationId -> stations.firstOrNull { it.id == stationId } }
      ?.let { station ->
        add(station.toWearSavedPlaceSurfaceState(text, label = text.get(R.string.wear_place_home)))
      }
    workStationId
      ?.takeIf { it != homeStationId }
      ?.let { stationId -> stations.firstOrNull { it.id == stationId } }
      ?.let { station ->
        add(station.toWearSavedPlaceSurfaceState(text, label = text.get(R.string.wear_place_work)))
      }
  }

private fun wearMonitoringCountdownText(remainingSeconds: Int): String = formatMonitoringCountdown(remainingSeconds)

private fun Station.toWearSavedPlaceSurfaceState(
  text: WearText,
  label: String,
): WearSavedPlaceSurfaceState =
  WearSavedPlaceSurfaceState(
    stationId = id,
    label = label,
    title = name,
    statusText = text.statusLabel(this),
    statusLevel = surfaceStatusLevel(),
    bikesLabel = "🚲 $bikesAvailable",
    docksLabel = "🅿 $slotsFree",
  )
