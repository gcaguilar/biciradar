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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.Text
import com.gcaguilar.biciradar.core.AppConfiguration
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.platform.AndroidPlatformBindings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Paleta BiciRadar para WearOS
private val WearPrimary = Color(0xFF1D74BD)
private val WearSecondary = Color(0xFF64C23A)
private val WearTertiary = Color(0xFF0D1B2A)
private val WearNeutral = Color(0xFF64779D)
private val WearError = Color(0xFFCF6679)
private val WearSurface = Color(0xFF1A1A2E)
private val WearOnSurface = Color(0xFFE8EDF4)

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

  LaunchedEffect(graph, refreshKey) {
    favoritesRepository.bootstrap()
    stationsRepository.loadIfNeeded()
  }

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

        stationsState.isLoading -> CircularProgressIndicator(
          colors = ProgressIndicatorDefaults.colors(indicatorColor = WearPrimary),
        )

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
    item {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        WearTab.entries.forEach { tab ->
          val isSelected = currentTab == tab
          Button(
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(tab) },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isSelected) WearPrimary else WearSurface,
            ),
          ) {
            Text(
              text = tab.name,
              style = MaterialTheme.typography.labelSmall,
              color = WearOnSurface,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
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
          color = WearNeutral,
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.bodySmall,
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
      Button(
        onClick = onRefresh,
        colors = ButtonDefaults.buttonColors(containerColor = WearSurface),
      ) {
        Text("↻  Actualizar", style = MaterialTheme.typography.labelSmall, color = WearNeutral)
      }
    }
  }
}

@Composable
private fun WearAvailabilityDot(available: Int) {
  val color = when {
    available == 0 -> WearError
    available <= 2 -> Color(0xFFF28000)
    else -> WearSecondary
  }
  Box(
    modifier = Modifier
      .size(8.dp)
      .clip(CircleShape)
      .background(color),
  )
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
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
      Text(
        text = if (isFavorite) "★ ${station.name}" else station.name,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = WearOnSurface,
      )
      Spacer(Modifier.height(4.dp))
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        WearChip(label = "${station.distanceMeters} m", color = WearNeutral)
        WearChip(label = "🚲 ${station.bikesAvailable}", color = WearPrimary)
        WearChip(label = "🅿 ${station.slotsFree}", color = WearSecondary)
      }
    }
  }
}

@Composable
private fun WearChip(label: String, color: Color) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(color.copy(alpha = 0.18f))
      .padding(horizontal = 6.dp, vertical = 2.dp),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = color,
      fontWeight = FontWeight.Medium,
    )
  }
}

@Composable
private fun WearStatBadge(label: String, value: Int, positiveColor: Color) {
  val valueColor = if (value > 0) positiveColor else WearError
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(valueColor.copy(alpha = 0.15f))
      .padding(horizontal = 10.dp, vertical = 6.dp),
  ) {
    Text(
      text = "$value",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = valueColor,
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = WearNeutral,
    )
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
        color = WearOnSurface,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
      )
    }
    item {
      Text(
        text = station.address,
        style = MaterialTheme.typography.bodySmall,
        color = WearNeutral,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
      )
    }
    item {
      Text(
        text = "${station.distanceMeters} m",
        style = MaterialTheme.typography.bodySmall,
        color = WearNeutral,
      )
    }
    item {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 4.dp),
      ) {
        WearStatBadge(
          label = "Bicis",
          value = station.bikesAvailable,
          positiveColor = WearPrimary,
        )
        WearStatBadge(
          label = "Huecos",
          value = station.slotsFree,
          positiveColor = WearSecondary,
        )
      }
    }
    item {
      Button(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        onClick = onRoute,
        colors = ButtonDefaults.buttonColors(containerColor = WearPrimary),
      ) {
        Text("Abrir ruta", style = MaterialTheme.typography.labelSmall, color = Color.White)
      }
    }
    item {
      Button(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        onClick = onToggleFavorite,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isFavorite) WearSurface else WearSecondary.copy(alpha = 0.25f),
        ),
      ) {
        Text(
          text = if (isFavorite) "★ Quitar favorita" else "☆ Añadir favorita",
          style = MaterialTheme.typography.labelSmall,
          color = if (isFavorite) WearNeutral else WearSecondary,
        )
      }
    }
    item {
      Button(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        onClick = onBack,
        colors = ButtonDefaults.buttonColors(containerColor = WearSurface),
      ) {
        Text("← Volver", style = MaterialTheme.typography.labelSmall, color = WearNeutral)
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
      color = WearError,
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.bodySmall,
    )
    Spacer(Modifier.height(8.dp))
    Button(
      onClick = onRetry,
      colors = ButtonDefaults.buttonColors(containerColor = WearPrimary),
    ) {
      Text("Reintentar", color = Color.White)
    }
  }
}
