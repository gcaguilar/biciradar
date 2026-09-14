package com.gcaguilar.biciradar.mobileui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.FavoriteCategoryIds
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.findSavedPlaceAlertRule
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.back
import com.gcaguilar.biciradar.mobile_ui.generated.resources.bikes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favorite
import com.gcaguilar.biciradar.mobile_ui.generated.resources.home
import com.gcaguilar.biciradar.mobile_ui.generated.resources.openRoute
import com.gcaguilar.biciradar.mobile_ui.generated.resources.save
import com.gcaguilar.biciradar.mobile_ui.generated.resources.saveThisStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.saveThisStationDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.saved
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsStationDetailHint
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.slots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.stationMarkedHome
import com.gcaguilar.biciradar.mobile_ui.generated.resources.stationMarkedHomeAndWork
import com.gcaguilar.biciradar.mobile_ui.generated.resources.stationMarkedWork
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tapHomeOrWorkToAssign
import com.gcaguilar.biciradar.mobile_ui.generated.resources.work
import com.gcaguilar.biciradar.mobileui.BiziAlpha
import com.gcaguilar.biciradar.mobileui.BiziSpacing
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.PlatformBackHandler
import com.gcaguilar.biciradar.mobileui.PlatformStationMap
import com.gcaguilar.biciradar.mobileui.SavedPlaceAlertEditorSheet
import com.gcaguilar.biciradar.mobileui.biziCardBorder
import com.gcaguilar.biciradar.mobileui.biziCardColors
import com.gcaguilar.biciradar.mobileui.biziCardElevation
import com.gcaguilar.biciradar.mobileui.components.SavedPlacePill
import com.gcaguilar.biciradar.mobileui.components.cards.BiziSectionCard
import com.gcaguilar.biciradar.mobileui.components.station.FavoritePill
import com.gcaguilar.biciradar.mobileui.components.station.StationDetailAlertBell
import com.gcaguilar.biciradar.mobileui.components.station.StationPatternCard
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import com.gcaguilar.biciradar.mobileui.viewmodel.StationDetailUiState
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StationDetailScreen(
  state: StationDetailUiState,
  mobilePlatform: MobileUiPlatform,
  isMapReady: Boolean,
  onBack: () -> Unit,
  onToggleFavorite: () -> Unit,
  onToggleHome: () -> Unit,
  onToggleWork: () -> Unit,
  onRoute: () -> Unit,
  onUpsertSavedPlaceAlert: (SavedPlaceAlertTarget, SavedPlaceAlertCondition) -> Unit,
  onRemoveSavedPlaceAlertForTarget: (SavedPlaceAlertTarget) -> Unit,
) {
  val station = state.station ?: return
  val isFavorite = state.isFavorite
  val isHomeStation = state.isHomeStation
  val isWorkStation = state.isWorkStation
  val userLocation = state.userLocation
  val supportsUsagePatterns = state.supportsUsagePatterns
  val savedPlaceAlertsCityId = state.savedPlaceAlertsCityId
  val savedPlaceAlertRules = state.savedPlaceAlertRules
  val patterns = state.patterns
  val patternsLoading = state.patternsLoading
  val patternsError = state.patternsError
  PlatformBackHandler(enabled = true, onBack = onBack)
  var alertEditor by remember { mutableStateOf<Pair<SavedPlaceAlertTarget, SavedPlaceAlertRule?>?>(null) }
  var showWeekend by rememberSaveable { mutableStateOf(false) }
  val mapStations = remember(station) { listOf(station) }
  Box(Modifier.fillMaxSize()) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .background(LocalBiziColors.current.surface)
              .windowInsetsPadding(WindowInsets.statusBars)
              .height(48.dp)
              .padding(end = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
          }
          Text(
            text = station.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
          )
          IconButton(onClick = onRoute) {
            Icon(
              Icons.Filled.Directions,
              contentDescription = stringResource(Res.string.openRoute),
            )
          }
        }
      },
    ) { innerPadding ->
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .background(pageBackgroundColor(mobilePlatform)),
        contentAlignment = Alignment.TopCenter,
      ) {
        LazyColumn(
          modifier = Modifier.responsivePageWidth(),
          contentPadding =
            PaddingValues(
              start = BiziSpacing.screenPadding,
              end = BiziSpacing.screenPadding,
              top = innerPadding.calculateTopPadding() + BiziSpacing.screenPadding,
              bottom = BiziSpacing.screenPadding,
            ),
          verticalArrangement = Arrangement.spacedBy(BiziSpacing.screenPadding),
        ) {
          item {
            Row(horizontalArrangement = Arrangement.spacedBy(BiziSpacing.xLarge)) {
              AvailabilityCard(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.slots),
                value = station.slotsFree.toString(),
                icon = Icons.Filled.LocalParking,
                tint = LocalBiziColors.current.blue,
                mobilePlatform = mobilePlatform,
              )
              AvailabilityCard(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.bikes),
                value = station.bikesAvailable.toString(),
                icon = Icons.AutoMirrored.Filled.DirectionsBike,
                tint = LocalBiziColors.current.red,
                mobilePlatform = mobilePlatform,
              )
            }
          }
          if (!isFavorite) {
            item {
              BiziSectionCard(
                title = stringResource(Res.string.saveThisStation),
                description = stringResource(Res.string.saveThisStationDescription),
                contentSpacing = BiziSpacing.xLarge,
              ) {
                Row(horizontalArrangement = Arrangement.spacedBy(BiziSpacing.medium)) {
                  FavoritePill(
                    active = isFavorite,
                    onClick = onToggleFavorite,
                    label = if (isFavorite) stringResource(Res.string.favorite) else stringResource(Res.string.save),
                  )
                  SavedPlacePill(
                    active = isHomeStation,
                    label = stringResource(Res.string.home),
                    onClick = onToggleHome,
                  )
                  SavedPlacePill(
                    active = isWorkStation,
                    label = stringResource(Res.string.work),
                    onClick = onToggleWork,
                  )
                }
                Text(
                  when {
                    isHomeStation && isWorkStation -> stringResource(Res.string.stationMarkedHomeAndWork)
                    isHomeStation -> stringResource(Res.string.stationMarkedHome)
                    isWorkStation -> stringResource(Res.string.stationMarkedWork)
                    else -> stringResource(Res.string.tapHomeOrWorkToAssign)
                  },
                  style = MaterialTheme.typography.bodySmall,
                  color = LocalBiziColors.current.muted,
                )
              }
            }
          }
          if (isFavorite || isHomeStation || isWorkStation) {
            item {
              val homeTarget =
                SavedPlaceAlertTarget.CategoryStation(
                  stationId = station.id,
                  cityId = savedPlaceAlertsCityId,
                  stationName = station.name,
                  categoryId = FavoriteCategoryIds.HOME,
                  categoryLabel = stringResource(Res.string.home),
                )
              val workTarget =
                SavedPlaceAlertTarget.CategoryStation(
                  stationId = station.id,
                  cityId = savedPlaceAlertsCityId,
                  stationName = station.name,
                  categoryId = FavoriteCategoryIds.WORK,
                  categoryLabel = stringResource(Res.string.work),
                )
              val favoriteTarget =
                SavedPlaceAlertTarget.CategoryStation(
                  stationId = station.id,
                  cityId = savedPlaceAlertsCityId,
                  stationName = station.name,
                  categoryId = FavoriteCategoryIds.FAVORITE,
                  categoryLabel = stringResource(Res.string.favorite),
                )
              BiziSectionCard(
                title = stringResource(Res.string.savedPlaceAlertsTitle),
                description = stringResource(Res.string.savedPlaceAlertsStationDetailHint),
                contentSpacing = BiziSpacing.xLarge,
              ) {
                Row(
                  horizontalArrangement = Arrangement.spacedBy(BiziSpacing.xLarge),
                  verticalAlignment = Alignment.Top,
                ) {
                  if (isHomeStation) {
                    StationDetailAlertBell(
                      label = stringResource(Res.string.home),
                      active = findSavedPlaceAlertRule(savedPlaceAlertRules, homeTarget) != null,
                      onClick = {
                        alertEditor = homeTarget to findSavedPlaceAlertRule(savedPlaceAlertRules, homeTarget)
                      },
                    )
                  }
                  if (isWorkStation) {
                    StationDetailAlertBell(
                      label = stringResource(Res.string.work),
                      active = findSavedPlaceAlertRule(savedPlaceAlertRules, workTarget) != null,
                      onClick = {
                        alertEditor = workTarget to findSavedPlaceAlertRule(savedPlaceAlertRules, workTarget)
                      },
                    )
                  }
                  if (isFavorite) {
                    StationDetailAlertBell(
                      label = stringResource(Res.string.favorite),
                      active = findSavedPlaceAlertRule(savedPlaceAlertRules, favoriteTarget) != null,
                      onClick = {
                        alertEditor = favoriteTarget to findSavedPlaceAlertRule(savedPlaceAlertRules, favoriteTarget)
                      },
                    )
                  }
                }
              }
            }
          }
          item {
            Card(
              shape = MaterialTheme.shapes.extraLarge,
              colors = biziCardColors(),
              border = biziCardBorder(),
              elevation = biziCardElevation(),
            ) {
              PlatformStationMap(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                stations = mapStations,
                userLocation = userLocation,
                highlightedStationId = station.id,
                isMapReady = isMapReady,
                onStationSelected = {},
              )
            }
          }
          if (supportsUsagePatterns) {
            item {
              StationPatternCard(
                patterns = patterns,
                isLoading = patternsLoading,
                isError = patternsError,
                showWeekend = showWeekend,
                onToggleDayType = { showWeekend = !showWeekend },
              )
            }
          }
        }
      }
    }
    if (isFavorite || isHomeStation || isWorkStation) {
      alertEditor?.let { (target, rule) ->
        SavedPlaceAlertEditorSheet(
          target = target,
          existingRule = rule,
          onDismiss = { alertEditor = null },
          onSave = { cond ->
            onUpsertSavedPlaceAlert(target, cond)
            alertEditor = null
          },
          onRemove = {
            onRemoveSavedPlaceAlertForTarget(target)
            alertEditor = null
          },
        )
      }
    }
  }
}

@Composable
private fun AvailabilityCard(
  modifier: Modifier,
  label: String,
  value: String,
  icon: ImageVector,
  tint: Color,
  mobilePlatform: MobileUiPlatform,
) {
  Card(
    modifier = modifier,
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, tint.copy(alpha = 0.14f)) else null,
    colors =
      CardDefaults.cardColors(
        containerColor =
          if (mobilePlatform == MobileUiPlatform.IOS) {
            LocalBiziColors.current.surface
          } else {
            tint.copy(alpha = BiziAlpha.subtleTint)
          },
      ),
  ) {
    Column(
      modifier =
        Modifier
          .padding(BiziSpacing.screenPadding)
          .animateContentSize(animationSpec = spring(dampingRatio = 0.86f, stiffness = 500f)),
      verticalArrangement = Arrangement.spacedBy(BiziSpacing.xSmall),
    ) {
      Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
      Text(label, color = tint)
      Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    }
  }
}
