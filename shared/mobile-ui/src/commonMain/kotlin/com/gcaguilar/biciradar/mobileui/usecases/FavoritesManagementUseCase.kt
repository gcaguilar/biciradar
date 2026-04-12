package com.gcaguilar.biciradar.mobileui.usecases

import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.FavoriteCategory
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.StationsState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

/**
 * Use case que agrupa las operaciones relacionadas con la gestión de favoritos,
 * estaciones home/work y datos de estaciones.
 */
@Inject
class FavoritesManagementUseCase(
  private val favoritesRepository: FavoritesRepository,
  private val stationsRepository: StationsRepository,
  private val settingsRepository: SettingsRepository,
) {
  val favoriteIds: StateFlow<Set<String>> = favoritesRepository.favoriteIds
  val homeStationId: StateFlow<String?> = favoritesRepository.homeStationId
  val workStationId: StateFlow<String?> = favoritesRepository.workStationId
  val stationsState: StateFlow<StationsState> = stationsRepository.state
  val selectedCity: StateFlow<City> = settingsRepository.selectedCity
  val categories: StateFlow<List<FavoriteCategory>> = favoritesRepository.categories
  val stationCategory: StateFlow<Map<String, String>> = favoritesRepository.stationCategory

  suspend fun setHomeStationId(stationId: String?) {
    favoritesRepository.setHomeStationId(stationId)
  }

  suspend fun setWorkStationId(stationId: String?) {
    favoritesRepository.setWorkStationId(stationId)
  }

  suspend fun toggleFavorite(stationId: String) {
    favoritesRepository.toggle(stationId)
  }

  suspend fun upsertCategory(
    id: String,
    label: String,
    isSystem: Boolean = false,
  ) {
    favoritesRepository.upsertCategory(id, label, isSystem)
  }

  suspend fun removeCategory(categoryId: String) {
    favoritesRepository.removeCategory(categoryId)
  }

  suspend fun assignStationToCategory(
    stationId: String,
    categoryId: String?,
  ) {
    favoritesRepository.assignStationToCategory(stationId, categoryId)
  }

  fun stationById(stationId: String): Station? = stationsRepository.stationById(stationId)

  suspend fun forceRefresh() {
    stationsRepository.forceRefresh()
  }

  /**
   * Combina los estados necesarios para la UI de favoritos
   */
  fun observeFavoritesState(): Flow<FavoritesState> =
    combine(
      stationsState,
      favoriteIds,
      homeStationId,
      workStationId,
      selectedCity,
    ) { stationsState, favoriteIds, homeId, workId, city ->
      FavoritesState(
        stations = stationsState.stations,
        favoriteIds = favoriteIds,
        homeStationId = homeId,
        workStationId = workId,
        selectedCity = city,
      )
    }

  data class FavoritesState(
    val stations: List<Station>,
    val favoriteIds: Set<String>,
    val homeStationId: String?,
    val workStationId: String?,
    val selectedCity: City,
  )
}

/**
 * Use case para la gestión de alertas de lugares guardados
 */
@Inject
class SavedPlaceAlertsUseCase(
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
) {
  val rules: StateFlow<List<SavedPlaceAlertRule>> = savedPlaceAlertsRepository.rules

  suspend fun upsertRule(
    target: SavedPlaceAlertTarget,
    condition: SavedPlaceAlertCondition,
  ) {
    savedPlaceAlertsRepository.upsertRule(target, condition)
  }

  suspend fun removeRuleForTarget(target: SavedPlaceAlertTarget) {
    savedPlaceAlertsRepository.removeRuleForTarget(target)
  }
}

/**
 * Use case para el lanzamiento de rutas de navegación
 */
@Inject
class RouteLaunchUseCase(
  private val routeLauncher: RouteLauncher,
) {
  fun launchRoute(station: Station) {
    routeLauncher.launch(station)
  }
}
