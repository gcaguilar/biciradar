package com.gcaguilar.biciradar.mobileui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.gcaguilar.biciradar.core.EmbeddedMapProvider
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.TripRepository
import com.gcaguilar.biciradar.core.isGoogleMapsReady
import com.gcaguilar.biciradar.mobile_ui.generated.resources.*
import com.gcaguilar.biciradar.mobileui.components.BiziNavigationShell
import com.gcaguilar.biciradar.mobileui.components.OverlayManager
import com.gcaguilar.biciradar.mobileui.di.MobileGraph
import com.gcaguilar.biciradar.mobileui.experience.GuidedOnboardingCallbacks
import com.gcaguilar.biciradar.mobileui.experience.GuidedOnboardingFlow
import com.gcaguilar.biciradar.mobileui.navigation.AssistantLaunchRequest
import com.gcaguilar.biciradar.mobileui.navigation.MobileLaunchRequest
import com.gcaguilar.biciradar.mobileui.navigation.NavigationHost
import com.gcaguilar.biciradar.mobileui.navigation.NavigationHostConfig
import com.gcaguilar.biciradar.mobileui.navigation.Screen
import com.gcaguilar.biciradar.mobileui.screens.CitySelectionScreen
import com.gcaguilar.biciradar.mobileui.state.AppState
import com.gcaguilar.biciradar.mobileui.state.rememberAppState
import com.gcaguilar.biciradar.mobileui.theme.ThemeProvider
import com.gcaguilar.biciradar.mobileui.theme.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.viewmodel.AppRootViewModel
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.launch

/**
 * Creates and remembers the navigation configuration.
 */
@Composable
private fun rememberNavigationConfig(
  navController: NavHostController,
  mobilePlatform: MobileUiPlatform,
  canSelectGoogleMapsInIos: Boolean,
  isMapReady: Boolean,
  appState: AppState,
  platformBindings: PlatformBindings,
  onOpenOnboarding: () -> Unit,
  onShowChangelogManual: () -> Unit,
  showNearbyFeedbackNudge: Boolean,
  onNearbyFeedbackOpened: () -> Unit,
  onNearbyFeedbackDismiss: () -> Unit,
  onOpenNearbyFeedbackForm: () -> Unit,
): NavigationHostConfig {
  val onOpenAssistant =
    remember(navController) {
      { navController.navigate(Screen.Shortcuts) { launchSingleTop = true } }
    }
  val onInitialActionConsumed = remember(appState) { { appState.pendingAssistantAction = null } }
  val onInitialMapSearchQueryConsumed = remember(appState) { { appState.pendingMapSearchQuery = null } }

  return NavigationHostConfig(
    navController = navController,
    mobilePlatform = mobilePlatform,
    canSelectGoogleMapsInIos = canSelectGoogleMapsInIos,
    isMapReady = isMapReady,
    onOpenAssistant = onOpenAssistant,
    platformBindings = platformBindings,
    initialAssistantAction = appState.pendingAssistantAction,
    onInitialActionConsumed = onInitialActionConsumed,
    initialMapSearchQuery = appState.pendingMapSearchQuery,
    onInitialMapSearchQueryConsumed = onInitialMapSearchQueryConsumed,
    onOpenOnboarding = onOpenOnboarding,
    onShowChangelogManual = onShowChangelogManual,
    showNearbyFeedbackNudge = showNearbyFeedbackNudge,
    onNearbyFeedbackOpened = onNearbyFeedbackOpened,
    onNearbyFeedbackDismiss = onNearbyFeedbackDismiss,
    onOpenNearbyFeedbackForm = onOpenNearbyFeedbackForm,
    paddingValues = PaddingValues(),
  )
}

/**
 * App principal de BiciRadar.
 *
 * @param platformBindings Bindings de plataforma
 * @param graph Grafo de dependencias (si es null, se crea uno nuevo - no recomendado para producción)
 */
@Composable
fun BiziMobileApp(
  platformBindings: PlatformBindings,
  graph: SharedGraph? = null,
  modifier: Modifier = Modifier,
  refreshKey: Any? = Unit,
  launchRequest: MobileLaunchRequest? = null,
  assistantLaunchRequest: AssistantLaunchRequest? = null,
  onTripRepositoryReady: ((TripRepository) -> Unit)? = null,
  onSurfaceMonitoringRepositoryReady: (() -> Unit)? = null,
  onSurfaceSnapshotRepositoryReady: (() -> Unit)? = null,
  onStartupReadyChanged: (Boolean) -> Unit = {},
  useInAppStartupSplash: Boolean = true,
) {
  val mobilePlatform = remember { currentMobileUiPlatform() }
  val resolvedGraph: MobileGraph =
    remember(platformBindings, graph) {
      (graph as? MobileGraph) ?: MobileGraph.Companion.create(platformBindings)
    }
  val mapSupportStatus = remember(platformBindings) { platformBindings.mapSupport.currentStatus() }
  val launchCoordinator =
    remember(resolvedGraph, platformBindings) {
      LaunchCoordinator(
        changeCityUseCase = resolvedGraph.changeCityUseCase,
        observeFavorites = resolvedGraph.observeFavorites,
        localNotifier = platformBindings.localNotifier,
        routeLauncher = resolvedGraph.routeLauncher,
        findStationById = resolvedGraph.findStationById,
        refreshStationDataIfNeeded = resolvedGraph.refreshStationDataIfNeeded,
        startStationMonitoring = resolvedGraph.startStationMonitoring,
      )
    }
  val scope = rememberCoroutineScope()
  val appState = rememberAppState()
  val navController = rememberNavController()
  val stationsState by resolvedGraph.observeStationsState.state.collectAsState()
  val searchRadiusMeters by resolvedGraph.observeSettings.searchRadiusMeters.collectAsState()
  val preferredMapApp by resolvedGraph.observeSettings.preferredMapApp.collectAsState()
  val themePreference by resolvedGraph.observeSettings.themePreference.collectAsState()
  val canSelectGoogleMapsInIos =
    remember(mobilePlatform, mapSupportStatus) {
      mobilePlatform != MobileUiPlatform.IOS || mapSupportStatus.googleMapsAppInstalled
    }
  val isMapReady =
    when {
      mobilePlatform == MobileUiPlatform.IOS && preferredMapApp == PreferredMapApp.GoogleMaps ->
        mapSupportStatus.googleMapsSdkLinked && canSelectGoogleMapsInIos
      mobilePlatform == MobileUiPlatform.IOS -> false
      else ->
        when (mapSupportStatus.embeddedProvider) {
          EmbeddedMapProvider.GoogleMaps -> mapSupportStatus.isGoogleMapsReady()
          EmbeddedMapProvider.None -> false
          else -> true
        }
    }
  CompositionLocalProvider(LocalMetroViewModelFactory provides resolvedGraph.metroViewModelFactory) {
    val appRootViewModel = metroViewModel<AppRootViewModel>(key = "app-root")
    val appRootUiState by appRootViewModel.uiState.collectAsState()
    val onboardingChecklist = appRootUiState.onboardingChecklist
    val isCityConfigured = !(appRootUiState.isCitySelectionRequired)
    val shouldShowGuidedOnboarding = appRootUiState.shouldShowGuidedOnboarding

    LaunchedEffect(resolvedGraph) {
      platformBindings.onGraphCreated(resolvedGraph)
      // TODO: migrar a tripGraphFactory.createTripGraph() cuando el wrapper iOS
      //       gestione el ciclo de vida del TripGraph de forma aislada.
      @Suppress("DEPRECATION")
      onTripRepositoryReady?.invoke(resolvedGraph.tripRepository)
      onSurfaceMonitoringRepositoryReady?.invoke()
      onSurfaceSnapshotRepositoryReady?.invoke()
    }

    LaunchedEffect(refreshKey) {
      appRootViewModel.onRefreshSignal()
    }

    BiziLaunchEffects(
      startupLaunchReady = appRootUiState.startupLaunchReady,
      onStartupReadyChanged = onStartupReadyChanged,
      appState = appState,
      launchRequest = launchRequest,
      assistantLaunchRequest = assistantLaunchRequest,
      stationsState = stationsState,
      searchRadiusMeters = searchRadiusMeters,
      launchCoordinator = launchCoordinator,
      navController = navController,
    )

    val changelogPresentation = appRootUiState.changelogPresentation
    val showStartupSplash =
      remember(
        useInAppStartupSplash,
        appRootUiState.startupLaunchReady,
      ) {
        useInAppStartupSplash && !appRootUiState.startupLaunchReady
      }

    ThemeProvider(mobilePlatform, themePreference) {
      val windowLayout = LocalBiziWindowLayout.current
      Surface(
        modifier = modifier.fillMaxSize(),
        color = pageBackgroundColor(mobilePlatform),
      ) {
        when {
          !appRootUiState.settingsBootstrapped -> {
            // Loading state while settings are being initialized
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center,
            ) {
              CircularProgressIndicator()
            }
          }
          !isCityConfigured -> {
            CitySelectionScreen(
              onCitySelected = { city ->
                scope.launch {
                  resolvedGraph.changeCityUseCase.execute(city = city)
                }
              },
            )
          }
          shouldShowGuidedOnboarding -> {
            val onboardingCallbacks =
              remember(
                scope,
                platformBindings,
                navController,
              ) {
                GuidedOnboardingCallbacks(
                  onContinueFeatureHighlights = {
                    appRootViewModel.onOnboardingFeatureHighlightsContinued()
                  },
                  onRequestLocationPermission = {
                    scope.launch {
                      platformBindings.permissionPrompter.requestLocationPermission()
                    }
                    appRootViewModel.onOnboardingLocationDecisionMade()
                  },
                  onDismissLocationStep = {
                    appRootViewModel.onOnboardingLocationDecisionMade()
                  },
                  onRequestNotificationsPermission = {
                    scope.launch {
                      platformBindings.localNotifier.requestPermission()
                    }
                    appRootViewModel.onOnboardingNotificationsDecisionMade()
                  },
                  onDismissNotificationsStep = {
                    appRootViewModel.onOnboardingNotificationsDecisionMade()
                  },
                  onCompleteSurfacesStep = {
                    appRootViewModel.onOnboardingSurfacesCompleted()
                  },
                  onSkipAll = {
                    appRootViewModel.onSkipOnboarding()
                  },
                )
              }
            GuidedOnboardingFlow(
              checklist = onboardingChecklist,
              callbacks = onboardingCallbacks,
            )
          }
          else -> {
            Box(Modifier.fillMaxSize()) {
              AnimatedContent(
                targetState = showStartupSplash,
                transitionSpec = {
                  fadeIn(animationSpec = tween(220)).togetherWith(fadeOut(animationSpec = tween(140)))
                },
                label = "startup-splash-transition",
              ) { splashVisible ->
                if (splashVisible) {
                  StartupSplashScreen(mobilePlatform = mobilePlatform)
                } else {
                  // Create navigation configuration
                  val navConfig =
                    rememberNavigationConfig(
                      navController = navController,
                      mobilePlatform = mobilePlatform,
                      canSelectGoogleMapsInIos = canSelectGoogleMapsInIos,
                      isMapReady = isMapReady,
                      appState = appState,
                      platformBindings = platformBindings,
                      onOpenOnboarding = appRootViewModel::onOnboardingOpenedFromSettings,
                      onShowChangelogManual = appRootViewModel::showChangelogHistory,
                      showNearbyFeedbackNudge = appRootUiState.showFeedbackNudge,
                      onNearbyFeedbackOpened = appRootViewModel::onFeedbackOpened,
                      onNearbyFeedbackDismiss = appRootViewModel::onFeedbackDismissed,
                      onOpenNearbyFeedbackForm = platformBindings.externalLinks::openFeedbackForm,
                    )

                  Box(modifier = Modifier.fillMaxSize()) {
                    BiziNavigationShell(
                      mobilePlatform = mobilePlatform,
                      navController = navController,
                      windowLayout = windowLayout,
                    ) { innerPadding ->
                      NavigationHost(
                        config = navConfig.copy(paddingValues = innerPadding),
                      )
                    }
                    OverlayManager(
                      mobilePlatform = mobilePlatform,
                      updateBanner = appRootUiState.topUpdateBanner,
                      changelogSections = changelogPresentation?.sections ?: emptyList(),
                      highlightedVersion = changelogPresentation?.highlightedVersion,
                      showChangelog = !showStartupSplash && changelogPresentation != null,
                      onDismissAvailableUpdate = appRootViewModel::dismissAvailableUpdate,
                      onDismissDownloadedUpdate = appRootViewModel::dismissDownloadedUpdate,
                      onStartUpdate = appRootViewModel::onStartUpdateRequested,
                      onRestartToUpdate = appRootViewModel::onRestartToUpdateRequested,
                      onChangelogDismiss = appRootViewModel::dismissChangelog,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  } // end CompositionLocalProvider
}
