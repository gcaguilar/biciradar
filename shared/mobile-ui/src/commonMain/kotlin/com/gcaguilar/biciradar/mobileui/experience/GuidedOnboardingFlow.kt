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
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun GuidedOnboardingFlow(
  checklist: OnboardingChecklistSnapshot,
  platformBindings: PlatformBindings,
  scope: CoroutineScope,
  settingsRepository: SettingsRepository,
  onOpenFavorites: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .windowInsetsPadding(WindowInsets.statusBars)
      .padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    when {
      !checklist.locationDecisionMade -> {
        Text(stringResource(Res.string.onboardingLocationTitle), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(Res.string.onboardingLocationBody), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(
          onClick = {
            scope.launch {
              platformBindings.permissionPrompter.requestLocationPermission()
              settingsRepository.updateOnboardingChecklist { it.copy(locationDecisionMade = true) }
            }
          },
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingAllow)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
          onClick = {
            scope.launch {
              settingsRepository.updateOnboardingChecklist { it.copy(locationDecisionMade = true) }
            }
          },
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingLater)) }
      }
      !checklist.notificationsDecisionMade -> {
        Text(stringResource(Res.string.onboardingNotificationsTitle), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(Res.string.onboardingNotificationsBody), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(
          onClick = {
            scope.launch {
              platformBindings.localNotifier.requestPermission()
              settingsRepository.updateOnboardingChecklist { it.copy(notificationsDecisionMade = true) }
            }
          },
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingAllow)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
          onClick = {
            scope.launch {
              settingsRepository.updateOnboardingChecklist { it.copy(notificationsDecisionMade = true) }
            }
          },
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingLater)) }
      }
      !checklist.firstStationSaved -> {
        Text(stringResource(Res.string.onboardingFavoriteTitle), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(Res.string.onboardingFavoriteBody), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(
          onClick = onOpenFavorites,
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingGoToFavorites)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
          onClick = {
            scope.launch {
              settingsRepository.updateOnboardingChecklist { it.copy(firstStationSaved = true) }
            }
          },
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingLater)) }
      }
      !checklist.savedPlacesConfigured -> {
        Text(stringResource(Res.string.onboardingSavedPlacesTitle), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(Res.string.onboardingSavedPlacesBody), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(
          onClick = onOpenFavorites,
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingGoToFavorites)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
          onClick = {
            scope.launch {
              settingsRepository.updateOnboardingChecklist { it.copy(savedPlacesConfigured = true) }
            }
          },
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingLater)) }
      }
      !checklist.surfacesDiscovered -> {
        Text(stringResource(Res.string.onboardingSurfacesTitle), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(Res.string.onboardingSurfacesBody), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(
          onClick = {
            scope.launch {
              settingsRepository.setOnboardingChecklist(
                checklist.copy(surfacesDiscovered = true).markCompleted(),
              )
            }
          },
          modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.onboardingFinish)) }
      }
    }
  }
}
