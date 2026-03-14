package com.gcaguilar.bizizaragoza.core

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
  suspend fun requestPermission(): Boolean
  suspend fun notify(title: String, body: String)
}

interface PlatformBindings {
  val appConfiguration: AppConfiguration
  val assistantIntentResolver: AssistantIntentResolver
  val fileSystem: FileSystem
  val googleMapsApiKey: String?
  val httpClientFactory: BiziHttpClientFactory
  val localNotifier: LocalNotifier
  val locationProvider: LocationProvider
  val mapSupport: MapSupport
  val routeLauncher: RouteLauncher
  val storageDirectoryProvider: StorageDirectoryProvider
  val watchSyncBridge: WatchSyncBridge
}
