package com.gcaguilar.biciradar.mobileui.experience

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.mobile_ui.generated.resources.*
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import org.jetbrains.compose.resources.stringResource

enum class GuidedOnboardingStep {
  FeatureHighlights,
  LocationPermission,
  NotificationsPermission,
  Surfaces,
  Completed,
}

data class GuidedOnboardingCallbacks(
  val onContinueFeatureHighlights: () -> Unit,
  val onRequestLocationPermission: () -> Unit,
  val onDismissLocationStep: () -> Unit,
  val onRequestNotificationsPermission: () -> Unit,
  val onDismissNotificationsStep: () -> Unit,
  val onCompleteSurfacesStep: () -> Unit,
  val onSkipAll: () -> Unit,
)

internal fun OnboardingChecklistSnapshot.guidedOnboardingStep(): GuidedOnboardingStep =
  when {
    isCompleted() -> GuidedOnboardingStep.Completed
    !featureHighlightsSeen -> GuidedOnboardingStep.FeatureHighlights
    !locationDecisionMade -> GuidedOnboardingStep.LocationPermission
    !notificationsDecisionMade -> GuidedOnboardingStep.NotificationsPermission
    !surfacesDiscovered -> GuidedOnboardingStep.Surfaces
    else -> GuidedOnboardingStep.Completed
  }

@Composable
fun GuidedOnboardingFlow(
  checklist: OnboardingChecklistSnapshot,
  callbacks: GuidedOnboardingCallbacks,
) {
  val step = checklist.guidedOnboardingStep()
  if (step == GuidedOnboardingStep.FeatureHighlights) {
    GuidedOnboardingHighlightsScreen(
      onContinue = callbacks.onContinueFeatureHighlights,
      onSkipAll = callbacks.onSkipAll,
    )
    return
  }

  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .windowInsetsPadding(WindowInsets.statusBars),
  ) {
    TextButton(
      onClick = callbacks.onSkipAll,
      modifier =
        Modifier
          .align(Alignment.TopEnd)
          .padding(horizontal = 24.dp),
    ) {
      Text(stringResource(Res.string.onboardingSkip))
    }

    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(24.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      when (step) {
        GuidedOnboardingStep.FeatureHighlights -> Unit
        GuidedOnboardingStep.LocationPermission -> {
          Text(
            stringResource(Res.string.onboardingLocationTitle),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
          )
          Spacer(Modifier.height(12.dp))
          Text(
            stringResource(Res.string.onboardingLocationBody),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
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
          Text(
            stringResource(Res.string.onboardingNotificationsTitle),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
          )
          Spacer(Modifier.height(12.dp))
          Text(
            stringResource(Res.string.onboardingNotificationsBody),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
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

        GuidedOnboardingStep.Surfaces -> {
          Text(
            stringResource(Res.string.onboardingSurfacesTitle),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
          )
          Spacer(Modifier.height(12.dp))
          Text(
            stringResource(Res.string.onboardingSurfacesBody),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
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
}

@Composable
private fun GuidedOnboardingHighlightsScreen(
  onContinue: () -> Unit,
  onSkipAll: () -> Unit,
) {
  val featureCards =
    remember {
      listOf(
        Res.string.onboardingFeatureNearbyTitle to Res.string.onboardingFeatureNearbyBody,
        Res.string.onboardingFeatureSavedTitle to Res.string.onboardingFeatureSavedBody,
        Res.string.onboardingFeatureAlertsTitle to Res.string.onboardingFeatureAlertsBody,
        Res.string.onboardingFeatureRoutesTitle to Res.string.onboardingFeatureRoutesBody,
      )
    }

  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .windowInsetsPadding(WindowInsets.statusBars),
  ) {
    TextButton(
      onClick = onSkipAll,
      modifier =
        Modifier
          .align(Alignment.TopEnd)
          .padding(horizontal = 24.dp),
    ) {
      Text(stringResource(Res.string.onboardingSkip))
    }
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.Start,
    ) {
      Spacer(Modifier.height(8.dp))
      Text(
        stringResource(Res.string.onboardingHighlightsTitle),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
      )
      Text(
        stringResource(Res.string.onboardingHighlightsBody),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      featureCards.forEach { (title, body) ->
        Card(modifier = Modifier.fillMaxWidth()) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              stringResource(title),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              stringResource(body),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
      Spacer(Modifier.height(8.dp))
      Button(
        onClick = onContinue,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(stringResource(Res.string.onboardingHighlightsContinue))
      }
    }
  }
}
