package com.gcaguilar.bizizaragoza.mobileui

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
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
import com.gcaguilar.bizizaragoza.core.AssistantAction
import com.gcaguilar.bizizaragoza.core.EmbeddedMapProvider
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.MapSupportStatus
import com.gcaguilar.bizizaragoza.core.PlatformBindings
import com.gcaguilar.bizizaragoza.core.PreferredMapApp
import com.gcaguilar.bizizaragoza.core.ThemePreference
import com.gcaguilar.bizizaragoza.core.SEARCH_RADIUS_OPTIONS_METERS
import com.gcaguilar.bizizaragoza.core.SharedGraph
import com.gcaguilar.bizizaragoza.core.Station
import com.gcaguilar.bizizaragoza.core.filterStationsByQuery
import com.gcaguilar.bizizaragoza.core.findStationMatchingQuery
import com.gcaguilar.bizizaragoza.core.findStationMatchingQueryOrPinnedAlias
import com.gcaguilar.bizizaragoza.core.isGoogleMapsReady
import com.gcaguilar.bizizaragoza.core.selectNearbyStation
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
import com.gcaguilar.bizizaragoza.core.DatosBiziApi
import com.gcaguilar.bizizaragoza.core.GooglePlacesApi
import com.gcaguilar.bizizaragoza.core.MONITORING_DURATION_OPTIONS_SECONDS
import com.gcaguilar.bizizaragoza.core.PlacePrediction
import com.gcaguilar.bizizaragoza.core.StationHourlyPattern
import com.gcaguilar.bizizaragoza.core.TripDestination
import com.gcaguilar.bizizaragoza.core.LocalNotifier
import com.gcaguilar.bizizaragoza.core.TripRepository
import com.gcaguilar.bizizaragoza.core.TripState

private val BiziRed = Color(0xFFD7191F)
private val BiziLight = Color(0xFFF8F6F6)
private val BiziGrouped = Color(0xFFF2F2F7)
private val BiziInk = Color(0xFF211111)
private val BiziMuted = Color(0xFF64748B)
private val BiziPanel = Color(0xFFF1F5F9)
private val BiziGreen = Color(0xFF167C3C)
private val BiziBlue = Color(0xFF2563EB)

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
  onAccent = Color.White,
  navBar = BiziDarkSurface,
  navBarIos = BiziDarkSurface.copy(alpha = 0.96f),
  fieldSurfaceIos = BiziDarkSurface,
  fieldSurfaceAndroid = BiziDarkPanel,
  dismissAlphaBase = 0.16f,
)

internal val LocalBiziColors = staticCompositionLocalOf { LightBiziColors }

private enum class MobileTab(val label: String) {
  Cerca("Cerca"),
  Mapa("Mapa"),
  Favoritos("Favoritos"),
  Viaje("Viaje"),
  Perfil("Ajustes"),
}

private val MobileTabs = listOf(
  MobileTab.Cerca,
  MobileTab.Mapa,
  MobileTab.Favoritos,
  MobileTab.Viaje,
  MobileTab.Perfil,
)

private enum class MapFilter(val label: String) {
  BIKES_AND_SLOTS("Bicis + Huecos"),
  ONLY_BIKES("Solo bicis"),
  ONLY_SLOTS("Solo huecos"),
}

private const val CURRENT_CHANGELOG_VERSION = 1

private data class ChangelogEntry(val title: String, val description: String)

private val CHANGELOG_ENTRIES = listOf(
  ChangelogEntry("Tema oscuro", "La app se adapta automáticamente al modo oscuro del sistema."),
  ChangelogEntry("Tarjeta descartable", "Ahora puedes cerrar la tarjeta de estación en el mapa con el botón X."),
  ChangelogEntry("Filtros en el mapa", "Filtra estaciones por disponibilidad: bicis + huecos, solo bicis o solo huecos."),
  ChangelogEntry("Indicador de refresco", "Cuenta atrás visible junto al botón de refresco (actualización cada 5 min)."),
  ChangelogEntry("Patrón de uso", "Gráfica con la media histórica de bicis por hora en cada estación (datos de datosbizi.com)."),
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
private class AppState(
  initialTab: MobileTab = MobileTab.Cerca,
) {
  var currentTab by mutableStateOf(initialTab)
  var selectedStationId by mutableStateOf<String?>(null)
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
  val stationsState by stationsRepository.state.collectAsState()
  val favoriteIds by favoritesRepository.favoriteIds.collectAsState()
  val homeStationId by favoritesRepository.homeStationId.collectAsState()
  val workStationId by favoritesRepository.workStationId.collectAsState()
  val searchRadiusMeters by settingsRepository.searchRadiusMeters.collectAsState()
  val preferredMapApp by settingsRepository.preferredMapApp.collectAsState()
  val themePreference by settingsRepository.themePreference.collectAsState()
  val isMapReady = mapSupportStatus.isGoogleMapsReady() &&
    (mobilePlatform != MobileUiPlatform.IOS || preferredMapApp == PreferredMapApp.GoogleMaps)
  var settingsBootstrapped by remember(graph) { mutableStateOf(false) }
  var favoritesBootstrapped by remember(graph) { mutableStateOf(false) }
  var initialLoadAttemptFinished by remember(graph) { mutableStateOf(false) }
  var minimumSplashElapsed by remember(graph) { mutableStateOf(false) }
  var refreshCountdownSeconds by remember(graph) { mutableStateOf(0) }
  val lastSeenChangelog by settingsRepository.lastSeenChangelogVersion.collectAsState()
  var showChangelog by remember(graph) { mutableStateOf(false) }

  LaunchedEffect(graph) {
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

  LaunchedEffect(graph) {
    val intervalSeconds = 300 // 5 minutes
    while (true) {
      for (remaining in intervalSeconds downTo 1) {
        refreshCountdownSeconds = remaining
        kotlinx.coroutines.delay(1_000)
      }
      refreshCountdownSeconds = 0
      val ids = stationsRepository.state.value.stations.take(20).map { it.id }
      stationsRepository.refreshAvailability(ids)
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

  LaunchedEffect(appState.pendingLaunchRequest, stationsState.stations, searchRadiusMeters) {
    when (val request = appState.pendingLaunchRequest ?: return@LaunchedEffect) {
      MobileLaunchRequest.Favorites -> {
        appState.currentTab = MobileTab.Favoritos
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.NearestStation -> {
        val station = nearestSelection.highlightedStation ?: return@LaunchedEffect
        appState.selectedStationId = station.id
        appState.currentTab = MobileTab.Mapa
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.NearestStationWithBikes -> {
        val station = selectNearbyStation(
          stationsState.stations,
          searchRadiusMeters,
        ) { station -> station.bikesAvailable > 0 }.highlightedStation ?: return@LaunchedEffect
        appState.selectedStationId = station.id
        appState.currentTab = MobileTab.Mapa
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.NearestStationWithSlots -> {
        val station = selectNearbyStation(
          stationsState.stations,
          searchRadiusMeters,
        ) { station -> station.slotsFree > 0 }.highlightedStation ?: return@LaunchedEffect
        appState.selectedStationId = station.id
        appState.currentTab = MobileTab.Mapa
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.OpenAssistant -> {
        appState.currentTab = MobileTab.Viaje
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.StationStatus -> {
        val station = stationsState.stations.firstOrNull() ?: return@LaunchedEffect
        appState.pendingAssistantAction = AssistantAction.StationStatus(station.id)
        appState.currentTab = MobileTab.Viaje
        appState.pendingLaunchRequest = null
      }
      is MobileLaunchRequest.RouteToStation -> {
        val station = request.stationId?.let(stationsRepository::stationById)
          ?: stationsState.stations.firstOrNull()
          ?: return@LaunchedEffect
        appState.selectedStationId = station.id
        graph.routeLauncher.launch(station)
        appState.pendingLaunchRequest = null
      }
      is MobileLaunchRequest.ShowStation -> {
        if (stationsRepository.stationById(request.stationId) == null) return@LaunchedEffect
        appState.selectedStationId = request.stationId
        appState.currentTab = MobileTab.Mapa
        appState.pendingLaunchRequest = null
      }
    }
  }

  LaunchedEffect(appState.pendingAssistantLaunchRequest, stationsState.stations) {
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
        appState.currentTab = MobileTab.Mapa
        station?.let { appState.selectedStationId = it.id }
      }
      is AssistantLaunchRequest.StationStatus -> {
        if (station != null) {
          appState.pendingAssistantAction = AssistantAction.StationStatus(station.id)
          appState.currentTab = MobileTab.Viaje
        } else {
          appState.searchQuery = request.stationQuery.orEmpty()
          appState.currentTab = MobileTab.Mapa
        }
      }
      is AssistantLaunchRequest.StationBikeCount -> {
        if (station != null) {
          appState.pendingAssistantAction = AssistantAction.StationBikeCount(station.id)
          appState.currentTab = MobileTab.Viaje
        } else {
          appState.searchQuery = request.stationQuery.orEmpty()
          appState.currentTab = MobileTab.Mapa
        }
      }
      is AssistantLaunchRequest.StationSlotCount -> {
        if (station != null) {
          appState.pendingAssistantAction = AssistantAction.StationSlotCount(station.id)
          appState.currentTab = MobileTab.Viaje
        } else {
          appState.searchQuery = request.stationQuery.orEmpty()
          appState.currentTab = MobileTab.Mapa
        }
      }
      is AssistantLaunchRequest.RouteToStation -> {
        if (station != null) {
          appState.selectedStationId = station.id
          graph.routeLauncher.launch(station)
        } else {
          appState.searchQuery = request.stationQuery.orEmpty()
          appState.currentTab = MobileTab.Mapa
        }
      }
    }

    appState.pendingAssistantLaunchRequest = null
  }

  val filteredStations = remember(stationsState.stations, appState.searchQuery) {
    filterStations(stationsState.stations, appState.searchQuery)
  }
  val favoriteStations = remember(filteredStations, favoriteIds) {
    val pinnedStationIds = setOfNotNull(homeStationId, workStationId)
    filteredStations.filter { station -> station.id in favoriteIds && station.id !in pinnedStationIds }
  }
  val homeStation = remember(homeStationId, stationsState.stations, stationsRepository) {
    homeStationId?.let(stationsRepository::stationById)
  }
  val workStation = remember(workStationId, stationsState.stations, stationsRepository) {
    workStationId?.let(stationsRepository::stationById)
  }
  val selectedStation = remember(appState.selectedStationId, stationsState.stations, stationsRepository) {
    appState.selectedStationId?.let(stationsRepository::stationById)
  }
  val showStartupSplash = remember(
    minimumSplashElapsed,
    settingsBootstrapped,
    favoritesBootstrapped,
    initialLoadAttemptFinished,
    stationsState.isLoading,
    stationsState.stations,
    stationsState.errorMessage,
  ) {
    val startupDataReady = settingsBootstrapped &&
      favoritesBootstrapped &&
      (initialLoadAttemptFinished || stationsState.stations.isNotEmpty() || stationsState.errorMessage != null)
    !minimumSplashElapsed || !startupDataReady || (stationsState.isLoading && stationsState.stations.isEmpty())
  }

  BiziTheme(mobilePlatform, themePreference) {
    Surface(
      modifier = modifier.fillMaxSize(),
      color = pageBackgroundColor(mobilePlatform),
    ) {
      if (showChangelog) {
        ChangelogDialog(
          onDismiss = {
            showChangelog = false
            scope.launch { settingsRepository.setLastSeenChangelogVersion(CURRENT_CHANGELOG_VERSION) }
          },
        )
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
          AnimatedContent(
            targetState = when {
              selectedStation != null -> "station:${selectedStation.id}"
              else -> "tab:${appState.currentTab.name}"
            },
            transitionSpec = {
              (fadeIn(animationSpec = tween(240)) + slideInHorizontally(animationSpec = tween(240)) { it / 10 })
                .togetherWith(
                  fadeOut(animationSpec = tween(160)) + slideOutHorizontally(animationSpec = tween(160)) { -it / 12 },
                )
            },
            label = "mobile-root-transition",
          ) {
            when {
              selectedStation != null -> StationDetailScreen(
                mobilePlatform = mobilePlatform,
                station = selectedStation,
                datosBiziApi = graph.datosBiziApi,
                isFavorite = favoriteIds.contains(selectedStation.id),
                isHomeStation = homeStationId == selectedStation.id,
                isWorkStation = workStationId == selectedStation.id,
                userLocation = stationsState.userLocation,
                isMapReady = isMapReady,
                onBack = remember(appState) { { appState.selectedStationId = null } },
                onToggleFavorite = remember(appState, scope, favoritesRepository) {
                  { scope.launch { favoritesRepository.toggle(appState.selectedStationId ?: return@launch) } }
                },
                onToggleHome = remember(appState, scope, favoritesRepository) {
                  {
                    val id = appState.selectedStationId ?: return@remember
                    scope.launch {
                      favoritesRepository.setHomeStationId(
                        if (homeStationId == id) null else id,
                      )
                    }
                  }
                },
                onToggleWork = remember(appState, scope, favoritesRepository) {
                  {
                    val id = appState.selectedStationId ?: return@remember
                    scope.launch {
                      favoritesRepository.setWorkStationId(
                        if (workStationId == id) null else id,
                      )
                    }
                  }
                },
                onRoute = remember(appState, graph, stationsRepository) {
                  {
                    val station = appState.selectedStationId?.let(stationsRepository::stationById) ?: return@remember
                    graph.routeLauncher.launch(station)
                  }
                },
              )
              else -> Scaffold(
                containerColor = pageBackgroundColor(mobilePlatform),
                bottomBar = {
                  MobileBottomNavigationBar(
                    mobilePlatform = mobilePlatform,
                    currentTab = appState.currentTab,
                    onTabSelected = remember(appState) { { tab -> appState.currentTab = tab } },
                  )
                },
              ) { innerPadding ->
                AnimatedContent(
                  targetState = appState.currentTab,
                  transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220)) { it / 14 })
                      .togetherWith(
                        fadeOut(animationSpec = tween(140)) + slideOutHorizontally(animationSpec = tween(140)) { -it / 16 },
                      )
                  },
                  label = "mobile-tab-transition",
                ) { tab ->
                  when (tab) {
                    MobileTab.Mapa -> MapScreen(
                      mobilePlatform = mobilePlatform,
                      stations = filteredStations,
                      loading = stationsState.isLoading,
                      errorMessage = stationsState.errorMessage,
                      nearestSelection = nearestSelection,
                      searchQuery = appState.searchQuery,
                      searchRadiusMeters = searchRadiusMeters,
                      userLocation = stationsState.userLocation,
                      onSearchQueryChange = remember(appState) { { appState.searchQuery = it } },
                      onStationSelected = remember(appState) { { appState.selectedStationId = it.id } },
                      onRetry = remember(scope, stationsRepository) { { scope.launch { stationsRepository.loadIfNeeded() } } },
                      onFavoriteToggle = remember(scope, favoritesRepository) { { station -> scope.launch { favoritesRepository.toggle(station.id) } } },
                      favoriteIds = favoriteIds,
                      onQuickRoute = remember(graph) { { station -> graph.routeLauncher.launch(station) } },
                      paddingValues = innerPadding,
                      isMapReady = isMapReady,
                    )
                    MobileTab.Cerca -> NearbyScreen(
                       mobilePlatform = mobilePlatform,
                       stations = stationsState.stations,
                       favoriteIds = favoriteIds,
                       loading = stationsState.isLoading,
                       errorMessage = stationsState.errorMessage,
                       nearestSelection = nearestSelection,
                       searchRadiusMeters = searchRadiusMeters,
                       onStationSelected = remember(appState) { { appState.selectedStationId = it.id } },
                       onRetry = remember(scope, stationsRepository) { { scope.launch { stationsRepository.loadIfNeeded() } } },
                       onRefresh = remember(scope, stationsRepository) { { scope.launch { stationsRepository.forceRefresh() } } },
                       onFavoriteToggle = remember(scope, favoritesRepository) { { station -> scope.launch { favoritesRepository.toggle(station.id) } } },
                       onQuickRoute = remember(graph) { { station -> graph.routeLauncher.launch(station) } },
                       refreshCountdownSeconds = refreshCountdownSeconds,
                       paddingValues = innerPadding,
                     )
                    MobileTab.Favoritos -> FavoritesScreen(
                      mobilePlatform = mobilePlatform,
                      onOpenAssistant = remember(appState) { { appState.currentTab = MobileTab.Viaje } },
                      allStations = stationsState.stations,
                      stations = favoriteStations,
                      homeStation = homeStation,
                      workStation = workStation,
                      searchQuery = appState.searchQuery,
                      onSearchQueryChange = remember(appState) { { appState.searchQuery = it } },
                      onStationSelected = remember(appState) { { appState.selectedStationId = it.id } },
                      onAssignHomeStation = remember(scope, favoritesRepository) { { station -> scope.launch { favoritesRepository.setHomeStationId(station.id) } } },
                      onAssignWorkStation = remember(scope, favoritesRepository) { { station -> scope.launch { favoritesRepository.setWorkStationId(station.id) } } },
                      onClearHomeStation = remember(scope, favoritesRepository) { { scope.launch { favoritesRepository.setHomeStationId(null) } } },
                      onClearWorkStation = remember(scope, favoritesRepository) { { scope.launch { favoritesRepository.setWorkStationId(null) } } },
                      onRemoveFavorite = remember(scope, favoritesRepository) { { station -> scope.launch { favoritesRepository.toggle(station.id) } } },
                      onQuickRoute = remember(graph) { { station -> graph.routeLauncher.launch(station) } },
                      paddingValues = innerPadding,
                    )
                     MobileTab.Viaje -> TripScreen(
                       mobilePlatform = mobilePlatform,
                       tripRepository = graph.tripRepository,
                       googlePlacesApi = graph.googlePlacesApi,
                       googleMapsApiKey = platformBindings.googleMapsApiKey,
                       localNotifier = platformBindings.localNotifier,
                       userLocation = stationsState.userLocation,
                       searchRadiusMeters = searchRadiusMeters,
                       paddingValues = innerPadding,
                     )
                     MobileTab.Perfil -> ProfileScreen(
                       mobilePlatform = mobilePlatform,
                       onOpenAssistant = remember(appState) { { appState.currentTab = MobileTab.Viaje } },
                       paddingValues = innerPadding,
                       mapSupportStatus = mapSupportStatus,
                       searchRadiusMeters = searchRadiusMeters,
                       preferredMapApp = preferredMapApp,
                       themePreference = themePreference,
                       userLocation = stationsState.userLocation,
                       stations = stationsState.stations,
                       graph = graph,
                       stationsRepository = stationsRepository,
                       favoriteIds = favoriteIds,
                       initialAction = appState.pendingAssistantAction,
                       onInitialActionConsumed = remember(appState) { { appState.pendingAssistantAction = null } },
                       onSearchRadiusSelected = remember(scope, settingsRepository) { { radiusMeters -> scope.launch { settingsRepository.setSearchRadiusMeters(radiusMeters) } } },
                       onPreferredMapAppSelected = remember(scope, settingsRepository) { { mapApp -> scope.launch { settingsRepository.setPreferredMapApp(mapApp) } } },
                       onThemePreferenceSelected = remember(scope, settingsRepository) { { pref: ThemePreference -> scope.launch { settingsRepository.setThemePreference(pref) } } },
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
          text = "Bizi Zaragoza",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = c.red,
        )
        Text(
          text = "Cargando estaciones cercanas, favoritas y atajos...",
          style = MaterialTheme.typography.bodyMedium,
          color = c.muted,
        )
      }
      Text(
        text = if (mobilePlatform == MobileUiPlatform.IOS) {
          "Preparando la experiencia del iPhone."
        } else {
          "Preparando la experiencia de Android."
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
  currentTab: MobileTab,
  onTabSelected: (MobileTab) -> Unit,
) {
  NavigationBar(
    containerColor = if (mobilePlatform == MobileUiPlatform.IOS) {
      LocalBiziColors.current.navBarIos
    } else {
      LocalBiziColors.current.navBar
    },
  ) {
    MobileTabs.forEach { tab ->
      NavigationBarItem(
        selected = currentTab == tab,
        onClick = { onTabSelected(tab) },
        icon = {
          Icon(
            imageVector = tab.icon(),
            contentDescription = tab.label,
          )
        },
        label = { Text(tab.label) },
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
  nearestSelection: com.gcaguilar.bizizaragoza.core.NearbyStationSelection,
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
        label = "Buscar estación o dirección",
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
          title = "No hemos podido actualizar el mapa",
          description = errorMessage,
          primaryAction = "Reintentar",
          onPrimaryAction = onRetry,
        )
      } else {
        EmptyStateCard(
          title = if (searchQuery.isBlank()) "Todavía no tenemos estaciones en pantalla" else "No hay estaciones para esa búsqueda",
          description = if (searchQuery.isBlank()) {
            "La app volverá a usar Zaragoza centro si la ubicación tarda demasiado o no está disponible."
          } else {
            "Prueba con otro nombre, dirección o número de estación."
          },
          primaryAction = "Cargar estaciones",
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
      AnimatedVisibility(
        visible = selectedMapStation != null && !isCardDismissed,
        enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(animationSpec = tween(140)),
        label = "map-selected-station-overlay",
      ) {
        selectedMapStation?.let { station ->
          MapSelectedStationCard(
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
  }
}

@Composable
private fun NearbyScreen(
  mobilePlatform: MobileUiPlatform,
  stations: List<Station>,
  favoriteIds: Set<String>,
  loading: Boolean,
  errorMessage: String?,
  nearestSelection: com.gcaguilar.bizizaragoza.core.NearbyStationSelection,
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
              text = "Cerca",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "Acciones rápidas para encontrar bicis, huecos y abrir rutas sin pasar por el mapa completo.",
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
              text = "Cerca de ti",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = LocalBiziColors.current.red,
            )
            Text(
              text = "Estaciones ordenadas por cercanía y acciones rápidas para moverte.",
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
          title = "Más cercana con bicis",
          emptyTitle = "Sin bicis cercanas",
          selection = nearestWithBikesSelection,
          icon = Icons.AutoMirrored.Filled.DirectionsBike,
          tint = LocalBiziColors.current.red,
          mobilePlatform = mobilePlatform,
          onRoute = onQuickRoute,
        )
        QuickRouteActionCard(
          modifier = Modifier.weight(1f),
          title = "Más cercana con huecos",
          emptyTitle = "Sin huecos cercanos",
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
            text = if (loading) "Actualizando estaciones..." else "Estaciones cercanas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = if (nearestSelection.usesFallback) {
              "Si no hay estaciones dentro del radio, te seguimos mostrando igualmente la más cercana."
            } else {
              "Toca cualquier tarjeta para abrir el detalle, guardarla o lanzar la ruta."
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
                Text("Reintentar")
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
            title = "Todavía no tenemos estaciones en pantalla",
            description = "Vuelve a intentarlo y la app usará Zaragoza centro si la ubicación tarda demasiado.",
            primaryAction = "Cargar estaciones",
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
      Icon(Icons.Filled.Sync, contentDescription = "Actualizar estaciones")
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
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    MapFilter.entries.forEach { filter ->
      MapFilterChip(
        label = filter.label,
        selected = filter in activeFilters,
        onClick = { onToggleFilter(filter) },
      )
    }
  }
}

@Composable
private fun MapFilterChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val c = LocalBiziColors.current
  val backgroundColor by animateColorAsState(
    targetValue = if (selected) c.red else c.surface,
    animationSpec = tween(180),
  )
  val contentColor by animateColorAsState(
    targetValue = if (selected) c.onAccent else c.ink,
    animationSpec = tween(180),
  )
  val borderColor by animateColorAsState(
    targetValue = if (selected) c.red else c.muted.copy(alpha = 0.28f),
    animationSpec = tween(180),
  )
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = backgroundColor,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = if (selected) 2.dp else 1.dp,
    modifier = Modifier.clickable(onClick = onClick),
  ) {
    Text(
      text = label,
      color = contentColor,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
    )
  }
}

@Composable
private fun MapSelectedStationCard(
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
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 24.dp else 28.dp),
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, c.red.copy(alpha = 0.12f)) else null,
    colors = CardDefaults.cardColors(containerColor = if (mobilePlatform == MobileUiPlatform.IOS) c.surface else c.red),
  ) {
    Column(
      modifier = Modifier
        .padding(16.dp)
        .animateContentSize(animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f)),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          if (isFallbackSelection) {
            "No hay dentro de ${searchRadiusMeters} m"
          } else if (isShowingNearestSelection) {
            "Estación más cercana"
          } else {
            "Estación seleccionada"
          },
          color = if (mobilePlatform == MobileUiPlatform.IOS) c.red else overlayBody,
        )
        Icon(
          imageVector = Icons.Filled.Close,
          contentDescription = "Cerrar",
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
          "La más cercana está a ${station.distanceMeters} m · ${station.bikesAvailable} bicis · ${station.slotsFree} libres"
        } else {
          "${station.distanceMeters} m · ${station.bikesAvailable} bicis · ${station.slotsFree} libres"
        },
        color = overlayBody,
      )
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RoutePill(
          label = "Ruta",
          onDarkBackground = mobilePlatform != MobileUiPlatform.IOS,
          onClick = { onQuickRoute(station) },
        )
        if (mobilePlatform == MobileUiPlatform.IOS) {
          FavoritePill(
            active = isFavorite,
            onClick = onFavoriteToggle,
            label = if (isFavorite) "Guardada" else "Guardar",
          )
        } else {
          OutlineActionPill(
            label = if (isFavorite) "Guardada" else "Guardar",
            tint = c.onAccent,
            borderTint = c.onAccent.copy(alpha = 0.32f),
            onClick = onFavoriteToggle,
          )
        }
        OutlineActionPill(
          label = "Detalle",
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
          title = "Favoritas",
          subtitle = "Tus estaciones guardadas para consultar el estado o abrir una ruta con un toque.",
          onOpenAssistant = onOpenAssistant,
        )
      } else {
        Text(
          text = "Mis estaciones",
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
        label = "Buscar estación para fijarla o filtrar favoritas",
      )
    }
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Casa y trabajo",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = "Puedes fijar Casa o Trabajo desde el buscador o directamente desde una favorita. Desliza una favorita para quitarla.",
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      }
    }
    item {
      SavedPlaceCard(
        mobilePlatform = mobilePlatform,
        title = "Casa",
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
        title = "Trabajo",
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
          title = "Todavía no tienes favoritas",
          description = "Guarda estaciones desde el mapa para consultarlas más rápido y compartirlas con el reloj.",
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
  onOpenAssistant: () -> Unit,
  paddingValues: PaddingValues,
  mapSupportStatus: MapSupportStatus,
  searchRadiusMeters: Int,
  preferredMapApp: PreferredMapApp,
  themePreference: ThemePreference,
  userLocation: GeoPoint?,
  stations: List<Station>,
  graph: SharedGraph,
  stationsRepository: com.gcaguilar.bizizaragoza.core.StationsRepository,
  favoriteIds: Set<String>,
  initialAction: AssistantAction?,
  onInitialActionConsumed: () -> Unit,
  onSearchRadiusSelected: (Int) -> Unit,
  onPreferredMapAppSelected: (PreferredMapApp) -> Unit,
  onThemePreferenceSelected: (ThemePreference) -> Unit,
) {
  val scope = rememberCoroutineScope()
  var latestAnswer by rememberSaveable { mutableStateOf("Pregunta por estaciones cercanas, favoritas o rutas.") }
  val assistantSuggestions = listOf(
    AssistantAction.NearestStation,
    AssistantAction.NearestStationWithBikes,
    AssistantAction.NearestStationWithSlots,
    AssistantAction.FavoriteStations,
    stations.firstOrNull()?.let { AssistantAction.RouteToStation(it.id) },
  ).filterNotNull()
  val shortcutGuides = remember(mobilePlatform) { shortcutGuidesFor(mobilePlatform) }

  LaunchedEffect(initialAction, stations, favoriteIds) {
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
          title = "Ajustes",
          subtitle = "Controla el radio cercano, revisa la ubicación activa y mantén clara la experiencia en cada plataforma.",
          onOpenAssistant = onOpenAssistant,
        )
      } else {
        Text("Perfil y ajustes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
      }
    }
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
      ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          InfoLine(icon = Icons.Filled.LocationOn, label = "Ubicación") {
            if (userLocation != null) {
              "Activa: ${userLocation.latitude.formatCoordinate()}, ${userLocation.longitude.formatCoordinate()}"
            } else {
              "Pendiente. Se usa el centro de Zaragoza como fallback."
            }
          }
          InfoLine(icon = Icons.Filled.Directions, label = "Rutas") {
            if (mobilePlatform == MobileUiPlatform.IOS) {
              "Se abrirán en ${preferredMapApp.displayName()} cuando lances una ruta."
            } else {
              "Se abrirán en la app de navegación disponible en Android."
            }
          }
          InfoLine(icon = Icons.Filled.Map, label = "Mapa en la app") {
            mapSupportStatus.embeddedProvider.displayName()
          }
          InfoLine(icon = Icons.Filled.KeyboardVoice, label = "Atajos") {
            "App Actions, App Shortcuts y Siri/App Intents."
          }
          InfoLine(icon = Icons.Filled.Tune, label = "Idioma") {
            "Español."
          }
        }
      }
    }
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
      ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text("Radio para estación cercana", fontWeight = FontWeight.SemiBold)
          Text(
            "Si no hay estaciones dentro del radio, la app mostrará igualmente la más cercana y te indicará la distancia.",
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
          SEARCH_RADIUS_OPTIONS_METERS.chunked(2).forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              rowOptions.forEach { radiusMeters ->
                RadiusSelectionButton(
                  modifier = Modifier.weight(1f),
                  selected = radiusMeters == searchRadiusMeters,
                  label = if (radiusMeters == searchRadiusMeters) "${radiusMeters} m activo" else "${radiusMeters} m",
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
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text("Integración de Google Maps", fontWeight = FontWeight.SemiBold)
          Text(
            mapSupportStatus.googleMapsSupportHeadline(mobilePlatform),
            style = MaterialTheme.typography.bodyMedium,
          )
          Text(
            mapSupportStatus.googleMapsSupportDescription(mobilePlatform),
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
        }
      }
    }
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
      ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text("Apariencia", fontWeight = FontWeight.SemiBold)
          Text(
            "Elige si la app sigue el sistema o usa siempre el tema claro u oscuro.",
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RadiusSelectionButton(
              modifier = Modifier.weight(1f),
              selected = themePreference == ThemePreference.System,
              label = "Sistema",
              onClick = { onThemePreferenceSelected(ThemePreference.System) },
            )
            RadiusSelectionButton(
              modifier = Modifier.weight(1f),
              selected = themePreference == ThemePreference.Light,
              label = "Claro",
              onClick = { onThemePreferenceSelected(ThemePreference.Light) },
            )
            RadiusSelectionButton(
              modifier = Modifier.weight(1f),
              selected = themePreference == ThemePreference.Dark,
              label = "Oscuro",
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
            Text("App de rutas en iPhone", fontWeight = FontWeight.SemiBold)
            Text(
              "Elige qué app abrir para las rutas rápidas, el detalle de estación y los atajos de Siri.",
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
               "Si Google Maps no está instalado, Bizi Zaragoza usará Apple Maps como fallback.",
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
          Text("Datos de predicción", fontWeight = FontWeight.SemiBold)
          Text(
            "Los patrones horarios y predicciones de ocupación se obtienen de datosbizi.com, un proyecto independiente con datos históricos de Bizi Zaragoza.",
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
          TextButton(
            onClick = { uriHandler.openUri("https://datosbizi.com") },
            contentPadding = PaddingValues(0.dp),
          ) {
            Text("Visitar datosbizi.com", style = MaterialTheme.typography.bodySmall)
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
          Text("Comentarios y sugerencias", fontWeight = FontWeight.SemiBold)
          Text(
            "¿Algo no funciona bien o tienes una idea? Usa el formulario para enviar tu opinión.",
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
          TextButton(
            onClick = { uriHandler.openUri("https://forms.gle/j6hMxPQypzhqXp5v5") },
            contentPadding = PaddingValues(0.dp),
          ) {
            Text("Abrir formulario de feedback", style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    }
    // -------- Atajos / assistant shortcuts section --------
    item {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          "Atajos",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
        Text(
          "Qué puede hacer la app y cómo invocarlo con ${mobilePlatform.assistantDisplayName()}.",
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      }
    }
    item {
      Card(colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface)) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text("Cómo invocarlos", fontWeight = FontWeight.SemiBold)
          Text(
            if (mobilePlatform == MobileUiPlatform.IOS) {
              "Abre Siri o Atajos y usa frases como las de abajo terminando en \u201cen Bizi Zaragoza\u201d."
            } else {
              "Abre Google Assistant y prueba frases como las de abajo. Si hace falta, empieza por \u201cabre Bizi Zaragoza y...\u201d. La ruta se abrir\u00e1 en la navegaci\u00f3n del tel\u00e9fono."
            },
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
          Text(
            "Radio actual para búsquedas cercanas: ${searchRadiusMeters} m.",
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.ink,
          )
        }
      }
    }
    items(shortcutGuides, key = { it.title }) { guide ->
      ShortcutGuideCard(guide = guide)
    }
    item {
      Card(colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface)) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text("Última respuesta", fontWeight = FontWeight.SemiBold)
          Text(latestAnswer)
        }
      }
    }
    item {
      Text(
        text = "Prueba rápida",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
      )
    }
    items(assistantSuggestions, key = { it.label() }) { action ->
      OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
          scope.launch {
            val resolution = graph.assistantIntentResolver.resolve(
              action = action,
              stationsState = stationsRepository.state.value,
              favoriteIds = favoriteIds,
              searchRadiusMeters = searchRadiusMeters,
            )
            latestAnswer = resolution.spokenResponse
          }
        },
      ) {
        Icon(action.icon(), contentDescription = null)
        Spacer(Modifier.width(10.dp))
        Text(action.label())
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
      TopAppBar(
        title = { Text(station.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
          }
        },
      )
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
              label = if (isFavorite) "Guardada" else "Guardar",
            )
          }
          Text(station.address, style = MaterialTheme.typography.bodyMedium, color = LocalBiziColors.current.muted)
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StationMetricPill(
              modifier = Modifier.weight(1f),
              label = "Distancia",
              value = "${station.distanceMeters} m",
              tint = LocalBiziColors.current.blue,
            )
            StationMetricPill(
              modifier = Modifier.weight(1f),
              label = "Fuente",
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
          Text("Guardar esta estación", fontWeight = FontWeight.SemiBold)
          Text(
            "Márcala como favorita o fíjala como Casa o Trabajo para recuperarla más rápido desde Favoritas y con los atajos de voz.",
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FavoritePill(
              active = isFavorite,
              onClick = onToggleFavorite,
              label = if (isFavorite) "Favorita" else "Guardar",
            )
            SavedPlacePill(
              active = isHomeStation,
              label = "Casa",
              onClick = onToggleHome,
            )
            SavedPlacePill(
              active = isWorkStation,
              label = "Trabajo",
              onClick = onToggleWork,
            )
          }
          Text(
            when {
              isHomeStation && isWorkStation -> "Ahora mismo esta estación está marcada como Casa y Trabajo."
              isHomeStation -> "Ahora mismo esta estación está marcada como Casa."
              isWorkStation -> "Ahora mismo esta estación está marcada como Trabajo."
              else -> "Puedes tocar Casa o Trabajo para asignarla directamente."
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
          label = "Bicis",
          value = station.bikesAvailable.toString(),
          icon = Icons.AutoMirrored.Filled.DirectionsBike,
          tint = LocalBiziColors.current.red,
          mobilePlatform = mobilePlatform,
        )
        AvailabilityCard(
          modifier = Modifier.weight(1f),
          label = "Huecos",
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
        Text("Abrir ruta")
      }
    }
    item {
      OutlinedButton(onClick = onToggleFavorite, modifier = Modifier.fillMaxWidth()) {
        Icon(
          if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
          contentDescription = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(if (isFavorite) "Quitar de favoritos" else "Guardar en favoritos")
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
        Text("Patrón de uso", fontWeight = FontWeight.SemiBold)
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
              "No hay datos de patrón disponibles",
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
                "No hay datos para este tipo de día",
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
              )
            }
          } else {
            Text(
              "Media de bicis disponibles en esta estación según la hora del día, basada en datos históricos de datosbizi.com.",
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
                  label = "Más bicis",
                  value = "${bestBikesHour.hour}:00h (~${bestBikesHour.bikesAvg.roundToInt()})",
                  tint = colors.red,
                )
                PatternHintPill(
                  modifier = Modifier.weight(1f),
                  label = "Más huecos",
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
      Text("Novedades", fontWeight = FontWeight.Bold)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        items(CHANGELOG_ENTRIES.size) { index ->
          val entry = CHANGELOG_ENTRIES[index]
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              entry.title,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              entry.description,
              style = MaterialTheme.typography.bodySmall,
              color = colors.muted,
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Entendido")
      }
    },
  )
}

// ---------------------------------------------------------------------------
// TripScreen — plan a trip to a destination and optionally monitor a station
// ---------------------------------------------------------------------------

@Composable
private fun TripScreen(
  mobilePlatform: MobileUiPlatform,
  tripRepository: TripRepository,
  googlePlacesApi: GooglePlacesApi,
  googleMapsApiKey: String?,
  localNotifier: LocalNotifier,
  userLocation: GeoPoint?,
  searchRadiusMeters: Int,
  paddingValues: PaddingValues,
) {
  val c = LocalBiziColors.current
  val scope = rememberCoroutineScope()
  val tripState by tripRepository.state.collectAsState()

  // ---------- autocomplete state ----------
  var query by rememberSaveable { mutableStateOf("") }
  var suggestions by remember { mutableStateOf<List<PlacePrediction>>(emptyList()) }
  var isLoadingSuggestions by remember { mutableStateOf(false) }
  var debounceJob by remember { mutableStateOf<Job?>(null) }

  // Trigger autocomplete on query change (debounced 400 ms)
  LaunchedEffect(query) {
    debounceJob?.cancel()
    if (query.isBlank() || googleMapsApiKey == null) {
      suggestions = emptyList()
      return@LaunchedEffect
    }
    debounceJob = scope.launch {
      delay(400)
      isLoadingSuggestions = true
      suggestions = googlePlacesApi.autocomplete(query, userLocation, googleMapsApiKey)
      isLoadingSuggestions = false
    }
  }

  // Reset query when the trip is cleared
  LaunchedEffect(tripState.destination) {
    if (tripState.destination == null) {
      query = ""
      suggestions = emptyList()
    }
  }

  // ---------- monitoring duration selection ----------
  var selectedDurationSeconds by rememberSaveable { mutableStateOf(MONITORING_DURATION_OPTIONS_SECONDS[0]) }

  // ---------- layout ----------
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(pageBackgroundColor(mobilePlatform)),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Header
    item {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          "Viaje",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
        )
        Text(
          "Busca la estación más cercana a tu destino y vigílala.",
          style = MaterialTheme.typography.bodyMedium,
          color = c.muted,
        )
      }
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
                "Estación llena",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = c.red,
              )
            }
            Text(
              "\"${alert.fullStation.name}\" ya no tiene plazas libres.",
              style = MaterialTheme.typography.bodyMedium,
            )
            val altStation = alert.alternativeStation
            if (altStation != null) {
              val dist = alert.alternativeDistanceMeters
              val distText = if (dist != null) " (${dist} m)" else ""
              Text(
                "Alternativa sugerida: ${altStation.name}$distText — ${altStation.slotsFree} plazas.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
              )
            } else {
              Text(
                "No se encontró alternativa cercana con plazas.",
                style = MaterialTheme.typography.bodySmall,
                color = c.muted,
              )
            }
            Button(
              onClick = { tripRepository.dismissAlert() },
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text("Entendido")
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
              "¿Adónde vas?",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
              value = query,
              onValueChange = { query = it },
              modifier = Modifier.fillMaxWidth(),
              label = { Text("Destino") },
              placeholder = { Text("Escribe una dirección o lugar") },
              singleLine = true,
              leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null)
              },
              trailingIcon = {
                if (query.isNotEmpty()) {
                  IconButton(onClick = { query = ""; suggestions = emptyList() }) {
                    Icon(Icons.Filled.Close, contentDescription = "Borrar")
                  }
                }
              },
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = c.red,
                cursorColor = c.red,
              ),
            )
            if (googleMapsApiKey == null) {
              Text(
                "Google Maps API key no disponible. La búsqueda de destinos no funcionará.",
                style = MaterialTheme.typography.bodySmall,
                color = c.red,
              )
            }
          }
        }
      }

      // Autocomplete suggestions
      if (suggestions.isNotEmpty()) {
        item(key = "suggestions-header") {
          Text(
            "Sugerencias",
            style = MaterialTheme.typography.labelMedium,
            color = c.muted,
          )
        }
        items(suggestions, key = { it.placeId }) { prediction ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = c.surface,
            border = BorderStroke(1.dp, c.panel),
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                if (googleMapsApiKey != null) {
                  scope.launch {
                    val details = googlePlacesApi.placeDetails(prediction.placeId, googleMapsApiKey)
                    if (details != null) {
                      suggestions = emptyList()
                      query = details.name
                      tripRepository.setDestination(
                        destination = TripDestination(
                          name = details.name,
                          location = details.location,
                        ),
                        searchRadiusMeters = searchRadiusMeters,
                      )
                    }
                  }
                }
              },
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
              Text(
                prediction.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      } else if (isLoadingSuggestions) {
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
                "Destino",
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
              onClick = { tripRepository.clearTrip() },
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
              Icon(
                Icons.Filled.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
              Spacer(Modifier.width(4.dp))
              Text("Limpiar", style = MaterialTheme.typography.labelMedium)
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
                "Buscando estación cercana...",
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

        // Monitoring active (State 6)
        if (tripState.monitoring.isActive) {
          item(key = "monitoring-active") {
            TripMonitoringActiveCard(
              monitoring = tripState.monitoring,
              onStop = { tripRepository.stopMonitoring() },
            )
          }
        } else {
          // Monitoring setup (State 5)
          item(key = "monitoring-setup") {
            TripMonitoringSetupCard(
              selectedDurationSeconds = selectedDurationSeconds,
              onDurationSelected = { selectedDurationSeconds = it },
              onStartMonitoring = {
                scope.launch {
                  localNotifier.requestPermission()
                  tripRepository.startMonitoring(selectedDurationSeconds)
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
          "Estación sugerida",
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
          label = "Plazas libres",
          value = station.slotsFree.toString(),
          tint = c.blue,
        )
        StationMetricPill(
          label = "Bicis",
          value = station.bikesAvailable.toString(),
          tint = c.red,
        )
        if (distanceMeters != null) {
          StationMetricPill(
            label = "Distancia",
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
        "Vigilar esta estación",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        "Recibirás una notificación si se llena antes de que llegues.",
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
        Text("Iniciar vigilancia")
      }
    }
  }
}

@Composable
private fun TripMonitoringActiveCard(
  monitoring: com.gcaguilar.bizizaragoza.core.TripMonitoringState,
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
          "Vigilancia activa",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = c.blue,
        )
      }
      Text(
        "Tiempo restante: ${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
      Text(
        "Se comprueba cada 30 s. Notificación si la estación se llena.",
        style = MaterialTheme.typography.bodySmall,
        color = c.muted,
      )
      OutlinedButton(
        onClick = onStop,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Parar vigilancia")
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
              label = "Ruta",
              onClick = quickRoute,
            )
          }
          if (showFavoriteCta) {
            FavoritePill(
              active = isFavorite,
              onClick = onFavoriteToggle,
              label = if (isFavorite) "Guardada" else "Guardar",
            )
          } else {
            FavoritePill(
              active = true,
              onClick = {},
              label = "Favorita",
            )
          }
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StationMetricPill(
          modifier = Modifier.weight(1f),
          label = "Bicis",
          value = station.bikesAvailable.toString(),
          tint = LocalBiziColors.current.red,
        )
        StationMetricPill(
          modifier = Modifier.weight(1f),
          label = "Huecos",
          value = station.slotsFree.toString(),
          tint = LocalBiziColors.current.blue,
        )
        StationMetricPill(
          modifier = Modifier.weight(1f),
          label = "Distancia",
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
              label = "Casa",
              tint = LocalBiziColors.current.green,
              onClick = onAssignHome,
            )
          }
          if (canAssignWork) {
            SavedPlaceQuickAction(
              label = "Trabajo",
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
        text = if (mobilePlatform == MobileUiPlatform.IOS) "Quitar favorita" else "Eliminar favorita",
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
            label = "Bicis",
            value = station.bikesAvailable.toString(),
            tint = LocalBiziColors.current.red,
          )
          StationMetricPill(
            modifier = Modifier.weight(1f),
            label = "Huecos",
            value = station.slotsFree.toString(),
            tint = LocalBiziColors.current.blue,
          )
          StationMetricPill(
            modifier = Modifier.weight(1f),
            label = "Distancia",
            value = "${station.distanceMeters} m",
            tint = LocalBiziColors.current.green,
          )
        }
      } else {
        Text(
          text = "Todavía no has fijado una estación para $title.",
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
            label = "Ruta",
            onClick = { onQuickRoute(station) },
          )
          OutlineActionPill(
            label = "Detalle",
            tint = LocalBiziColors.current.red,
            borderTint = LocalBiziColors.current.red.copy(alpha = 0.16f),
            onClick = { onOpenStationDetails(station) },
          )
        }
        if (assignableCandidate != null) {
          OutlineActionPill(
            label = "Asignar búsqueda",
            tint = LocalBiziColors.current.blue,
            borderTint = LocalBiziColors.current.blue.copy(alpha = 0.16f),
            onClick = { onAssignCandidate(assignableCandidate) },
          )
        }
        if (station != null) {
          OutlineActionPill(
            label = "Quitar",
            tint = LocalBiziColors.current.muted,
            borderTint = LocalBiziColors.current.panel,
            onClick = onClear,
          )
        }
      }
      if (assignableCandidate != null) {
        Text(
          text = "La búsqueda actual apunta a ${assignableCandidate.name}. Se usará para $title si pulsas asignar.",
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      } else if (station == null) {
        Text(
          text = "Usa el buscador de arriba para elegir una estación y asignarla.",
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
        Text("Atajos", color = LocalBiziColors.current.red, fontWeight = FontWeight.SemiBold)
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
          contentDescription = "Limpiar búsqueda",
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
  selection: com.gcaguilar.bizizaragoza.core.NearbyStationSelection,
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
          "Actualiza estaciones para poder abrir la ruta.",
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
            "No hay dentro de ${selection.radiusMeters} m. La más cercana está a ${station.distanceMeters} m."
          } else {
            "${station.distanceMeters} m · ${station.bikesAvailable} bicis · ${station.slotsFree} huecos"
          },
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
        Text(
          "Abrir ruta",
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
    title = "Estación más cercana",
    description = "Encuentra la estación más cercana a tu ubicación actual y te abre la app en el contexto correcto.",
    examples = listOf(
      "Muéstrame la estación más cercana en Bizi Zaragoza",
      if (mobilePlatform == MobileUiPlatform.Android) {
        "Abre Bizi Zaragoza y muéstrame la estación más cercana"
      } else {
        "Abre Bizi Zaragoza y enséñame la estación más cercana"
      },
    ),
    icon = Icons.Filled.LocationOn,
  ),
  ShortcutGuide(
    title = "Más cercana con bicis o huecos",
    description = "Busca la mejor estación cercana para coger bici o para devolverla con huecos libres.",
    examples = listOf(
      "Muéstrame la estación más cercana con bicis en Bizi Zaragoza",
      "Muéstrame la estación más cercana con huecos en Bizi Zaragoza",
    ),
    icon = Icons.AutoMirrored.Filled.DirectionsBike,
  ),
  ShortcutGuide(
    title = "Estado de una estación",
    description = "Consulta una estación concreta por nombre, dirección, número o usando tus alias de Casa y Trabajo.",
    examples = listOf(
      "Enséñame el estado de casa en Bizi Zaragoza",
      "Enséñame cuántas bicis hay en Plaza España",
      "Enséñame cuántos huecos hay en la estación 48",
    ),
    icon = Icons.Filled.Search,
  ),
  ShortcutGuide(
    title = "Favoritas",
    description = "Abre directamente tu lista de favoritas, con Casa y Trabajo incluidos cuando estén configurados.",
    examples = listOf(
      "Abre mis favoritas en Bizi Zaragoza",
      "Enséñame el estado de trabajo en Bizi Zaragoza",
    ),
    icon = Icons.Filled.Favorite,
  ),
  ShortcutGuide(
    title = "Ruta a una estación",
    description = "Lanza la ruta rápida hacia una estación concreta o hacia Casa y Trabajo si ya los has configurado.",
    examples = listOf(
      "Llévame a Plaza España con Bizi Zaragoza",
      "Llévame a trabajo con Bizi Zaragoza",
    ),
    icon = Icons.Filled.Directions,
  ),
)

private fun EmbeddedMapProvider.displayName(): String = when (this) {
  EmbeddedMapProvider.None -> "No hay mapa embebido"
  EmbeddedMapProvider.AppleMapKit -> "Apple Maps"
  EmbeddedMapProvider.GoogleMaps -> "Google Maps"
}

private fun MapSupportStatus.googleMapsSupportHeadline(
  mobilePlatform: MobileUiPlatform,
): String = when {
  isGoogleMapsReady() -> "Google Maps está listo para usarse en esta plataforma."
  mobilePlatform == MobileUiPlatform.Android ->
    "Android ya usa Google Maps dentro de la app y solo falta dejar la API key configurada donde corresponda."
  else ->
    "iPhone sigue usando Apple Maps dentro de la app, pero ya queda preparado para activar Google Maps en cuanto enlacemos el SDK oficial."
}

private fun MapSupportStatus.googleMapsSupportDescription(
  mobilePlatform: MobileUiPlatform,
): String = when {
  isGoogleMapsReady() && mobilePlatform == MobileUiPlatform.Android ->
    "La clave de Google Maps está presente y el mapa embebido de Android queda operativo."
  isGoogleMapsReady() ->
    "El SDK de Google Maps está enlazado y la clave API está configurada."
  mobilePlatform == MobileUiPlatform.Android ->
    "Puedes usar la variable GOOGLE_MAPS_API_KEY, la propiedad de Gradle googleMapsApiKey o local.properties para dejarlo listo también en CI y desarrollo local."
  googleMapsSdkLinked && !googleMapsApiKeyConfigured ->
    "Falta definir la clave GOOGLE_MAPS_IOS_API_KEY para poder activar Google Maps en iPhone sin tocar más la UI."
  !googleMapsSdkLinked && googleMapsApiKeyConfigured ->
    "La clave ya está puesta. El siguiente paso será enlazar el SDK oficial de Google Maps en el proyecto iOS."
  else ->
    "La app ya detecta si el SDK de Google Maps está enlazado y si la clave está definida. Cuando lo añadamos al proyecto, el bootstrap quedará resuelto."
}

@Composable
private fun InfoLine(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: () -> String,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Icon(icon, contentDescription = null, tint = LocalBiziColors.current.red)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(label, style = MaterialTheme.typography.labelMedium, color = LocalBiziColors.current.muted)
      Text(value(), style = MaterialTheme.typography.bodyMedium)
    }
  }
}

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
  val tint = if (label == "Casa") LocalBiziColors.current.green else LocalBiziColors.current.blue
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
  AssistantAction.FavoriteStations -> "Mis favoritas"
  AssistantAction.NearestStation -> "Estación más cercana"
  AssistantAction.NearestStationWithBikes -> "Cercana con bicis"
  AssistantAction.NearestStationWithSlots -> "Cercana con huecos"
  is AssistantAction.StationBikeCount -> "Bicis en estación"
  is AssistantAction.StationSlotCount -> "Huecos en estación"
  is AssistantAction.RouteToStation -> "Ruta a estación"
  is AssistantAction.StationStatus -> "Estado de estación"
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

private fun Double.formatCoordinate(): String = ((this * 10_000).roundToInt() / 10_000.0).toString()
