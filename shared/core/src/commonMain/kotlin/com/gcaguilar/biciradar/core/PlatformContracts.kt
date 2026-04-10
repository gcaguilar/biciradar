package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.crypto.SecureKeyStore
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import okio.FileSystem

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
  suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot)
  suspend fun latestFavorites(): FavoritesSyncSnapshot?
}

interface LocalNotifier {
  suspend fun hasPermission(): Boolean = true
  suspend fun requestPermission(): Boolean
  suspend fun notify(title: String, body: String)
}

interface DatabaseProvider {
  fun provideDatabase(): BiciRadarDatabase?
}

interface DatabaseFactory {
  fun create(json: Json = Json { ignoreUnknownKeys = true }): BiciRadarDatabase?
}

interface PlatformBindings {
  val appConfiguration: AppConfiguration
  @AppVersion val appVersion: String
  val assistantIntentResolver: AssistantIntentResolver
  val databaseFactory: DatabaseFactory?
  val fileSystem: FileSystem
  @GoogleMapsApiKey val googleMapsApiKey: String?
  val httpClientFactory: BiziHttpClientFactory
  val localNotifier: LocalNotifier
  val locationProvider: LocationProvider
  val mapSupport: MapSupport
  @OsVersion val osVersion: String
  @Platform val platform: String
  val routeLauncher: RouteLauncher
  val secureKeyStore: SecureKeyStore
  val storageDirectoryProvider: StorageDirectoryProvider
  val watchSyncBridge: WatchSyncBridge
  val permissionPrompter: PermissionPrompter get() = NoOpPermissionPrompter
  val externalLinks: ExternalLinks get() = NoOpExternalLinks
  val reviewPrompter: ReviewPrompter get() = NoOpReviewPrompter
  val appUpdatePrompter: AppUpdatePrompter get() = NoOpAppUpdatePrompter

  /** Called once after the [SharedGraph] has been created. Implementations may use this to wire
   *  graph-provided dependencies (e.g. [SettingsRepository]) into platform-specific objects that
   *  were constructed before the graph existed. */
  fun onGraphCreated(graph: SharedGraph) {}
}
