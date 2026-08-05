package com.gcaguilar.biciradar.mobileui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.TripRepository
import com.gcaguilar.biciradar.core.platform.IOSPlatformBindings
import com.gcaguilar.biciradar.core.platform.IOSRemoteConfigBridge
import com.gcaguilar.biciradar.mobileui.di.MobileGraph
import com.gcaguilar.biciradar.mobileui.navigation.MobileLaunchRequest
import com.gcaguilar.biciradar.mobileui.navigation.MobileTabNavigator
import com.gcaguilar.biciradar.mobileui.navigation.Screen
import com.gcaguilar.biciradar.mobileui.navigation.ScreenContent
import com.gcaguilar.biciradar.mobileui.theme.ThemeProvider
import com.gcaguilar.biciradar.mobileui.theme.pageBackgroundColor
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.UIKit.UIViewController

/**
 * Creates a [BiziMainViewControllerWrapper] that holds the Compose UIViewController.
 * Use [BiziMainViewControllerWrapper.viewController] to embed it and
 * [BiziMainViewControllerWrapper.updateLaunchRequest] to push new launch requests
 * without recreating the Compose tree.
 */
@Suppress("ktlint:standard:function-naming")
fun MainViewControllerWrapper(
  launchRequest: MobileLaunchRequest? = null,
  stationMapViewFactory: StationMapViewFactory? = null,
  remoteConfigBridge: IOSRemoteConfigBridge? = null,
  /**
   * When provided, a native SwiftUI TabView/NavigationStack shell (iOS 26 Liquid Glass)
   * owns navigation chrome and Compose forwards pushes/tab switches here instead of
   * drawing its own bottom bar. Leave both null to keep today's fully Compose-driven
   * behavior (used on every Android build and on iOS builds that haven't adopted the
   * native shell yet).
   */
  onNavigate: ((Screen) -> Unit)? = null,
  onActivate: ((Screen) -> Unit)? = null,
  /**
   * Shared dependency graph to reuse across multiple Compose mount points (e.g. one
   * per tab, in the native TabView shell). Leave null to keep today's behavior of
   * creating a fresh graph for this single view controller. When embedding several
   * [MainViewControllerWrapper]/[ScreenViewController] instances side by side (one
   * per tab), create ONE graph via `MobileGraphFactory.shared.create(...)` in Swift
   * and pass the same instance to every call — otherwise each tab ends up with its
   * own database/network/background-scheduler stack.
   */
  graph: SharedGraph? = null,
  /**
   * `IOSPlatformBindings` compartidas con el resto del proceso (widgets, atajos,
   * watch sync, `BiziAppleGraph`). Cuando pasas [graph] también DEBES pasar las
   * mismas bindings usadas para construirlo (ver `BiziSharedGraph.platformBindings`
   * en Swift). Si dejas esto en `null`, Compose construye sus PROPIAS bindings —
   * los repos siguen unificados porque vienen del [graph] compartido, pero cualquier
   * estado propio de `IOSPlatformBindings` (por ejemplo el `IOSRouteLauncher` con
   * late-wiring de `SettingsRepository`) queda duplicado y desincronizado.
   */
  platformBindings: IOSPlatformBindings? = null,
): BiziMainViewControllerWrapper =
  BiziMainViewControllerWrapper(
    initialLaunchRequest = launchRequest,
    stationMapViewFactory = stationMapViewFactory,
    remoteConfigBridge = remoteConfigBridge,
    onNavigate = onNavigate,
    onActivate = onActivate,
    graph = graph,
    platformBindings = platformBindings,
  )

@Suppress("ktlint:standard:function-naming")
fun RootViewController(): UIViewController = MainViewControllerWrapper().viewController

class BiziMainViewControllerWrapper(
  initialLaunchRequest: MobileLaunchRequest?,
  stationMapViewFactory: StationMapViewFactory?,
  remoteConfigBridge: IOSRemoteConfigBridge?,
  onNavigate: ((Screen) -> Unit)? = null,
  onActivate: ((Screen) -> Unit)? = null,
  graph: SharedGraph? = null,
  platformBindings: IOSPlatformBindings? = null,
) {
  private val resolvedPlatformBindings: IOSPlatformBindings =
    platformBindings ?: IOSPlatformBindings(remoteConfigBridge = remoteConfigBridge)

  private var currentLaunchRequest: MobileLaunchRequest? by mutableStateOf(
    value = initialLaunchRequest,
    policy = neverEqualPolicy(),
  )
  private var refreshNonce by mutableStateOf(0)
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private var tripRepository: TripRepository? = null
  private var tabNavigator: MobileTabNavigator? = null

  val viewController: UIViewController =
    ComposeUIViewController(
      configure = { enforceStrictPlistSanityCheck = false },
    ) {
      CompositionLocalProvider(LocalStationMapViewFactory provides stationMapViewFactory) {
        BiziMobileApp(
          platformBindings = resolvedPlatformBindings,
          graph = graph,
          launchRequest = currentLaunchRequest,
          refreshKey = refreshNonce,
          onTripRepositoryReady = { repo ->
            tripRepository = repo
          },
          onNavigateNative = onNavigate,
          onActivateNative = onActivate,
          onTabNavigatorReady = { navigator -> tabNavigator = navigator },
        )
      }
    }

  /**
   * Switches the Compose NavController to a top-level tab. Called by the native SwiftUI
   * `TabView` in the Liquid Glass shell, which owns only the tab bar chrome — navigation
   * itself stays inside this single Compose instance, exactly as on Android.
   *
   * No-op until Compose has composed at least once (the navigator is published from the
   * composition); the initial tab is [Screen.Nearby] anyway, which is what the native
   * TabView starts on.
   */
  fun selectTab(tab: Screen) {
    tabNavigator?.selectTab(tab)
  }

  fun updateLaunchRequest(request: MobileLaunchRequest?) {
    currentLaunchRequest = request
  }

  /** Called by Swift when the app returns to the foreground and should pull fresh station data. */
  fun requestRefresh() {
    refreshNonce += 1
  }

  /** Called by Swift when the app enters background with active monitoring. */
  fun doFinalBackgroundCheck(completion: () -> Unit) {
    scope.launch {
      tripRepository?.doFinalBackgroundCheck()
      completion()
    }
  }
}

// Keep legacy entry point for backward compatibility
@Suppress("ktlint:standard:function-naming")
fun MainViewController(
  launchRequest: MobileLaunchRequest? = null,
  stationMapViewFactory: StationMapViewFactory? = null,
  remoteConfigBridge: IOSRemoteConfigBridge? = null,
): UIViewController =
  MainViewControllerWrapper(
    launchRequest = launchRequest,
    stationMapViewFactory = stationMapViewFactory,
    remoteConfigBridge = remoteConfigBridge,
  ).viewController

/**
 * Renders a single detail [route] (e.g. [Screen.StationDetail], [Screen.FavoritesSearch])
 * with no [com.gcaguilar.biciradar.mobileui.navigation.BiziNavHost] of its own. This is
 * the counterpart to a native `NavigationStack` destination pushed from the SwiftUI shell:
 * every push forwards here, and this view controller only needs to know how to pop itself
 * (via [onBack]) or push further (via [onNavigate]) — the actual stack lives in SwiftUI.
 *
 * Pass the SAME [graph] used for the tab-root [MainViewControllerWrapper] instances so this
 * screen shares repositories/state with the rest of the app instead of spinning up its own.
 */
@Suppress("ktlint:standard:function-naming")
@OptIn(FlowPreview::class)
fun ScreenViewController(
  route: Screen,
  remoteConfigBridge: IOSRemoteConfigBridge? = null,
  graph: SharedGraph? = null,
  /**
   * `IOSPlatformBindings` compartidas con el resto del proceso. Pásalas cuando embebas este
   * view controller dentro del shell nativo iOS para reutilizar las MISMAS bindings que
   * usaron para construir [graph] (ver `BiziSharedGraph.platformBindings` en Swift). Dejar
   * en `null` sólo tiene sentido para previews/harnesses aislados.
   */
  platformBindings: IOSPlatformBindings? = null,
  onNavigate: ((Screen) -> Unit)? = null,
  onBack: (() -> Unit)? = null,
): UIViewController =
  ComposeUIViewController(
    configure = { enforceStrictPlistSanityCheck = false },
  ) {
    val resolvedPlatformBindings =
      remember(platformBindings, remoteConfigBridge) {
        platformBindings ?: IOSPlatformBindings(remoteConfigBridge = remoteConfigBridge)
      }
    val resolvedGraph =
      remember(graph, resolvedPlatformBindings) {
        // Mismo aviso que en BiziMobileApp: pasar `graph = null` construye un grafo nuevo
        // que duplica cada @SingleIn. Cualquier push desde el shell nativo iOS DEBE
        // propagar `BiziSharedGraph.graph`.
        (graph as? MobileGraph) ?: run {
          resolvedPlatformBindings.logger.warn(
            "ScreenViewController",
            "ScreenViewController invocado con graph=null. Se está creando un nuevo " +
              "MobileGraph — esto duplica los @SingleIn del grafo. En el shell nativo iOS " +
              "propaga siempre BiziSharedGraph.graph.",
          )
          MobileGraph.Companion.create(resolvedPlatformBindings)
        }
      }
    CompositionLocalProvider(LocalMetroViewModelFactory provides resolvedGraph.metroViewModelFactory) {
      val mobilePlatform = remember { currentMobileUiPlatform() }
      val themePreference by resolvedGraph.observeSettings.themePreference.collectAsState()
      ThemeProvider(mobilePlatform, themePreference) {
        androidx.compose.material3.Surface(
          modifier = Modifier.fillMaxSize(),
          color = pageBackgroundColor(mobilePlatform),
        ) {
          ScreenContent(
            route = route,
            mobilePlatform = mobilePlatform,
            platformBindings = resolvedPlatformBindings,
            // TODO: thread real map readiness through once station-detail/map-picker
            // previews need to render an embedded map from this standalone screen host.
            isMapReady = false,
            onBack = { onBack?.invoke() },
            onNavigate = { screen -> onNavigate?.invoke(screen) },
          )
        }
      }
    }
  }
