package com.gcaguilar.biciradar.mobileui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.environmentalLegendGood
import com.gcaguilar.biciradar.mobile_ui.generated.resources.environmentalLegendHigh
import com.gcaguilar.biciradar.mobile_ui.generated.resources.environmentalLegendLow
import com.gcaguilar.biciradar.mobile_ui.generated.resources.environmentalLegendMedium
import com.gcaguilar.biciradar.mobile_ui.generated.resources.environmentalLegendModerate
import com.gcaguilar.biciradar.mobile_ui.generated.resources.environmentalLegendPoor
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapClearEnvironmentalLayer
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapEnvironmentalLayerHint
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapFilterAirQuality
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapFilterPollen
import com.gcaguilar.biciradar.mobileui.BiziCard
import com.gcaguilar.biciradar.mobileui.BiziDataColors
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MapEnvironmentalLayer
import com.gcaguilar.biciradar.mobileui.MapEnvironmentalZoneSnapshot
import org.jetbrains.compose.resources.stringResource

/**
 * Tarjeta de capa ambiental para mostrar información de calidad del aire o polen.
 *
 * @param layer La capa ambiental activa
 * @param zones Lista de zonas ambientales
 * @param onClear Callback al limpiar la capa
 */
@Composable
internal fun EnvironmentalLayerCard(
  layer: MapEnvironmentalLayer,
  zones: List<MapEnvironmentalZoneSnapshot>,
  onClear: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val c = LocalBiziColors.current
  BiziCard(
    modifier = modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text =
          when (layer) {
            MapEnvironmentalLayer.AirQuality -> stringResource(Res.string.mapFilterAirQuality)
            MapEnvironmentalLayer.Pollen -> stringResource(Res.string.mapFilterPollen)
          },
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.titleMedium,
      )
      Text(
        text = stringResource(Res.string.mapEnvironmentalLayerHint),
        style = MaterialTheme.typography.bodySmall,
        color = c.muted,
      )
      EnvironmentalLegendRow(layer = layer)

      // Lista de zonas (máximo 4)
      zones.take(4).forEach { zone ->
        val score =
          when (layer) {
            MapEnvironmentalLayer.AirQuality -> zone.airQualityScore
            MapEnvironmentalLayer.Pollen -> zone.pollenScore
          }
        val tone = environmentalToneForLayer(layer = layer, score = score, muted = c.muted)
        val valueText =
          when {
            score == null -> "--"
            layer == MapEnvironmentalLayer.AirQuality -> "AQI $score"
            else -> "$score gr/m3"
          }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(zone.zoneLabel, style = MaterialTheme.typography.bodySmall, color = c.ink)
          Text(
            valueText,
            style = MaterialTheme.typography.bodySmall,
            color = tone,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }

      TextButton(onClick = onClear, contentPadding = PaddingValues(0.dp)) {
        Text(stringResource(Res.string.mapClearEnvironmentalLayer))
      }
    }
  }
}

@Composable
private fun EnvironmentalLegendRow(layer: MapEnvironmentalLayer) {
  val c = LocalBiziColors.current
  val labels =
    when (layer) {
      MapEnvironmentalLayer.AirQuality ->
        listOf(
          stringResource(Res.string.environmentalLegendGood),
          stringResource(Res.string.environmentalLegendModerate),
          stringResource(Res.string.environmentalLegendPoor),
        )
      MapEnvironmentalLayer.Pollen ->
        listOf(
          stringResource(Res.string.environmentalLegendLow),
          stringResource(Res.string.environmentalLegendMedium),
          stringResource(Res.string.environmentalLegendHigh),
        )
    }
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val legendColors =
      when (layer) {
        MapEnvironmentalLayer.AirQuality ->
          listOf(
            BiziDataColors.AqiGood,
            BiziDataColors.AqiModerate,
            BiziDataColors.AqiBad,
          )
        MapEnvironmentalLayer.Pollen ->
          listOf(
            BiziDataColors.PollenLow,
            BiziDataColors.PollenMedium,
            BiziDataColors.PollenHigh,
          )
      }
    legendColors.zip(labels).forEach { (color, label) ->
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        MapColorDot(color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = c.muted)
      }
    }
  }
}

@Composable
private fun MapColorDot(
  color: Color,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .size(10.dp)
        .clip(CircleShape)
        .background(color),
  )
}

private fun environmentalToneForLayer(
  layer: MapEnvironmentalLayer,
  score: Int?,
  muted: Color,
): Color =
  when {
    score == null -> muted
    layer == MapEnvironmentalLayer.AirQuality && score <= 50 -> BiziDataColors.AqiGood
    layer == MapEnvironmentalLayer.AirQuality && score <= 100 -> BiziDataColors.AqiModerate
    layer == MapEnvironmentalLayer.AirQuality -> BiziDataColors.AqiBad
    layer == MapEnvironmentalLayer.Pollen && score <= 10 -> BiziDataColors.PollenLow
    layer == MapEnvironmentalLayer.Pollen && score <= 30 -> BiziDataColors.PollenMedium
    else -> BiziDataColors.PollenHigh
  }
