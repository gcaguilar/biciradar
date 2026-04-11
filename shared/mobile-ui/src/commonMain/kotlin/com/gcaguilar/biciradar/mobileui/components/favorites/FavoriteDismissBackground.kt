package com.gcaguilar.biciradar.mobileui.components.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.deleteFavorite
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import org.jetbrains.compose.resources.stringResource

/**
 * Background del swipe-to-dismiss para favoritos.
 *
 * Muestra un indicador visual rojo cuando el usuario hace swipe para eliminar.
 *
 * @param mobilePlatform La plataforma actual (afecta el radio de esquinas)
 * @param progress Progreso del swipe (0f a 1f)
 */
@Composable
internal fun FavoriteDismissBackground(
  mobilePlatform: MobileUiPlatform,
  progress: Float,
) {
  val clampedProgress = progress.coerceIn(0f, 1f)
  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 22.dp else 24.dp))
        .background(LocalBiziColors.current.red.copy(alpha = 0.10f + (0.10f * clampedProgress)))
        .padding(horizontal = 20.dp, vertical = 12.dp),
    contentAlignment = Alignment.CenterEnd,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Icon(
        imageVector = Icons.Default.Delete,
        contentDescription = null,
        tint = LocalBiziColors.current.red,
        modifier = Modifier.size(24.dp),
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = stringResource(Res.string.deleteFavorite),
        style = MaterialTheme.typography.labelLarge,
        color = LocalBiziColors.current.red,
      )
    }
  }
}
