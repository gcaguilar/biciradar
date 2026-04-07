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
import com.gcaguilar.biciradar.core.epochMillisForUi
import com.gcaguilar.biciradar.mobileui.initialization.AppInitializer
import com.gcaguilar.biciradar.mobileui.usecases.AppLifecycleUseCase
import com.gcaguilar.biciradar.mobileui.usecases.ResolveOnboardingPresentationUseCase
import com.gcaguilar.biciradar.mobileui.usecases.SettingsAggregationUseCase
import com.gcaguilar.biciradar.mobileui.usecases.StartupUseCase
import com.gcaguilar.biciradar.mobileui.usecases.SurfaceManagementUseCase

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
    val settingsAggregationUseCase = SettingsAggregationUseCase(
      settingsRepository = settingsRepository,
    )

    val startupUseCase = StartupUseCase(
      settingsRepository = settingsRepository,
      favoritesRepository = favoritesRepository,
      stationsRepository = stationsRepository,
    )

    val appLifecycleUseCase = AppLifecycleUseCase(
      engagementRepository = engagementRepository,
      appUpdatePrompter = appUpdatePrompter,
      reviewPrompter = reviewPrompter,
      settingsAggregationUseCase = settingsAggregationUseCase,
      appVersion = appVersion,
    )

    val resolveOnboardingPresentationUseCase = ResolveOnboardingPresentationUseCase()

    val surfaceManagementUseCase = SurfaceManagementUseCase(
      surfaceSnapshotRepository = surfaceSnapshotRepository,
      surfaceMonitoringRepository = surfaceMonitoringRepository,
      savedPlaceAlertsRepository = savedPlaceAlertsRepository,
    )

    // Create AppInitializer
    val appInitializer = AppInitializer(
      startupUseCase = startupUseCase,
      appLifecycleUseCase = appLifecycleUseCase,
      savedPlaceAlertsRepository = savedPlaceAlertsRepository,
      surfaceManagementUseCase = surfaceManagementUseCase,
      clock = ::epochMillisForUi,
    )

    return AppRootViewModel(
      startupUseCase = startupUseCase,
      appLifecycleUseCase = appLifecycleUseCase,
      resolveOnboardingPresentationUseCase = resolveOnboardingPresentationUseCase,
      appInitializer = appInitializer,
      appVersion = appVersion,
    )
  }
}
