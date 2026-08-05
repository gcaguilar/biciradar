package com.gcaguilar.biciradar.mobileui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gcaguilar.biciradar.mobileui.BiziWindowLayout
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.navigation.BiziBottomBar
import com.gcaguilar.biciradar.mobileui.navigation.MobileNavigationRail
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor

/**
 * Height reserved for a natively rendered tab bar (UIKit/SwiftUI `TabView`), measured
 * above the bottom safe-area inset. iOS uses 49pt for a standard tab bar; iOS 26's
 * floating glass bar adds a little breathing room around it.
 */
private val NativeTabBarHeight = 56.dp

@Composable
internal fun BiziNavigationShell(
  mobilePlatform: MobileUiPlatform,
  navController: NavHostController,
  windowLayout: BiziWindowLayout,
  /**
   * True when a native host (SwiftUI TabView with iOS 26 Liquid Glass) already renders
   * the tab bar / navigation rail chrome, so Compose must not draw its own on top of it.
   * Defaults to false: Android and any iOS build without the native shell keep today's
   * Compose-drawn bottom bar / rail exactly as before.
   */
  useNativeChrome: Boolean = false,
  content: @Composable (PaddingValues) -> Unit,
) {
  if (useNativeChrome) {
    // The native tab bar floats *over* this Compose content (the Compose view ignores
    // safe areas so it can draw its own status-bar insets, same as before the native
    // shell). Without reserving room for it, anything anchored to the bottom — sheets,
    // snackbars, dismiss buttons — ends up physically under the glass bar and never
    // receives touches.
    content(
      PaddingValues(
        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + NativeTabBarHeight,
      ),
    )
    return
  }

  if (windowLayout == BiziWindowLayout.Compact) {
    Scaffold(
      containerColor = pageBackgroundColor(mobilePlatform),
      bottomBar = {
        BiziBottomBar(
          mobilePlatform = mobilePlatform,
          navController = navController,
        )
      },
    ) { innerPadding ->
      content(innerPadding)
    }
    return
  }

  Row(
    modifier =
      Modifier
        .fillMaxSize()
        .background(pageBackgroundColor(mobilePlatform)),
  ) {
    MobileNavigationRail(
      mobilePlatform = mobilePlatform,
      navController = navController,
    )
    VerticalDivider(color = LocalBiziColors.current.panel)
    Box(
      modifier =
        Modifier
          .weight(1f)
          .fillMaxHeight(),
    ) {
      content(PaddingValues())
    }
  }
}
