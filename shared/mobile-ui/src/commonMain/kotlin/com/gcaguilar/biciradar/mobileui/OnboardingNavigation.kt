package com.gcaguilar.biciradar.mobileui

fun shouldNavigateToFavoritesAfterOnboarding(
  hasPendingFavoritesNavigation: Boolean,
  shouldShowGuidedOnboarding: Boolean,
): Boolean =
  com.gcaguilar.biciradar.mobileui.state.shouldNavigateToFavoritesAfterOnboarding(
    hasPendingFavoritesNavigation = hasPendingFavoritesNavigation,
    shouldShowGuidedOnboarding = shouldShowGuidedOnboarding,
  )
