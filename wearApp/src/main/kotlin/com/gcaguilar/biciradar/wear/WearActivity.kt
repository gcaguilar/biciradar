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
import com.gcaguilar.biciradar.core.AppConfiguration
import com.gcaguilar.biciradar.core.platform.AndroidPlatformBindings

class WearActivity : ComponentActivity() {
  private val locationPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions(),
  ) {
    refreshNonce += 1
  }

  private var refreshNonce by mutableIntStateOf(0)
  private var launchStationId by mutableStateOf<String?>(null)
  private var launchStationNonce by mutableIntStateOf(0)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleLaunchIntent(intent)
    val platformBindings = AndroidPlatformBindings(
      context = applicationContext,
      appConfiguration = AppConfiguration(),
    )
    setContent {
      WearRoot(
        platformBindings = platformBindings,
        refreshKey = refreshNonce,
        launchStationId = launchStationId,
        launchStationNonce = launchStationNonce,
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
    launchStationId = if (intent?.action == ACTION_OPEN_STATION) {
      intent.getStringExtra(EXTRA_OPEN_STATION_ID)
    } else {
      null
    }
    launchStationNonce += 1
  }

  companion object {
    const val ACTION_OPEN_STATION = "com.gcaguilar.biciradar.wear.action.OPEN_STATION"
    const val EXTRA_OPEN_STATION_ID = "com.gcaguilar.biciradar.wear.extra.OPEN_STATION_ID"
  }
}
