package com.gcaguilar.biciradar.mobileui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.EnvironmentalRepository
import com.gcaguilar.biciradar.core.UpdateAvailabilityState
import com.gcaguilar.biciradar.core.epochMillisForUi
import com.gcaguilar.biciradar.core.pendingChangelogVersion
import com.gcaguilar.biciradar.core.normalizeAppVersionForCatalog
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.core.SEARCH_RADIUS_OPTIONS_METERS
import com.gcaguilar.biciradar.core.formatDistance
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.Station
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import com.gcaguilar.biciradar.core.filterStationsByQuery
import com.gcaguilar.biciradar.core.findStationMatchingQuery
import com.gcaguilar.biciradar.core.findStationMatchingQueryOrPinnedAlias
import com.gcaguilar.biciradar.core.isGoogleMapsReady
import com.gcaguilar.biciradar.core.selectNearbyStation


import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import com.gcaguilar.biciradar.core.DatosBiziApi
import com.gcaguilar.biciradar.core.DEFAULT_SURFACE_MONITORING_DURATION_SECONDS
import com.gcaguilar.biciradar.core.geo.GeoResult
import com.gcaguilar.biciradar.core.MONITORING_DURATION_OPTIONS_SECONDS
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.SurfaceSnapshotRepository
import com.gcaguilar.biciradar.core.StationHourlyPattern
import com.gcaguilar.biciradar.core.TripDestination
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.core.TripRepository
import com.gcaguilar.biciradar.core.TripState
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gcaguilar.biciradar.mobile_ui.generated.resources.*
import com.gcaguilar.biciradar.mobileui.experience.ChangelogCatalog
import com.gcaguilar.biciradar.mobileui.experience.ChangelogCatalogEntry
import com.gcaguilar.biciradar.mobileui.experience.GuidedOnboardingFlow
import com.gcaguilar.biciradar.mobileui.navigation.BiziNavHost
import com.gcaguilar.biciradar.mobileui.navigation.Screen
import androidx.window.core.layout.WindowSizeClass

// --- Light-mode palette raw tokens ---
private val BiziLight = Color(0xFFF8F6F6)
private val BiziGrouped = Color(0xFFF2F2F7)
private val BiziInk = Color(0xFF0D1B2A)
private val BiziMuted = Color(0xFF64779D)
private val BiziPanel = Color(0xFFE8EDF4)
private val BiziPrimary = Color(0xFF1D74BD)
private val BiziSecondary = Color(0xFF64C23A)
private val BiziTertiary = Color(0xFF0D1B2A)
private val BiziOrange = Color(0xFFF28000)
private val BiziNeutral = Color(0xFF64779D)

// --- Dark-mode palette raw tokens ---
private val BiziDarkBackground = Color(0xFF0F172A)
private val BiziDarkGrouped = Color(0xFF1C1C1E)
private val BiziDarkSurface = Color(0xFF1E1E1E)
private val BiziDarkInk = Color(0xFFF1EDED)
private val BiziDarkMuted = Color(0xFF94A3B8)
private val BiziDarkPanel = Color(0xFF2A2A2C)
private val BiziDarkPrimary = Color(0xFF1070CA)
private val BiziDarkSecondary = Color(0xFF64C832)
private val BiziDarkTertiary = Color(0xFFA05ABA)
private val BiziDarkNeutral = Color(0xFF0F172A)

/**
 * Semantic color scheme consumed by every composable in the app.
 * Two instances exist: [LightBiziColors] and [DarkBiziColors].
 */
internal data class BiziColors(
  /** Page-level background (Android). */
  val background: Color,
  /** Page-level background (iOS grouped style). */
  val groupedBackground: Color,
  /** Card / surface container. */
  val surface: Color,
  /** Primary text on surfaces. */
  val ink: Color,
  /** Secondary / caption text. */
  val muted: Color,
  /** Subtle borders and dividers. */
  val panel: Color,
  /** Accent: bikes count, favorites, brand. */
  val red: Color,
  /** Accent: slots count, routes. */
  val blue: Color,
  /** Accent: distance, home. */
  val green: Color,
  /** Accent: partial availability on the map. */
  val orange: Color,
  /** Accent: regular bikes. */
  val purple: Color,
  /** Foreground on accent fills (e.g. icon on BiziRed circle). */
  val onAccent: Color,
  /** NavigationBar container. */
  val navBar: Color,
  /** NavigationBar container (iOS translucent). */
  val navBarIos: Color,
  /** Text-field container (iOS). */
  val fieldSurfaceIos: Color,
  /** Text-field container (Android). */
  val fieldSurfaceAndroid: Color,
  /** Dismiss-to-delete background base alpha factor. */
  val dismissAlphaBase: Float,
)

private val LightBiziColors = BiziColors(
  background = BiziLight,
  groupedBackground = BiziGrouped,
  surface = Color.White,
  ink = BiziInk,
  muted = BiziMuted,
  panel = BiziPanel,
  red = BiziPrimary,
  blue = BiziTertiary,
  green = BiziSecondary,
  orange = BiziOrange,
  purple = BiziNeutral,
  onAccent = Color.White,
  navBar = Color.White,
  navBarIos = Color.White.copy(alpha = 0.96f),
  fieldSurfaceIos = Color.White,
  fieldSurfaceAndroid = BiziPanel,
  dismissAlphaBase = 0.10f,
)

private val DarkBiziColors = BiziColors(
  background = BiziDarkBackground,
  groupedBackground = BiziDarkGrouped,
  surface = BiziDarkSurface,
  ink = BiziDarkInk,
  muted = BiziDarkMuted,
  panel = BiziDarkPanel,
  red = BiziDarkPrimary,
  blue = BiziDarkTertiary,
  green = BiziDarkSecondary,
  orange = BiziOrange,
  purple = BiziDarkNeutral,
  onAccent = Color.White,
  navBar = BiziDarkSurface,
  navBarIos = BiziDarkSurface.copy(alpha = 0.96f),
  fieldSurfaceIos = BiziDarkSurface,
  fieldSurfaceAndroid = BiziDarkPanel,
  dismissAlphaBase = 0.16f,
)

internal val LocalBiziColors = staticCompositionLocalOf { LightBiziColors }

internal enum class BiziWindowLayout {
  Compact,
  Medium,
  Expanded,
}

internal val LocalBiziWindowLayout = staticCompositionLocalOf { BiziWindowLayout.Compact }

private enum class MobileTab(val labelKey: StringResource) {
  Cerca(Res.string.nearby),
  Mapa(Res.string.map),
  Favoritos(Res.string.favorites),
  Viaje(Res.string.trip),
  Perfil(Res.string.settings),
}

private val MobileTabs = listOf(
  MobileTab.Cerca,
  MobileTab.Mapa,
  MobileTab.Favoritos,
  MobileTab.Viaje,
  MobileTab.Perfil,
)

private enum class MapFilter(val labelKey: StringResource) {
  BIKES_AND_SLOTS(Res.string.mapFilterBikesAndSlots),
  ONLY_BIKES(Res.string.mapFilterOnlyBikes),
  ONLY_SLOTS(Res.string.mapFilterOnlySlots),
  ONLY_EBIKES(Res.string.mapFilterOnlyEbikes),
  ONLY_REGULAR_BIKES(Res.string.mapFilterOnlyRegularBikes),
  AIR_QUALITY(Res.string.mapFilterAirQuality),
  POLLEN(Res.string.mapFilterPollen),
}

sealed interface MobileLaunchRequest {
  data object Home : MobileLaunchRequest
  data object Map : MobileLaunchRequest
  data object Favorites : MobileLaunchRequest
  data object SavedPlaceAlerts : MobileLaunchRequest
  data object NearestStation : MobileLaunchRequest
  data object NearestStationWithBikes : MobileLaunchRequest
  data object NearestStationWithSlots : MobileLaunchRequest
  data object OpenAssistant : MobileLaunchRequest
  data object StationStatus : MobileLaunchRequest
  data class MonitorStation(val stationId: String) : MobileLaunchRequest
  data class SelectCity(val cityId: String) : MobileLaunchRequest
  data class RouteToStation(val stationId: String? = null) : MobileLaunchRequest
  data class ShowStation(val stationId: String) : MobileLaunchRequest
}

sealed interface AssistantLaunchRequest {
  data class SearchStation(
    val stationQuery: String,
  ) : AssistantLaunchRequest

  data class StationStatus(
    val stationId: String? = null,
    val stationQuery: String? = null,
  ) : AssistantLaunchRequest

  data class StationBikeCount(
    val stationId: String? = null,
    val stationQuery: String? = null,
  ) : AssistantLaunchRequest

  data class StationSlotCount(
    val stationId: String? = null,
    val stationQuery: String? = null,
  ) : AssistantLaunchRequest

  data class RouteToStation(
    val stationId: String? = null,
    val stationQuery: String? = null,
  ) : AssistantLaunchRequest
}

private sealed interface TopUpdateBanner {
  data object Hidden : TopUpdateBanner

  data class Available(
    val version: String,
    val flexible: Boolean,
    val storeUrl: String?,
  ) : TopUpdateBanner

  data class Downloaded(val version: String) : TopUpdateBanner
}

private enum class EnvironmentalLayer {
  AirQuality,
  Pollen,
}

private data class ZoneEnvironmentalSnapshot(
  val centerLatitude: Double,
  val centerLongitude: Double,
  val zoneLabel: String,
  val airQualityScore: Int? = null,
  val pollenScore: Int? = null,
)

@androidx.compose.runtime.Stable
private class AppState {
  var searchQuery by mutableStateOf("")
  var pendingAssistantAction by mutableStateOf<AssistantAction?>(null)
  var pendingLaunchRequest by mutableStateOf<MobileLaunchRequest?>(null)
  var pendingAssistantLaunchRequest by mutableStateOf<AssistantLaunchRequest?>(null)
}

@Composable
private fun rememberAppState(): AppState = remember { AppState() }

@Composable
fun BiziMobileApp(
  platformBindings: PlatformBindings,
  modifier: Modifier = Modifier,
  refreshKey: Any? = Unit,
  launchRequest: MobileLaunchRequest? = null,
  assistantLaunchRequest: AssistantLaunchRequest? = null,
  onTripRepositoryReady: ((TripRepository) -> Unit)? = null,
  onSurfaceMonitoringRepositoryReady: ((SurfaceMonitoringRepository) -> Unit)? = null,
  onSurfaceSnapshotRepositoryReady: ((SurfaceSnapshotRepository) -> Unit)? = null,
  onStartupReadyChanged: (Boolean) -> Unit = {},
  useInAppStartupSplash: Boolean = true,
) {
  val mobilePlatform = remember { currentMobileUiPlatform() }
  val graph = remember(platformBindings) {
    SharedGraph.Companion.create(platformBindings)
  }
  val mapSupportStatus = remember(platformBindings) { platformBindings.mapSupport.currentStatus() }
  val stationsRepository = remember(graph) { graph.stationsRepository }
  val favoritesRepository = remember(graph) { graph.favoritesRepository }
  val settingsRepository = remember(graph) { graph.settingsRepository }
  val scope = rememberCoroutineScope()
  val appState = rememberAppState()
  val navController = rememberNavController()
  val stationsState by stationsRepository.state.collectAsState()
  val favoriteIds by favoritesRepository.favoriteIds.collectAsState()
  val searchRadiusMeters by settingsRepository.searchRadiusMeters.collectAsState()
  val preferredMapApp by settingsRepository.preferredMapApp.collectAsState()
  val themePreference by settingsRepository.themePreference.collectAsState()
  val isMapReady = when {
    mobilePlatform == MobileUiPlatform.IOS && preferredMapApp == PreferredMapApp.GoogleMaps ->
      mapSupportStatus.googleMapsSdkLinked
    mobilePlatform == MobileUiPlatform.IOS -> false
    else -> mapSupportStatus.isGoogleMapsReady()
  }
  var settingsBootstrapped by remember(graph) { mutableStateOf(false) }
  var favoritesBootstrapped by remember(graph) { mutableStateOf(false) }
  var initialLoadAttemptFinished by remember(graph) { mutableStateOf(false) }
  var minimumSplashElapsed by remember(graph) { mutableStateOf(false) }
  val onboardingChecklist by settingsRepository.onboardingChecklist.collectAsState()
  val hasCompletedOnboarding by settingsRepository.hasCompletedOnboarding.collectAsState()
  val homeStationId by favoritesRepository.homeStationId.collectAsState()
  val workStationId by favoritesRepository.workStationId.collectAsState()
  var showChangelog by remember(graph) { mutableStateOf(false) }
  var changelogDialogEntries by remember(graph) { mutableStateOf<List<ChangelogCatalogEntry>>(emptyList()) }
  var currentCity by remember(graph) { mutableStateOf(City.ZARAGOZA) }
  val engagementSnap by settingsRepository.engagementSnapshot.collectAsState()
  var topUpdateBanner by remember { mutableStateOf<TopUpdateBanner>(TopUpdateBanner.Hidden) }
  var showFeedbackNudge by remember { mutableStateOf(false) }
  var showFeedbackDialog by remember { mutableStateOf(false) }
  var showOnboardingFromProfile by rememberSaveable { mutableStateOf(false) }
  var updatePollToken by remember { mutableIntStateOf(0) }

  LaunchedEffect(graph) {
    platformBindings.onGraphCreated(graph)
    onTripRepositoryReady?.invoke(graph.tripRepository)
    onSurfaceMonitoringRepositoryReady?.invoke(graph.surfaceMonitoringRepository)
    onSurfaceSnapshotRepositoryReady?.invoke(graph.surfaceSnapshotRepository)
    graph.surfaceSnapshotRepository.bootstrap()
    graph.surfaceMonitoringRepository.bootstrap()
  }

  LaunchedEffect(graph) {
    settingsBootstrapped = false
    runCatching { settingsRepository.bootstrap() }
    settingsBootstrapped = true
  }

  LaunchedEffect(settingsBootstrapped, platformBindings.appVersion) {
    if (!settingsBootstrapped) return@LaunchedEffect
    val lastSeen = settingsRepository.currentLastSeenChangelogAppVersion() ?: "0.0.0"
    val pending = pendingChangelogVersion(
      platformBindings.appVersion,
      lastSeen,
      ChangelogCatalog.catalogVersionSet(),
    )
    val entries = pending?.let { ChangelogCatalog.entriesFor(it) }.orEmpty()
    println(
      "[BiziRadar][Changelog] current=${platformBindings.appVersion} lastSeen=$lastSeen pending=$pending entries=${entries.size}",
    )
    if (entries.isNotEmpty()) {
      changelogDialogEntries = entries
      showChangelog = true
      println("[BiziRadar][Changelog] showing dialog for $pending")
    }
  }

  LaunchedEffect(settingsBootstrapped, onboardingChecklist.cityConfirmed) {
    if (settingsBootstrapped && onboardingChecklist.cityConfirmed) {
      currentCity = runCatching { settingsRepository.selectedCity.value }.getOrNull() ?: City.ZARAGOZA
    }
  }

  LaunchedEffect(
    settingsBootstrapped,
    favoriteIds,
    onboardingChecklist.cityConfirmed,
    onboardingChecklist.firstStationSaved,
  ) {
    if (!settingsBootstrapped || !onboardingChecklist.cityConfirmed || onboardingChecklist.firstStationSaved) return@LaunchedEffect
    if (favoriteIds.isNotEmpty()) {
      settingsRepository.updateOnboardingChecklist { it.copy(firstStationSaved = true) }
    }
  }

  LaunchedEffect(settingsBootstrapped, homeStationId, workStationId, onboardingChecklist.savedPlacesConfigured) {
    if (!settingsBootstrapped || onboardingChecklist.savedPlacesConfigured) return@LaunchedEffect
    if (homeStationId != null && workStationId != null) {
      settingsRepository.updateOnboardingChecklist { it.copy(savedPlacesConfigured = true) }
    }
  }

  LaunchedEffect(settingsBootstrapped, graph) {
    if (!settingsBootstrapped) return@LaunchedEffect
    runCatching { graph.savedPlaceAlertsRepository.bootstrap() }
    graph.engagementRepository.bootstrap()
    graph.engagementRepository.markSessionStarted()
    graph.engagementRepository.markUsefulSession()
  }

  LaunchedEffect(stationsState.freshness, graph) {
    graph.engagementRepository.markDataFreshnessObserved(stationsState.freshness)
  }

  var previousFavoriteCount by remember(graph) { mutableStateOf(favoriteIds.size) }
  LaunchedEffect(favoriteIds.size, graph) {
    if (favoriteIds.size > previousFavoriteCount) {
      graph.engagementRepository.markFavoriteCreated()
    }
    previousFavoriteCount = favoriteIds.size
  }

  var inAppReviewRequested by remember(graph) { mutableStateOf(false) }

  LaunchedEffect(graph) {
    favoritesBootstrapped = false
    runCatching { favoritesRepository.syncFromPeer() }
    favoritesBootstrapped = true
  }

  LaunchedEffect(
    settingsBootstrapped,
    favoritesBootstrapped,
    stationsState.stations,
    stationsState.lastUpdatedEpoch,
    stationsState.userLocation,
    favoriteIds,
  ) {
    if (settingsBootstrapped && favoritesBootstrapped) {
      graph.surfaceSnapshotRepository.refreshSnapshot()
    }
  }

  LaunchedEffect(graph) {
    minimumSplashElapsed = false
    kotlinx.coroutines.delay(700)
    minimumSplashElapsed = true
  }

  LaunchedEffect(graph, refreshKey, settingsBootstrapped, favoritesBootstrapped) {
    if (settingsBootstrapped && favoritesBootstrapped) {
      runCatching { favoritesRepository.syncFromPeer() }
      stationsRepository.forceRefresh()
      initialLoadAttemptFinished = true
    }
  }

  LaunchedEffect(
    graph,
    stationsState.stations,
    stationsState.isLoading,
    stationsState.errorMessage,
  ) {
    if (stationsState.isLoading || stationsState.stations.isNotEmpty() || stationsState.errorMessage != null) {
      return@LaunchedEffect
    }
    kotlinx.coroutines.delay(5_000)
    val latestState = stationsRepository.state.value
    if (!latestState.isLoading && latestState.stations.isEmpty() && latestState.errorMessage == null) {
      stationsRepository.loadIfNeeded()
    }
  }

  val nearestSelection = remember(stationsState.stations, searchRadiusMeters) {
    selectNearbyStation(stationsState.stations, searchRadiusMeters)
  }

  LaunchedEffect(launchRequest) {
    appState.pendingLaunchRequest = launchRequest
  }

  LaunchedEffect(assistantLaunchRequest) {
    appState.pendingAssistantLaunchRequest = assistantLaunchRequest
  }

  val startupLaunchReady = remember(
    minimumSplashElapsed,
    settingsBootstrapped,
    favoritesBootstrapped,
    initialLoadAttemptFinished,
    stationsState.isLoading,
    stationsState.stations,
    stationsState.errorMessage,
  ) {
    settingsBootstrapped &&
      favoritesBootstrapped &&
      minimumSplashElapsed &&
      (initialLoadAttemptFinished || stationsState.stations.isNotEmpty() || stationsState.errorMessage != null) &&
      !(stationsState.isLoading && stationsState.stations.isEmpty())
  }

  LaunchedEffect(startupLaunchReady) {
    onStartupReadyChanged(startupLaunchReady)
  }

  LaunchedEffect(
    settingsBootstrapped,
    startupLaunchReady,
    engagementSnap.lastUpdateCheckAtEpoch,
    engagementSnap.dismissedUpdateVersion,
  ) {
    if (!settingsBootstrapped || !startupLaunchReady) return@LaunchedEffect
    val day = 24L * 60 * 60 * 1000
    val now = epochMillisForUi()
    val last = engagementSnap.lastUpdateCheckAtEpoch
    if (last != null && now - last < day) return@LaunchedEffect
    graph.engagementRepository.markUpdateChecked(nowEpoch = now)
    when (val u = platformBindings.appUpdatePrompter.checkForUpdate()) {
      is UpdateAvailabilityState.Available -> {
        if (u.versionName == engagementSnap.dismissedUpdateVersion) return@LaunchedEffect
        topUpdateBanner = TopUpdateBanner.Available(u.versionName, u.isFlexibleAllowed, u.storeUrl)
      }
      is UpdateAvailabilityState.Downloaded -> {
        topUpdateBanner = TopUpdateBanner.Downloaded(u.versionName)
      }
      else -> {}
    }
  }

  LaunchedEffect(updatePollToken) {
    if (updatePollToken == 0) return@LaunchedEffect
    repeat(10) {
      delay(8_000)
      when (val u = platformBindings.appUpdatePrompter.checkForUpdate()) {
        is UpdateAvailabilityState.Downloaded -> {
          topUpdateBanner = TopUpdateBanner.Downloaded(u.versionName)
          return@LaunchedEffect
        }
        else -> {}
      }
    }
  }

  LaunchedEffect(
    startupLaunchReady,
    hasCompletedOnboarding,
    onboardingChecklist,
    platformBindings.appVersion,
  ) {
    if (!startupLaunchReady) return@LaunchedEffect
    if (!onboardingChecklist.isCompleted()) return@LaunchedEffect
    if (!graph.engagementRepository.shouldShowFeedbackNudge(platformBindings.appVersion)) return@LaunchedEffect
    showFeedbackNudge = true
    graph.engagementRepository.markFeedbackNudgeShown(platformBindings.appVersion)
  }

  LaunchedEffect(
    startupLaunchReady,
    settingsBootstrapped,
    onboardingChecklist,
    hasCompletedOnboarding,
    stationsState.freshness,
    graph,
    platformBindings.appVersion,
  ) {
    if (!startupLaunchReady || !settingsBootstrapped || inAppReviewRequested) {
      return@LaunchedEffect
    }
    if (!onboardingChecklist.isCompleted()) return@LaunchedEffect
    if (stationsState.freshness == DataFreshness.Unavailable) return@LaunchedEffect
    val eligibility = graph.engagementRepository.reviewEligibility(
      appVersion = platformBindings.appVersion,
      onboardingCompleted = onboardingChecklist.isCompleted() || hasCompletedOnboarding,
      currentFreshness = stationsState.freshness,
    )
    if (eligibility.isEligible) {
      inAppReviewRequested = true
      scope.launch {
        delay(4_000)
        graph.engagementRepository.markReviewPrompted(platformBindings.appVersion)
        platformBindings.reviewPrompter.requestInAppReview()
      }
    }
  }

  LaunchedEffect(startupLaunchReady, appState.pendingLaunchRequest, stationsState.stations, searchRadiusMeters) {
    if (!startupLaunchReady) return@LaunchedEffect
    when (val request = appState.pendingLaunchRequest ?: return@LaunchedEffect) {
      MobileLaunchRequest.Home -> {
        navController.navigate(Screen.Nearby) { launchSingleTop = true }
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.Map -> {
        navController.navigate(Screen.Map) { launchSingleTop = true }
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.Favorites -> {
        navController.navigate(Screen.Favorites) { launchSingleTop = true }
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.SavedPlaceAlerts -> {
        navController.navigate(Screen.SavedPlaceAlerts) { launchSingleTop = true }
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.NearestStation -> {
        val station = nearestSelection.highlightedStation ?: return@LaunchedEffect
        navController.navigate(Screen.StationDetail(station.id))
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.NearestStationWithBikes -> {
        val station = selectNearbyStation(
          stationsState.stations,
          searchRadiusMeters,
        ) { station -> station.bikesAvailable > 0 }.highlightedStation ?: return@LaunchedEffect
        navController.navigate(Screen.StationDetail(station.id))
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.NearestStationWithSlots -> {
        val station = selectNearbyStation(
          stationsState.stations,
          searchRadiusMeters,
        ) { station -> station.slotsFree > 0 }.highlightedStation ?: return@LaunchedEffect
        navController.navigate(Screen.StationDetail(station.id))
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.OpenAssistant -> {
        navController.navigate(Screen.Shortcuts) { launchSingleTop = true }
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.StationStatus -> {
        val station = stationsState.stations.firstOrNull() ?: return@LaunchedEffect
        appState.pendingAssistantAction = AssistantAction.StationStatus(station.id)
        navController.navigate(Screen.Shortcuts) { launchSingleTop = true }
        appState.pendingLaunchRequest = null
      }
      is MobileLaunchRequest.MonitorStation -> {
        val station = stationsRepository.stationById(request.stationId)
          ?: stationsState.stations.firstOrNull { it.id == request.stationId }
          ?: return@LaunchedEffect
        val notificationsGranted = platformBindings.localNotifier.requestPermission()
        graph.surfaceSnapshotRepository.refreshSnapshot()
        if (notificationsGranted) {
          graph.surfaceMonitoringRepository.startMonitoring(
            stationId = station.id,
            durationSeconds = DEFAULT_SURFACE_MONITORING_DURATION_SECONDS,
            kind = SurfaceMonitoringKind.Docks,
          )
        }
        navController.navigate(Screen.StationDetail(station.id))
        appState.pendingLaunchRequest = null
      }
      is MobileLaunchRequest.SelectCity -> {
        val city = City.fromId(request.cityId) ?: return@LaunchedEffect
        settingsRepository.setSelectedCity(city)
        stationsRepository.forceRefresh()
        navController.navigate(Screen.Nearby) { launchSingleTop = true }
        appState.pendingLaunchRequest = null
      }
      is MobileLaunchRequest.RouteToStation -> {
        val station = request.stationId?.let(stationsRepository::stationById)
          ?: stationsState.stations.firstOrNull()
          ?: return@LaunchedEffect
        graph.routeLauncher.launch(station)
        appState.pendingLaunchRequest = null
      }
      is MobileLaunchRequest.ShowStation -> {
        if (stationsRepository.stationById(request.stationId) == null) return@LaunchedEffect
        navController.navigate(Screen.StationDetail(request.stationId))
        appState.pendingLaunchRequest = null
      }
    }
  }

  LaunchedEffect(startupLaunchReady, appState.pendingAssistantLaunchRequest, stationsState.stations) {
    if (!startupLaunchReady) return@LaunchedEffect
    val request = appState.pendingAssistantLaunchRequest ?: return@LaunchedEffect
    val station = resolveLaunchStation(
      stations = stationsState.stations,
      graph = graph,
      stationId = when (request) {
        is AssistantLaunchRequest.RouteToStation -> request.stationId
        is AssistantLaunchRequest.SearchStation -> null
        is AssistantLaunchRequest.StationBikeCount -> request.stationId
        is AssistantLaunchRequest.StationSlotCount -> request.stationId
        is AssistantLaunchRequest.StationStatus -> request.stationId
      },
      stationQuery = when (request) {
        is AssistantLaunchRequest.RouteToStation -> request.stationQuery
        is AssistantLaunchRequest.SearchStation -> request.stationQuery
        is AssistantLaunchRequest.StationBikeCount -> request.stationQuery
        is AssistantLaunchRequest.StationSlotCount -> request.stationQuery
        is AssistantLaunchRequest.StationStatus -> request.stationQuery
      },
    )

    when (request) {
      is AssistantLaunchRequest.SearchStation -> {
        appState.searchQuery = request.stationQuery
        if (station != null) {
          navController.navigate(Screen.StationDetail(station.id))
        } else {
          navController.navigate(Screen.Map) { launchSingleTop = true }
        }
      }
      is AssistantLaunchRequest.StationStatus -> {
        if (station != null) {
          appState.pendingAssistantAction = AssistantAction.StationStatus(station.id)
          navController.navigate(Screen.Shortcuts) { launchSingleTop = true }
        } else {
          appState.searchQuery = request.stationQuery.orEmpty()
          navController.navigate(Screen.Map) { launchSingleTop = true }
        }
      }
      is AssistantLaunchRequest.StationBikeCount -> {
        if (station != null) {
          appState.pendingAssistantAction = AssistantAction.StationBikeCount(station.id)
          navController.navigate(Screen.Shortcuts) { launchSingleTop = true }
        } else {
          appState.searchQuery = request.stationQuery.orEmpty()
          navController.navigate(Screen.Map) { launchSingleTop = true }
        }
      }
      is AssistantLaunchRequest.StationSlotCount -> {
        if (station != null) {
          appState.pendingAssistantAction = AssistantAction.StationSlotCount(station.id)
          navController.navigate(Screen.Shortcuts) { launchSingleTop = true }
        } else {
          appState.searchQuery = request.stationQuery.orEmpty()
          navController.navigate(Screen.Map) { launchSingleTop = true }
        }
      }
      is AssistantLaunchRequest.RouteToStation -> {
        if (station != null) {
          graph.routeLauncher.launch(station)
        } else {
          appState.searchQuery = request.stationQuery.orEmpty()
          navController.navigate(Screen.Map) { launchSingleTop = true }
        }
      }
    }

    appState.pendingAssistantLaunchRequest = null
  }

  val filteredStations = remember(stationsState.stations, appState.searchQuery) {
    filterStations(stationsState.stations, appState.searchQuery)
  }
  val showStartupSplash = remember(
    useInAppStartupSplash,
    startupLaunchReady,
  ) {
    useInAppStartupSplash && !startupLaunchReady
  }

  BiziTheme(mobilePlatform, themePreference) {
    val windowLayout = rememberBiziWindowLayout()
    CompositionLocalProvider(LocalBiziWindowLayout provides windowLayout) {
      Surface(
        modifier = modifier.fillMaxSize(),
        color = pageBackgroundColor(mobilePlatform),
      ) {
        when {
          !onboardingChecklist.isCompleted() && !onboardingChecklist.cityConfirmed -> {
            CitySelectionScreen(
              onCitySelected = { city ->
                scope.launch {
                  settingsRepository.setSelectedCity(city)
                  settingsRepository.updateOnboardingChecklist { it.copy(cityConfirmed = true) }
                  favoritesRepository.clearAll()
                  stationsRepository.forceRefresh()
                }
              },
            )
          }
          !onboardingChecklist.isCompleted() -> {
            GuidedOnboardingFlow(
              checklist = onboardingChecklist,
              platformBindings = platformBindings,
              scope = scope,
              settingsRepository = settingsRepository,
              onOpenFavorites = {
                runCatching {
                  navController.navigate(Screen.Favorites) { launchSingleTop = true }
                }
              },
            )
          }
          else -> {
            Box(Modifier.fillMaxSize()) {
              if (showChangelog && changelogDialogEntries.isNotEmpty()) {
                val onChangelogDismiss = remember(changelogDialogEntries) {
                  {
                    showChangelog = false
                    scope.launch {
                      settingsRepository.setLastSeenChangelogAppVersion(
                        normalizeAppVersionForCatalog(platformBindings.appVersion) ?: platformBindings.appVersion,
                      )
                    }
                    println("[BiziRadar][Changelog] dismissed and persisted=${platformBindings.appVersion}")
                    Unit
                  }
                }
                ChangelogDialog(entries = changelogDialogEntries, onDismiss = onChangelogDismiss)
              }
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
            val tripViewModelFactory = remember(graph) {
              com.gcaguilar.biciradar.mobileui.viewmodel.TripViewModelFactory(
                tripRepository = graph.tripRepository,
                surfaceMonitoringRepository = graph.surfaceMonitoringRepository,
                geoSearchUseCase = graph.geoSearchUseCase,
                reverseGeocodeUseCase = graph.reverseGeocodeUseCase,
                settingsRepository = graph.settingsRepository,
              )
            }
            val nearbyViewModelFactory = remember(graph) {
              com.gcaguilar.biciradar.mobileui.viewmodel.NearbyViewModelFactory(
                stationsRepository = graph.stationsRepository,
                favoritesRepository = graph.favoritesRepository,
                routeLauncher = graph.routeLauncher,
                settingsRepository = graph.settingsRepository,
              )
            }
            val favoritesViewModelFactory = remember(graph) {
              com.gcaguilar.biciradar.mobileui.viewmodel.FavoritesViewModelFactory(
                favoritesRepository = graph.favoritesRepository,
                stationsRepository = graph.stationsRepository,
                settingsRepository = graph.settingsRepository,
                savedPlaceAlertsRepository = graph.savedPlaceAlertsRepository,
                routeLauncher = graph.routeLauncher,
              )
            }
            val profileViewModelFactory = remember(graph, platformBindings) {
              com.gcaguilar.biciradar.mobileui.viewmodel.ProfileViewModelFactory(
                settingsRepository = graph.settingsRepository,
                stationsRepository = graph.stationsRepository,
                favoritesRepository = graph.favoritesRepository,
                permissionPrompter = platformBindings.permissionPrompter,
                localNotifier = platformBindings.localNotifier,
              )
            }
            val savedPlaceAlertsViewModelFactory = remember(graph) {
              com.gcaguilar.biciradar.mobileui.viewmodel.SavedPlaceAlertsViewModelFactory(
                savedPlaceAlertsRepository = graph.savedPlaceAlertsRepository,
              )
            }
            val stationDetailViewModelFactory = remember(graph) {
              com.gcaguilar.biciradar.mobileui.viewmodel.StationDetailViewModelFactory(
                favoritesRepository = graph.favoritesRepository,
                settingsRepository = graph.settingsRepository,
                savedPlaceAlertsRepository = graph.savedPlaceAlertsRepository,
                datosBiziApi = graph.datosBiziApi,
                routeLauncher = graph.routeLauncher,
              )
            }
            val onRefreshStations = remember(scope, stationsRepository) {
              {
                scope.launch { stationsRepository.forceRefresh() }
                Unit
              }
            }
            Box(modifier = Modifier.fillMaxSize()) {
              BiziNavigationShell(
                mobilePlatform = mobilePlatform,
                navController = navController,
                windowLayout = windowLayout,
              ) { innerPadding ->
                BiziNavHost(
                  navController = navController,
                  mobilePlatform = mobilePlatform,
                  tripViewModelFactory = tripViewModelFactory,
                  nearbyViewModelFactory = nearbyViewModelFactory,
                  favoritesViewModelFactory = favoritesViewModelFactory,
                  profileViewModelFactory = profileViewModelFactory,
                  savedPlaceAlertsViewModelFactory = savedPlaceAlertsViewModelFactory,
                  stationDetailViewModelFactory = stationDetailViewModelFactory,
                  stations = filteredStations,
                  favoriteIds = favoriteIds,
                  loading = stationsState.isLoading,
                  errorMessage = stationsState.errorMessage,
                  stationsFreshness = stationsState.freshness,
                  stationsLastUpdatedEpoch = stationsState.lastUpdatedEpoch,
                  onRefreshStations = onRefreshStations,
                  nearestSelection = nearestSelection,
                  userLocation = stationsState.userLocation,
                  searchQuery = appState.searchQuery,
                  searchRadiusMeters = searchRadiusMeters,
                  isMapReady = isMapReady,
                  onSearchQueryChange = remember(appState) { { appState.searchQuery = it } },
                  onRetry = remember(scope, stationsRepository) { { scope.launch { stationsRepository.loadIfNeeded() } } },
                  onFavoriteToggle = remember(scope, favoritesRepository) { { station -> scope.launch { favoritesRepository.toggle(station.id) } } },
                  onQuickRoute = remember(graph, scope) {
                    { station ->
                      scope.launch {
                        graph.engagementRepository.markRouteOpened()
                        graph.routeLauncher.launch(station)
                      }
                    }
                  },
                  onOpenAssistant = remember(navController) { { navController.navigate(Screen.Shortcuts) { launchSingleTop = true } } },
                  localNotifier = platformBindings.localNotifier,
                  routeLauncher = graph.routeLauncher,
                  platformBindings = platformBindings,
                  graph = graph,
                  stationsRepository = stationsRepository,
                  initialAssistantAction = appState.pendingAssistantAction,
                  onInitialActionConsumed = remember(appState) { { appState.pendingAssistantAction = null } },
                  onOpenOnboarding = { showOnboardingFromProfile = true },
                  onShowChangelogManual = {
                    val entries = ChangelogCatalog.entriesFor(platformBindings.appVersion)
                    if (entries.isNotEmpty()) {
                      changelogDialogEntries = entries
                      showChangelog = true
                    }
                  },
                  paddingValues = innerPadding,
                )
              }
              EngagementTopOverlays(
                updateBanner = topUpdateBanner,
                showFeedbackNudge = showFeedbackNudge,
                onDismissAvailableUpdate = { version ->
                  scope.launch {
                    graph.engagementRepository.markUpdateBannerDismissed(version, epochMillisForUi())
                  }
                  topUpdateBanner = TopUpdateBanner.Hidden
                },
                onDismissDownloadedUpdate = { topUpdateBanner = TopUpdateBanner.Hidden },
                onStartUpdate = {
                  scope.launch {
                    val banner = topUpdateBanner as? TopUpdateBanner.Available ?: return@launch
                    if (banner.flexible) {
                      if (platformBindings.appUpdatePrompter.startFlexibleUpdate()) {
                        updatePollToken++
                      }
                    } else {
                      platformBindings.appUpdatePrompter.openStoreListing()
                    }
                  }
                },
                onRestartToUpdate = {
                  scope.launch {
                    platformBindings.appUpdatePrompter.completeFlexibleUpdateIfReady()
                  }
                },
                onFeedbackSend = {
                  scope.launch { graph.engagementRepository.markFeedbackOpened() }
                  showFeedbackDialog = true
                  showFeedbackNudge = false
                },
                onFeedbackDismiss = {
                  scope.launch { graph.engagementRepository.markFeedbackDismissed() }
                  showFeedbackNudge = false
                },
              )
              if (showFeedbackDialog) {
                FeedbackDialog(
                  onDismiss = { showFeedbackDialog = false },
                  onOpenFeedbackForm = {
                    platformBindings.externalLinks.openFeedbackForm()
                    showFeedbackDialog = false
                  },
                )
              }
            }
                }
              }
            }
          }
        }
      }
    }

  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BiziTheme(
  mobilePlatform: MobileUiPlatform,
  themePreference: ThemePreference = ThemePreference.System,
  content: @Composable () -> Unit,
) {
  val isDark = when (themePreference) {
    ThemePreference.Light -> false
    ThemePreference.Dark -> true
    ThemePreference.System -> isSystemInDarkTheme()
  }
  val dynamicColorScheme = platformDynamicColorScheme(isDark)
  val colors = dynamicColorScheme?.let { dynamicScheme ->
    dynamicBiziColors(dynamicScheme, mobilePlatform, isDark)
  } ?: if (isDark) {
    DarkBiziColors
  } else {
    LightBiziColors
  }
  CompositionLocalProvider(LocalBiziColors provides colors) {
    MaterialTheme(
      colorScheme = dynamicColorScheme ?: biziColorScheme(
        isDark = isDark,
        colors = colors,
        mobilePlatform = mobilePlatform,
      ),
      motionScheme = MotionScheme.expressive(),
      content = content,
    )
  }
}

@Composable
private fun BoxScope.EngagementTopOverlays(
  updateBanner: TopUpdateBanner,
  showFeedbackNudge: Boolean,
  onDismissAvailableUpdate: (String) -> Unit,
  onDismissDownloadedUpdate: () -> Unit,
  onStartUpdate: () -> Unit,
  onRestartToUpdate: () -> Unit,
  onFeedbackSend: () -> Unit,
  onFeedbackDismiss: () -> Unit,
) {
  val c = LocalBiziColors.current
  Column(
    modifier = Modifier
      .align(Alignment.TopCenter)
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(horizontal = 12.dp, vertical = 8.dp)
      .zIndex(4f),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    when (val b = updateBanner) {
      TopUpdateBanner.Hidden -> Unit
      is TopUpdateBanner.Available -> {
        Surface(
          color = c.blue.copy(alpha = 0.12f),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = stringResource(Res.string.updateAvailableTitle),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = c.ink,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              TextButton(onClick = onStartUpdate) {
                Text(stringResource(Res.string.updateNow))
              }
              TextButton(onClick = { onDismissAvailableUpdate(b.version) }) {
                Text(stringResource(Res.string.updateDismiss))
              }
            }
          }
        }
      }
      is TopUpdateBanner.Downloaded -> {
        Surface(
          color = c.green.copy(alpha = 0.12f),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = stringResource(Res.string.restartToUpdate),
              style = MaterialTheme.typography.bodyMedium,
              color = c.ink,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              TextButton(onClick = onRestartToUpdate) {
                Text(stringResource(Res.string.restartToUpdate))
              }
              TextButton(onClick = onDismissDownloadedUpdate) {
                Text(stringResource(Res.string.close))
              }
            }
          }
        }
      }
    }
    if (showFeedbackNudge) {
      Surface(
        color = c.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, c.panel),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = stringResource(Res.string.feedbackNudgeTitle),
            fontWeight = FontWeight.SemiBold,
            color = c.ink,
          )
          Text(
            text = stringResource(Res.string.feedbackNudgeBody),
            style = MaterialTheme.typography.bodySmall,
            color = c.muted,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = onFeedbackSend) {
              Text(stringResource(Res.string.feedbackNudgeAction))
            }
            TextButton(onClick = onFeedbackDismiss) {
              Text(stringResource(Res.string.updateDismiss))
            }
          }
        }
      }
    }
  }
}

@Composable
private fun FeedbackDialog(
  onDismiss: () -> Unit,
  onOpenFeedbackForm: () -> Unit,
) {
  val c = LocalBiziColors.current
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = c.surface,
    tonalElevation = 6.dp,
    title = {
      Text(
        text = stringResource(Res.string.feedbackAndSuggestions),
        color = c.ink,
        fontWeight = FontWeight.SemiBold,
      )
    },
    text = {
      Text(
        text = stringResource(Res.string.feedbackDescription),
        color = c.muted,
      )
    },
    confirmButton = {
      TextButton(onClick = onOpenFeedbackForm) {
        Text(stringResource(Res.string.openFeedbackForm))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(Res.string.close))
      }
    },
  )
}

@Composable
private fun BiziNavigationShell(
  mobilePlatform: MobileUiPlatform,
  navController: NavHostController,
  windowLayout: BiziWindowLayout,
  content: @Composable (PaddingValues) -> Unit,
) {
  if (windowLayout == BiziWindowLayout.Compact) {
    Scaffold(
      containerColor = pageBackgroundColor(mobilePlatform),
      bottomBar = {
        MobileBottomNavigationBar(
          mobilePlatform = mobilePlatform,
          navController = navController,
        )
      },
    ) { innerPadding ->
      content(innerPadding)
    }
    return
  }

  Row(
    modifier = Modifier
      .fillMaxSize()
      .background(pageBackgroundColor(mobilePlatform)),
  ) {
    MobileNavigationRail(
      mobilePlatform = mobilePlatform,
      navController = navController,
    )
    VerticalDivider(color = LocalBiziColors.current.panel)
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxHeight(),
    ) {
      content(PaddingValues())
    }
  }
}

@Composable
private fun rememberBiziWindowLayout(): BiziWindowLayout {
  val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
  return when {
    windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) &&
      windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) -> BiziWindowLayout.Expanded
    windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) &&
      windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) -> BiziWindowLayout.Medium
    else -> BiziWindowLayout.Compact
  }
}

private fun biziColorScheme(
  isDark: Boolean,
  colors: BiziColors,
  mobilePlatform: MobileUiPlatform,
) = if (isDark) {
  darkColorScheme(
    primary = colors.red,
    onPrimary = colors.onAccent,
    secondary = colors.green,
    onSecondary = colors.onAccent,
    tertiary = colors.blue,
    onTertiary = colors.onAccent,
    background = if (mobilePlatform == MobileUiPlatform.IOS) colors.groupedBackground else colors.background,
    onBackground = colors.ink,
    surface = colors.surface,
    onSurface = colors.ink,
    surfaceVariant = if (mobilePlatform == MobileUiPlatform.IOS) colors.panel else colors.background,
    onSurfaceVariant = colors.muted,
    outline = colors.panel,
    inverseSurface = colors.ink,
    inverseOnSurface = colors.surface,
  )
} else {
  lightColorScheme(
    primary = colors.red,
    onPrimary = colors.onAccent,
    secondary = colors.green,
    onSecondary = colors.onAccent,
    tertiary = colors.blue,
    onTertiary = colors.onAccent,
    background = if (mobilePlatform == MobileUiPlatform.IOS) colors.groupedBackground else colors.background,
    onBackground = colors.ink,
    surface = colors.surface,
    onSurface = colors.ink,
    surfaceVariant = if (mobilePlatform == MobileUiPlatform.IOS) colors.panel else colors.background,
    onSurfaceVariant = colors.muted,
    outline = colors.panel,
    inverseSurface = colors.ink,
    inverseOnSurface = colors.surface,
  )
}

private fun dynamicBiziColors(
  colorScheme: ColorScheme,
  mobilePlatform: MobileUiPlatform,
  isDark: Boolean,
): BiziColors = BiziColors(
  background = colorScheme.background,
  groupedBackground = if (mobilePlatform == MobileUiPlatform.IOS) colorScheme.surface else colorScheme.background,
  surface = colorScheme.surface,
  ink = colorScheme.onSurface,
  muted = colorScheme.onSurfaceVariant,
  panel = colorScheme.surfaceVariant,
  red = colorScheme.primary,
  blue = colorScheme.tertiary,
  green = colorScheme.secondary,
  orange = colorScheme.tertiary,
  purple = colorScheme.secondary,
  onAccent = colorScheme.onPrimary,
  navBar = colorScheme.surface,
  navBarIos = colorScheme.surface.copy(alpha = 0.96f),
  fieldSurfaceIos = colorScheme.surface,
  fieldSurfaceAndroid = colorScheme.surfaceVariant,
  dismissAlphaBase = if (isDark) 0.16f else 0.10f,
)

@Composable
private fun StartupSplashScreen(
  mobilePlatform: MobileUiPlatform,
) {
  val c = LocalBiziColors.current
  val backgroundColor = if (mobilePlatform == MobileUiPlatform.IOS) c.groupedBackground else c.background
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(backgroundColor),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Surface(
        shape = CircleShape,
        color = c.red,
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
          contentDescription = null,
          tint = c.onAccent,
          modifier = Modifier.padding(18.dp).size(30.dp),
        )
      }
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Text(
          text = stringResource(Res.string.appName),
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = c.red,
        )
        Text(
          text = stringResource(Res.string.loadingStationsFavoritesShortcuts),
          style = MaterialTheme.typography.bodyMedium,
          color = c.muted,
        )
      }
      Text(
        text = if (mobilePlatform == MobileUiPlatform.IOS) {
          stringResource(Res.string.preparingIphoneExperience)
        } else {
          stringResource(Res.string.preparingAndroidExperience)
        },
        style = MaterialTheme.typography.labelMedium,
        color = c.muted,
      )
    }
  }
}

@Composable
private fun MobileBottomNavigationBar(
  mobilePlatform: MobileUiPlatform,
  navController: NavHostController,
) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  NavigationBar(
    containerColor = if (mobilePlatform == MobileUiPlatform.IOS) {
      LocalBiziColors.current.navBarIos
    } else {
      LocalBiziColors.current.navBar
    },
  ) {
    MobileTabs.forEach { tab ->
      val screen = tab.screen()
      NavigationBarItem(
        selected = currentRoute?.contains(screen::class.qualifiedName.orEmpty()) == true,
        onClick = { navController.navigateToPrimaryDestination(screen) },
        icon = {
          Icon(
            imageVector = tab.icon(),
            contentDescription = stringResource(tab.labelKey),
          )
        },
        label = { Text(stringResource(tab.labelKey)) },
      )
    }
  }
}

@Composable
private fun MobileNavigationRail(
  mobilePlatform: MobileUiPlatform,
  navController: NavHostController,
) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val colors = LocalBiziColors.current

  NavigationRail(
    modifier = Modifier
      .fillMaxHeight()
      .padding(vertical = 12.dp),
    containerColor = if (mobilePlatform == MobileUiPlatform.IOS) {
      colors.navBarIos
    } else {
      colors.navBar
    },
    header = {
      Column(
        modifier = Modifier.padding(bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Surface(
          shape = CircleShape,
          color = colors.red,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
            contentDescription = null,
            tint = colors.onAccent,
            modifier = Modifier.padding(12.dp).size(20.dp),
          )
        }
        Text(
          text = stringResource(Res.string.appName),
          style = MaterialTheme.typography.labelSmall,
          color = colors.muted,
        )
      }
    },
  ) {
    MobileTabs.forEach { tab ->
      val screen = tab.screen()
      NavigationRailItem(
        selected = currentRoute?.contains(screen::class.qualifiedName.orEmpty()) == true,
        onClick = { navController.navigateToPrimaryDestination(screen) },
        icon = {
          Icon(
            imageVector = tab.icon(),
            contentDescription = stringResource(tab.labelKey),
          )
        },
        label = { Text(stringResource(tab.labelKey)) },
      )
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MapScreen(
  mobilePlatform: MobileUiPlatform,
  stations: List<Station>,
  favoriteIds: Set<String>,
  loading: Boolean,
  errorMessage: String?,
  dataFreshness: DataFreshness,
  lastUpdatedEpoch: Long?,
  onRefreshStations: () -> Unit,
  nearestSelection: com.gcaguilar.biciradar.core.NearbyStationSelection,
  searchQuery: String,
  searchRadiusMeters: Int,
  userLocation: GeoPoint?,
  isMapReady: Boolean,
  onSearchQueryChange: (String) -> Unit,
  onStationSelected: (Station) -> Unit,
  onRetry: () -> Unit,
  onFavoriteToggle: (Station) -> Unit,
  onQuickRoute: (Station) -> Unit,
  environmentalRepository: EnvironmentalRepository,
  paddingValues: PaddingValues,
) {
  val nearestStation = nearestSelection.highlightedStation
  var selectedMapStationId by rememberSaveable { mutableStateOf<String?>(null) }
  var hasExplicitMapSelection by rememberSaveable { mutableStateOf(false) }
  var isCardDismissed by rememberSaveable { mutableStateOf(false) }
  var showEnvironmentalSheet by rememberSaveable { mutableStateOf(false) }
  var activeFilters by remember { mutableStateOf(emptySet<MapFilter>()) }
  var recenterRequestToken by rememberSaveable { mutableStateOf(0) }
  val activeEnvironmentalLayer = remember(activeFilters) {
    when {
      MapFilter.AIR_QUALITY in activeFilters -> EnvironmentalLayer.AirQuality
      MapFilter.POLLEN in activeFilters -> EnvironmentalLayer.Pollen
      else -> null
    }
  }
  val stationAvailabilityFilters = remember(activeFilters) {
    activeFilters.filterNot { it == MapFilter.AIR_QUALITY || it == MapFilter.POLLEN }.toSet()
  }

  val mapStations = remember(stations, stationAvailabilityFilters) {
    if (stationAvailabilityFilters.isEmpty()) {
      stations
    } else {
      stations.filter { station ->
        stationAvailabilityFilters.any { filter ->
          when (filter) {
            MapFilter.BIKES_AND_SLOTS -> station.bikesAvailable > 0 && station.slotsFree > 0
            MapFilter.ONLY_BIKES -> station.bikesAvailable > 0 && station.slotsFree == 0
            MapFilter.ONLY_SLOTS -> station.bikesAvailable == 0 && station.slotsFree > 0
            MapFilter.ONLY_EBIKES -> station.ebikesAvailable > 0
            MapFilter.ONLY_REGULAR_BIKES -> station.regularBikesAvailable > 0
            MapFilter.AIR_QUALITY,
            MapFilter.POLLEN -> true
          }
        }
      }
    }
  }
  val estimatedEnvironmentalSnapshots = remember(stations, activeEnvironmentalLayer) {
    if (activeEnvironmentalLayer == null) {
      emptyList()
    } else {
      buildEnvironmentalZoneSnapshots(stations)
    }
  }
  var environmentalSnapshots by remember { mutableStateOf<List<ZoneEnvironmentalSnapshot>>(emptyList()) }
  LaunchedEffect(estimatedEnvironmentalSnapshots, activeEnvironmentalLayer) {
    if (activeEnvironmentalLayer == null) {
      environmentalSnapshots = emptyList()
      showEnvironmentalSheet = false
      return@LaunchedEffect
    }
    environmentalSnapshots = estimatedEnvironmentalSnapshots.map { zone ->
      val reading = environmentalRepository.readingAt(zone.centerLatitude, zone.centerLongitude)
      zone.copy(
        airQualityScore = reading?.airQualityIndex,
        pollenScore = reading?.pollenIndex,
      )
    }
  }

  // Reset dismiss when the selected station changes (user tapped a marker).
  LaunchedEffect(selectedMapStationId) {
    isCardDismissed = false
  }

  LaunchedEffect(stations, nearestStation?.id, searchQuery) {
    val hasSelectedStation = selectedMapStationId != null && stations.any { station -> station.id == selectedMapStationId }
    if (!hasSelectedStation) {
      selectedMapStationId = if (searchQuery.isNotBlank()) {
        stations.firstOrNull()?.id
      } else {
        nearestStation?.id ?: stations.firstOrNull()?.id
      }
      hasExplicitMapSelection = false
    }
  }

  val selectedMapStation = remember(selectedMapStationId, stations, nearestStation, searchQuery) {
    selectedMapStationId?.let { id -> stations.firstOrNull { station -> station.id == id } }
      ?: when {
        stations.isEmpty() -> null
        searchQuery.isNotBlank() -> stations.firstOrNull()
        else -> nearestStation?.takeIf { nearest -> stations.any { it.id == nearest.id } } ?: stations.firstOrNull()
      }
  }
  val mapIsShowingNearestSelection = !hasExplicitMapSelection && selectedMapStation?.id == nearestStation?.id
  val mapIsShowingNearestFallback = selectedMapStation?.id == nearestStation?.id && nearestSelection.usesFallback

  LaunchedEffect(searchQuery, stations) {
    if (searchQuery.isBlank()) return@LaunchedEffect
    val firstMatch = stations.firstOrNull() ?: return@LaunchedEffect
    selectedMapStationId = firstMatch.id
    hasExplicitMapSelection = false
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(pageBackgroundColor(mobilePlatform)),
  ) {
    val platformEnvironmentalOverlay = activeEnvironmentalLayer?.let { layer ->
      EnvironmentalOverlayData(
        layer = when (layer) {
          EnvironmentalLayer.AirQuality -> EnvironmentalOverlayLayer.AirQuality
          EnvironmentalLayer.Pollen -> EnvironmentalOverlayLayer.Pollen
        },
        zones = environmentalSnapshots
          .ifEmpty { estimatedEnvironmentalSnapshots }
          .mapNotNull { zone ->
            val value = when (layer) {
              EnvironmentalLayer.AirQuality -> zone.airQualityScore
              EnvironmentalLayer.Pollen -> zone.pollenScore
            } ?: return@mapNotNull null
            EnvironmentalOverlayZone(
              center = GeoPoint(zone.centerLatitude, zone.centerLongitude),
              value = value,
            )
          },
      )
    }

    PlatformStationMap(
      modifier = Modifier.fillMaxSize(),
      stations = mapStations,
      userLocation = userLocation,
      highlightedStationId = selectedMapStation?.id,
      isMapReady = isMapReady,
      onStationSelected = { station ->
        hasExplicitMapSelection = true
        selectedMapStationId = station.id
      },
      recenterRequestToken = recenterRequestToken,
      environmentalOverlay = platformEnvironmentalOverlay,
    )

    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      StationSearchField(
        mobilePlatform = mobilePlatform,
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        label = stringResource(Res.string.mapSearchStationOrAddress),
      )
      if (mobilePlatform != MobileUiPlatform.Desktop) {
        MapFilterChipRow(
          activeFilters = activeFilters,
          onToggleFilter = { filter ->
            val next = if (filter in activeFilters) emptySet<MapFilter>() else setOf(filter)
            activeFilters = next
            showEnvironmentalSheet = filter == MapFilter.AIR_QUALITY || filter == MapFilter.POLLEN
          },
        )
      }
      DataFreshnessBanner(
        freshness = dataFreshness,
        lastUpdatedEpoch = lastUpdatedEpoch,
        loading = loading,
        onRefresh = onRefreshStations,
        modifier = Modifier.padding(bottom = 8.dp),
      )
    }

    AnimatedVisibility(
      modifier = Modifier
        .align(Alignment.Center)
        .padding(horizontal = 16.dp),
      visible = errorMessage != null || (!loading && stations.isEmpty()),
      enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
      exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
      label = "map-centered-feedback",
    ) {
      if (errorMessage != null) {
        EmptyStateCard(
          title = stringResource(Res.string.mapUpdateFailed),
          description = errorMessage,
          primaryAction = stringResource(Res.string.retry),
          onPrimaryAction = onRetry,
        )
      } else {
        EmptyStateCard(
          title = if (searchQuery.isBlank()) {
            stringResource(Res.string.mapNoStationsOnScreen)
          } else {
            stringResource(Res.string.mapNoStations)
          },
          description = if (searchQuery.isBlank()) {
            stringResource(Res.string.mapLocationFallbackDescription)
          } else {
            stringResource(Res.string.mapTryAnotherQuery)
          },
          primaryAction = stringResource(Res.string.loadStations),
          onPrimaryAction = onRetry,
        )
      }
    }

    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom,
        ) {
          AnimatedVisibility(
            visible = selectedMapStation != null && !isCardDismissed,
            modifier = Modifier.weight(1f),
            enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(animationSpec = tween(140)),
            label = "map-selected-station-overlay",
          ) {
            selectedMapStation?.let { station ->
              MapSelectedStationCard(
                modifier = Modifier.fillMaxWidth(),
                mobilePlatform = mobilePlatform,
                station = station,
                isFavorite = station.id in favoriteIds,
                isShowingNearestSelection = mapIsShowingNearestSelection,
                isFallbackSelection = mapIsShowingNearestFallback,
                searchRadiusMeters = searchRadiusMeters,
                onFavoriteToggle = { onFavoriteToggle(station) },
                onOpenStationDetails = { onStationSelected(station) },
                onQuickRoute = { onQuickRoute(station) },
                onDismiss = { isCardDismissed = true },
              )
            }
          }
          if (mobilePlatform != MobileUiPlatform.Desktop) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              activeEnvironmentalLayer?.let {
                MapEnvironmentalSheetButton(
                  onClick = { showEnvironmentalSheet = true },
                )
              }
              MapRecenterButton(
                enabled = userLocation != null || stations.isNotEmpty(),
                onClick = {
                  recenterRequestToken += 1
                  isCardDismissed = false
                },
              )
            }
          }
        }
      }
    }

    if (activeEnvironmentalLayer != null && showEnvironmentalSheet) {
      val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
      ModalBottomSheet(
        onDismissRequest = { showEnvironmentalSheet = false },
        sheetState = sheetState,
        containerColor = LocalBiziColors.current.surface,
      ) {
        EnvironmentalLayerCard(
          layer = activeEnvironmentalLayer,
          zones = if (environmentalSnapshots.isNotEmpty()) environmentalSnapshots else estimatedEnvironmentalSnapshots,
          onClear = {
            showEnvironmentalSheet = false
            activeFilters = emptySet()
          },
        )
      }
    }
  }

@Composable
private fun NearbyScreen(
  mobilePlatform: MobileUiPlatform,
  stations: List<Station>,
  favoriteIds: Set<String>,
  loading: Boolean,
  errorMessage: String?,
  dataFreshness: DataFreshness,
  lastUpdatedEpoch: Long?,
  nearestSelection: com.gcaguilar.biciradar.core.NearbyStationSelection,
  searchRadiusMeters: Int,
  onStationSelected: (Station) -> Unit,
  onRetry: () -> Unit,
  onRefresh: () -> Unit,
  onFavoriteToggle: (Station) -> Unit,
  onQuickRoute: (Station) -> Unit,
  refreshCountdownSeconds: Int,
  paddingValues: PaddingValues,
) {
  val nearestWithBikesSelection = remember(stations, searchRadiusMeters) {
    selectNearbyStation(stations, searchRadiusMeters) { station -> station.bikesAvailable > 0 }
  }
  val nearestWithSlotsSelection = remember(stations, searchRadiusMeters) {
    selectNearbyStation(stations, searchRadiusMeters) { station -> station.slotsFree > 0 }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(pageBackgroundColor(mobilePlatform)),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier = Modifier.responsivePageWidth(),
    ) {
      // Header + quick-action cards — always visible, never scroll away
      Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        if (mobilePlatform == MobileUiPlatform.IOS) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = stringResource(Res.string.nearby),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = stringResource(Res.string.nearbyQuickActionsDescription),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalBiziColors.current.muted,
              )
            }
            RefreshButtonWithCountdown(
              countdown = refreshCountdownSeconds,
              loading = loading,
              onRefresh = onRefresh,
            )
          }
        } else {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = stringResource(Res.string.nearbyNearYou),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = LocalBiziColors.current.red,
              )
              Text(
                text = stringResource(Res.string.nearbyStationsSortedDescription),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalBiziColors.current.muted,
              )
            }
            RefreshButtonWithCountdown(
              countdown = refreshCountdownSeconds,
              loading = loading,
              onRefresh = onRefresh,
            )
          }
        }
        Row(
          modifier = Modifier.animateContentSize(animationSpec = spring(dampingRatio = 0.9f, stiffness = 500f)),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          QuickRouteActionCard(
            modifier = Modifier.weight(1f),
            title = stringResource(Res.string.nearbyNearestWithBikes),
            emptyTitle = stringResource(Res.string.nearbyNoBikesNearby),
            selection = nearestWithBikesSelection,
            icon = Icons.AutoMirrored.Filled.DirectionsBike,
            tint = LocalBiziColors.current.red,
            mobilePlatform = mobilePlatform,
            onRoute = onQuickRoute,
          )
          QuickRouteActionCard(
            modifier = Modifier.weight(1f),
            title = stringResource(Res.string.nearbyNearestWithSlots),
            emptyTitle = stringResource(Res.string.nearbyNoSlotsNearby),
            selection = nearestWithSlotsSelection,
            icon = Icons.Filled.LocalParking,
            tint = LocalBiziColors.current.blue,
            mobilePlatform = mobilePlatform,
            onRoute = onQuickRoute,
          )
        }
      }
      DataFreshnessBanner(
        freshness = dataFreshness,
        lastUpdatedEpoch = lastUpdatedEpoch,
        loading = loading,
        onRefresh = onRefresh,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 10.dp),
      )
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = if (loading) stringResource(Res.string.nearbyUpdatingStations) else stringResource(Res.string.nearbyStations),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              text = if (nearestSelection.usesFallback) {
                stringResource(Res.string.nearbyRadiusFallbackHint)
              } else {
                stringResource(Res.string.nearbyCardActionsHint)
              },
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            AnimatedVisibility(
              visible = errorMessage != null,
              enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
              exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
              label = "nearby-error",
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(errorMessage.orEmpty(), color = LocalBiziColors.current.red)
                OutlinedButton(onClick = onRetry) {
                  Icon(Icons.Filled.Sync, contentDescription = null)
                  Spacer(Modifier.width(8.dp))
                  Text(stringResource(Res.string.retry))
                }
              }
            }
          }
        }
        item {
          AnimatedVisibility(
            visible = !loading && stations.isEmpty(),
            enter = fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
            label = "nearby-empty",
          ) {
            EmptyStateCard(
              title = stringResource(Res.string.mapNoStationsOnScreen),
              description = stringResource(Res.string.mapLocationFallbackDescription),
              primaryAction = stringResource(Res.string.loadStations),
              onPrimaryAction = onRetry,
            )
          }
        }
        items(stations.take(12), key = { it.id }) { station ->
          StationRow(
            mobilePlatform = mobilePlatform,
            station = station,
            isFavorite = station.id in favoriteIds,
            onClick = { onStationSelected(station) },
            onFavoriteToggle = { onFavoriteToggle(station) },
            onQuickRoute = { onQuickRoute(station) },
          )
        }
      }
    }

  }
}

private fun buildEnvironmentalZoneSnapshots(stations: List<Station>): List<ZoneEnvironmentalSnapshot> {
  if (stations.isEmpty()) return emptyList()
  val averageLatitude = stations.map { it.location.latitude }.average()
  val averageLongitude = stations.map { it.location.longitude }.average()
  val grouped = stations.groupBy { station ->
    val north = station.location.latitude >= averageLatitude
    val east = station.location.longitude >= averageLongitude
    when {
      north && east -> "Noreste"
      north && !east -> "Noroeste"
      !north && east -> "Sureste"
      else -> "Suroeste"
    }
  }
  return grouped.entries.sortedBy { it.key }.map { (zone, zoneStations) ->
    val centerLatitude = zoneStations.map { it.location.latitude }.average()
    val centerLongitude = zoneStations.map { it.location.longitude }.average()
    ZoneEnvironmentalSnapshot(
      centerLatitude = centerLatitude,
      centerLongitude = centerLongitude,
      zoneLabel = zone,
    )
  }
}

@Composable
private fun EnvironmentalLayerSummaryCard(
  layer: EnvironmentalLayer,
  onOpenDetails: () -> Unit,
  onClear: () -> Unit,
) {
  val c = LocalBiziColors.current
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = c.surface),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = when (layer) {
            EnvironmentalLayer.AirQuality -> stringResource(Res.string.mapFilterAirQuality)
            EnvironmentalLayer.Pollen -> stringResource(Res.string.mapFilterPollen)
          },
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = stringResource(Res.string.mapEnvironmentalLayerHint),
          style = MaterialTheme.typography.bodySmall,
          color = c.muted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onOpenDetails, contentPadding = PaddingValues(0.dp)) {
          Text(stringResource(Res.string.details))
        }
        TextButton(onClick = onClear, contentPadding = PaddingValues(0.dp)) {
          Text(stringResource(Res.string.mapClearEnvironmentalLayer))
        }
      }
    }
  }
}

@Composable
private fun EnvironmentalLayerCard(
  layer: EnvironmentalLayer,
  zones: List<ZoneEnvironmentalSnapshot>,
  onClear: () -> Unit,
) {
  val c = LocalBiziColors.current
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = c.surface),
  ) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = when (layer) {
          EnvironmentalLayer.AirQuality -> stringResource(Res.string.mapFilterAirQuality)
          EnvironmentalLayer.Pollen -> stringResource(Res.string.mapFilterPollen)
        },
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = stringResource(Res.string.mapEnvironmentalLayerHint),
        style = MaterialTheme.typography.bodySmall,
        color = c.muted,
      )
      EnvironmentalLegendRow(layer = layer)
      zones.take(4).forEach { zone ->
        val score = when (layer) {
          EnvironmentalLayer.AirQuality -> zone.airQualityScore
          EnvironmentalLayer.Pollen -> zone.pollenScore
        }
        val tone = environmentalToneForLayer(layer = layer, score = score, muted = c.muted)
        val valueText = when {
          score == null -> "--"
          layer == EnvironmentalLayer.AirQuality -> "AQI $score"
          else -> "$score gr/m3"
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(zone.zoneLabel, style = MaterialTheme.typography.bodySmall, color = c.ink)
          Text(
            valueText,
            style = MaterialTheme.typography.bodySmall,
            color = tone,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
      TextButton(onClick = onClear, contentPadding = PaddingValues(0.dp)) {
        Text(stringResource(Res.string.mapClearEnvironmentalLayer))
      }
    }
  }
}

@Composable
private fun EnvironmentalLegendRow(layer: EnvironmentalLayer) {
  val c = LocalBiziColors.current
  val labels = when (layer) {
    EnvironmentalLayer.AirQuality -> listOf(
      stringResource(Res.string.environmentalLegendGood),
      stringResource(Res.string.environmentalLegendModerate),
      stringResource(Res.string.environmentalLegendPoor),
    )
    EnvironmentalLayer.Pollen -> listOf(
      stringResource(Res.string.environmentalLegendLow),
      stringResource(Res.string.environmentalLegendMedium),
      stringResource(Res.string.environmentalLegendHigh),
    )
  }
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    listOf(c.green, c.orange, c.red).forEachIndexed { index, color ->
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        MapColorDot(color = color)
        Text(labels[index], style = MaterialTheme.typography.labelSmall, color = c.muted)
      }
    }
  }
}

@Composable
private fun EnvironmentalMapOverlay(
  modifier: Modifier,
  layer: EnvironmentalLayer,
  zones: List<ZoneEnvironmentalSnapshot>,
  stations: List<Station>,
) {
  if (zones.isEmpty() || stations.isEmpty()) return
  val minLat = stations.minOfOrNull { it.location.latitude } ?: return
  val maxLat = stations.maxOfOrNull { it.location.latitude } ?: return
  val minLon = stations.minOfOrNull { it.location.longitude } ?: return
  val maxLon = stations.maxOfOrNull { it.location.longitude } ?: return
  val latRange = (maxLat - minLat).takeIf { it > 0.00001 } ?: return
  val lonRange = (maxLon - minLon).takeIf { it > 0.00001 } ?: return
  val c = LocalBiziColors.current

  Canvas(modifier = modifier) {
    zones.forEach { zone ->
      val value = when (layer) {
        EnvironmentalLayer.AirQuality -> zone.airQualityScore
        EnvironmentalLayer.Pollen -> zone.pollenScore
      } ?: return@forEach

      val tone = environmentalToneForLayer(layer = layer, score = value, muted = c.muted)
      val intensity = when (layer) {
        EnvironmentalLayer.AirQuality -> (value.coerceIn(0, 200) / 200f)
        EnvironmentalLayer.Pollen -> (value.coerceIn(0, 80) / 80f)
      }
      val x = (((zone.centerLongitude - minLon) / lonRange).toFloat() * size.width).coerceIn(0f, size.width)
      val y = (size.height - (((zone.centerLatitude - minLat) / latRange).toFloat() * size.height)).coerceIn(0f, size.height)
      drawCircle(
        color = tone.copy(alpha = 0.14f + (0.20f * intensity)),
        radius = size.minDimension * (0.10f + (0.14f * intensity)),
        center = androidx.compose.ui.geometry.Offset(x, y),
      )
    }
  }
}

private fun environmentalToneForLayer(layer: EnvironmentalLayer, score: Int?, muted: Color): Color = when {
  score == null -> muted
  layer == EnvironmentalLayer.AirQuality && score <= 50 -> Color(0xFF26A69A)
  layer == EnvironmentalLayer.AirQuality && score <= 100 -> Color(0xFFFFB300)
  layer == EnvironmentalLayer.AirQuality -> Color(0xFFD84315)
  layer == EnvironmentalLayer.Pollen && score <= 10 -> Color(0xFF8BC34A)
  layer == EnvironmentalLayer.Pollen && score <= 30 -> Color(0xFFFF9800)
  else -> Color(0xFFC2185B)
}

@Composable
private fun RefreshButtonWithCountdown(
  countdown: Int,
  loading: Boolean,
  onRefresh: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    IconButton(onClick = onRefresh, enabled = !loading) {
      Icon(Icons.Filled.Sync, contentDescription = stringResource(Res.string.refreshStations))
    }
    if (countdown > 0 && !loading) {
      val minutes = countdown / 60
      val seconds = countdown % 60
      Text(
        text = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s",
        style = MaterialTheme.typography.labelSmall,
        color = LocalBiziColors.current.muted,
      )
    }
  }
}

@Composable
private fun MapFilterChipRow(
  activeFilters: Set<MapFilter>,
  onToggleFilter: (MapFilter) -> Unit,
) {
  Row(
    modifier = Modifier.horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    MapFilter.entries.forEach { filter ->
      MapFilterChip(
        filter = filter,
        label = stringResource(filter.labelKey),
        selected = filter in activeFilters,
        onClick = { onToggleFilter(filter) },
      )
    }
  }
}

@Composable
private fun MapLegendChip(
  label: String,
  color: Color,
) {
  val c = LocalBiziColors.current
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = c.surface.copy(alpha = 0.92f),
    border = BorderStroke(1.dp, c.panel),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      MapColorDot(color = color)
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = c.ink,
      )
    }
  }
}

@Composable
private fun MapColorDot(
  color: Color,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .size(10.dp)
      .clip(CircleShape)
      .background(color),
  )
}

@Composable
private fun MapFilterChip(
  filter: MapFilter,
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val c = LocalBiziColors.current
  val accent = when (filter) {
    MapFilter.BIKES_AND_SLOTS -> c.green
    MapFilter.ONLY_BIKES -> c.blue
    MapFilter.ONLY_SLOTS -> c.red
    MapFilter.ONLY_EBIKES -> c.orange
    MapFilter.ONLY_REGULAR_BIKES -> c.purple
    MapFilter.AIR_QUALITY -> c.green
    MapFilter.POLLEN -> c.orange
  }
  val backgroundColor by animateColorAsState(
    targetValue = if (selected) c.surface else c.surface,
    animationSpec = tween(180),
  )
  val contentColor by animateColorAsState(
    targetValue = if (selected) accent else c.ink,
    animationSpec = tween(180),
  )
  val borderColor by animateColorAsState(
    targetValue = if (selected) accent else c.panel,
    animationSpec = tween(180),
  )
  val selectionScale by animateFloatAsState(
    targetValue = if (selected) 1f else 0.98f,
    animationSpec = spring(dampingRatio = 0.82f, stiffness = 700f),
    label = "map-filter-scale",
  )
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = backgroundColor,
    border = BorderStroke(1.dp, borderColor),
    modifier = Modifier
      .graphicsLayer {
        scaleX = selectionScale
        scaleY = selectionScale
      }
      .clickable(onClick = onClick),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      MapColorDot(color = accent)
      Text(
        text = label,
        color = contentColor,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

@Composable
private fun MapRecenterButton(
  enabled: Boolean,
  onClick: () -> Unit,
) {
  val c = LocalBiziColors.current
  Surface(
    modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
    shape = CircleShape,
    color = c.surface.copy(alpha = if (enabled) 0.96f else 0.88f),
    tonalElevation = 4.dp,
    shadowElevation = 6.dp,
  ) {
    Icon(
      imageVector = Icons.Filled.MyLocation,
      contentDescription = stringResource(Res.string.mapMyLocation),
      tint = if (enabled) c.green else c.muted,
      modifier = Modifier.padding(14.dp).size(22.dp),
    )
  }
}

@Composable
private fun MapEnvironmentalSheetButton(
  onClick: () -> Unit,
) {
  val c = LocalBiziColors.current
  Surface(
    modifier = Modifier.clickable(onClick = onClick),
    shape = CircleShape,
    color = c.surface.copy(alpha = 0.96f),
    tonalElevation = 4.dp,
    shadowElevation = 6.dp,
  ) {
    Icon(
      imageVector = Icons.Filled.Tune,
      contentDescription = stringResource(Res.string.details),
      tint = c.blue,
      modifier = Modifier.padding(14.dp).size(22.dp),
    )
  }
}

@Composable
private fun MapSelectedStationCard(
  modifier: Modifier = Modifier,
  mobilePlatform: MobileUiPlatform,
  station: Station,
  isFavorite: Boolean,
  isShowingNearestSelection: Boolean,
  isFallbackSelection: Boolean,
  searchRadiusMeters: Int,
  onFavoriteToggle: () -> Unit,
  onOpenStationDetails: (Station) -> Unit,
  onQuickRoute: (Station) -> Unit,
  onDismiss: () -> Unit,
) {
  val c = LocalBiziColors.current
  val overlayTitle = if (mobilePlatform == MobileUiPlatform.IOS) c.ink else c.onAccent
  val overlayBody = if (mobilePlatform == MobileUiPlatform.IOS) c.muted else c.onAccent.copy(alpha = 0.84f)
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 24.dp else 28.dp),
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, c.red.copy(alpha = 0.12f)) else null,
    colors = CardDefaults.cardColors(containerColor = if (mobilePlatform == MobileUiPlatform.IOS) c.surface else c.red),
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 14.dp, vertical = 13.dp)
        .animateContentSize(animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f)),
      verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          if (isFallbackSelection) {
            stringResource(Res.string.mapNoStationsWithinRadius, searchRadiusMeters)
          } else if (isShowingNearestSelection) {
            stringResource(Res.string.mapNearestStationLabel)
          } else {
            stringResource(Res.string.mapSelectedStationLabel)
          },
          color = if (mobilePlatform == MobileUiPlatform.IOS) c.red else overlayBody,
        )
        Icon(
          imageVector = Icons.Filled.Close,
          contentDescription = stringResource(Res.string.close),
          tint = if (mobilePlatform == MobileUiPlatform.IOS) c.muted else overlayBody,
          modifier = Modifier.size(20.dp).clickable(onClick = onDismiss),
        )
      }
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = station.name,
          style = MaterialTheme.typography.titleLarge,
          color = overlayTitle,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = station.address,
          style = MaterialTheme.typography.bodySmall,
          color = overlayBody,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Text(
        text = if (isFallbackSelection) {
          stringResource(
            Res.string.mapNearestFallbackSummary,
            formatDistance(station.distanceMeters),
            station.bikesAvailable,
            station.slotsFree,
          )
        } else {
          stringResource(
            Res.string.mapStationDistanceSummary,
            formatDistance(station.distanceMeters),
            station.bikesAvailable,
            station.slotsFree,
          )
        },
        color = overlayBody,
      )
      Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        RoutePill(
          label = stringResource(Res.string.route),
          onDarkBackground = mobilePlatform != MobileUiPlatform.IOS,
          onClick = { onQuickRoute(station) },
        )
        if (mobilePlatform == MobileUiPlatform.IOS) {
          FavoritePill(
            active = isFavorite,
            onClick = onFavoriteToggle,
            label = if (isFavorite) stringResource(Res.string.saved) else stringResource(Res.string.save),
          )
        } else {
          OutlineActionPill(
            label = if (isFavorite) stringResource(Res.string.saved) else stringResource(Res.string.save),
            tint = c.onAccent,
            borderTint = c.onAccent.copy(alpha = 0.32f),
            onClick = onFavoriteToggle,
          )
        }
        OutlineActionPill(
          label = stringResource(Res.string.details),
          tint = if (mobilePlatform == MobileUiPlatform.IOS) c.red else c.onAccent,
          borderTint = if (mobilePlatform == MobileUiPlatform.IOS) c.red.copy(alpha = 0.16f) else c.onAccent.copy(alpha = 0.32f),
          onClick = { onOpenStationDetails(station) },
        )
      }
    }
  }
}

@Composable
private fun FavoritesScreen(
  mobilePlatform: MobileUiPlatform,
  onOpenAssistant: () -> Unit,
  allStations: List<Station>,
  stations: List<Station>,
  homeStation: Station?,
  workStation: Station?,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onStationSelected: (Station) -> Unit,
  onAssignHomeStation: (Station) -> Unit,
  onAssignWorkStation: (Station) -> Unit,
  onClearHomeStation: () -> Unit,
  onClearWorkStation: () -> Unit,
  onRemoveFavorite: (Station) -> Unit,
  onQuickRoute: (Station) -> Unit,
  dataFreshness: DataFreshness,
  lastUpdatedEpoch: Long?,
  stationsLoading: Boolean,
  onRefreshStations: () -> Unit,
  onOpenSavedPlaceAlerts: () -> Unit,
  paddingValues: PaddingValues,
  savedPlaceAlertsCityId: String,
  savedPlaceAlertRules: List<SavedPlaceAlertRule>,
  onUpsertSavedPlaceAlert: ((SavedPlaceAlertTarget, SavedPlaceAlertCondition) -> Unit)?,
  onRemoveSavedPlaceAlertForTarget: ((SavedPlaceAlertTarget) -> Unit)?,
) {
  fun ruleFor(target: SavedPlaceAlertTarget): SavedPlaceAlertRule? =
    savedPlaceAlertRules.firstOrNull { it.target.identityKey() == target.identityKey() }
  var alertEditor by remember { mutableStateOf<Pair<SavedPlaceAlertTarget, SavedPlaceAlertRule?>?>(null) }
  val upsertAlert = onUpsertSavedPlaceAlert
  val removeAlertForTarget = onRemoveSavedPlaceAlertForTarget
  if (upsertAlert != null && removeAlertForTarget != null) {
    alertEditor?.let { (target, rule) ->
      SavedPlaceAlertEditorSheet(
        target = target,
        existingRule = rule,
        onDismiss = { alertEditor = null },
        onSave = { cond ->
          upsertAlert(target, cond)
          alertEditor = null
        },
        onRemove = {
          removeAlertForTarget(target)
          alertEditor = null
        },
      )
    }
  }
  val assignmentCandidate = remember(allStations, searchQuery) {
    searchQuery
      .takeIf { it.isNotBlank() }
      ?.let { query -> findStationMatchingQuery(allStations, query) }
  }
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(pageBackgroundColor(mobilePlatform)),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = if (mobilePlatform == MobileUiPlatform.Desktop) Modifier.fillMaxWidth() else Modifier.responsivePageWidth(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = if (mobilePlatform == MobileUiPlatform.IOS) stringResource(Res.string.favorites) else stringResource(Res.string.myStations),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = stringResource(Res.string.favoritesSubtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalBiziColors.current.muted,
          )
        }
      }
      item {
        StationSearchField(
          mobilePlatform = mobilePlatform,
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          label = stringResource(Res.string.favoritesSearchStation),
        )
      }
      item {
        DataFreshnessBanner(
          freshness = dataFreshness,
          lastUpdatedEpoch = lastUpdatedEpoch,
          loading = stationsLoading,
          onRefresh = onRefreshStations,
        )
      }
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(Res.string.savedPlaceAlertsTitle), fontWeight = FontWeight.SemiBold)
            Text(
              stringResource(Res.string.savedPlaceAlertsProfileSubtitle),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            Text(
              stringResource(Res.string.savedPlaceAlertsStationDetailHint),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            OutlinedButton(
              modifier = Modifier.fillMaxWidth(),
              onClick = onOpenSavedPlaceAlerts,
            ) {
              Icon(Icons.Filled.Notifications, contentDescription = null)
              Spacer(Modifier.width(8.dp))
              Text(stringResource(Res.string.savedPlaceAlertsProfileAction))
            }
          }
        }
      }
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = stringResource(Res.string.homeAndWork),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = stringResource(Res.string.homeAndWorkDescription),
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
        }
      }
      item {
        SavedPlaceCard(
          mobilePlatform = mobilePlatform,
          title = stringResource(Res.string.home),
          station = homeStation,
          assignmentCandidate = assignmentCandidate,
          onAssignCandidate = onAssignHomeStation,
          onClear = onClearHomeStation,
          onOpenStationDetails = onStationSelected,
          onQuickRoute = onQuickRoute,
          onSavedPlaceAlertClick = run {
            val s = homeStation
            if (s != null && upsertAlert != null) {
              {
                val t = SavedPlaceAlertTarget.Home(s.id, savedPlaceAlertsCityId, s.name)
                alertEditor = t to ruleFor(t)
              }
            } else {
              null
            }
          },
          savedPlaceAlertActive = homeStation?.let { s ->
            ruleFor(SavedPlaceAlertTarget.Home(s.id, savedPlaceAlertsCityId, s.name)) != null
          } == true,
        )
      }
      item {
        SavedPlaceCard(
          mobilePlatform = mobilePlatform,
          title = stringResource(Res.string.work),
          station = workStation,
          assignmentCandidate = assignmentCandidate,
          onAssignCandidate = onAssignWorkStation,
          onClear = onClearWorkStation,
          onOpenStationDetails = onStationSelected,
          onQuickRoute = onQuickRoute,
          onSavedPlaceAlertClick = run {
            val s = workStation
            if (s != null && upsertAlert != null) {
              {
                val t = SavedPlaceAlertTarget.Work(s.id, savedPlaceAlertsCityId, s.name)
                alertEditor = t to ruleFor(t)
              }
            } else {
              null
            }
          },
          savedPlaceAlertActive = workStation?.let { s ->
            ruleFor(SavedPlaceAlertTarget.Work(s.id, savedPlaceAlertsCityId, s.name)) != null
          } == true,
        )
      }
      item {
        AnimatedVisibility(
          visible = stations.isEmpty() && homeStation == null && workStation == null,
          enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
          exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
          label = "favorites-empty",
        ) {
          EmptyStateCard(
            title = stringResource(Res.string.favoritesEmptyTitle),
            description = stringResource(Res.string.favoritesEmptyDescription),
          )
        }
      }
      if (stations.isNotEmpty()) {
        items(stations.distinctBy { it.id }, key = { it.id }) { station ->
          DismissibleFavoriteStationRow(
            mobilePlatform = mobilePlatform,
            station = station,
            canAssignHome = homeStation == null,
            canAssignWork = workStation == null,
            onClick = { onStationSelected(station) },
            onAssignHome = { onAssignHomeStation(station) },
            onAssignWork = { onAssignWorkStation(station) },
            onQuickRoute = { onQuickRoute(station) },
            onRemoveFavorite = { onRemoveFavorite(station) },
            onSavedPlaceAlertClick = if (upsertAlert != null) {
              {
                val t = SavedPlaceAlertTarget.FavoriteStation(station.id, savedPlaceAlertsCityId, station.name)
                alertEditor = t to ruleFor(t)
              }
            } else {
              null
            },
            savedPlaceAlertActive = ruleFor(
              SavedPlaceAlertTarget.FavoriteStation(station.id, savedPlaceAlertsCityId, station.name),
            ) != null,
          )
        }
      }
    }
  }
}

@Composable
private fun ProfileScreen(
  mobilePlatform: MobileUiPlatform,
  paddingValues: PaddingValues,
  searchRadiusMeters: Int,
  preferredMapApp: PreferredMapApp,
  themePreference: ThemePreference,
  selectedCity: City,
  onOpenShortcuts: () -> Unit,
  onSearchRadiusSelected: (Int) -> Unit,
  onPreferredMapAppSelected: (PreferredMapApp) -> Unit,
  onThemePreferenceSelected: (ThemePreference) -> Unit,
  onCitySelected: (City) -> Unit,
  showProfileSetupCard: Boolean,
  onShowChangelog: () -> Unit,
  onOpenOnboarding: () -> Unit,
  onOpenFeedback: () -> Unit,
  onRateApp: () -> Unit,
) {
  var showFeedbackDialog by remember { mutableStateOf(false) }
  var showDataSourcesDialog by remember { mutableStateOf(false) }
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(pageBackgroundColor(mobilePlatform)),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.responsivePageWidth(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = stringResource(Res.string.settings),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = stringResource(Res.string.profileSubtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalBiziColors.current.muted,
          )
        }
      }
      if (showProfileSetupCard) {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
          ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(stringResource(Res.string.profileSetupCardTitle), fontWeight = FontWeight.SemiBold)
              Text(
                stringResource(Res.string.profileSetupCardBody),
                style = MaterialTheme.typography.bodySmall,
                color = LocalBiziColors.current.muted,
              )
              TextButton(
                onClick = onOpenOnboarding,
                contentPadding = PaddingValues(0.dp),
              ) {
                Text(stringResource(Res.string.profileSetupCardAction), style = MaterialTheme.typography.bodySmall)
              }
            }
          }
        }
      }
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(Res.string.viewWhatsNew), fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onShowChangelog, contentPadding = PaddingValues(0.dp)) {
              Text(stringResource(Res.string.viewWhatsNew), style = MaterialTheme.typography.bodySmall)
            }
          }
        }
      }
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(Res.string.nearbyStationRadius), fontWeight = FontWeight.SemiBold)
            Text(
              stringResource(Res.string.nearbyStationRadiusDescription),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            SearchRadiusSelector(
              selectedRadiusMeters = searchRadiusMeters,
              onSearchRadiusSelected = onSearchRadiusSelected,
            )
          }
        }
      }
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(Res.string.selectedCity), fontWeight = FontWeight.SemiBold)
            Text(
              stringResource(Res.string.citySelectionSubtitle),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            CitySelector(
              selectedCity = selectedCity,
              onCitySelected = onCitySelected,
            )
          }
        }
      }
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(Res.string.appearance), fontWeight = FontWeight.SemiBold)
            Text(
              stringResource(Res.string.appearanceDescription),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              RadiusSelectionButton(
                modifier = Modifier.weight(1f),
                selected = themePreference == ThemePreference.System,
                label = stringResource(Res.string.system),
                onClick = { onThemePreferenceSelected(ThemePreference.System) },
              )
              RadiusSelectionButton(
                modifier = Modifier.weight(1f),
                selected = themePreference == ThemePreference.Light,
                label = stringResource(Res.string.light),
                onClick = { onThemePreferenceSelected(ThemePreference.Light) },
              )
              RadiusSelectionButton(
                modifier = Modifier.weight(1f),
                selected = themePreference == ThemePreference.Dark,
                label = stringResource(Res.string.dark),
                onClick = { onThemePreferenceSelected(ThemePreference.Dark) },
              )
            }
          }
        }
      }
      if (mobilePlatform == MobileUiPlatform.IOS) {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
          ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
              Text(stringResource(Res.string.iPhoneRouteApp), fontWeight = FontWeight.SemiBold)
              Text(
                stringResource(Res.string.iPhoneRouteAppDescription),
                style = MaterialTheme.typography.bodySmall,
                color = LocalBiziColors.current.muted,
              )
              Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RadiusSelectionButton(
                  modifier = Modifier.weight(1f),
                  selected = preferredMapApp == PreferredMapApp.AppleMaps,
                  label = "Apple Maps",
                  onClick = { onPreferredMapAppSelected(PreferredMapApp.AppleMaps) },
                )
                RadiusSelectionButton(
                  modifier = Modifier.weight(1f),
                  selected = preferredMapApp == PreferredMapApp.GoogleMaps,
                  label = "Google Maps",
                  onClick = { onPreferredMapAppSelected(PreferredMapApp.GoogleMaps) },
                )
              }
              Text(
                 stringResource(Res.string.iPhoneRouteAppFallback),
                style = MaterialTheme.typography.bodySmall,
                color = LocalBiziColors.current.muted,
              )
            }
          }
        }
      }
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(Res.string.rateApp), fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onRateApp, contentPadding = PaddingValues(0.dp)) {
              Text(stringResource(Res.string.rateApp), style = MaterialTheme.typography.bodySmall)
            }
          }
        }
      }
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(Res.string.feedbackAndSuggestions), fontWeight = FontWeight.SemiBold)
            Text(
              stringResource(Res.string.feedbackDescription),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            TextButton(
              onClick = { showFeedbackDialog = true },
              contentPadding = PaddingValues(0.dp),
            ) {
              Text(stringResource(Res.string.openFeedbackForm), style = MaterialTheme.typography.bodySmall)
            }
          }
        }
      }
      if (showFeedbackDialog) {
        item {
          FeedbackDialog(
            onDismiss = { showFeedbackDialog = false },
            onOpenFeedbackForm = {
              onOpenFeedback()
              showFeedbackDialog = false
            },
          )
        }
      }
      item {
        val uriHandler = LocalUriHandler.current
        Card(
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(Res.string.privacyAndData), fontWeight = FontWeight.SemiBold)
            Text(
              stringResource(Res.string.privacyDescription),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            TextButton(
              onClick = { uriHandler.openUri("https://gcaguilar.github.io/biciradar-privacy-policy/") },
              contentPadding = PaddingValues(0.dp),
            ) {
              Text(stringResource(Res.string.openPrivacyPolicy), style = MaterialTheme.typography.bodySmall)
            }
          }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showDataSourcesDialog = true }
              .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Text(stringResource(Res.string.dataSourceTitle), fontWeight = FontWeight.SemiBold)
            Text(
              stringResource(Res.string.dataSourceDescription),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            Text(
              text = stringResource(Res.string.dataSourceDetailsAction),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.blue,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
      }
    }
  }

  if (showDataSourcesDialog) {
    AlertDialog(
      onDismissRequest = { showDataSourcesDialog = false },
      title = { Text(stringResource(Res.string.dataSourceTitle)) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = stringResource(Res.string.dataSourceGbfsDetail),
            style = MaterialTheme.typography.bodySmall,
          )
          Text(
            text = stringResource(Res.string.dataSourceEnvironmentalDetail),
            style = MaterialTheme.typography.bodySmall,
          )
        }
      },
      confirmButton = {
        TextButton(onClick = { showDataSourcesDialog = false }) {
          Text(stringResource(Res.string.close))
        }
      },
    )
  }
}

@Composable
private fun SearchRadiusSelector(
  selectedRadiusMeters: Int,
  onSearchRadiusSelected: (Int) -> Unit,
) {
  val colors = LocalBiziColors.current
  var expanded by remember { mutableStateOf(false) }
  Box {
    OutlinedButton(
      onClick = { expanded = true },
      modifier = Modifier.fillMaxWidth(),
      border = BorderStroke(1.dp, colors.panel),
      colors = ButtonDefaults.outlinedButtonColors(containerColor = colors.surface),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = formatDistance(selectedRadiusMeters),
          color = colors.ink,
        )
        Icon(
          imageVector = Icons.Filled.Navigation,
          contentDescription = null,
          tint = colors.red,
        )
      }
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.background(colors.surface),
    ) {
      SEARCH_RADIUS_OPTIONS_METERS.forEach { radius ->
        DropdownMenuItem(
          text = {
            Text(
              text = formatDistance(radius),
              color = if (radius == selectedRadiusMeters) colors.red else colors.ink,
              fontWeight = if (radius == selectedRadiusMeters) FontWeight.SemiBold else FontWeight.Normal,
            )
          },
          onClick = {
            expanded = false
            onSearchRadiusSelected(radius)
          },
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShortcutsScreen(
  mobilePlatform: MobileUiPlatform,
  paddingValues: PaddingValues,
  graph: SharedGraph,
  stationsRepository: com.gcaguilar.biciradar.core.StationsRepository,
  favoriteIds: Set<String>,
  searchRadiusMeters: Int,
  initialAction: AssistantAction?,
  onInitialActionConsumed: () -> Unit,
  onBack: () -> Unit,
) {
  PlatformBackHandler(enabled = true, onBack = onBack)
  var latestAnswer by remember { mutableStateOf<String?>(null) }
  val shortcutGuides = shortcutGuidesFor(mobilePlatform)

  LaunchedEffect(initialAction, favoriteIds, searchRadiusMeters) {
    val action = initialAction ?: return@LaunchedEffect
    val resolution = graph.assistantIntentResolver.resolve(
      action = action,
      stationsState = stationsRepository.state.value,
      favoriteIds = favoriteIds,
      searchRadiusMeters = searchRadiusMeters,
    )
    latestAnswer = resolution.spokenResponse
    onInitialActionConsumed()
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(pageBackgroundColor(mobilePlatform)),
    topBar = {
      TopAppBar(
        title = {
          if (mobilePlatform == MobileUiPlatform.IOS) {
            Text("")
          } else {
            Text(stringResource(Res.string.shortcuts))
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
          }
        },
      )
    },
    containerColor = pageBackgroundColor(mobilePlatform),
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(pageBackgroundColor(mobilePlatform)),
      contentAlignment = Alignment.TopCenter,
    ) {
      LazyColumn(
        modifier = Modifier.responsivePageWidth(),
        contentPadding = PaddingValues(
          start = 16.dp,
          top = innerPadding.calculateTopPadding() + 16.dp,
          end = 16.dp,
          bottom = innerPadding.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        if (mobilePlatform == MobileUiPlatform.IOS) {
          item {
            Column(
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = stringResource(Res.string.shortcuts),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = stringResource(Res.string.shortcutsIosSubtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalBiziColors.current.muted,
              )
            }
          }
        }
        item {
          Card(colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface)) {
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text(stringResource(Res.string.howToInvoke), fontWeight = FontWeight.SemiBold)
              Text(
                if (mobilePlatform == MobileUiPlatform.IOS) {
                  stringResource(Res.string.shortcutsAvailableOnIos)
                } else {
                  stringResource(Res.string.shortcutsAvailableWithAssistant, mobilePlatform.assistantDisplayName())
                },
                style = MaterialTheme.typography.bodySmall,
                color = LocalBiziColors.current.muted,
              )
              Text(
                if (mobilePlatform == MobileUiPlatform.IOS) {
                  stringResource(Res.string.shortcutsIosInvocationHint)
                } else {
                  stringResource(Res.string.shortcutsAndroidInvocationHint)
                },
                style = MaterialTheme.typography.bodySmall,
                color = LocalBiziColors.current.muted,
              )
              Text(
                stringResource(Res.string.shortcutsCurrentRadius, searchRadiusMeters),
                style = MaterialTheme.typography.bodySmall,
                color = LocalBiziColors.current.ink,
              )
            }
          }
        }
        latestAnswer?.let { answer ->
          item {
            Card(colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface)) {
              Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Text(stringResource(Res.string.latestAnswer), fontWeight = FontWeight.SemiBold)
                Text(answer)
              }
            }
          }
        }
        items(shortcutGuides, key = { it.title }) { guide ->
          ShortcutGuideCard(guide = guide)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationDetailScreen(
  mobilePlatform: MobileUiPlatform,
  station: Station,
  isFavorite: Boolean,
  isHomeStation: Boolean,
  isWorkStation: Boolean,
  userLocation: GeoPoint?,
  isMapReady: Boolean,
  supportsUsagePatterns: Boolean,
  dataFreshness: DataFreshness,
  lastUpdatedEpoch: Long?,
  stationsLoading: Boolean,
  onRefreshStations: () -> Unit,
  onBack: () -> Unit,
  onToggleFavorite: () -> Unit,
  onToggleHome: () -> Unit,
  onToggleWork: () -> Unit,
  onRoute: () -> Unit,
  savedPlaceAlertsCityId: String,
  savedPlaceAlertRules: List<SavedPlaceAlertRule>,
  onUpsertSavedPlaceAlert: (SavedPlaceAlertTarget, SavedPlaceAlertCondition) -> Unit,
  onRemoveSavedPlaceAlertForTarget: (SavedPlaceAlertTarget) -> Unit,
  patterns: List<StationHourlyPattern>,
  patternsLoading: Boolean,
  patternsError: Boolean,
) {
  PlatformBackHandler(enabled = true, onBack = onBack)
  fun ruleFor(target: SavedPlaceAlertTarget): SavedPlaceAlertRule? =
    savedPlaceAlertRules.firstOrNull { it.target.identityKey() == target.identityKey() }
  var alertEditor by remember { mutableStateOf<Pair<SavedPlaceAlertTarget, SavedPlaceAlertRule?>?>(null) }
  var showWeekend by rememberSaveable { mutableStateOf(false) }
  Box(Modifier.fillMaxSize()) {
  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(LocalBiziColors.current.surface)
          .windowInsetsPadding(WindowInsets.statusBars)
          .height(48.dp)
          .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
        }
        Text(
          text = station.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
      }
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(pageBackgroundColor(mobilePlatform)),
      contentAlignment = Alignment.TopCenter,
    ) {
      LazyColumn(
        modifier = Modifier.responsivePageWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = innerPadding.calculateTopPadding() + 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
          ) {
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  station.name,
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.weight(1f),
                )
                FavoritePill(
                  active = isFavorite,
                  onClick = onToggleFavorite,
                  label = if (isFavorite) stringResource(Res.string.saved) else stringResource(Res.string.save),
                )
              }
              Text(station.address, style = MaterialTheme.typography.bodyMedium, color = LocalBiziColors.current.muted)
              DataFreshnessBanner(
                freshness = dataFreshness,
                lastUpdatedEpoch = lastUpdatedEpoch,
                loading = stationsLoading,
                onRefresh = onRefreshStations,
                modifier = Modifier.padding(top = 8.dp),
              )
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StationMetricPill(
                  modifier = Modifier.weight(1f),
                  label = stringResource(Res.string.distance),
                  value = formatDistance(station.distanceMeters),
                  tint = LocalBiziColors.current.blue,
                )
                StationMetricPill(
                  modifier = Modifier.weight(1f),
                  label = stringResource(Res.string.source),
                  value = station.sourceLabel,
                  tint = LocalBiziColors.current.muted,
                )
              }
            }
          }
        }
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
          ) {
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Text(stringResource(Res.string.saveThisStation), fontWeight = FontWeight.SemiBold)
              Text(
                stringResource(Res.string.saveThisStationDescription),
                style = MaterialTheme.typography.bodySmall,
                color = LocalBiziColors.current.muted,
              )
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FavoritePill(
                  active = isFavorite,
                  onClick = onToggleFavorite,
                  label = if (isFavorite) stringResource(Res.string.favorite) else stringResource(Res.string.save),
                )
                SavedPlacePill(
                  active = isHomeStation,
                  label = stringResource(Res.string.home),
                  onClick = onToggleHome,
                )
                SavedPlacePill(
                  active = isWorkStation,
                  label = stringResource(Res.string.work),
                  onClick = onToggleWork,
                )
              }
              Text(
                when {
                  isHomeStation && isWorkStation -> stringResource(Res.string.stationMarkedHomeAndWork)
                  isHomeStation -> stringResource(Res.string.stationMarkedHome)
                  isWorkStation -> stringResource(Res.string.stationMarkedWork)
                  else -> stringResource(Res.string.tapHomeOrWorkToAssign)
                },
                style = MaterialTheme.typography.bodySmall,
                color = LocalBiziColors.current.muted,
              )
            }
          }
        }
        if (isFavorite || isHomeStation || isWorkStation) {
          item {
            val homeTarget = SavedPlaceAlertTarget.Home(station.id, savedPlaceAlertsCityId, station.name)
            val workTarget = SavedPlaceAlertTarget.Work(station.id, savedPlaceAlertsCityId, station.name)
            val favoriteTarget = SavedPlaceAlertTarget.FavoriteStation(station.id, savedPlaceAlertsCityId, station.name)
            Card(
              colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
            ) {
              Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Text(stringResource(Res.string.savedPlaceAlertsTitle), fontWeight = FontWeight.SemiBold)
                Text(
                  stringResource(Res.string.savedPlaceAlertsStationDetailHint),
                  style = MaterialTheme.typography.bodySmall,
                  color = LocalBiziColors.current.muted,
                )
                Row(
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                  verticalAlignment = Alignment.Top,
                ) {
                  if (isHomeStation) {
                    StationDetailAlertBellColumn(
                      label = stringResource(Res.string.home),
                      active = ruleFor(homeTarget) != null,
                      onClick = { alertEditor = homeTarget to ruleFor(homeTarget) },
                    )
                  }
                  if (isWorkStation) {
                    StationDetailAlertBellColumn(
                      label = stringResource(Res.string.work),
                      active = ruleFor(workTarget) != null,
                      onClick = { alertEditor = workTarget to ruleFor(workTarget) },
                    )
                  }
                  if (isFavorite) {
                    StationDetailAlertBellColumn(
                      label = stringResource(Res.string.favorite),
                      active = ruleFor(favoriteTarget) != null,
                      onClick = { alertEditor = favoriteTarget to ruleFor(favoriteTarget) },
                    )
                  }
                }
              }
            }
          }
        }
        item {
          Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
          ) {
            PlatformStationMap(
              modifier = Modifier.fillMaxWidth().height(200.dp),
              stations = listOf(station),
              userLocation = userLocation,
              highlightedStationId = station.id,
              isMapReady = isMapReady,
              onStationSelected = {},
            )
          }
        }
        item {
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AvailabilityCard(
              modifier = Modifier.weight(1f),
              label = stringResource(Res.string.bikes),
              value = station.bikesAvailable.toString(),
              icon = Icons.AutoMirrored.Filled.DirectionsBike,
              tint = LocalBiziColors.current.red,
              mobilePlatform = mobilePlatform,
            )
            AvailabilityCard(
              modifier = Modifier.weight(1f),
              label = stringResource(Res.string.slots),
              value = station.slotsFree.toString(),
              icon = Icons.Filled.LocalParking,
              tint = LocalBiziColors.current.blue,
              mobilePlatform = mobilePlatform,
            )
          }
        }
        if (supportsUsagePatterns) {
          item {
            StationPatternCard(
              patterns = patterns,
              isLoading = patternsLoading,
              isError = patternsError,
              showWeekend = showWeekend,
              onToggleDayType = { showWeekend = !showWeekend },
            )
          }
        }
        item {
          Button(onClick = onRoute, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Directions, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(Res.string.openRoute))
          }
        }
        item {
          OutlinedButton(onClick = onToggleFavorite, modifier = Modifier.fillMaxWidth()) {
            Icon(
              if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
              contentDescription = null,
            )
            Spacer(Modifier.width(8.dp))
            Text(if (isFavorite) stringResource(Res.string.removeFromFavorites) else stringResource(Res.string.saveToFavorites))
          }
        }
      }
    }
  }
  if (isFavorite || isHomeStation || isWorkStation) {
    alertEditor?.let { (target, rule) ->
      SavedPlaceAlertEditorSheet(
        target = target,
        existingRule = rule,
        onDismiss = { alertEditor = null },
        onSave = { cond ->
          onUpsertSavedPlaceAlert(target, cond)
          alertEditor = null
        },
        onRemove = {
          onRemoveSavedPlaceAlertForTarget(target)
          alertEditor = null
        },
      )
    }
  }
  }
}

@Composable
private fun StationDetailAlertBellColumn(
  label: String,
  active: Boolean,
  onClick: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
      Icon(
        imageVector = if (active) Icons.Filled.Notifications else Icons.Outlined.Notifications,
        contentDescription = stringResource(Res.string.savedPlaceAlertsBell),
        tint = if (active) LocalBiziColors.current.blue else LocalBiziColors.current.muted,
      )
    }
    Text(
      label,
      style = MaterialTheme.typography.labelSmall,
      color = LocalBiziColors.current.muted,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun StationPatternCard(
  patterns: List<StationHourlyPattern>,
  isLoading: Boolean,
  isError: Boolean,
  showWeekend: Boolean,
  onToggleDayType: () -> Unit,
) {
  val colors = LocalBiziColors.current
  Card(
    colors = CardDefaults.cardColors(containerColor = colors.surface),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(stringResource(Res.string.usagePattern), fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          FilterChip(
            selected = !showWeekend,
            onClick = { if (showWeekend) onToggleDayType() },
            label = { Text("L-V", style = MaterialTheme.typography.labelSmall) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = colors.red,
              selectedLabelColor = colors.onAccent,
            ),
          )
          FilterChip(
            selected = showWeekend,
            onClick = { if (!showWeekend) onToggleDayType() },
            label = { Text("S-D", style = MaterialTheme.typography.labelSmall) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = colors.red,
              selectedLabelColor = colors.onAccent,
            ),
          )
        }
      }
      when {
        isLoading -> {
          Box(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.Center,
          ) {
            CircularProgressIndicator(color = colors.red, modifier = Modifier.size(24.dp))
          }
        }
        isError || patterns.isEmpty() -> {
          Box(
            modifier = Modifier.fillMaxWidth().height(80.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
                stringResource(Res.string.noUsagePatternData),
              style = MaterialTheme.typography.bodySmall,
              color = colors.muted,
            )
          }
        }
        else -> {
          val dayType = if (showWeekend) "WEEKEND" else "WEEKDAY"
          val filtered = patterns.filter { it.dayType == dayType }.sortedBy { it.hour }
          if (filtered.isEmpty()) {
            Box(
              modifier = Modifier.fillMaxWidth().height(80.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                stringResource(Res.string.noDataForDayType),
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
              )
            }
          } else {
            Text(
              stringResource(Res.string.averageBikesHour),
              style = MaterialTheme.typography.bodySmall,
              color = colors.muted,
            )
            StationPatternChart(
              patterns = filtered,
              modifier = Modifier.fillMaxWidth().height(160.dp),
            )
            val bestBikesHour = filtered.maxByOrNull { it.bikesAvg }
            val bestSlotsHour = filtered.maxByOrNull { it.anchorsAvg }
            if (bestBikesHour != null && bestSlotsHour != null) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                PatternHintPill(
                  modifier = Modifier.weight(1f),
                  label = stringResource(Res.string.mostBikes),
                  value = "${bestBikesHour.hour}:00h (~${bestBikesHour.bikesAvg.roundToInt()})",
                  tint = colors.red,
                )
                PatternHintPill(
                  modifier = Modifier.weight(1f),
                  label = stringResource(Res.string.mostSlots),
                  value = "${bestSlotsHour.hour}:00h (~${bestSlotsHour.anchorsAvg.roundToInt()})",
                  tint = colors.blue,
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StationPatternChart(
  patterns: List<StationHourlyPattern>,
  modifier: Modifier = Modifier,
) {
  val colors = LocalBiziColors.current
  val barColor = colors.red
  val labelColor = colors.muted
  val gridColor = colors.muted.copy(alpha = 0.2f)
  val textMeasurer = rememberTextMeasurer()
  val labelStyle = MaterialTheme.typography.labelSmall.copy(color = labelColor)
  val maxBikes = patterns.maxOfOrNull { it.bikesAvg }?.coerceAtLeast(1.0) ?: 1.0

  Canvas(modifier = modifier) {
    val bottomPadding = 24f
    val topPadding = 8f
    val leftPadding = 0f
    val chartHeight = size.height - bottomPadding - topPadding
    val chartWidth = size.width - leftPadding
    val barCount = patterns.size
    val totalBarSpace = chartWidth / barCount
    val barWidth = (totalBarSpace * 0.65f).coerceAtMost(20f)
    val gap = totalBarSpace - barWidth

    // Horizontal grid lines
    val gridLines = 3
    for (i in 1..gridLines) {
      val y = topPadding + chartHeight * (1f - i.toFloat() / (gridLines + 1))
      drawLine(
        color = gridColor,
        start = Offset(leftPadding, y),
        end = Offset(size.width, y),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
      )
    }

    // Bars and labels
    patterns.forEachIndexed { index, pattern ->
      val x = leftPadding + index * totalBarSpace + gap / 2
      val ratio = (pattern.bikesAvg / maxBikes).toFloat()
      val barHeight = chartHeight * ratio
      val barY = topPadding + chartHeight - barHeight

      drawRoundRect(
        color = barColor,
        topLeft = Offset(x, barY),
        size = Size(barWidth, barHeight),
        cornerRadius = CornerRadius(barWidth / 4, barWidth / 4),
      )

      // Hour label every 3 hours
      if (pattern.hour % 3 == 0) {
        val text = "${pattern.hour}h"
        val measured = textMeasurer.measure(text, labelStyle)
        drawText(
          textLayoutResult = measured,
          topLeft = Offset(
            x + barWidth / 2 - measured.size.width / 2,
            size.height - bottomPadding + 4f,
          ),
        )
      }
    }
  }
}

@Composable
private fun PatternHintPill(
  label: String,
  value: String,
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = tint.copy(alpha = 0.12f),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
      Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
      Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
  }
}

@Composable
private fun ChangelogDialog(
  entries: List<ChangelogCatalogEntry>,
  onDismiss: () -> Unit,
) {
  val colors = LocalBiziColors.current
  androidx.compose.material3.AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    title = {
      Text(stringResource(Res.string.changelogWhatsNew), fontWeight = FontWeight.Bold)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        items(entries.size) { index ->
          val entry = entries[index]
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              stringResource(entry.titleKey),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              stringResource(entry.descriptionKey),
              style = MaterialTheme.typography.bodySmall,
              color = colors.muted,
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(Res.string.gotIt))
      }
    },
  )
}

@Composable
private fun CitySelectionScreen(
  onCitySelected: (City) -> Unit,
) {
  val colors = LocalBiziColors.current
  val sortedCities = remember { City.entries.sortedBy { it.displayName } }
  Column(
    modifier = Modifier
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
    Spacer(modifier = Modifier.height(32.dp))
    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      items(sortedCities.size) { index ->
        val city = sortedCities[index]
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onCitySelected(city) },
          colors = CardDefaults.cardColors(containerColor = colors.surface),
          border = BorderStroke(1.dp, colors.panel),
        ) {
          Row(
            modifier = Modifier
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

private fun geoSuggestionSecondaryText(result: GeoResult): String? {
  val address = result.address.trim()
  if (address.isBlank()) return null
  if (address.equals(result.name.trim(), ignoreCase = true)) return null
  return address
}

// ---------------------------------------------------------------------------
// TripScreen — plan a trip to a destination and optionally monitor a station
// ---------------------------------------------------------------------------

@Composable
private fun TripScreen(
  viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.TripViewModel,
  mobilePlatform: MobileUiPlatform,
  localNotifier: LocalNotifier,
  routeLauncher: RouteLauncher,
  userLocation: GeoPoint?,
  stations: List<Station>,
  isMapReady: Boolean,
  dataFreshness: DataFreshness,
  lastUpdatedEpoch: Long?,
  stationsLoading: Boolean,
  onRefreshStations: () -> Unit,
  paddingValues: PaddingValues,
) {
  val c = LocalBiziColors.current
  val scope = rememberCoroutineScope()
  val uiState by viewModel.uiState.collectAsState()
  val tripState by viewModel.tripState.collectAsState()

  // ---------- layout ----------
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(pageBackgroundColor(mobilePlatform)),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier = Modifier.responsivePageWidth(),
    ) {
      // Map picker — hoisted out of LazyColumn so the native map view is never
      // disposed / recreated by lazy-item recycling on scroll.
      AnimatedVisibility(
        visible = tripState.destination == null && uiState.mapPickerActive,
        enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(animationSpec = tween(140)),
      ) {
        Card(
          colors = CardDefaults.cardColors(containerColor = c.surface),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
            .height(300.dp),
        ) {
          Box(modifier = Modifier.fillMaxSize()) {
            PlatformStationMap(
              modifier = Modifier.fillMaxSize(),
              stations = stations,
              userLocation = userLocation,
              highlightedStationId = null,
              isMapReady = isMapReady,
              onStationSelected = { station ->
                viewModel.onStationPickedFromMap(station)
              },
              onMapClick = { tappedLocation ->
                viewModel.onLocationPicked(tappedLocation)
              },
              pinLocation = uiState.pickedLocation,
            )
            if (uiState.isReverseGeocoding) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(c.background.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
              ) {
                CircularProgressIndicator(color = c.red, modifier = Modifier.size(32.dp))
              }
            } else {
              Surface(
                modifier = Modifier
                  .align(Alignment.TopCenter)
                  .padding(top = 10.dp),
                shape = RoundedCornerShape(20.dp),
                color = c.surface.copy(alpha = 0.92f),
              ) {
                Text(
                  stringResource(Res.string.tapMapToPickDestination),
                  style = MaterialTheme.typography.labelMedium,
                  color = c.muted,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
              }
            }
          }
        }
      }
      LazyColumn(
        modifier = Modifier
          .weight(1f),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item(key = "data-freshness") {
          DataFreshnessBanner(
            freshness = dataFreshness,
            lastUpdatedEpoch = lastUpdatedEpoch,
            loading = stationsLoading,
            onRefresh = onRefreshStations,
          )
        }
    // ---------- ALERT card (State 7) — shown above everything when active ----------
    if (tripState.alert != null) {
      val alert = tripState.alert!!
      item(key = "alert") {
        Card(
          colors = CardDefaults.cardColors(containerColor = c.red.copy(alpha = 0.09f)),
          border = BorderStroke(1.dp, c.red.copy(alpha = 0.22f)),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(Icons.Filled.Sync, contentDescription = null, tint = c.red)
              Text(
                stringResource(Res.string.tripStationFull),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = c.red,
              )
            }
            Text(
              stringResource(Res.string.tripStationNoLongerHasSlots, alert.fullStation.name),
              style = MaterialTheme.typography.bodyMedium,
            )
            val altStation = alert.alternativeStation
            if (altStation != null) {
              val dist = alert.alternativeDistanceMeters
              val distText = if (dist != null) " (${dist} m)" else ""
              Text(
                stringResource(Res.string.tripSuggestedAlternative, altStation.name, distText, altStation.slotsFree),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
              )
            } else {
              Text(
                stringResource(Res.string.tripNoNearbyAlternative),
                style = MaterialTheme.typography.bodySmall,
                color = c.muted,
              )
            }
            Button(
              onClick = { viewModel.onDismissAlert() },
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(stringResource(Res.string.gotIt))
            }
          }
        }
      }
    }

    // ---------- DESTINATION INPUT (State 1) — shown when no destination yet ----------
    if (tripState.destination == null) {
      item(key = "destination-input") {
        Card(colors = CardDefaults.cardColors(containerColor = c.surface)) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text(
              stringResource(Res.string.whereAreYouGoing),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              stringResource(Res.string.tripSubtitle),
              style = MaterialTheme.typography.bodySmall,
              color = c.muted,
            )
            OutlinedTextField(
              value = uiState.query,
              onValueChange = { viewModel.onQueryChange(it) },
              modifier = Modifier.fillMaxWidth(),
              label = { Text(stringResource(Res.string.destination)) },
              placeholder = { Text(stringResource(Res.string.destinationPlaceholder)) },
              singleLine = true,
              leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null)
              },
              trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                  IconButton(onClick = { viewModel.onClearQuery() }) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(Res.string.clearField))
                  }
                }
              },
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = c.red,
                unfocusedBorderColor = c.panel,
                cursorColor = c.red,
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
            OutlinedButton(
              onClick = { viewModel.onMapPickerToggle() },
              modifier = Modifier.fillMaxWidth(),
              border = BorderStroke(1.dp, c.blue.copy(alpha = if (uiState.mapPickerActive) 0.30f else 0.22f)),
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (uiState.mapPickerActive) c.blue.copy(alpha = 0.08f) else Color.Transparent,
              ),
            ) {
              Icon(
                if (uiState.mapPickerActive) Icons.Filled.Close else Icons.Filled.Map,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = c.blue,
              )
              Spacer(Modifier.width(6.dp))
              Text(
                if (uiState.mapPickerActive) {
                  stringResource(Res.string.cancelMap)
                } else {
                  stringResource(Res.string.pickOnMap)
                },
                color = c.blue,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }
      }

      // Autocomplete suggestions
      if (uiState.suggestions.isNotEmpty()) {
        item(key = "suggestions-header") {
          Text(
            stringResource(Res.string.suggestions),
            style = MaterialTheme.typography.labelMedium,
            color = c.muted,
          )
        }
        items(uiState.suggestions, key = { it.id }) { prediction ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = c.surface,
            border = BorderStroke(1.dp, c.panel),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewModel.onSuggestionSelected(prediction) },
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(
                Icons.Filled.LocationOn,
                contentDescription = null,
                tint = c.muted,
                modifier = Modifier.size(18.dp),
              )
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
              ) {
                Text(
                  prediction.name,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                geoSuggestionSecondaryText(prediction)?.let { secondaryText ->
                  Text(
                    secondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
              if (prediction.address.isNotBlank()) {
                Surface(
                  shape = RoundedCornerShape(999.dp),
                  color = c.panel,
                ) {
                  Text(
                    text = stringResource(Res.string.destination),
                    style = MaterialTheme.typography.labelSmall,
                    color = c.muted,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  )
                }
              }
            }
          }
        }
      } else if (uiState.isLoadingSuggestions) {
        item(key = "suggestions-loading") {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(24.dp),
              strokeWidth = 2.dp,
              color = c.red,
            )
          }
        }
      } else if (uiState.suggestionsError != null) {
        item(key = "suggestions-error") {
          Text(
            uiState.suggestionsError ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = c.red,
          )
        }
      }
    }

    // ---------- DESTINATION SELECTED section ----------
    if (tripState.destination != null) {
      // Destination header with clear button
      item(key = "destination-header") {
        Card(colors = CardDefaults.cardColors(containerColor = c.surface)) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              Icons.Filled.Navigation,
              contentDescription = null,
              tint = c.red,
              modifier = Modifier.size(22.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                stringResource(Res.string.destination),
                style = MaterialTheme.typography.labelSmall,
                color = c.muted,
              )
              Text(
                tripState.destination!!.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
              )
            }
            OutlinedButton(
              onClick = { viewModel.onClearTrip() },
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
              Icon(
                Icons.Filled.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
              Spacer(Modifier.width(4.dp))
              Text(stringResource(Res.string.clear), style = MaterialTheme.typography.labelMedium)
            }
          }
        }
      }

      // Searching spinner (State 3)
      if (tripState.isSearchingStation) {
        item(key = "searching") {
          Card(colors = CardDefaults.cardColors(containerColor = c.surface)) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
              horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = c.red,
              )
              Text(
                stringResource(Res.string.searchingNearbyStation),
                style = MaterialTheme.typography.bodyMedium,
                color = c.muted,
              )
            }
          }
        }
      }

      // Search error
      if (tripState.searchError != null) {
        item(key = "search-error") {
          Card(
            colors = CardDefaults.cardColors(containerColor = c.red.copy(alpha = 0.07f)),
            border = BorderStroke(1.dp, c.red.copy(alpha = 0.18f)),
          ) {
            Text(
              tripState.searchError!!,
              modifier = Modifier.padding(14.dp),
              style = MaterialTheme.typography.bodyMedium,
              color = c.red,
            )
          }
        }
      }
    }

    // Station found card (State 4)
    val station = tripState.nearestStationWithSlots
      if (station != null && !tripState.isSearchingStation) {
        item(key = "station-card") {
          TripStationCard(station = station, distanceMeters = tripState.distanceToStation)
        }

        // Walking route to destination button
        val destination = tripState.destination
        if (destination != null) {
          item(key = "walk-to-destination") {
            OutlinedButton(
              onClick = { routeLauncher.launchWalkToLocation(destination.location) },
              modifier = Modifier.fillMaxWidth(),
              border = BorderStroke(1.dp, c.red.copy(alpha = 0.5f)),
            ) {
              Icon(
                Icons.Filled.Directions,
                contentDescription = null,
                tint = c.red,
                modifier = Modifier.size(18.dp),
              )
              Spacer(Modifier.width(6.dp))
              Text(
                stringResource(Res.string.walkRouteTo, destination.name),
                color = c.red,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        // Monitoring active (State 6)
        if (tripState.monitoring.isActive) {
          item(key = "monitoring-active") {
            TripMonitoringActiveCard(
              monitoring = tripState.monitoring,
              onStop = { viewModel.onStopMonitoring() },
            )
          }
        } else {
          // Monitoring setup (State 5)
          item(key = "monitoring-setup") {
            TripMonitoringSetupCard(
              selectedDurationSeconds = uiState.selectedDurationSeconds,
              onDurationSelected = { viewModel.onDurationSelected(it) },
              onStartMonitoring = {
                scope.launch {
                  if (localNotifier.requestPermission()) {
                    viewModel.onStartMonitoring()
                  }
                }
              },
            )
          }
        }
      }
      }
    }
  }
}

@Composable
private fun TripStationCard(
  station: Station,
  distanceMeters: Int?,
) {
  val c = LocalBiziColors.current
  Card(colors = CardDefaults.cardColors(containerColor = c.surface)) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          Icons.AutoMirrored.Filled.DirectionsBike,
          contentDescription = null,
          tint = c.red,
        )
        Text(
          stringResource(Res.string.tripSuggestedStation),
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = c.muted,
        )
      }
      Text(
        station.name,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
      )
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        StationMetricPill(
          label = stringResource(Res.string.freeSlots),
          value = station.slotsFree.toString(),
          tint = c.blue,
        )
        StationMetricPill(
          label = stringResource(Res.string.bikes),
          value = station.bikesAvailable.toString(),
          tint = c.red,
        )
        if (distanceMeters != null) {
          StationMetricPill(
            label = stringResource(Res.string.distance),
            value = formatDistance(distanceMeters),
            tint = c.green,
          )
        }
      }
    }
  }
}

@Composable
private fun TripMonitoringSetupCard(
  selectedDurationSeconds: Int,
  onDurationSelected: (Int) -> Unit,
  onStartMonitoring: () -> Unit,
) {
  val c = LocalBiziColors.current
  Card(colors = CardDefaults.cardColors(containerColor = c.surface)) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        stringResource(Res.string.monitorThisStation),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        stringResource(Res.string.monitorThisStationDescription),
        style = MaterialTheme.typography.bodySmall,
        color = c.muted,
      )
      Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        MONITORING_DURATION_OPTIONS_SECONDS.forEach { durationSeconds ->
          val minutes = durationSeconds / 60
          FilterChip(
            selected = selectedDurationSeconds == durationSeconds,
            onClick = { onDurationSelected(durationSeconds) },
            label = { Text("${minutes} min") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = c.red.copy(alpha = 0.12f),
              selectedLabelColor = c.red,
            ),
          )
        }
      }
      Button(
        onClick = onStartMonitoring,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(stringResource(Res.string.startMonitoring))
      }
    }
  }
}

@Composable
private fun TripMonitoringActiveCard(
  monitoring: com.gcaguilar.biciradar.core.TripMonitoringState,
  onStop: () -> Unit,
) {
  val c = LocalBiziColors.current
  val remaining = monitoring.remainingSeconds
  val total = monitoring.totalSeconds
  val minutes = remaining / 60
  val seconds = remaining % 60
  val progress = if (total > 0) remaining.toFloat() / total.toFloat() else 0f

  Card(
    colors = CardDefaults.cardColors(containerColor = c.blue.copy(alpha = 0.07f)),
    border = BorderStroke(1.dp, c.blue.copy(alpha = 0.18f)),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        CircularProgressIndicator(
          progress = { progress },
          modifier = Modifier.size(22.dp),
          strokeWidth = 3.dp,
          color = c.blue,
          trackColor = c.blue.copy(alpha = 0.15f),
        )
        Text(
          stringResource(Res.string.monitoringActive),
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = c.blue,
        )
      }
      Text(
        stringResource(Res.string.remainingTime, "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
      Text(
        stringResource(Res.string.monitoringActiveDescription),
        style = MaterialTheme.typography.bodySmall,
        color = c.muted,
      )
      OutlinedButton(
        onClick = onStop,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(stringResource(Res.string.stopMonitoring))
      }
    }
  }
}

private data class ShortcutGuide(
  val title: String,
  val description: String,
  val examples: List<String>,
  val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

@Composable
private fun ShortcutGuideCard(
  guide: ShortcutGuide,
) {
  Card(colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface)) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(guide.icon, contentDescription = null, tint = LocalBiziColors.current.red)
        Text(guide.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
      }
      Text(
        guide.description,
        style = MaterialTheme.typography.bodySmall,
        color = LocalBiziColors.current.muted,
      )
      guide.examples.forEach { example ->
        Text(
          "\u2022 $example",
          style = MaterialTheme.typography.bodyMedium,
          color = LocalBiziColors.current.ink,
        )
      }
    }
  }
}

@Composable
private fun AvailabilityCard(
  modifier: Modifier,
  label: String,
  value: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  tint: Color,
  mobilePlatform: MobileUiPlatform,
) {
  Card(
    modifier = modifier,
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, tint.copy(alpha = 0.14f)) else null,
    colors = CardDefaults.cardColors(
      containerColor = if (mobilePlatform == MobileUiPlatform.IOS) {
        LocalBiziColors.current.surface
      } else {
        tint.copy(alpha = 0.08f)
      },
    ),
  ) {
    Column(
      modifier = Modifier
        .padding(16.dp)
        .animateContentSize(animationSpec = spring(dampingRatio = 0.86f, stiffness = 500f)),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
      Text(label, color = tint)
      Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    }
  }
}

@Composable
private fun StationRow(
  mobilePlatform: MobileUiPlatform,
  station: Station,
  isFavorite: Boolean,
  onClick: () -> Unit,
  onFavoriteToggle: () -> Unit,
  onQuickRoute: (() -> Unit)? = null,
  savedPlaceAlertSlot: @Composable (() -> Unit)? = null,
  extraActions: @Composable (() -> Unit)? = null,
  showFavoriteCta: Boolean = true,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .animateContentSize(animationSpec = spring(dampingRatio = 0.9f, stiffness = 520f)),
    shape = RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 22.dp else 24.dp),
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, LocalBiziColors.current.panel) else null,
    colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            station.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            station.address,
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
        Spacer(Modifier.width(12.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          onQuickRoute?.let { quickRoute ->
            RoutePill(
              label = stringResource(Res.string.route),
              onClick = quickRoute,
            )
          }
          savedPlaceAlertSlot?.invoke()
          if (showFavoriteCta) {
            FavoritePill(
              active = isFavorite,
              onClick = onFavoriteToggle,
              label = if (isFavorite) stringResource(Res.string.saved) else stringResource(Res.string.save),
            )
          } else {
            FavoritePill(
              active = true,
              onClick = {},
              label = stringResource(Res.string.favorite),
            )
          }
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StationMetricPill(
          modifier = Modifier.weight(1f),
          label = stringResource(Res.string.bikes),
          value = station.bikesAvailable.toString(),
          tint = LocalBiziColors.current.red,
        )
        StationMetricPill(
          modifier = Modifier.weight(1f),
          label = stringResource(Res.string.slots),
          value = station.slotsFree.toString(),
          tint = LocalBiziColors.current.blue,
        )
        StationMetricPill(
          modifier = Modifier.weight(1f),
          label = stringResource(Res.string.distance),
          value = formatDistance(station.distanceMeters),
          tint = LocalBiziColors.current.green,
        )
      }
      extraActions?.let { actions ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          actions()
        }
      }
    }
  }
}

@Composable
private fun DismissibleFavoriteStationRow(
  mobilePlatform: MobileUiPlatform,
  station: Station,
  canAssignHome: Boolean,
  canAssignWork: Boolean,
  onClick: () -> Unit,
  onAssignHome: () -> Unit,
  onAssignWork: () -> Unit,
  onQuickRoute: () -> Unit,
  onRemoveFavorite: () -> Unit,
  onSavedPlaceAlertClick: (() -> Unit)?,
  savedPlaceAlertActive: Boolean,
) {
  val dismissState = rememberSwipeToDismissBoxState()
  LaunchedEffect(dismissState.currentValue) {
    if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
      onRemoveFavorite()
      dismissState.snapTo(SwipeToDismissBoxValue.Settled)
    }
  }
  SwipeToDismissBox(
    state = dismissState,
    enableDismissFromStartToEnd = false,
    backgroundContent = {
      FavoriteDismissBackground(
        mobilePlatform = mobilePlatform,
        progress = dismissState.progress,
      )
    },
    content = {
      StationRow(
        mobilePlatform = mobilePlatform,
        station = station,
        isFavorite = true,
        onClick = onClick,
        onFavoriteToggle = {},
        onQuickRoute = onQuickRoute,
        savedPlaceAlertSlot = if (onSavedPlaceAlertClick != null) {
          {
            IconButton(
              onClick = onSavedPlaceAlertClick,
              modifier = Modifier.size(40.dp),
            ) {
              Icon(
                imageVector = if (savedPlaceAlertActive) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                contentDescription = stringResource(Res.string.savedPlaceAlertsBell),
                tint = if (savedPlaceAlertActive) LocalBiziColors.current.blue else LocalBiziColors.current.muted,
              )
            }
          }
        } else {
          null
        },
        extraActions = {
          if (canAssignHome) {
            SavedPlaceQuickAction(
              label = stringResource(Res.string.home),
              tint = LocalBiziColors.current.green,
              onClick = onAssignHome,
            )
          }
          if (canAssignWork) {
            SavedPlaceQuickAction(
              label = stringResource(Res.string.work),
              tint = LocalBiziColors.current.blue,
              onClick = onAssignWork,
            )
          }
        },
        showFavoriteCta = false,
      )
    },
  )
}

@Composable
private fun FavoriteDismissBackground(
  mobilePlatform: MobileUiPlatform,
  progress: Float,
) {
  val clampedProgress = progress.coerceIn(0f, 1f)
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 22.dp else 24.dp))
      .background(LocalBiziColors.current.red.copy(alpha = 0.10f + (0.10f * clampedProgress)))
      .padding(horizontal = 20.dp, vertical = 12.dp),
    contentAlignment = Alignment.CenterEnd,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.graphicsLayer {
        alpha = 0.55f + (0.45f * clampedProgress)
        scaleX = 0.92f + (0.08f * clampedProgress)
        scaleY = 0.92f + (0.08f * clampedProgress)
      },
    ) {
      Icon(
        Icons.Filled.Delete,
        contentDescription = null,
        tint = LocalBiziColors.current.red,
      )
      Text(
        text = if (mobilePlatform == MobileUiPlatform.IOS) stringResource(Res.string.removeFavorite) else stringResource(Res.string.deleteFavorite),
        color = LocalBiziColors.current.red,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

@Composable
private fun RoutePill(
  label: String,
  onClick: () -> Unit,
  onDarkBackground: Boolean = false,
) {
  val c = LocalBiziColors.current
  val pillColor = if (onDarkBackground) c.onAccent else c.blue
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = if (onDarkBackground) c.onAccent.copy(alpha = 0.14f) else c.blue.copy(alpha = 0.08f),
    border = BorderStroke(1.dp, if (onDarkBackground) c.onAccent.copy(alpha = 0.32f) else c.blue.copy(alpha = 0.16f)),
    modifier = Modifier.clickable(onClick = onClick),
  ) {
    Row(
      modifier = Modifier
        .padding(horizontal = 12.dp, vertical = 9.dp)
        .animateContentSize(animationSpec = tween(180)),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(
        imageVector = Icons.Filled.Directions,
        contentDescription = null,
        tint = pillColor,
        modifier = Modifier.size(16.dp),
      )
      Text(label, color = pillColor, style = MaterialTheme.typography.labelMedium)
    }
  }
}

@Composable
private fun SavedPlaceCard(
  mobilePlatform: MobileUiPlatform,
  title: String,
  station: Station?,
  assignmentCandidate: Station?,
  onAssignCandidate: (Station) -> Unit,
  onClear: () -> Unit,
  onOpenStationDetails: (Station) -> Unit,
  onQuickRoute: (Station) -> Unit,
  onSavedPlaceAlertClick: (() -> Unit)? = null,
  savedPlaceAlertActive: Boolean = false,
) {
  val assignableCandidate = assignmentCandidate?.takeIf { it.id != station?.id }
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .animateContentSize(animationSpec = spring(dampingRatio = 0.88f, stiffness = 500f)),
    shape = RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 22.dp else 24.dp),
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, LocalBiziColors.current.panel) else null,
    colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
      )
      if (station != null) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = station.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = station.address,
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          StationMetricPill(
            modifier = Modifier.weight(1f),
            label = stringResource(Res.string.bikes),
            value = station.bikesAvailable.toString(),
            tint = LocalBiziColors.current.red,
          )
          StationMetricPill(
            modifier = Modifier.weight(1f),
            label = stringResource(Res.string.slots),
            value = station.slotsFree.toString(),
            tint = LocalBiziColors.current.blue,
          )
          StationMetricPill(
            modifier = Modifier.weight(1f),
            label = stringResource(Res.string.distance),
            value = formatDistance(station.distanceMeters),
            tint = LocalBiziColors.current.green,
          )
        }
      } else {
        Text(
          text = stringResource(Res.string.savedPlaceNotSet, title),
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (station != null) {
          RoutePill(
            label = stringResource(Res.string.route),
            onClick = { onQuickRoute(station) },
          )
          OutlineActionPill(
            label = stringResource(Res.string.details),
            tint = LocalBiziColors.current.red,
            borderTint = LocalBiziColors.current.red.copy(alpha = 0.16f),
            onClick = { onOpenStationDetails(station) },
          )
        }
        if (assignableCandidate != null) {
          OutlineActionPill(
            label = stringResource(Res.string.assignSearchResult),
            tint = LocalBiziColors.current.blue,
            borderTint = LocalBiziColors.current.blue.copy(alpha = 0.16f),
            onClick = { onAssignCandidate(assignableCandidate) },
          )
        }
        if (station != null) {
          OutlineActionPill(
            label = stringResource(Res.string.remove),
            tint = LocalBiziColors.current.muted,
            borderTint = LocalBiziColors.current.panel,
            onClick = onClear,
          )
        }
        if (station != null && onSavedPlaceAlertClick != null) {
          IconButton(
            onClick = onSavedPlaceAlertClick,
            modifier = Modifier.size(40.dp),
          ) {
            Icon(
              imageVector = if (savedPlaceAlertActive) Icons.Filled.Notifications else Icons.Outlined.Notifications,
              contentDescription = stringResource(Res.string.savedPlaceAlertsBell),
              tint = if (savedPlaceAlertActive) LocalBiziColors.current.blue else LocalBiziColors.current.muted,
            )
          }
        }
      }
      if (assignableCandidate != null) {
        Text(
          text = stringResource(Res.string.currentSearchAssignmentHint, assignableCandidate.name, title),
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      } else if (station == null) {
        Text(
          text = stringResource(Res.string.useSearchToAssignStation),
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      }
    }
  }
}

@Composable
private fun SavedPlaceQuickAction(
  label: String,
  tint: Color,
  onClick: () -> Unit,
) {
  OutlineActionPill(
    label = label,
    tint = tint,
    borderTint = tint.copy(alpha = 0.16f),
    onClick = onClick,
  )
}

@Composable
private fun MobilePageHeader(
  title: String,
  subtitle: String,
  onOpenAssistant: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top,
  ) {
    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.Top,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = LocalBiziColors.current.muted,
        )
      }
    }
    Spacer(Modifier.width(12.dp))
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = LocalBiziColors.current.red.copy(alpha = 0.10f),
      border = BorderStroke(1.dp, LocalBiziColors.current.red.copy(alpha = 0.12f)),
      modifier = Modifier.clickable(onClick = onOpenAssistant),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          Icons.Filled.KeyboardVoice,
          contentDescription = null,
          tint = LocalBiziColors.current.red,
        )
        Text(stringResource(Res.string.shortcuts), color = LocalBiziColors.current.red, fontWeight = FontWeight.SemiBold)
      }
    }
  }
}

@Composable
private fun StationSearchField(
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
    trailingIcon = if (value.isNotEmpty()) {
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
    label = if (mobilePlatform == MobileUiPlatform.Android) {
      { Text(label) }
    } else {
      null
    },
    placeholder = { Text(label, color = c.muted) },
    colors = OutlinedTextFieldDefaults.colors(
      focusedContainerColor = if (mobilePlatform == MobileUiPlatform.IOS) c.fieldSurfaceIos else c.fieldSurfaceAndroid,
      unfocusedContainerColor = if (mobilePlatform == MobileUiPlatform.IOS) c.fieldSurfaceIos else c.fieldSurfaceAndroid,
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
private fun QuickRouteActionCard(
  modifier: Modifier,
  title: String,
  emptyTitle: String,
  selection: com.gcaguilar.biciradar.core.NearbyStationSelection,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  tint: Color,
  mobilePlatform: MobileUiPlatform,
  onRoute: (Station) -> Unit,
) {
  val station = selection.highlightedStation
  Card(
    modifier = modifier
      .clickable(enabled = station != null) {
        station?.let(onRoute)
      },
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, LocalBiziColors.current.panel) else null,
    colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
  ) {
    Column(
      modifier = Modifier
        .padding(14.dp)
        .animateContentSize(animationSpec = spring(dampingRatio = 0.88f, stiffness = 520f)),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
      Text(title, style = MaterialTheme.typography.labelSmall, color = LocalBiziColors.current.muted)
      if (station == null) {
        Text(
          emptyTitle,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = tint,
        )
        Text(
          stringResource(Res.string.refreshStationsToOpenRoute),
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      } else {
        Text(
          station.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = tint,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          if (selection.usesFallback) {
            stringResource(
              Res.string.quickRouteFallbackSummary,
              formatDistance(selection.radiusMeters),
              formatDistance(station.distanceMeters),
            )
          } else {
            stringResource(
              Res.string.quickRouteDistanceSummary,
              formatDistance(station.distanceMeters),
              station.bikesAvailable,
              station.slotsFree,
            )
          },
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
        Text(
          stringResource(Res.string.openRoute),
          style = MaterialTheme.typography.labelMedium,
          color = tint,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }
  }
}

@Composable
private fun OutlineActionPill(
  label: String,
  tint: Color,
  borderTint: Color,
  onClick: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = Color.Transparent,
    border = BorderStroke(1.dp, borderTint),
    modifier = Modifier.clickable(onClick = onClick),
  ) {
    Row(
      modifier = Modifier
        .padding(horizontal = 12.dp, vertical = 9.dp)
        .animateContentSize(animationSpec = tween(180)),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(label, color = tint, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
  }
}

@Composable
private fun EmptyStateCard(
  title: String,
  description: String,
  primaryAction: String? = null,
  onPrimaryAction: (() -> Unit)? = null,
) {
  val c = LocalBiziColors.current
  Card(
    modifier = Modifier.animateContentSize(animationSpec = spring(dampingRatio = 0.9f, stiffness = 500f)),
    colors = CardDefaults.cardColors(containerColor = c.surface),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(title, fontWeight = FontWeight.SemiBold, color = c.ink)
      Text(description, style = MaterialTheme.typography.bodySmall, color = c.muted)
      if (primaryAction != null && onPrimaryAction != null) {
        OutlinedButton(
          onClick = onPrimaryAction,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = c.red),
        ) {
          Text(primaryAction)
        }
      }
    }
  }
}

@Composable
private fun RadiusSelectionButton(
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
    modifier = modifier
      .graphicsLayer {
        scaleX = selectionScale
        scaleY = selectionScale
      }
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(18.dp),
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
  ) {
    Text(
      text = label,
      modifier = Modifier
        .padding(horizontal = 14.dp, vertical = 12.dp)
        .animateContentSize(animationSpec = tween(180)),
      color = textColor,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
    )
  }
}

private fun PreferredMapApp.displayName(): String = when (this) {
  PreferredMapApp.AppleMaps -> "Apple Maps"
  PreferredMapApp.GoogleMaps -> "Google Maps"
}

@Composable
private fun CitySelector(
  selectedCity: City,
  onCitySelected: (City) -> Unit,
) {
  val colors = LocalBiziColors.current
  var expanded by remember { mutableStateOf(false) }

  Box {
    OutlinedButton(
      onClick = { expanded = true },
      modifier = Modifier.fillMaxWidth(),
      border = BorderStroke(1.dp, colors.panel),
      colors = ButtonDefaults.outlinedButtonColors(containerColor = colors.surface),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = selectedCity.displayName,
          color = colors.ink,
        )
        Icon(
          imageVector = Icons.Filled.Navigation,
          contentDescription = null,
          tint = colors.red,
        )
      }
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.background(colors.surface),
    ) {
      City.entries.sortedBy { it.displayName }.forEach { city ->
        DropdownMenuItem(
          text = {
            Text(
              text = city.displayName,
              color = if (city == selectedCity) colors.red else colors.ink,
              fontWeight = if (city == selectedCity) FontWeight.SemiBold else FontWeight.Normal,
            )
          },
          onClick = {
            onCitySelected(city)
            expanded = false
          },
          leadingIcon = {
            if (city == selectedCity) {
              Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = colors.red,
              )
            }
          },
        )
      }
    }
  }
}

private fun MobileUiPlatform.assistantDisplayName(): String = when (this) {
  MobileUiPlatform.Android -> "Google Assistant"
  MobileUiPlatform.IOS -> "Siri"
  MobileUiPlatform.Desktop -> "Asistente"
}

@Composable
private fun shortcutGuidesFor(
  mobilePlatform: MobileUiPlatform,
): List<ShortcutGuide> = listOf(
  ShortcutGuide(
    title = stringResource(Res.string.mapNearestStationLabel),
    description = stringResource(Res.string.guideNearestStationDescription),
    examples = listOf(
      stringResource(Res.string.guideNearestStationExampleNearest),
      stringResource(Res.string.guideNearestStationExampleClosest),
    ),
    icon = Icons.Filled.LocationOn,
  ),
  ShortcutGuide(
    title = stringResource(Res.string.guideNearestWithBikesOrSlots),
    description = stringResource(Res.string.guideNearestWithBikesOrSlotsDescription),
    examples = listOf(
      stringResource(Res.string.guideNearestWithBikesOrSlotsExampleBikes),
      stringResource(Res.string.guideNearestWithBikesOrSlotsExampleSlots),
    ),
    icon = Icons.AutoMirrored.Filled.DirectionsBike,
  ),
  ShortcutGuide(
    title = stringResource(Res.string.guideStationStatus),
    description = stringResource(Res.string.guideStationStatusDescription),
    examples = listOf(
      stringResource(Res.string.guideStationStatusExampleHome),
      stringResource(Res.string.guideStationStatusExampleHomeBikes),
      stringResource(Res.string.guideStationStatusExampleStationSlots),
    ),
    icon = Icons.Filled.Search,
  ),
  ShortcutGuide(
    title = stringResource(Res.string.favorites),
    description = stringResource(Res.string.guideFavoritesDescription),
    examples = listOf(
      stringResource(Res.string.guideFavoritesExampleOpen),
      stringResource(Res.string.guideFavoritesExampleWork),
    ),
    icon = Icons.Filled.Favorite,
  ),
  ShortcutGuide(
    title = stringResource(Res.string.guideRouteToStation),
    description = stringResource(Res.string.guideRouteToStationDescription),
    examples = listOf(
      stringResource(Res.string.guideRouteToStationExamplePlazaEspana),
      stringResource(Res.string.guideRouteToStationExampleWork),
    ),
    icon = Icons.Filled.Directions,
  ),
)

@Composable
private fun StationMetricPill(
  modifier: Modifier = Modifier,
  label: String,
  value: String,
  tint: Color,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    color = tint.copy(alpha = 0.09f),
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 12.dp, vertical = 10.dp)
        .animateContentSize(animationSpec = spring(dampingRatio = 0.9f, stiffness = 550f)),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
      Text(
        value,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun FavoritePill(
  active: Boolean,
  onClick: () -> Unit,
  label: String,
) {
  val containerColor by animateColorAsState(
    targetValue = if (active) LocalBiziColors.current.red.copy(alpha = 0.10f) else Color.Transparent,
    animationSpec = tween(180),
    label = "favorite-pill-container",
  )
  val borderColor by animateColorAsState(
    targetValue = if (active) LocalBiziColors.current.red.copy(alpha = 0.16f) else LocalBiziColors.current.panel,
    animationSpec = tween(180),
    label = "favorite-pill-border",
  )
  val scale by animateFloatAsState(
    targetValue = if (active) 1f else 0.97f,
    animationSpec = spring(dampingRatio = 0.78f, stiffness = 720f),
    label = "favorite-pill-scale",
  )
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
    modifier = Modifier
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .clickable(onClick = onClick),
  ) {
    Row(
      modifier = Modifier
        .padding(horizontal = 12.dp, vertical = 9.dp)
        .animateContentSize(animationSpec = tween(180)),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(
        imageVector = if (active) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
        contentDescription = null,
        tint = LocalBiziColors.current.red,
        modifier = Modifier.size(16.dp),
      )
      Text(label, color = LocalBiziColors.current.red, style = MaterialTheme.typography.labelMedium)
    }
  }
}

@Composable
private fun SavedPlacePill(
  active: Boolean,
  label: String,
  onClick: () -> Unit,
) {
  val tint = if (label == stringResource(Res.string.home)) LocalBiziColors.current.green else LocalBiziColors.current.blue
  val containerColor by animateColorAsState(
    targetValue = if (active) tint.copy(alpha = 0.10f) else Color.Transparent,
    animationSpec = tween(180),
    label = "saved-place-pill-container",
  )
  val borderColor by animateColorAsState(
    targetValue = if (active) tint.copy(alpha = 0.18f) else LocalBiziColors.current.panel,
    animationSpec = tween(180),
    label = "saved-place-pill-border",
  )
  val scale by animateFloatAsState(
    targetValue = if (active) 1f else 0.97f,
    animationSpec = spring(dampingRatio = 0.78f, stiffness = 720f),
    label = "saved-place-pill-scale",
  )
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
    modifier = Modifier
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .clickable(onClick = onClick),
  ) {
    Text(
      text = label,
      modifier = Modifier
        .padding(horizontal = 12.dp, vertical = 9.dp)
        .animateContentSize(animationSpec = tween(180)),
      color = tint,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
    )
  }
}

@Composable
internal fun pageBackgroundColor(platform: MobileUiPlatform): Color {
  val c = LocalBiziColors.current
  return if (platform == MobileUiPlatform.IOS) c.groupedBackground else c.background
}

@Composable
private fun Modifier.responsivePageWidth(): Modifier {
  val maxWidth = when (LocalBiziWindowLayout.current) {
    BiziWindowLayout.Compact -> null
    BiziWindowLayout.Medium -> 760.dp
    BiziWindowLayout.Expanded -> 920.dp
  }
  return if (maxWidth == null) {
    fillMaxSize()
  } else {
    fillMaxSize().widthIn(max = maxWidth)
  }
}

private fun NavHostController.navigateToPrimaryDestination(screen: Screen) {
  navigate(screen) {
    launchSingleTop = true
    restoreState = true
    popUpTo(Screen.Nearby) { saveState = true }
  }
}

private fun MobileTab.screen(): Screen = when (this) {
  MobileTab.Mapa -> Screen.Map
  MobileTab.Cerca -> Screen.Nearby
  MobileTab.Favoritos -> Screen.Favorites
  MobileTab.Viaje -> Screen.Trip()
  MobileTab.Perfil -> Screen.Profile
}

private fun MobileTab.icon() = when (this) {
  MobileTab.Mapa -> Icons.Filled.Map
  MobileTab.Cerca -> Icons.Filled.LocationOn
  MobileTab.Favoritos -> Icons.Filled.Favorite
  MobileTab.Viaje -> Icons.Filled.Navigation
  MobileTab.Perfil -> Icons.Filled.Tune
}

private fun AssistantAction.icon() = when (this) {
  AssistantAction.FavoriteStations -> Icons.Filled.Favorite
  AssistantAction.NearestStation -> Icons.Filled.LocationOn
  AssistantAction.NearestStationWithBikes -> Icons.AutoMirrored.Filled.DirectionsBike
  AssistantAction.NearestStationWithSlots -> Icons.Filled.LocalParking
  is AssistantAction.StationBikeCount -> Icons.AutoMirrored.Filled.DirectionsBike
  is AssistantAction.StationSlotCount -> Icons.Filled.LocalParking
  is AssistantAction.RouteToStation -> Icons.Filled.Directions
  is AssistantAction.StationStatus -> Icons.Filled.LocationOn
}

@Composable
private fun AssistantAction.label(): String = when (this) {
  AssistantAction.FavoriteStations -> stringResource(Res.string.myFavorites)
  AssistantAction.NearestStation -> stringResource(Res.string.mapNearestStationLabel)
  AssistantAction.NearestStationWithBikes -> stringResource(Res.string.assistantLabelNearestWithBikes)
  AssistantAction.NearestStationWithSlots -> stringResource(Res.string.assistantLabelNearestWithSlots)
  is AssistantAction.StationBikeCount -> stringResource(Res.string.bikesInStation)
  is AssistantAction.StationSlotCount -> stringResource(Res.string.slotsInStation)
  is AssistantAction.RouteToStation -> stringResource(Res.string.routeToStation)
  is AssistantAction.StationStatus -> stringResource(Res.string.stationStatusLabel)
}

private fun filterStations(
  stations: List<Station>,
  searchQuery: String,
): List<Station> = filterStationsByQuery(stations, searchQuery)

private fun resolveLaunchStation(
  stations: List<Station>,
  graph: SharedGraph,
  stationId: String?,
  stationQuery: String?,
): Station? {
  val stationsRepository = graph.stationsRepository
  return stationId?.let(stationsRepository::stationById) ?: findStationMatchingQueryOrPinnedAlias(
    stations = stations,
    query = stationQuery,
    homeStationId = graph.favoritesRepository.currentHomeStationId(),
    workStationId = graph.favoritesRepository.currentWorkStationId(),
  )
}

internal object BiziMobileAppContent {
  @Composable
  fun TripScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.TripViewModel,
    mobilePlatform: MobileUiPlatform,
    localNotifier: LocalNotifier,
    routeLauncher: RouteLauncher,
    userLocation: GeoPoint?,
    stations: List<Station>,
    isMapReady: Boolean,
    dataFreshness: DataFreshness,
    lastUpdatedEpoch: Long?,
    stationsLoading: Boolean,
    onRefreshStations: () -> Unit,
    paddingValues: PaddingValues,
  ) = TripScreen(
    viewModel = viewModel,
    mobilePlatform = mobilePlatform,
    localNotifier = localNotifier,
    routeLauncher = routeLauncher,
    userLocation = userLocation,
    stations = stations,
    isMapReady = isMapReady,
    dataFreshness = dataFreshness,
    lastUpdatedEpoch = lastUpdatedEpoch,
    stationsLoading = stationsLoading,
    onRefreshStations = onRefreshStations,
    paddingValues = paddingValues,
  )

  @Composable
  fun NearbyScreenContent(
    mobilePlatform: MobileUiPlatform,
    stations: List<Station>,
    favoriteIds: Set<String>,
    loading: Boolean,
    errorMessage: String?,
    dataFreshness: DataFreshness,
    lastUpdatedEpoch: Long?,
    nearestSelection: com.gcaguilar.biciradar.core.NearbyStationSelection,
    searchRadiusMeters: Int,
    onStationSelected: (Station) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onFavoriteToggle: (Station) -> Unit,
    onQuickRoute: (Station) -> Unit,
    refreshCountdownSeconds: Int,
    paddingValues: PaddingValues,
  ) = NearbyScreen(
    mobilePlatform = mobilePlatform,
    stations = stations,
    favoriteIds = favoriteIds,
    loading = loading,
    errorMessage = errorMessage,
    dataFreshness = dataFreshness,
    lastUpdatedEpoch = lastUpdatedEpoch,
    nearestSelection = nearestSelection,
    searchRadiusMeters = searchRadiusMeters,
    onStationSelected = onStationSelected,
    onRetry = onRetry,
    onRefresh = onRefresh,
    onFavoriteToggle = onFavoriteToggle,
    onQuickRoute = onQuickRoute,
    refreshCountdownSeconds = refreshCountdownSeconds,
    paddingValues = paddingValues,
  )

  @Composable
  fun NearbyScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.NearbyViewModel,
    mobilePlatform: MobileUiPlatform,
    onStationSelected: (Station) -> Unit,
    paddingValues: PaddingValues,
  ) {
    val uiState by viewModel.uiState.collectAsState()
    NearbyScreen(
      mobilePlatform = mobilePlatform,
      stations = uiState.stations,
      favoriteIds = uiState.favoriteIds,
      loading = uiState.isLoading,
      errorMessage = uiState.errorMessage,
      dataFreshness = uiState.dataFreshness,
      lastUpdatedEpoch = uiState.lastUpdatedEpoch,
      nearestSelection = uiState.nearestSelection,
      searchRadiusMeters = uiState.searchRadiusMeters,
      onStationSelected = onStationSelected,
      onRetry = viewModel::onRetry,
      onRefresh = viewModel::onRefresh,
      onFavoriteToggle = viewModel::onFavoriteToggle,
      onQuickRoute = viewModel::onQuickRoute,
      refreshCountdownSeconds = uiState.refreshCountdownSeconds,
      paddingValues = paddingValues,
    )
  }

  @Composable
  fun FavoritesScreenContent(
    mobilePlatform: MobileUiPlatform,
    onOpenAssistant: () -> Unit,
    allStations: List<Station>,
    stations: List<Station>,
    homeStation: Station?,
    workStation: Station?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onStationSelected: (Station) -> Unit,
    onAssignHomeStation: (Station) -> Unit,
    onAssignWorkStation: (Station) -> Unit,
    onClearHomeStation: () -> Unit,
    onClearWorkStation: () -> Unit,
    onRemoveFavorite: (Station) -> Unit,
    onQuickRoute: (Station) -> Unit,
    dataFreshness: DataFreshness,
    lastUpdatedEpoch: Long?,
    stationsLoading: Boolean,
    onRefreshStations: () -> Unit,
    onOpenSavedPlaceAlerts: () -> Unit,
    paddingValues: PaddingValues,
    savedPlaceAlertsCityId: String = City.ZARAGOZA.id,
    savedPlaceAlertRules: List<SavedPlaceAlertRule> = emptyList(),
    onUpsertSavedPlaceAlert: ((SavedPlaceAlertTarget, SavedPlaceAlertCondition) -> Unit)? = null,
    onRemoveSavedPlaceAlertForTarget: ((SavedPlaceAlertTarget) -> Unit)? = null,
  ) = FavoritesScreen(
    mobilePlatform = mobilePlatform,
    onOpenAssistant = onOpenAssistant,
    allStations = allStations,
    stations = stations,
    homeStation = homeStation,
    workStation = workStation,
    searchQuery = searchQuery,
    onSearchQueryChange = onSearchQueryChange,
    onStationSelected = onStationSelected,
    onAssignHomeStation = onAssignHomeStation,
    onAssignWorkStation = onAssignWorkStation,
    onClearHomeStation = onClearHomeStation,
    onClearWorkStation = onClearWorkStation,
    onRemoveFavorite = onRemoveFavorite,
    onQuickRoute = onQuickRoute,
    dataFreshness = dataFreshness,
    lastUpdatedEpoch = lastUpdatedEpoch,
    stationsLoading = stationsLoading,
    onRefreshStations = onRefreshStations,
    onOpenSavedPlaceAlerts = onOpenSavedPlaceAlerts,
    paddingValues = paddingValues,
    savedPlaceAlertsCityId = savedPlaceAlertsCityId,
    savedPlaceAlertRules = savedPlaceAlertRules,
    onUpsertSavedPlaceAlert = onUpsertSavedPlaceAlert,
    onRemoveSavedPlaceAlertForTarget = onRemoveSavedPlaceAlertForTarget,
  )

  @Composable
  fun FavoritesScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.FavoritesViewModel,
    mobilePlatform: MobileUiPlatform,
    onOpenAssistant: () -> Unit,
    onStationSelected: (Station) -> Unit,
    dataFreshness: DataFreshness,
    lastUpdatedEpoch: Long?,
    stationsLoading: Boolean,
    onRefreshStations: () -> Unit,
    onOpenSavedPlaceAlerts: () -> Unit,
    paddingValues: PaddingValues,
  ) {
    val uiState by viewModel.uiState.collectAsState()
    FavoritesScreen(
      mobilePlatform = mobilePlatform,
      onOpenAssistant = onOpenAssistant,
      allStations = uiState.allStations,
      stations = uiState.favoriteStations,
      homeStation = uiState.homeStation,
      workStation = uiState.workStation,
      searchQuery = uiState.searchQuery,
      onSearchQueryChange = viewModel::onSearchQueryChange,
      onStationSelected = onStationSelected,
      onAssignHomeStation = viewModel::onAssignHomeStation,
      onAssignWorkStation = viewModel::onAssignWorkStation,
      onClearHomeStation = viewModel::onClearHomeStation,
      onClearWorkStation = viewModel::onClearWorkStation,
      onRemoveFavorite = viewModel::onRemoveFavorite,
      onQuickRoute = viewModel::onQuickRoute,
      dataFreshness = dataFreshness,
      lastUpdatedEpoch = lastUpdatedEpoch,
      stationsLoading = stationsLoading,
      onRefreshStations = onRefreshStations,
      onOpenSavedPlaceAlerts = onOpenSavedPlaceAlerts,
      paddingValues = paddingValues,
      savedPlaceAlertsCityId = uiState.savedPlaceAlertsCityId,
      savedPlaceAlertRules = uiState.savedPlaceAlertRules,
      onUpsertSavedPlaceAlert = viewModel::onUpsertSavedPlaceAlert,
      onRemoveSavedPlaceAlertForTarget = viewModel::onRemoveSavedPlaceAlertForTarget,
    )
  }

  @Composable
  fun ProfileScreenContent(
    mobilePlatform: MobileUiPlatform,
    paddingValues: PaddingValues,
    searchRadiusMeters: Int,
    preferredMapApp: PreferredMapApp,
    themePreference: ThemePreference,
    selectedCity: City,
    onOpenShortcuts: () -> Unit,
    onSearchRadiusSelected: (Int) -> Unit,
    onPreferredMapAppSelected: (PreferredMapApp) -> Unit,
    onThemePreferenceSelected: (ThemePreference) -> Unit,
    onCitySelected: (City) -> Unit,
    showProfileSetupCard: Boolean = false,
    onShowChangelog: () -> Unit = {},
    onOpenFeedback: () -> Unit = {},
    onOpenOnboarding: () -> Unit = {},
    onRateApp: () -> Unit = {},
  ) = ProfileScreen(
    mobilePlatform = mobilePlatform,
    paddingValues = paddingValues,
    searchRadiusMeters = searchRadiusMeters,
    preferredMapApp = preferredMapApp,
    themePreference = themePreference,
    selectedCity = selectedCity,
    onOpenShortcuts = onOpenShortcuts,
    onSearchRadiusSelected = onSearchRadiusSelected,
    onPreferredMapAppSelected = onPreferredMapAppSelected,
    onThemePreferenceSelected = onThemePreferenceSelected,
    onCitySelected = onCitySelected,
    showProfileSetupCard = showProfileSetupCard,
    onShowChangelog = onShowChangelog,
    onOpenOnboarding = onOpenOnboarding,
    onOpenFeedback = onOpenFeedback,
    onRateApp = onRateApp,
  )

  @Composable
  fun ProfileScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.ProfileViewModel,
    mobilePlatform: MobileUiPlatform,
    paddingValues: PaddingValues,
    onOpenShortcuts: () -> Unit,
    onOpenOnboarding: () -> Unit,
    platformBindings: PlatformBindings,
    onShowChangelogManual: () -> Unit,
  ) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) {
      viewModel.refreshSetupRequirements()
    }
    ProfileScreen(
      mobilePlatform = mobilePlatform,
      paddingValues = paddingValues,
      searchRadiusMeters = uiState.searchRadiusMeters,
      preferredMapApp = uiState.preferredMapApp,
      themePreference = uiState.themePreference,
      selectedCity = uiState.selectedCity,
      onOpenShortcuts = onOpenShortcuts,
      onSearchRadiusSelected = viewModel::onSearchRadiusSelected,
      onPreferredMapAppSelected = viewModel::onPreferredMapAppSelected,
      onThemePreferenceSelected = viewModel::onThemePreferenceSelected,
      onCitySelected = viewModel::onCitySelected,
      showProfileSetupCard = uiState.showProfileSetupCard,
      onShowChangelog = onShowChangelogManual,
      onOpenOnboarding = onOpenOnboarding,
      onOpenFeedback = { platformBindings.externalLinks.openFeedbackForm() },
      onRateApp = {
        // Manual CTA should open the store review page directly.
        platformBindings.reviewPrompter.openStoreWriteReview()
      },
    )
  }

  @Composable
  fun SavedPlaceAlertsScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.SavedPlaceAlertsViewModel,
    mobilePlatform: MobileUiPlatform,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
  ) {
    val uiState by viewModel.uiState.collectAsState()
    SavedPlaceAlertsListScreen(
      mobilePlatform = mobilePlatform,
      rules = uiState.rules,
      paddingValues = paddingValues,
      onBack = onBack,
      onSetEnabled = viewModel::onSetEnabled,
      onUpsert = viewModel::onUpsert,
      onRemoveRule = viewModel::onRemoveRule,
    )
  }

  @Composable
  fun ShortcutsScreenContent(
    mobilePlatform: MobileUiPlatform,
    paddingValues: PaddingValues,
    graph: SharedGraph,
    stationsRepository: StationsRepository,
    favoriteIds: Set<String>,
    searchRadiusMeters: Int,
    initialAction: AssistantAction?,
    onInitialActionConsumed: () -> Unit,
    onBack: () -> Unit,
  ) = ShortcutsScreen(
    mobilePlatform = mobilePlatform,
    paddingValues = paddingValues,
    graph = graph,
    stationsRepository = stationsRepository,
    favoriteIds = favoriteIds,
    searchRadiusMeters = searchRadiusMeters,
    initialAction = initialAction,
    onInitialActionConsumed = onInitialActionConsumed,
    onBack = onBack,
  )

  @Composable
  fun MapScreenContent(
    mobilePlatform: MobileUiPlatform,
    stations: List<Station>,
    favoriteIds: Set<String>,
    loading: Boolean,
    errorMessage: String?,
    nearestSelection: com.gcaguilar.biciradar.core.NearbyStationSelection,
    searchQuery: String,
    searchRadiusMeters: Int,
    userLocation: GeoPoint?,
    isMapReady: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onStationSelected: (Station) -> Unit,
    onRetry: () -> Unit,
    onFavoriteToggle: (Station) -> Unit,
    onQuickRoute: (Station) -> Unit,
    environmentalRepository: EnvironmentalRepository,
    dataFreshness: DataFreshness,
    lastUpdatedEpoch: Long?,
    onRefreshStations: () -> Unit,
    paddingValues: PaddingValues,
  ) = MapScreen(
    mobilePlatform = mobilePlatform,
    stations = stations,
    favoriteIds = favoriteIds,
    loading = loading,
    errorMessage = errorMessage,
    dataFreshness = dataFreshness,
    lastUpdatedEpoch = lastUpdatedEpoch,
    onRefreshStations = onRefreshStations,
    nearestSelection = nearestSelection,
    searchQuery = searchQuery,
    searchRadiusMeters = searchRadiusMeters,
    userLocation = userLocation,
    isMapReady = isMapReady,
    onSearchQueryChange = onSearchQueryChange,
    onStationSelected = onStationSelected,
    onRetry = onRetry,
    onFavoriteToggle = onFavoriteToggle,
    onQuickRoute = onQuickRoute,
    environmentalRepository = environmentalRepository,
    paddingValues = paddingValues,
  )

  @Composable
  fun StationDetailScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.StationDetailViewModel,
    mobilePlatform: MobileUiPlatform,
    station: Station,
    userLocation: GeoPoint?,
    isMapReady: Boolean,
    dataFreshness: DataFreshness,
    lastUpdatedEpoch: Long?,
    stationsLoading: Boolean,
    onRefreshStations: () -> Unit,
    onBack: () -> Unit,
  ) {
    val uiState by viewModel.uiState.collectAsState()
    StationDetailScreen(
      mobilePlatform = mobilePlatform,
      station = station,
      isFavorite = uiState.isFavorite,
      isHomeStation = uiState.isHomeStation,
      isWorkStation = uiState.isWorkStation,
      userLocation = userLocation,
      isMapReady = isMapReady,
      supportsUsagePatterns = uiState.supportsUsagePatterns,
      dataFreshness = dataFreshness,
      lastUpdatedEpoch = lastUpdatedEpoch,
      stationsLoading = stationsLoading,
      onRefreshStations = onRefreshStations,
      onBack = onBack,
      onToggleFavorite = viewModel::onToggleFavorite,
      onToggleHome = viewModel::onToggleHome,
      onToggleWork = viewModel::onToggleWork,
      onRoute = { viewModel.onRoute(station) },
      savedPlaceAlertsCityId = uiState.savedPlaceAlertsCityId,
      savedPlaceAlertRules = uiState.savedPlaceAlertRules,
      onUpsertSavedPlaceAlert = viewModel::onUpsertSavedPlaceAlert,
      onRemoveSavedPlaceAlertForTarget = viewModel::onRemoveSavedPlaceAlertForTarget,
      patterns = uiState.patterns,
      patternsLoading = uiState.patternsLoading,
      patternsError = uiState.patternsError,
    )
  }
}
