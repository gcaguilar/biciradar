package com.gcaguilar.biciradar.mobileui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.City
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
import com.gcaguilar.biciradar.mobileui.theme.ThemeProvider
import com.gcaguilar.biciradar.mobileui.theme.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.viewmodel.AppRootViewModel
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@androidx.compose.runtime.Stable
internal class AppState {
  var pendingMapSearchQuery by mutableStateOf<String?>(null)
  var pendingAssistantAction by mutableStateOf<AssistantAction?>(null)
  var pendingLaunchRequest by mutableStateOf<MobileLaunchRequest?>(null)
  var pendingAssistantLaunchRequest by mutableStateOf<AssistantLaunchRequest?>(null)
}

@Composable
private fun rememberAppState(): AppState = remember { AppState() }

internal fun shouldNavigateToFavoritesAfterOnboarding(
  hasPendingFavoritesNavigation: Boolean,
  shouldShowGuidedOnboarding: Boolean,
): Boolean = hasPendingFavoritesNavigation && !shouldShowGuidedOnboarding

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
      else -> mapSupportStatus.isGoogleMapsReady()
    }
  CompositionLocalProvider(LocalMetroViewModelFactory provides resolvedGraph.metroViewModelFactory) {
    val appRootViewModel = metroViewModel<AppRootViewModel>(key = "app-root")
    val appRootUiState by appRootViewModel.uiState.collectAsState()
    val onboardingChecklist = appRootUiState.onboardingChecklist
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var pendingOnboardingFavoritesNavigation by remember { mutableStateOf(false) }
    val isCityConfigured = !(appRootUiState.isCitySelectionRequired)
    val shouldShowGuidedOnboarding = appRootUiState.shouldShowGuidedOnboarding

    LaunchedEffect(resolvedGraph) {
      platformBindings.onGraphCreated(resolvedGraph)
      onTripRepositoryReady?.invoke(resolvedGraph.tripRepository)
      onSurfaceMonitoringRepositoryReady?.invoke()
      onSurfaceSnapshotRepositoryReady?.invoke()
    }

    LaunchedEffect(refreshKey) {
      appRootViewModel.onRefreshSignal()
    }

    LaunchedEffect(shouldShowGuidedOnboarding, pendingOnboardingFavoritesNavigation, navController) {
      if (shouldNavigateToFavoritesAfterOnboarding(
          hasPendingFavoritesNavigation = pendingOnboardingFavoritesNavigation,
          shouldShowGuidedOnboarding = shouldShowGuidedOnboarding,
        )
      ) {
        navController.navigate(Screen.Favorites) { launchSingleTop = true }
        pendingOnboardingFavoritesNavigation = false
      }
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
                  onOpenFavorites = {
                    appRootViewModel.onOnboardingOpenFavoritesRequested()
                    pendingOnboardingFavoritesNavigation = true
                  },
                  onDismissFavoritesStep = {
                    appRootViewModel.onOnboardingFavoritesDismissed()
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
                      showFeedbackNudge = appRootUiState.showFeedbackNudge,
                      showFeedbackDialog = showFeedbackDialog,
                      changelogSections = changelogPresentation?.sections ?: emptyList(),
                      highlightedVersion = changelogPresentation?.highlightedVersion,
                      showChangelog = !showStartupSplash && changelogPresentation != null,
                      onDismissAvailableUpdate = appRootViewModel::dismissAvailableUpdate,
                      onDismissDownloadedUpdate = appRootViewModel::dismissDownloadedUpdate,
                      onStartUpdate = appRootViewModel::onStartUpdateRequested,
                      onRestartToUpdate = appRootViewModel::onRestartToUpdateRequested,
                      onFeedbackSend = {
                        appRootViewModel.onFeedbackOpened()
                        showFeedbackDialog = true
                      },
                      onFeedbackDismiss = appRootViewModel::onFeedbackDismissed,
                      onFeedbackDialogDismiss = { showFeedbackDialog = false },
                      onOpenFeedbackForm = {
                        platformBindings.externalLinks.openFeedbackForm()
                        showFeedbackDialog = false
                      },
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

@Composable
private fun CitySelectionScreen(onCitySelected: (City) -> Unit) {
  val colors = LocalBiziColors.current
  var searchQuery by remember { mutableStateOf("") }
  val sortedCities = remember { City.entries.sortedBy { it.displayName } }
  val normalizedQuery = remember(searchQuery) { searchQuery.trim().normalizedForSearch() }
  val filteredCities =
    remember(normalizedQuery, sortedCities) {
      if (normalizedQuery.isBlank()) {
        sortedCities
      } else {
        sortedCities.filter { city ->
          city.displayName.normalizedForSearch().contains(normalizedQuery)
        }
      }
    }
  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .background(colors.background)
        .windowInsetsPadding(WindowInsets.statusBars)
        .padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = stringResource(Res.string.citySelectionTitle),
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold,
      color = colors.ink,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = stringResource(Res.string.citySelectionSubtitle),
      style = MaterialTheme.typography.bodyMedium,
      color = colors.muted,
    )
    Spacer(modifier = Modifier.height(20.dp))
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      placeholder = { Text(stringResource(Res.string.citySelectionSearchPlaceholder)) },
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedContainerColor = colors.surface,
          unfocusedContainerColor = colors.surface,
        ),
    )
    Spacer(modifier = Modifier.height(12.dp))
    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (filteredCities.isEmpty()) {
        item {
          Text(
            text = stringResource(Res.string.citySelectionSearchNoResults),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
            modifier = Modifier.padding(vertical = 8.dp),
          )
        }
      }
      items(filteredCities.size) { index ->
        val city = filteredCities[index]
        Card(
          modifier =
            Modifier
              .fillMaxWidth()
              .clickable { onCitySelected(city) },
          colors = CardDefaults.cardColors(containerColor = colors.surface),
          border = BorderStroke(1.dp, colors.panel),
        ) {
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = city.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.ink,
              )
              if (city.supportsEbikes) {
                Text(
                  text = "Bicis eléctricas disponibles",
                  style = MaterialTheme.typography.bodySmall,
                  color = colors.muted,
                )
              }
            }
            Icon(
              imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
              contentDescription = null,
              tint = colors.red,
            )
          }
        }
      }
    }
    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
internal fun StationSearchField(
  mobilePlatform: MobileUiPlatform,
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
) {
  val c = LocalBiziColors.current
  OutlinedTextField(
    modifier = Modifier.fillMaxWidth(),
    value = value,
    onValueChange = onValueChange,
    singleLine = true,
    shape = RoundedCornerShape(20.dp),
    leadingIcon = {
      Icon(Icons.Filled.Search, contentDescription = null, tint = c.muted)
    },
    trailingIcon =
      if (value.isNotEmpty()) {
        {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = stringResource(Res.string.clearSearch),
            tint = c.muted,
            modifier = Modifier.clickable { onValueChange("") },
          )
        }
      } else {
        null
      },
    label =
      if (mobilePlatform == MobileUiPlatform.Android) {
        { Text(label) }
      } else {
        null
      },
    placeholder = { Text(label, color = c.muted) },
    colors =
      OutlinedTextFieldDefaults.colors(
        focusedContainerColor =
          if (mobilePlatform ==
            MobileUiPlatform.IOS
          ) {
            c.fieldSurfaceIos
          } else {
            c.fieldSurfaceAndroid
          },
        unfocusedContainerColor =
          if (mobilePlatform ==
            MobileUiPlatform.IOS
          ) {
            c.fieldSurfaceIos
          } else {
            c.fieldSurfaceAndroid
          },
        focusedBorderColor = c.red.copy(alpha = if (mobilePlatform == MobileUiPlatform.IOS) 0.18f else 0.30f),
        unfocusedBorderColor = if (mobilePlatform == MobileUiPlatform.IOS) c.panel else c.muted.copy(alpha = 0.18f),
        focusedTextColor = c.ink,
        unfocusedTextColor = c.ink,
        focusedLabelColor = c.ink,
        unfocusedLabelColor = c.muted,
        focusedPlaceholderColor = c.muted,
        unfocusedPlaceholderColor = c.muted,
        focusedLeadingIconColor = c.muted,
        unfocusedLeadingIconColor = c.muted,
        focusedTrailingIconColor = c.muted,
        unfocusedTrailingIconColor = c.muted,
      ),
  )
}

@Composable
internal fun RadiusSelectionButton(
  modifier: Modifier,
  selected: Boolean,
  label: String,
  onClick: () -> Unit,
) {
  val containerColor by animateColorAsState(
    targetValue = if (selected) LocalBiziColors.current.red.copy(alpha = 0.10f) else LocalBiziColors.current.surface,
    animationSpec = tween(180),
    label = "radius-container",
  )
  val borderColor by animateColorAsState(
    targetValue = if (selected) LocalBiziColors.current.red.copy(alpha = 0.25f) else LocalBiziColors.current.panel,
    animationSpec = tween(180),
    label = "radius-border",
  )
  val textColor by animateColorAsState(
    targetValue = if (selected) LocalBiziColors.current.red else LocalBiziColors.current.ink,
    animationSpec = tween(180),
    label = "radius-text",
  )
  val selectionScale by animateFloatAsState(
    targetValue = if (selected) 1f else 0.98f,
    animationSpec = spring(dampingRatio = 0.82f, stiffness = 700f),
    label = "radius-scale",
  )
  Surface(
    modifier =
      modifier
        .graphicsLayer {
          scaleX = selectionScale
          scaleY = selectionScale
        }.clickable(onClick = onClick),
    shape = RoundedCornerShape(18.dp),
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
  ) {
    Text(
      text = label,
      modifier =
        Modifier
          .padding(horizontal = 14.dp, vertical = 12.dp)
          .animateContentSize(animationSpec = tween(180)),
      color = textColor,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
    )
  }
}
