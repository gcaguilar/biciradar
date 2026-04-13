package com.gcaguilar.biciradar.mobileui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobileui.LocalBiziColors

@Composable
internal fun EmptyStatePlaceholder(
  title: String,
  description: String,
  primaryAction: String? = null,
  onPrimaryAction: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  val c = LocalBiziColors.current
  Card(
    modifier =
      modifier.animateContentSize(
        animationSpec =
          androidx.compose.animation.core
            .spring(dampingRatio = 0.9f, stiffness = 500f),
      ),
    colors = CardDefaults.cardColors(containerColor = c.surface),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(title, fontWeight = FontWeight.SemiBold, color = c.ink)
      Text(description, style = MaterialTheme.typography.bodySmall, color = c.muted)
      if (primaryAction != null && onPrimaryAction != null) {
        OutlinedButton(
          onClick = onPrimaryAction,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = c.red),
        ) {
          Text(primaryAction)
        }
      }
    }
  }
}
