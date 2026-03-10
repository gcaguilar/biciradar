package com.gcaguilar.bizizaragoza.wear

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.gcaguilar.bizizaragoza.core.AppConfiguration
import com.gcaguilar.bizizaragoza.core.PlatformBindings
import com.gcaguilar.bizizaragoza.core.SharedGraph
import com.gcaguilar.bizizaragoza.core.platform.AndroidPlatformBindings
import dev.zacsweers.metro.createGraphFactory
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
      appConfiguration = AppConfiguration(
        geminiProxyBaseUrl = BuildConfig.GEMINI_PROXY_BASE_URL,
      ),
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

@Composable
private fun WearRoot(
  platformBindings: PlatformBindings,
  refreshKey: Int,
) {
  val graph = remember(platformBindings) {
    createGraphFactory<SharedGraph.Factory>().create(platformBindings)
  }
  val stationsState by graph.stationsRepository.state.collectAsState()
  val favoriteIds by graph.favoritesRepository.favoriteIds.collectAsState()
  val scope = rememberCoroutineScope()
  var selectedStationId by rememberSaveable { mutableStateOf<String?>(null) }

  LaunchedEffect(graph, refreshKey) {
    graph.favoritesRepository.bootstrap()
    graph.stationsRepository.refresh()
  }

  MaterialTheme {
    val selectedStation = selectedStationId?.let(graph.stationsRepository::stationById)
    if (selectedStation != null) {
      Card(onClick = { graph.routeLauncher.launch(selectedStation) }) {
        Text(selectedStation.name)
        Text("${selectedStation.bikesAvailable} bicis · ${selectedStation.slotsFree} libres")
        Button(onClick = { selectedStationId = null }) {
          Text("Volver")
        }
      }
    } else {
      ScalingLazyColumn(modifier = Modifier.fillMaxSize()) {
        items(stationsState.stations.take(8), key = { it.id }) { station ->
          Card(onClick = { selectedStationId = station.id }) {
            Text(station.name)
            Text("${station.distanceMeters} m")
            Text("${station.bikesAvailable} bicis · ${station.slotsFree} libres")
            Button(onClick = {
              scope.launch { graph.favoritesRepository.toggle(station.id) }
            }) {
              Text(if (station.id in favoriteIds) "Guardada" else "Guardar")
            }
          }
        }
      }
    }
  }
}
