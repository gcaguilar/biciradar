package com.gcaguilar.biciradar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.gcaguilar.biciradar.core.AppConfiguration
import com.gcaguilar.biciradar.core.TripRepository
import com.gcaguilar.biciradar.core.platform.AndroidPlatformBindings
import com.gcaguilar.biciradar.mobileui.AssistantLaunchRequest
import com.gcaguilar.biciradar.mobileui.BiziMobileApp
import com.gcaguilar.biciradar.mobileui.MobileLaunchRequest
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {
  private val locationPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions(),
  ) {
    refreshNonce += 1
  }

  // Coroutine continuation resumed when the user responds to the POST_NOTIFICATIONS dialog.
  private var notificationPermissionContinuation: kotlin.coroutines.Continuation<Boolean>? = null
  private val notificationPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission(),
  ) { granted ->
    notificationPermissionContinuation?.resume(granted)
    notificationPermissionContinuation = null
  }

  private var launchRequest by mutableStateOf<MobileLaunchRequest?>(null)
  private var assistantLaunchRequest by mutableStateOf<AssistantLaunchRequest?>(null)
  private var refreshNonce by mutableIntStateOf(0)

  /** Coroutine scope used to observe TripRepository state for foreground service control. */
  private var tripServiceScope: CoroutineScope? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)

    val platformBindings = AndroidPlatformBindings(
      context = applicationContext,
      appConfiguration = AppConfiguration(),
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      platformBindings.bindNotificationPermissionRequester {
        if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
          ) == PackageManager.PERMISSION_GRANTED
        ) {
          return@bindNotificationPermissionRequester true
        }
        suspendCoroutine { cont ->
          notificationPermissionContinuation = cont
          notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
      }
    }

    applyLaunchPayload(intent)
    AndroidAssistantShortcuts.reportUsed(this, launchRequest, assistantLaunchRequest)

    setContent {
      BiziMobileApp(
        platformBindings = platformBindings,
        refreshKey = refreshNonce,
        launchRequest = launchRequest,
        assistantLaunchRequest = assistantLaunchRequest,
        onTripRepositoryReady = { repo -> wireTripMonitorService(repo) },
      )
    }

    ensureLocationPermissions()
  }

  override fun onDestroy() {
    tripServiceScope?.cancel()
    tripServiceScope = null
    TripRepositoryHolder.tripRepository = null
    super.onDestroy()
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    applyLaunchPayload(intent)
    AndroidAssistantShortcuts.reportUsed(this, launchRequest, assistantLaunchRequest)
  }

  private fun wireTripMonitorService(repo: TripRepository) {
    TripRepositoryHolder.tripRepository = repo
    tripServiceScope?.cancel()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    tripServiceScope = scope
    var serviceRunning = false
    repo.state
      .onEach { state ->
        val shouldRun = state.monitoring.isActive
        if (shouldRun && !serviceRunning) {
          TripMonitorService.start(applicationContext)
          serviceRunning = true
        } else if (!shouldRun && serviceRunning) {
          TripMonitorService.stop(applicationContext)
          serviceRunning = false
        }
      }
      .launchIn(scope)
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

  private fun applyLaunchPayload(intent: Intent) {
    val payload = intent.toLaunchPayload()
    launchRequest = payload?.launchRequest
    assistantLaunchRequest = payload?.assistantLaunchRequest
    ShortcutAnalytics.trackLaunch(
      context = applicationContext,
      intent = intent,
      launchRequest = launchRequest,
      assistantLaunchRequest = assistantLaunchRequest,
    )
  }
}
