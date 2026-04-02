package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.AssistantIntentResolver

internal class ShortcutsViewModelFactory(
  private val assistantIntentResolver: AssistantIntentResolver,
) {
  fun create(): ShortcutsViewModel = ShortcutsViewModel(
    assistantIntentResolver = assistantIntentResolver,
  )
}
