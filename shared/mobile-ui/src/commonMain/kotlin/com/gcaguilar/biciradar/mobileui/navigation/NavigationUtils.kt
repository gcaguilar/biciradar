package com.gcaguilar.biciradar.mobileui.navigation

import androidx.navigation.NavHostController

fun NavHostController.navigateToPrimaryDestination(screen: Screen) {
  navigate(screen) {
    popUpTo(graph.startDestinationId) { saveState = true }
    launchSingleTop = true
    restoreState = true
  }
}
