package com.gcaguilar.biciradar.mobileui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favorites
import com.gcaguilar.biciradar.mobile_ui.generated.resources.map
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearby
import com.gcaguilar.biciradar.mobile_ui.generated.resources.settings
import com.gcaguilar.biciradar.mobile_ui.generated.resources.trip
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private enum class MobileTab(val labelKey: StringResource) {
  Cerca(Res.string.nearby),
  Mapa(Res.string.map),
  Favoritos(Res.string.favorites),
  Viaje(Res.string.trip),
  Perfil(Res.string.settings),
}

private val MobileTabs = listOf(
  MobileTab.Cerca,
  MobileTab.Mapa,
  MobileTab.Favoritos,
  MobileTab.Viaje,
  MobileTab.Perfil,
)

@Composable
fun BiziBottomBar(
  mobilePlatform: MobileUiPlatform,
  navController: NavHostController,
  modifier: Modifier = Modifier,
) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  NavigationBar(
    modifier = modifier,
    containerColor = if (mobilePlatform == MobileUiPlatform.IOS) {
      LocalBiziColors.current.navBarIos
    } else {
      LocalBiziColors.current.navBar
    },
  ) {
    MobileTabs.forEach { tab ->
      val screen = tab.screen()
      NavigationBarItem(
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

private fun MobileTab.screen(): Screen = when (this) {
  MobileTab.Cerca -> Screen.Nearby
  MobileTab.Mapa -> Screen.Map
  MobileTab.Favoritos -> Screen.Favorites
  MobileTab.Viaje -> Screen.Trip()
  MobileTab.Perfil -> Screen.Profile
}

private fun MobileTab.icon() = when (this) {
  MobileTab.Cerca -> Icons.AutoMirrored.Filled.DirectionsBike
  MobileTab.Mapa -> Icons.Filled.Map
  MobileTab.Favoritos -> Icons.Filled.Favorite
  MobileTab.Viaje -> Icons.Filled.Directions
  MobileTab.Perfil -> Icons.Filled.Tune
}
