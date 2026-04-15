package com.gcaguilar.biciradar.desktop

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.gcaguilar.biciradar.core.platform.DesktopPlatformBindings
import com.gcaguilar.biciradar.mobileui.BiziMobileApp

fun main() =
  application {
    val windowState = rememberWindowState(width = 1360.dp, height = 920.dp)
    val platformBindings = DesktopPlatformBindings()

    Window(
      onCloseRequest = ::exitApplication,
      title = "BiciRadar",
      state = windowState,
    ) {
      BiziMobileApp(
        platformBindings = platformBindings,
        modifier = Modifier.fillMaxSize(),
      )
    }
  }
