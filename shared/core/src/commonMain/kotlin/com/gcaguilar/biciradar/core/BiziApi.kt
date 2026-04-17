package com.gcaguilar.biciradar.core

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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
    httpClient.get("https://datosbizi.com/api/patterns?stationId=${stationId.trimStart('0').ifEmpty { "0" }}").body()
}

data class StationAvailability(
  val bikesAvailable: Int,
  val slotsFree: Int,
  val ebikesAvailable: Int = 0,
  val regularBikesAvailable: Int = 0,
)

class GbfsBiziApi(
  private val httpClient: HttpClient,
  private val configuration: AppConfiguration,
  private val settingsRepository: SettingsRepository? = null,
  private val logger: Logger = NoOpLogger,
) : BiziApi {
  private val stationStatusUrlCache = mutableMapOf<String, String>()
  private val stationStatusUrlCacheLock = Mutex()

  private val gbfsDiscoveryUrl: String
    get() = settingsRepository?.currentSelectedCity()?.gbfsDiscoveryUrl ?: configuration.gbfsDiscoveryUrl

  override suspend fun fetchStations(origin: GeoPoint): List<Station> {
    val discovery = httpClient.get(gbfsDiscoveryUrl).body<GbfsDiscoveryEnvelope>()
    val stationInfoUrl =
      discovery.data.feeds
        .firstOrNull { it.name == "station_information" }
        ?.url
        ?: error("No station_information feed found")
    val stationStatusUrl =
      discovery.data.feeds
        .firstOrNull { it.name == "station_status" }
        ?.url
        ?: error("No station_status feed found")

    val stationInfo = httpClient.get(stationInfoUrl).body<GbfsStationInfoEnvelope>()
    val stationStatus = httpClient.get(stationStatusUrl).body<GbfsStationStatusEnvelope>()

    val statusById = stationStatus.data.stations.associateBy { it.stationId }

    return stationInfo.data.stations
      .mapNotNull { info ->
        val status = statusById[info.stationId] ?: return@mapNotNull null
        val location = GeoPoint(info.latitude, info.longitude)

        val vehicleTypes = status.vehicleTypesAvailable ?: emptyList()
        val ebikesCount =
          vehicleTypes
            .filter { it.vehicleTypeId.contains("ebike", ignoreCase = true) }
            .sumOf { it.count }
        val regularBikesCount =
          vehicleTypes
            .filter {
              it.vehicleTypeId.contains("bike", ignoreCase = true) &&
                !it.vehicleTypeId.contains("ebike", ignoreCase = true)
            }.sumOf { it.count }

        Station(
          id = info.stationId,
          name = info.name,
          address = info.address ?: info.name,
          location = location,
          bikesAvailable = status.numBikesAvailable,
          slotsFree = status.numDocksAvailable,
          distanceMeters = distanceBetween(origin, location),
          sourceLabel = "GBFS",
          ebikesAvailable = ebikesCount,
          regularBikesAvailable = regularBikesCount,
        )
      }.sortedBy(Station::distanceMeters)
  }

  override suspend fun fetchAvailability(stationIds: List<String>): Map<String, StationAvailability> {
    val cacheKey = settingsRepository?.currentSelectedCity()?.id ?: gbfsDiscoveryUrl
    val stationStatusUrl = getStationStatusUrl(cacheKey) ?: return emptyMap()

    return try {
      val stationStatus = httpClient.get(stationStatusUrl).body<GbfsStationStatusEnvelope>()
      val statusById = stationStatus.data.stations.associateBy { it.stationId }

      stationIds
        .mapNotNull { id ->
          val status = statusById[id] ?: return@mapNotNull null

          val vehicleTypes = status.vehicleTypesAvailable ?: emptyList()
          val ebikesCount =
            vehicleTypes
              .filter { it.vehicleTypeId.contains("ebike", ignoreCase = true) }
              .sumOf { it.count }
          val regularBikesCount =
            vehicleTypes
              .filter {
                it.vehicleTypeId.contains("bike", ignoreCase = true) &&
                  !it.vehicleTypeId.contains("ebike", ignoreCase = true)
              }.sumOf { it.count }

          id to
            StationAvailability(
              bikesAvailable = status.numBikesAvailable,
              slotsFree = status.numDocksAvailable,
              ebikesAvailable = ebikesCount,
              regularBikesAvailable = regularBikesCount,
            )
        }.toMap()
    } catch (e: Exception) {
      invalidateStationStatusUrl(cacheKey)
      logger.warn("GbfsBiziApi", "Failed to fetch availability for $cacheKey", e)
      emptyMap()
    }
  }

  private suspend fun getStationStatusUrl(cacheKey: String): String? {
    stationStatusUrlCacheLock.withLock {
      stationStatusUrlCache[cacheKey]?.let { return it }
    }

    val resolved =
      runCatching {
        val discovery = httpClient.get(gbfsDiscoveryUrl).body<GbfsDiscoveryEnvelope>()
        discovery.data.feeds
          .firstOrNull { it.name == "station_status" }
          ?.url
      }.onFailure { error ->
        logger.warn("GbfsBiziApi", "Failed to resolve station_status feed for $cacheKey", error)
      }.getOrNull()

    if (resolved != null) {
      stationStatusUrlCacheLock.withLock {
        stationStatusUrlCache[cacheKey] = resolved
      }
    }
    return resolved
  }

  private suspend fun invalidateStationStatusUrl(cacheKey: String) {
    stationStatusUrlCacheLock.withLock { stationStatusUrlCache.remove(cacheKey) }
  }
}

class CityBikesBiziApi(
  private val httpClient: HttpClient,
  private val configuration: AppConfiguration,
) : BiziApi {
  override suspend fun fetchStations(origin: GeoPoint): List<Station> =
    runCatching {
      fetchOfficialStations(origin)
    }.getOrElse {
      fetchFallbackStations(origin)
    }

  override suspend fun fetchAvailability(stationIds: List<String>): Map<String, StationAvailability> =
    stationIds
      .mapNotNull { id ->
        runCatching {
          val station =
            httpClient
              .get(configuration.stationAvailabilityUrl(id))
              .body<ZaragozaStation>()
          id to
            StationAvailability(
              bikesAvailable = station.bikesAvailable,
              slotsFree = station.slotsFree,
            )
        }.getOrNull()
      }.toMap()

  private suspend fun fetchOfficialStations(origin: GeoPoint): List<Station> {
    val pageSize = 100
    val firstPage =
      httpClient
        .get(configuration.stationsApiUrl(start = 0, rows = pageSize))
        .body<ZaragozaStationsEnvelope>()
    val totalCount = firstPage.totalCount ?: firstPage.result.size
    val allRawStations = firstPage.result.toMutableList()

    var start = pageSize
    while (allRawStations.size < totalCount) {
      val page =
        httpClient
          .get(configuration.stationsApiUrl(start = start, rows = pageSize))
          .body<ZaragozaStationsEnvelope>()
      if (page.result.isEmpty()) break
      allRawStations += page.result
      start += pageSize
    }

    return allRawStations
      .asSequence()
      .filter { station ->
        station.geometry.coordinates.size >= 2 && station.status.equals("IN_SERVICE", ignoreCase = true)
      }.map { station ->
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
      }.sortedBy(Station::distanceMeters)
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
        address =
          station.extra
            ?.address
            .orEmpty()
            .ifBlank { station.name },
        location = location,
        bikesAvailable = station.freeBikes ?: 0,
        slotsFree = station.emptySlots ?: 0,
        distanceMeters = distanceBetween(origin, location),
        sourceLabel = "CityBikes",
      )
    }
  }
}

class RoutingBiziApi(
  private val settingsRepository: SettingsRepository,
  private val cityRegistry: CityRegistry,
  private val gbfsBiziApi: GbfsBiziApi,
  private val cityBikesBiziApi: CityBikesBiziApi,
) : BiziApi {
  override suspend fun fetchStations(origin: GeoPoint): List<Station> = delegate().fetchStations(origin)

  override suspend fun fetchAvailability(stationIds: List<String>): Map<String, StationAvailability> =
    delegate().fetchAvailability(stationIds)

  private suspend fun delegate(): BiziApi {
    val currentCity = settingsRepository.currentSelectedCity()
    val strategy = cityRegistry.cityById(currentCity.id)?.apiStrategy ?: currentCity.apiStrategy()
    return when (strategy) {
      BiziApiStrategy.CityBikes -> cityBikesBiziApi
      BiziApiStrategy.Gbfs -> gbfsBiziApi
    }
  }
}

fun distanceBetween(
  origin: GeoPoint,
  destination: GeoPoint,
): Int {
  val earthRadiusMeters = 6_371_000.0
  val latitudeDelta = (destination.latitude - origin.latitude).toRadians()
  val longitudeDelta = (destination.longitude - origin.longitude).toRadians()
  val a =
    sin(latitudeDelta / 2).pow(2) +
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

@Serializable
private data class GbfsDiscoveryEnvelope(
  val data: GbfsDiscoveryData,
)

@Serializable(with = GbfsDiscoveryDataSerializer::class)
private data class GbfsDiscoveryData(
  val feeds: List<GbfsFeed> = emptyList(),
)

private object GbfsDiscoveryDataSerializer : KSerializer<GbfsDiscoveryData> {
  override val descriptor = buildClassSerialDescriptor("GbfsDiscoveryData")

  override fun deserialize(decoder: Decoder): GbfsDiscoveryData {
    val map = decoder.decodeSerializableValue(MapSerializer(String.serializer(), GbfsDiscoveryLanguage.serializer()))
    val feeds = (map["es"] ?: map["en"] ?: map.values.firstOrNull())?.feeds ?: emptyList()
    return GbfsDiscoveryData(feeds)
  }

  override fun serialize(
    encoder: Encoder,
    value: GbfsDiscoveryData,
  ) {
    encoder.encodeSerializableValue(
      MapSerializer(String.serializer(), GbfsDiscoveryLanguage.serializer()),
      mapOf("en" to GbfsDiscoveryLanguage(value.feeds)),
    )
  }
}

@Serializable
private data class GbfsDiscoveryLanguage(
  val feeds: List<GbfsFeed> = emptyList(),
)

@Serializable
private data class GbfsFeed(
  val name: String,
  val url: String,
)

@Serializable
private data class GbfsStationInfoEnvelope(
  val data: GbfsStationInfoData,
)

@Serializable
private data class GbfsStationInfoData(
  val stations: List<GbfsStationInfo> = emptyList(),
)

@Serializable
private data class GbfsStationInfo(
  @SerialName("station_id") val stationId: String,
  val name: String,
  @SerialName("lat") val latitude: Double,
  @SerialName("lon") val longitude: Double,
  val address: String? = null,
)

@Serializable
private data class GbfsStationStatusEnvelope(
  val data: GbfsStationStatusData,
)

@Serializable
private data class GbfsStationStatusData(
  val stations: List<GbfsStationStatus> = emptyList(),
)

@Serializable
private data class GbfsStationStatus(
  @SerialName("station_id") val stationId: String,
  @SerialName("num_bikes_available") val numBikesAvailable: Int,
  @SerialName("num_docks_available") val numDocksAvailable: Int,
  @SerialName("vehicle_types_available") val vehicleTypesAvailable: List<VehicleTypeAvailable>? = null,
)

@Serializable
private data class VehicleTypeAvailable(
  @SerialName("vehicle_type_id") val vehicleTypeId: String,
  val count: Int,
)
