package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.local.StationEntity

/**
 * Low-level cache interface for station availability data.
 *
 * Abstracts the SQLDelight-based caching of station bike/dock counts so that
 * consumers (StationsCacheManager, tests) depend on a narrow interface
 * instead of the concrete class with direct database coupling.
 */
interface StationCacheStorage {
  /**
   * Loads cached stations for the given city from the database.
   * Returns null if no cached data exists for [cityId].
   */
  fun loadStations(cityId: String): List<StationEntity>?

  /**
   * Checks whether the cache is fresh for [cityId] and contains stations.
   * Freshness is defined as updated within [STATION_CACHE_REFRESH_INTERVAL_MS].
   */
  fun isFresh(cityId: String): Boolean

  /**
   * Returns the last update timestamp for the cache of [cityId], or null.
   */
  fun lastUpdated(cityId: String): Long?

  /**
   * Updates station bike/dock counts for the given availability data.
   */
  suspend fun updateAvailability(
    availability: Map<String, Pair<Int, Int>>,
    refreshedAt: Long,
  )

  /**
   * Saves full station data for [cityId] into the cache.
   */
  suspend fun save(
    cityId: String,
    stations: List<Station>,
  )

  /**
   * Clears all cached station data.
   */
  suspend fun clear()
}
