package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.di.CoreBindings
import com.gcaguilar.biciradar.core.di.DatabaseBindings
import com.gcaguilar.biciradar.core.di.GeoBindings
import com.gcaguilar.biciradar.core.di.NetworkBindings
import com.gcaguilar.biciradar.core.di.OnboardingGraph
import com.gcaguilar.biciradar.core.di.TripGraph
import com.gcaguilar.biciradar.core.geo.GeoApi
import com.gcaguilar.biciradar.core.geo.GeoSearchUseCase
import com.gcaguilar.biciradar.core.geo.ReverseGeocodeUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes

/**
 * Grafo de dependencias principal de la aplicación.
 *
 * Este grafo está organizado usando Binding Containers por capa funcional:
 * - [CoreBindings]: CoroutineScope, Json
 * - [DatabaseBindings]: BiciRadarDatabase, StationsCacheManager
 * - [NetworkBindings]: HttpClient, BiziApi, GooglePlacesApi
 * - [GeoBindings]: GeoApi, TokenManager, InstallationIdentityRepository
 *
 * Además, soporta Graph Extensions para flujos con ciclo de vida independiente:
 * - [TripGraph]: Flujo de viaje/trip
 * - [OnboardingGraph]: Flujo de onboarding
 *
 * Los repositorios se registran automáticamente mediante @ContributesBinding.
 */
@DependencyGraph(
    AppScope::class,
    bindingContainers = [CoreBindings::class, DatabaseBindings::class, NetworkBindings::class, GeoBindings::class]
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

    /**
     * Factory para crear grafos de onboarding aislados.
     * Usar esto cuando se necesite un contexto de onboarding independiente.
     */
    val onboardingGraphFactory: OnboardingGraph.Factory

    // ==================== FACTORY ====================

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Includes platformBindings: PlatformBindings): SharedGraph
    }
}