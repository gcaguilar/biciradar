package com.gcaguilar.bizizaragoza.mobileui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import com.gcaguilar.bizizaragoza.core.platform.IOSPlatformBindings
import platform.UIKit.UIViewController

fun MainViewController(
  launchRequest: MobileLaunchRequest? = null,
  stationMapViewFactory: StationMapViewFactory? = null,
): UIViewController = ComposeUIViewController(
  configure = {
    enforceStrictPlistSanityCheck = false
  },
) {
  CompositionLocalProvider(LocalStationMapViewFactory provides stationMapViewFactory) {
    BiziMobileApp(
      platformBindings = IOSPlatformBindings(),
      launchRequest = launchRequest,
    )
  }
}

fun RootViewController(): UIViewController = MainViewController(launchRequest = null)
