package com.gcaguilar.bizizaragoza.core

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

interface BiziApi {
  suspend fun fetchStations(origin: GeoPoint): List<Station>
}

class CityBikesBiziApi(
  private val httpClient: HttpClient,
  private val configuration: AppConfiguration,
) : BiziApi {
  override suspend fun fetchStations(origin: GeoPoint): List<Station> {
    val response = httpClient.get(configuration.cityBikesNetworkUrl).body<CityBikesNetworkEnvelope>()
    return response.network.stations.map { station ->
      Station(
        id = station.id ?: station.name,
        name = station.name,
        address = station.extra?.address.orEmpty().ifBlank { station.name },
        location = GeoPoint(station.latitude, station.longitude),
        bikesAvailable = station.freeBikes ?: 0,
        slotsFree = station.emptySlots ?: 0,
        distanceMeters = distanceBetween(origin, GeoPoint(station.latitude, station.longitude)),
      )
    }
  }
}

internal fun distanceBetween(origin: GeoPoint, destination: GeoPoint): Int {
  val earthRadiusMeters = 6_371_000.0
  val latitudeDelta = (destination.latitude - origin.latitude).toRadians()
  val longitudeDelta = (destination.longitude - origin.longitude).toRadians()
  val a = sin(latitudeDelta / 2).pow(2) +
    cos(origin.latitude.toRadians()) * cos(destination.latitude.toRadians()) *
    sin(longitudeDelta / 2).pow(2)
  val c = 2 * asin(sqrt(a))
  return (earthRadiusMeters * c).roundToInt()
}

private fun Double.toRadians(): Double = this * PI / 180.0

@Serializable
private data class CityBikesNetworkEnvelope(
  val network: CityBikesNetwork,
)

@Serializable
private data class CityBikesNetwork(
  val stations: List<CityBikesStation>,
)

@Serializable
private data class CityBikesStation(
  val id: String? = null,
  val name: String,
  val latitude: Double,
  val longitude: Double,
  @SerialName("free_bikes") val freeBikes: Int? = null,
  @SerialName("empty_slots") val emptySlots: Int? = null,
  val extra: CityBikesExtra? = null,
)

@Serializable
private data class CityBikesExtra(
  val address: String? = null,
)
