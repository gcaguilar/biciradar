package com.gcaguilar.biciradar.appfunctions

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import androidx.appfunctions.AppFunctionStringValueConstraint
import com.gcaguilar.biciradar.BiziAppGraph
import com.gcaguilar.biciradar.MainActivity
import com.gcaguilar.biciradar.TripMonitorService
import com.gcaguilar.biciradar.WidgetRefreshWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Entry point that exposes BiciRadar to on-device agents through Android
 * AppFunctions.
 *
 * KSP generates the concrete `BiciRadarAppFunctionService` named by
 * [AppFunctionServiceEntryPoint.serviceName], plus the AppFunctions inventory
 * that the manifest declares. Dependencies come from [BiziAppGraph], which
 * `BiciRadarApplication.onCreate()` initializes before the system can bind
 * this service.
 *
 * Every function delegates its orchestration to [BiciRadarAppFunctions] and
 * keeps only the Android glue (Intents, PendingIntents and widget refresh) here.
 */
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
  serviceName = "BiciRadarAppFunctionService",
  appFunctionXmlFileName = "biciradar_app_function_service",
)
abstract class BaseBiciRadarAppFunctionService : AppFunctionService() {
  private val appFunctions: BiciRadarAppFunctions by lazy {
    BiciRadarAppFunctions(
      data = GraphBiciRadarAppData(BiziAppGraph.graph),
      onFavoritesChanged = { WidgetRefreshWorker.scheduleImmediate(applicationContext) },
    )
  }

  /**
   * Read the city currently selected in BiciRadar to learn which bike-sharing
   * network every other function operates on.
   *
   * @return The selected city with its stable ID, display name and e-bike support.
   * @throws androidx.appfunctions.AppFunctionAppUnknownException If no city is configured.
   * Ask the user to open BiciRadar and select a city first.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun getCurrentCity(): CityInfo =
    withContext(Dispatchers.IO) {
      appFunctions.currentCity()
    }

  /**
   * Find bike-sharing stations by name, street, or pinned alias to obtain a
   * stationId for the functions that require one.
   *
   * Required workflow: call this before "getStationAvailability",
   * "addFavoriteStation", "removeFavoriteStation" or "openStationInApp".
   *
   * @param query Station name, address fragment, or a pinned alias: "home", "casa",
   * "work", "trabajo" or "oficina". Must not be blank.
   * @param limit Maximum number of stations to return, from 1 to 20. Omit it or pass null
   * to use 5.
   * @return Matching stations ranked by relevance, at most "limit" items. Empty when
   * nothing matches; do not retry the same query.
   * @throws androidx.appfunctions.AppFunctionInvalidArgumentException If "query" is blank
   * or "limit" is outside 1..20. Fix the argument before retrying.
   * @throws androidx.appfunctions.AppFunctionAppUnknownException If no city is configured.
   * Ask the user to open BiciRadar and select a city first.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun searchStations(
    query: String,
    limit: Int? = null,
  ): List<StationInfo> =
    withContext(Dispatchers.IO) {
      appFunctions.searchStations(query, limit)
    }

  /**
   * Check live bike and dock availability at one station so the user knows
   * whether to walk there right now.
   *
   * Availability is refreshed before it is returned; never reuse an earlier
   * result for a later request.
   *
   * Required workflow: call "searchStations", "findNearestStation" or
   * "listFavoriteStations" first to obtain a stationId.
   *
   * @param stationId ID returned by another BiciRadar function. Must not be blank.
   * @return The station with refreshed bikes, e-bikes, mechanical bikes, free docks,
   * distance and update time.
   * @throws androidx.appfunctions.AppFunctionInvalidArgumentException If "stationId" is blank.
   * @throws androidx.appfunctions.AppFunctionElementNotFoundException If the ID is not in the
   * selected city. Call "searchStations" to resolve a valid stationId.
   * @throws androidx.appfunctions.AppFunctionAppUnknownException If no city is configured or no
   * station data is available. Ask the user to open BiciRadar and try again later.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun getStationAvailability(stationId: String): StationInfo =
    withContext(Dispatchers.IO) {
      appFunctions.stationAvailability(stationId)
    }

  /**
   * Find the nearest station that meets a bike or dock requirement so the user
   * can rent or return a bike immediately.
   *
   * @param requirement What the station must offer: "ANY" for any station, "BIKES" for
   * at least one bike, "EBIKES" for at least one electric bike, "SLOTS" for at least one
   * free dock. Case-insensitive. Omit it or pass null to use "ANY".
   * @param latitude Latitude of the user in degrees, from -90 to 90. Supply it together
   * with "longitude", or omit both to use the app's last known location.
   * @param longitude Longitude of the user in degrees, from -180 to 180. Supply it together
   * with "latitude", or omit both to use the app's last known location.
   * @return The closest matching station, or null when no station satisfies "requirement".
   * A null result is a valid answer: tell the user nothing matches and do not retry.
   * @throws androidx.appfunctions.AppFunctionInvalidArgumentException If "requirement" is not
   * one of the listed values, or only one coordinate is supplied, or a coordinate is out of
   * range. Correct the argument before retrying.
   * @throws androidx.appfunctions.AppFunctionAppUnknownException If no city is configured.
   * Ask the user to open BiciRadar and select a city first.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun findNearestStation(
    @AppFunctionStringValueConstraint(enumValues = ["ANY", "BIKES", "EBIKES", "SLOTS"])
    requirement: String? = null,
    latitude: Double? = null,
    longitude: Double? = null,
  ): StationInfo? =
    withContext(Dispatchers.IO) {
      appFunctions.findNearestStation(requirement, latitude, longitude)
    }

  /**
   * List the closest stations so the user can compare bike and dock availability
   * before choosing one.
   *
   * @param limit Maximum number of stations to return, from 1 to 20. Omit it or pass null
   * to use 5.
   * @param latitude Latitude of the user in degrees, from -90 to 90. Supply it together
   * with "longitude", or omit both to use the app's last known location.
   * @param longitude Longitude of the user in degrees, from -180 to 180. Supply it together
   * with "latitude", or omit both to use the app's last known location.
   * @return Stations ordered from closest to farthest, at most "limit" items. Empty when no
   * station data is available.
   * @throws androidx.appfunctions.AppFunctionInvalidArgumentException If "limit" is outside
   * 1..20, or only one coordinate is supplied, or a coordinate is out of range. Correct the
   * argument before retrying.
   * @throws androidx.appfunctions.AppFunctionAppUnknownException If no city is configured.
   * Ask the user to open BiciRadar and select a city first.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun listNearbyStations(
    limit: Int? = null,
    latitude: Double? = null,
    longitude: Double? = null,
  ): List<StationInfo> =
    withContext(Dispatchers.IO) {
      appFunctions.listNearbyStations(limit, latitude, longitude)
    }

  /**
   * List the user's favorite stations with live availability so saved places can
   * be checked in one call instead of many "getStationAvailability" calls.
   *
   * @return Favorite stations ordered by distance, each flagged with "isFavorite",
   * "isHome" and "isWork". Empty when the user has no favorites.
   * @throws androidx.appfunctions.AppFunctionAppUnknownException If no city is configured.
   * Ask the user to open BiciRadar and select a city first.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun listFavoriteStations(): List<StationInfo> =
    withContext(Dispatchers.IO) {
      appFunctions.listFavoriteStations()
    }

  /**
   * Add a station to the user's favorites so it appears in the app, widgets and
   * paired watch.
   *
   * Adding a station that is already a favorite is a no-op; this is not a toggle
   * and never removes a saved station.
   *
   * Required workflow: call "searchStations" or "findNearestStation" first to
   * obtain a stationId.
   *
   * @param stationId ID of an existing station in the selected city. Must not be blank.
   * @return The station marked as favorite, with its current availability.
   * @throws androidx.appfunctions.AppFunctionInvalidArgumentException If "stationId" is blank.
   * @throws androidx.appfunctions.AppFunctionElementNotFoundException If the ID is not in the
   * selected city. Call "searchStations" to resolve a valid stationId.
   * @throws androidx.appfunctions.AppFunctionAppUnknownException If no city is configured.
   * Ask the user to open BiciRadar and select a city first.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun addFavoriteStation(stationId: String): StationInfo =
    withContext(Dispatchers.IO) {
      appFunctions.addFavoriteStation(stationId)
    }

  /**
   * Remove a station from the user's favorites so it stops appearing in saved
   * places, widgets and the paired watch.
   *
   * Reversible and safe to repeat: removing a station that is not a favorite
   * returns false and changes nothing.
   *
   * Required workflow: call "listFavoriteStations" first to confirm the station is saved.
   *
   * @param stationId ID of an existing station in the selected city. Must not be blank.
   * @return True when the station was a favorite and has been removed; false when it was
   * already not a favorite.
   * @throws androidx.appfunctions.AppFunctionInvalidArgumentException If "stationId" is blank.
   * @throws androidx.appfunctions.AppFunctionElementNotFoundException If the ID is not in the
   * selected city. Call "searchStations" to resolve a valid stationId.
   * @throws androidx.appfunctions.AppFunctionAppUnknownException If no city is configured.
   * Ask the user to open BiciRadar and select a city first.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun removeFavoriteStation(stationId: String): Boolean =
    withContext(Dispatchers.IO) {
      appFunctions.removeFavoriteStation(stationId)
    }

  /**
   * Open a station inside BiciRadar so the user can inspect it on the map or
   * start navigation to it.
   *
   * Launch the returned PendingIntent; do not try to deep-link the station in
   * another app.
   *
   * Required workflow: call "searchStations", "findNearestStation" or
   * "listFavoriteStations" first to obtain a stationId.
   *
   * @param stationId ID of an existing station in the selected city. Must not be blank.
   * @return PendingIntent that opens BiciRadar on the station detail screen.
   * @throws androidx.appfunctions.AppFunctionInvalidArgumentException If "stationId" is blank.
   * @throws androidx.appfunctions.AppFunctionElementNotFoundException If the ID is not in the
   * selected city. Call "searchStations" to resolve a valid stationId.
   * @throws androidx.appfunctions.AppFunctionAppUnknownException If no city is configured.
   * Ask the user to open BiciRadar and select a city first.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun openStationInApp(stationId: String): PendingIntent =
    withContext(Dispatchers.IO) {
      val validatedId = appFunctions.validatedStationId(stationId)
      stationPendingIntent(validatedId)
    }

  /**
   * Pin a station as the user's home so "home"/"casa" resolves to it in the app,
   * widgets and the paired watch.
   *
   * Home is a separate category: pinning does not add the station to favorites.
   * There is only one home station, so this replaces any previous home pin.
   *
   * Required workflow: call "searchStations" first to obtain a stationId.
   *
   * @param stationId ID of an existing station in the selected city. Must not be blank.
   * @return The station with its current availability and "isHome" set to true.
   * @throws androidx.appfunctions.AppFunctionInvalidArgumentException If "stationId" is blank.
   * @throws androidx.appfunctions.AppFunctionElementNotFoundException If the ID is not in the
   * selected city. Call "searchStations" to resolve a valid stationId.
   * @throws androidx.appfunctions.AppFunctionAppUnknownException If no city is configured.
   * Ask the user to open BiciRadar and select a city first.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun setHomeStation(stationId: String): StationInfo =
    withContext(Dispatchers.IO) {
      appFunctions.setHomeStation(stationId)
    }

  /**
   * Pin a station as the user's work station so "work"/"trabajo" resolves to it
   * in the app, widgets and the paired watch.
   *
   * Work is a separate category: pinning does not add the station to favorites.
   * There is only one work station, so this replaces any previous work pin.
   *
   * Required workflow: call "searchStations" first to obtain a stationId.
   *
   * @param stationId ID of an existing station in the selected city. Must not be blank.
   * @return The station with its current availability and "isWork" set to true.
   * @throws androidx.appfunctions.AppFunctionInvalidArgumentException If "stationId" is blank.
   * @throws androidx.appfunctions.AppFunctionElementNotFoundException If the ID is not in the
   * selected city. Call "searchStations" to resolve a valid stationId.
   * @throws androidx.appfunctions.AppFunctionAppUnknownException If no city is configured.
   * Ask the user to open BiciRadar and select a city first.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun setWorkStation(stationId: String): StationInfo =
    withContext(Dispatchers.IO) {
      appFunctions.setWorkStation(stationId)
    }

  /**
   * Watch a station for a limited time so the user is alerted when its bikes or
   * free docks change.
   *
   * Monitoring is time-boxed: the session ends automatically after
   * "durationMinutes". A result with "started" false means no alerts were
   * scheduled; report that honestly instead of claiming the station is watched.
   *
   * Required workflow: call "searchStations" first to obtain a stationId.
   *
   * @param stationId ID of an existing station in the selected city. Must not be blank.
   * @param kind What the session watches: "BIKES" or "DOCKS". Case-insensitive. Omit it or
   * pass null to use "DOCKS".
   * @param durationMinutes Length of the session in minutes, from 1 to 60. Omit it or pass
   * null to use 10.
   * @return The session summary. When "started" is true, alerts will be delivered. When
   * false, no alert was scheduled and "message" explains what the user must fix.
   * @throws androidx.appfunctions.AppFunctionInvalidArgumentException If "stationId" is blank,
   * "kind" is not BIKES/DOCKS, or "durationMinutes" is outside 1..60. Correct the argument.
   * @throws androidx.appfunctions.AppFunctionElementNotFoundException If the ID is not in the
   * selected city. Call "searchStations" to resolve a valid stationId.
   * @throws androidx.appfunctions.AppFunctionAppUnknownException If no city is configured.
   * Ask the user to open BiciRadar and select a city first.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun startStationMonitoring(
    stationId: String,
    @AppFunctionStringValueConstraint(enumValues = ["BIKES", "DOCKS"])
    kind: String? = null,
    durationMinutes: Int? = null,
  ): MonitoringInfo =
    withContext(Dispatchers.IO) {
      val info = appFunctions.startStationMonitoring(stationId, kind, durationMinutes)
      if (info.started) {
        // The foreground notification is optional: a blocked start must not turn
        // a successfully scheduled session into an error, because backend push
        // alerts are delivered independently of the service.
        runCatching { TripMonitorService.start(applicationContext) }
      }
      info
    }

  /**
   * Stop station monitoring so the user stops receiving alerts.
   *
   * Without "stationId" this stops every active session. Stopping a station that
   * is not being monitored is a no-op.
   *
   * Required workflow: call "startStationMonitoring" first, or omit "stationId"
   * to stop all sessions.
   *
   * @param stationId Station whose session should stop. Omit it or pass null to stop all
   * active sessions.
   * @return True when at least one active session was stopped; false when nothing was
   * being monitored, in which case tell the user there was nothing to stop.
   * @throws androidx.appfunctions.AppFunctionInvalidArgumentException If "stationId" is supplied
   * but blank.
   * @throws androidx.appfunctions.AppFunctionAppUnknownException If no city is configured.
   * Ask the user to open BiciRadar and select a city first.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun stopStationMonitoring(stationId: String? = null): Boolean =
    withContext(Dispatchers.IO) {
      appFunctions.stopStationMonitoring(stationId)
    }

  /**
   * Create or replace an availability alert for a saved station so the user is
   * notified when it meets a bike or dock condition.
   *
   * Rules are keyed by station and target: repeating the same station and target
   * replaces the condition instead of adding a duplicate.
   *
   * Required workflow: the station must already be a favorite
   * ("addFavoriteStation") or the matching pinned station ("setHomeStation" or
   * "setWorkStation"), depending on "target".
   *
   * @param stationId ID of an existing station in the selected city. Must not be blank.
   * @param target Saved place the alert belongs to: "FAVORITE", "HOME" or "WORK".
   * Case-insensitive. Omit it or pass null to use "FAVORITE".
   * @param condition Required condition: "BIKES_AT_LEAST", "DOCKS_AT_LEAST",
   * "BIKES_EQUALS_ZERO" or "DOCKS_EQUALS_ZERO". Case-insensitive.
   * @param count Threshold in vehicles for "*_AT_LEAST", from 1 to 100. Omit it or pass null
   * to use 1. Ignored for "*_EQUALS_ZERO".
   * @return The created or replaced rule with its stable ID and enabled state.
   * @throws androidx.appfunctions.AppFunctionInvalidArgumentException If an argument is invalid
   * or the target precondition is not met. Follow the message: call "addFavoriteStation",
   * "setHomeStation" or "setWorkStation" first, then retry.
   * @throws androidx.appfunctions.AppFunctionElementNotFoundException If the ID is not in the
   * selected city. Call "searchStations" to resolve a valid stationId.
   * @throws androidx.appfunctions.AppFunctionAppUnknownException If no city is configured.
   * Ask the user to open BiciRadar and select a city first.
   */
  @AppFunctionDeclaration(isDescribedByKDoc = true)
  suspend fun createSavedPlaceAlert(
    stationId: String,
    @AppFunctionStringValueConstraint(enumValues = ["FAVORITE", "HOME", "WORK"])
    target: String? = null,
    @AppFunctionStringValueConstraint(
      enumValues = ["BIKES_AT_LEAST", "DOCKS_AT_LEAST", "BIKES_EQUALS_ZERO", "DOCKS_EQUALS_ZERO"],
    )
    condition: String,
    count: Int? = null,
  ): SavedPlaceAlertInfo =
    withContext(Dispatchers.IO) {
      appFunctions.createSavedPlaceAlert(stationId, target, condition, count)
    }

  /** Builds the deep link PendingIntent for a station detail screen. */
  private fun stationPendingIntent(stationId: String): PendingIntent {
    val uri = Uri.parse("biciradar://station/$stationId")
    val intent =
      Intent(Intent.ACTION_VIEW, uri, this, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
      }
    return PendingIntent.getActivity(
      this,
      uri.hashCode(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }
}
