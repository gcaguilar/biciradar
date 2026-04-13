package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.di.OnboardingGraph
import com.gcaguilar.biciradar.core.di.TripGraph
import com.gcaguilar.biciradar.core.geo.GeoApi
import com.gcaguilar.biciradar.core.geo.GeoSearchUseCase
import com.gcaguilar.biciradar.core.geo.ReverseGeocodeUseCase

/**
 * Dependencias del feature de estaciones.
 *
 * Cubre consulta, refresco, monitorización y observación de estaciones.
 * Consumidores: shells de iOS, watchOS, Android y sus extensiones (widgets, Siri).
 */
interface StationsFeatureDeps {
  val changeCityUseCase: ChangeCityUseCase
  val isCityConfiguredUseCase: IsCityConfiguredUseCase
  val getCachedStationSnapshot: GetCachedStationSnapshot
  val getFavoriteStations: GetFavoriteStations
  val getNearestStations: GetNearestStations
  val getStationStatus: GetStationStatus
  val getSuggestedAlternativeStation: GetSuggestedAlternativeStation
  val refreshStationDataIfNeeded: RefreshStationDataIfNeeded
  val startStationMonitoring: StartStationMonitoring
  val stopStationMonitoring: StopStationMonitoring
  val getSuggestedStations: GetSuggestedStations
  val filterStationsByQuery: FilterStationsByQuery
  val findStationMatchingQuery: FindStationMatchingQuery
  val findNearestStation: FindNearestStation
  val findNearestStationWithBikes: FindNearestStationWithBikes
  val findNearestStationWithSlots: FindNearestStationWithSlots
  val cityRegistry: CityRegistry
  val refreshStationAvailability: RefreshStationAvailability
  val observeStationsState: ObserveStationsState
  val findStationById: FindStationById
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
