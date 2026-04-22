package com.gcaguilar.biciradar.mobileui.components.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.gcaguilar.biciradar.mobileui.BiziSpacing
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.biziCardBorder

@Composable
internal fun SuggestionRow(
  title: String,
  secondaryText: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalBiziColors.current
  Surface(
    modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
    shape = MaterialTheme.shapes.medium,
    color = colors.background,
    border = biziCardBorder(),
  ) {
    Row(
      modifier = Modifier.padding(BiziSpacing.xLarge),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(BiziSpacing.large),
    ) {
      Icon(
        imageVector = Icons.Filled.LocationOn,
        contentDescription = null,
        tint = colors.muted,
        modifier = Modifier.size(BiziSpacing.cardPadding),
      )
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(BiziSpacing.xxSmall),
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        secondaryText?.let {
          Text(
            text = it,
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}
