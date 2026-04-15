package com.gcaguilar.biciradar.wear

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.platform.AndroidPlatformBindings

internal enum class WearScreenshotSurface(
  val rawValue: String,
) {
  Dashboard("dashboard"),
  StationDetail("station_detail"),
  Monitoring("monitoring"),
  ;

  companion object {
    fun from(rawValue: String?): WearScreenshotSurface? = entries.firstOrNull { it.rawValue == rawValue }
  }
}

/**
 * Activity principal de la app Wear.
 * Usa WearAppGraph para obtener el singleton de SharedGraph.
 */
class WearActivity : ComponentActivity() {
  private val locationPermissionLauncher =
    registerForActivityResult(
      ActivityResultContracts.RequestMultiplePermissions(),
    ) {
      refreshNonce += 1
    }

  private var refreshNonce by mutableIntStateOf(0)
  private var launchStationId by mutableStateOf<String?>(null)
  private var launchStationNonce by mutableIntStateOf(0)
  private var screenshotSurface by mutableStateOf<WearScreenshotSurface?>(null)

  // Acceso al grafo singleton
  private val graph: SharedGraph
    get() = WearAppGraph.graph
  private val platformBindings: AndroidPlatformBindings
    get() = WearAppGraph.platformBindings

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Inicializar grafo si no lo está
    if (!WearAppGraph.isInitialized()) {
      WearAppGraph.initialize(application)
    }

    handleLaunchIntent(intent)

    setContent {
      WearRoot(
        platformBindings = platformBindings,
        graph = graph,
        refreshKey = refreshNonce,
        launchStationId = launchStationId,
        launchStationNonce = launchStationNonce,
        screenshotSurface = screenshotSurface,
      )
    }
    locationPermissionLauncher.launch(
      arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
      ),
    )
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleLaunchIntent(intent)
  }

  private fun handleLaunchIntent(intent: Intent?) {
    screenshotSurface = WearScreenshotSurface.from(intent?.getStringExtra(EXTRA_SCREENSHOT_SURFACE))
    launchStationId =
      intent
        ?.takeIf { it.action == ACTION_OPEN_STATION || it.hasExtra(EXTRA_OPEN_STATION_ID) }
        ?.getStringExtra(EXTRA_OPEN_STATION_ID)
    launchStationNonce += 1
  }

  companion object {
    const val ACTION_OPEN_STATION = "com.gcaguilar.biciradar.wear.action.OPEN_STATION"
    const val EXTRA_OPEN_STATION_ID = "com.gcaguilar.biciradar.wear.extra.OPEN_STATION_ID"
    const val EXTRA_SCREENSHOT_SURFACE = "com.gcaguilar.biciradar.wear.extra.SCREENSHOT_SURFACE"
  }
}
