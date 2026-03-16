package com.gcaguilar.bizizaragoza.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json

@DependencyGraph(AppScope::class)
interface SharedGraph {
  val assistantIntentResolver: AssistantIntentResolver
  val datosBiziApi: DatosBiziApi
  val favoritesRepository: FavoritesRepository
  val googlePlacesApi: GooglePlacesApi
  val routeLauncher: RouteLauncher
  val settingsRepository: SettingsRepository
  val stationsRepository: StationsRepository
  val tripRepository: TripRepository
  val watchSyncBridge: WatchSyncBridge

  @SingleIn(AppScope::class)
  @Provides
  fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  @SingleIn(AppScope::class)
  @Provides
  fun provideJson(): Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
  }

  @SingleIn(AppScope::class)
  @Provides
  fun provideHttpClient(
    httpClientFactory: BiziHttpClientFactory,
    json: Json,
  ): HttpClient = httpClientFactory.create(json)

  @SingleIn(AppScope::class)
  @Provides
  fun provideBiziApi(
    appConfiguration: AppConfiguration,
    httpClient: HttpClient,
  ): BiziApi = CityBikesBiziApi(httpClient, appConfiguration)

  @SingleIn(AppScope::class)
  @Provides
  fun provideDatosBiziApi(
    httpClient: HttpClient,
  ): DatosBiziApi = DatosBiziApiImpl(httpClient)

  @SingleIn(AppScope::class)
  @Provides
  fun provideStationsRepository(
    implementation: StationsRepositoryImpl,
  ): StationsRepository = implementation

  @SingleIn(AppScope::class)
  @Provides
  fun provideFavoritesRepository(
    implementation: FavoritesRepositoryImpl,
  ): FavoritesRepository = implementation

  @SingleIn(AppScope::class)
  @Provides
  fun provideSettingsRepository(
    implementation: SettingsRepositoryImpl,
  ): SettingsRepository = implementation

  @SingleIn(AppScope::class)
  @Provides
  fun provideGooglePlacesApi(
    httpClient: HttpClient,
  ): GooglePlacesApi = GooglePlacesApiImpl(httpClient)

  @SingleIn(AppScope::class)
  @Provides
  fun provideTripRepository(
    implementation: TripRepositoryImpl,
  ): TripRepository = implementation

  @DependencyGraph.Factory
  fun interface Factory {
    fun create(@Includes platformBindings: PlatformBindings): SharedGraph
  }
}
