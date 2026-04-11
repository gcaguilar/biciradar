package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.currentTimeMs
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Age &lt; this since [StationsState.lastUpdatedEpoch] counts as [DataFreshness.Fresh] when data is trusted. */
const val STATION_SNAPSHOT_FRESH_MS = 5 * 60 * 1000L

/** Beyond this age, cached data is [DataFreshness.Expired]. */
const val STATION_SNAPSHOT_STALE_MAX_MS = 60 * 60 * 1000L

/** Wall-clock millis for UI copy such as “updated N min ago” (same clock as freshness). */
fun epochMillisForUi(): Long = currentTimeMs()

@Serializable
data class GeoPoint(
  val latitude: Double,
  val longitude: Double,
)

@Serializable
data class Station(
  val id: String,
  val name: String,
  val address: String,
  val location: GeoPoint,
  val bikesAvailable: Int,
  val slotsFree: Int,
  val distanceMeters: Int,
  val sourceLabel: String = "BiciRadar",
  val ebikesAvailable: Int = 0,
  val regularBikesAvailable: Int = 0,
)

@Serializable
data class StationsState(
  val stations: List<Station> = emptyList(),
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val userLocation: GeoPoint? = null,
  val lastUpdatedEpoch: Long? = null,
  val dataSource: StationDataSource = StationDataSource.Network,
  val freshness: DataFreshness = DataFreshness.Fresh,
  val lastRefreshAttemptEpoch: Long? = null,
  val lastRefreshFailureEpoch: Long? = null,
)

/**
 * Derive [DataFreshness] from [lastUpdatedEpoch] age. When [servingCacheAfterFailure] is true,
 * offline copy rules apply (no [DataFreshness.Fresh] — still usable cache shows as [DataFreshness.StaleUsable]).
 */
fun computeStationsFreshness(
  lastUpdatedEpoch: Long?,
  nowEpoch: Long = currentTimeMs(),
  servingCacheAfterFailure: Boolean,
  stationsEmpty: Boolean,
  hardFailure: Boolean,
): DataFreshness {
  if (hardFailure && stationsEmpty) return DataFreshness.Unavailable
  val updated = lastUpdatedEpoch ?: return if (stationsEmpty) DataFreshness.Unavailable else DataFreshness.Fresh
  val age = nowEpoch - updated
  if (servingCacheAfterFailure) {
    return if (age <= STATION_SNAPSHOT_STALE_MAX_MS) DataFreshness.StaleUsable else DataFreshness.Expired
  }
  return when {
    age < STATION_SNAPSHOT_FRESH_MS -> DataFreshness.Fresh
    age <= STATION_SNAPSHOT_STALE_MAX_MS -> DataFreshness.StaleUsable
    else -> DataFreshness.Expired
  }
}

@Serializable
data class FavoritesSyncSnapshot(
  val categories: List<FavoriteCategory> = emptyList(),
  val stationCategory: Map<String, String> = emptyMap(),
  val favoriteIds: Set<String> = emptySet(),
  val homeStationId: String? = null,
  val workStationId: String? = null,
)

@Serializable
data class FavoriteCategory(
  val id: String,
  val label: String,
  val isSystem: Boolean = false,
)

object FavoriteCategoryIds {
  const val FAVORITE = "favorite"
  const val HOME = "home"
  const val WORK = "work"
}

@Serializable
sealed interface AssistantAction {
  @Serializable
  @SerialName("nearest_station")
  data object NearestStation : AssistantAction

  @Serializable
  @SerialName("nearest_station_with_bikes")
  data object NearestStationWithBikes : AssistantAction

  @Serializable
  @SerialName("nearest_station_with_slots")
  data object NearestStationWithSlots : AssistantAction

  @Serializable
  @SerialName("station_bike_count")
  data class StationBikeCount(
    val stationId: String,
  ) : AssistantAction

  @Serializable
  @SerialName("station_slot_count")
  data class StationSlotCount(
    val stationId: String,
  ) : AssistantAction

  @Serializable
  @SerialName("station_status")
  data class StationStatus(
    val stationId: String,
  ) : AssistantAction

  @Serializable
  @SerialName("favorite_stations")
  data object FavoriteStations : AssistantAction

  @Serializable
  @SerialName("route_to_station")
  data class RouteToStation(
    val stationId: String,
  ) : AssistantAction
}

data class AssistantResolution(
  val spokenResponse: String,
  val highlightedStationId: String? = null,
)

@Serializable
data class StationHourlyPattern(
  val stationId: String,
  val dayType: String,
  val hour: Int,
  val bikesAvg: Double,
  val anchorsAvg: Double,
  val occupancyAvg: Double,
  val sampleCount: Int,
)
