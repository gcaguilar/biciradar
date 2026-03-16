package com.gcaguilar.bizizaragoza.core

import com.gcaguilar.bizizaragoza.core.crypto.SecureKeyStore
import com.gcaguilar.bizizaragoza.core.geo.GeoApi
import com.gcaguilar.bizizaragoza.core.geo.GeoApiImpl
import com.gcaguilar.bizizaragoza.core.geo.GeoSearchUseCase
import com.gcaguilar.bizizaragoza.core.geo.InstallationIdentityRepository
import com.gcaguilar.bizizaragoza.core.geo.RequestSigner
import com.gcaguilar.bizizaragoza.core.geo.ReverseGeocodeUseCase
import com.gcaguilar.bizizaragoza.core.geo.TokenManager
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
  val geoApi: GeoApi
  val geoSearchUseCase: GeoSearchUseCase
  val googlePlacesApi: GooglePlacesApi
  val reverseGeocodeUseCase: ReverseGeocodeUseCase
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
  fun provideHttpClient(httpClientFactory: BiziHttpClientFactory, json: Json): HttpClient =
    httpClientFactory.create(json)

  @SingleIn(AppScope::class)
  @Provides
  fun provideBiziApi(appConfiguration: AppConfiguration, httpClient: HttpClient): BiziApi =
    CityBikesBiziApi(httpClient, appConfiguration)

  @SingleIn(AppScope::class)
  @Provides
  fun provideDatosBiziApi(httpClient: HttpClient): DatosBiziApi = DatosBiziApiImpl(httpClient)

  @SingleIn(AppScope::class)
  @Provides
  fun provideStationsRepository(implementation: StationsRepositoryImpl): StationsRepository = implementation

  @SingleIn(AppScope::class)
  @Provides
  fun provideFavoritesRepository(implementation: FavoritesRepositoryImpl): FavoritesRepository = implementation

  @SingleIn(AppScope::class)
  @Provides
  fun provideSettingsRepository(implementation: SettingsRepositoryImpl): SettingsRepository = implementation

  @SingleIn(AppScope::class)
  @Provides
  fun provideGooglePlacesApi(httpClient: HttpClient): GooglePlacesApi = GooglePlacesApiImpl(httpClient)

  @SingleIn(AppScope::class)
  @Provides
  fun provideTripRepository(implementation: TripRepositoryImpl): TripRepository = implementation

  // ------------------------------------------------------------------
  // Geo / datosbizi.com
  // ------------------------------------------------------------------

  @SingleIn(AppScope::class)
  @Provides
  fun provideInstallationIdentityRepository(
    httpClient: HttpClient,
    json: Json,
    fileSystem: okio.FileSystem,
    storageDirectoryProvider: StorageDirectoryProvider,
    secureKeyStore: SecureKeyStore,
    @AppVersion appVersion: String,
    @OsVersion osVersion: String,
    @Platform platform: String,
  ): InstallationIdentityRepository = InstallationIdentityRepository(
    httpClient = httpClient,
    json = json,
    fileSystem = fileSystem,
    storageDirectoryProvider = storageDirectoryProvider,
    secureKeyStore = secureKeyStore,
    appVersion = appVersion,
    osVersion = osVersion,
    platform = platform,
  )

  @SingleIn(AppScope::class)
  @Provides
  fun provideTokenManager(
    httpClient: HttpClient,
    json: Json,
    identityRepo: InstallationIdentityRepository,
  ): TokenManager = TokenManager(
    httpClient = httpClient,
    json = json,
    identityRepo = identityRepo,
  )

  @SingleIn(AppScope::class)
  @Provides
  fun provideRequestSigner(
    identityRepo: InstallationIdentityRepository,
  ): RequestSigner = RequestSigner(identityRepo = identityRepo)

  @SingleIn(AppScope::class)
  @Provides
  fun provideGeoApi(
    httpClient: HttpClient,
    json: Json,
    tokenManager: TokenManager,
    identityRepo: InstallationIdentityRepository,
  ): GeoApi = GeoApiImpl(
    httpClient = httpClient,
    json = json,
    tokenManager = tokenManager,
    identityRepo = identityRepo,
  )

  @SingleIn(AppScope::class)
  @Provides
  fun provideGeoSearchUseCase(geoApi: GeoApi): GeoSearchUseCase = GeoSearchUseCase(geoApi)

  @SingleIn(AppScope::class)
  @Provides
  fun provideReverseGeocodeUseCase(geoApi: GeoApi): ReverseGeocodeUseCase = ReverseGeocodeUseCase(geoApi)

  @DependencyGraph.Factory
  fun interface Factory {
    fun create(@Includes platformBindings: PlatformBindings): SharedGraph
  }
}
