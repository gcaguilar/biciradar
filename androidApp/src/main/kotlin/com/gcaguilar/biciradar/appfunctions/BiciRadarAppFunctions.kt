package com.gcaguilar.biciradar.appfunctions

import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationRequirement
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.stationsSortedByDistanceFrom
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Orchestration behind the AppFunctions surface.
 *
 * The service only translates these results into Android primitives; everything
 * that decides which data to read, which validations to apply, and which error
 * to report lives here so it can be tested without Android.
 *
 * @param data Data and session seam.
 * @param onFavoritesChanged Invoked after a favorite mutation so the platform can
 * refresh widgets and paired devices.
 * @param errors Factory for the AppFunctions errors reported to the calling agent.
 */
internal class BiciRadarAppFunctions(
  private val data: BiciRadarAppData,
  private val onFavoritesChanged: () -> Unit,
  private val errors: AppFunctionErrors = DefaultAppFunctionErrors,
) {
  private val readinessMutex = Mutex()

  @Volatile private var ready: Boolean = false

  /**
   * Bootstraps the session and verifies that a city is configured.
   *
   * Idempotent and serialized: concurrent AppFunction calls bootstrap once. A
   * missing city is not memoized as ready, so a later call succeeds as soon as
   * the user configures one.
   */
  suspend fun ensureReady() {
    if (ready) return
    readinessMutex.withLock {
      if (ready) return
      data.bootstrapSession()
      if (!data.isCityConfigured()) {
        throw errors.appUnknown(
          "No city is configured. Ask the user to open BiciRadar and select a city first.",
        )
      }
      ready = true
    }
  }

  /** Returns the selected city as [CityInfo]. */
  suspend fun currentCity(): CityInfo {
    ensureReady()
    return data.currentCity().toCityInfo()
  }

  /** Searches stations by text or pinned alias. */
  suspend fun searchStations(
    query: String,
    limit: Int?,
  ): List<StationInfo> {
    ensureReady()
    val trimmedQuery = query.trim()
    if (trimmedQuery.isEmpty()) {
      throw errors.invalidArgument(
        "query must not be blank. Pass a station name, an address fragment, or a pinned alias " +
          "such as \"home\" or \"work\".",
      )
    }
    val resolvedLimit = requireValidLimit(limit ?: DEFAULT_RESULT_LIMIT)
    val stations =
      if (isPinnedStationAlias(trimmedQuery)) {
        listOfNotNull(data.findStationMatchingQuery(trimmedQuery))
      } else {
        data.searchStations(trimmedQuery)
      }
    return stations.take(resolvedLimit).map { it.toAppFunctionInfo() }
  }

  /** Refreshes and returns a single station. */
  suspend fun stationAvailability(stationId: String): StationInfo {
    ensureReady()
    val id = requireStationId(stationId)
    val loadedStations = data.allStations()
    data.refreshAvailability(id)
    val station =
      data.stationById(id)
        ?: throw if (loadedStations.isEmpty()) {
          errors.appUnknown(
            "No station data is available right now. Ask the user to open BiciRadar and try " +
              "again later.",
          )
        } else {
          errors.elementNotFound(unknownStationMessage(id))
        }
    return station.toAppFunctionInfo()
  }

  /** Finds the nearest station that satisfies [requirement]. */
  suspend fun findNearestStation(
    requirement: String?,
    latitude: Double?,
    longitude: Double?,
  ): StationInfo? {
    ensureReady()
    val parsedRequirement =
      parseStationRequirement(requirement ?: StationRequirement.ANY.name)
        ?: throw errors.invalidArgument(
          "requirement must be one of \"ANY\", \"BIKES\", \"EBIKES\" or \"SLOTS\". " +
            "Received \"$requirement\".",
        )
    val origin = requireValidOrigin(latitude, longitude)
    val station =
      when {
        origin is OriginArgument.Present -> {
          data.nearestStationFrom(origin.point, parsedRequirement)
        }

        parsedRequirement == StationRequirement.ANY -> {
          data.nearestStation()
        }

        parsedRequirement == StationRequirement.BIKES -> {
          data.nearestStationWithBikes()
        }

        parsedRequirement == StationRequirement.SLOTS -> {
          data.nearestStationWithSlots()
        }

        else -> {
          data.allStations().firstOrNull { station -> station.ebikesAvailable > 0 }
        }
      } ?: return null
    return station.toAppFunctionInfo(
      distanceIsUserRelative = origin is OriginArgument.Present || data.hasUserLocation(),
    )
  }

  /** Lists the stations closest to the caller's origin. */
  suspend fun listNearbyStations(
    limit: Int?,
    latitude: Double?,
    longitude: Double?,
  ): List<StationInfo> {
    ensureReady()
    val resolvedLimit = requireValidLimit(limit ?: DEFAULT_RESULT_LIMIT)
    val origin = requireValidOrigin(latitude, longitude)
    val stations =
      if (origin is OriginArgument.Present) {
        stationsSortedByDistanceFrom(data.allStations(), origin.point).take(resolvedLimit)
      } else {
        data.nearbyStations(resolvedLimit)
      }
    return stations.map {
      it.toAppFunctionInfo(
        distanceIsUserRelative = origin is OriginArgument.Present || data.hasUserLocation(),
      )
    }
  }

  /** Lists the user's favorite stations. */
  suspend fun listFavoriteStations(): List<StationInfo> {
    ensureReady()
    return data.favoriteStations().map { it.toAppFunctionInfo() }
  }

  /** Adds a station to favorites without toggling an existing one. */
  suspend fun addFavoriteStation(stationId: String): StationInfo {
    ensureReady()
    val id = requireStationId(stationId)
    val station = requireExistingStation(id)
    if (!data.isFavorite(id)) {
      data.toggleFavorite(id)
      onFavoritesChanged()
    }
    return station.toAppFunctionInfo(isFavorite = true)
  }

  /** Removes a station from favorites, reporting whether it was one. */
  suspend fun removeFavoriteStation(stationId: String): Boolean {
    ensureReady()
    val id = requireStationId(stationId)
    requireExistingStation(id)
    if (!data.isFavorite(id)) return false
    data.toggleFavorite(id)
    onFavoritesChanged()
    return true
  }

  /**
   * Pins [stationId] as the user's home station, replacing any previous pin.
   *
   * Home is its own category: pinning does not add the station to favorites.
   */
  suspend fun setHomeStation(stationId: String): StationInfo = pinStation(stationId, isHome = true)

  /**
   * Pins [stationId] as the user's work station, replacing any previous pin.
   *
   * Work is its own category: pinning does not add the station to favorites.
   */
  suspend fun setWorkStation(stationId: String): StationInfo = pinStation(stationId, isHome = false)

  /** Starts a time-boxed monitoring session for a station. */
  suspend fun startStationMonitoring(
    stationId: String,
    kind: String?,
    durationMinutes: Int?,
  ): MonitoringInfo {
    ensureReady()
    val id = requireStationId(stationId)
    val parsedKind =
      parseMonitoringKind(kind ?: SurfaceMonitoringKind.Docks.name)
        ?: throw errors.invalidArgument(
          "kind must be \"BIKES\" or \"DOCKS\". Received \"$kind\".",
        )
    val resolvedDuration = durationMinutes ?: DEFAULT_MONITORING_MINUTES
    if (!isValidMonitoringDuration(resolvedDuration)) {
      throw errors.invalidArgument(
        "durationMinutes must be between $MIN_MONITORING_MINUTES and " +
          "$MAX_MONITORING_MINUTES. Received $resolvedDuration.",
      )
    }
    val station = requireExistingStation(id)
    val started = data.startMonitoring(id, resolvedDuration * SECONDS_PER_MINUTE, parsedKind)
    val monitors = parsedKind.name.uppercase()
    val message =
      if (started) {
        "Monitoring \"${station.name}\" ($id) for $resolvedDuration minutes. Alerts will be " +
          "delivered while the session is active; it ends automatically when the time runs out."
      } else {
        "Monitoring did not start, so no alerts were scheduled. Ask the user to open BiciRadar " +
          "and check that they are signed in with notifications enabled, then try again."
      }
    return MonitoringInfo(
      stationId = id,
      started = started,
      durationMinutes = resolvedDuration,
      monitors = monitors,
      message = message,
    )
  }

  /**
   * Stops monitoring for one station, or every active session when [stationId]
   * is null.
   *
   * @return True when at least one active session was stopped.
   */
  suspend fun stopStationMonitoring(stationId: String?): Boolean {
    ensureReady()
    if (stationId == null) {
      if (!data.isMonitoring(null)) return false
      data.stopMonitoring(null)
      return true
    }
    val id = requireStationId(stationId)
    if (!data.isMonitoring(id)) return false
    data.stopMonitoring(id)
    return true
  }

  /** Creates or replaces the alert rule identified by station and target. */
  suspend fun createSavedPlaceAlert(
    stationId: String,
    target: String?,
    condition: String,
    count: Int?,
  ): SavedPlaceAlertInfo {
    ensureReady()
    val id = requireStationId(stationId)
    val parsedTarget =
      parseAlertTargetArgument(target ?: AlertTargetArgument.FAVORITE.name)
        ?: throw errors.invalidArgument(
          "target must be \"FAVORITE\", \"HOME\" or \"WORK\". Received \"$target\".",
        )
    val parsedCondition =
      parseAlertConditionArgument(condition)
        ?: throw errors.invalidArgument(
          "condition must be \"BIKES_AT_LEAST\", \"DOCKS_AT_LEAST\", " +
            "\"BIKES_EQUALS_ZERO\" or \"DOCKS_EQUALS_ZERO\". Received \"$condition\".",
        )
    val station = requireExistingStation(id)
    requireAlertTargetPrecondition(parsedTarget, id)
    val alertCondition = buildAlertCondition(parsedCondition, count)
    val cityId = data.currentCity().id
    val alertTarget = buildAlertTarget(parsedTarget, id, cityId, station.name)
    return data.upsertSavedPlaceAlertRule(alertTarget, alertCondition).toSavedPlaceAlertInfo()
  }

  /**
   * Validates that [stationId] exists and returns it trimmed.
   *
   * Used by the service before building the deep link for `openStationInApp`.
   */
  suspend fun validatedStationId(stationId: String): String {
    ensureReady()
    val id = requireStationId(stationId)
    requireExistingStation(id)
    return id
  }

  private suspend fun pinStation(
    stationId: String,
    isHome: Boolean,
  ): StationInfo {
    ensureReady()
    val id = requireStationId(stationId)
    val station = requireExistingStation(id)
    val currentPin = if (isHome) data.homeStationId() else data.workStationId()
    if (currentPin != id) {
      if (isHome) data.setHomeStation(id) else data.setWorkStation(id)
      onFavoritesChanged()
    }
    return station.toAppFunctionInfo()
  }

  private fun requireAlertTargetPrecondition(
    target: AlertTargetArgument,
    stationId: String,
  ) {
    when (target) {
      AlertTargetArgument.FAVORITE -> {
        if (!data.isFavorite(stationId)) {
          throw errors.invalidArgument(
            "An alert for target FAVORITE requires the station to be a favorite. Call " +
              "\"addFavoriteStation\" with this stationId first, then retry.",
          )
        }
      }

      AlertTargetArgument.HOME -> {
        if (data.homeStationId() != stationId) {
          throw errors.invalidArgument(
            "An alert for target HOME requires this station to be the pinned home station. " +
              "Call \"setHomeStation\" with this stationId first, then retry.",
          )
        }
      }

      AlertTargetArgument.WORK -> {
        if (data.workStationId() != stationId) {
          throw errors.invalidArgument(
            "An alert for target WORK requires this station to be the pinned work station. " +
              "Call \"setWorkStation\" with this stationId first, then retry.",
          )
        }
      }
    }
  }

  private fun buildAlertCondition(
    condition: AlertConditionArgument,
    count: Int?,
  ): SavedPlaceAlertCondition =
    when (condition) {
      AlertConditionArgument.BIKES_AT_LEAST -> {
        SavedPlaceAlertCondition.BikesAtLeast(requireAlertThreshold(count))
      }

      AlertConditionArgument.DOCKS_AT_LEAST -> {
        SavedPlaceAlertCondition.DocksAtLeast(requireAlertThreshold(count))
      }

      AlertConditionArgument.BIKES_EQUALS_ZERO -> {
        SavedPlaceAlertCondition.BikesEqualsZero
      }

      AlertConditionArgument.DOCKS_EQUALS_ZERO -> {
        SavedPlaceAlertCondition.DocksEqualsZero
      }
    }

  private fun requireAlertThreshold(count: Int?): Int {
    val resolved = count ?: DEFAULT_ALERT_COUNT
    if (!isValidAlertCount(resolved)) {
      throw errors.invalidArgument(
        "count must be between $MIN_ALERT_COUNT and $MAX_ALERT_COUNT for *_AT_LEAST " +
          "conditions. Received $resolved.",
      )
    }
    return resolved
  }

  private fun buildAlertTarget(
    target: AlertTargetArgument,
    stationId: String,
    cityId: String,
    stationName: String,
  ): SavedPlaceAlertTarget =
    when (target) {
      AlertTargetArgument.FAVORITE -> {
        SavedPlaceAlertTarget.FavoriteStation(
          stationId = stationId,
          cityId = cityId,
          stationName = stationName,
        )
      }

      AlertTargetArgument.HOME -> {
        SavedPlaceAlertTarget.Home(
          stationId = stationId,
          cityId = cityId,
          stationName = stationName,
        )
      }

      AlertTargetArgument.WORK -> {
        SavedPlaceAlertTarget.Work(
          stationId = stationId,
          cityId = cityId,
          stationName = stationName,
        )
      }
    }

  private fun requireStationId(stationId: String): String {
    val id = stationId.trim()
    if (id.isEmpty()) {
      throw errors.invalidArgument(
        "stationId must not be blank. Call \"searchStations\" first to obtain a stationId.",
      )
    }
    return id
  }

  private fun requireValidLimit(limit: Int): Int {
    if (!isValidResultLimit(limit)) {
      throw errors.invalidArgument(
        "limit must be between $MIN_RESULT_LIMIT and $MAX_RESULT_LIMIT. Received $limit.",
      )
    }
    return limit
  }

  private fun requireValidOrigin(
    latitude: Double?,
    longitude: Double?,
  ): OriginArgument =
    when (val origin = parseOriginArgument(latitude, longitude)) {
      OriginArgument.Invalid -> {
        throw errors.invalidArgument(
          "latitude and longitude must be supplied together, with latitude in -90..90 and " +
            "longitude in -180..180. Received latitude=$latitude, longitude=$longitude.",
        )
      }

      else -> {
        origin
      }
    }

  private suspend fun requireExistingStation(stationId: String): Station {
    data.allStations()
    return data.stationById(stationId) ?: throw errors.elementNotFound(unknownStationMessage(stationId))
  }

  private fun Station.toAppFunctionInfo(
    distanceIsUserRelative: Boolean = data.hasUserLocation(),
    isFavorite: Boolean = data.isFavorite(id),
  ): StationInfo =
    toStationInfo(
      isFavorite = isFavorite,
      isHome = data.homeStationId() == id,
      isWork = data.workStationId() == id,
      lastUpdatedEpoch = data.lastUpdatedEpoch(),
      distanceIsUserRelative = distanceIsUserRelative,
    )

  private fun unknownStationMessage(stationId: String): String =
    "No station with id \"$stationId\" is available in the selected city. Call " +
      "\"searchStations\" to resolve a valid stationId."
}
