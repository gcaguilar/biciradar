package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.distanceBetween
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

interface BiziApi {
  suspend fun fetchStations(origin: GeoPoint): List<Station>

  suspend fun fetchAvailability(stationIds: List<String>): Map<String, StationAvailability>
}

interface DatosBiziApi {
  /** Whether hourly usage patterns are available for [cityId]. */
  fun supports(cityId: String): Boolean

  suspend fun fetchPatterns(
    cityId: String,
    stationId: String,
  ): List<StationHourlyPattern>
}

/**
 * Zaragoza provider: datosbizi.com is the only source of usage patterns for
 * that city. Every other city is served by the BiciRadar backend (see
 * `RoutingDatosBiziApi`).
 */
class DatosBiziApiImpl(
  private val httpClient: HttpClient,
) : DatosBiziApi {
  override fun supports(cityId: String): Boolean = cityId == City.ZARAGOZA.id

  override suspend fun fetchPatterns(
    cityId: String,
    stationId: String,
  ): List<StationHourlyPattern> {
    if (!supports(cityId)) return emptyList()
    return httpClient
      .get(
        "https://datosbizi.com/api/patterns?stationId=${stationId.trimStart('0').ifEmpty { "0" }}",
      ).body()
  }
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
  private val discoveryCache = mutableMapOf<String, GbfsDiscoveryEnvelope>()
  private val discoveryCacheLock = Mutex()
  private val vehicleTypePropulsionCache = mutableMapOf<String, Map<String, String>>()
  private val vehicleTypePropulsionLock = Mutex()

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
    val cacheKey = settingsRepository?.currentSelectedCity()?.id ?: gbfsDiscoveryUrl
    val propulsion = resolveVehicleTypePropulsion(discovery, cacheKey)

    val statusById = stationStatus.data.stations.associateBy { it.stationId }

    return stationInfo.data.stations
      .mapNotNull { info ->
        val status = statusById[info.stationId] ?: return@mapNotNull null
        val location = GeoPoint(info.latitude, info.longitude)
        val (ebikesCount, regularBikesCount) = status.bikeBreakdown(propulsion)

        Station(
          id = info.stationId,
          name = info.name,
          address = info.address ?: info.name,
          location = location,
          bikesAvailable = status.bikesAvailable,
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
    return try {
      val discovery = discoveryFor(cacheKey) ?: return emptyMap()
      val stationStatusUrl =
        discovery.data.feeds
          .firstOrNull { it.name == "station_status" }
          ?.url ?: return emptyMap()
      val propulsion = resolveVehicleTypePropulsion(discovery, cacheKey)
      val stationStatus = httpClient.get(stationStatusUrl).body<GbfsStationStatusEnvelope>()
      val statusById = stationStatus.data.stations.associateBy { it.stationId }

      stationIds
        .mapNotNull { id ->
          val status = statusById[id] ?: return@mapNotNull null
          val (ebikesCount, regularBikesCount) = status.bikeBreakdown(propulsion)

          id to
            StationAvailability(
              bikesAvailable = status.bikesAvailable,
              slotsFree = status.numDocksAvailable,
              ebikesAvailable = ebikesCount,
              regularBikesAvailable = regularBikesCount,
            )
        }.toMap()
    } catch (e: Exception) {
      invalidateCaches(cacheKey)
      logger.warn("GbfsBiziApi", "Failed to fetch availability for $cacheKey", e)
      emptyMap()
    }
  }

  /** Resolves and caches the discovery document; the feeds rarely change. */
  private suspend fun discoveryFor(cacheKey: String): GbfsDiscoveryEnvelope? {
    discoveryCacheLock.withLock { discoveryCache[cacheKey]?.let { return it } }
    val discovery =
      runCatching {
        httpClient.get(gbfsDiscoveryUrl).body<GbfsDiscoveryEnvelope>()
      }.onFailure { error ->
        logger.warn("GbfsBiziApi", "Failed to resolve GBFS discovery for $cacheKey", error)
      }.getOrNull() ?: return null
    discoveryCacheLock.withLock { discoveryCache[cacheKey] = discovery }
    return discovery
  }

  /**
   * Maps `vehicle_type_id` to its GBFS `propulsion_type`. Official feeds keep
   * operator ids (`FIT`, `EFIT`, `BOOST`, ...), so the country-agnostic id
   * heuristic alone is not enough to tell e-bikes apart.
   */
  private suspend fun resolveVehicleTypePropulsion(
    discovery: GbfsDiscoveryEnvelope,
    cacheKey: String,
  ): Map<String, String> {
    vehicleTypePropulsionLock.withLock { vehicleTypePropulsionCache[cacheKey]?.let { return it } }
    val vehicleTypesUrl =
      discovery.data.feeds
        .firstOrNull { it.name == "vehicle_types" }
        ?.url ?: return emptyMap()
    val types =
      runCatching {
        httpClient.get(vehicleTypesUrl).body<GbfsVehicleTypesEnvelope>()
      }.onFailure { error ->
        logger.warn("GbfsBiziApi", "Failed to resolve vehicle types for $cacheKey", error)
      }.getOrNull() ?: return emptyMap()
    val propulsion = types.data.vehicleTypes.associate { it.vehicleTypeId to it.propulsionType }
    vehicleTypePropulsionLock.withLock { vehicleTypePropulsionCache[cacheKey] = propulsion }
    return propulsion
  }

  private fun GbfsStationStatus.bikeBreakdown(propulsion: Map<String, String>): Pair<Int, Int> {
    val types = vehicleTypesAvailable ?: return 0 to 0
    if (types.isEmpty()) return 0 to 0
    val total = bikesAvailable.coerceAtLeast(0)
    val electric =
      types
        .filter { type ->
          propulsion[type.vehicleTypeId]?.contains("electric", ignoreCase = true)
            ?: type.vehicleTypeId.looksElectric()
        }.sumOf { it.count }
    // GBFS 3.0 counts can include overflow vehicles, so never exceed the total.
    val ebikes = electric.coerceAtMost(total)
    return ebikes to (total - ebikes)
  }

  private fun String.looksElectric(): Boolean =
    contains("ebike", ignoreCase = true) ||
      contains("e-bike", ignoreCase = true) ||
      contains("electric", ignoreCase = true) ||
      contains("efit", ignoreCase = true) ||
      contains("boost", ignoreCase = true)

  private suspend fun invalidateCaches(cacheKey: String) {
    discoveryCacheLock.withLock { discoveryCache.remove(cacheKey) }
    vehicleTypePropulsionLock.withLock { vehicleTypePropulsionCache.remove(cacheKey) }
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
    // GBFS 3.0 keeps feeds at data.feeds; GBFS 2.x nests them per language
    // (data.es.feeds, data.en.feeds, ...).
    val map = decoder.decodeSerializableValue(MapSerializer(String.serializer(), JsonElement.serializer()))
    val direct = map["feeds"] as? JsonArray
    val nested =
      map.values
        .filterIsInstance<JsonObject>()
        .firstNotNullOfOrNull { it["feeds"] as? JsonArray }
    val elements = direct ?: nested ?: return GbfsDiscoveryData()
    val feeds =
      Json.decodeFromJsonElement(
        ListSerializer(GbfsFeed.serializer()),
        elements,
      )
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
  @Serializable(with = LocalizedTextSerializer::class) val name: String,
  @SerialName("lat") val latitude: Double,
  @SerialName("lon") val longitude: Double,
  @Serializable(with = LocalizedTextSerializer::class) val address: String? = null,
)

/**
 * GBFS 3.0 allows `name`/`address` to be a language list
 * (`[{"text": "...", "language": "es"}, ...]`) instead of a plain string.
 * Picks Spanish, then English, then the first entry.
 */
private object LocalizedTextSerializer : KSerializer<String> {
  override val descriptor = PrimitiveSerialDescriptor("LocalizedText", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): String {
    val jsonDecoder = decoder as? JsonDecoder ?: return decoder.decodeString()
    val element = jsonDecoder.decodeJsonElement()
    return when (element) {
      is JsonPrimitive -> element.content
      is JsonArray -> pick(element)
      is JsonObject -> (element["text"] as? JsonPrimitive)?.content ?: ""
    }
  }

  private fun pick(array: JsonArray): String {
    val entries = array.mapNotNull { entry -> (entry as? JsonObject)?.let { textEntry(it) } }
    val byLanguage = entries.associate { it.second to it.first }
    return byLanguage["es"] ?: byLanguage["en"] ?: entries.firstOrNull()?.first ?: ""
  }

  private fun textEntry(entry: JsonObject): Pair<String, String?>? {
    val text = (entry["text"] as? JsonPrimitive)?.content ?: return null
    return text to (entry["language"] as? JsonPrimitive)?.content
  }

  override fun serialize(
    encoder: Encoder,
    value: String,
  ) {
    encoder.encodeString(value)
  }
}

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
  @SerialName("num_bikes_available") val numBikesAvailable: Int? = null,
  @SerialName("num_vehicles_available") val numVehiclesAvailable: Int? = null,
  @SerialName("num_docks_available") val numDocksAvailable: Int,
  @SerialName("vehicle_types_available") val vehicleTypesAvailable: List<VehicleTypeAvailable>? = null,
) {
  /** GBFS 3.0 renamed `num_bikes_available` to `num_vehicles_available`. */
  val bikesAvailable: Int
    get() = numBikesAvailable ?: numVehiclesAvailable ?: 0
}

@Serializable
private data class VehicleTypeAvailable(
  @SerialName("vehicle_type_id") val vehicleTypeId: String,
  val count: Int,
)

@Serializable
private data class GbfsVehicleTypesEnvelope(
  val data: GbfsVehicleTypesData,
)

@Serializable
private data class GbfsVehicleTypesData(
  @SerialName("vehicle_types") val vehicleTypes: List<GbfsVehicleType> = emptyList(),
)

@Serializable
private data class GbfsVehicleType(
  @SerialName("vehicle_type_id") val vehicleTypeId: String,
  @SerialName("propulsion_type") val propulsionType: String = "",
)
