package com.gcaguilar.biciradar.mobileui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobileui.LocalBiziColors

/**
 * Marcador de estación en el mapa.
 *
 * @param bikesAvailable Número de bicicletas disponibles
 * @param slotsFree Número de slots libres
 * @param isHighlighted Si el marcador está destacado/seleccionado
 * @param isFavorite Si la estación es favorita
 * @param onClick Callback al hacer click
 */
@Composable
internal fun StationMapMarker(
  bikesAvailable: Int,
  slotsFree: Int,
  isHighlighted: Boolean,
  isFavorite: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val c = LocalBiziColors.current
  val total = bikesAvailable + slotsFree
  val occupancyRatio = if (total > 0) bikesAvailable.toFloat() / total else 0f

  // Color basado en disponibilidad
  val markerColor = when {
    bikesAvailable == 0 -> c.red
    occupancyRatio > 0.7f -> c.green
    occupancyRatio > 0.3f -> c.orange
    else -> c.blue
  }

  // Tamaño basado en si está destacado
  val size = if (isHighlighted) 44.dp else 36.dp
  val strokeWidth = if (isHighlighted) 3.dp else 2.dp

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(if (isFavorite) c.purple else markerColor)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = bikesAvailable.toString(),
      color = c.onAccent,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
    )
  }
}

/**
 * Marcador simple de punto para estaciones.
 *
 * @param isHighlighted Si el marcador está destacado
 * @param onClick Callback al hacer click
 */
@Composable
internal fun StationDotMarker(
  isHighlighted: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val c = LocalBiziColors.current
  val size = if (isHighlighted) 16.dp else 12.dp

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(c.red)
      .clickable(onClick = onClick),
  )
}
