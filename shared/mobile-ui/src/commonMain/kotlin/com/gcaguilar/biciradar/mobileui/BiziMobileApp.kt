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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.core.SEARCH_RADIUS_OPTIONS_METERS
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.SharedString
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.filterStationsByQuery
import com.gcaguilar.biciradar.core.findStationMatchingQuery
import com.gcaguilar.biciradar.core.findStationMatchingQueryOrPinnedAlias
import com.gcaguilar.biciradar.core.isGoogleMapsReady
import com.gcaguilar.biciradar.core.selectNearbyStation
import com.gcaguilar.biciradar.core.sharedString
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
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
import com.gcaguilar.biciradar.core.geo.GeoResult
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.MONITORING_DURATION_OPTIONS_SECONDS
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.StationHourlyPattern
import com.gcaguilar.biciradar.core.TripDestination
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.core.TripRepository
import com.gcaguilar.biciradar.core.TripState
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gcaguilar.biciradar.mobileui.navigation.BiziNavHost
import com.gcaguilar.biciradar.mobileui.navigation.Screen

private val BiziRed = Color(0xFFD7191F)
private val BiziLight = Color(0xFFF8F6F6)
private val BiziGrouped = Color(0xFFF2F2F7)
private val BiziInk = Color(0xFF211111)
private val BiziMuted = Color(0xFF64748B)
private val BiziPanel = Color(0xFFF1F5F9)
private val BiziGreen = Color(0xFF167C3C)
private val BiziBlue = Color(0xFF2563EB)
private val BiziOrange = Color(0xFFF28000)

// --- Dark-mode palette raw tokens ---
private val BiziDarkBackground = Color(0xFF121212)
private val BiziDarkGrouped = Color(0xFF1C1C1E)
private val BiziDarkSurface = Color(0xFF1E1E1E)
private val BiziDarkInk = Color(0xFFF1EDED)
private val BiziDarkMuted = Color(0xFF94A3B8)
private val BiziDarkPanel = Color(0xFF2A2A2C)
private val BiziDarkGreen = Color(0xFF22C55E)
private val BiziDarkBlue = Color(0xFF60A5FA)
private val BiziDarkRed = Color(0xFFEF4444)

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
  red = BiziRed,
  blue = BiziBlue,
  green = BiziGreen,
  orange = BiziOrange,
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
  red = BiziDarkRed,
  blue = BiziDarkBlue,
  green = BiziDarkGreen,
  orange = BiziOrange,
  onAccent = Color.White,
  navBar = BiziDarkSurface,
  navBarIos = BiziDarkSurface.copy(alpha = 0.96f),
  fieldSurfaceIos = BiziDarkSurface,
  fieldSurfaceAndroid = BiziDarkPanel,
  dismissAlphaBase = 0.16f,
)

internal val LocalBiziColors = staticCompositionLocalOf { LightBiziColors }

private enum class MobileTab(val labelKey: SharedString) {
  Cerca(SharedString.TAB_NEARBY),
  Mapa(SharedString.TAB_MAP),
  Favoritos(SharedString.TAB_FAVORITES),
  Viaje(SharedString.TAB_TRIP),
  Perfil(SharedString.TAB_SETTINGS),
}

private val MobileTabs = listOf(
  MobileTab.Cerca,
  MobileTab.Mapa,
  MobileTab.Favoritos,
  MobileTab.Viaje,
  MobileTab.Perfil,
)

private enum class MapFilter(val labelKey: SharedString) {
  BIKES_AND_SLOTS(SharedString.MAP_FILTER_BIKES_AND_SLOTS),
  ONLY_BIKES(SharedString.MAP_FILTER_ONLY_BIKES),
  ONLY_SLOTS(SharedString.MAP_FILTER_ONLY_SLOTS),
}

private const val CURRENT_CHANGELOG_VERSION = 1

private data class ChangelogEntry(val titleKey: SharedString, val descriptionKey: SharedString)

private val CHANGELOG_ENTRIES = listOf(
  ChangelogEntry(SharedString.CHANGELOG_DARK_THEME_TITLE, SharedString.CHANGELOG_DARK_THEME_DESCRIPTION),
  ChangelogEntry(SharedString.CHANGELOG_DISMISSIBLE_CARD_TITLE, SharedString.CHANGELOG_DISMISSIBLE_CARD_DESCRIPTION),
  ChangelogEntry(SharedString.CHANGELOG_MAP_FILTERS_TITLE, SharedString.CHANGELOG_MAP_FILTERS_DESCRIPTION),
  ChangelogEntry(SharedString.CHANGELOG_REFRESH_INDICATOR_TITLE, SharedString.CHANGELOG_REFRESH_INDICATOR_DESCRIPTION),
  ChangelogEntry(SharedString.PATTERN_USAGE, SharedString.PATTERN_DESCRIPTION),
)

sealed interface MobileLaunchRequest {
  data object Favorites : MobileLaunchRequest
  data object NearestStation : MobileLaunchRequest
  data object NearestStationWithBikes : MobileLaunchRequest
  data object NearestStationWithSlots : MobileLaunchRequest
  data object OpenAssistant : MobileLaunchRequest
  data object StationStatus : MobileLaunchRequest
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
  val isMapReady = mapSupportStatus.isGoogleMapsReady() &&
    (mobilePlatform != MobileUiPlatform.IOS || preferredMapApp == PreferredMapApp.GoogleMaps)
  var settingsBootstrapped by remember(graph) { mutableStateOf(false) }
  var favoritesBootstrapped by remember(graph) { mutableStateOf(false) }
  var initialLoadAttemptFinished by remember(graph) { mutableStateOf(false) }
  var minimumSplashElapsed by remember(graph) { mutableStateOf(false) }
  val lastSeenChangelog by settingsRepository.lastSeenChangelogVersion.collectAsState()
  var showChangelog by remember(graph) { mutableStateOf(false) }

  LaunchedEffect(graph) {
    platformBindings.onGraphCreated(graph)
    onTripRepositoryReady?.invoke(graph.tripRepository)
  }

  LaunchedEffect(graph) {
    settingsBootstrapped = false
    runCatching { settingsRepository.bootstrap() }
    settingsBootstrapped = true
  }

  LaunchedEffect(settingsBootstrapped, lastSeenChangelog) {
    if (settingsBootstrapped && lastSeenChangelog in 1 until CURRENT_CHANGELOG_VERSION) {
      showChangelog = true
    } else if (settingsBootstrapped && lastSeenChangelog == 0) {
      // First install — silently mark as seen so the dialog only appears on future updates.
      settingsRepository.setLastSeenChangelogVersion(CURRENT_CHANGELOG_VERSION)
    }
  }

  LaunchedEffect(graph) {
    favoritesBootstrapped = false
    runCatching { favoritesRepository.bootstrap() }
    favoritesBootstrapped = true
  }

  LaunchedEffect(graph) {
    minimumSplashElapsed = false
    kotlinx.coroutines.delay(700)
    minimumSplashElapsed = true
  }

  LaunchedEffect(graph, refreshKey) {
    stationsRepository.loadIfNeeded()
    initialLoadAttemptFinished = true
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

  LaunchedEffect(startupLaunchReady, appState.pendingLaunchRequest, stationsState.stations, searchRadiusMeters) {
    if (!startupLaunchReady) return@LaunchedEffect
    when (val request = appState.pendingLaunchRequest ?: return@LaunchedEffect) {
      MobileLaunchRequest.Favorites -> {
        navController.navigate(Screen.Favorites) { launchSingleTop = true }
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
    Surface(
      modifier = modifier.fillMaxSize(),
      color = pageBackgroundColor(mobilePlatform),
    ) {
      if (showChangelog) {
        val onChangelogDismiss = remember {
          {
            showChangelog = false
            scope.launch { settingsRepository.setLastSeenChangelogVersion(CURRENT_CHANGELOG_VERSION) }
            Unit
          }
        }
        ChangelogDialog(onDismiss = onChangelogDismiss)
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
          val tripViewModelFactory = remember(graph, searchRadiusMeters) {
            com.gcaguilar.biciradar.mobileui.viewmodel.TripViewModelFactory(
              tripRepository = graph.tripRepository,
              geoSearchUseCase = graph.geoSearchUseCase,
              reverseGeocodeUseCase = graph.reverseGeocodeUseCase,
              searchRadiusMeters = searchRadiusMeters,
            )
          }
          val nearbyViewModelFactory = remember(graph, searchRadiusMeters) {
            com.gcaguilar.biciradar.mobileui.viewmodel.NearbyViewModelFactory(
              stationsRepository = graph.stationsRepository,
              favoritesRepository = graph.favoritesRepository,
              routeLauncher = graph.routeLauncher,
              searchRadiusMeters = searchRadiusMeters,
            )
          }
          val favoritesViewModelFactory = remember(graph) {
            com.gcaguilar.biciradar.mobileui.viewmodel.FavoritesViewModelFactory(
              favoritesRepository = graph.favoritesRepository,
              stationsRepository = graph.stationsRepository,
              routeLauncher = graph.routeLauncher,
            )
          }
          val profileViewModelFactory = remember(graph, searchRadiusMeters) {
            com.gcaguilar.biciradar.mobileui.viewmodel.ProfileViewModelFactory(
              settingsRepository = graph.settingsRepository,
              stationsRepository = graph.stationsRepository,
              searchRadiusMeters = searchRadiusMeters,
            )
          }
          Scaffold(
            containerColor = pageBackgroundColor(mobilePlatform),
            bottomBar = {
              MobileBottomNavigationBar(
                mobilePlatform = mobilePlatform,
                navController = navController,
              )
            },
          ) { innerPadding ->
            BiziNavHost(
              navController = navController,
              mobilePlatform = mobilePlatform,
              tripViewModelFactory = tripViewModelFactory,
              nearbyViewModelFactory = nearbyViewModelFactory,
              favoritesViewModelFactory = favoritesViewModelFactory,
              profileViewModelFactory = profileViewModelFactory,
              stations = filteredStations,
              favoriteIds = favoriteIds,
              loading = stationsState.isLoading,
              errorMessage = stationsState.errorMessage,
              nearestSelection = nearestSelection,
              userLocation = stationsState.userLocation,
              searchQuery = appState.searchQuery,
              searchRadiusMeters = searchRadiusMeters,
              isMapReady = isMapReady,
              onSearchQueryChange = remember(appState) { { appState.searchQuery = it } },
              onRetry = remember(scope, stationsRepository) { { scope.launch { stationsRepository.loadIfNeeded() } } },
              onFavoriteToggle = remember(scope, favoritesRepository) { { station -> scope.launch { favoritesRepository.toggle(station.id) } } },
              onQuickRoute = remember(graph) { { station -> graph.routeLauncher.launch(station) } },
              onOpenAssistant = remember(navController) { { navController.navigate(Screen.Shortcuts) { launchSingleTop = true } } },
              localNotifier = platformBindings.localNotifier,
              routeLauncher = graph.routeLauncher,
              graph = graph,
              stationsRepository = stationsRepository,
              initialAssistantAction = appState.pendingAssistantAction,
              onInitialActionConsumed = remember(appState) { { appState.pendingAssistantAction = null } },
              paddingValues = innerPadding,
            )
          }
        }
      }
    }
  }
}

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
  val colors = if (isDark) DarkBiziColors else LightBiziColors
  CompositionLocalProvider(LocalBiziColors provides colors) {
    MaterialTheme(
      colorScheme = MaterialTheme.colorScheme.copy(
        primary = colors.red,
        background = if (mobilePlatform == MobileUiPlatform.IOS) colors.groupedBackground else colors.background,
        surface = colors.surface,
        onSurface = colors.ink,
        onBackground = colors.ink,
        surfaceVariant = if (mobilePlatform == MobileUiPlatform.IOS) colors.panel else colors.background,
      ),
      content = content,
    )
  }
}

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
          text = sharedString(SharedString.APP_NAME),
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = c.red,
        )
        Text(
          text = sharedString(SharedString.SPLASH_LOADING_STATIONS),
          style = MaterialTheme.typography.bodyMedium,
          color = c.muted,
        )
      }
      Text(
        text = if (mobilePlatform == MobileUiPlatform.IOS) {
          sharedString(SharedString.SPLASH_PREPARING_IPHONE)
        } else {
          sharedString(SharedString.SPLASH_PREPARING_ANDROID)
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
        onClick = { navController.navigate(screen) { launchSingleTop = true; restoreState = true; popUpTo(Screen.Nearby) { saveState = true } } },
        icon = {
          Icon(
            imageVector = tab.icon(),
            contentDescription = sharedString(tab.labelKey),
          )
        },
        label = { Text(sharedString(tab.labelKey)) },
      )
    }
  }
}

@Composable
private fun MapScreen(
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
  paddingValues: PaddingValues,
) {
  val nearestStation = nearestSelection.highlightedStation
  var selectedMapStationId by rememberSaveable { mutableStateOf<String?>(null) }
  var hasExplicitMapSelection by rememberSaveable { mutableStateOf(false) }
  var isCardDismissed by rememberSaveable { mutableStateOf(false) }
  var activeFilters by remember { mutableStateOf(emptySet<MapFilter>()) }
  var recenterRequestToken by rememberSaveable { mutableStateOf(0) }

  val mapStations = remember(stations, activeFilters) {
    if (activeFilters.isEmpty()) {
      stations
    } else {
      stations.filter { station ->
        activeFilters.any { filter ->
          when (filter) {
            MapFilter.BIKES_AND_SLOTS -> station.bikesAvailable > 0 && station.slotsFree > 0
            MapFilter.ONLY_BIKES -> station.bikesAvailable > 0 && station.slotsFree == 0
            MapFilter.ONLY_SLOTS -> station.bikesAvailable == 0 && station.slotsFree > 0
          }
        }
      }
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
        label = sharedString(SharedString.MAP_SEARCH_STATION_OR_ADDRESS),
      )
      MapFilterChipRow(
        activeFilters = activeFilters,
        onToggleFilter = { filter ->
          activeFilters = if (filter in activeFilters) emptySet() else setOf(filter)
        },
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
          title = sharedString(SharedString.MAP_UPDATE_FAILED),
          description = errorMessage,
          primaryAction = sharedString(SharedString.RETRY),
          onPrimaryAction = onRetry,
        )
      } else {
        EmptyStateCard(
          title = if (searchQuery.isBlank()) {
            sharedString(SharedString.MAP_NO_STATIONS_ON_SCREEN)
          } else {
            sharedString(SharedString.MAP_NO_SEARCH_RESULTS)
          },
          description = if (searchQuery.isBlank()) {
            sharedString(SharedString.MAP_LOCATION_FALLBACK_DESCRIPTION)
          } else {
            sharedString(SharedString.MAP_TRY_ANOTHER_QUERY)
          },
          primaryAction = sharedString(SharedString.LOAD_STATIONS),
          onPrimaryAction = onRetry,
        )
      }
    }

    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 16.dp),
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

@Composable
private fun NearbyScreen(
  mobilePlatform: MobileUiPlatform,
  stations: List<Station>,
  favoriteIds: Set<String>,
  loading: Boolean,
  errorMessage: String?,
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

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(pageBackgroundColor(mobilePlatform)),
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
              text = sharedString(SharedString.TAB_NEARBY),
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = sharedString(SharedString.NEARBY_QUICK_ACTIONS_DESCRIPTION),
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
              text = sharedString(SharedString.NEARBY_NEAR_YOU),
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = LocalBiziColors.current.red,
            )
            Text(
              text = sharedString(SharedString.NEARBY_STATIONS_SORTED_DESCRIPTION),
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
          title = sharedString(SharedString.NEARBY_NEAREST_WITH_BIKES),
          emptyTitle = sharedString(SharedString.NEARBY_NO_BIKES_NEARBY),
          selection = nearestWithBikesSelection,
          icon = Icons.AutoMirrored.Filled.DirectionsBike,
          tint = LocalBiziColors.current.red,
          mobilePlatform = mobilePlatform,
          onRoute = onQuickRoute,
        )
        QuickRouteActionCard(
          modifier = Modifier.weight(1f),
          title = sharedString(SharedString.NEARBY_NEAREST_WITH_SLOTS),
          emptyTitle = sharedString(SharedString.NEARBY_NO_SLOTS_NEARBY),
          selection = nearestWithSlotsSelection,
          icon = Icons.Filled.LocalParking,
          tint = LocalBiziColors.current.blue,
          mobilePlatform = mobilePlatform,
          onRoute = onQuickRoute,
        )
      }
    }
    // Scrollable list below
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = if (loading) sharedString(SharedString.NEARBY_UPDATING_STATIONS) else sharedString(SharedString.NEARBY_STATIONS),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = if (nearestSelection.usesFallback) {
              sharedString(SharedString.NEARBY_RADIUS_FALLBACK_HINT)
            } else {
              sharedString(SharedString.NEARBY_CARD_ACTIONS_HINT)
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
                Text(sharedString(SharedString.RETRY))
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
            title = sharedString(SharedString.MAP_NO_STATIONS_ON_SCREEN),
            description = sharedString(SharedString.MAP_LOCATION_FALLBACK_DESCRIPTION),
            primaryAction = sharedString(SharedString.LOAD_STATIONS),
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
      Icon(Icons.Filled.Sync, contentDescription = sharedString(SharedString.REFRESH_STATIONS))
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
        label = sharedString(filter.labelKey),
        selected = filter in activeFilters,
        onClick = { onToggleFilter(filter) },
      )
    }
    MapLegendChip(
      label = sharedString(SharedString.MAP_LEGEND_OUT_OF_SERVICE),
      color = LocalBiziColors.current.red,
    )
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
    MapFilter.ONLY_BIKES,
    MapFilter.ONLY_SLOTS -> c.orange
  }
  val backgroundColor by animateColorAsState(
    targetValue = if (selected) accent.copy(alpha = 0.12f) else c.surface,
    animationSpec = tween(180),
  )
  val contentColor by animateColorAsState(
    targetValue = if (selected) accent else c.ink,
    animationSpec = tween(180),
  )
  val borderColor by animateColorAsState(
    targetValue = if (selected) accent.copy(alpha = 0.28f) else c.panel,
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
      contentDescription = sharedString(SharedString.MAP_MY_LOCATION),
      tint = if (enabled) c.green else c.muted,
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
            sharedString(SharedString.MAP_NO_STATIONS_WITHIN_RADIUS, searchRadiusMeters)
          } else if (isShowingNearestSelection) {
            sharedString(SharedString.MAP_NEAREST_STATION_LABEL)
          } else {
            sharedString(SharedString.MAP_SELECTED_STATION_LABEL)
          },
          color = if (mobilePlatform == MobileUiPlatform.IOS) c.red else overlayBody,
        )
        Icon(
          imageVector = Icons.Filled.Close,
          contentDescription = sharedString(SharedString.CLOSE),
          tint = if (mobilePlatform == MobileUiPlatform.IOS) c.muted else overlayBody,
          modifier = Modifier.size(20.dp).clickable(onClick = onDismiss),
        )
      }
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
      }
      Text(
        text = if (isFallbackSelection) {
          sharedString(
            SharedString.MAP_NEAREST_FALLBACK_SUMMARY,
            station.distanceMeters,
            station.bikesAvailable,
            station.slotsFree,
          )
        } else {
          sharedString(SharedString.MAP_STATION_DISTANCE_SUMMARY, station.distanceMeters, station.bikesAvailable, station.slotsFree)
        },
        color = overlayBody,
      )
      Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        RoutePill(
          label = sharedString(SharedString.ROUTE),
          onDarkBackground = mobilePlatform != MobileUiPlatform.IOS,
          onClick = { onQuickRoute(station) },
        )
        if (mobilePlatform == MobileUiPlatform.IOS) {
          FavoritePill(
            active = isFavorite,
            onClick = onFavoriteToggle,
            label = if (isFavorite) sharedString(SharedString.SAVED) else sharedString(SharedString.SAVE),
          )
        } else {
          OutlineActionPill(
            label = if (isFavorite) sharedString(SharedString.SAVED) else sharedString(SharedString.SAVE),
            tint = c.onAccent,
            borderTint = c.onAccent.copy(alpha = 0.32f),
            onClick = onFavoriteToggle,
          )
        }
        OutlineActionPill(
          label = sharedString(SharedString.DETAILS),
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
  paddingValues: PaddingValues,
) {
  val assignmentCandidate = remember(allStations, searchQuery) {
    searchQuery
      .takeIf { it.isNotBlank() }
      ?.let { query -> findStationMatchingQuery(allStations, query) }
  }
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(pageBackgroundColor(mobilePlatform)),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      if (mobilePlatform == MobileUiPlatform.IOS) {
        MobilePageHeader(
          title = sharedString(SharedString.TAB_FAVORITES),
          subtitle = sharedString(SharedString.FAVORITES_SUBTITLE),
          onOpenAssistant = onOpenAssistant,
        )
      } else {
        Text(
          text = sharedString(SharedString.MY_STATIONS),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
        )
      }
    }
    item {
      StationSearchField(
        mobilePlatform = mobilePlatform,
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        label = sharedString(SharedString.FAVORITES_SEARCH_STATION),
      )
    }
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = sharedString(SharedString.HOME_AND_WORK),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = sharedString(SharedString.HOME_AND_WORK_DESCRIPTION),
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      }
    }
    item {
      SavedPlaceCard(
        mobilePlatform = mobilePlatform,
        title = sharedString(SharedString.HOME),
        station = homeStation,
        assignmentCandidate = assignmentCandidate,
        onAssignCandidate = onAssignHomeStation,
        onClear = onClearHomeStation,
        onOpenStationDetails = onStationSelected,
        onQuickRoute = onQuickRoute,
      )
    }
    item {
      SavedPlaceCard(
        mobilePlatform = mobilePlatform,
        title = sharedString(SharedString.WORK),
        station = workStation,
        assignmentCandidate = assignmentCandidate,
        onAssignCandidate = onAssignWorkStation,
        onClear = onClearWorkStation,
        onOpenStationDetails = onStationSelected,
        onQuickRoute = onQuickRoute,
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
          title = sharedString(SharedString.FAVORITES_EMPTY_TITLE),
          description = sharedString(SharedString.FAVORITES_EMPTY_DESCRIPTION),
        )
      }
    }
    if (stations.isNotEmpty()) {
      items(stations, key = { it.id }) { station ->
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
        )
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
  onOpenShortcuts: () -> Unit,
  onSearchRadiusSelected: (Int) -> Unit,
  onPreferredMapAppSelected: (PreferredMapApp) -> Unit,
  onThemePreferenceSelected: (ThemePreference) -> Unit,
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(pageBackgroundColor(mobilePlatform)),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = sharedString(SharedString.TAB_SETTINGS),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = sharedString(SharedString.PROFILE_SUBTITLE),
          style = MaterialTheme.typography.bodyMedium,
          color = LocalBiziColors.current.muted,
        )
      }
    }
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
      ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text(sharedString(SharedString.NEARBY_STATION_RADIUS), fontWeight = FontWeight.SemiBold)
          Text(
            sharedString(SharedString.NEARBY_STATION_RADIUS_DESCRIPTION),
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
          SEARCH_RADIUS_OPTIONS_METERS.chunked(2).forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              rowOptions.forEach { radiusMeters ->
                RadiusSelectionButton(
                  modifier = Modifier.weight(1f),
                  selected = radiusMeters == searchRadiusMeters,
                  label = if (radiusMeters == searchRadiusMeters) sharedString(SharedString.RADIUS_ACTIVE, radiusMeters) else sharedString(SharedString.METERS, radiusMeters),
                  onClick = { onSearchRadiusSelected(radiusMeters) },
                )
              }
              if (rowOptions.size == 1) {
                Spacer(Modifier.weight(1f))
              }
            }
          }
        }
      }
    }
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
      ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(sharedString(SharedString.SHORTCUTS), fontWeight = FontWeight.SemiBold)
          Text(
            sharedString(SharedString.SHORTCUTS_REVIEW_COMMANDS, mobilePlatform.assistantDisplayName()),
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
          OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenShortcuts,
            border = BorderStroke(1.dp, LocalBiziColors.current.red.copy(alpha = 0.24f)),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = LocalBiziColors.current.red.copy(alpha = 0.04f),
            ),
          ) {
            Icon(Icons.Filled.KeyboardVoice, contentDescription = null, tint = LocalBiziColors.current.red)
            Spacer(Modifier.width(8.dp))
            Text(
              sharedString(SharedString.OPEN_SHORTCUTS_GUIDE),
              color = LocalBiziColors.current.red,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
      }
    }
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
      ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text(sharedString(SharedString.APPEARANCE), fontWeight = FontWeight.SemiBold)
          Text(
            sharedString(SharedString.APPEARANCE_DESCRIPTION),
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RadiusSelectionButton(
              modifier = Modifier.weight(1f),
              selected = themePreference == ThemePreference.System,
              label = sharedString(SharedString.SYSTEM),
              onClick = { onThemePreferenceSelected(ThemePreference.System) },
            )
            RadiusSelectionButton(
              modifier = Modifier.weight(1f),
              selected = themePreference == ThemePreference.Light,
              label = sharedString(SharedString.LIGHT),
              onClick = { onThemePreferenceSelected(ThemePreference.Light) },
            )
            RadiusSelectionButton(
              modifier = Modifier.weight(1f),
              selected = themePreference == ThemePreference.Dark,
              label = sharedString(SharedString.DARK),
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
            Text(sharedString(SharedString.IPHONE_ROUTE_APP), fontWeight = FontWeight.SemiBold)
            Text(
              sharedString(SharedString.IPHONE_ROUTE_APP_DESCRIPTION),
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
               sharedString(SharedString.IPHONE_ROUTE_APP_FALLBACK),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
          }
        }
      }
    }
    item {
      val uriHandler = LocalUriHandler.current
      Card(
        colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
      ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(sharedString(SharedString.FEEDBACK_AND_SUGGESTIONS), fontWeight = FontWeight.SemiBold)
          Text(
            sharedString(SharedString.FEEDBACK_DESCRIPTION),
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
          TextButton(
            onClick = { uriHandler.openUri("https://forms.gle/j6hMxPQypzhqXp5v5") },
            contentPadding = PaddingValues(0.dp),
          ) {
            Text(sharedString(SharedString.OPEN_FEEDBACK_FORM), style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    }
    item {
      val uriHandler = LocalUriHandler.current
      Card(
        colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
      ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(sharedString(SharedString.PRIVACY_AND_DATA), fontWeight = FontWeight.SemiBold)
          Text(
            sharedString(SharedString.PRIVACY_DESCRIPTION),
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
          TextButton(
            onClick = { uriHandler.openUri("https://gcaguilar.github.io/biciradar-privacy-policy/") },
            contentPadding = PaddingValues(0.dp),
          ) {
            Text(sharedString(SharedString.OPEN_PRIVACY_POLICY), style = MaterialTheme.typography.bodySmall)
          }
        }
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
  var latestAnswer by rememberSaveable { mutableStateOf<String?>(null) }
  val shortcutGuides = remember(mobilePlatform) { shortcutGuidesFor(mobilePlatform) }

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
            Text(sharedString(SharedString.SHORTCUTS))
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = sharedString(SharedString.BACK))
          }
        },
      )
    },
    containerColor = pageBackgroundColor(mobilePlatform),
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(pageBackgroundColor(mobilePlatform)),
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
              text = sharedString(SharedString.SHORTCUTS),
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = sharedString(SharedString.SHORTCUTS_IOS_SUBTITLE),
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
            Text(sharedString(SharedString.HOW_TO_INVOKE), fontWeight = FontWeight.SemiBold)
            Text(
              if (mobilePlatform == MobileUiPlatform.IOS) {
                sharedString(SharedString.SHORTCUTS_AVAILABLE_ON_IOS)
              } else {
                sharedString(SharedString.SHORTCUTS_AVAILABLE_WITH_ASSISTANT, mobilePlatform.assistantDisplayName())
              },
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            Text(
              if (mobilePlatform == MobileUiPlatform.IOS) {
                sharedString(SharedString.SHORTCUTS_IOS_INVOCATION_HINT)
              } else {
                sharedString(SharedString.SHORTCUTS_ANDROID_INVOCATION_HINT)
              },
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            Text(
              sharedString(SharedString.SHORTCUTS_CURRENT_RADIUS, searchRadiusMeters),
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
              Text(sharedString(SharedString.LATEST_ANSWER), fontWeight = FontWeight.SemiBold)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationDetailScreen(
  mobilePlatform: MobileUiPlatform,
  station: Station,
  datosBiziApi: DatosBiziApi,
  isFavorite: Boolean,
  isHomeStation: Boolean,
  isWorkStation: Boolean,
  userLocation: GeoPoint?,
  isMapReady: Boolean,
  onBack: () -> Unit,
  onToggleFavorite: () -> Unit,
  onToggleHome: () -> Unit,
  onToggleWork: () -> Unit,
  onRoute: () -> Unit,
) {
  PlatformBackHandler(enabled = true, onBack = onBack)
  var patterns by remember { mutableStateOf<List<StationHourlyPattern>>(emptyList()) }
  var patternsLoading by remember { mutableStateOf(true) }
  var patternsError by remember { mutableStateOf(false) }
  var showWeekend by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(station.id) {
    patternsLoading = true
    patternsError = false
    try {
      patterns = datosBiziApi.fetchPatterns(station.id)
    } catch (_: Exception) {
      patternsError = true
    }
    patternsLoading = false
  }
  Scaffold(
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
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = sharedString(SharedString.BACK))
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
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(pageBackgroundColor(mobilePlatform)),
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
              label = if (isFavorite) sharedString(SharedString.SAVED) else sharedString(SharedString.SAVE),
            )
          }
          Text(station.address, style = MaterialTheme.typography.bodyMedium, color = LocalBiziColors.current.muted)
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StationMetricPill(
              modifier = Modifier.weight(1f),
              label = sharedString(SharedString.DISTANCE),
              value = "${station.distanceMeters} m",
              tint = LocalBiziColors.current.blue,
            )
            StationMetricPill(
              modifier = Modifier.weight(1f),
              label = sharedString(SharedString.SOURCE),
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
          Text(sharedString(SharedString.SAVE_THIS_STATION), fontWeight = FontWeight.SemiBold)
          Text(
            sharedString(SharedString.SAVE_THIS_STATION_DESCRIPTION),
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FavoritePill(
              active = isFavorite,
              onClick = onToggleFavorite,
              label = if (isFavorite) sharedString(SharedString.FAVORITE) else sharedString(SharedString.SAVE),
            )
            SavedPlacePill(
              active = isHomeStation,
              label = sharedString(SharedString.HOME),
              onClick = onToggleHome,
            )
            SavedPlacePill(
              active = isWorkStation,
              label = sharedString(SharedString.WORK),
              onClick = onToggleWork,
            )
          }
          Text(
            when {
              isHomeStation && isWorkStation -> sharedString(SharedString.STATION_MARKED_HOME_AND_WORK)
              isHomeStation -> sharedString(SharedString.STATION_MARKED_HOME)
              isWorkStation -> sharedString(SharedString.STATION_MARKED_WORK)
              else -> sharedString(SharedString.TAP_HOME_OR_WORK_TO_ASSIGN)
            },
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
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
          label = sharedString(SharedString.BIKES),
          value = station.bikesAvailable.toString(),
          icon = Icons.AutoMirrored.Filled.DirectionsBike,
          tint = LocalBiziColors.current.red,
          mobilePlatform = mobilePlatform,
        )
        AvailabilityCard(
          modifier = Modifier.weight(1f),
          label = sharedString(SharedString.SLOTS),
          value = station.slotsFree.toString(),
          icon = Icons.Filled.LocalParking,
          tint = LocalBiziColors.current.blue,
          mobilePlatform = mobilePlatform,
        )
      }
    }
    item {
      StationPatternCard(
        patterns = patterns,
        isLoading = patternsLoading,
        isError = patternsError,
        showWeekend = showWeekend,
        onToggleDayType = { showWeekend = !showWeekend },
      )
    }
    item {
      Button(onClick = onRoute, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Filled.Directions, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(sharedString(SharedString.OPEN_ROUTE))
      }
    }
    item {
      OutlinedButton(onClick = onToggleFavorite, modifier = Modifier.fillMaxWidth()) {
        Icon(
          if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
          contentDescription = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(if (isFavorite) sharedString(SharedString.REMOVE_FROM_FAVORITES) else sharedString(SharedString.SAVE_TO_FAVORITES))
      }
    }
  }
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
        Text(sharedString(SharedString.PATTERN_USAGE), fontWeight = FontWeight.SemiBold)
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
                sharedString(SharedString.PATTERN_NO_DATA),
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
                sharedString(SharedString.PATTERN_NO_DAY_TYPE_DATA),
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
              )
            }
          } else {
            Text(
              sharedString(SharedString.PATTERN_DESCRIPTION),
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
                  label = sharedString(SharedString.PATTERN_MORE_BIKES),
                  value = "${bestBikesHour.hour}:00h (~${bestBikesHour.bikesAvg.roundToInt()})",
                  tint = colors.red,
                )
                PatternHintPill(
                  modifier = Modifier.weight(1f),
                  label = sharedString(SharedString.PATTERN_MORE_SLOTS),
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
private fun ChangelogDialog(onDismiss: () -> Unit) {
  val colors = LocalBiziColors.current
  androidx.compose.material3.AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    title = {
      Text(sharedString(SharedString.CHANGELOG_WHATS_NEW), fontWeight = FontWeight.Bold)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        items(CHANGELOG_ENTRIES.size) { index ->
          val entry = CHANGELOG_ENTRIES[index]
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              sharedString(entry.titleKey),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              sharedString(entry.descriptionKey),
              style = MaterialTheme.typography.bodySmall,
              color = colors.muted,
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(sharedString(SharedString.GOT_IT))
      }
    },
  )
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
  paddingValues: PaddingValues,
) {
  val c = LocalBiziColors.current
  val scope = rememberCoroutineScope()
  val uiState by viewModel.uiState.collectAsState()
  val tripState by viewModel.tripState.collectAsState()

  // ---------- layout ----------
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(pageBackgroundColor(mobilePlatform)),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
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
                sharedString(SharedString.TRIP_STATION_FULL),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = c.red,
              )
            }
            Text(
              sharedString(SharedString.TRIP_STATION_NO_LONGER_HAS_SLOTS, alert.fullStation.name),
              style = MaterialTheme.typography.bodyMedium,
            )
            val altStation = alert.alternativeStation
            if (altStation != null) {
              val dist = alert.alternativeDistanceMeters
              val distText = if (dist != null) " (${dist} m)" else ""
              Text(
                sharedString(SharedString.TRIP_SUGGESTED_ALTERNATIVE, altStation.name, distText, altStation.slotsFree),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
              )
            } else {
              Text(
                sharedString(SharedString.TRIP_NO_NEARBY_ALTERNATIVE),
                style = MaterialTheme.typography.bodySmall,
                color = c.muted,
              )
            }
            Button(
              onClick = { viewModel.onDismissAlert() },
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(sharedString(SharedString.GOT_IT))
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
              sharedString(SharedString.WHERE_ARE_YOU_GOING),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              sharedString(SharedString.TRIP_SUBTITLE),
              style = MaterialTheme.typography.bodySmall,
              color = c.muted,
            )
            OutlinedTextField(
              value = uiState.query,
              onValueChange = { viewModel.onQueryChange(it) },
              modifier = Modifier.fillMaxWidth(),
              label = { Text(sharedString(SharedString.DESTINATION)) },
              placeholder = { Text(sharedString(SharedString.DESTINATION_PLACEHOLDER)) },
              singleLine = true,
              leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null)
              },
              trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                  IconButton(onClick = { viewModel.onClearQuery() }) {
                    Icon(Icons.Filled.Close, contentDescription = sharedString(SharedString.CLEAR_FIELD))
                  }
                }
              },
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = c.red,
                cursorColor = c.red,
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
                  sharedString(SharedString.CANCEL_MAP)
                } else {
                  sharedString(SharedString.PICK_ON_MAP)
                },
                color = c.blue,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }
      }

      // Map picker overlay
      if (uiState.mapPickerActive) {
        item(key = "map-picker") {
          Card(
            colors = CardDefaults.cardColors(containerColor = c.surface),
            modifier = Modifier.height(300.dp),
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
                    sharedString(SharedString.TAP_MAP_TO_PICK_DESTINATION),
                    style = MaterialTheme.typography.labelMedium,
                    color = c.muted,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                  )
                }
              }
            }
          }
        }
      }

      // Autocomplete suggestions
      if (uiState.suggestions.isNotEmpty()) {
        item(key = "suggestions-header") {
          Text(
            sharedString(SharedString.SUGGESTIONS),
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
                    text = sharedString(SharedString.DESTINATION),
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
                sharedString(SharedString.DESTINATION),
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
              Text(sharedString(SharedString.CLEAR), style = MaterialTheme.typography.labelMedium)
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
                sharedString(SharedString.SEARCHING_NEARBY_STATION),
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
                sharedString(SharedString.WALK_ROUTE_TO, destination.name),
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
                  localNotifier.requestPermission()
                  viewModel.onStartMonitoring()
                }
              },
            )
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
          sharedString(SharedString.TRIP_SUGGESTED_STATION),
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
          label = sharedString(SharedString.FREE_SLOTS),
          value = station.slotsFree.toString(),
          tint = c.blue,
        )
        StationMetricPill(
          label = sharedString(SharedString.BIKES),
          value = station.bikesAvailable.toString(),
          tint = c.red,
        )
        if (distanceMeters != null) {
          StationMetricPill(
            label = sharedString(SharedString.DISTANCE),
            value = "${distanceMeters} m",
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
        sharedString(SharedString.MONITOR_THIS_STATION),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        sharedString(SharedString.MONITOR_THIS_STATION_DESCRIPTION),
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
        Text(sharedString(SharedString.START_MONITORING))
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
          sharedString(SharedString.MONITORING_ACTIVE),
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = c.blue,
        )
      }
      Text(
        sharedString(
          SharedString.REMAINING_TIME,
          "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
        ),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
      Text(
        sharedString(SharedString.MONITORING_ACTIVE_DESCRIPTION),
        style = MaterialTheme.typography.bodySmall,
        color = c.muted,
      )
      OutlinedButton(
        onClick = onStop,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(sharedString(SharedString.STOP_MONITORING))
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
              label = sharedString(SharedString.ROUTE),
              onClick = quickRoute,
            )
          }
          if (showFavoriteCta) {
            FavoritePill(
              active = isFavorite,
              onClick = onFavoriteToggle,
              label = if (isFavorite) sharedString(SharedString.SAVED) else sharedString(SharedString.SAVE),
            )
          } else {
            FavoritePill(
              active = true,
              onClick = {},
              label = sharedString(SharedString.FAVORITE),
            )
          }
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StationMetricPill(
          modifier = Modifier.weight(1f),
          label = sharedString(SharedString.BIKES),
          value = station.bikesAvailable.toString(),
          tint = LocalBiziColors.current.red,
        )
        StationMetricPill(
          modifier = Modifier.weight(1f),
          label = sharedString(SharedString.SLOTS),
          value = station.slotsFree.toString(),
          tint = LocalBiziColors.current.blue,
        )
        StationMetricPill(
          modifier = Modifier.weight(1f),
          label = sharedString(SharedString.DISTANCE),
          value = "${station.distanceMeters} m",
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
        extraActions = {
          if (canAssignHome) {
            SavedPlaceQuickAction(
              label = sharedString(SharedString.HOME),
              tint = LocalBiziColors.current.green,
              onClick = onAssignHome,
            )
          }
          if (canAssignWork) {
            SavedPlaceQuickAction(
              label = sharedString(SharedString.WORK),
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
        text = if (mobilePlatform == MobileUiPlatform.IOS) sharedString(SharedString.REMOVE_FAVORITE) else sharedString(SharedString.DELETE_FAVORITE),
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
            label = sharedString(SharedString.BIKES),
            value = station.bikesAvailable.toString(),
            tint = LocalBiziColors.current.red,
          )
          StationMetricPill(
            modifier = Modifier.weight(1f),
            label = sharedString(SharedString.SLOTS),
            value = station.slotsFree.toString(),
            tint = LocalBiziColors.current.blue,
          )
          StationMetricPill(
            modifier = Modifier.weight(1f),
            label = sharedString(SharedString.DISTANCE),
            value = "${station.distanceMeters} m",
            tint = LocalBiziColors.current.green,
          )
        }
      } else {
        Text(
          text = sharedString(SharedString.SAVED_PLACE_NOT_SET, title),
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
            label = sharedString(SharedString.ROUTE),
            onClick = { onQuickRoute(station) },
          )
          OutlineActionPill(
            label = sharedString(SharedString.DETAILS),
            tint = LocalBiziColors.current.red,
            borderTint = LocalBiziColors.current.red.copy(alpha = 0.16f),
            onClick = { onOpenStationDetails(station) },
          )
        }
        if (assignableCandidate != null) {
          OutlineActionPill(
            label = sharedString(SharedString.ASSIGN_SEARCH_RESULT),
            tint = LocalBiziColors.current.blue,
            borderTint = LocalBiziColors.current.blue.copy(alpha = 0.16f),
            onClick = { onAssignCandidate(assignableCandidate) },
          )
        }
        if (station != null) {
          OutlineActionPill(
            label = sharedString(SharedString.REMOVE),
            tint = LocalBiziColors.current.muted,
            borderTint = LocalBiziColors.current.panel,
            onClick = onClear,
          )
        }
      }
      if (assignableCandidate != null) {
        Text(
          text = sharedString(SharedString.CURRENT_SEARCH_ASSIGNMENT_HINT, assignableCandidate.name, title),
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      } else if (station == null) {
        Text(
          text = sharedString(SharedString.USE_SEARCH_TO_ASSIGN_STATION),
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
        Text(sharedString(SharedString.SHORTCUTS), color = LocalBiziColors.current.red, fontWeight = FontWeight.SemiBold)
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
  OutlinedTextField(
    modifier = Modifier.fillMaxWidth(),
    value = value,
    onValueChange = onValueChange,
    singleLine = true,
    shape = RoundedCornerShape(20.dp),
    leadingIcon = {
      Icon(Icons.Filled.Search, contentDescription = null, tint = LocalBiziColors.current.muted)
    },
    trailingIcon = if (value.isNotEmpty()) {
      {
        Icon(
          imageVector = Icons.Filled.Close,
          contentDescription = sharedString(SharedString.CLEAR_SEARCH),
          tint = LocalBiziColors.current.muted,
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
    placeholder = { Text(label) },
    colors = OutlinedTextFieldDefaults.colors(
      focusedContainerColor = if (mobilePlatform == MobileUiPlatform.IOS) LocalBiziColors.current.fieldSurfaceIos else LocalBiziColors.current.fieldSurfaceAndroid,
      unfocusedContainerColor = if (mobilePlatform == MobileUiPlatform.IOS) LocalBiziColors.current.fieldSurfaceIos else LocalBiziColors.current.fieldSurfaceAndroid,
      focusedBorderColor = LocalBiziColors.current.red.copy(alpha = if (mobilePlatform == MobileUiPlatform.IOS) 0.18f else 0.30f),
      unfocusedBorderColor = if (mobilePlatform == MobileUiPlatform.IOS) LocalBiziColors.current.panel else LocalBiziColors.current.muted.copy(alpha = 0.18f),
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
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
          sharedString(SharedString.REFRESH_STATIONS_TO_OPEN_ROUTE),
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
            sharedString(SharedString.QUICK_ROUTE_FALLBACK_SUMMARY, selection.radiusMeters, station.distanceMeters)
          } else {
            sharedString(
              SharedString.QUICK_ROUTE_DISTANCE_SUMMARY,
              station.distanceMeters,
              station.bikesAvailable,
              station.slotsFree,
            )
          },
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
        Text(
          sharedString(SharedString.OPEN_ROUTE),
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
  Card(
    modifier = Modifier.animateContentSize(animationSpec = spring(dampingRatio = 0.9f, stiffness = 500f)),
    colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(title, fontWeight = FontWeight.SemiBold)
      Text(description, style = MaterialTheme.typography.bodySmall, color = LocalBiziColors.current.muted)
      if (primaryAction != null && onPrimaryAction != null) {
        OutlinedButton(onClick = onPrimaryAction) {
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

private fun MobileUiPlatform.assistantDisplayName(): String = when (this) {
  MobileUiPlatform.Android -> "Google Assistant"
  MobileUiPlatform.IOS -> "Siri"
}

private fun shortcutGuidesFor(
  mobilePlatform: MobileUiPlatform,
): List<ShortcutGuide> = listOf(
  ShortcutGuide(
    title = sharedString(SharedString.MAP_NEAREST_STATION_LABEL),
    description = sharedString(SharedString.GUIDE_NEAREST_STATION_DESCRIPTION),
    examples = listOf(
      sharedString(SharedString.GUIDE_NEAREST_STATION_EXAMPLE_NEAREST),
      sharedString(SharedString.GUIDE_NEAREST_STATION_EXAMPLE_CLOSEST),
    ),
    icon = Icons.Filled.LocationOn,
  ),
  ShortcutGuide(
    title = sharedString(SharedString.GUIDE_NEAREST_WITH_BIKES_OR_SLOTS),
    description = sharedString(SharedString.GUIDE_NEAREST_WITH_BIKES_OR_SLOTS_DESCRIPTION),
    examples = listOf(
      sharedString(SharedString.GUIDE_NEAREST_WITH_BIKES_OR_SLOTS_EXAMPLE_BIKES),
      sharedString(SharedString.GUIDE_NEAREST_WITH_BIKES_OR_SLOTS_EXAMPLE_SLOTS),
    ),
    icon = Icons.AutoMirrored.Filled.DirectionsBike,
  ),
  ShortcutGuide(
    title = sharedString(SharedString.GUIDE_STATION_STATUS),
    description = sharedString(SharedString.GUIDE_STATION_STATUS_DESCRIPTION),
    examples = listOf(
      sharedString(SharedString.GUIDE_STATION_STATUS_EXAMPLE_HOME),
      sharedString(SharedString.GUIDE_STATION_STATUS_EXAMPLE_HOME_BIKES),
      sharedString(SharedString.GUIDE_STATION_STATUS_EXAMPLE_STATION_SLOTS),
    ),
    icon = Icons.Filled.Search,
  ),
  ShortcutGuide(
    title = sharedString(SharedString.TAB_FAVORITES),
    description = sharedString(SharedString.GUIDE_FAVORITES_DESCRIPTION),
    examples = listOf(
      sharedString(SharedString.GUIDE_FAVORITES_EXAMPLE_OPEN),
      sharedString(SharedString.GUIDE_FAVORITES_EXAMPLE_WORK),
    ),
    icon = Icons.Filled.Favorite,
  ),
  ShortcutGuide(
    title = sharedString(SharedString.GUIDE_ROUTE_TO_STATION),
    description = sharedString(SharedString.GUIDE_ROUTE_TO_STATION_DESCRIPTION),
    examples = listOf(
      sharedString(SharedString.GUIDE_ROUTE_TO_STATION_EXAMPLE_PLAZA_ESPANA),
      sharedString(SharedString.GUIDE_ROUTE_TO_STATION_EXAMPLE_WORK),
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
  val tint = if (label == sharedString(SharedString.HOME)) LocalBiziColors.current.green else LocalBiziColors.current.blue
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
private fun pageBackgroundColor(platform: MobileUiPlatform): Color {
  val c = LocalBiziColors.current
  return if (platform == MobileUiPlatform.IOS) c.groupedBackground else c.background
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

private fun AssistantAction.label(): String = when (this) {
  AssistantAction.FavoriteStations -> sharedString(SharedString.MY_FAVORITES)
  AssistantAction.NearestStation -> sharedString(SharedString.MAP_NEAREST_STATION_LABEL)
  AssistantAction.NearestStationWithBikes -> sharedString(SharedString.ASSISTANT_LABEL_NEAREST_WITH_BIKES)
  AssistantAction.NearestStationWithSlots -> sharedString(SharedString.ASSISTANT_LABEL_NEAREST_WITH_SLOTS)
  is AssistantAction.StationBikeCount -> sharedString(SharedString.BIKES_IN_STATION)
  is AssistantAction.StationSlotCount -> sharedString(SharedString.SLOTS_IN_STATION)
  is AssistantAction.RouteToStation -> sharedString(SharedString.ROUTE_TO_STATION)
  is AssistantAction.StationStatus -> sharedString(SharedString.STATION_STATUS_LABEL)
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
    paddingValues: PaddingValues,
  ) = TripScreen(
    viewModel = viewModel,
    mobilePlatform = mobilePlatform,
    localNotifier = localNotifier,
    routeLauncher = routeLauncher,
    userLocation = userLocation,
    stations = stations,
    isMapReady = isMapReady,
    paddingValues = paddingValues,
  )

  @Composable
  fun NearbyScreenContent(
    mobilePlatform: MobileUiPlatform,
    stations: List<Station>,
    favoriteIds: Set<String>,
    loading: Boolean,
    errorMessage: String?,
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
      nearestSelection = uiState.nearestSelection
        ?: com.gcaguilar.biciradar.core.NearbyStationSelection(null, null, uiState.searchRadiusMeters),
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
    paddingValues: PaddingValues,
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
    paddingValues = paddingValues,
  )

  @Composable
  fun FavoritesScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.FavoritesViewModel,
    mobilePlatform: MobileUiPlatform,
    onOpenAssistant: () -> Unit,
    onStationSelected: (Station) -> Unit,
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
      paddingValues = paddingValues,
    )
  }

  @Composable
  fun ProfileScreenContent(
    mobilePlatform: MobileUiPlatform,
    paddingValues: PaddingValues,
    searchRadiusMeters: Int,
    preferredMapApp: PreferredMapApp,
    themePreference: ThemePreference,
    onOpenShortcuts: () -> Unit,
    onSearchRadiusSelected: (Int) -> Unit,
    onPreferredMapAppSelected: (PreferredMapApp) -> Unit,
    onThemePreferenceSelected: (ThemePreference) -> Unit,
  ) = ProfileScreen(
    mobilePlatform = mobilePlatform,
    paddingValues = paddingValues,
    searchRadiusMeters = searchRadiusMeters,
    preferredMapApp = preferredMapApp,
    themePreference = themePreference,
    onOpenShortcuts = onOpenShortcuts,
    onSearchRadiusSelected = onSearchRadiusSelected,
    onPreferredMapAppSelected = onPreferredMapAppSelected,
    onThemePreferenceSelected = onThemePreferenceSelected,
  )

  @Composable
  fun ProfileScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.ProfileViewModel,
    mobilePlatform: MobileUiPlatform,
    paddingValues: PaddingValues,
    onOpenShortcuts: () -> Unit,
  ) {
    val uiState by viewModel.uiState.collectAsState()
    ProfileScreen(
      mobilePlatform = mobilePlatform,
      paddingValues = paddingValues,
      searchRadiusMeters = uiState.searchRadiusMeters,
      preferredMapApp = uiState.preferredMapApp,
      themePreference = uiState.themePreference,
      onOpenShortcuts = onOpenShortcuts,
      onSearchRadiusSelected = viewModel::onSearchRadiusSelected,
      onPreferredMapAppSelected = viewModel::onPreferredMapAppSelected,
      onThemePreferenceSelected = viewModel::onThemePreferenceSelected,
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
    paddingValues: PaddingValues,
  ) = MapScreen(
    mobilePlatform = mobilePlatform,
    stations = stations,
    favoriteIds = favoriteIds,
    loading = loading,
    errorMessage = errorMessage,
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
    paddingValues = paddingValues,
  )

  @Composable
  fun StationDetailScreenContent(
    mobilePlatform: MobileUiPlatform,
    station: Station,
    graph: SharedGraph,
    favoriteIds: Set<String>,
    userLocation: GeoPoint?,
    isMapReady: Boolean,
    onBack: () -> Unit,
    stationsRepository: StationsRepository,
  ) {
    val scope = rememberCoroutineScope()
    val favoritesRepository = graph.favoritesRepository
    val homeStationId by favoritesRepository.homeStationId.collectAsState()
    val workStationId by favoritesRepository.workStationId.collectAsState()
    StationDetailScreen(
      mobilePlatform = mobilePlatform,
      station = station,
      datosBiziApi = graph.datosBiziApi,
      isFavorite = station.id in favoriteIds,
      isHomeStation = homeStationId == station.id,
      isWorkStation = workStationId == station.id,
      userLocation = userLocation,
      isMapReady = isMapReady,
      onBack = onBack,
      onToggleFavorite = { scope.launch { favoritesRepository.toggle(station.id) } },
      onToggleHome = {
        scope.launch {
          favoritesRepository.setHomeStationId(if (homeStationId == station.id) null else station.id)
        }
      },
      onToggleWork = {
        scope.launch {
          favoritesRepository.setWorkStationId(if (workStationId == station.id) null else station.id)
        }
      },
      onRoute = { graph.routeLauncher.launch(station) },
    )
  }
}
