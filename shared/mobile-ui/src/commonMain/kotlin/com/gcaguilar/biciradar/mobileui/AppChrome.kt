package com.gcaguilar.biciradar.mobileui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.gcaguilar.biciradar.mobile_ui.generated.resources.*
import org.jetbrains.compose.resources.stringResource

internal sealed interface TopUpdateBanner {
  data object Hidden : TopUpdateBanner

  data class Available(
    val version: String,
    val flexible: Boolean,
    val storeUrl: String?,
  ) : TopUpdateBanner

  data class Downloaded(
    val version: String,
  ) : TopUpdateBanner
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BoxScope.EngagementTopOverlays(
  updateBanner: TopUpdateBanner,
  showFeedbackNudge: Boolean,
  onDismissAvailableUpdate: (String) -> Unit,
  onDismissDownloadedUpdate: () -> Unit,
  onStartUpdate: () -> Unit,
  onRestartToUpdate: () -> Unit,
  onFeedbackSend: () -> Unit,
  onFeedbackDismiss: () -> Unit,
) {
  val colors = LocalBiziColors.current
  Column(
    modifier =
      Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 12.dp, vertical = 8.dp)
        .zIndex(4f),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    when (val banner = updateBanner) {
      TopUpdateBanner.Hidden -> Unit
      is TopUpdateBanner.Available -> {
        Surface(
          color = colors.blue.copy(alpha = 0.12f),
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = stringResource(Res.string.updateAvailableTitle),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = colors.ink,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              TextButton(onClick = onStartUpdate) {
                Text(stringResource(Res.string.updateNow))
              }
              TextButton(onClick = { onDismissAvailableUpdate(banner.version) }) {
                Text(stringResource(Res.string.updateDismiss))
              }
            }
          }
        }
      }

      is TopUpdateBanner.Downloaded -> {
        Surface(
          color = colors.green.copy(alpha = 0.12f),
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = stringResource(Res.string.restartToUpdate),
              style = MaterialTheme.typography.bodyMedium,
              color = colors.ink,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              TextButton(onClick = onRestartToUpdate) {
                Text(stringResource(Res.string.restartToUpdate))
              }
              TextButton(onClick = onDismissDownloadedUpdate) {
                Text(stringResource(Res.string.close))
              }
            }
          }
        }
      }
    }

    if (showFeedbackNudge) {
      FeedbackNudgeCard(
        onFeedbackSend = onFeedbackSend,
        onFeedbackDismiss = onFeedbackDismiss,
      )
    }
  }
}

@Composable
internal fun FeedbackNudgeCard(
  onFeedbackSend: () -> Unit,
  onFeedbackDismiss: () -> Unit,
) {
  val colors = LocalBiziColors.current
  Surface(
    color = colors.surface,
    shape = MaterialTheme.shapes.medium,
    border = BorderStroke(1.dp, colors.panel),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = stringResource(Res.string.feedbackNudgeTitle),
        fontWeight = FontWeight.SemiBold,
        color = colors.ink,
      )
      Text(
        text = stringResource(Res.string.feedbackNudgeBody),
        style = MaterialTheme.typography.bodySmall,
        color = colors.muted,
      )
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        TextButton(onClick = onFeedbackSend) {
          Text(stringResource(Res.string.feedbackNudgeAction))
        }
        TextButton(onClick = onFeedbackDismiss) {
          Text(stringResource(Res.string.updateDismiss))
        }
      }
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun FeedbackBottomSheet(
  onDismiss: () -> Unit,
  onOpenFeedbackForm: () -> Unit,
) {
  val colors = LocalBiziColors.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = colors.surface,
  ) {
    Column(
      modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(
        text = stringResource(Res.string.feedbackAndSuggestions),
        color = colors.ink,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.titleLarge,
      )
      Text(
        text = stringResource(Res.string.feedbackDescription),
        color = colors.muted,
      )
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onOpenFeedbackForm) {
          Text(stringResource(Res.string.openFeedbackForm))
        }
        TextButton(onClick = onDismiss) {
          Text(stringResource(Res.string.close))
        }
      }
    }
  }
}

@Composable
internal fun StartupSplashScreen(mobilePlatform: MobileUiPlatform) {
  val colors = LocalBiziColors.current
  val backgroundColor =
    if (mobilePlatform == MobileUiPlatform.IOS) {
      colors.groupedBackground
    } else {
      colors.background
    }
  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(backgroundColor),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Surface(
        shape = CircleShape,
        color = colors.red,
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
          contentDescription = null,
          tint = colors.onAccent,
          modifier = Modifier.padding(18.dp).size(30.dp),
        )
      }
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Text(
          text = stringResource(Res.string.appName),
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = colors.red,
        )
        Text(
          text = stringResource(Res.string.loadingStationsFavoritesShortcuts),
          style = MaterialTheme.typography.bodyMedium,
          color = colors.muted,
        )
      }
      Text(
        text =
          if (mobilePlatform == MobileUiPlatform.IOS) {
            stringResource(Res.string.preparingIphoneExperience)
          } else {
            stringResource(Res.string.preparingAndroidExperience)
          },
        style = MaterialTheme.typography.labelMedium,
        color = colors.muted,
      )
    }
  }
}
