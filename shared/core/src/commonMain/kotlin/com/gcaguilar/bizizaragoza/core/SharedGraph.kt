package com.gcaguilar.bizizaragoza.core

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

@DependencyGraph
interface SharedGraph {
  val assistantIntentResolver: AssistantIntentResolver
  val favoritesRepository: FavoritesRepository
  val geminiPromptService: GeminiPromptService
  val routeLauncher: RouteLauncher
  val settingsRepository: SettingsRepository
  val stationsRepository: StationsRepository
  val watchSyncBridge: WatchSyncBridge

  @Provides
  fun provideJson(): Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
  }

  @Provides
  fun provideHttpClient(
    httpClientFactory: BiziHttpClientFactory,
    json: Json,
  ): HttpClient = httpClientFactory.create(json)

  @Provides
  fun provideBiziApi(
    appConfiguration: AppConfiguration,
    httpClient: HttpClient,
  ): BiziApi = CityBikesBiziApi(httpClient, appConfiguration)

  @Provides
  fun provideStationsRepository(
    implementation: StationsRepositoryImpl,
  ): StationsRepository = implementation

  @Provides
  fun provideFavoritesRepository(
    implementation: FavoritesRepositoryImpl,
  ): FavoritesRepository = implementation

  @Provides
  fun provideSettingsRepository(
    implementation: SettingsRepositoryImpl,
  ): SettingsRepository = implementation

  @Provides
  fun provideGeminiPromptService(
    implementation: GeminiPromptServiceImpl,
  ): GeminiPromptService = implementation

  @DependencyGraph.Factory
  fun interface Factory {
    fun create(@Includes platformBindings: PlatformBindings): SharedGraph
  }
}
