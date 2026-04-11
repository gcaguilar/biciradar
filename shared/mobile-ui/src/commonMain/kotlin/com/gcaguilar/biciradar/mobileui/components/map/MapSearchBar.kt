package com.gcaguilar.biciradar.mobileui.components.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapSearchStationOrAddress
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.StationSearchField
import org.jetbrains.compose.resources.stringResource

/**
 * Barra de búsqueda del mapa para estaciones y direcciones.
 *
 * @param searchQuery Query actual de búsqueda
 * @param onSearchQueryChange Callback cuando cambia la búsqueda
 * @param mobilePlatform Plataforma móvil para ajustes visuales
 */
@Composable
internal fun MapSearchBar(
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  mobilePlatform: MobileUiPlatform,
  modifier: Modifier = Modifier,
) {
  StationSearchField(
    mobilePlatform = mobilePlatform,
    value = searchQuery,
    onValueChange = onSearchQueryChange,
    label = stringResource(Res.string.mapSearchStationOrAddress),
  )
}
