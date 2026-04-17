package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.currentTimeMs
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

data class EnvironmentalReading(
  val airQualityIndex: Int?,
  val pollenIndex: Int?,
)

interface EnvironmentalRepository {
  suspend fun readingAt(
    latitude: Double,
    longitude: Double,
  ): EnvironmentalReading?
}

/**
 * Implementación de EnvironmentalRepository.
 * Registrado automáticamente en el grafo vía @ContributesBinding.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class EnvironmentalRepositoryImpl(
  private val httpClient: HttpClient,
  private val database: BiciRadarDatabase?,
  private val appScope: CoroutineScope,
) : EnvironmentalRepository {
  private val cacheMutex = Mutex()
  private val readingCache = mutableMapOf<String, CachedEnvironmentalReading>()
  private val inflightRequests = mutableMapOf<String, Deferred<EnvironmentalReading?>>()

  override suspend fun readingAt(
    latitude: Double,
    longitude: Double,
  ): EnvironmentalReading? {
    val cacheKey = cacheKey(latitude, longitude)
    val now = currentTimeMs()
    val deferred =
      cacheMutex.withLock {
        val cached = readingCache[cacheKey]
        if (cached != null && (now - cached.savedAtEpochMs) <= ENVIRONMENTAL_CACHE_TTL_MS) {
          return cached.reading
        }
        val persisted = loadFromDatabase(cacheKey)
        if (persisted != null && (now - persisted.savedAtEpochMs) <= ENVIRONMENTAL_CACHE_TTL_MS) {
          readingCache[cacheKey] = persisted
          return persisted.reading
        }
        inflightRequests[cacheKey] ?: appScope
          .async {
            fetchAndCache(cacheKey, latitude, longitude, now)
          }.also { inflightRequests[cacheKey] = it }
      }
    return deferred.await()
  }

  private suspend fun fetchAndCache(
    cacheKey: String,
    latitude: Double,
    longitude: Double,
    now: Long,
  ): EnvironmentalReading? {
    try {
      val response =
        runCatching {
          httpClient
            .get("https://air-quality-api.open-meteo.com/v1/air-quality") {
              parameter("latitude", latitude)
              parameter("longitude", longitude)
              parameter(
                "current",
                "us_aqi,alder_pollen,birch_pollen,grass_pollen,mugwort_pollen,olive_pollen,ragweed_pollen",
              )
              parameter(
                "hourly",
                "us_aqi,alder_pollen,birch_pollen,grass_pollen,mugwort_pollen,olive_pollen,ragweed_pollen",
              )
            }.body<OpenMeteoAirQualityResponse>()
        }.getOrNull() ?: return null
      val current = response.current ?: return null
      val currentPollenValues =
        listOfNotNull(
          current.alderPollen,
          current.birchPollen,
          current.grassPollen,
          current.mugwortPollen,
          current.olivePollen,
          current.ragweedPollen,
        )
      val hourlyPollenMax = response.hourly?.maxPollen()
      val pollenRaw =
        listOfNotNull(
          hourlyPollenMax,
          currentPollenValues.maxOrNull(),
        ).maxOrNull()
      val pollenIndex = pollenRaw?.roundToInt()?.coerceIn(0, 100)
      val hourlyAqi =
        response.hourly
          ?.usAqi
          ?.firstOrNull { it != null }
          ?.toInt()
          ?: response.hourly
            ?.europeanAqi
            ?.firstOrNull { it != null }
            ?.toInt()
      val freshReading =
        EnvironmentalReading(
          airQualityIndex = (current.usAqi?.toInt() ?: current.europeanAqi?.toInt() ?: hourlyAqi)?.coerceIn(0, 500),
          pollenIndex = pollenIndex,
        )
      cacheMutex.withLock {
        readingCache[cacheKey] =
          CachedEnvironmentalReading(
            reading = freshReading,
            savedAtEpochMs = now,
          )
      }
      saveToDatabase(cacheKey, freshReading, now)
      deleteExpiredFromDatabase(now)
      return freshReading
    } finally {
      cacheMutex.withLock { inflightRequests.remove(cacheKey) }
    }
  }

  private fun cacheKey(
    latitude: Double,
    longitude: Double,
  ): String {
    // 3 decimals (~110m) avoids collapsing nearby zones into one cache bucket.
    val lat = (latitude * 1000.0).roundToInt() / 1000.0
    val lon = (longitude * 1000.0).roundToInt() / 1000.0
    return "$lat,$lon"
  }

  private fun loadFromDatabase(zoneKey: String): CachedEnvironmentalReading? {
    val db = database ?: return null
    return runCatching {
      db.biciradarQueries.getEnvironmentalReadingByZoneKey(zoneKey).executeAsOneOrNull()?.let { row ->
        CachedEnvironmentalReading(
          reading =
            EnvironmentalReading(
              airQualityIndex = row.air_quality_index?.toInt(),
              pollenIndex = row.pollen_index?.toInt(),
            ),
          savedAtEpochMs = row.updated_at,
        )
      }
    }.getOrNull()
  }

  private fun saveToDatabase(
    zoneKey: String,
    reading: EnvironmentalReading,
    updatedAt: Long,
  ) {
    val db = database ?: return
    runCatching {
      db.biciradarQueries.upsertEnvironmentalReading(
        zoneKey = zoneKey,
        airQualityIndex = reading.airQualityIndex?.toLong(),
        pollenIndex = reading.pollenIndex?.toLong(),
        updatedAt = updatedAt,
      )
    }
  }

  private fun deleteExpiredFromDatabase(nowEpoch: Long) {
    val db = database ?: return
    val cutoff = nowEpoch - ENVIRONMENTAL_CACHE_TTL_MS
    runCatching {
      db.biciradarQueries.deleteExpiredEnvironmentalReadings(cutoff)
    }
  }
}

private data class CachedEnvironmentalReading(
  val reading: EnvironmentalReading,
  val savedAtEpochMs: Long,
)

private const val ENVIRONMENTAL_CACHE_TTL_MS = 60L * 60L * 1000L

@Serializable
private data class OpenMeteoAirQualityResponse(
  val current: OpenMeteoAirQualityCurrent? = null,
  val hourly: OpenMeteoAirQualityHourly? = null,
)

@Serializable
private data class OpenMeteoAirQualityCurrent(
  @SerialName("us_aqi") val usAqi: Double? = null,
  @SerialName("european_aqi") val europeanAqi: Double? = null,
  @SerialName("alder_pollen") val alderPollen: Double? = null,
  @SerialName("birch_pollen") val birchPollen: Double? = null,
  @SerialName("grass_pollen") val grassPollen: Double? = null,
  @SerialName("mugwort_pollen") val mugwortPollen: Double? = null,
  @SerialName("olive_pollen") val olivePollen: Double? = null,
  @SerialName("ragweed_pollen") val ragweedPollen: Double? = null,
)

@Serializable
private data class OpenMeteoAirQualityHourly(
  @SerialName("us_aqi") val usAqi: List<Double?> = emptyList(),
  @SerialName("european_aqi") val europeanAqi: List<Double?> = emptyList(),
  @SerialName("alder_pollen") val alderPollen: List<Double?> = emptyList(),
  @SerialName("birch_pollen") val birchPollen: List<Double?> = emptyList(),
  @SerialName("grass_pollen") val grassPollen: List<Double?> = emptyList(),
  @SerialName("mugwort_pollen") val mugwortPollen: List<Double?> = emptyList(),
  @SerialName("olive_pollen") val olivePollen: List<Double?> = emptyList(),
  @SerialName("ragweed_pollen") val ragweedPollen: List<Double?> = emptyList(),
)

private fun OpenMeteoAirQualityHourly.maxPollen(): Double? =
  listOf(
    alderPollen,
    birchPollen,
    grassPollen,
    mugwortPollen,
    olivePollen,
    ragweedPollen,
  ).asSequence()
    .flatMap { it.asSequence() }
    .filterNotNull()
    .maxOrNull()
