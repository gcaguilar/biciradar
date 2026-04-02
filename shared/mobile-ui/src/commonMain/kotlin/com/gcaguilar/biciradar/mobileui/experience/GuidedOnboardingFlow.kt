package com.gcaguilar.biciradar.mobileui.experience

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.*
import org.jetbrains.compose.resources.stringResource

enum class GuidedOnboardingStep {
  LocationPermission,
  NotificationsPermission,
  FirstFavorite,
  SavedPlaces,
  Surfaces,
  Completed,
}

data class GuidedOnboardingCallbacks(
  val onRequestLocationPermission: () -> Unit,
  val onDismissLocationStep: () -> Unit,
  val onRequestNotificationsPermission: () -> Unit,
  val onDismissNotificationsStep: () -> Unit,
  val onOpenFavorites: () -> Unit,
  val onDismissFirstFavoriteStep: () -> Unit,
  val onDismissSavedPlacesStep: () -> Unit,
  val onCompleteSurfacesStep: () -> Unit,
)

internal fun OnboardingChecklistSnapshot.guidedOnboardingStep(): GuidedOnboardingStep = when {
  !locationDecisionMade -> GuidedOnboardingStep.LocationPermission
  !notificationsDecisionMade -> GuidedOnboardingStep.NotificationsPermission
  !firstStationSaved -> GuidedOnboardingStep.FirstFavorite
  !savedPlacesConfigured -> GuidedOnboardingStep.SavedPlaces
  !surfacesDiscovered -> GuidedOnboardingStep.Surfaces
  else -> GuidedOnboardingStep.Completed
}

@Composable
fun GuidedOnboardingFlow(
  checklist: OnboardingChecklistSnapshot,
  callbacks: GuidedOnboardingCallbacks,
) {
  val step = checklist.guidedOnboardingStep()
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .windowInsetsPadding(WindowInsets.statusBars)
      .padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    when (step) {
      GuidedOnboardingStep.LocationPermission -> {
        Text(stringResource(Res.string.onboardingLocationTitle), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(Res.string.onboardingLocationBody), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(
          onClick = callbacks.onRequestLocationPermission,
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingAllow)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
          onClick = callbacks.onDismissLocationStep,
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingLater)) }
      }

      GuidedOnboardingStep.NotificationsPermission -> {
        Text(stringResource(Res.string.onboardingNotificationsTitle), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(Res.string.onboardingNotificationsBody), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(
          onClick = callbacks.onRequestNotificationsPermission,
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingAllow)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
          onClick = callbacks.onDismissNotificationsStep,
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingLater)) }
      }

      GuidedOnboardingStep.FirstFavorite -> {
        Text(stringResource(Res.string.onboardingFavoriteTitle), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(Res.string.onboardingFavoriteBody), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(
          onClick = callbacks.onOpenFavorites,
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingGoToFavorites)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
          onClick = callbacks.onDismissFirstFavoriteStep,
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingLater)) }
      }

      GuidedOnboardingStep.SavedPlaces -> {
        Text(stringResource(Res.string.onboardingSavedPlacesTitle), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(Res.string.onboardingSavedPlacesBody), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(
          onClick = callbacks.onOpenFavorites,
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingGoToFavorites)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
          onClick = callbacks.onDismissSavedPlacesStep,
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingLater)) }
      }

      GuidedOnboardingStep.Surfaces -> {
        Text(stringResource(Res.string.onboardingSurfacesTitle), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(Res.string.onboardingSurfacesBody), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(
          onClick = callbacks.onCompleteSurfacesStep,
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingFinish)) }
      }

      GuidedOnboardingStep.Completed -> Unit
    }
  }
}
