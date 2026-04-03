package com.gcaguilar.biciradar.mobileui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.SEARCH_RADIUS_OPTIONS_METERS
import com.gcaguilar.biciradar.core.formatDistance
import com.gcaguilar.biciradar.mobileui.LocalBiziColors

@Composable
internal fun SearchRadiusSelector(
  selectedRadiusMeters: Int,
  onSearchRadiusSelected: (Int) -> Unit,
) {
  val colors = LocalBiziColors.current
  var expanded by remember { mutableStateOf(false) }
  Box {
    OutlinedButton(
      onClick = { expanded = true },
      modifier = Modifier.fillMaxWidth(),
      border = BorderStroke(1.dp, colors.panel),
      colors = ButtonDefaults.outlinedButtonColors(containerColor = colors.surface),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = formatDistance(selectedRadiusMeters),
          color = colors.ink,
        )
        Icon(
          imageVector = Icons.Filled.Navigation,
          contentDescription = null,
          tint = colors.red,
        )
      }
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.background(colors.surface),
    ) {
      SEARCH_RADIUS_OPTIONS_METERS.forEach { radius ->
        DropdownMenuItem(
          text = {
            Text(
              text = formatDistance(radius),
              color = if (radius == selectedRadiusMeters) colors.red else colors.ink,
              fontWeight = if (radius == selectedRadiusMeters) FontWeight.SemiBold else FontWeight.Normal,
            )
          },
          onClick = {
            expanded = false
            onSearchRadiusSelected(radius)
          },
        )
      }
    }
  }
}
