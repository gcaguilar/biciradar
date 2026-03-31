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
  val datosBiziApi: DatosBiziApi
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
  ): BiciRadarDatabase? = databaseFactory?.create()

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
  fun provideStationsRepository(implementation: StationsRepositoryImpl): StationsRepository = implementation

  @SingleIn(AppScope::class)
  @Provides
  fun provideFavoritesRepository(implementation: FavoritesRepositoryImpl): FavoritesRepository = implementation

  @SingleIn(AppScope::class)
  @Provides
  fun provideEngagementRepository(implementation: EngagementRepositoryImpl): EngagementRepository = implementation

  @SingleIn(AppScope::class)
  @Provides
  fun provideSavedPlaceAlertsRepository(implementation: SavedPlaceAlertsRepositoryImpl): SavedPlaceAlertsRepository =
    implementation

  @SingleIn(AppScope::class)
  @Provides
  fun provideSettingsRepository(implementation: SettingsRepositoryImpl): SettingsRepository = implementation

  @SingleIn(AppScope::class)
  @Provides
  fun provideSurfaceSnapshotRepository(implementation: SurfaceSnapshotRepositoryImpl): SurfaceSnapshotRepository = implementation

  @SingleIn(AppScope::class)
  @Provides
  fun provideSurfaceMonitoringRepository(implementation: SurfaceMonitoringRepositoryImpl): SurfaceMonitoringRepository = implementation

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
    requestSigner: RequestSigner,
  ): GeoApi = GeoApiImpl(
    httpClient = httpClient,
    json = json,
    tokenManager = tokenManager,
    identityRepo = identityRepo,
    requestSigner = requestSigner,
  )

  @SingleIn(AppScope::class)
  @Provides
  fun provideGeoSearchUseCase(
    geoApi: GeoApi,
    googlePlacesApi: GooglePlacesApi,
    @GoogleMapsApiKey googleMapsApiKey: String?,
  ): GeoSearchUseCase = GeoSearchUseCase(
    geoApi = geoApi,
    googlePlacesApi = googlePlacesApi,
    googleMapsApiKey = googleMapsApiKey,
  )

  @SingleIn(AppScope::class)
  @Provides
  fun provideReverseGeocodeUseCase(
    geoApi: GeoApi,
    googlePlacesApi: GooglePlacesApi,
    @GoogleMapsApiKey googleMapsApiKey: String?,
  ): ReverseGeocodeUseCase = ReverseGeocodeUseCase(
    geoApi = geoApi,
    googlePlacesApi = googlePlacesApi,
    googleMapsApiKey = googleMapsApiKey,
  )

  @DependencyGraph.Factory
  fun interface Factory {
    fun create(@Includes platformBindings: PlatformBindings): SharedGraph
  }
}
