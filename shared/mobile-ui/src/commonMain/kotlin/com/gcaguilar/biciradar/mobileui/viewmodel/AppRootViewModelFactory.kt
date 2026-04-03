package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.AppUpdatePrompter
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.ReviewPrompter
import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.SurfaceSnapshotRepository
import com.gcaguilar.biciradar.core.EngagementRepository
import com.gcaguilar.biciradar.mobileui.usecases.ChangelogUseCase
import com.gcaguilar.biciradar.mobileui.usecases.FeedbackUseCase
import com.gcaguilar.biciradar.mobileui.usecases.StartupUseCase

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
  fun create(): AppRootViewModel {
    // Create Use Cases
    val startupUseCase = StartupUseCase(
      settingsRepository = settingsRepository,
      favoritesRepository = favoritesRepository,
      stationsRepository = stationsRepository,
    )

    val feedbackUseCase = FeedbackUseCase(
      appUpdatePrompter = appUpdatePrompter,
      reviewPrompter = reviewPrompter,
      engagementRepository = engagementRepository,
    )

    val changelogUseCase = ChangelogUseCase(
      settingsRepository = settingsRepository,
      appVersion = appVersion,
    )

    return AppRootViewModel(
      startupUseCase = startupUseCase,
      feedbackUseCase = feedbackUseCase,
      changelogUseCase = changelogUseCase,
      savedPlaceAlertsRepository = savedPlaceAlertsRepository,
      surfaceSnapshotRepository = surfaceSnapshotRepository,
      surfaceMonitoringRepository = surfaceMonitoringRepository,
      appVersion = appVersion,
    )
  }
}
