package com.gcaguilar.bizizaragoza.mobileui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import com.gcaguilar.bizizaragoza.core.platform.IOSPlatformBindings
import platform.UIKit.UIViewController

/**
 * Creates a [BiziMainViewControllerWrapper] that holds the Compose UIViewController.
 * Use [BiziMainViewControllerWrapper.viewController] to embed it and
 * [BiziMainViewControllerWrapper.updateLaunchRequest] to push new launch requests
 * without recreating the Compose tree.
 */
fun MainViewControllerWrapper(
  launchRequest: MobileLaunchRequest? = null,
  stationMapViewFactory: StationMapViewFactory? = null,
): BiziMainViewControllerWrapper = BiziMainViewControllerWrapper(launchRequest, stationMapViewFactory)

fun RootViewController(): UIViewController = MainViewControllerWrapper().viewController

class BiziMainViewControllerWrapper(
  initialLaunchRequest: MobileLaunchRequest?,
  stationMapViewFactory: StationMapViewFactory?,
) {
  private var currentLaunchRequest: MobileLaunchRequest? by mutableStateOf(initialLaunchRequest)

  val viewController: UIViewController = ComposeUIViewController(
    configure = { enforceStrictPlistSanityCheck = false },
  ) {
    CompositionLocalProvider(LocalStationMapViewFactory provides stationMapViewFactory) {
      BiziMobileApp(
        platformBindings = IOSPlatformBindings(),
        launchRequest = currentLaunchRequest,
      )
    }
  }

  fun updateLaunchRequest(request: MobileLaunchRequest?) {
    currentLaunchRequest = request
  }
}

// Keep legacy entry point for backward compatibility
fun MainViewController(
  launchRequest: MobileLaunchRequest? = null,
  stationMapViewFactory: StationMapViewFactory? = null,
): UIViewController = MainViewControllerWrapper(launchRequest, stationMapViewFactory).viewController
