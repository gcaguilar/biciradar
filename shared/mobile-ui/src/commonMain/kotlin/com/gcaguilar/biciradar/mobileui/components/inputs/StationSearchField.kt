package com.gcaguilar.biciradar.mobileui.components.inputs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
  modifier: Modifier = Modifier,
) {
  val c = LocalBiziColors.current
  val isIos = mobilePlatform == MobileUiPlatform.IOS
  val fieldSurface = if (isIos) c.fieldSurfaceIos else c.fieldSurfaceAndroid
  OutlinedTextField(
    modifier = modifier.fillMaxWidth(),
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
          IconButton(onClick = { onValueChange("") }) {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = stringResource(Res.string.clearSearch),
              tint = c.muted,
            )
          }
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
    // Android floats the label; repeating it as placeholder showed the text twice
    // when focused. iOS has no label, so the placeholder carries it.
    placeholder =
      if (mobilePlatform == MobileUiPlatform.Android) {
        null
      } else {
        { Text(label, color = c.muted) }
      },
    colors =
      OutlinedTextFieldDefaults.colors(
        focusedContainerColor = fieldSurface,
        unfocusedContainerColor = fieldSurface,
        // Material focus indicator: full primary on Android, subtle on iOS.
        focusedBorderColor = if (isIos) c.red.copy(alpha = 0.18f) else c.red,
        unfocusedBorderColor = if (isIos) c.panel else c.muted.copy(alpha = 0.18f),
        focusedTextColor = c.ink,
        unfocusedTextColor = c.ink,
        focusedLabelColor = if (isIos) c.ink else c.red,
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
