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
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gcaguilar.bizizaragoza.core.AssistantAction
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.PlatformBindings
import com.gcaguilar.bizizaragoza.core.PreferredMapApp
import com.gcaguilar.bizizaragoza.core.SEARCH_RADIUS_OPTIONS_METERS
import com.gcaguilar.bizizaragoza.core.SharedGraph
import com.gcaguilar.bizizaragoza.core.Station
import com.gcaguilar.bizizaragoza.core.filterStationsByQuery
import com.gcaguilar.bizizaragoza.core.findStationMatchingQuery
import com.gcaguilar.bizizaragoza.core.findStationMatchingQueryOrPinnedAlias
import com.gcaguilar.bizizaragoza.core.selectNearbyStation
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val BiziRed = Color(0xFFD7191F)
private val BiziLight = Color(0xFFF8F6F6)
private val BiziGrouped = Color(0xFFF2F2F7)
private val BiziInk = Color(0xFF211111)
private val BiziMuted = Color(0xFF64748B)
private val BiziPanel = Color(0xFFF1F5F9)
private val BiziGreen = Color(0xFF167C3C)
private val BiziBlue = Color(0xFF2563EB)

private enum class MobileTab(val label: String) {
  Mapa("Mapa"),
  Favoritos("Favoritos"),
  Perfil("Ajustes"),
}

private val MobileTabs = listOf(
  MobileTab.Mapa,
  MobileTab.Favoritos,
  MobileTab.Perfil,
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

@Composable
fun BiziMobileApp(
  platformBindings: PlatformBindings,
  modifier: Modifier = Modifier,
  refreshKey: Any? = Unit,
  launchRequest: MobileLaunchRequest? = null,
  assistantLaunchRequest: AssistantLaunchRequest? = null,
) {
  val mobilePlatform = remember { currentMobileUiPlatform() }
  val graph = remember(platformBindings) {
    SharedGraph.Companion.create(platformBindings)
  }
  val stationsRepository = remember(graph) { graph.stationsRepository }
  val favoritesRepository = remember(graph) { graph.favoritesRepository }
  val settingsRepository = remember(graph) { graph.settingsRepository }
  val scope = rememberCoroutineScope()
  val stationsState by stationsRepository.state.collectAsState()
  val favoriteIds by favoritesRepository.favoriteIds.collectAsState()
  val homeStationId by favoritesRepository.homeStationId.collectAsState()
  val workStationId by favoritesRepository.workStationId.collectAsState()
  val searchRadiusMeters by settingsRepository.searchRadiusMeters.collectAsState()
  val preferredMapApp by settingsRepository.preferredMapApp.collectAsState()
  var currentTab by rememberSaveable { mutableStateOf(MobileTab.Mapa) }
  var selectedStationId by rememberSaveable { mutableStateOf<String?>(null) }
  var assistantOpen by rememberSaveable { mutableStateOf(false) }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var pendingAssistantAction by remember { mutableStateOf<AssistantAction?>(null) }
  var pendingLaunchRequest by remember { mutableStateOf<MobileLaunchRequest?>(null) }
  var pendingAssistantLaunchRequest by remember { mutableStateOf<AssistantLaunchRequest?>(null) }

  LaunchedEffect(graph, refreshKey) {
    launch { settingsRepository.bootstrap() }
    launch { favoritesRepository.bootstrap() }
    stationsRepository.loadIfNeeded()
  }

  LaunchedEffect(graph) {
    while (true) {
      kotlinx.coroutines.delay(30_000)
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
    pendingLaunchRequest = launchRequest
  }

  LaunchedEffect(assistantLaunchRequest) {
    pendingAssistantLaunchRequest = assistantLaunchRequest
  }

  LaunchedEffect(pendingLaunchRequest, stationsState.stations, searchRadiusMeters) {
    when (val request = pendingLaunchRequest ?: return@LaunchedEffect) {
      MobileLaunchRequest.Favorites -> {
        currentTab = MobileTab.Favoritos
        pendingLaunchRequest = null
      }
      MobileLaunchRequest.NearestStation -> {
        val station = nearestSelection.highlightedStation ?: return@LaunchedEffect
        selectedStationId = station.id
        currentTab = MobileTab.Mapa
        pendingLaunchRequest = null
      }
      MobileLaunchRequest.NearestStationWithBikes -> {
        val station = selectNearbyStation(
          stationsState.stations,
          searchRadiusMeters,
        ) { station -> station.bikesAvailable > 0 }.highlightedStation ?: return@LaunchedEffect
        selectedStationId = station.id
        currentTab = MobileTab.Mapa
        pendingLaunchRequest = null
      }
      MobileLaunchRequest.NearestStationWithSlots -> {
        val station = selectNearbyStation(
          stationsState.stations,
          searchRadiusMeters,
        ) { station -> station.slotsFree > 0 }.highlightedStation ?: return@LaunchedEffect
        selectedStationId = station.id
        currentTab = MobileTab.Mapa
        pendingLaunchRequest = null
      }
      MobileLaunchRequest.OpenAssistant -> {
        assistantOpen = true
        pendingLaunchRequest = null
      }
      MobileLaunchRequest.StationStatus -> {
        val station = stationsState.stations.firstOrNull() ?: return@LaunchedEffect
        pendingAssistantAction = AssistantAction.StationStatus(station.id)
        assistantOpen = true
        pendingLaunchRequest = null
      }
      is MobileLaunchRequest.RouteToStation -> {
        val station = request.stationId?.let(stationsRepository::stationById)
          ?: stationsState.stations.firstOrNull()
          ?: return@LaunchedEffect
        selectedStationId = station.id
        graph.routeLauncher.launch(station)
        pendingLaunchRequest = null
      }
      is MobileLaunchRequest.ShowStation -> {
        if (stationsRepository.stationById(request.stationId) == null) return@LaunchedEffect
        selectedStationId = request.stationId
        currentTab = MobileTab.Mapa
        pendingLaunchRequest = null
      }
    }
  }

  LaunchedEffect(pendingAssistantLaunchRequest, stationsState.stations) {
    val request = pendingAssistantLaunchRequest ?: return@LaunchedEffect
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
        searchQuery = request.stationQuery
        currentTab = MobileTab.Mapa
        station?.let { selectedStationId = it.id }
      }
      is AssistantLaunchRequest.StationStatus -> {
        if (station != null) {
          pendingAssistantAction = AssistantAction.StationStatus(station.id)
          assistantOpen = true
        } else {
          searchQuery = request.stationQuery.orEmpty()
          currentTab = MobileTab.Mapa
        }
      }
      is AssistantLaunchRequest.StationBikeCount -> {
        if (station != null) {
          pendingAssistantAction = AssistantAction.StationBikeCount(station.id)
          assistantOpen = true
        } else {
          searchQuery = request.stationQuery.orEmpty()
          currentTab = MobileTab.Mapa
        }
      }
      is AssistantLaunchRequest.StationSlotCount -> {
        if (station != null) {
          pendingAssistantAction = AssistantAction.StationSlotCount(station.id)
          assistantOpen = true
        } else {
          searchQuery = request.stationQuery.orEmpty()
          currentTab = MobileTab.Mapa
        }
      }
      is AssistantLaunchRequest.RouteToStation -> {
        if (station != null) {
          selectedStationId = station.id
          graph.routeLauncher.launch(station)
        } else {
          searchQuery = request.stationQuery.orEmpty()
          currentTab = MobileTab.Mapa
        }
      }
    }

    pendingAssistantLaunchRequest = null
  }

  val filteredStations = remember(stationsState.stations, searchQuery) {
    filterStations(stationsState.stations, searchQuery)
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
  val selectedStation = remember(selectedStationId, stationsState.stations, stationsRepository) {
    selectedStationId?.let(stationsRepository::stationById)
  }

  BiziTheme(mobilePlatform) {
    Surface(
      modifier = modifier.fillMaxSize(),
      color = pageBackgroundColor(mobilePlatform),
    ) {
      AnimatedContent(
        targetState = when {
          assistantOpen -> "assistant"
          selectedStation != null -> "station:${selectedStation.id}"
          else -> "tab:${currentTab.name}"
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
          assistantOpen -> AssistantScreen(
            mobilePlatform = mobilePlatform,
            graph = graph,
            stationsRepository = stationsRepository,
            favoriteIds = favoriteIds,
            searchRadiusMeters = searchRadiusMeters,
            stations = stationsState.stations,
            initialAction = pendingAssistantAction,
            onInitialActionConsumed = { pendingAssistantAction = null },
            onBack = { assistantOpen = false },
          )
          selectedStation != null -> StationDetailScreen(
            mobilePlatform = mobilePlatform,
            station = selectedStation,
            isFavorite = favoriteIds.contains(selectedStation.id),
            userLocation = stationsState.userLocation,
            onBack = { selectedStationId = null },
            onToggleFavorite = {
              scope.launch { favoritesRepository.toggle(selectedStation.id) }
            },
            onRoute = { graph.routeLauncher.launch(selectedStation) },
          )
          else -> Scaffold(
            containerColor = pageBackgroundColor(mobilePlatform),
            floatingActionButton = {
              if (mobilePlatform == MobileUiPlatform.Android) {
                ExtendedFloatingActionButton(
                  containerColor = BiziRed,
                  contentColor = Color.White,
                  onClick = { assistantOpen = true },
                  icon = {
                    Icon(Icons.Filled.KeyboardVoice, contentDescription = null)
                  },
                  text = {
                    Text("Atajos")
                  },
                )
              }
            },
            bottomBar = {
              MobileBottomNavigationBar(
                mobilePlatform = mobilePlatform,
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
              )
            },
          ) { innerPadding ->
            AnimatedContent(
              targetState = currentTab,
              transitionSpec = {
                (fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220)) { it / 14 })
                  .togetherWith(
                    fadeOut(animationSpec = tween(140)) + slideOutHorizontally(animationSpec = tween(140)) { -it / 16 },
                  )
              },
              label = "mobile-tab-transition",
            ) { tab ->
              when (tab) {
                MobileTab.Mapa -> DashboardScreen(
                  mobilePlatform = mobilePlatform,
                  currentTab = currentTab,
                  onTabSelected = { currentTab = it },
                  onOpenAssistant = { assistantOpen = true },
                  stations = filteredStations,
                  favoriteIds = favoriteIds,
                  loading = stationsState.isLoading,
                  errorMessage = stationsState.errorMessage,
                  nearestSelection = nearestSelection,
                  searchQuery = searchQuery,
                  searchRadiusMeters = searchRadiusMeters,
                  userLocation = stationsState.userLocation,
                  onSearchQueryChange = { searchQuery = it },
                  onStationSelected = { selectedStationId = it.id },
                  onRetry = { scope.launch { stationsRepository.loadIfNeeded() } },
                  onFavoriteToggle = { station ->
                    scope.launch { favoritesRepository.toggle(station.id) }
                  },
                  onQuickRoute = { station -> graph.routeLauncher.launch(station) },
                  paddingValues = innerPadding,
                )
                MobileTab.Favoritos -> FavoritesScreen(
                  mobilePlatform = mobilePlatform,
                  currentTab = currentTab,
                  onTabSelected = { currentTab = it },
                  onOpenAssistant = { assistantOpen = true },
                  allStations = stationsState.stations,
                  stations = favoriteStations,
                  homeStation = homeStation,
                  workStation = workStation,
                  searchQuery = searchQuery,
                  onSearchQueryChange = { searchQuery = it },
                  onStationSelected = { selectedStationId = it.id },
                  onAssignHomeStation = { station ->
                    scope.launch { favoritesRepository.setHomeStationId(station.id) }
                  },
                  onAssignWorkStation = { station ->
                    scope.launch { favoritesRepository.setWorkStationId(station.id) }
                  },
                  onClearHomeStation = {
                    scope.launch { favoritesRepository.setHomeStationId(null) }
                  },
                  onClearWorkStation = {
                    scope.launch { favoritesRepository.setWorkStationId(null) }
                  },
                  onQuickRoute = { station -> graph.routeLauncher.launch(station) },
                  paddingValues = innerPadding,
                )
                MobileTab.Perfil -> ProfileScreen(
                  mobilePlatform = mobilePlatform,
                  currentTab = currentTab,
                  onTabSelected = { currentTab = it },
                  onOpenAssistant = { assistantOpen = true },
                  paddingValues = innerPadding,
                  searchRadiusMeters = searchRadiusMeters,
                  preferredMapApp = preferredMapApp,
                  userLocation = stationsState.userLocation,
                  onSearchRadiusSelected = { radiusMeters ->
                    scope.launch { settingsRepository.setSearchRadiusMeters(radiusMeters) }
                  },
                  onPreferredMapAppSelected = { mapApp ->
                    scope.launch { settingsRepository.setPreferredMapApp(mapApp) }
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

@Composable
private fun BiziTheme(
  mobilePlatform: MobileUiPlatform,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = MaterialTheme.colorScheme.copy(
      primary = BiziRed,
      background = pageBackgroundColor(mobilePlatform),
      surface = Color.White,
      onSurface = BiziInk,
      onBackground = BiziInk,
      surfaceVariant = if (mobilePlatform == MobileUiPlatform.IOS) BiziPanel else BiziLight,
    ),
    content = content,
  )
}

@Composable
private fun MobileBottomNavigationBar(
  mobilePlatform: MobileUiPlatform,
  currentTab: MobileTab,
  onTabSelected: (MobileTab) -> Unit,
) {
  NavigationBar(
    containerColor = if (mobilePlatform == MobileUiPlatform.IOS) {
      Color.White.copy(alpha = 0.96f)
    } else {
      Color.White
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
private fun DashboardScreen(
  mobilePlatform: MobileUiPlatform,
  currentTab: MobileTab,
  onTabSelected: (MobileTab) -> Unit,
  onOpenAssistant: () -> Unit,
  stations: List<Station>,
  favoriteIds: Set<String>,
  loading: Boolean,
  errorMessage: String?,
  nearestSelection: com.gcaguilar.bizizaragoza.core.NearbyStationSelection,
  searchQuery: String,
  searchRadiusMeters: Int,
  userLocation: GeoPoint?,
  onSearchQueryChange: (String) -> Unit,
  onStationSelected: (Station) -> Unit,
  onRetry: () -> Unit,
  onFavoriteToggle: (Station) -> Unit,
  onQuickRoute: (Station) -> Unit,
  paddingValues: PaddingValues,
) {
  val nearestStation = nearestSelection.highlightedStation
  val nearestWithBikesSelection = remember(stations, searchRadiusMeters) {
    selectNearbyStation(stations, searchRadiusMeters) { station -> station.bikesAvailable > 0 }
  }
  val nearestWithSlotsSelection = remember(stations, searchRadiusMeters) {
    selectNearbyStation(stations, searchRadiusMeters) { station -> station.slotsFree > 0 }
  }
  var selectedMapStationId by rememberSaveable { mutableStateOf<String?>(null) }
  var hasExplicitMapSelection by rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(stations, nearestStation?.id) {
    val hasSelectedStation = selectedMapStationId != null && stations.any { station -> station.id == selectedMapStationId }
    if (!hasSelectedStation) {
      selectedMapStationId = nearestStation?.id
      hasExplicitMapSelection = false
    }
  }

  val selectedMapStation = remember(selectedMapStationId, stations, nearestStation) {
    selectedMapStationId?.let { id -> stations.firstOrNull { station -> station.id == id } } ?: nearestStation
  }
  val mapIsShowingNearestSelection = !hasExplicitMapSelection && selectedMapStation?.id == nearestStation?.id
  val mapIsShowingNearestFallback = selectedMapStation?.id == nearestStation?.id && nearestSelection.usesFallback
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
          title = "Bizi Zaragoza",
          subtitle = "Muévete con una vista clara de estaciones, favoritas y rutas. Radio actual: ${searchRadiusMeters} m.",
          onOpenAssistant = onOpenAssistant,
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Zaragoza Bizi",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BiziRed,
          )
          Text(
            text = "Estaciones cercanas, favoritas y rutas rápidas desde una sola vista.",
            style = MaterialTheme.typography.bodyMedium,
          )
          StationMetricPill(
            modifier = Modifier.fillMaxWidth(),
            label = "Radio cercano",
            value = "${searchRadiusMeters} m",
            tint = BiziBlue,
          )
        }
      }
    }
    item {
      MapHeroCard(
        mobilePlatform = mobilePlatform,
        stations = stations,
        userLocation = userLocation,
        selectedStation = selectedMapStation,
        isShowingNearestSelection = mapIsShowingNearestSelection,
        isFallbackSelection = mapIsShowingNearestFallback,
        searchRadiusMeters = searchRadiusMeters,
        onStationSelected = { station ->
          hasExplicitMapSelection = true
          selectedMapStationId = station.id
        },
        onOpenStationDetails = { station ->
          selectedMapStationId = station.id
          onStationSelected(station)
        },
        onQuickRoute = onQuickRoute,
      )
    }
    item {
      StationSearchField(
        mobilePlatform = mobilePlatform,
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        label = "Buscar estación o dirección",
      )
    }
    item {
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
          tint = BiziRed,
          mobilePlatform = mobilePlatform,
          onRoute = onQuickRoute,
        )
        QuickRouteActionCard(
          modifier = Modifier.weight(1f),
          title = "Más cercana con huecos",
          emptyTitle = "Sin huecos cercanos",
          selection = nearestWithSlotsSelection,
          icon = Icons.Filled.LocalParking,
          tint = BiziBlue,
          mobilePlatform = mobilePlatform,
          onRoute = onQuickRoute,
        )
      }
    }
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = if (loading) "Actualizando estaciones..." else "Cerca de ti",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = if (nearestSelection.usesFallback) {
            "Si no hay estaciones dentro del radio, te seguimos mostrando la opción más cercana."
          } else {
            "Toca cualquier tarjeta para abrir el detalle, guardar la estación o lanzar la ruta."
          },
          style = MaterialTheme.typography.bodySmall,
          color = BiziMuted,
        )
        AnimatedVisibility(
          visible = errorMessage != null,
          enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
          exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
          label = "dashboard-error",
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(errorMessage.orEmpty(), color = BiziRed)
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
        label = "dashboard-empty",
      ) {
        EmptyStateCard(
          title = "Todavía no tenemos estaciones en pantalla",
          description = "La app volverá a usar Zaragoza centro si la ubicación tarda demasiado o no está disponible.",
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

@Composable
private fun MapHeroCard(
  mobilePlatform: MobileUiPlatform,
  stations: List<Station>,
  userLocation: GeoPoint?,
  selectedStation: Station?,
  isShowingNearestSelection: Boolean,
  isFallbackSelection: Boolean,
  searchRadiusMeters: Int,
  onStationSelected: (Station) -> Unit,
  onOpenStationDetails: (Station) -> Unit,
  onQuickRoute: (Station) -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 24.dp else 28.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
  ) {
    Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
      PlatformStationMap(
        modifier = Modifier.fillMaxSize(),
        stations = stations.take(12),
        userLocation = userLocation,
        highlightedStationId = selectedStation?.id,
        onStationSelected = onStationSelected,
      )
      Box(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(16.dp),
      ) {
        selectedStation?.let { station ->
          val overlayContainer = if (mobilePlatform == MobileUiPlatform.IOS) Color.White else BiziRed
          val overlayTitle = if (mobilePlatform == MobileUiPlatform.IOS) BiziInk else Color.White
          val overlayBody = if (mobilePlatform == MobileUiPlatform.IOS) BiziMuted else Color.White.copy(alpha = 0.84f)
          Card(
            modifier = Modifier.animateContentSize(animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f)),
            shape = RoundedCornerShape(22.dp),
            border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, BiziRed.copy(alpha = 0.12f)) else null,
            colors = CardDefaults.cardColors(containerColor = overlayContainer),
          ) {
            Column(
              modifier = Modifier
                .padding(16.dp)
                .animateContentSize(animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f)),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                if (isFallbackSelection) {
                  "No hay dentro de ${searchRadiusMeters} m"
                } else if (isShowingNearestSelection) {
                  "Estación más cercana"
                } else {
                  "Estado en el mapa"
                },
                color = if (mobilePlatform == MobileUiPlatform.IOS) BiziRed else overlayBody,
              )
              Text(
                text = station.name,
                style = MaterialTheme.typography.headlineSmall,
                color = overlayTitle,
                fontWeight = FontWeight.Bold,
              )
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
                  label = if (mobilePlatform == MobileUiPlatform.IOS) "Maps" else "Ruta",
                  onClick = { onQuickRoute(station) },
                )
                OutlineActionPill(
                  label = "Detalle",
                  tint = if (mobilePlatform == MobileUiPlatform.IOS) BiziRed else Color.White,
                  borderTint = if (mobilePlatform == MobileUiPlatform.IOS) BiziRed.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.32f),
                  onClick = { onOpenStationDetails(station) },
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
private fun FavoritesScreen(
  mobilePlatform: MobileUiPlatform,
  currentTab: MobileTab,
  onTabSelected: (MobileTab) -> Unit,
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
          text = "Busca una estación y asígnala como Casa o Trabajo para pedirla luego con atajos de voz.",
          style = MaterialTheme.typography.bodySmall,
          color = BiziMuted,
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
        StationRow(
          mobilePlatform = mobilePlatform,
          station = station,
          isFavorite = true,
          onClick = { onStationSelected(station) },
          onFavoriteToggle = {},
          showFavoriteCta = false,
        )
      }
    }
  }
}

@Composable
private fun ProfileScreen(
  mobilePlatform: MobileUiPlatform,
  currentTab: MobileTab,
  onTabSelected: (MobileTab) -> Unit,
  onOpenAssistant: () -> Unit,
  paddingValues: PaddingValues,
  searchRadiusMeters: Int,
  preferredMapApp: PreferredMapApp,
  userLocation: GeoPoint?,
  onSearchRadiusSelected: (Int) -> Unit,
  onPreferredMapAppSelected: (PreferredMapApp) -> Unit,
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
              "Delegadas a Google Maps y Apple Maps."
            }
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
      ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text("Radio para estación cercana", fontWeight = FontWeight.SemiBold)
          Text(
            "Si no hay estaciones dentro del radio, la app mostrará igualmente la más cercana y te indicará la distancia.",
            style = MaterialTheme.typography.bodySmall,
            color = BiziMuted,
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
    if (mobilePlatform == MobileUiPlatform.IOS) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("App de mapas en iPhone", fontWeight = FontWeight.SemiBold)
            Text(
              "Elige qué app abrir para las rutas rápidas, el detalle de estación y los atajos de Siri.",
              style = MaterialTheme.typography.bodySmall,
              color = BiziMuted,
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
              color = BiziMuted,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StationDetailScreen(
  mobilePlatform: MobileUiPlatform,
  station: Station,
  isFavorite: Boolean,
  userLocation: GeoPoint?,
  onBack: () -> Unit,
  onToggleFavorite: () -> Unit,
  onRoute: () -> Unit,
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(pageBackgroundColor(mobilePlatform)),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      TextButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(if (mobilePlatform == MobileUiPlatform.IOS) "Volver al mapa" else "Volver")
      }
    }
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
          Text(station.address, style = MaterialTheme.typography.bodyMedium, color = BiziMuted)
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StationMetricPill(
              modifier = Modifier.weight(1f),
              label = "Distancia",
              value = "${station.distanceMeters} m",
              tint = BiziBlue,
            )
            StationMetricPill(
              modifier = Modifier.weight(1f),
              label = "Fuente",
              value = station.sourceLabel,
              tint = BiziMuted,
            )
          }
        }
      }
    }
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
      ) {
        PlatformStationMap(
          modifier = Modifier.fillMaxWidth().height(200.dp),
          stations = listOf(station),
          userLocation = userLocation,
          highlightedStationId = station.id,
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
          tint = BiziRed,
          mobilePlatform = mobilePlatform,
        )
        AvailabilityCard(
          modifier = Modifier.weight(1f),
          label = "Huecos",
          value = station.slotsFree.toString(),
          icon = Icons.Filled.LocalParking,
          tint = BiziBlue,
          mobilePlatform = mobilePlatform,
        )
      }
    }
    item {
      Button(onClick = onRoute, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Filled.Directions, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Abrir ruta en mapas")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssistantScreen(
  mobilePlatform: MobileUiPlatform,
  graph: SharedGraph,
  stationsRepository: com.gcaguilar.bizizaragoza.core.StationsRepository,
  favoriteIds: Set<String>,
  searchRadiusMeters: Int,
  stations: List<Station>,
  initialAction: AssistantAction?,
  onInitialActionConsumed: () -> Unit,
  onBack: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  var latestAnswer by rememberSaveable { mutableStateOf("Pregunta por estaciones cercanas, favoritas o rutas.") }
  val suggestions = listOf(
    AssistantAction.NearestStation,
    AssistantAction.NearestStationWithBikes,
    AssistantAction.NearestStationWithSlots,
    AssistantAction.FavoriteStations,
    stations.firstOrNull()?.let { AssistantAction.RouteToStation(it.id) },
  ).filterNotNull()

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
      .background(pageBackgroundColor(mobilePlatform)),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      TextButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Cerrar")
      }
    }
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Atajos y asistentes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
          "Usa acciones rápidas del sistema para resolver consultas sin navegar por toda la app. Radio actual: ${searchRadiusMeters} m.",
          style = MaterialTheme.typography.bodyMedium,
          color = BiziMuted,
        )
      }
    }
    item {
      Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
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
    items(suggestions, key = { it.label() }) { action ->
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
        Color.White
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
  showFavoriteCta: Boolean = true,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .animateContentSize(animationSpec = spring(dampingRatio = 0.9f, stiffness = 520f)),
    shape = RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 22.dp else 24.dp),
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, BiziPanel) else null,
    colors = CardDefaults.cardColors(containerColor = Color.White),
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
            color = BiziMuted,
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
              label = if (mobilePlatform == MobileUiPlatform.IOS) "Maps" else "Ruta",
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
          tint = BiziRed,
        )
        StationMetricPill(
          modifier = Modifier.weight(1f),
          label = "Huecos",
          value = station.slotsFree.toString(),
          tint = BiziBlue,
        )
        StationMetricPill(
          modifier = Modifier.weight(1f),
          label = "Distancia",
          value = "${station.distanceMeters} m",
          tint = BiziGreen,
        )
      }
    }
  }
}

@Composable
private fun RoutePill(
  label: String,
  onClick: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = BiziBlue.copy(alpha = 0.08f),
    border = BorderStroke(1.dp, BiziBlue.copy(alpha = 0.16f)),
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
        tint = BiziBlue,
        modifier = Modifier.size(16.dp),
      )
      Text(label, color = BiziBlue, style = MaterialTheme.typography.labelMedium)
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
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, BiziPanel) else null,
    colors = CardDefaults.cardColors(containerColor = Color.White),
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
            color = BiziMuted,
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          StationMetricPill(
            modifier = Modifier.weight(1f),
            label = "Bicis",
            value = station.bikesAvailable.toString(),
            tint = BiziRed,
          )
          StationMetricPill(
            modifier = Modifier.weight(1f),
            label = "Huecos",
            value = station.slotsFree.toString(),
            tint = BiziBlue,
          )
          StationMetricPill(
            modifier = Modifier.weight(1f),
            label = "Distancia",
            value = "${station.distanceMeters} m",
            tint = BiziGreen,
          )
        }
      } else {
        Text(
          text = "Todavía no has fijado una estación para $title.",
          style = MaterialTheme.typography.bodySmall,
          color = BiziMuted,
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (station != null) {
          RoutePill(
            label = if (mobilePlatform == MobileUiPlatform.IOS) "Maps" else "Ruta",
            onClick = { onQuickRoute(station) },
          )
          OutlineActionPill(
            label = "Detalle",
            tint = BiziRed,
            borderTint = BiziRed.copy(alpha = 0.16f),
            onClick = { onOpenStationDetails(station) },
          )
        }
        if (assignableCandidate != null) {
          OutlineActionPill(
            label = "Asignar búsqueda",
            tint = BiziBlue,
            borderTint = BiziBlue.copy(alpha = 0.16f),
            onClick = { onAssignCandidate(assignableCandidate) },
          )
        }
        if (station != null) {
          OutlineActionPill(
            label = "Quitar",
            tint = BiziMuted,
            borderTint = BiziPanel,
            onClick = onClear,
          )
        }
      }
      if (assignableCandidate != null) {
        Text(
          text = "La búsqueda actual apunta a ${assignableCandidate.name}. Se usará para $title si pulsas asignar.",
          style = MaterialTheme.typography.bodySmall,
          color = BiziMuted,
        )
      } else if (station == null) {
        Text(
          text = "Usa el buscador de arriba para elegir una estación y asignarla.",
          style = MaterialTheme.typography.bodySmall,
          color = BiziMuted,
        )
      }
    }
  }
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
          color = BiziMuted,
        )
      }
    }
    Spacer(Modifier.width(12.dp))
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = BiziRed.copy(alpha = 0.10f),
      border = BorderStroke(1.dp, BiziRed.copy(alpha = 0.12f)),
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
          tint = BiziRed,
        )
        Text("Atajos", color = BiziRed, fontWeight = FontWeight.SemiBold)
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
      Icon(Icons.Filled.Search, contentDescription = null, tint = BiziMuted)
    },
    trailingIcon = if (value.isNotEmpty()) {
      {
        Icon(
          imageVector = Icons.Filled.Close,
          contentDescription = "Limpiar búsqueda",
          tint = BiziMuted,
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
      focusedContainerColor = if (mobilePlatform == MobileUiPlatform.IOS) Color.White else BiziPanel.copy(alpha = 0.35f),
      unfocusedContainerColor = if (mobilePlatform == MobileUiPlatform.IOS) Color.White else BiziPanel.copy(alpha = 0.35f),
      focusedBorderColor = BiziRed.copy(alpha = if (mobilePlatform == MobileUiPlatform.IOS) 0.18f else 0.30f),
      unfocusedBorderColor = if (mobilePlatform == MobileUiPlatform.IOS) BiziPanel else BiziMuted.copy(alpha = 0.18f),
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
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, BiziPanel) else null,
    colors = CardDefaults.cardColors(containerColor = Color.White),
  ) {
    Column(
      modifier = Modifier
        .padding(14.dp)
        .animateContentSize(animationSpec = spring(dampingRatio = 0.88f, stiffness = 520f)),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
      Text(title, style = MaterialTheme.typography.labelSmall, color = BiziMuted)
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
          color = BiziMuted,
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
          color = BiziMuted,
        )
        Text(
          if (mobilePlatform == MobileUiPlatform.IOS) "Abrir en Maps" else "Abrir ruta",
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
    colors = CardDefaults.cardColors(containerColor = Color.White),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(title, fontWeight = FontWeight.SemiBold)
      Text(description, style = MaterialTheme.typography.bodySmall, color = BiziMuted)
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
    targetValue = if (selected) BiziRed.copy(alpha = 0.10f) else Color.White,
    animationSpec = tween(180),
    label = "radius-container",
  )
  val borderColor by animateColorAsState(
    targetValue = if (selected) BiziRed.copy(alpha = 0.25f) else BiziPanel,
    animationSpec = tween(180),
    label = "radius-border",
  )
  val textColor by animateColorAsState(
    targetValue = if (selected) BiziRed else BiziInk,
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
private fun InfoLine(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: () -> String,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Icon(icon, contentDescription = null, tint = BiziRed)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(label, style = MaterialTheme.typography.labelMedium, color = BiziMuted)
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
    targetValue = if (active) BiziRed.copy(alpha = 0.10f) else Color.Transparent,
    animationSpec = tween(180),
    label = "favorite-pill-container",
  )
  val borderColor by animateColorAsState(
    targetValue = if (active) BiziRed.copy(alpha = 0.16f) else BiziPanel,
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
        tint = BiziRed,
        modifier = Modifier.size(16.dp),
      )
      Text(label, color = BiziRed, style = MaterialTheme.typography.labelMedium)
    }
  }
}

private fun pageBackgroundColor(platform: MobileUiPlatform): Color =
  if (platform == MobileUiPlatform.IOS) BiziGrouped else BiziLight

private fun MobileTab.icon() = when (this) {
  MobileTab.Mapa -> Icons.Filled.Map
  MobileTab.Favoritos -> Icons.Filled.Favorite
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
