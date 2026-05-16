package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.di.OnboardingGraph
import com.gcaguilar.biciradar.core.di.TripGraph
import com.gcaguilar.biciradar.core.geo.GeoApi
import com.gcaguilar.biciradar.core.geo.GeoSearchUseCase
import com.gcaguilar.biciradar.core.geo.ReverseGeocodeUseCase

/**
 * Bundled use cases for querying stations.
 *
 * Deep interface: 6 methods behind a focused seam. Callers get all station lookup
 * operations without depending on the full [StationsFeatureDeps] surface.
 */
interface StationsQueryUseCases {
  val getCachedStationSnapshot: GetCachedStationSnapshot
  val getFavoriteStations: GetFavoriteStations
  val getNearestStations: GetNearestStations
  val getStationStatus: GetStationStatus
  val getSuggestedAlternativeStation: GetSuggestedAlternativeStation
  val getSuggestedStations: GetSuggestedStations
  val filterStationsByQuery: FilterStationsByQuery
  val findStationMatchingQuery: FindStationMatchingQuery
  val findNearestStation: FindNearestStation
  val findNearestStationWithBikes: FindNearestStationWithBikes
  val findNearestStationWithSlots: FindNearestStationWithSlots
  val findStationById: FindStationById
  val getNearbyStationList: GetNearbyStationList
}

/**
 * Bundled use cases for refreshing station data.
 *
 * Deep interface: 2 methods. All refresh logic concentrated in one seam.
 */
interface StationsRefreshUseCases {
  val refreshStationDataIfNeeded: RefreshStationDataIfNeeded
  val refreshStationAvailability: RefreshStationAvailability
}

/**
 * Bundled use cases for station monitoring.
 *
 * Deep interface: 2 methods. Start and stop monitoring behind a small seam.
 */
interface StationsMonitoringUseCases {
  val startStationMonitoring: StartStationMonitoring
  val stopStationMonitoring: StopStationMonitoring
}

/**
 * Bundled use cases for city configuration.
 *
 * Deep interface: 2 methods. City selection and configuration in one seam.
 */
interface CityConfigurationUseCases {
  val changeCityUseCase: ChangeCityUseCase
  val isCityConfiguredUseCase: IsCityConfiguredUseCase
}

/**
 * Dependencias del feature de estaciones.
 *
 * Cubre consulta, refresco, monitorización y observación de estaciones.
 * Consumidores: shells de iOS, watchOS, Android y sus extensiones (widgets, Siri).
 *
 * Station operations are split into focused sub-interfaces for depth:
 * callers that only need queries depend on [StationsQueryUseCases],
 * not the full [StationsFeatureDeps].
 */
interface StationsFeatureDeps :
  StationsQueryUseCases,
  StationsRefreshUseCases,
  StationsMonitoringUseCases,
  CityConfigurationUseCases {
  val cityRegistry: CityRegistry
  val observeStationsState: ObserveStationsState
  val datosBiziApi: DatosBiziApi
}

/**
 * Dependencias del feature de favoritos.
 *
 * Cubre mutaciones, sincronización con el reloj y alertas de lugar guardado.
 * Consumidores: shells de iOS, watchOS, Android.
 */
interface FavoritesFeatureDeps {
  val getFavoriteStationList: GetFavoriteStationList
  val toggleFavoriteStation: ToggleFavoriteStation
  val syncFavoritesFromPeer: SyncFavoritesFromPeer
  val observeFavorites: ObserveFavorites
  val savedPlaceAlertsEvaluator: SavedPlaceAlertsEvaluator
  val evaluateSavedPlaceAlerts: EvaluateSavedPlaceAlerts
  val watchSyncBridge: WatchSyncBridge
}

/**
 * Dependencias del feature de sesión y configuración.
 *
 * Cubre bootstrap de sesión, ciudad seleccionada, asistente y ajustes de usuario.
 * Consumidores: shells de iOS, watchOS, Android y la UI compartida.
 */
interface SessionFeatureDeps {
  val bootstrapSession: BootstrapSession
  val getCurrentCity: GetCurrentCity
  val updateSelectedCity: UpdateSelectedCity
  val resolveAssistantIntent: ResolveAssistantIntent
  val assistantIntentResolver: AssistantIntentResolver

  /** Expuesto únicamente para late-wiring de IOSRouteLauncher/DesktopRouteLauncher. */
  val settingsRepository: SettingsRepository
  val observeSettings: ObserveSettings
  val getNearbyStationList: GetNearbyStationList
}

/**
 * Dependencias del feature de superficie (snapshots de la pantalla del reloj/widget).
 */
interface SurfaceFeatureDeps {
  val observeSurfaceSnapshot: ObserveSurfaceSnapshot
  val observeSurfaceMonitoring: ObserveSurfaceMonitoring
  val refreshWidgetDataUseCase: RefreshWidgetDataUseCase
}

/**
 * Dependencias de plataforma y servicios de soporte.
 *
 * Cubre geo-búsqueda, navegación, logging y config remota.
 */
interface PlatformFeatureDeps {
  val geoApi: GeoApi
  val geoSearchUseCase: GeoSearchUseCase
  val googlePlacesApi: GooglePlacesApi
  val logger: Logger
  val reverseGeocodeUseCase: ReverseGeocodeUseCase
  val routeLauncher: RouteLauncher
  val remoteConfigProvider: RemoteConfigProvider
}

/**
 * Dependencias del feature de trip/viaje.
 *
 * Contiene únicamente la factory para crear un grafo de trip aislado.
 * El [tripRepository] legado se mantiene por compatibilidad mientras se migra
 * al patrón de scoped graph; ver [tripGraphFactory].
 *
 * @see TripGraph
 */
interface TripFeatureDeps {
  /**
   * Repositorio de trip global (legado).
   *
   * **Deprecado**: usar [tripGraphFactory] para crear un grafo con ciclo de vida acotado.
   * Este accessor se eliminará una vez que todos los consumidores migren a [tripGraphFactory].
   */
  @Deprecated(
    message = "Usar tripGraphFactory para crear un TripGraph con ciclo de vida acotado.",
    replaceWith = ReplaceWith("tripGraphFactory.createTripGraph().tripRepository"),
  )
  val tripRepository: TripRepository

  /**
   * Factory para crear grafos de trip aislados.
   * Usar para iniciar un nuevo trip con su propio ciclo de vida.
   */
  val tripGraphFactory: TripGraph.Factory
}

/**
 * Dependencias del feature de onboarding.
 */
interface OnboardingFeatureDeps {
  val onboardingGraphFactory: OnboardingGraph.Factory
}
