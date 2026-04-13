package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

enum class BiziApiStrategy {
  Gbfs,
  CityBikes,
}

@Serializable
data class CityCatalogEntry(
  val id: String,
  val displayName: String,
  val gbfsDiscoveryUrl: String,
  val defaultLatitude: Double,
  val defaultLongitude: Double,
  val supportsUsagePatterns: Boolean,
  val supportsEbikes: Boolean,
  val apiStrategy: BiziApiStrategy = BiziApiStrategy.Gbfs,
)

interface CityRegistry {
  suspend fun availableCities(): List<CityCatalogEntry>

  suspend fun cityById(id: String): CityCatalogEntry?

  fun fallbackCities(): List<CityCatalogEntry>
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class CityRegistryImpl(
  private val remoteConfigProvider: RemoteConfigProvider,
  private val json: Json,
  private val logger: Logger,
) : CityRegistry {
  @kotlin.concurrent.Volatile
  private var cachedRemoteCities: List<CityCatalogEntry>? = null

  override suspend fun availableCities(): List<CityCatalogEntry> = cachedRemoteCities ?: loadRemoteOrFallback()

  override suspend fun cityById(id: String): CityCatalogEntry? =
    availableCities().firstOrNull { it.id == id } ?: fallbackCities().firstOrNull { it.id == id }

  override fun fallbackCities(): List<CityCatalogEntry> = cityFallbackCatalog()

  private suspend fun loadRemoteOrFallback(): List<CityCatalogEntry> {
    val remoteCatalog =
      runCatching {
        remoteConfigProvider.getString(CITY_REGISTRY_REMOTE_CONFIG_KEY)
      }.onFailure { error ->
        logger.warn(TAG, "Failed to fetch city catalog from remote config", error)
      }.getOrNull()

    val decoded =
      remoteCatalog
        ?.takeIf { it.isNotBlank() }
        ?.let { payload ->
          runCatching {
            json.decodeFromString<List<CityCatalogEntry>>(payload)
          }.onFailure { error ->
            logger.warn(TAG, "Failed to decode remote city catalog; using fallback", error)
          }.getOrNull()
        }?.filter { it.id.isNotBlank() && it.displayName.isNotBlank() && it.gbfsDiscoveryUrl.isNotBlank() }
        ?.distinctBy { it.id }

    val resolved = decoded?.takeIf { it.isNotEmpty() } ?: fallbackCities()
    cachedRemoteCities = resolved
    return resolved
  }

  private companion object {
    const val CITY_REGISTRY_REMOTE_CONFIG_KEY = "city_registry_catalog_json"
    const val TAG = "CityRegistry"
  }
}

fun cityFallbackCatalog(): List<CityCatalogEntry> =
  City.entries.map { city ->
    CityCatalogEntry(
      id = city.id,
      displayName = city.displayName,
      gbfsDiscoveryUrl = city.gbfsDiscoveryUrl,
      defaultLatitude = city.defaultLatitude,
      defaultLongitude = city.defaultLongitude,
      supportsUsagePatterns = city.supportsUsagePatterns,
      supportsEbikes = city.supportsEbikes,
      apiStrategy = city.apiStrategy(),
    )
  }

fun City.apiStrategy(): BiziApiStrategy =
  when (this) {
    City.ZARAGOZA -> BiziApiStrategy.CityBikes
    else -> BiziApiStrategy.Gbfs
  }
