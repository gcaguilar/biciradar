package com.gcaguilar.biciradar.mobileui

import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.AssistantResolution
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.FavoritesFeatureDeps
import com.gcaguilar.biciradar.core.PlatformFeatureDeps
import com.gcaguilar.biciradar.core.SavedPlaceAlertTrigger
import com.gcaguilar.biciradar.core.SessionFeatureDeps
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsFeatureDeps
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle
import com.gcaguilar.biciradar.core.geo.currentTimeMs

/**
 * Facade tipada que expone las operaciones del [SharedGraph] necesarias para
 * los shells de iOS y watchOS.
 *
 * **Propósito**
 * Sustituye el acceso por reflexión dinámica (`NSClassFromString`, `NSSelectorFromString`,
 * `unsafeBitCast`) que existía en `BiziAppleGraph.swift`. Todos los métodos de esta
 * clase son llamables directamente desde Swift con seguridad de tipos en tiempo de
 * compilación: cualquier cambio de firma o nombre rompe la compilación del proyecto
 * Xcode, no el runtime.
 *
 * **Contrato de dependencias**
 * Recibe el [SharedGraph] como [StationsFeatureDeps] + [FavoritesFeatureDeps] +
 * [SessionFeatureDeps] para que cada operación declare solo lo que realmente necesita.
 * Se construye mediante [BiziAppleFacade.create] una vez que el grafo está disponible.
 *
 * **Uso en Swift**
 * ```swift
 * // Creación (una sola vez, al inicio del actor)
 * let graph = MobileGraphFactory.shared.create(platformBindings: bindings)
 * let facade = BiziAppleFacade.companion.create(graph: graph)
 *
 * // Invocación
 * let city = facade.currentSelectedCity()
 * let station = try await facade.nearestStation()
 * ```
 */
class BiziAppleFacade private constructor(
  private val stations: StationsFeatureDeps,
  private val favorites: FavoritesFeatureDeps,
  private val session: SessionFeatureDeps,
  private val platform: PlatformFeatureDeps,
) {

  // ─────────────────────────── Sesión / arranque ───────────────────────────

  /** Inicializa todos los repositorios persistentes. Idempotente. */
  suspend fun bootstrap() {
    session.bootstrapSession.execute()
  }

  // ─────────────────────────── Estaciones ──────────────────────────────────

  /**
   * Refresca el estado de estaciones.
   * @return snapshot de superficie actualizado, o null si no hay datos disponibles.
   */
  suspend fun refreshData(forceRefresh: Boolean = false): SurfaceSnapshotBundle? =
    stations.refreshStationDataIfNeeded.execute(forceRefresh = forceRefresh)

  /** Ciudad actualmente seleccionada (sin I/O). */
  fun currentSelectedCity(): City = session.getCurrentCity.execute()

  /** Cambia la ciudad seleccionada y fuerza refresco de estaciones. */
  suspend fun setSelectedCity(city: City) {
    session.updateSelectedCity.execute(city)
  }

  /** Estación más cercana al usuario, o null si no hay datos. */
  suspend fun nearestStation(): Station? = stations.findNearestStation.execute()

  /** Estación más cercana con bicicletas disponibles. */
  suspend fun nearestStationWithBikes(): Station? = stations.findNearestStationWithBikes.execute()

  /** Estación más cercana con anclajes libres. */
  suspend fun nearestStationWithSlots(): Station? = stations.findNearestStationWithSlots.execute()

  /** Lista de estaciones marcadas como favoritas, ordenadas por distancia. */
  suspend fun favoriteStations(): List<Station> = favorites.getFavoriteStationList.execute()

  /** Lista sugerida de estaciones priorizando home, work y favoritos. */
  suspend fun suggestedStations(limit: Int = 8): List<Station> =
    stations.getSuggestedStations.execute(limit = limit)

  /**
   * Sugerencias filtradas por [query]. Si no hay resultados, devuelve [suggestedStations].
   */
  suspend fun stationSuggestions(query: String, limit: Int = 8): List<Station> {
    val filtered = stations.filterStationsByQuery.execute(query = query)
    if (filtered.isEmpty()) return suggestedStations(limit = limit)
    return filtered.take(limit)
  }

  /**
   * Estación que mejor coincide con [query], o la más cercana si la query es nula/vacía.
   * Soporta alias "casa" / "trabajo" para home/work.
   */
  suspend fun stationMatchingQuery(query: String?): Station? =
    stations.findStationMatchingQuery.execute(query = query)

  /** Estación por ID exacto (sin I/O, solo lectura en memoria). */
  fun stationById(stationId: String): Station? = stations.findStationById.execute(stationId = stationId)

  // ─────────────────────────── Asistente ───────────────────────────────────

  /**
   * Resuelve la [action] del asistente contra el estado actual.
   * @throws [BiziAppleFacadeError.EmptyAssistantResponse] si la resolución devuelve nulo.
   */
  suspend fun assistantResponse(action: AssistantAction): AssistantResolution =
    session.resolveAssistantIntent.execute(action = action)

  /**
   * Busca la estación que mejor coincide con [query] y lanza la navegación hacia ella.
   * @return la estación encontrada, o null si no hay coincidencia.
   */
  suspend fun routeToStation(query: String?): Station? {
    val station = stationMatchingQuery(query) ?: return null
    platform.routeLauncher.launch(station = station)
    return station
  }

  // ─────────────────────────── Alertas de lugar guardado ───────────────────

  /**
   * Evalúa las reglas de alerta y devuelve los disparadores activos.
   *
   * El shell iOS es responsable de entregar las notificaciones de plataforma
   * usando los [SavedPlaceAlertTrigger] devueltos.
   */
  suspend fun evaluateSavedPlaceAlerts(nowEpoch: Long = currentTimeMs()): List<SavedPlaceAlertTrigger> =
    favorites.evaluateSavedPlaceAlerts.execute(nowEpoch = nowEpoch)

  // ─────────────────────────── Factory ─────────────────────────────────────

  companion object {
    /**
     * Crea una instancia de [BiziAppleFacade] a partir del [graph] ya inicializado.
     *
     * Swift: `BiziAppleFacade.companion.create(graph: graph)`
     */
    fun create(graph: SharedGraph): BiziAppleFacade =
      BiziAppleFacade(
        stations = graph,
        favorites = graph,
        session = graph,
        platform = graph,
      )
  }
}

/** Errores específicos de la capa de facade Apple. */
sealed class BiziAppleFacadeError : Exception() {
  /** El asistente no devolvió ninguna resolución. */
  object EmptyAssistantResponse : BiziAppleFacadeError() {
    override val message: String = "No se recibió respuesta del asistente."
  }
}
