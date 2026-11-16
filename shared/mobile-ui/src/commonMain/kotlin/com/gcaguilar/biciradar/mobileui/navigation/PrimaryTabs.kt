package com.gcaguilar.biciradar.mobileui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favorites
import com.gcaguilar.biciradar.mobile_ui.generated.resources.map
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearby
import com.gcaguilar.biciradar.mobile_ui.generated.resources.settings
import com.gcaguilar.biciradar.mobile_ui.generated.resources.trip
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Primary destinations shown by the Compose-drawn bottom bar and navigation rail.
 * Each tab carries a filled icon for the selected state and an outlined one for the
 * rest, as Material 3 recommends for navigation components.
 */
internal enum class PrimaryTab(
  val labelKey: StringResource,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
) {
  Cerca(Res.string.nearby, Icons.AutoMirrored.Filled.DirectionsBike, Icons.AutoMirrored.Outlined.DirectionsBike),
  Mapa(Res.string.map, Icons.Filled.Map, Icons.Outlined.Map),
  Favoritos(Res.string.favorites, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
  Viaje(Res.string.trip, Icons.Filled.Directions, Icons.Outlined.Directions),
  Perfil(Res.string.settings, Icons.Filled.Tune, Icons.Outlined.Tune),
  ;

  fun screen(): Screen =
    when (this) {
      Cerca -> Screen.Nearby
      Mapa -> Screen.Map
      Favoritos -> Screen.Favorites
      Viaje -> Screen.Trip()
      Perfil -> Screen.Profile
    }

  fun isSelected(destination: NavDestination?): Boolean =
    destination?.route?.contains(screen()::class.qualifiedName.orEmpty()) == true
}

@Composable
internal fun PrimaryTabIcon(
  tab: PrimaryTab,
  selected: Boolean,
) {
  Icon(
    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
    contentDescription = stringResource(tab.labelKey),
  )
}
