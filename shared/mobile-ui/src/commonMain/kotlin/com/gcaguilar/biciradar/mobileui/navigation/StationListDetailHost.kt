package com.gcaguilar.biciradar.mobileui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.mobileui.BiziMobileAppContent
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.PlatformBackHandler
import com.gcaguilar.biciradar.mobileui.layout.LocalBiziWindowInfo
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.viewmodel.StationDetailViewModel
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.coroutines.launch

/**
 * Hosts a station list screen and, on wide windows (Medium and Expanded), a station detail
 * next to it using the Material 3 list-detail layout.
 *
 * - Compact windows keep today's behaviour: tapping a station pushes `Screen.StationDetail` onto
 *   the navigation back stack through [onOpenStationFullScreen].
 * - Wide windows show the detail in a secondary pane. The pane navigator owns that selection,
 *   so the list stays visible and system back returns to the list instead of leaving the app.
 *
 * The scaffold directive is built with `calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth`
 * so foldables and large phones get two panes from the Medium breakpoint on, and the library's
 * hinge handling keeps the panes clear of a separating fold.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun StationListDetailHost(
  mobilePlatform: MobileUiPlatform,
  isMapReady: Boolean,
  onOpenStationFullScreen: (Station) -> Unit,
  listContent: @Composable ((Station) -> Unit) -> Unit,
) {
  val windowInfo = LocalBiziWindowInfo.current
  if (!windowInfo.isWide) {
    listContent(onOpenStationFullScreen)
    return
  }

  val directive =
    calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth(
      currentWindowAdaptiveInfoV2(),
    )
  val navigator =
    rememberListDetailPaneScaffoldNavigator<String>(
      scaffoldDirective = directive,
    )
  val scope = rememberCoroutineScope()
  val detailStationId = navigator.currentDestination?.contentKey
  // If the window shrinks while a station is open, the detail can end up as the only
  // visible pane; keep a back affordance in that case so the list is still reachable
  // without a system back gesture (iOS has none for Compose panes).
  val detailIsOnlyVisiblePane =
    navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Hidden

  ListDetailPaneScaffold(
    directive = navigator.scaffoldDirective,
    scaffoldState = navigator.scaffoldState,
    listPane = {
      AnimatedPane {
        listContent { station ->
          scope.launch {
            navigator.navigateTo(
              pane = ListDetailPaneScaffoldRole.Detail,
              contentKey = station.id,
            )
          }
        }
      }
    },
    detailPane = {
      AnimatedPane {
        StationDetailPane(
          stationId = detailStationId,
          mobilePlatform = mobilePlatform,
          isMapReady = isMapReady,
          onBack = { scope.launch { navigator.navigateBack() } },
          showBackButton = detailIsOnlyVisiblePane,
        )
      }
    },
  )

  // The detail screen registers its own back handler, but it returns early while the station
  // is still loading (no top bar, no handler). Cover that window too: with a detail selected,
  // back must close the pane instead of popping the list destination.
  PlatformBackHandler(enabled = detailStationId != null) {
    scope.launch { navigator.navigateBack() }
  }
}

@Composable
private fun StationDetailPane(
  stationId: String?,
  mobilePlatform: MobileUiPlatform,
  isMapReady: Boolean,
  onBack: () -> Unit,
  showBackButton: Boolean,
) {
  if (stationId == null) {
    // No station selected yet: keep the pane empty instead of inventing a placeholder
    // station. The list pane stays interactive and drives the selection.
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(pageBackgroundColor(mobilePlatform)),
    )
    return
  }

  val viewModel =
    assistedMetroViewModel<StationDetailViewModel, StationDetailViewModel.Factory>(
      key = "station-detail-$stationId",
    ) {
      create(stationId)
    }
  BiziMobileAppContent.StationDetailScreenContent(
    viewModel = viewModel,
    mobilePlatform = mobilePlatform,
    isMapReady = isMapReady,
    onBack = onBack,
    showBackButton = showBackButton,
  )
}
