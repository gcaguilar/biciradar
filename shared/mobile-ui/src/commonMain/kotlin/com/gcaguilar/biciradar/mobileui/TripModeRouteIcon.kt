package com.gcaguilar.biciradar.mobileui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.ui.graphics.vector.ImageVector
import com.gcaguilar.biciradar.core.TripMode

/** Icon for route actions that follow the current trip mode. */
internal fun TripMode.routeIcon(): ImageVector =
  when (this) {
    TripMode.Pedestrian -> Icons.AutoMirrored.Filled.DirectionsWalk
    TripMode.Cyclist -> Icons.AutoMirrored.Filled.DirectionsBike
  }
