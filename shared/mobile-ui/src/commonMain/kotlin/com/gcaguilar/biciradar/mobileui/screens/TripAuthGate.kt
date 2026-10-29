package com.gcaguilar.biciradar.mobileui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripAccountAction
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripAccountError
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripAccountRequired
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripAccountTitle
import com.gcaguilar.biciradar.mobileui.BiziSpacing
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import org.jetbrains.compose.resources.stringResource

/**
 * Pantalla bloqueante de la pestaña Viaje: sin sesión no se puede vigilar, así
 * que sustituye por completo al contenido de viaje hasta iniciar sesión o crear
 * una cuenta.
 */
@Composable
internal fun TripAuthGate(
  mobilePlatform: MobileUiPlatform,
  loginInProgress: Boolean,
  loginError: Boolean,
  paddingValues: PaddingValues,
  onLogin: () -> Unit,
) {
  val colors = LocalBiziColors.current
  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(pageBackgroundColor(mobilePlatform)),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier
          .responsivePageWidth()
          .padding(BiziSpacing.screenPadding),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(BiziSpacing.xLarge),
    ) {
      Icon(
        imageVector = Icons.Filled.AccountCircle,
        contentDescription = null,
        tint = colors.red,
        modifier = Modifier.size(64.dp),
      )
      Text(
        text = stringResource(Res.string.tripAccountTitle),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
      )
      Text(
        text = stringResource(Res.string.tripAccountRequired),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.muted,
        textAlign = TextAlign.Center,
      )
      Button(
        onClick = onLogin,
        enabled = !loginInProgress,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = colors.red, contentColor = colors.onAccent),
      ) {
        if (loginInProgress) {
          CircularProgressIndicator(
            modifier =
              Modifier
                .size(18.dp)
                .padding(end = 8.dp),
            strokeWidth = 2.dp,
            color = colors.onAccent,
          )
        }
        Text(stringResource(Res.string.tripAccountAction))
      }
      if (loginError) {
        Text(
          text = stringResource(Res.string.tripAccountError),
          style = MaterialTheme.typography.bodySmall,
          color = colors.red,
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}
