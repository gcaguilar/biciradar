package com.gcaguilar.biciradar.mobileui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.mobileui.navigation.AssistantLaunchRequest
import com.gcaguilar.biciradar.mobileui.navigation.MobileLaunchRequest

/**
 * Estado global de la aplicación móvil.
 * Gestiona el estado compartido entre diferentes pantallas y componentes.
 */
@Stable
class AppState {
  var searchQuery = mutableStateOf("")
    private set

  var pendingAssistantAction = mutableStateOf<AssistantAction?>(null)
    private set

  var pendingLaunchRequest = mutableStateOf<MobileLaunchRequest?>(null)
    private set

  var pendingAssistantLaunchRequest = mutableStateOf<AssistantLaunchRequest?>(null)
    private set

  fun updateSearchQuery(query: String) {
    searchQuery.value = query
  }

  fun setPendingAssistantAction(action: AssistantAction?) {
    pendingAssistantAction.value = action
  }

  fun setPendingLaunchRequest(request: MobileLaunchRequest?) {
    pendingLaunchRequest.value = request
  }

  fun setPendingAssistantLaunchRequest(request: AssistantLaunchRequest?) {
    pendingAssistantLaunchRequest.value = request
  }

  fun clearPendingAssistantAction() {
    pendingAssistantAction.value = null
  }
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
