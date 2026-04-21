package com.gcaguilar.biciradar.mobileui.components.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobileui.LocalBiziColors

/**
 * Header de sección con título y badge de conteo.
 *
 * @param title Título de la sección
 * @param countLabel Texto del badge de conteo
 */
@Composable
internal fun SectionHeader(
  title: String,
  countLabel: String,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.SemiBold,
    )
    CountBadge(label = countLabel)
  }
}

/**
 * Badge de contador para mostrar cantidades.
 *
 * @param label Texto del badge (ej: "5 alertas")
 * @param modifier Modificador opcional
 */
@Composable
internal fun CountBadge(
  label: String,
  modifier: Modifier = Modifier,
) {
  val colors = LocalBiziColors.current
  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.extraLarge,
    color = colors.panel.copy(alpha = 0.7f),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = colors.muted,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
    )
  }
}

/**
 * Pill de sección para etiquetar grupos de elementos.
 *
 * @param label Texto de la sección
 * @param tint Color del tinte
 * @param modifier Modificador opcional
 */
@Composable
internal fun SectionPill(
  label: String,
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.extraLarge,
    color = tint.copy(alpha = 0.10f),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = tint,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
    )
  }
}

/**
 * Pill de estado para mostrar información de estado.
 *
 * @param label Texto del estado
 * @param active Si está activo o no
 * @param onClick Acción al hacer clic
 * @param modifier Modificador opcional
 */
@Composable
internal fun StatusPill(
  label: String,
  active: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalBiziColors.current
  Surface(
    modifier = modifier.clickable(onClick = onClick),
    shape = MaterialTheme.shapes.extraLarge,
    color = if (active) colors.blue.copy(alpha = 0.10f) else colors.panel.copy(alpha = 0.7f),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = if (active) colors.blue else colors.muted,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
    )
  }
}
