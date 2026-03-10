package com.gcaguilar.bizizaragoza.mobileui

import androidx.compose.ui.window.ComposeUIViewController
import com.gcaguilar.bizizaragoza.core.platform.IOSPlatformBindings
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
  BiziMobileApp(platformBindings = IOSPlatformBindings())
}
