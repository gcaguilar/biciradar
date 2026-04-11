package com.gcaguilar.biciradar.mobileui.components.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.formatDistance
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.close
import com.gcaguilar.biciradar.mobile_ui.generated.resources.details
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapNearestFallbackSummary
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapNearestStationLabel
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapNoStationsWithinRadius
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapSelectedStationLabel
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapStationDistanceSummary
import com.gcaguilar.biciradar.mobile_ui.generated.resources.route
import com.gcaguilar.biciradar.mobile_ui.generated.resources.save
import com.gcaguilar.biciradar.mobile_ui.generated.resources.saved
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.components.station.FavoritePill
import com.gcaguilar.biciradar.mobileui.components.station.OutlineActionPill
import com.gcaguilar.biciradar.mobileui.components.station.RoutePill
import org.jetbrains.compose.resources.stringResource

/**
 * Tarjeta de estación seleccionada en el mapa (Bottom sheet).
 *
 * @param station La estación seleccionada
 * @param isFavorite Si la estación es favorita
 * @param isShowingNearestSelection Si se está mostrando la selección más cercana
 * @param isFallbackSelection Si es una selección fallback
 * @param searchRadiusMeters Radio de búsqueda en metros
 * @param onFavoriteToggle Callback al alternar favorito
 * @param onOpenStationDetails Callback al abrir detalles
 * @param onQuickRoute Callback al pedir ruta rápida
 * @param onDismiss Callback al cerrar la tarjeta
 */
@Composable
internal fun StationDetailBottomSheet(
  station: Station,
  isFavorite: Boolean,
  isShowingNearestSelection: Boolean,
  isFallbackSelection: Boolean,
  searchRadiusMeters: Int,
  mobilePlatform: MobileUiPlatform,
  onFavoriteToggle: () -> Unit,
  onOpenStationDetails: (Station) -> Unit,
  onQuickRoute: (Station) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val c = LocalBiziColors.current
  val overlayTitle = if (mobilePlatform == MobileUiPlatform.IOS) c.ink else c.onAccent
  val overlayBody = if (mobilePlatform == MobileUiPlatform.IOS) c.muted else c.onAccent.copy(alpha = 0.84f)

  Card(
    modifier = modifier,
    shape = RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 24.dp else 28.dp),
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, c.red.copy(alpha = 0.12f)) else null,
    colors = CardDefaults.cardColors(containerColor = if (mobilePlatform == MobileUiPlatform.IOS) c.surface else c.red),
  ) {
    Column(
      modifier =
        Modifier
          .padding(horizontal = 14.dp, vertical = 13.dp),
      verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
      // Header con etiqueta y botón cerrar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = getHeaderLabel(isFallbackSelection, isShowingNearestSelection, searchRadiusMeters),
          color = if (mobilePlatform == MobileUiPlatform.IOS) c.red else overlayBody,
          style = MaterialTheme.typography.bodySmall,
        )
        Icon(
          imageVector = Icons.Filled.Close,
          contentDescription = stringResource(Res.string.close),
          tint = if (mobilePlatform == MobileUiPlatform.IOS) c.muted else overlayBody,
          modifier = Modifier.size(20.dp).clickable(onClick = onDismiss),
        )
      }

      // Información de la estación
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = station.name,
          style = MaterialTheme.typography.titleLarge,
          color = overlayTitle,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = station.address,
          style = MaterialTheme.typography.bodySmall,
          color = overlayBody,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }

      // Resumen con distancia, bicis y slots
      Text(
        text =
          if (isFallbackSelection) {
            stringResource(
              Res.string.mapNearestFallbackSummary,
              formatDistance(station.distanceMeters),
              station.bikesAvailable,
              station.slotsFree,
            )
          } else {
            stringResource(
              Res.string.mapStationDistanceSummary,
              formatDistance(station.distanceMeters),
              station.bikesAvailable,
              station.slotsFree,
            )
          },
        color = overlayBody,
        style = MaterialTheme.typography.bodyMedium,
      )

      // Botones de acción
      Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        RoutePill(
          label = stringResource(Res.string.route),
          onDarkBackground = mobilePlatform != MobileUiPlatform.IOS,
          onClick = { onQuickRoute(station) },
        )
        if (mobilePlatform == MobileUiPlatform.IOS) {
          FavoritePill(
            active = isFavorite,
            onClick = onFavoriteToggle,
            label = if (isFavorite) stringResource(Res.string.saved) else stringResource(Res.string.save),
          )
        } else {
          OutlineActionPill(
            label = if (isFavorite) stringResource(Res.string.saved) else stringResource(Res.string.save),
            tint = c.onAccent,
            borderTint = c.onAccent.copy(alpha = 0.32f),
            onClick = onFavoriteToggle,
          )
        }
        OutlineActionPill(
          label = stringResource(Res.string.details),
          tint = if (mobilePlatform == MobileUiPlatform.IOS) c.red else c.onAccent,
          borderTint =
            if (mobilePlatform ==
              MobileUiPlatform.IOS
            ) {
              c.red.copy(alpha = 0.16f)
            } else {
              c.onAccent.copy(alpha = 0.32f)
            },
          onClick = { onOpenStationDetails(station) },
        )
      }
    }
  }
}

@Composable
private fun getHeaderLabel(
  isFallbackSelection: Boolean,
  isShowingNearestSelection: Boolean,
  searchRadiusMeters: Int,
): String =
  when {
    isFallbackSelection -> stringResource(Res.string.mapNoStationsWithinRadius, searchRadiusMeters)
    isShowingNearestSelection -> stringResource(Res.string.mapNearestStationLabel)
    else -> stringResource(Res.string.mapSelectedStationLabel)
  }
