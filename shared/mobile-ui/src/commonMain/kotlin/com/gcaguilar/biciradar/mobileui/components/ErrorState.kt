package com.gcaguilar.biciradar.mobileui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobileui.LocalBiziColors

@Composable
internal fun ErrorState(
  title: String,
  description: String,
  primaryAction: String? = null,
  onPrimaryAction: (() -> Unit)? = null,
  secondaryAction: String? = null,
  onSecondaryAction: (() -> Unit)? = null,
) {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
      color = LocalBiziColors.current.ink,
      textAlign = TextAlign.Center,
    )
    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium,
      color = LocalBiziColors.current.muted,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(8.dp))
    if (primaryAction != null && onPrimaryAction != null) {
      Button(
        onClick = onPrimaryAction,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.size(8.dp))
        Text(primaryAction)
      }
    }
    if (secondaryAction != null && onSecondaryAction != null) {
      OutlinedButton(
        onClick = onSecondaryAction,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(secondaryAction)
      }
    }
  }
}
