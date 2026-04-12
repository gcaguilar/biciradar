package com.gcaguilar.biciradar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.mobileui.BiziMobileApp
import com.gcaguilar.biciradar.mobileui.navigation.AssistantLaunchRequest
import com.gcaguilar.biciradar.mobileui.navigation.MobileLaunchRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * MainActivity que usa BiziAppGraph para obtener dependencias.
 *
 * Esto elimina:
 * - Creación duplicada de PlatformBindings
 * - Creación duplicada de SharedGraph
 * - Holders temporales para Services
 */
class MainActivity : ComponentActivity() {
  // Acceso al grafo singleton inicializado en Application
  private val graph: SharedGraph
    get() = BiziAppGraph.graph

  private val platformBindings
    get() = BiziAppGraph.platformBindings

  private var locationPermissionContinuation: kotlin.coroutines.Continuation<Boolean>? = null
  private val locationPermissionLauncher =
    registerForActivityResult(
      ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
      refreshNonce += 1
      val granted = results.values.any { it }
      locationPermissionContinuation?.resume(granted)
      locationPermissionContinuation = null
    }

  // Coroutine continuation resumed when the user responds to the POST_NOTIFICATIONS dialog.
  private var notificationPermissionContinuation: kotlin.coroutines.Continuation<Boolean>? = null
  private val notificationPermissionLauncher =
    registerForActivityResult(
      ActivityResultContracts.RequestPermission(),
    ) { granted ->
      notificationPermissionContinuation?.resume(granted)
      notificationPermissionContinuation = null
    }

  private var launchRequest by mutableStateOf<MobileLaunchRequest?>(null)
  private var assistantLaunchRequest by mutableStateOf<AssistantLaunchRequest?>(null)
  private var refreshNonce by mutableIntStateOf(0)
  private var startupReady by mutableStateOf(false)

  /** Coroutine scope used to observe monitoring and surface snapshots. */
  private var appSurfaceScope: CoroutineScope? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    // Asegurar que el grafo está inicializado
    if (!BiziAppGraph.isInitialized()) {
      BiziAppGraph.initialize(application)
    }

    installSplashScreen().setKeepOnScreenCondition { !startupReady }
    super.onCreate(savedInstanceState)

    platformBindings.bindLocationPermissionRequester {
      if (hasLocationPermission()) {
        return@bindLocationPermissionRequester true
      }
      suspendCoroutine { cont ->
        locationPermissionContinuation = cont
        locationPermissionLauncher.launch(
          arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
          ),
        )
      }
    }

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
        graph = graph,
        refreshKey = refreshNonce,
        launchRequest = launchRequest,
        assistantLaunchRequest = assistantLaunchRequest,
        onSurfaceMonitoringRepositoryReady = { wireMonitoringService() },
        onSurfaceSnapshotRepositoryReady = { wireWidgets() },
        onStartupReadyChanged = { ready -> startupReady = ready },
        useInAppStartupSplash = false,
      )
    }

    SavedPlaceAlertsWorker.schedule(applicationContext)
  }

  override fun onStart() {
    super.onStart()
    platformBindings.attachExperienceActivity(this)
  }

  override fun onStop() {
    platformBindings.attachExperienceActivity(null)
    super.onStop()
  }

  override fun onDestroy() {
    appSurfaceScope?.cancel()
    appSurfaceScope = null
    super.onDestroy()
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    applyLaunchPayload(intent)
    AndroidAssistantShortcuts.reportUsed(this, launchRequest, assistantLaunchRequest)
  }

  /**
   * Conecta el servicio de monitorización usando el singleton del grafo.
   * Ya no necesita pasar dependencias - el Service las obtiene de BiziAppGraph.
   */
  private fun wireMonitoringService() {
    appSurfaceScope?.cancel()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    appSurfaceScope = scope
    var serviceRunning = false

    graph.observeSurfaceMonitoring.state
      .onEach { state ->
        val shouldRun = state?.isActive == true
        if (shouldRun && !serviceRunning) {
          TripMonitorService.start(applicationContext)
          serviceRunning = true
        } else if (!shouldRun && serviceRunning) {
          TripMonitorService.stop(applicationContext)
          serviceRunning = false
        }
      }.launchIn(scope)
  }

  private fun wireWidgets() {
    val scope =
      appSurfaceScope ?: CoroutineScope(SupervisorJob() + Dispatchers.Main).also {
        appSurfaceScope = it
      }
    graph.observeSurfaceSnapshot.bundle
      .onEach { snapshot ->
        FavoriteStationWidgetProvider.updateAll(applicationContext)
        NearbyStationsWidgetProvider.updateAll(applicationContext)
        QuickActionsWidgetProvider.updateAll(applicationContext)
        CommuteWidgetProvider.updateAll(applicationContext)
        AndroidDynamicShortcuts.publish(applicationContext, snapshot)
      }.launchIn(scope)
  }

  private fun hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(
      this,
      Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
      ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION,
      ) == PackageManager.PERMISSION_GRANTED

  private fun applyLaunchPayload(intent: Intent) {
    val payload = intent.toLaunchPayload()
    launchRequest = payload?.launchRequest
    assistantLaunchRequest = payload?.assistantLaunchRequest
  }
}
