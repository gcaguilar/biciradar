package com.gcaguilar.biciradar.mobileui.components.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.gcaguilar.biciradar.mobileui.BiziCard
import com.gcaguilar.biciradar.mobileui.BiziSpacing
import com.gcaguilar.biciradar.mobileui.LocalBiziColors

@Composable
internal fun BiziSectionCard(
  title: String,
  modifier: Modifier = Modifier,
  description: String? = null,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
  onClick: (() -> Unit)? = null,
  contentSpacing: Dp = BiziSpacing.large,
  content: (@Composable ColumnScope.() -> Unit)? = null,
) {
  val contentModifier =
    if (onClick != null) {
      Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(BiziSpacing.cardPadding)
    } else {
      Modifier.padding(BiziSpacing.cardPadding)
    }

  BiziCard(modifier = modifier.fillMaxWidth()) {
    Column(
      modifier = contentModifier,
      verticalArrangement = Arrangement.spacedBy(contentSpacing),
    ) {
      Text(title, fontWeight = FontWeight.SemiBold)
      description?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      }
      content?.invoke(this)
      if (actionLabel != null && onAction != null) {
        Row {
          TextButton(onClick = onAction) {
            Text(actionLabel, style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    }
  }
}
