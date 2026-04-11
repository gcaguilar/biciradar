package com.gcaguilar.biciradar.mobileui.components.station

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsBell
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StationDetailAlertBell(
  label: String,
  active: Boolean,
  onClick: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
      Icon(
        imageVector = if (active) Icons.Filled.Notifications else Icons.Outlined.Notifications,
        contentDescription = stringResource(Res.string.savedPlaceAlertsBell),
        tint = if (active) LocalBiziColors.current.blue else LocalBiziColors.current.muted,
      )
    }
    Text(
      label,
      style = MaterialTheme.typography.labelSmall,
      color = LocalBiziColors.current.muted,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
