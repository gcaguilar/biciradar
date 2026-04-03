package com.gcaguilar.biciradar.mobileui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.appName
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favorites
import com.gcaguilar.biciradar.mobile_ui.generated.resources.map
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearby
import com.gcaguilar.biciradar.mobile_ui.generated.resources.settings
import com.gcaguilar.biciradar.mobile_ui.generated.resources.trip
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private enum class RailTab(val labelKey: StringResource) {
  Cerca(Res.string.nearby),
  Mapa(Res.string.map),
  Favoritos(Res.string.favorites),
  Viaje(Res.string.trip),
  Perfil(Res.string.settings),
}

private val RailTabs = listOf(
  RailTab.Cerca,
  RailTab.Mapa,
  RailTab.Favoritos,
  RailTab.Viaje,
  RailTab.Perfil,
)

@Composable
fun MobileNavigationRail(
  mobilePlatform: MobileUiPlatform,
  navController: NavHostController,
  modifier: Modifier = Modifier,
) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val colors = LocalBiziColors.current

  NavigationRail(
    modifier = modifier
      .fillMaxHeight()
      .padding(vertical = 12.dp),
    containerColor = if (mobilePlatform == MobileUiPlatform.IOS) {
      colors.navBarIos
    } else {
      colors.navBar
    },
    header = {
      Column(
        modifier = Modifier.padding(bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Surface(
          shape = CircleShape,
          color = colors.red,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
            contentDescription = null,
            tint = colors.onAccent,
            modifier = Modifier.padding(12.dp).size(20.dp),
          )
        }
        Text(
          text = stringResource(Res.string.appName),
          style = MaterialTheme.typography.labelSmall,
          color = colors.muted,
        )
      }
    },
  ) {
    RailTabs.forEach { tab ->
      val screen = tab.screen()
      NavigationRailItem(
        selected = currentRoute?.contains(screen::class.qualifiedName.orEmpty()) == true,
        onClick = { navController.navigateToPrimaryDestination(screen) },
        icon = {
          Icon(
            imageVector = tab.icon(),
            contentDescription = stringResource(tab.labelKey),
          )
        },
        label = { Text(stringResource(tab.labelKey)) },
      )
    }
  }
}

private fun RailTab.screen(): Screen = when (this) {
  RailTab.Cerca -> Screen.Nearby
  RailTab.Mapa -> Screen.Map
  RailTab.Favoritos -> Screen.Favorites
  RailTab.Viaje -> Screen.Trip()
  RailTab.Perfil -> Screen.Profile
}

private fun RailTab.icon() = when (this) {
  RailTab.Cerca -> Icons.AutoMirrored.Filled.DirectionsBike
  RailTab.Mapa -> Icons.Filled.Map
  RailTab.Favoritos -> Icons.Filled.Favorite
  RailTab.Viaje -> Icons.Filled.Directions
  RailTab.Perfil -> Icons.Filled.Tune
}
