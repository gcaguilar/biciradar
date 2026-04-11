package com.gcaguilar.biciradar.mobileui.usecases

import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.DatosBiziApi
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationHourlyPattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

/**
 * Use case que agrupa las operaciones relacionadas con el detalle de una estación,
 * incluyendo favoritos, alertas y patrones de uso.
 */
class StationDetailUseCase(
  private val favoritesRepository: FavoritesRepository,
  private val settingsRepository: SettingsRepository,
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
  private val datosBiziApi: DatosBiziApi,
  private val routeLauncher: RouteLauncher,
) {
  val favoriteIds: StateFlow<Set<String>> = favoritesRepository.favoriteIds
  val homeStationId: StateFlow<String?> = favoritesRepository.homeStationId
  val workStationId: StateFlow<String?> = favoritesRepository.workStationId
  val selectedCity: StateFlow<City> = settingsRepository.selectedCity
  val alertRules: StateFlow<List<SavedPlaceAlertRule>> = savedPlaceAlertsRepository.rules

  fun isFavorite(stationId: String): Boolean = stationId in favoriteIds.value

  fun isHomeStation(stationId: String): Boolean = homeStationId.value == stationId

  fun isWorkStation(stationId: String): Boolean = workStationId.value == stationId

  suspend fun toggleFavorite(stationId: String) {
    favoritesRepository.toggle(stationId)
  }

  suspend fun toggleHomeStation(stationId: String) {
    val currentHomeId = homeStationId.value
    favoritesRepository.setHomeStationId(if (currentHomeId == stationId) null else stationId)
  }

  suspend fun toggleWorkStation(stationId: String) {
    val currentWorkId = workStationId.value
    favoritesRepository.setWorkStationId(if (currentWorkId == stationId) null else stationId)
  }

  suspend fun fetchStationPatterns(stationId: String): Result<List<StationHourlyPattern>> =
    runCatching {
      datosBiziApi.fetchPatterns(stationId)
    }

  fun launchRoute(station: Station) {
    routeLauncher.launch(station)
  }

  suspend fun upsertAlertRule(
    target: SavedPlaceAlertTarget,
    condition: SavedPlaceAlertCondition,
  ) {
    savedPlaceAlertsRepository.upsertRule(target, condition)
  }

  suspend fun removeAlertRuleForTarget(target: SavedPlaceAlertTarget) {
    savedPlaceAlertsRepository.removeRuleForTarget(target)
  }

  /**
   * Observa el estado completo del detalle de una estación
   */
  fun observeStationDetailState(stationId: String): Flow<StationDetailState> =
    combine(
      favoriteIds,
      homeStationId,
      workStationId,
      selectedCity,
      alertRules,
    ) { favorites, homeId, workId, city, rules ->
      StationDetailState(
        isFavorite = stationId in favorites,
        isHomeStation = homeId == stationId,
        isWorkStation = workId == stationId,
        supportsUsagePatterns = city.supportsUsagePatterns,
        savedPlaceAlertsCityId = city.id,
        savedPlaceAlertRules = rules,
      )
    }

  data class StationDetailState(
    val isFavorite: Boolean,
    val isHomeStation: Boolean,
    val isWorkStation: Boolean,
    val supportsUsagePatterns: Boolean,
    val savedPlaceAlertsCityId: String,
    val savedPlaceAlertRules: List<SavedPlaceAlertRule>,
  )
}
