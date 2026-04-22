package com.gcaguilar.biciradar.mobileui.components.cards

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobileui.BiziCard
import com.gcaguilar.biciradar.core.NearbyStationSelection
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.formatDistance
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.openRoute
import com.gcaguilar.biciradar.mobile_ui.generated.resources.quickRouteDistanceSummary
import com.gcaguilar.biciradar.mobile_ui.generated.resources.quickRouteFallbackSummary
import com.gcaguilar.biciradar.mobile_ui.generated.resources.refreshStationsToOpenRoute
import com.gcaguilar.biciradar.mobileui.LocalBiziCardShape
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import org.jetbrains.compose.resources.stringResource

/**
 * Tarjeta de acción rápida de ruta.
 * Muestra la estación más cercana con bicicletas o slots disponibles.
 */
@Composable
internal fun QuickRouteActionCard(
  modifier: Modifier,
  title: String,
  emptyTitle: String,
  selection: NearbyStationSelection,
  icon: ImageVector,
  tint: Color,
  mobilePlatform: MobileUiPlatform,
  onRoute: (Station) -> Unit,
) {
  val station = selection.highlightedStation
  BiziCard(
    modifier =
      modifier
        .clickable(enabled = station != null) {
          station?.let(onRoute)
        },
    shape = LocalBiziCardShape.current,
  ) {
    Column(
      modifier =
        Modifier
          .padding(14.dp)
          .animateContentSize(animationSpec = spring(dampingRatio = 0.88f, stiffness = 520f)),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
      Text(title, style = MaterialTheme.typography.labelSmall, color = LocalBiziColors.current.muted)
      if (station == null) {
        Text(
          emptyTitle,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = tint,
        )
        Text(
          stringResource(Res.string.refreshStationsToOpenRoute),
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      } else {
        Text(
          station.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = tint,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          if (selection.usesFallback) {
            stringResource(
              Res.string.quickRouteFallbackSummary,
              formatDistance(selection.radiusMeters),
              formatDistance(station.distanceMeters),
            )
          } else {
            stringResource(
              Res.string.quickRouteDistanceSummary,
              formatDistance(station.distanceMeters),
              station.bikesAvailable,
              station.slotsFree,
            )
          },
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
        Text(
          stringResource(Res.string.openRoute),
          style = MaterialTheme.typography.labelMedium,
          color = tint,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }
  }
}
