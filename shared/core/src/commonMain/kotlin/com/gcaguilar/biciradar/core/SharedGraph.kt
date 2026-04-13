package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.di.OnboardingGraph
import com.gcaguilar.biciradar.core.di.TripGraph
import com.gcaguilar.biciradar.core.geo.GeoApi
import com.gcaguilar.biciradar.core.geo.GeoSearchUseCase
import com.gcaguilar.biciradar.core.geo.ReverseGeocodeUseCase

/**
 * Interfaz base de dependencias principal de la aplicación.
 *
 * Define todos los accessors de repositorios, use cases y servicios disponibles
 * para los consumidores del grafo. Las implementaciones concretas con @DependencyGraph
 * se declaran en los módulos hoja (shared/mobile-ui, wearApp) para que Metro pueda
 * recoger todas las contribuciones (@ContributesBinding, @ContributesIntoMap) visibles
 * en cada módulo de compilación.
 *
 * @see CoreGraph — grafo concreto para wearApp y tests (en shared/core/di)
 */
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

  // Station query / selection use cases (shared logic, consumed by iOS + watchOS extensions)
  val getSuggestedStations: GetSuggestedStations
  val filterStationsByQuery: FilterStationsByQuery
  val findStationMatchingQuery: FindStationMatchingQuery
  val findNearestStation: FindNearestStation
  val findNearestStationWithBikes: FindNearestStationWithBikes
  val findNearestStationWithSlots: FindNearestStationWithSlots
  val evaluateSavedPlaceAlerts: EvaluateSavedPlaceAlerts
  val cityRegistry: CityRegistry

  // Session / city / assistant use cases
  val bootstrapSession: BootstrapSession
  val getCurrentCity: GetCurrentCity
  val updateSelectedCity: UpdateSelectedCity
  val findStationById: FindStationById
  val getFavoriteStationList: GetFavoriteStationList
  val getNearbyStationList: GetNearbyStationList
  val resolveAssistantIntent: ResolveAssistantIntent

  // Reactive observation (Android ViewModels / Services)
  val observeStationsState: ObserveStationsState
  val observeFavorites: ObserveFavorites
  val observeSurfaceSnapshot: ObserveSurfaceSnapshot
  val observeSurfaceMonitoring: ObserveSurfaceMonitoring
  val observeSettings: ObserveSettings

  // Favorites mutations
  val toggleFavoriteStation: ToggleFavoriteStation
  val syncFavoritesFromPeer: SyncFavoritesFromPeer

  // Station data mutations
  val refreshStationAvailability: RefreshStationAvailability

  // Repositorios (todos con @ContributesBinding)
  // NOTA: settingsRepository se mantiene aquí únicamente para el late-wiring de
  // IOSRouteLauncher / DesktopRouteLauncher en onGraphCreated(). El resto de repos
  // han sido reemplazados por use cases y ya no se exponen en esta interfaz.
  val settingsRepository: SettingsRepository

  // APIs y servicios de plataforma
  val assistantIntentResolver: AssistantIntentResolver
  val datosBiziApi: DatosBiziApi
  val geoApi: GeoApi
  val geoSearchUseCase: GeoSearchUseCase
  val googlePlacesApi: GooglePlacesApi
  val logger: Logger
  val reverseGeocodeUseCase: ReverseGeocodeUseCase
  val routeLauncher: RouteLauncher
  val remoteConfigProvider: RemoteConfigProvider
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
}
