package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.AppUpdatePrompter
import com.gcaguilar.biciradar.core.EngagementRepository
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.ReviewPrompter
import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.SurfaceSnapshotRepository

internal class AppRootViewModelFactory(
  private val settingsRepository: SettingsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val stationsRepository: StationsRepository,
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
  private val engagementRepository: EngagementRepository,
  private val surfaceSnapshotRepository: SurfaceSnapshotRepository,
  private val surfaceMonitoringRepository: SurfaceMonitoringRepository,
  private val appUpdatePrompter: AppUpdatePrompter,
  private val reviewPrompter: ReviewPrompter,
  private val appVersion: String,
) {
  fun create(): AppRootViewModel = AppRootViewModel(
    settingsRepository = settingsRepository,
    favoritesRepository = favoritesRepository,
    stationsRepository = stationsRepository,
    savedPlaceAlertsRepository = savedPlaceAlertsRepository,
    engagementRepository = engagementRepository,
    surfaceSnapshotRepository = surfaceSnapshotRepository,
    surfaceMonitoringRepository = surfaceMonitoringRepository,
    appUpdatePrompter = appUpdatePrompter,
    reviewPrompter = reviewPrompter,
    appVersion = appVersion,
  )
}
