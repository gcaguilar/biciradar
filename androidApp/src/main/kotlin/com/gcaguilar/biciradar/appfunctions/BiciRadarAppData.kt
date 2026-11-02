package com.gcaguilar.biciradar.appfunctions

import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationRequirement
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind

/**
 * Data and session operations behind [BiciRadarAppFunctions].
 *
 * The interface is deliberately narrow and free of Android framework types: the
 * service owns Intents and PendingIntents, while this seam owns everything that
 * can be exercised by a plain JVM test. It also keeps repository reads (such as
 * whether the app knows the user's location) out of the service.
 */
internal interface BiciRadarAppData {
  /** Bootstraps the persistent repositories; idempotent. */
  suspend fun bootstrapSession()

  /** Whether the user has already confirmed a city in the app. */
  suspend fun isCityConfigured(): Boolean

  /** The city currently selected by the user. */
  suspend fun currentCity(): City

  /** Whether repository distances are measured from a real user location. */
  fun hasUserLocation(): Boolean

  /** Timestamp of the station availability snapshot, in epoch millis. */
  fun lastUpdatedEpoch(): Long?

  /** Stations matching a free-text query, ranked by relevance. */
  suspend fun searchStations(query: String): List<Station>

  /** Best single station for a free-text query, including pinned aliases. */
  suspend fun findStationMatchingQuery(query: String): Station?

  /** All loaded stations, already ordered by distance from the app location. */
  suspend fun allStations(): List<Station>

  /** The first [limit] loaded stations, ordered by distance. */
  suspend fun nearbyStations(limit: Int): List<Station>

  /** A loaded station by ID, or null when it is not in the current snapshot. */
  suspend fun stationById(stationId: String): Station?

  /** Refreshes availability for one station without reloading the whole list. */
  suspend fun refreshAvailability(stationId: String)

  /** Nearest loaded station, using the app's search radius. */
  suspend fun nearestStation(): Station?

  /** Nearest loaded station with at least one bike. */
  suspend fun nearestStationWithBikes(): Station?

  /** Nearest loaded station with at least one free dock. */
  suspend fun nearestStationWithSlots(): Station?

  /** Nearest loaded station that satisfies [requirement] relative to [origin]. */
  suspend fun nearestStationFrom(
    origin: GeoPoint,
    requirement: StationRequirement,
  ): Station?

  /** Stations the user marked as favorite, ordered by distance then name. */
  suspend fun favoriteStations(): List<Station>

  /** Whether the user marked [stationId] as favorite. */
  fun isFavorite(stationId: String): Boolean

  /** The user's pinned home station ID, if any. */
  fun homeStationId(): String?

  /** The user's pinned work station ID, if any. */
  fun workStationId(): String?

  /** Pins [stationId] as the home station, replacing any previous pin. */
  suspend fun setHomeStation(stationId: String)

  /** Pins [stationId] as the work station, replacing any previous pin. */
  suspend fun setWorkStation(stationId: String)

  /**
   * Starts a time-boxed monitoring session for [stationId].
   *
   * @return True when the session started and alerts will be delivered; false
   * when the backend window cannot open, which is not an error.
   */
  suspend fun startMonitoring(
    stationId: String,
    durationSeconds: Int,
    kind: SurfaceMonitoringKind,
  ): Boolean

  /** Stops the session for [stationId], or every active session when null. */
  suspend fun stopMonitoring(stationId: String?)

  /** Whether an active session exists for [stationId], or for any station when null. */
  fun isMonitoring(stationId: String?): Boolean

  /** Creates or replaces the alert rule identified by [target]. */
  suspend fun upsertSavedPlaceAlertRule(
    target: SavedPlaceAlertTarget,
    condition: SavedPlaceAlertCondition,
  ): SavedPlaceAlertRule

  /** Toggles the favorite state of [stationId]. */
  suspend fun toggleFavorite(stationId: String)
}

/**
 * [BiciRadarAppData] backed by the application [SharedGraph].
 *
 * It reads station state through the graph's observation use cases instead of
 * touching repositories directly, so no persistence detail leaks into the
 * AppFunctions layer.
 */
internal class GraphBiciRadarAppData(
  private val graph: SharedGraph,
) : BiciRadarAppData {
  override suspend fun bootstrapSession() {
    graph.bootstrapSession.execute()
  }

  override suspend fun isCityConfigured(): Boolean = graph.isCityConfiguredUseCase.execute()

  override suspend fun currentCity(): City = graph.getCurrentCity.execute()

  override fun hasUserLocation(): Boolean = graph.observeStationsState.state.value.userLocation != null

  override fun lastUpdatedEpoch(): Long? = graph.observeStationsState.state.value.lastUpdatedEpoch

  override suspend fun searchStations(query: String): List<Station> = graph.filterStationsByQuery.execute(query)

  override suspend fun findStationMatchingQuery(query: String): Station? = graph.findStationMatchingQuery.execute(query)

  override suspend fun allStations(): List<Station> = graph.filterStationsByQuery.execute("")

  override suspend fun nearbyStations(limit: Int): List<Station> = graph.getNearbyStationList.execute(limit)

  override suspend fun stationById(stationId: String): Station? = graph.findStationById.execute(stationId)

  override suspend fun refreshAvailability(stationId: String) {
    graph.refreshStationAvailability.execute(listOf(stationId))
  }

  override suspend fun nearestStation(): Station? = graph.findNearestStation.execute()

  override suspend fun nearestStationWithBikes(): Station? = graph.findNearestStationWithBikes.execute()

  override suspend fun nearestStationWithSlots(): Station? = graph.findNearestStationWithSlots.execute()

  override suspend fun nearestStationFrom(
    origin: GeoPoint,
    requirement: StationRequirement,
  ): Station? = graph.findNearestStationFrom.execute(origin, requirement)

  override suspend fun favoriteStations(): List<Station> = graph.getFavoriteStationList.execute()

  override fun isFavorite(stationId: String): Boolean =
    graph.observeFavorites.favoriteIds.value
      .contains(stationId)

  override fun homeStationId(): String? = graph.observeFavorites.currentHomeStationId()

  override fun workStationId(): String? = graph.observeFavorites.currentWorkStationId()

  override suspend fun setHomeStation(stationId: String) {
    graph.setHomeStation.execute(stationId)
  }

  override suspend fun setWorkStation(stationId: String) {
    graph.setWorkStation.execute(stationId)
  }

  override suspend fun startMonitoring(
    stationId: String,
    durationSeconds: Int,
    kind: SurfaceMonitoringKind,
  ): Boolean = graph.startStationMonitoring.execute(stationId, durationSeconds, kind)

  override suspend fun stopMonitoring(stationId: String?) {
    graph.stopStationMonitoring.execute(stationId = stationId)
  }

  override fun isMonitoring(stationId: String?): Boolean {
    val sessions = graph.observeSurfaceMonitoring.state.value
    return if (stationId == null) {
      sessions.values.any { it.isActive }
    } else {
      sessions[stationId]?.isActive == true
    }
  }

  override suspend fun upsertSavedPlaceAlertRule(
    target: SavedPlaceAlertTarget,
    condition: SavedPlaceAlertCondition,
  ): SavedPlaceAlertRule = graph.upsertSavedPlaceAlertRule.execute(target, condition)

  override suspend fun toggleFavorite(stationId: String) {
    graph.toggleFavoriteStation.execute(stationId)
  }
}
