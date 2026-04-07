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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.WindowInsets
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
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.FavoriteCategoryIds
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationHourlyPattern
import com.gcaguilar.biciradar.core.findSavedPlaceAlertRule
import com.gcaguilar.biciradar.core.formatDistance
import com.gcaguilar.biciradar.mobileui.DataFreshnessBanner
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.PlatformBackHandler
import com.gcaguilar.biciradar.mobileui.PlatformStationMap
import com.gcaguilar.biciradar.mobileui.SavedPlaceAlertEditorSheet
import com.gcaguilar.biciradar.mobileui.components.SavedPlacePill
import com.gcaguilar.biciradar.mobileui.components.station.FavoritePill
import com.gcaguilar.biciradar.mobileui.components.station.StationDetailAlertBell
import com.gcaguilar.biciradar.mobileui.components.station.StationMetricPill
import com.gcaguilar.biciradar.mobileui.components.station.StationPatternCard
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.back
import com.gcaguilar.biciradar.mobile_ui.generated.resources.bikes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.distance
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favorite
import com.gcaguilar.biciradar.mobile_ui.generated.resources.home
import com.gcaguilar.biciradar.mobile_ui.generated.resources.openRoute
import com.gcaguilar.biciradar.mobile_ui.generated.resources.removeFromFavorites
import com.gcaguilar.biciradar.mobile_ui.generated.resources.save
import com.gcaguilar.biciradar.mobile_ui.generated.resources.saved
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsStationDetailHint
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.saveThisStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.saveThisStationDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.saveToFavorites
import com.gcaguilar.biciradar.mobile_ui.generated.resources.slots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.source
import com.gcaguilar.biciradar.mobile_ui.generated.resources.stationMarkedHome
import com.gcaguilar.biciradar.mobile_ui.generated.resources.stationMarkedHomeAndWork
import com.gcaguilar.biciradar.mobile_ui.generated.resources.stationMarkedWork
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tapHomeOrWorkToAssign
import com.gcaguilar.biciradar.mobile_ui.generated.resources.work
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StationDetailScreen(
  mobilePlatform: MobileUiPlatform,
  station: Station,
  isFavorite: Boolean,
  isHomeStation: Boolean,
  isWorkStation: Boolean,
  userLocation: GeoPoint?,
  isMapReady: Boolean,
  supportsUsagePatterns: Boolean,
  dataFreshness: DataFreshness,
  lastUpdatedEpoch: Long?,
  stationsLoading: Boolean,
  onRefreshStations: () -> Unit,
  onBack: () -> Unit,
  onToggleFavorite: () -> Unit,
  onToggleHome: () -> Unit,
  onToggleWork: () -> Unit,
  onRoute: () -> Unit,
  savedPlaceAlertsCityId: String,
  savedPlaceAlertRules: List<SavedPlaceAlertRule>,
  onUpsertSavedPlaceAlert: (SavedPlaceAlertTarget, SavedPlaceAlertCondition) -> Unit,
  onRemoveSavedPlaceAlertForTarget: (SavedPlaceAlertTarget) -> Unit,
  patterns: List<StationHourlyPattern>,
  patternsLoading: Boolean,
  patternsError: Boolean,
) {
  PlatformBackHandler(enabled = true, onBack = onBack)
  var alertEditor by remember { mutableStateOf<Pair<SavedPlaceAlertTarget, SavedPlaceAlertRule?>?>(null) }
  var showWeekend by rememberSaveable { mutableStateOf(false) }
  Box(Modifier.fillMaxSize()) {
  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(LocalBiziColors.current.surface)
          .windowInsetsPadding(WindowInsets.statusBars)
          .height(48.dp)
          .padding(end = 16.dp),
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
      }
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(pageBackgroundColor(mobilePlatform)),
      contentAlignment = Alignment.TopCenter,
    ) {
      LazyColumn(
        modifier = Modifier.responsivePageWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = innerPadding.calculateTopPadding() + 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
          ) {
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  station.name,
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.weight(1f),
                )
                FavoritePill(
                  active = isFavorite,
                  onClick = onToggleFavorite,
                  label = if (isFavorite) stringResource(Res.string.saved) else stringResource(Res.string.save),
                )
              }
              Text(station.address, style = MaterialTheme.typography.bodyMedium, color = LocalBiziColors.current.muted)
              DataFreshnessBanner(
                freshness = dataFreshness,
                lastUpdatedEpoch = lastUpdatedEpoch,
                loading = stationsLoading,
                onRefresh = onRefreshStations,
                modifier = Modifier.padding(top = 8.dp),
              )
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StationMetricPill(
                  modifier = Modifier.weight(1f),
                  label = stringResource(Res.string.distance),
                  value = formatDistance(station.distanceMeters),
                  tint = LocalBiziColors.current.blue,
                )
                StationMetricPill(
                  modifier = Modifier.weight(1f),
                  label = stringResource(Res.string.source),
                  value = station.sourceLabel,
                  tint = LocalBiziColors.current.muted,
                )
              }
            }
          }
        }
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
          ) {
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Text(stringResource(Res.string.saveThisStation), fontWeight = FontWeight.SemiBold)
              Text(
                stringResource(Res.string.saveThisStationDescription),
                style = MaterialTheme.typography.bodySmall,
                color = LocalBiziColors.current.muted,
              )
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            val homeTarget = SavedPlaceAlertTarget.CategoryStation(
              stationId = station.id,
              cityId = savedPlaceAlertsCityId,
              stationName = station.name,
              categoryId = FavoriteCategoryIds.HOME,
              categoryLabel = stringResource(Res.string.home),
            )
            val workTarget = SavedPlaceAlertTarget.CategoryStation(
              stationId = station.id,
              cityId = savedPlaceAlertsCityId,
              stationName = station.name,
              categoryId = FavoriteCategoryIds.WORK,
              categoryLabel = stringResource(Res.string.work),
            )
            val favoriteTarget = SavedPlaceAlertTarget.CategoryStation(
              stationId = station.id,
              cityId = savedPlaceAlertsCityId,
              stationName = station.name,
              categoryId = FavoriteCategoryIds.FAVORITE,
              categoryLabel = stringResource(Res.string.favorite),
            )
            Card(
              colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
            ) {
              Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Text(stringResource(Res.string.savedPlaceAlertsTitle), fontWeight = FontWeight.SemiBold)
                Text(
                  stringResource(Res.string.savedPlaceAlertsStationDetailHint),
                  style = MaterialTheme.typography.bodySmall,
                  color = LocalBiziColors.current.muted,
                )
                Row(
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
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
        }
        item {
          Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
          ) {
            PlatformStationMap(
              modifier = Modifier.fillMaxWidth().height(200.dp),
              stations = listOf(station),
              userLocation = userLocation,
              highlightedStationId = station.id,
              isMapReady = isMapReady,
              onStationSelected = {},
            )
          }
        }
        item {
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AvailabilityCard(
              modifier = Modifier.weight(1f),
              label = stringResource(Res.string.bikes),
              value = station.bikesAvailable.toString(),
              icon = Icons.AutoMirrored.Filled.DirectionsBike,
              tint = LocalBiziColors.current.red,
              mobilePlatform = mobilePlatform,
            )
            AvailabilityCard(
              modifier = Modifier.weight(1f),
              label = stringResource(Res.string.slots),
              value = station.slotsFree.toString(),
              icon = Icons.Filled.LocalParking,
              tint = LocalBiziColors.current.blue,
              mobilePlatform = mobilePlatform,
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
        item {
          Button(onClick = onRoute, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Directions, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(Res.string.openRoute))
          }
        }
        item {
          OutlinedButton(onClick = onToggleFavorite, modifier = Modifier.fillMaxWidth()) {
            Icon(
              if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
              contentDescription = null,
            )
            Spacer(Modifier.width(8.dp))
            Text(if (isFavorite) stringResource(Res.string.removeFromFavorites) else stringResource(Res.string.saveToFavorites))
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
    colors = CardDefaults.cardColors(
      containerColor = if (mobilePlatform == MobileUiPlatform.IOS) {
        LocalBiziColors.current.surface
      } else {
        tint.copy(alpha = 0.08f)
      },
    ),
  ) {
    Column(
      modifier = Modifier
        .padding(16.dp)
        .animateContentSize(animationSpec = spring(dampingRatio = 0.86f, stiffness = 500f)),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
      Text(label, color = tint)
      Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    }
  }
}
