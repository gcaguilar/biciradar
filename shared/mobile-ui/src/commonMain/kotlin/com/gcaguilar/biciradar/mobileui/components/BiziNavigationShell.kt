package com.gcaguilar.biciradar.mobileui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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

@Composable
internal fun BiziNavigationShell(
  mobilePlatform: MobileUiPlatform,
  navController: NavHostController,
  windowLayout: BiziWindowLayout,
  content: @Composable (PaddingValues) -> Unit,
) {
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
    modifier = Modifier
      .fillMaxSize()
      .background(pageBackgroundColor(mobilePlatform)),
  ) {
    MobileNavigationRail(
      mobilePlatform = mobilePlatform,
      navController = navController,
    )
    VerticalDivider(color = LocalBiziColors.current.panel)
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxHeight(),
    ) {
      content(PaddingValues())
    }
  }
}
