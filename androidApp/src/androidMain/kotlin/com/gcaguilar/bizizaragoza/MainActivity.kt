package com.gcaguilar.bizizaragoza

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.gcaguilar.bizizaragoza.core.AppConfiguration
import com.gcaguilar.bizizaragoza.core.platform.AndroidPlatformBindings
import com.gcaguilar.bizizaragoza.mobileui.BiziMobileApp
import com.gcaguilar.bizizaragoza.mobileui.MobileLaunchRequest

class MainActivity : ComponentActivity() {
  private val locationPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions(),
  ) {
    refreshNonce += 1
  }

  private var launchRequest by mutableStateOf<MobileLaunchRequest?>(null)
  private var refreshNonce by mutableIntStateOf(0)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val platformBindings = AndroidPlatformBindings(
      context = applicationContext,
      appConfiguration = AppConfiguration(
        geminiProxyBaseUrl = BuildConfig.GEMINI_PROXY_BASE_URL,
      ),
    )
    launchRequest = intent.toLaunchRequest()
    AndroidAssistantShortcuts.publish(this)
    AndroidAssistantShortcuts.reportUsed(this, launchRequest)

    setContent {
      BiziMobileApp(
        platformBindings = platformBindings,
        refreshKey = refreshNonce,
        launchRequest = launchRequest,
      )
    }

    ensureLocationPermissions()
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    launchRequest = intent.toLaunchRequest()
    AndroidAssistantShortcuts.reportUsed(this, launchRequest)
  }

  private fun ensureLocationPermissions() {
    if (hasLocationPermission()) return
    locationPermissionLauncher.launch(
      arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
      ),
    )
  }

  private fun hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
      this,
      Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
      ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION,
      ) == PackageManager.PERMISSION_GRANTED
  }
}
