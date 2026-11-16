package com.gcaguilar.biciradar.mobileui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Material 3 switch with a check icon on the thumb when on, and a [label] read by
 * screen readers so the switch is not announced only as "on"/"off".
 */
@Composable
internal fun BiziSwitch(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  label: String,
  modifier: Modifier = Modifier,
) {
  Switch(
    checked = checked,
    onCheckedChange = onCheckedChange,
    modifier = modifier.semantics { contentDescription = label },
    thumbContent =
      if (checked) {
        {
          Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            modifier = Modifier.size(SwitchDefaults.IconSize),
          )
        }
      } else {
        null
      },
  )
}
