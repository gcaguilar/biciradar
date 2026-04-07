package com.gcaguilar.biciradar.mobileui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.FavoriteCategory
import com.gcaguilar.biciradar.core.FavoriteCategoryIds
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.findSavedPlaceAlertRule
import com.gcaguilar.biciradar.core.formatDistance
import com.gcaguilar.biciradar.mobileui.DataFreshnessBanner
import com.gcaguilar.biciradar.mobileui.components.EmptyStatePlaceholder
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.SavedPlaceAlertEditorSheet
import com.gcaguilar.biciradar.mobileui.components.station.RoutePill
import com.gcaguilar.biciradar.mobileui.components.station.OutlineActionPill
import com.gcaguilar.biciradar.mobileui.components.station.StationMetricPill
import com.gcaguilar.biciradar.mobileui.components.station.StationRow
import com.gcaguilar.biciradar.mobileui.StationSearchField
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.assignSearchResult
import com.gcaguilar.biciradar.mobile_ui.generated.resources.bikes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.currentSearchAssignmentHint
import com.gcaguilar.biciradar.mobile_ui.generated.resources.deleteFavorite
import com.gcaguilar.biciradar.mobile_ui.generated.resources.details
import com.gcaguilar.biciradar.mobile_ui.generated.resources.distance
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favorites
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesEmptyDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesEmptyTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesSearchStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesSubtitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favorite
import com.gcaguilar.biciradar.mobile_ui.generated.resources.home
import com.gcaguilar.biciradar.mobile_ui.generated.resources.homeAndWork
import com.gcaguilar.biciradar.mobile_ui.generated.resources.homeAndWorkDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.myStations
import com.gcaguilar.biciradar.mobile_ui.generated.resources.remove
import com.gcaguilar.biciradar.mobile_ui.generated.resources.removeFavorite
import com.gcaguilar.biciradar.mobile_ui.generated.resources.route
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsBell
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsProfileAction
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsProfileSubtitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsStationDetailHint
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceNotSet
import com.gcaguilar.biciradar.mobile_ui.generated.resources.slots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.useSearchToAssignStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.work
import org.jetbrains.compose.resources.stringResource
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import com.gcaguilar.biciradar.mobile_ui.generated.resources.shortcuts

@Composable
internal fun FavoritesScreen(
  mobilePlatform: MobileUiPlatform,
  onOpenAssistant: () -> Unit,
  allStations: List<Station>,
  stations: List<Station>,
  homeStation: Station?,
  workStation: Station?,
  searchQuery: String,
  assignmentCandidate: Station?,
  onSearchQueryChange: (String) -> Unit,
  onStationSelected: (Station) -> Unit,
  onAssignHomeStation: (Station) -> Unit,
  onAssignWorkStation: (Station) -> Unit,
  onClearHomeStation: () -> Unit,
  onClearWorkStation: () -> Unit,
  onRemoveFavorite: (Station) -> Unit,
  onQuickRoute: (Station) -> Unit,
  dataFreshness: DataFreshness,
  lastUpdatedEpoch: Long?,
  stationsLoading: Boolean,
  onRefreshStations: () -> Unit,
  onOpenSavedPlaceAlerts: () -> Unit,
  paddingValues: PaddingValues,
  savedPlaceAlertsCityId: String,
  savedPlaceAlertRules: List<SavedPlaceAlertRule>,
  categories: List<FavoriteCategory>,
  stationCategory: Map<String, String>,
  onCreateCustomCategory: (String) -> Unit,
  onAssignCandidateToCategory: (String) -> Unit,
  onRemoveCustomCategory: (String) -> Unit,
  onClearCategoryAssignment: (String) -> Unit,
  onUpsertSavedPlaceAlert: ((SavedPlaceAlertTarget, SavedPlaceAlertCondition) -> Unit)?,
  onRemoveSavedPlaceAlertForTarget: ((SavedPlaceAlertTarget) -> Unit)?,
) {
  var alertEditor by remember { mutableStateOf<Pair<SavedPlaceAlertTarget, SavedPlaceAlertRule?>?>(null) }
  var newCategoryName by remember { mutableStateOf("") }
  val upsertAlert = onUpsertSavedPlaceAlert
  val removeAlertForTarget = onRemoveSavedPlaceAlertForTarget
  if (upsertAlert != null && removeAlertForTarget != null) {
    alertEditor?.let { (target, rule) ->
      SavedPlaceAlertEditorSheet(
        target = target,
        existingRule = rule,
        onDismiss = { alertEditor = null },
        onSave = { cond ->
          upsertAlert(target, cond)
          alertEditor = null
        },
        onRemove = {
          removeAlertForTarget(target)
          alertEditor = null
        },
      )
    }
  }
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(pageBackgroundColor(mobilePlatform)),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = if (mobilePlatform == MobileUiPlatform.Desktop) Modifier.fillMaxWidth() else Modifier.responsivePageWidth(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = if (mobilePlatform == MobileUiPlatform.IOS) stringResource(Res.string.favorites) else stringResource(Res.string.myStations),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = stringResource(Res.string.favoritesSubtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalBiziColors.current.muted,
          )
        }
      }
      item {
        StationSearchField(
          mobilePlatform = mobilePlatform,
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          label = stringResource(Res.string.favoritesSearchStation),
        )
      }
      item {
        DataFreshnessBanner(
          freshness = dataFreshness,
          lastUpdatedEpoch = lastUpdatedEpoch,
          loading = stationsLoading,
          onRefresh = onRefreshStations,
        )
      }
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(Res.string.savedPlaceAlertsTitle), fontWeight = FontWeight.SemiBold)
            Text(
              stringResource(Res.string.savedPlaceAlertsProfileSubtitle),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            Text(
              stringResource(Res.string.savedPlaceAlertsStationDetailHint),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            OutlinedButton(
              modifier = Modifier.fillMaxWidth(),
              onClick = onOpenSavedPlaceAlerts,
            ) {
              Icon(Icons.Filled.Notifications, contentDescription = null)
              Spacer(Modifier.width(8.dp))
              Text(stringResource(Res.string.savedPlaceAlertsProfileAction))
            }
          }
        }
      }
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = stringResource(Res.string.homeAndWork),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = stringResource(Res.string.homeAndWorkDescription),
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
        }
      }
      val customCategories = categories.filterNot { it.id in setOf(FavoriteCategoryIds.FAVORITE, FavoriteCategoryIds.HOME, FavoriteCategoryIds.WORK) }
      if (customCategories.isNotEmpty() || assignmentCandidate != null) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
          ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text("Categorias custom", fontWeight = FontWeight.SemiBold)
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.OutlinedTextField(
                  value = newCategoryName,
                  onValueChange = { newCategoryName = it },
                  modifier = Modifier.weight(1f),
                  singleLine = true,
                  label = { Text("Nueva categoria") },
                )
                TextButton(onClick = {
                  val label = newCategoryName.trim()
                  if (label.isNotBlank()) {
                    onCreateCustomCategory(label)
                    newCategoryName = ""
                  }
                }) { Text("Crear") }
              }
              customCategories.forEach { category ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(category.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (assignmentCandidate != null) {
                      TextButton(onClick = { onAssignCandidateToCategory(category.id) }) { Text("Asignar busqueda") }
                    }
                    TextButton(onClick = { onRemoveCustomCategory(category.id) }) { Text("Eliminar") }
                  }
                }
              }
            }
          }
        }
      }
      item {
        SavedPlaceCard(
          mobilePlatform = mobilePlatform,
          title = stringResource(Res.string.home),
          station = homeStation,
          assignmentCandidate = assignmentCandidate,
          onAssignCandidate = onAssignHomeStation,
          onClear = onClearHomeStation,
          onOpenStationDetails = onStationSelected,
          onQuickRoute = onQuickRoute,
          onSavedPlaceAlertClick = run {
            val s = homeStation
            if (s != null && upsertAlert != null) {
              {
                val t = SavedPlaceAlertTarget.CategoryStation(
                  stationId = s.id,
                  cityId = savedPlaceAlertsCityId,
                  stationName = s.name,
                  categoryId = FavoriteCategoryIds.HOME,
                  categoryLabel = "Casa",
                )
                alertEditor = t to findSavedPlaceAlertRule(savedPlaceAlertRules, t)
              }
            } else {
              null
            }
          },
          savedPlaceAlertActive = homeStation?.let { s ->
            findSavedPlaceAlertRule(
              savedPlaceAlertRules,
              SavedPlaceAlertTarget.CategoryStation(
                stationId = s.id,
                cityId = savedPlaceAlertsCityId,
                stationName = s.name,
                categoryId = FavoriteCategoryIds.HOME,
                categoryLabel = "Casa",
              ),
            ) != null
          } == true,
        )
      }
      item {
        SavedPlaceCard(
          mobilePlatform = mobilePlatform,
          title = stringResource(Res.string.work),
          station = workStation,
          assignmentCandidate = assignmentCandidate,
          onAssignCandidate = onAssignWorkStation,
          onClear = onClearWorkStation,
          onOpenStationDetails = onStationSelected,
          onQuickRoute = onQuickRoute,
          onSavedPlaceAlertClick = run {
            val s = workStation
            if (s != null && upsertAlert != null) {
              {
                val t = SavedPlaceAlertTarget.CategoryStation(
                  stationId = s.id,
                  cityId = savedPlaceAlertsCityId,
                  stationName = s.name,
                  categoryId = FavoriteCategoryIds.WORK,
                  categoryLabel = "Trabajo",
                )
                alertEditor = t to findSavedPlaceAlertRule(savedPlaceAlertRules, t)
              }
            } else {
              null
            }
          },
          savedPlaceAlertActive = workStation?.let { s ->
            findSavedPlaceAlertRule(
              savedPlaceAlertRules,
              SavedPlaceAlertTarget.CategoryStation(
                stationId = s.id,
                cityId = savedPlaceAlertsCityId,
                stationName = s.name,
                categoryId = FavoriteCategoryIds.WORK,
                categoryLabel = "Trabajo",
              ),
            ) != null
          } == true,
        )
      }
      customCategories.forEach { category ->
        item(key = "custom-category-${category.id}") {
          val assignedStationId = stationCategory.entries.firstOrNull { it.value == category.id }?.key
          val assignedStation = allStations.find { it.id == assignedStationId }
          SavedPlaceCard(
            mobilePlatform = mobilePlatform,
            title = category.label,
            station = assignedStation,
            assignmentCandidate = assignmentCandidate,
            onAssignCandidate = { onAssignCandidateToCategory(category.id) },
            onClear = { onClearCategoryAssignment(category.id) },
            onOpenStationDetails = onStationSelected,
            onQuickRoute = onQuickRoute,
            onSavedPlaceAlertClick = run {
              val s = assignedStation
              if (s != null && upsertAlert != null) {
                {
                  val t = SavedPlaceAlertTarget.CategoryStation(
                    stationId = s.id,
                    cityId = savedPlaceAlertsCityId,
                    stationName = s.name,
                    categoryId = category.id,
                    categoryLabel = category.label,
                  )
                  alertEditor = t to findSavedPlaceAlertRule(savedPlaceAlertRules, t)
                }
              } else {
                null
              }
            },
            savedPlaceAlertActive = assignedStation?.let { s ->
              findSavedPlaceAlertRule(
                savedPlaceAlertRules,
                SavedPlaceAlertTarget.CategoryStation(
                  stationId = s.id,
                  cityId = savedPlaceAlertsCityId,
                  stationName = s.name,
                  categoryId = category.id,
                  categoryLabel = category.label,
                ),
              ) != null
            } == true,
          )
        }
      }
      item {
        AnimatedVisibility(
          visible = stations.isEmpty() && homeStation == null && workStation == null,
          enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
          exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
          label = "favorites-empty",
        ) {
          EmptyStatePlaceholder(
            title = stringResource(Res.string.favoritesEmptyTitle),
            description = stringResource(Res.string.favoritesEmptyDescription),
          )
        }
      }
      if (stations.isNotEmpty()) {
        items(stations.distinctBy { it.id }, key = { it.id }) { station ->
          DismissibleFavoriteStationRow(
            mobilePlatform = mobilePlatform,
            station = station,
            canAssignHome = homeStation == null,
            canAssignWork = workStation == null,
            onClick = { onStationSelected(station) },
            onAssignHome = { onAssignHomeStation(station) },
            onAssignWork = { onAssignWorkStation(station) },
            onQuickRoute = { onQuickRoute(station) },
            onRemoveFavorite = { onRemoveFavorite(station) },
            onSavedPlaceAlertClick = if (upsertAlert != null) {
              {
                val t = SavedPlaceAlertTarget.CategoryStation(
                  stationId = station.id,
                  cityId = savedPlaceAlertsCityId,
                  stationName = station.name,
                  categoryId = FavoriteCategoryIds.FAVORITE,
                  categoryLabel = "Favorita",
                )
                alertEditor = t to findSavedPlaceAlertRule(savedPlaceAlertRules, t)
              }
            } else {
              null
            },
            savedPlaceAlertActive = findSavedPlaceAlertRule(
              savedPlaceAlertRules,
              SavedPlaceAlertTarget.CategoryStation(
                stationId = station.id,
                cityId = savedPlaceAlertsCityId,
                stationName = station.name,
                categoryId = FavoriteCategoryIds.FAVORITE,
                categoryLabel = "Favorita",
              ),
            ) != null,
          )
        }
      }
    }
  }
}

@Composable
internal fun DismissibleFavoriteStationRow(
  mobilePlatform: MobileUiPlatform,
  station: Station,
  canAssignHome: Boolean,
  canAssignWork: Boolean,
  onClick: () -> Unit,
  onAssignHome: () -> Unit,
  onAssignWork: () -> Unit,
  onQuickRoute: () -> Unit,
  onRemoveFavorite: () -> Unit,
  onSavedPlaceAlertClick: (() -> Unit)?,
  savedPlaceAlertActive: Boolean,
) {
  val dismissState = rememberSwipeToDismissBoxState()
  LaunchedEffect(dismissState.currentValue) {
    if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
      onRemoveFavorite()
      dismissState.snapTo(SwipeToDismissBoxValue.Settled)
    }
  }
  SwipeToDismissBox(
    state = dismissState,
    enableDismissFromStartToEnd = false,
    backgroundContent = {
      FavoriteDismissBackground(
        mobilePlatform = mobilePlatform,
        progress = dismissState.progress,
      )
    },
    content = {
      StationRow(
        mobilePlatform = mobilePlatform,
        station = station,
        isFavorite = true,
        onClick = onClick,
        onFavoriteToggle = {},
        onQuickRoute = onQuickRoute,
        savedPlaceAlertSlot = if (onSavedPlaceAlertClick != null) {
          {
            IconButton(
              onClick = onSavedPlaceAlertClick,
              modifier = Modifier.size(40.dp),
            ) {
              Icon(
                imageVector = if (savedPlaceAlertActive) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                contentDescription = stringResource(Res.string.savedPlaceAlertsBell),
                tint = if (savedPlaceAlertActive) LocalBiziColors.current.blue else LocalBiziColors.current.muted,
              )
            }
          }
        } else {
          null
        },
        extraActions = {
          if (canAssignHome) {
            SavedPlaceQuickAction(
              label = stringResource(Res.string.home),
              tint = LocalBiziColors.current.green,
              onClick = onAssignHome,
            )
          }
          if (canAssignWork) {
            SavedPlaceQuickAction(
              label = stringResource(Res.string.work),
              tint = LocalBiziColors.current.blue,
              onClick = onAssignWork,
            )
          }
        },
        showFavoriteCta = false,
      )
    },
  )
}

@Composable
internal fun FavoriteDismissBackground(
  mobilePlatform: MobileUiPlatform,
  progress: Float,
) {
  val clampedProgress = progress.coerceIn(0f, 1f)
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 22.dp else 24.dp))
      .background(LocalBiziColors.current.red.copy(alpha = 0.10f + (0.10f * clampedProgress)))
      .padding(horizontal = 20.dp, vertical = 12.dp),
    contentAlignment = Alignment.CenterEnd,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.graphicsLayer {
        alpha = 0.55f + (0.45f * clampedProgress)
        scaleX = 0.92f + (0.08f * clampedProgress)
        scaleY = 0.92f + (0.08f * clampedProgress)
      },
    ) {
      Icon(
        Icons.Filled.Delete,
        contentDescription = null,
        tint = LocalBiziColors.current.red,
      )
      Text(
        text = if (mobilePlatform == MobileUiPlatform.IOS) stringResource(Res.string.removeFavorite) else stringResource(Res.string.deleteFavorite),
        color = LocalBiziColors.current.red,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

@Composable
internal fun SavedPlaceCard(
  mobilePlatform: MobileUiPlatform,
  title: String,
  station: Station?,
  assignmentCandidate: Station?,
  onAssignCandidate: (Station) -> Unit,
  onClear: () -> Unit,
  onOpenStationDetails: (Station) -> Unit,
  onQuickRoute: (Station) -> Unit,
  onSavedPlaceAlertClick: (() -> Unit)? = null,
  savedPlaceAlertActive: Boolean = false,
) {
  val assignableCandidate = assignmentCandidate?.takeIf { it.id != station?.id }
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .animateContentSize(animationSpec = spring(dampingRatio = 0.88f, stiffness = 500f)),
    shape = RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 22.dp else 24.dp),
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, LocalBiziColors.current.panel) else null,
    colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
      )
      if (station != null) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = station.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = station.address,
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          StationMetricPill(
            modifier = Modifier.weight(1f),
            label = stringResource(Res.string.bikes),
            value = station.bikesAvailable.toString(),
            tint = LocalBiziColors.current.red,
          )
          StationMetricPill(
            modifier = Modifier.weight(1f),
            label = stringResource(Res.string.slots),
            value = station.slotsFree.toString(),
            tint = LocalBiziColors.current.blue,
          )
          StationMetricPill(
            modifier = Modifier.weight(1f),
            label = stringResource(Res.string.distance),
            value = formatDistance(station.distanceMeters),
            tint = LocalBiziColors.current.green,
          )
        }
      } else {
        Text(
          text = stringResource(Res.string.savedPlaceNotSet, title),
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (station != null) {
          RoutePill(
            label = stringResource(Res.string.route),
            onClick = { onQuickRoute(station) },
          )
          OutlineActionPill(
            label = stringResource(Res.string.details),
            tint = LocalBiziColors.current.red,
            borderTint = LocalBiziColors.current.red.copy(alpha = 0.16f),
            onClick = { onOpenStationDetails(station) },
          )
        }
        if (assignableCandidate != null) {
          OutlineActionPill(
            label = stringResource(Res.string.assignSearchResult),
            tint = LocalBiziColors.current.blue,
            borderTint = LocalBiziColors.current.blue.copy(alpha = 0.16f),
            onClick = { onAssignCandidate(assignableCandidate) },
          )
        }
        if (station != null) {
          OutlineActionPill(
            label = stringResource(Res.string.remove),
            tint = LocalBiziColors.current.muted,
            borderTint = LocalBiziColors.current.panel,
            onClick = onClear,
          )
        }
        if (station != null && onSavedPlaceAlertClick != null) {
          IconButton(
            onClick = onSavedPlaceAlertClick,
            modifier = Modifier.size(40.dp),
          ) {
            Icon(
              imageVector = if (savedPlaceAlertActive) Icons.Filled.Notifications else Icons.Outlined.Notifications,
              contentDescription = stringResource(Res.string.savedPlaceAlertsBell),
              tint = if (savedPlaceAlertActive) LocalBiziColors.current.blue else LocalBiziColors.current.muted,
            )
          }
        }
      }
      if (assignableCandidate != null) {
        Text(
          text = stringResource(Res.string.currentSearchAssignmentHint, assignableCandidate.name, title),
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      } else if (station == null) {
        Text(
          text = stringResource(Res.string.useSearchToAssignStation),
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      }
    }
  }
}

@Composable
internal fun SavedPlaceQuickAction(
  label: String,
  tint: Color,
  onClick: () -> Unit,
) {
  OutlineActionPill(
    label = label,
    tint = tint,
    borderTint = tint.copy(alpha = 0.16f),
    onClick = onClick,
  )
}
