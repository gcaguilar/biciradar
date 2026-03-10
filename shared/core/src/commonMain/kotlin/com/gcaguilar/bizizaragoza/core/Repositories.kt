package com.gcaguilar.bizizaragoza.core

import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

interface StationsRepository {
  val state: StateFlow<StationsState>
  suspend fun refresh()
  fun stationById(stationId: String): Station?
}

interface FavoritesRepository {
  val favoriteIds: StateFlow<Set<String>>
  suspend fun bootstrap()
  suspend fun toggle(stationId: String)
  fun isFavorite(stationId: String): Boolean
}

interface GeminiPromptService {
  suspend fun prompt(request: GeminiPromptRequest): GeminiPromptResponse
}

@Inject
class StationsRepositoryImpl(
  private val biziApi: BiziApi,
  private val appConfiguration: AppConfiguration,
  private val locationProvider: LocationProvider,
) : StationsRepository {
  private val mutableState = MutableStateFlow(StationsState(isLoading = true))

  override val state: StateFlow<StationsState> = mutableState.asStateFlow()

  override suspend fun refresh() {
    mutableState.update { it.copy(isLoading = true, errorMessage = null) }
    val currentLocation = locationProvider.currentLocation()
    val origin = currentLocation ?: appConfiguration.defaultLocation()
    runCatching { biziApi.fetchStations(origin).sortedBy { station -> station.distanceMeters } }
      .onSuccess { stations ->
        mutableState.value = StationsState(
          stations = stations,
          isLoading = false,
          userLocation = currentLocation,
        )
      }
      .onFailure { error ->
        mutableState.update {
          it.copy(
            isLoading = false,
            errorMessage = error.message ?: "No se pudo cargar Bizi Zaragoza.",
            userLocation = currentLocation,
          )
        }
      }
  }

  override fun stationById(stationId: String): Station? = mutableState.value.stations.firstOrNull {
    it.id == stationId
  }
}

@Inject
class FavoritesRepositoryImpl(
  private val fileSystem: FileSystem,
  private val json: Json,
  private val storageDirectoryProvider: StorageDirectoryProvider,
  private val watchSyncBridge: WatchSyncBridge,
) : FavoritesRepository {
  private val mutableFavoriteIds = MutableStateFlow(emptySet<String>())
  private var bootstrapped = false

  override val favoriteIds: StateFlow<Set<String>> = mutableFavoriteIds.asStateFlow()

  override suspend fun bootstrap() {
    if (bootstrapped) return
    val snapshotPath = favoritesPath()
    val localFavoriteIds = if (fileSystem.exists(snapshotPath)) {
      val snapshot = json.decodeFromString<FavoritesSnapshot>(fileSystem.read(snapshotPath) { readUtf8() })
      snapshot.favoriteIds
    } else {
      emptySet()
    }
    val remoteFavoriteIds = watchSyncBridge.latestFavoriteIds().orEmpty()
    val mergedFavoriteIds = localFavoriteIds + remoteFavoriteIds
    mutableFavoriteIds.value = mergedFavoriteIds
    if (mergedFavoriteIds.isNotEmpty()) {
      persist(mergedFavoriteIds)
      watchSyncBridge.pushFavoriteIds(mergedFavoriteIds)
    }
    bootstrapped = true
  }

  override suspend fun toggle(stationId: String) {
    if (!bootstrapped) bootstrap()
    val updated = mutableFavoriteIds.value.toMutableSet().apply {
      if (!add(stationId)) remove(stationId)
    }.toSet()
    mutableFavoriteIds.value = updated
    persist(updated)
    watchSyncBridge.pushFavoriteIds(updated)
  }

  override fun isFavorite(stationId: String): Boolean = mutableFavoriteIds.value.contains(stationId)

  private fun favoritesPath() = "${storageDirectoryProvider.rootPath}/favorites.json".toPath()

  private suspend fun persist(favoriteIds: Set<String>) {
    val path = favoritesPath()
    fileSystem.createDirectories(path.parent!!)
    fileSystem.write(path) {
      writeUtf8(json.encodeToString(FavoritesSnapshot(favoriteIds)))
    }
  }
}

@Inject
class GeminiPromptServiceImpl(
  private val appConfiguration: AppConfiguration,
  private val httpClient: HttpClient,
) : GeminiPromptService {
  override suspend fun prompt(request: GeminiPromptRequest): GeminiPromptResponse {
    return httpClient.post("${appConfiguration.geminiProxyBaseUrl}/api/v1/gemini/prompt") {
      contentType(ContentType.Application.Json)
      setBody(request)
    }.body()
  }
}

@Serializable
internal data class FavoritesSnapshot(
  val favoriteIds: Set<String>,
)
