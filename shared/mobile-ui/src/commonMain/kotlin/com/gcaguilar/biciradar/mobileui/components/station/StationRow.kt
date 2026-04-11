package com.gcaguilar.biciradar.mobileui.components.station

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.gcaguilar.biciradar.mobile_ui.generated.resources.bikes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.distance
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favorite
import com.gcaguilar.biciradar.mobile_ui.generated.resources.route
import com.gcaguilar.biciradar.mobile_ui.generated.resources.save
import com.gcaguilar.biciradar.mobile_ui.generated.resources.saved
import com.gcaguilar.biciradar.mobile_ui.generated.resources.slots
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StationRow(
  mobilePlatform: MobileUiPlatform,
  station: Station,
  isFavorite: Boolean,
  onClick: () -> Unit,
  onFavoriteToggle: () -> Unit,
  onQuickRoute: (() -> Unit)? = null,
  savedPlaceAlertSlot: @Composable (() -> Unit)? = null,
  extraActions: @Composable (() -> Unit)? = null,
  showFavoriteCta: Boolean = true,
) {
  Card(
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .animateContentSize(
          animationSpec =
            androidx.compose.animation.core
              .spring(dampingRatio = 0.9f, stiffness = 520f),
        ),
    shape = RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 22.dp else 24.dp),
    border =
      if (mobilePlatform ==
        MobileUiPlatform.IOS
      ) {
        BorderStroke(1.dp, LocalBiziColors.current.panel)
      } else {
        null
      },
    colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            station.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            station.address,
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
        Spacer(Modifier.width(12.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          onQuickRoute?.let { quickRoute ->
            RoutePill(
              label = stringResource(Res.string.route),
              onClick = quickRoute,
            )
          }
          savedPlaceAlertSlot?.invoke()
          if (showFavoriteCta) {
            FavoritePill(
              active = isFavorite,
              onClick = onFavoriteToggle,
              label =
                if (isFavorite) {
                  stringResource(
                    Res.string.saved,
                  )
                } else {
                  stringResource(Res.string.save)
                },
            )
          } else {
            FavoritePill(
              active = true,
              onClick = {},
              label = stringResource(Res.string.favorite),
            )
          }
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StationMetricPill(
          modifier = Modifier.weight(1f),
          label = stringResource(Res.string.bikes),
          value = station.bikesAvailable.toString(),
          tint = LocalBiziColors.current.red,
        )
        StationMetricPill(
          modifier = Modifier.weight(1f),
          label = stringResource(Res.string.slots),
          value = station.slotsFree.toString(),
          tint = LocalBiziColors.current.blue,
        )
        StationMetricPill(
          modifier = Modifier.weight(1f),
          label = stringResource(Res.string.distance),
          value = formatDistance(station.distanceMeters),
          tint = LocalBiziColors.current.green,
        )
      }
      extraActions?.let { actions ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          actions()
        }
      }
    }
  }
}
