package com.gcaguilar.biciradar.mobileui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.mobileui.navigation.AssistantLaunchRequest
import com.gcaguilar.biciradar.mobileui.navigation.MobileLaunchRequest

/**
 * Estado efímero de la aplicación móvil a nivel de Compose.
 * Gestiona el estado compartido entre diferentes pantallas y composables.
 */
@Stable
class AppState {
  var pendingMapSearchQuery by mutableStateOf<String?>(null)
  var pendingAssistantAction by mutableStateOf<AssistantAction?>(null)
  var pendingLaunchRequest by mutableStateOf<MobileLaunchRequest?>(null)
  var pendingAssistantLaunchRequest by mutableStateOf<AssistantLaunchRequest?>(null)
}

@Composable
fun rememberAppState(): AppState = remember { AppState() }

/**
 * Determina si se debe navegar a favoritos después del onboarding.
 */
fun shouldNavigateToFavoritesAfterOnboarding(
  hasPendingFavoritesNavigation: Boolean,
  shouldShowGuidedOnboarding: Boolean,
): Boolean = hasPendingFavoritesNavigation && !shouldShowGuidedOnboarding
