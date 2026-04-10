package com.gcaguilar.biciradar.core

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import com.gcaguilar.biciradar.core.local.StationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Metadatos del caché de estaciones.
 */
data class CacheMetadata(
  val cityId: String,
  val lastUpdated: Long,
)

/**
 * Abstracción para la gestión de caché de estaciones.
 *
 * Sigue SRP: Solo se encarga de almacenar y recuperar estaciones del caché.
 */
interface StationsCacheManager {
  /**
   * Flow reactivo de estaciones desde la base de datos.
   * Null si no hay base de datos disponible.
   */
  val stationsFlow: Flow<List<StationEntity>>?

  /**
   * Flow reactivo de metadatos del caché.
   * Null si no hay base de datos disponible.
   */
  val metadataFlow: Flow<CacheMetadata?>?

  /**
   * Verifica si el caché es fresco para una ciudad.
   *
   * @param cityId ID de la ciudad
   * @return true si el caché es válido y fresco
   */
  fun isFresh(cityId: String): Boolean

  /**
   * Obtiene la última fecha de actualización del caché.
   *
   * @param cityId ID de la ciudad
   * @return Timestamp de la última actualización o null
   */
  fun lastUpdated(cityId: String): Long?

  /**
   * Guarda estaciones en el caché.
   *
   * @param cityId ID de la ciudad
   * @param stations Lista de estaciones a guardar
   */
  suspend fun save(cityId: String, stations: List<Station>)

  /**
   * Actualiza la disponibilidad de estaciones en el caché.
   *
   * @param availability Mapa de ID de estación a par (bikesAvailable, slotsFree)
   * @param refreshedAt Timestamp de la actualización
   */
  suspend fun updateAvailability(
    availability: Map<String, Pair<Int, Int>>,
    refreshedAt: Long,
  )

  /**
   * Limpia el caché.
   */
  suspend fun clear()
}

/**
 * Implementación del gestor de caché usando SQLDelight.
 */
class StationsCacheManagerImpl(
  private val database: BiciRadarDatabase,
  private val cacheStore: StationCacheStore,
) : StationsCacheManager {

  override val stationsFlow: Flow<List<StationEntity>>? =
    database.biciradarQueries.getAllStations()
      .asFlow()
      .mapToList(Dispatchers.Default)
      .map { rows ->
        rows.map { row ->
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
      }

  override val metadataFlow: Flow<CacheMetadata?>? =
    database.biciradarQueries.getCacheMetadata()
      .asFlow()
      .mapToOneOrNull(Dispatchers.Default)
      .map { meta ->
        meta?.let {
          CacheMetadata(
            cityId = it.city_id,
            lastUpdated = it.last_updated,
          )
        }
      }

  override fun isFresh(cityId: String): Boolean {
    return cacheStore.isFresh(cityId)
  }

  override fun lastUpdated(cityId: String): Long? {
    return cacheStore.lastUpdated(cityId)
  }

  override suspend fun save(cityId: String, stations: List<Station>) {
    cacheStore.save(cityId, stations)
  }

  override suspend fun updateAvailability(
    availability: Map<String, Pair<Int, Int>>,
    refreshedAt: Long,
  ) {
    cacheStore.updateAvailability(availability, refreshedAt)
  }

  override suspend fun clear() {
    cacheStore.clear()
  }
}

/**
 * Implementación no-op del gestor de caché para cuando no hay base de datos.
 */
class NoOpStationsCacheManager : StationsCacheManager {
  override val stationsFlow: Flow<List<StationEntity>>? = null
  override val metadataFlow: Flow<CacheMetadata?>? = null
  override fun isFresh(cityId: String): Boolean = false
  override fun lastUpdated(cityId: String): Long? = null
  override suspend fun save(cityId: String, stations: List<Station>) {}
  override suspend fun updateAvailability(availability: Map<String, Pair<Int, Int>>, refreshedAt: Long) {}
  override suspend fun clear() {}
}
