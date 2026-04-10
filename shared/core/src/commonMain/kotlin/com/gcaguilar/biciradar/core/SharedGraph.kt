package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.crypto.SecureKeyStore
import com.gcaguilar.biciradar.core.geo.GeoApi
import com.gcaguilar.biciradar.core.geo.GeoApiImpl
import com.gcaguilar.biciradar.core.geo.GeoSearchUseCase
import com.gcaguilar.biciradar.core.geo.InstallationIdentityRepository
import com.gcaguilar.biciradar.core.geo.RequestSigner
import com.gcaguilar.biciradar.core.geo.ReverseGeocodeUseCase
import com.gcaguilar.biciradar.core.geo.TokenManager
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
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
  val changeCityUseCase: ChangeCityUseCase
  val isCityConfiguredUseCase: IsCityConfiguredUseCase
  val datosBiziApi: DatosBiziApi
  val environmentalRepository: EnvironmentalRepository
  val engagementRepository: EngagementRepository
  val favoritesRepository: FavoritesRepository
  val getCachedStationSnapshot: GetCachedStationSnapshot
  val getFavoriteStations: GetFavoriteStations
  val getNearestStations: GetNearestStations
  val getStationStatus: GetStationStatus
  val getSuggestedAlternativeStation: GetSuggestedAlternativeStation
  val geoApi: GeoApi
  val geoSearchUseCase: GeoSearchUseCase
  val googlePlacesApi: GooglePlacesApi
  val refreshStationDataIfNeeded: RefreshStationDataIfNeeded
  val reverseGeocodeUseCase: ReverseGeocodeUseCase
  val savedPlaceAlertsRepository: SavedPlaceAlertsRepository
  val savedPlaceAlertsEvaluator: SavedPlaceAlertsEvaluator
  val routeLauncher: RouteLauncher
  val settingsRepository: SettingsRepository
  val startStationMonitoring: StartStationMonitoring
  val stopStationMonitoring: StopStationMonitoring
  val surfaceMonitoringRepository: SurfaceMonitoringRepository
  val surfaceSnapshotRepository: SurfaceSnapshotRepository
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
  fun provideDatabase(
    databaseFactory: DatabaseFactory?,
    json: Json,
  ): BiciRadarDatabase? = databaseFactory?.create(json)

  @SingleIn(AppScope::class)
  @Provides
  fun provideBiziApi(
    appConfiguration: AppConfiguration,
    httpClient: HttpClient,
    settingsRepository: SettingsRepository,
  ): BiziApi = GbfsBiziApi(httpClient, appConfiguration, settingsRepository)

  @SingleIn(AppScope::class)
  @Provides
  fun provideDatosBiziApi(httpClient: HttpClient): DatosBiziApi = DatosBiziApiImpl(httpClient)

  @SingleIn(AppScope::class)
  @Provides
  fun provideStationsRemoteDataSource(biziApi: BiziApi): StationsRemoteDataSource =
    StationsRemoteDataSourceImpl(biziApi)

  @SingleIn(AppScope::class)
  @Provides
  fun provideStationsCacheManager(
    database: BiciRadarDatabase?,
  ): StationsCacheManager = if (database != null) {
    StationsCacheManagerImpl(database, StationCacheStore(database))
  } else {
    NoOpStationsCacheManager()
  }

  // Repositorios se proveen automáticamente mediante @ContributesBinding:
  // - StationsRepository (@ContributesBinding(AppScope::class))
  // - FavoritesRepository (@ContributesBinding(AppScope::class))
  // - EngagementRepository (@ContributesBinding(AppScope::class))
  // - EnvironmentalRepository (@ContributesBinding(AppScope::class))
  // - SavedPlaceAlertsRepository (@ContributesBinding(AppScope::class))
  // - SettingsRepository (@ContributesBinding(AppScope::class))
  // - SurfaceSnapshotRepository (@ContributesBinding(AppScope::class))
  // - SurfaceMonitoringRepository (@ContributesBinding(AppScope::class))
  // - TripRepository (@ContributesBinding(AppScope::class))

  @SingleIn(AppScope::class)
  @Provides
  fun provideGooglePlacesApi(httpClient: HttpClient): GooglePlacesApi = GooglePlacesApiImpl(httpClient)

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

  // GeoApi, GeoSearchUseCase y ReverseGeocodeUseCase se proveen automáticamente
  // por Metro mediante sus constructores anotados con @Inject

  @DependencyGraph.Factory
  fun interface Factory {
    fun create(@Includes platformBindings: PlatformBindings): SharedGraph
  }
}
