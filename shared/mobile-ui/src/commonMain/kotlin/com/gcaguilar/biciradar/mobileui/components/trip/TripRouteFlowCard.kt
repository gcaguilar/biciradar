package com.gcaguilar.biciradar.mobileui.components.trip

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.formatDistance
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripCurrentLocationLabel
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripRouteFlowTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripStationDistanceInBike
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripWalkingTimeToDestination
import com.gcaguilar.biciradar.mobileui.BiziCard
import com.gcaguilar.biciradar.mobileui.BiziSpacing
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import org.jetbrains.compose.resources.stringResource

/**
 * Tarjeta "Ruta sugerida": una línea de tiempo vertical (origen -> estación -> destino)
 * junto a los tres pasos de la ruta (ubicación actual, estación intermedia, destino).
 *
 * Nota: en este design system `colors.red` es el azul primario de marca (#1D74BD).
 */
@Composable
internal fun TripRouteFlowCard(
  station: Station,
  distanceToStationMeters: Int?,
  destinationName: String,
  walkingMinutesToDestination: Int?,
) {
  val c = LocalBiziColors.current
  BiziCard {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(BiziSpacing.large),
    ) {
      Text(
        text = stringResource(Res.string.tripRouteFlowTitle).uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = c.muted,
        fontWeight = FontWeight.SemiBold,
      )
      Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(BiziSpacing.xLarge),
      ) {
        RouteTimeline(
          modifier =
            Modifier
              .fillMaxHeight()
              .width(20.dp),
          originTint = c.red,
          destinationTint = c.green,
        )
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(BiziSpacing.large),
        ) {
          Text(
            text = stringResource(Res.string.tripCurrentLocationLabel),
            style = MaterialTheme.typography.bodyMedium,
            color = c.muted,
          )
          Surface(
            shape = MaterialTheme.shapes.medium,
            color = c.red.copy(alpha = 0.08f),
          ) {
            Column(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = station.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
              )
              if (distanceToStationMeters != null) {
                Text(
                  text =
                    stringResource(
                      Res.string.tripStationDistanceInBike,
                      formatDistance(distanceToStationMeters),
                    ),
                  style = MaterialTheme.typography.bodySmall,
                  color = c.muted,
                )
              }
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RouteMiniPill(text = "${station.slotsFree} 🅿", tint = c.blue)
                RouteMiniPill(text = "${station.bikesAvailable} 🚲", tint = c.red)
              }
            }
          }
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              text = destinationName,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
            )
            if (walkingMinutesToDestination != null) {
              Text(
                text = stringResource(Res.string.tripWalkingTimeToDestination, walkingMinutesToDestination),
                style = MaterialTheme.typography.bodySmall,
                color = c.muted,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RouteMiniPill(
  text: String,
  tint: Color,
) {
  Surface(shape = MaterialTheme.shapes.small, color = tint.copy(alpha = 0.12f)) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = tint,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    )
  }
}

@Composable
private fun RouteTimeline(
  modifier: Modifier = Modifier,
  originTint: Color,
  destinationTint: Color,
) {
  Canvas(modifier = modifier) {
    val dotRadius = 5.dp.toPx()
    val centerX = size.width / 2f
    val topY = dotRadius
    val bottomY = size.height - dotRadius
    val midY = size.height / 2f

    drawLine(
      brush = Brush.verticalGradient(listOf(originTint, originTint), startY = topY, endY = midY),
      start = Offset(centerX, topY),
      end = Offset(centerX, midY),
      strokeWidth = 3.dp.toPx(),
    )
    drawLine(
      brush = Brush.verticalGradient(listOf(originTint, destinationTint), startY = midY, endY = bottomY),
      start = Offset(centerX, midY),
      end = Offset(centerX, bottomY),
      strokeWidth = 3.dp.toPx(),
    )
    drawCircle(color = originTint, radius = dotRadius, center = Offset(centerX, topY))
    drawCircle(color = Color.White, radius = dotRadius, center = Offset(centerX, midY))
    drawCircle(
      color = originTint,
      radius = dotRadius,
      center = Offset(centerX, midY),
      style = Stroke(width = 2.dp.toPx()),
    )
    drawCircle(color = destinationTint, radius = dotRadius, center = Offset(centerX, bottomY))
  }
}
