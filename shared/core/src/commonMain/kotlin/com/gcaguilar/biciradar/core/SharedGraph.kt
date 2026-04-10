package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.crypto.SecureKeyStore
import com.gcaguilar.biciradar.core.di.CoreBindings
import com.gcaguilar.biciradar.core.di.DatabaseBindings
import com.gcaguilar.biciradar.core.di.NetworkBindings
import com.gcaguilar.biciradar.core.di.TripGraph
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import com.gcaguilar.biciradar.core.geo.GeoApi
import com.gcaguilar.biciradar.core.geo.GeoSearchUseCase
import com.gcaguilar.biciradar.core.geo.InstallationIdentityRepository
import com.gcaguilar.biciradar.core.geo.RequestSigner
import com.gcaguilar.biciradar.core.geo.ReverseGeocodeUseCase
import com.gcaguilar.biciradar.core.geo.TokenManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import okio.FileSystem

/**
 * Grafo de dependencias principal de la aplicación.
 *
 * Este grafo está organizado usando Binding Containers por capa funcional:
 * - [CoreBindings]: CoroutineScope, Json
 * - [DatabaseBindings]: BiciRadarDatabase, StationsCacheManager
 * - [NetworkBindings]: HttpClient, BiziApi, GooglePlacesApi
 *
 * Además, soporta Graph Extensions para flujos con ciclo de vida independiente:
 * - [TripGraph]: Flujo de viaje/trip
 *
 * Los repositorios se registran automáticamente mediante @ContributesBinding.
 */
@DependencyGraph(
    AppScope::class,
    bindingContainers = [CoreBindings::class, DatabaseBindings::class, NetworkBindings::class]
)
interface SharedGraph {
    // ==================== ACCESSORS PÚBLICOS ====================

    // Casos de uso
    val changeCityUseCase: ChangeCityUseCase
    val isCityConfiguredUseCase: IsCityConfiguredUseCase
    val getCachedStationSnapshot: GetCachedStationSnapshot
    val getFavoriteStations: GetFavoriteStations
    val getNearestStations: GetNearestStations
    val getStationStatus: GetStationStatus
    val getSuggestedAlternativeStation: GetSuggestedAlternativeStation
    val refreshStationDataIfNeeded: RefreshStationDataIfNeeded
    val savedPlaceAlertsEvaluator: SavedPlaceAlertsEvaluator
    val startStationMonitoring: StartStationMonitoring
    val stopStationMonitoring: StopStationMonitoring

    // Repositorios (todos con @ContributesBinding)
    val environmentalRepository: EnvironmentalRepository
    val engagementRepository: EngagementRepository
    val favoritesRepository: FavoritesRepository
    val savedPlaceAlertsRepository: SavedPlaceAlertsRepository
    val settingsRepository: SettingsRepository
    val surfaceMonitoringRepository: SurfaceMonitoringRepository
    val surfaceSnapshotRepository: SurfaceSnapshotRepository
    val stationsRepository: StationsRepository

    // APIs y servicios de plataforma
    val assistantIntentResolver: AssistantIntentResolver
    val datosBiziApi: DatadosBiziApi
    val geoApi: GeoApi
    val geoSearchUseCase: GeoSearchUseCase
    val googlePlacesApi: GooglePlacesApi
    val reverseGeocodeUseCase: ReverseGeocodeUseCase
    val routeLauncher: RouteLauncher
    val watchSyncBridge: WatchSyncBridge

    /**
     * Repositorio de trip global (legado).
     * Para nuevos flujos, usar [tripGraphFactory] para crear un grafo aislado.
     */
    val tripRepository: TripRepository

    /**
     * Factory para crear grafos de trip aislados.
     * Usar esto para iniciar un nuevo trip con su propio ciclo de vida.
     */
    val tripGraphFactory: TripGraph.Factory

    // ==================== PROVIDES MANUALES ====================
    // Geo bindings - requieren configuración específica

    @SingleIn(AppScope::class)
    @Provides
    fun provideInstallationIdentityRepository(
        httpClient: HttpClient,
        json: Json,
        fileSystem: FileSystem,
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

    // ==================== FACTORY ====================

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Includes platformBindings: PlatformBindings): SharedGraph
    }
}