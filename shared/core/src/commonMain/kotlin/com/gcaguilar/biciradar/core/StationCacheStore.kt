package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.currentTimeMs
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import com.gcaguilar.biciradar.core.local.StationEntity

/**
 * Intervalo de refresco del caché de estaciones (5 minutos).
 */
const val STATION_CACHE_REFRESH_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes

class StationCacheStore(
  private val database: BiciRadarDatabase,
) {
  fun loadStations(cityId: String): List<StationEntity>? =
    try {
      val metadata = database.biciradarQueries.getCacheMetadata().executeAsOneOrNull()
      if (metadata?.city_id == cityId) {
        database.biciradarQueries.getAllStations().executeAsList().map { row ->
          StationEntity(
            id = row.id,
            name = row.name,
            address = row.address ?: "",
            latitude = row.latitude,
            longitude = row.longitude,
            bikesAvailable = row.bikes_available.toInt(),
            slotsFree = row.slots_free.toInt(),
            ebikesAvailable = row.ebikes_available.toInt(),
            regularBikesAvailable = row.regular_bikes_available.toInt(),
            updatedAt = row.updated_at,
          )
        }
      } else {
        null
      }
    } catch (_: Exception) {
      null
    }

  fun isFresh(cityId: String): Boolean =
    try {
      val metadata = database.biciradarQueries.getCacheMetadata().executeAsOneOrNull()
      if (metadata?.city_id == cityId) {
        val elapsed = currentTimeMs() - metadata.last_updated
        elapsed < STATION_CACHE_REFRESH_INTERVAL_MS
      } else {
        false
      }
    } catch (_: Exception) {
      false
    }

  fun lastUpdated(cityId: String): Long? =
    try {
      database.biciradarQueries
        .getCacheMetadata()
        .executeAsOneOrNull()
        ?.takeIf { it.city_id == cityId }
        ?.last_updated
    } catch (_: Exception) {
      null
    }

  fun updateAvailability(
    availability: Map<String, Pair<Int, Int>>,
    refreshedAt: Long,
  ) {
    if (availability.isEmpty()) return
    try {
      database.transaction {
        availability.forEach { (stationId, counts) ->
          database.biciradarQueries.updateStationBikeDockCounts(
            bikesAvailable = counts.first.toLong(),
            slotsFree = counts.second.toLong(),
            updatedAt = refreshedAt,
            id = stationId,
          )
        }
      }
    } catch (_: Exception) {
      // Silently fail - cache is optional
    }
  }

  fun save(
    cityId: String,
    stations: List<Station>,
  ) {
    try {
      database.transaction {
        database.biciradarQueries.deleteAllStations()
        database.biciradarQueries.deleteAllCacheMetadata()
        stations.forEach { station ->
          database.biciradarQueries.insertStation(
            id = station.id,
            name = station.name,
            address = station.address,
            latitude = station.location.latitude,
            longitude = station.location.longitude,
            bikesAvailable = station.bikesAvailable.toLong(),
            slotsFree = station.slotsFree.toLong(),
            ebikesAvailable = station.ebikesAvailable.toLong(),
            regularBikesAvailable = station.regularBikesAvailable.toLong(),
            updatedAt = currentTimeMs(),
          )
        }
        database.biciradarQueries.upsertCacheMetadata(
          cityId = cityId,
          lastUpdated = currentTimeMs(),
        )
      }
    } catch (_: Exception) {
      // Silently fail - cache is optional
    }
  }

  fun clear() {
    try {
      database.transaction {
        database.biciradarQueries.deleteAllStations()
        database.biciradarQueries.deleteAllCacheMetadata()
      }
    } catch (_: Exception) {
      // Silently fail
    }
  }

  companion object {
    fun clearCacheForCityChange(
      database: BiciRadarDatabase,
      newCityId: String,
    ) {
      try {
        val metadata = database.biciradarQueries.getCacheMetadata().executeAsOneOrNull()
        if (metadata?.city_id != newCityId) {
          database.transaction {
            database.biciradarQueries.deleteAllStations()
            database.biciradarQueries.deleteAllCacheMetadata()
          }
        }
      } catch (_: Exception) {
        // Silently fail
      }
    }
  }
}
