package com.gcaguilar.biciradar.core

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
  suspend fun fetchAvailability(stationIds: List<String>): Map<String, StationAvailability>
}

interface DatosBiziApi {
  suspend fun fetchPatterns(stationId: String): List<StationHourlyPattern>
}

class DatosBiziApiImpl(
  private val httpClient: HttpClient,
) : DatosBiziApi {
  override suspend fun fetchPatterns(stationId: String): List<StationHourlyPattern> =
    httpClient.get("https://datosbizi.com/api/patterns?stationId=$stationId").body()
}

data class StationAvailability(
  val bikesAvailable: Int,
  val slotsFree: Int,
)

class CityBikesBiziApi(
  private val httpClient: HttpClient,
  private val configuration: AppConfiguration,
) : BiziApi {
  override suspend fun fetchStations(origin: GeoPoint): List<Station> {
    return runCatching {
      fetchOfficialStations(origin)
    }.getOrElse {
      fetchFallbackStations(origin)
    }
  }

  override suspend fun fetchAvailability(stationIds: List<String>): Map<String, StationAvailability> {
    return stationIds.mapNotNull { id ->
      runCatching {
        val station = httpClient.get(configuration.stationAvailabilityUrl(id))
          .body<ZaragozaStation>()
        id to StationAvailability(
          bikesAvailable = station.bikesAvailable,
          slotsFree = station.slotsFree,
        )
      }.getOrNull()
    }.toMap()
  }

  private suspend fun fetchOfficialStations(origin: GeoPoint): List<Station> {
    val pageSize = 100
    val firstPage = httpClient.get(configuration.stationsApiUrl(start = 0, rows = pageSize))
      .body<ZaragozaStationsEnvelope>()
    val totalCount = firstPage.totalCount ?: firstPage.result.size
    val allRawStations = firstPage.result.toMutableList()

    var start = pageSize
    while (allRawStations.size < totalCount) {
      val page = httpClient.get(configuration.stationsApiUrl(start = start, rows = pageSize))
        .body<ZaragozaStationsEnvelope>()
      if (page.result.isEmpty()) break
      allRawStations += page.result
      start += pageSize
    }

    return allRawStations
      .asSequence()
      .filter { station ->
        station.geometry.coordinates.size >= 2 && station.status.equals("IN_SERVICE", ignoreCase = true)
      }
      .map { station ->
        val latitude = station.geometry.coordinates[1]
        val longitude = station.geometry.coordinates[0]
        val location = GeoPoint(latitude, longitude)
        Station(
          id = station.id,
          name = station.title,
          address = station.address.ifBlank { station.title },
          location = location,
          bikesAvailable = station.bikesAvailable,
          slotsFree = station.slotsFree,
          distanceMeters = distanceBetween(origin, location),
          sourceLabel = "Ayuntamiento de Zaragoza",
        )
      }
      .sortedBy(Station::distanceMeters)
      .toList()
      .takeIf { it.isNotEmpty() }
      ?: error("Official Zaragoza Bizi feed returned no stations.")
  }

  private suspend fun fetchFallbackStations(origin: GeoPoint): List<Station> {
    val response = httpClient.get(configuration.stationsFallbackApiUrl).body<CityBikesNetworkEnvelope>()
    return response.network.stations.map { station ->
      val location = GeoPoint(station.latitude, station.longitude)
      Station(
        id = station.id ?: station.name,
        name = station.name,
        address = station.extra?.address.orEmpty().ifBlank { station.name },
        location = location,
        bikesAvailable = station.freeBikes ?: 0,
        slotsFree = station.emptySlots ?: 0,
        distanceMeters = distanceBetween(origin, location),
        sourceLabel = "CityBikes",
      )
    }
  }
}

fun distanceBetween(origin: GeoPoint, destination: GeoPoint): Int {
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

@Serializable
private data class ZaragozaStationsEnvelope(
  val result: List<ZaragozaStation> = emptyList(),
  val totalCount: Int? = null,
)

@Serializable
private data class ZaragozaStation(
  val id: String,
  val title: String,
  val address: String,
  @SerialName("estado") val status: String,
  @SerialName("bicisDisponibles") val bikesAvailable: Int = 0,
  @SerialName("anclajesDisponibles") val slotsFree: Int = 0,
  val geometry: ZaragozaGeometry,
)

@Serializable
private data class ZaragozaGeometry(
  val coordinates: List<Double> = emptyList(),
)
