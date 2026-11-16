package com.gcaguilar.biciradar.mobileui.navigation

import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import org.jetbrains.compose.resources.stringResource

/**
 * Material 3 Expressive short navigation bar used on compact windows.
 */
@Composable
fun BiziBottomBar(
  mobilePlatform: MobileUiPlatform,
  navController: NavHostController,
  modifier: Modifier = Modifier,
) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val destination = navBackStackEntry?.destination
  ShortNavigationBar(
    modifier = modifier,
    containerColor =
      if (mobilePlatform == MobileUiPlatform.IOS) {
        LocalBiziColors.current.navBarIos
      } else {
        LocalBiziColors.current.navBar
      },
  ) {
    PrimaryTab.entries.forEach { tab ->
      val selected = tab.isSelected(destination)
      ShortNavigationBarItem(
        selected = selected,
        onClick = { navController.navigateToPrimaryDestination(tab.screen()) },
        icon = { PrimaryTabIcon(tab, selected) },
        label = { Text(stringResource(tab.labelKey)) },
      )
    }
  }
}
