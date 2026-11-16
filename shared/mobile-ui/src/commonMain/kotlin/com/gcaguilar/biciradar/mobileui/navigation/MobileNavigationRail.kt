package com.gcaguilar.biciradar.mobileui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.appName
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import org.jetbrains.compose.resources.stringResource

/**
 * Material 3 Expressive navigation rail for medium and expanded windows. It stays
 * collapsed (labels under the icons): an expanded rail would take 220dp+ away from the
 * map and the list-detail panes, which already share the width at 840dp.
 */
@Composable
fun MobileNavigationRail(
  mobilePlatform: MobileUiPlatform,
  navController: NavHostController,
  modifier: Modifier = Modifier,
) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val destination = navBackStackEntry?.destination
  val colors = LocalBiziColors.current

  WideNavigationRail(
    modifier = modifier.fillMaxHeight(),
    colors =
      WideNavigationRailDefaults.colors(
        containerColor =
          if (mobilePlatform == MobileUiPlatform.IOS) {
            colors.navBarIos
          } else {
            colors.navBar
          },
      ),
    header = { RailHeader() },
  ) {
    PrimaryTab.entries.forEach { tab ->
      val selected = tab.isSelected(destination)
      WideNavigationRailItem(
        selected = selected,
        onClick = { navController.navigateToPrimaryDestination(tab.screen()) },
        icon = { PrimaryTabIcon(tab, selected) },
        label = { Text(stringResource(tab.labelKey)) },
        railExpanded = false,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RailHeader() {
  val colors = LocalBiziColors.current
  Column(
    modifier = Modifier.padding(bottom = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Surface(
      shape = MaterialShapes.Cookie9Sided.toShape(),
      color = colors.red,
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
        contentDescription = null,
        tint = colors.onAccent,
        modifier = Modifier.padding(12.dp).size(22.dp),
      )
    }
    Text(
      text = stringResource(Res.string.appName),
      style = MaterialTheme.typography.labelSmall,
      color = colors.muted,
    )
  }
}
