package com.gcaguilar.biciradar.mobileui.components.station

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.StationHourlyPattern
import com.gcaguilar.biciradar.mobileui.LocalBiziColors

@Composable
internal fun StationPatternChart(
  patterns: List<StationHourlyPattern>,
  modifier: Modifier = Modifier,
) {
  val colors = LocalBiziColors.current
  val barColor = colors.red
  val labelColor = colors.muted
  val gridColor = colors.muted.copy(alpha = 0.2f)
  val textMeasurer = rememberTextMeasurer()
  val labelStyle = MaterialTheme.typography.labelSmall.copy(color = labelColor)
  val maxBikes = patterns.maxOfOrNull { it.bikesAvg }?.coerceAtLeast(1.0) ?: 1.0

  Canvas(modifier = modifier) {
    val bottomPadding = 24f
    val topPadding = 8f
    val leftPadding = 0f
    val chartHeight = size.height - bottomPadding - topPadding
    val chartWidth = size.width - leftPadding
    val barCount = patterns.size
    val totalBarSpace = chartWidth / barCount
    val barWidth = (totalBarSpace * 0.65f).coerceAtMost(20f)
    val gap = totalBarSpace - barWidth

    // Horizontal grid lines
    val gridLines = 3
    for (i in 1..gridLines) {
      val y = topPadding + chartHeight * (1f - i.toFloat() / (gridLines + 1))
      drawLine(
        color = gridColor,
        start = Offset(leftPadding, y),
        end = Offset(size.width, y),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
      )
    }

    // Bars and labels
    patterns.forEachIndexed { index, pattern ->
      val x = leftPadding + index * totalBarSpace + gap / 2
      val ratio = (pattern.bikesAvg / maxBikes).toFloat()
      val barHeight = chartHeight * ratio
      val barY = topPadding + chartHeight - barHeight

      drawRoundRect(
        color = barColor,
        topLeft = Offset(x, barY),
        size = Size(barWidth, barHeight),
        cornerRadius = CornerRadius(barWidth / 4, barWidth / 4),
      )

      // Hour label every 3 hours
      if (pattern.hour % 3 == 0) {
        val text = "${pattern.hour}h"
        val measured = textMeasurer.measure(text, labelStyle)
        drawText(
          textLayoutResult = measured,
          topLeft = Offset(
            x + barWidth / 2 - measured.size.width / 2,
            size.height - bottomPadding + 4f,
          ),
        )
      }
    }
  }
}
