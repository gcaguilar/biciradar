package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Abstracción para el origen de datos remoto de estaciones.
 *
 * Sigue SRP: Solo se encarga de obtener datos de la red.
 */
interface StationsRemoteDataSource {
  /**
   * Obtiene estaciones desde la API remota.
   *
   * @param origin Punto de origen para calcular distancias
   * @return Lista de estaciones ordenadas por distancia
   */
  suspend fun fetchStations(origin: GeoPoint): List<Station>

  /**
   * Obtiene la disponibilidad actual de estaciones específicas.
   *
   * @param stationIds IDs de las estaciones a consultar
   * @return Mapa de ID de estación a disponibilidad
   */
  suspend fun fetchAvailability(stationIds: List<String>): Map<String, StationAvailability>
}

/**
 * Implementación del origen de datos remoto usando BiziApi.
 * Registrado automáticamente en el grafo vía @ContributesBinding.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class StationsRemoteDataSourceImpl(
  private val biziApi: BiziApi,
) : StationsRemoteDataSource {
  override suspend fun fetchStations(origin: GeoPoint): List<Station> = biziApi.fetchStations(origin)

  override suspend fun fetchAvailability(stationIds: List<String>): Map<String, StationAvailability> =
    biziApi.fetchAvailability(stationIds)
}
