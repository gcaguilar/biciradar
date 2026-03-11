package com.gcaguilar.bizizaragoza.mobileui

import androidx.compose.ui.window.ComposeUIViewController
import com.gcaguilar.bizizaragoza.core.platform.IOSPlatformBindings
import platform.UIKit.UIViewController

fun MainViewController(
  launchRequest: MobileLaunchRequest? = null,
): UIViewController = ComposeUIViewController(
  configure = {
    enforceStrictPlistSanityCheck = false
  },
) {
  BiziMobileApp(
    platformBindings = IOSPlatformBindings(),
    launchRequest = launchRequest,
  )
}

fun RootViewController(): UIViewController = MainViewController(launchRequest = null)
