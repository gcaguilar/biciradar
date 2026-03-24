package com.gcaguilar.biciradar.wear

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.gcaguilar.biciradar.core.AppConfiguration
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.platform.AndroidPlatformBindings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WearActivity : ComponentActivity() {
  private val locationPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions(),
  ) {
    refreshNonce += 1
  }

  private var refreshNonce by mutableIntStateOf(0)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val platformBindings = AndroidPlatformBindings(
      context = applicationContext,
      appConfiguration = AppConfiguration(),
    )
    setContent {
      WearRoot(platformBindings, refreshNonce)
    }
    locationPermissionLauncher.launch(
      arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
      ),
    )
  }
}

private enum class WearTab { Cercanas, Favoritas }

@Composable
private fun WearRoot(
  platformBindings: PlatformBindings,
  refreshKey: Int,
) {
  val graph = remember(platformBindings) {
    SharedGraph.Companion.create(platformBindings)
  }
  val stationsRepository = remember(graph) { graph.stationsRepository }
  val favoritesRepository = remember(graph) { graph.favoritesRepository }
  val stationsState by stationsRepository.state.collectAsState()
  val favoriteIds by favoritesRepository.favoriteIds.collectAsState()
  val scope = rememberCoroutineScope()
  var selectedStationId by rememberSaveable { mutableStateOf<String?>(null) }
  var currentTab by rememberSaveable { mutableStateOf(WearTab.Cercanas) }

  // Carga inicial
  LaunchedEffect(graph, refreshKey) {
    favoritesRepository.bootstrap()
    stationsRepository.loadIfNeeded()
  }

  // Refresco periódico de disponibilidad cada 30s
  LaunchedEffect(graph) {
    while (true) {
      delay(30_000)
      val ids = stationsRepository.state.value.stations.take(10).map { it.id }
      stationsRepository.refreshAvailability(ids)
    }
  }

  MaterialTheme {
    Box(
      modifier = Modifier.fillMaxSize().background(Color.Black),
      contentAlignment = Alignment.Center,
    ) {
      val selectedStation = selectedStationId?.let { stationsRepository.stationById(it) }

      when {
        selectedStation != null -> WearStationDetail(
          station = selectedStation,
          isFavorite = selectedStation.id in favoriteIds,
          onBack = { selectedStationId = null },
          onToggleFavorite = { scope.launch { favoritesRepository.toggle(selectedStation.id) } },
          onRoute = { graph.routeLauncher.launch(selectedStation) },
        )

        stationsState.isLoading -> CircularProgressIndicator()

        stationsState.errorMessage != null -> WearErrorScreen(
          message = stationsState.errorMessage!!,
          onRetry = { scope.launch { stationsRepository.loadIfNeeded() } },
        )

        else -> WearDashboard(
          stations = stationsState.stations,
          favoriteIds = favoriteIds,
          currentTab = currentTab,
          onTabSelected = { currentTab = it },
          onStationSelected = { selectedStationId = it.id },
          onRefresh = {
            scope.launch {
              val ids = stationsState.stations.take(10).map { it.id }
              stationsRepository.refreshAvailability(ids)
            }
          },
        )
      }
    }
  }
}

@Composable
private fun WearDashboard(
  stations: List<Station>,
  favoriteIds: Set<String>,
  currentTab: WearTab,
  onTabSelected: (WearTab) -> Unit,
  onStationSelected: (Station) -> Unit,
  onRefresh: () -> Unit,
) {
  val nearbyStations = stations.take(8)
  val favoriteStations = stations.filter { it.id in favoriteIds }
  val listState = rememberScalingLazyListState()

  ScalingLazyColumn(
    modifier = Modifier.fillMaxSize(),
    state = listState,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // Tabs
    item {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        WearTab.entries.forEach { tab ->
          Button(
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(tab) },
          ) {
            Text(
              text = tab.name,
              style = MaterialTheme.typography.labelSmall,
              color = if (currentTab == tab) Color.White else Color.Gray,
            )
          }
        }
      }
    }

    val displayedStations = when (currentTab) {
      WearTab.Cercanas -> nearbyStations
      WearTab.Favoritas -> favoriteStations
    }

    if (displayedStations.isEmpty()) {
      item {
        Text(
          text = when (currentTab) {
            WearTab.Cercanas -> "No hay estaciones cercanas."
            WearTab.Favoritas -> "No tienes favoritas aún."
          },
          color = Color.Gray,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(16.dp),
        )
      }
    } else {
      items(displayedStations, key = { it.id }) { station ->
        WearStationRow(
          station = station,
          isFavorite = station.id in favoriteIds,
          onClick = { onStationSelected(station) },
        )
      }
    }

    item {
      Spacer(Modifier.height(4.dp))
      Button(onClick = onRefresh) {
        Text("Actualizar", style = MaterialTheme.typography.labelSmall)
      }
    }
  }
}

@Composable
private fun WearStationRow(
  station: Station,
  isFavorite: Boolean,
  onClick: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
    onClick = onClick,
  ) {
    Column(modifier = Modifier.padding(8.dp)) {
      Text(
        text = if (isFavorite) "★ ${station.name}" else station.name,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = "${station.distanceMeters} m · ${station.bikesAvailable} bicis · ${station.slotsFree} huecos",
        style = MaterialTheme.typography.bodySmall,
        color = Color.Gray,
      )
    }
  }
}

@Composable
private fun WearStationDetail(
  station: Station,
  isFavorite: Boolean,
  onBack: () -> Unit,
  onToggleFavorite: () -> Unit,
  onRoute: () -> Unit,
) {
  val listState = rememberScalingLazyListState()
  ScalingLazyColumn(
    modifier = Modifier.fillMaxSize(),
    state = listState,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    item {
      Text(
        text = station.name,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
      )
    }
    item {
      Text(
        text = station.address,
        style = MaterialTheme.typography.bodySmall,
        color = Color.Gray,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
      )
    }
    item {
      Text(
        text = "${station.distanceMeters} m",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White,
      )
    }
    item {
      Text(
        text = "${station.bikesAvailable} bicis disponibles",
        style = MaterialTheme.typography.bodyMedium,
        color = if (station.bikesAvailable > 0) Color(0xFF4CAF50) else Color.Red,
      )
    }
    item {
      Text(
        text = "${station.slotsFree} huecos libres",
        style = MaterialTheme.typography.bodyMedium,
        color = if (station.slotsFree > 0) Color(0xFF4CAF50) else Color.Red,
      )
    }
    item {
      Button(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        onClick = onRoute,
      ) {
        Text("Abrir ruta", style = MaterialTheme.typography.labelSmall)
      }
    }
    item {
      Button(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        onClick = onToggleFavorite,
      ) {
        Text(
          text = if (isFavorite) "Quitar favorita" else "Añadir favorita",
          style = MaterialTheme.typography.labelSmall,
        )
      }
    }
    item {
      Button(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        onClick = onBack,
      ) {
        Text("Volver", style = MaterialTheme.typography.labelSmall)
      }
    }
  }
}

@Composable
private fun WearErrorScreen(
  message: String,
  onRetry: () -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = message,
      color = Color.Red,
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.bodySmall,
    )
    Spacer(Modifier.height(8.dp))
    Button(onClick = onRetry) {
      Text("Reintentar")
    }
  }
}
