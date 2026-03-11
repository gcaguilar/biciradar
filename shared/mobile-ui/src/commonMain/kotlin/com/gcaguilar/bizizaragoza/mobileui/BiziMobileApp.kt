package com.gcaguilar.bizizaragoza.mobileui

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gcaguilar.bizizaragoza.core.AssistantAction
import com.gcaguilar.bizizaragoza.core.GeminiPromptRequest
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.PlatformBindings
import com.gcaguilar.bizizaragoza.core.SharedGraph
import com.gcaguilar.bizizaragoza.core.Station
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val BiziRed = Color(0xFFD7191F)
private val BiziLight = Color(0xFFF8F6F6)
private val BiziInk = Color(0xFF211111)

private enum class MobileTab(val label: String) {
  Mapa("Mapa"),
  Favoritos("Favoritos"),
  Perfil("Perfil"),
}

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

@Composable
fun BiziMobileApp(
  platformBindings: PlatformBindings,
  modifier: Modifier = Modifier,
  refreshKey: Any? = Unit,
  launchRequest: MobileLaunchRequest? = null,
) {
  val graph = remember(platformBindings) {
    createGraphFactory<SharedGraph.Factory>().create(platformBindings)
  }
  val scope = rememberCoroutineScope()
  val stationsState by graph.stationsRepository.state.collectAsState()
  val favoriteIds by graph.favoritesRepository.favoriteIds.collectAsState()
  var currentTab by rememberSaveable { mutableStateOf(MobileTab.Mapa) }
  var selectedStationId by rememberSaveable { mutableStateOf<String?>(null) }
  var assistantOpen by rememberSaveable { mutableStateOf(false) }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var pendingAssistantAction by remember { mutableStateOf<AssistantAction?>(null) }
  var pendingLaunchRequest by remember { mutableStateOf<MobileLaunchRequest?>(null) }

  LaunchedEffect(graph, refreshKey) {
    graph.favoritesRepository.bootstrap()
    graph.stationsRepository.refresh()
  }

  LaunchedEffect(launchRequest) {
    pendingLaunchRequest = launchRequest
  }

  LaunchedEffect(pendingLaunchRequest, stationsState.stations) {
    when (val request = pendingLaunchRequest ?: return@LaunchedEffect) {
      MobileLaunchRequest.Favorites -> {
        currentTab = MobileTab.Favoritos
        pendingLaunchRequest = null
      }
      MobileLaunchRequest.NearestStation -> {
        val station = stationsState.stations.firstOrNull() ?: return@LaunchedEffect
        selectedStationId = station.id
        currentTab = MobileTab.Mapa
        pendingLaunchRequest = null
      }
      MobileLaunchRequest.NearestStationWithBikes -> {
        val station = stationsState.stations.firstOrNull { station -> station.bikesAvailable > 0 }
          ?: return@LaunchedEffect
        selectedStationId = station.id
        currentTab = MobileTab.Mapa
        pendingLaunchRequest = null
      }
      MobileLaunchRequest.NearestStationWithSlots -> {
        val station = stationsState.stations.firstOrNull { station -> station.slotsFree > 0 }
          ?: return@LaunchedEffect
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
        val station = request.stationId?.let(graph.stationsRepository::stationById)
          ?: stationsState.stations.firstOrNull()
          ?: return@LaunchedEffect
        selectedStationId = station.id
        graph.routeLauncher.launch(station)
        pendingLaunchRequest = null
      }
      is MobileLaunchRequest.ShowStation -> {
        if (graph.stationsRepository.stationById(request.stationId) == null) return@LaunchedEffect
        selectedStationId = request.stationId
        currentTab = MobileTab.Mapa
        pendingLaunchRequest = null
      }
    }
  }

  val filteredStations = remember(stationsState.stations, searchQuery) {
    filterStations(stationsState.stations, searchQuery)
  }
  val favoriteStations = remember(filteredStations, favoriteIds) {
    filteredStations.filter { station -> station.id in favoriteIds }
  }
  val selectedStation = remember(selectedStationId, stationsState.stations) {
    selectedStationId?.let(graph.stationsRepository::stationById)
  }

  BiziTheme {
    Surface(modifier = modifier.fillMaxSize(), color = BiziLight) {
      when {
        assistantOpen -> AssistantScreen(
          graph = graph,
          favoriteIds = favoriteIds,
          stations = stationsState.stations,
          initialAction = pendingAssistantAction,
          onInitialActionConsumed = { pendingAssistantAction = null },
          onBack = { assistantOpen = false },
        )
        selectedStation != null -> StationDetailScreen(
          station = selectedStation,
          isFavorite = favoriteIds.contains(selectedStation.id),
          userLocation = stationsState.userLocation,
          onBack = { selectedStationId = null },
          onToggleFavorite = {
            scope.launch { graph.favoritesRepository.toggle(selectedStation.id) }
          },
          onRoute = { graph.routeLauncher.launch(selectedStation) },
        )
        else -> Scaffold(
          floatingActionButton = {
            FloatingActionButton(
              containerColor = BiziRed,
              contentColor = Color.White,
              onClick = { assistantOpen = true },
            ) {
              Text("IA")
            }
          },
          bottomBar = {
            NavigationBar(containerColor = Color.White) {
              MobileTab.entries.forEach { tab ->
                NavigationBarItem(
                  selected = currentTab == tab,
                  onClick = { currentTab = tab },
                  icon = {
                    Box(
                      modifier = Modifier
                        .size(10.dp)
                        .background(if (currentTab == tab) BiziRed else Color.LightGray, CircleShape),
                    )
                  },
                  label = { Text(tab.label) },
                )
              }
            }
          },
        ) { innerPadding ->
          when (currentTab) {
            MobileTab.Mapa -> DashboardScreen(
              stations = filteredStations,
              favoriteIds = favoriteIds,
              loading = stationsState.isLoading,
              errorMessage = stationsState.errorMessage,
              searchQuery = searchQuery,
              userLocation = stationsState.userLocation,
              onSearchQueryChange = { searchQuery = it },
              onStationSelected = { selectedStationId = it.id },
              onFavoriteToggle = { station ->
                scope.launch { graph.favoritesRepository.toggle(station.id) }
              },
              paddingValues = innerPadding,
            )
            MobileTab.Favoritos -> FavoritesScreen(
              stations = favoriteStations,
              searchQuery = searchQuery,
              onSearchQueryChange = { searchQuery = it },
              onStationSelected = { selectedStationId = it.id },
              paddingValues = innerPadding,
            )
            MobileTab.Perfil -> ProfileScreen(
              paddingValues = innerPadding,
              userLocation = stationsState.userLocation,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BiziTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = MaterialTheme.colorScheme.copy(
      primary = BiziRed,
      background = BiziLight,
      surface = Color.White,
      onSurface = BiziInk,
      onBackground = BiziInk,
    ),
    content = content,
  )
}

@Composable
private fun DashboardScreen(
  stations: List<Station>,
  favoriteIds: Set<String>,
  loading: Boolean,
  errorMessage: String?,
  searchQuery: String,
  userLocation: GeoPoint?,
  onSearchQueryChange: (String) -> Unit,
  onStationSelected: (Station) -> Unit,
  onFavoriteToggle: (Station) -> Unit,
  paddingValues: PaddingValues,
) {
  val nearestStation = stations.firstOrNull()
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(BiziLight),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Text(
        text = "Zaragoza Bizi",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = BiziRed,
      )
      Spacer(Modifier.height(8.dp))
      Text(
        text = "Estaciones cercanas, favoritos y rutas rápidas desde una sola vista.",
        style = MaterialTheme.typography.bodyMedium,
      )
    }
    item {
      MapHeroCard(
        stations = stations,
        userLocation = userLocation,
        nearestStation = nearestStation,
        onStationSelected = onStationSelected,
      )
    }
    item {
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        label = { Text("Buscar estación o dirección") },
      )
    }
    item {
      Text(
        text = if (loading) "Actualizando estaciones..." else "Estaciones disponibles",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
      )
      if (errorMessage != null) {
        Spacer(Modifier.height(8.dp))
        Text(errorMessage, color = BiziRed)
      }
    }
    items(stations.take(10), key = { it.id }) { station ->
      StationRow(
        station = station,
        isFavorite = station.id in favoriteIds,
        onClick = { onStationSelected(station) },
        onFavoriteToggle = { onFavoriteToggle(station) },
      )
    }
  }
}

@Composable
private fun MapHeroCard(
  stations: List<Station>,
  userLocation: GeoPoint?,
  nearestStation: Station?,
  onStationSelected: (Station) -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
  ) {
    Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
      PlatformStationMap(
        modifier = Modifier.fillMaxSize(),
        stations = stations.take(12),
        userLocation = userLocation,
        highlightedStationId = nearestStation?.id,
        onStationSelected = onStationSelected,
      )
      nearestStation?.let { station ->
        Card(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(16.dp),
          colors = CardDefaults.cardColors(containerColor = BiziRed),
        ) {
          Column(Modifier.padding(16.dp)) {
            Text("Estación más cercana", color = Color.White.copy(alpha = 0.72f))
            Text(
              text = station.name,
              style = MaterialTheme.typography.headlineSmall,
              color = Color.White,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "${station.distanceMeters} m · ${station.bikesAvailable} bicis · ${station.slotsFree} libres",
              color = Color.White,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun FavoritesScreen(
  stations: List<Station>,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onStationSelected: (Station) -> Unit,
  paddingValues: PaddingValues,
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Text(
        text = "Mis estaciones",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
      Spacer(Modifier.height(8.dp))
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        label = { Text("Filtrar favoritas") },
      )
    }
    if (stations.isEmpty()) {
      item {
        Card {
          Text(
            text = "Guarda estaciones desde el mapa para tenerlas siempre a mano.",
            modifier = Modifier.padding(16.dp),
          )
        }
      }
    } else {
      items(stations, key = { it.id }) { station ->
        StationRow(
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
  paddingValues: PaddingValues,
  userLocation: GeoPoint?,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text("Perfil y ajustes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Card {
      Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Idioma inicial: Español")
        Text("Rutas: delegadas a Google Maps y Apple Maps")
        Text("Atajos: App Actions, App Shortcuts y Siri/App Intents")
        Text(
          if (userLocation != null) {
            "Ubicación activa: ${userLocation.latitude.formatCoordinate()}, ${userLocation.longitude.formatCoordinate()}"
          } else {
            "Ubicación pendiente. Se usa el centro de Zaragoza como fallback."
          },
        )
      }
    }
  }
}

@Composable
private fun StationDetailScreen(
  station: Station,
  isFavorite: Boolean,
  userLocation: GeoPoint?,
  onBack: () -> Unit,
  onToggleFavorite: () -> Unit,
  onRoute: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    TextButton(onClick = onBack) { Text("Volver") }
    Text(station.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text(station.address, style = MaterialTheme.typography.bodyLarge)
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
      PlatformStationMap(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        stations = listOf(station),
        userLocation = userLocation,
        highlightedStationId = station.id,
        onStationSelected = {},
      )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      AvailabilityCard(
        modifier = Modifier.fillMaxWidth(0.48f),
        label = "Bicis",
        value = station.bikesAvailable.toString(),
        tint = BiziRed,
      )
      AvailabilityCard(
        modifier = Modifier.fillMaxWidth(0.48f),
        label = "Libres",
        value = station.slotsFree.toString(),
        tint = Color(0xFF475569),
      )
    }
    Card {
      Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Distancia aproximada: ${station.distanceMeters} metros")
        Text("Fuente: ${station.sourceLabel}")
      }
    }
    Button(onClick = onRoute, modifier = Modifier.fillMaxWidth()) {
      Text("Ir con mapas")
    }
    OutlinedButton(onClick = onToggleFavorite, modifier = Modifier.fillMaxWidth()) {
      Text(if (isFavorite) "Quitar de favoritos" else "Guardar en favoritos")
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssistantScreen(
  graph: SharedGraph,
  favoriteIds: Set<String>,
  stations: List<Station>,
  initialAction: AssistantAction?,
  onInitialActionConsumed: () -> Unit,
  onBack: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  var prompt by rememberSaveable { mutableStateOf("¿Qué estación tiene más bicis cerca?") }
  var latestAnswer by rememberSaveable { mutableStateOf("Pregunta por estaciones cercanas, favoritos o rutas.") }
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
      stationsState = graph.stationsRepository.state.value,
      favoriteIds = favoriteIds,
    )
    latestAnswer = resolution.spokenResponse
    onInitialActionConsumed()
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    TextButton(onClick = onBack) { Text("Cerrar") }
    Text("Gemini y asistentes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    OutlinedTextField(
      modifier = Modifier.fillMaxWidth(),
      value = prompt,
      onValueChange = { prompt = it },
      label = { Text("Consulta") },
    )
    Button(
      modifier = Modifier.fillMaxWidth(),
      onClick = {
        scope.launch {
          latestAnswer = graph.geminiPromptService.prompt(GeminiPromptRequest(prompt = prompt)).answer
        }
      },
    ) {
      Text("Consultar Gemini")
    }
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
      Text(latestAnswer, modifier = Modifier.padding(16.dp))
    }
    Text("Sugerencias")
    suggestions.forEach { action ->
      OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
          scope.launch {
            val resolution = graph.assistantIntentResolver.resolve(
              action = action,
              stationsState = graph.stationsRepository.state.value,
              favoriteIds = favoriteIds,
            )
            latestAnswer = resolution.spokenResponse
          }
        },
      ) {
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
  tint: Color,
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.08f)),
  ) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(label, color = tint)
      Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    }
  }
}

@Composable
private fun StationRow(
  station: Station,
  isFavorite: Boolean,
  onClick: () -> Unit,
  onFavoriteToggle: () -> Unit,
  showFavoriteCta: Boolean = true,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    colors = CardDefaults.cardColors(containerColor = Color.White),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          station.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text("${station.bikesAvailable} bicis · ${station.slotsFree} libres · ${station.distanceMeters} m")
        Text(station.address, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
      }
      Spacer(Modifier.width(12.dp))
      Column(horizontalAlignment = Alignment.End) {
        if (showFavoriteCta) {
          TextButton(onClick = onFavoriteToggle) {
            Text(if (isFavorite) "Guardada" else "Guardar", color = BiziRed)
          }
        } else {
          Text("Favorita", color = BiziRed)
        }
      }
    }
  }
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
): List<Station> {
  if (searchQuery.isBlank()) return stations
  val normalizedQuery = searchQuery.trim().lowercase()
  return stations.filter { station ->
    station.name.lowercase().contains(normalizedQuery) ||
      station.address.lowercase().contains(normalizedQuery)
  }
}

private fun Double.formatCoordinate(): String = ((this * 10_000).roundToInt() / 10_000.0).toString()
