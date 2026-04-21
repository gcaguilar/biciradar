package com.gcaguilar.biciradar.mobileui.components.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.clearSearch
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import org.jetbrains.compose.resources.stringResource

/**
 * Campo de búsqueda de estaciones con estilo consistente.
 */
@Composable
internal fun StationSearchField(
  mobilePlatform: MobileUiPlatform,
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
) {
  val c = LocalBiziColors.current
  OutlinedTextField(
    modifier = Modifier.fillMaxWidth(),
    value = value,
    onValueChange = onValueChange,
    singleLine = true,
    shape = MaterialTheme.shapes.extraLarge,
    leadingIcon = {
      Icon(Icons.Filled.Search, contentDescription = null, tint = c.muted)
    },
    trailingIcon =
      if (value.isNotEmpty()) {
        {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = stringResource(Res.string.clearSearch),
            tint = c.muted,
            modifier = Modifier.clickable { onValueChange("") },
          )
        }
      } else {
        null
      },
    label =
      if (mobilePlatform == MobileUiPlatform.Android) {
        { Text(label) }
      } else {
        null
      },
    placeholder = { Text(label, color = c.muted) },
    colors =
      OutlinedTextFieldDefaults.colors(
        focusedContainerColor =
          if (mobilePlatform ==
            MobileUiPlatform.IOS
          ) {
            c.fieldSurfaceIos
          } else {
            c.fieldSurfaceAndroid
          },
        unfocusedContainerColor =
          if (mobilePlatform ==
            MobileUiPlatform.IOS
          ) {
            c.fieldSurfaceIos
          } else {
            c.fieldSurfaceAndroid
          },
        focusedBorderColor = c.red.copy(alpha = if (mobilePlatform == MobileUiPlatform.IOS) 0.18f else 0.30f),
        unfocusedBorderColor = if (mobilePlatform == MobileUiPlatform.IOS) c.panel else c.muted.copy(alpha = 0.18f),
        focusedTextColor = c.ink,
        unfocusedTextColor = c.ink,
        focusedLabelColor = c.ink,
        unfocusedLabelColor = c.muted,
        focusedPlaceholderColor = c.muted,
        unfocusedPlaceholderColor = c.muted,
        focusedLeadingIconColor = c.muted,
        unfocusedLeadingIconColor = c.muted,
        focusedTrailingIconColor = c.muted,
        unfocusedTrailingIconColor = c.muted,
      ),
  )
}
