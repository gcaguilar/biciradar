package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.crypto.SecureKeyStore
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import okio.FileSystem

// ============================================================================
// EXISTING PLATFORM-LEVEL INTERFACES (kept for backward compatibility)
// ============================================================================

interface BiziHttpClientFactory {
  fun create(json: Json): HttpClient
}

interface StorageDirectoryProvider {
  val rootPath: String
}

interface LocationProvider {
  suspend fun currentLocation(): GeoPoint?
}

interface RouteLauncher {
  fun launch(station: Station)

  /** Launch walking directions to [destination] (from the user's current location or a given origin). */
  fun launchWalkToLocation(destination: GeoPoint)

  /** Launch cycling directions to [destination]. Falls back to walking when a platform does not override it. */
  fun launchBikeToLocation(destination: GeoPoint) {
    launchWalkToLocation(destination)
  }
}

interface AssistantIntentResolver {
  suspend fun resolve(
    action: AssistantAction,
    stationsState: StationsState,
    favoriteIds: Set<String>,
    searchRadiusMeters: Int,
  ): AssistantResolution
}

interface WatchSyncBridge {
  fun bindOnRemoteFavoritesChanged(listener: suspend () -> Unit) = Unit

  suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot)

  suspend fun latestFavorites(): FavoritesSyncSnapshot?
}

interface LocalNotifier {
  suspend fun hasPermission(): Boolean = true

  suspend fun requestPermission(): Boolean

  suspend fun notify(
    title: String,
    body: String,
  )
}

interface DatabaseProvider {
  fun provideDatabase(): BiciRadarDatabase?
}

interface DatabaseFactory {
  fun create(json: Json = Json { ignoreUnknownKeys = true }): BiciRadarDatabase?
}

// ============================================================================
// DEEP PLATFORM INTERFACES (granular composition)
// ============================================================================

/**
 * Deep interface for network platform services.
 */
interface NetworkPlatform {
  val httpClientFactory: BiziHttpClientFactory
  val remoteConfigProvider: RemoteConfigProvider get() = NoOpRemoteConfigProvider
}

/**
 * Deep interface for location platform services.
 */
interface LocationPlatform {
  val locationProvider: LocationProvider
  val mapSupport: MapSupport
}

/**
 * Deep interface for navigation platform services.
 */
interface NavigationPlatform {
  val routeLauncher: RouteLauncher
}

/**
 * Deep interface for crypto platform services.
 */
interface CryptoPlatform {
  val secureKeyStore: SecureKeyStore
}

/**
 * Deep interface for notification platform services.
 */
interface NotificationPlatform {
  val localNotifier: LocalNotifier
}

/**
 * Deep interface for experience platform services.
 */
interface ExperiencePlatform {
  val permissionPrompter: PermissionPrompter get() = NoOpPermissionPrompter
  val externalLinks: ExternalLinks get() = NoOpExternalLinks
  val reviewPrompter: ReviewPrompter get() = NoOpReviewPrompter
  val appUpdatePrompter: AppUpdatePrompter get() = NoOpAppUpdatePrompter
}

/**
 * Deep interface for sync platform services.
 */
interface SyncPlatform {
  val watchSyncBridge: WatchSyncBridge
}

/**
 * Deep interface for database platform services.
 */
interface DatabasePlatform {
  val databaseFactory: DatabaseFactory?
}

/**
 * Deep interface for storage platform services.
 */
interface StoragePlatform {
  val fileSystem: FileSystem
  val storageDirectoryProvider: StorageDirectoryProvider
}

// ============================================================================
// COMPOSITION ROOT
// ============================================================================

/**
 * Composition root for all platform services.
 *
 * PlatformBindings extends all focused platform interfaces to provide
 * a single access point.
 */
interface PlatformBindings :
  NetworkPlatform,
  LocationPlatform,
  NavigationPlatform,
  CryptoPlatform,
  NotificationPlatform,
  ExperiencePlatform,
  SyncPlatform,
  DatabasePlatform,
  StoragePlatform {
  val appConfiguration: AppConfiguration

  @AppVersion val appVersion: String
  val assistantIntentResolver: AssistantIntentResolver
  val crashlyticsReporter: CrashlyticsReporter get() = NoOpCrashlyticsReporter
  val logger: Logger get() = NoOpLogger

  @GoogleMapsApiKey val googleMapsApiKey: String?

  @OsVersion val osVersion: String

  @Platform val platform: String

  /** Called once after the [SharedGraph] has been created. */
  fun onGraphCreated(graph: SharedGraph) {}
}
