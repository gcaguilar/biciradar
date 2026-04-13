package com.gcaguilar.biciradar.mobileui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.FavoriteCategoryIds
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.findSavedPlaceAlertRule
import com.gcaguilar.biciradar.core.formatDistance
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.assignSearchResult
import com.gcaguilar.biciradar.mobile_ui.generated.resources.create
import com.gcaguilar.biciradar.mobile_ui.generated.resources.currentSearchAssignmentHint
import com.gcaguilar.biciradar.mobile_ui.generated.resources.delete
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favorite
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favorites
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesAddStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesAlertActive
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesAlertConditionBikesPlural
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesAlertConditionBikesSingular
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesAlertConditionNoBikes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesAlertConditionNoSlots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesAlertConditionSlotsPlural
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesAlertConditionSlotsSingular
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesAlertInactive
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesAvailabilitySummary
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesCustomCategoriesTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesCustomCategoryPlaceholder
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesDistanceFromYou
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesEmptyDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesEmptyTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesManyAlerts
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesManyFavoritesCount
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesManyStationsCount
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesNoAlerts
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesNoFavoritesCount
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesNoStationsCount
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesOneAlert
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesOneFavoriteCount
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesOneStationCount
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesOtherSection
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesOverviewDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesOverviewTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesPinnedSection
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesQuickAccessTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesSearchStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesSubtitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.home
import com.gcaguilar.biciradar.mobile_ui.generated.resources.myStations
import com.gcaguilar.biciradar.mobile_ui.generated.resources.remove
import com.gcaguilar.biciradar.mobile_ui.generated.resources.route
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsBell
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsProfileAction
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceNotSet
import com.gcaguilar.biciradar.mobile_ui.generated.resources.useSearchToAssignStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.work
import com.gcaguilar.biciradar.mobileui.DataFreshnessBanner
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.SavedPlaceAlertEditorSheet
import com.gcaguilar.biciradar.mobileui.components.EmptyStatePlaceholder
import com.gcaguilar.biciradar.mobileui.components.favorites.CountBadge
import com.gcaguilar.biciradar.mobileui.components.favorites.FavoriteDismissBackground
import com.gcaguilar.biciradar.mobileui.components.favorites.SectionHeader
import com.gcaguilar.biciradar.mobileui.components.favorites.SectionPill
import com.gcaguilar.biciradar.mobileui.components.favorites.StatusPill
import com.gcaguilar.biciradar.mobileui.components.station.OutlineActionPill
import com.gcaguilar.biciradar.mobileui.components.station.RoutePill
import com.gcaguilar.biciradar.mobileui.components.station.StationRow
import com.gcaguilar.biciradar.mobileui.favoriteCategoryLabel
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import com.gcaguilar.biciradar.mobileui.viewmodel.FavoritesUiState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FavoritesScreen(
  state: FavoritesUiState,
  mobilePlatform: MobileUiPlatform,
  onOpenAssistant: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onStationSelected: (Station) -> Unit,
  onAssignHomeStation: (Station) -> Unit,
  onAssignWorkStation: (Station) -> Unit,
  onClearHomeStation: () -> Unit,
  onClearWorkStation: () -> Unit,
  onRemoveFavorite: (Station) -> Unit,
  onQuickRoute: (Station) -> Unit,
  onRefreshStations: () -> Unit,
  onOpenSavedPlaceAlerts: () -> Unit,
  onOpenSearch: () -> Unit,
  paddingValues: PaddingValues,
  onCreateCustomCategory: (String) -> Unit,
  onNewCategoryNameChange: (String) -> Unit,
  onAssignCandidateToCategory: (String) -> Unit,
  onRemoveCustomCategory: (String) -> Unit,
  onClearCategoryAssignment: (String) -> Unit,
  onUpsertSavedPlaceAlert: ((SavedPlaceAlertTarget, SavedPlaceAlertCondition) -> Unit)?,
  onRemoveSavedPlaceAlertForTarget: ((SavedPlaceAlertTarget) -> Unit)?,
) {
  val allStations = state.allStations
  val stations = state.favoriteStations
  val homeStation = state.homeStation
  val workStation = state.workStation
  val searchQuery = state.searchQuery
  val assignmentCandidate = state.assignmentCandidate
  val dataFreshness = state.dataFreshness
  val lastUpdatedEpoch = state.lastUpdatedEpoch
  val stationsLoading = state.stationsLoading
  val savedPlaceAlertsCityId = state.savedPlaceAlertsCityId
  val savedPlaceAlertRules = state.savedPlaceAlertRules
  val categories = state.categories
  val stationCategory = state.stationCategory
  var alertEditor by remember { mutableStateOf<Pair<SavedPlaceAlertTarget, SavedPlaceAlertRule?>?>(null) }
  val newCategoryName = state.newCategoryName
  val colors = LocalBiziColors.current
  val customCategories =
    remember(categories) {
      categories.filterNot {
        it.id in
          setOf(FavoriteCategoryIds.FAVORITE, FavoriteCategoryIds.HOME, FavoriteCategoryIds.WORK)
      }
    }
  val pinnedCount = listOfNotNull(homeStation, workStation).size
  val activeAlertCount = savedPlaceAlertRules.count { it.isEnabled }
  val homeLabel = stringResource(Res.string.home)
  val workLabel = stringResource(Res.string.work)
  val favoriteLabel = stringResource(Res.string.favorite)
  val pageTitle =
    if (mobilePlatform == MobileUiPlatform.IOS) {
      stringResource(Res.string.favorites)
    } else {
      stringResource(Res.string.myStations)
    }
  val currentAlertEditor = alertEditor
  if (onUpsertSavedPlaceAlert != null && onRemoveSavedPlaceAlertForTarget != null && currentAlertEditor != null) {
    val (target, rule) = currentAlertEditor
    SavedPlaceAlertEditorSheet(
      target = target,
      existingRule = rule,
      onDismiss = { },
      onSave = { cond ->
        onUpsertSavedPlaceAlert(target, cond)
      },
      onRemove = {
        onRemoveSavedPlaceAlertForTarget(target)
      },
    )
  }
  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(pageBackgroundColor(mobilePlatform)),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier =
        if (mobilePlatform ==
          MobileUiPlatform.Desktop
        ) {
          Modifier.fillMaxWidth()
        } else {
          Modifier.responsivePageWidth()
        },
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
        ) {
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = stringResource(Res.string.favoritesQuickAccessTitle),
              style = MaterialTheme.typography.bodySmall,
              color = colors.muted,
            )
            Text(
              text = pageTitle,
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = stringResource(Res.string.favoritesSubtitle),
              style = MaterialTheme.typography.bodyMedium,
              color = colors.muted,
            )
          }
          Spacer(Modifier.width(12.dp))
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = colors.surface,
            border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, colors.panel) else null,
            modifier = Modifier.clickable(onClick = onOpenSavedPlaceAlerts),
          ) {
            Icon(
              imageVector = Icons.Filled.Notifications,
              contentDescription = null,
              tint = colors.blue,
              modifier = Modifier.padding(12.dp),
            )
          }
        }
      }
      item {
        FavoritesOverviewCard(
          mobilePlatform = mobilePlatform,
          activeAlertCount = activeAlertCount,
          onOpenSavedPlaceAlerts = onOpenSavedPlaceAlerts,
          onOpenSearch = onOpenSearch,
        )
      }
      item {
        FavoritesSearchLauncher(
          mobilePlatform = mobilePlatform,
          value = searchQuery,
          label = stringResource(Res.string.favoritesSearchStation),
          onClick = onOpenSearch,
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
        SectionHeader(
          title = stringResource(Res.string.favoritesPinnedSection),
          countLabel = countLabelForPinned(pinnedCount),
        )
      }
      item {
        val rule =
          homeStation?.let { station ->
            findSavedPlaceAlertRule(
              savedPlaceAlertRules,
              SavedPlaceAlertTarget.CategoryStation(
                stationId = station.id,
                cityId = savedPlaceAlertsCityId,
                stationName = station.name,
                categoryId = FavoriteCategoryIds.HOME,
                categoryLabel = homeLabel,
              ),
            )
          }
        val savedPlaceAlertLabel =
          if (rule != null) {
            savedPlaceAlertConditionSummary(rule.condition)
          } else {
            null
          }
        SavedPlaceCard(
          mobilePlatform = mobilePlatform,
          title = stringResource(Res.string.home),
          station = homeStation,
          assignmentCandidate = assignmentCandidate,
          onAssignCandidate = onAssignHomeStation,
          onClear = onClearHomeStation,
          onOpenStationDetails = onStationSelected,
          onQuickRoute = onQuickRoute,
          onSavedPlaceAlertClick =
            run {
              val s = homeStation
              if (s != null && onUpsertSavedPlaceAlert != null) {
                {
                  val t =
                    SavedPlaceAlertTarget.CategoryStation(
                      stationId = s.id,
                      cityId = savedPlaceAlertsCityId,
                      stationName = s.name,
                      categoryId = FavoriteCategoryIds.HOME,
                      categoryLabel = homeLabel,
                    )
                  alertEditor = t to findSavedPlaceAlertRule(savedPlaceAlertRules, t)
                }
              } else {
                null
              }
            },
          savedPlaceAlertLabel = savedPlaceAlertLabel,
          savedPlaceAlertActive =
            homeStation?.let { s ->
              findSavedPlaceAlertRule(
                savedPlaceAlertRules,
                SavedPlaceAlertTarget.CategoryStation(
                  stationId = s.id,
                  cityId = savedPlaceAlertsCityId,
                  stationName = s.name,
                  categoryId = FavoriteCategoryIds.HOME,
                  categoryLabel = homeLabel,
                ),
              ) != null
            } == true,
        )
      }
      item {
        val rule =
          workStation?.let { station ->
            findSavedPlaceAlertRule(
              savedPlaceAlertRules,
              SavedPlaceAlertTarget.CategoryStation(
                stationId = station.id,
                cityId = savedPlaceAlertsCityId,
                stationName = station.name,
                categoryId = FavoriteCategoryIds.WORK,
                categoryLabel = workLabel,
              ),
            )
          }
        val savedPlaceAlertLabel =
          if (rule != null) {
            savedPlaceAlertConditionSummary(rule.condition)
          } else {
            null
          }
        SavedPlaceCard(
          mobilePlatform = mobilePlatform,
          title = stringResource(Res.string.work),
          station = workStation,
          assignmentCandidate = assignmentCandidate,
          onAssignCandidate = onAssignWorkStation,
          onClear = onClearWorkStation,
          onOpenStationDetails = onStationSelected,
          onQuickRoute = onQuickRoute,
          onSavedPlaceAlertClick =
            run {
              val s = workStation
              if (s != null && onUpsertSavedPlaceAlert != null) {
                {
                  val t =
                    SavedPlaceAlertTarget.CategoryStation(
                      stationId = s.id,
                      cityId = savedPlaceAlertsCityId,
                      stationName = s.name,
                      categoryId = FavoriteCategoryIds.WORK,
                      categoryLabel = workLabel,
                    )
                  alertEditor = t to findSavedPlaceAlertRule(savedPlaceAlertRules, t)
                }
              } else {
                null
              }
            },
          savedPlaceAlertLabel = savedPlaceAlertLabel,
          savedPlaceAlertActive =
            workStation?.let { s ->
              findSavedPlaceAlertRule(
                savedPlaceAlertRules,
                SavedPlaceAlertTarget.CategoryStation(
                  stationId = s.id,
                  cityId = savedPlaceAlertsCityId,
                  stationName = s.name,
                  categoryId = FavoriteCategoryIds.WORK,
                  categoryLabel = workLabel,
                ),
              ) != null
            } == true,
        )
      }
      customCategories.forEach { category ->
        item(key = "custom-category-${category.id}") {
          val assignedStationId = stationCategory.entries.firstOrNull { it.value == category.id }?.key
          val assignedStation = allStations.find { it.id == assignedStationId }
          val rule =
            assignedStation?.let { station ->
              findSavedPlaceAlertRule(
                savedPlaceAlertRules,
                SavedPlaceAlertTarget.CategoryStation(
                  stationId = station.id,
                  cityId = savedPlaceAlertsCityId,
                  stationName = station.name,
                  categoryId = category.id,
                  categoryLabel = favoriteCategoryLabel(category, homeLabel, workLabel, favoriteLabel),
                ),
              )
            }
          val savedPlaceAlertLabel =
            if (rule != null) {
              savedPlaceAlertConditionSummary(rule.condition)
            } else {
              null
            }
          SavedPlaceCard(
            mobilePlatform = mobilePlatform,
            title = favoriteCategoryLabel(category, homeLabel, workLabel, favoriteLabel),
            station = assignedStation,
            assignmentCandidate = assignmentCandidate,
            onAssignCandidate = { onAssignCandidateToCategory(category.id) },
            onClear = { onClearCategoryAssignment(category.id) },
            onOpenStationDetails = onStationSelected,
            onQuickRoute = onQuickRoute,
            onSavedPlaceAlertClick =
              run {
                val s = assignedStation
                if (s != null && onUpsertSavedPlaceAlert != null) {
                  {
                    val t =
                      SavedPlaceAlertTarget.CategoryStation(
                        stationId = s.id,
                        cityId = savedPlaceAlertsCityId,
                        stationName = s.name,
                        categoryId = category.id,
                        categoryLabel = favoriteCategoryLabel(category, homeLabel, workLabel, favoriteLabel),
                      )
                    alertEditor = t to findSavedPlaceAlertRule(savedPlaceAlertRules, t)
                  }
                } else {
                  null
                }
              },
            savedPlaceAlertLabel = savedPlaceAlertLabel,
            savedPlaceAlertActive =
              assignedStation?.let { s ->
                findSavedPlaceAlertRule(
                  savedPlaceAlertRules,
                  SavedPlaceAlertTarget.CategoryStation(
                    stationId = s.id,
                    cityId = savedPlaceAlertsCityId,
                    stationName = s.name,
                    categoryId = category.id,
                    categoryLabel = favoriteCategoryLabel(category, homeLabel, workLabel, favoriteLabel),
                  ),
                ) != null
              } == true,
          )
        }
      }
      if (customCategories.isNotEmpty() || assignmentCandidate != null) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 22.dp else 24.dp),
            border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, colors.panel) else null,
            colors = CardDefaults.cardColors(containerColor = colors.surface),
          ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(stringResource(Res.string.favoritesCustomCategoriesTitle), fontWeight = FontWeight.SemiBold)
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.OutlinedTextField(
                  value = newCategoryName,
                  onValueChange = { onNewCategoryNameChange(it) },
                  modifier = Modifier.weight(1f),
                  singleLine = true,
                  label = { Text(stringResource(Res.string.favoritesCustomCategoryPlaceholder)) },
                )
                TextButton(onClick = {
                  val label = newCategoryName.trim()
                  if (label.isNotBlank()) {
                    onCreateCustomCategory(label)
                    onNewCategoryNameChange("")
                  }
                }) { Text(stringResource(Res.string.create)) }
              }
              customCategories.forEach { category ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    favoriteCategoryLabel(category, homeLabel, workLabel, favoriteLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                  )
                  Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    if (assignmentCandidate != null) {
                      TextButton(onClick = {
                        onAssignCandidateToCategory(category.id)
                      }) { Text(stringResource(Res.string.assignSearchResult)) }
                    }
                    TextButton(
                      onClick = { onRemoveCustomCategory(category.id) },
                    ) { Text(stringResource(Res.string.delete)) }
                  }
                }
              }
            }
          }
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
        item {
          SectionHeader(
            title = stringResource(Res.string.favoritesOtherSection),
            countLabel = countLabelForFavorites(stations.distinctBy { it.id }.size),
          )
        }
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
            onSavedPlaceAlertClick =
              if (onUpsertSavedPlaceAlert != null) {
                {
                  val t =
                    SavedPlaceAlertTarget.CategoryStation(
                      stationId = station.id,
                      cityId = savedPlaceAlertsCityId,
                      stationName = station.name,
                      categoryId = FavoriteCategoryIds.FAVORITE,
                      categoryLabel = favoriteLabel,
                    )
                  alertEditor = t to findSavedPlaceAlertRule(savedPlaceAlertRules, t)
                }
              } else {
                null
              },
            savedPlaceAlertActive =
              findSavedPlaceAlertRule(
                savedPlaceAlertRules,
                SavedPlaceAlertTarget.CategoryStation(
                  stationId = station.id,
                  cityId = savedPlaceAlertsCityId,
                  stationName = station.name,
                  categoryId = FavoriteCategoryIds.FAVORITE,
                  categoryLabel = favoriteLabel,
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
        savedPlaceAlertSlot =
          if (onSavedPlaceAlertClick != null) {
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
  savedPlaceAlertLabel: String? = null,
  savedPlaceAlertActive: Boolean = false,
) {
  val colors = LocalBiziColors.current
  val assignableCandidate = assignmentCandidate?.takeIf { it.id != station?.id }
  Card(
    modifier =
      Modifier
        .fillMaxWidth()
        .animateContentSize(animationSpec = spring(dampingRatio = 0.88f, stiffness = 500f)),
    shape = RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 22.dp else 24.dp),
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, colors.panel) else null,
    colors = CardDefaults.cardColors(containerColor = colors.surface),
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SectionPill(
          label = title.uppercase(),
          tint =
            when (title.lowercase()) {
              stringResource(Res.string.home).lowercase() -> colors.blue
              stringResource(Res.string.work).lowercase() -> colors.orange
              else -> colors.muted
            },
        )
        if (station != null && onSavedPlaceAlertClick != null) {
          StatusPill(
            label =
              savedPlaceAlertLabel ?: if (savedPlaceAlertActive) {
                stringResource(Res.string.favoritesAlertActive)
              } else {
                stringResource(Res.string.favoritesAlertInactive)
              },
            active = savedPlaceAlertActive,
            onClick = onSavedPlaceAlertClick,
          )
        }
      }
      if (station != null) {
        Column(
          modifier = Modifier.clickable { onOpenStationDetails(station) },
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = station.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = stringResource(Res.string.favoritesDistanceFromYou, formatDistance(station.distanceMeters)),
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
          )
        }
        Text(
          text =
            stringResource(
              Res.string.favoritesAvailabilitySummary,
              station.bikesAvailable,
              station.slotsFree,
            ),
          style = MaterialTheme.typography.bodySmall,
          color = colors.ink,
        )
      } else {
        Text(
          text = stringResource(Res.string.savedPlaceNotSet, title),
          style = MaterialTheme.typography.bodySmall,
          color = colors.muted,
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
        }
        if (assignableCandidate != null) {
          OutlineActionPill(
            label = stringResource(Res.string.assignSearchResult),
            tint = colors.blue,
            borderTint = colors.blue.copy(alpha = 0.16f),
            onClick = { onAssignCandidate(assignableCandidate) },
          )
        }
        if (station != null) {
          OutlineActionPill(
            label = stringResource(Res.string.remove),
            tint = colors.muted,
            borderTint = colors.panel,
            onClick = onClear,
          )
        }
      }
      if (assignableCandidate != null) {
        Text(
          text = stringResource(Res.string.currentSearchAssignmentHint, assignableCandidate.name, title),
          style = MaterialTheme.typography.bodySmall,
          color = colors.muted,
        )
      } else if (station == null) {
        Text(
          text = stringResource(Res.string.useSearchToAssignStation),
          style = MaterialTheme.typography.bodySmall,
          color = colors.muted,
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

@Composable
private fun FavoritesOverviewCard(
  mobilePlatform: MobileUiPlatform,
  activeAlertCount: Int,
  onOpenSavedPlaceAlerts: () -> Unit,
  onOpenSearch: () -> Unit,
) {
  val colors = LocalBiziColors.current
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 22.dp else 24.dp),
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, colors.panel) else null,
    colors = CardDefaults.cardColors(containerColor = colors.surface),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = stringResource(Res.string.favoritesOverviewTitle),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
        )
        CountBadge(
          label =
            when (activeAlertCount) {
              0 -> stringResource(Res.string.favoritesNoAlerts)
              1 -> stringResource(Res.string.favoritesOneAlert)
              else -> stringResource(Res.string.favoritesManyAlerts, activeAlertCount)
            },
        )
      }
      Text(
        text = stringResource(Res.string.favoritesOverviewDescription),
        style = MaterialTheme.typography.bodySmall,
        color = colors.muted,
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Button(
          modifier = Modifier.weight(1f),
          onClick = onOpenSavedPlaceAlerts,
          colors =
            ButtonDefaults.buttonColors(
              containerColor = colors.blue,
              contentColor = colors.onAccent,
            ),
        ) {
          Text(stringResource(Res.string.savedPlaceAlertsProfileAction))
        }
        OutlinedButton(
          modifier = Modifier.weight(1f),
          onClick = onOpenSearch,
        ) {
          Text(stringResource(Res.string.favoritesAddStation))
        }
      }
    }
  }
}

@Composable
private fun FavoritesSearchLauncher(
  mobilePlatform: MobileUiPlatform,
  value: String,
  label: String,
  onClick: () -> Unit,
) {
  val colors = LocalBiziColors.current
  OutlinedButton(
    modifier = Modifier.fillMaxWidth(),
    onClick = onClick,
    shape = RoundedCornerShape(20.dp),
    border =
      BorderStroke(
        1.dp,
        if (mobilePlatform == MobileUiPlatform.IOS) colors.panel else colors.muted.copy(alpha = 0.18f),
      ),
    colors =
      ButtonDefaults.outlinedButtonColors(
        containerColor =
          if (mobilePlatform ==
            MobileUiPlatform.IOS
          ) {
            colors.fieldSurfaceIos
          } else {
            colors.fieldSurfaceAndroid
          },
        contentColor = colors.ink,
      ),
  ) {
    Icon(
      imageVector = Icons.Filled.Search,
      contentDescription = null,
      tint = colors.muted,
      modifier = Modifier.size(18.dp),
    )
    Spacer(Modifier.width(8.dp))
    Text(
      text = value.ifBlank { label },
      color = if (value.isBlank()) colors.muted else colors.ink,
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun countLabelForPinned(count: Int): String =
  when (count) {
    0 -> stringResource(Res.string.favoritesNoStationsCount)
    1 -> stringResource(Res.string.favoritesOneStationCount)
    else -> stringResource(Res.string.favoritesManyStationsCount, count)
  }

@Composable
private fun countLabelForFavorites(count: Int): String =
  when (count) {
    0 -> stringResource(Res.string.favoritesNoFavoritesCount)
    1 -> stringResource(Res.string.favoritesOneFavoriteCount)
    else -> stringResource(Res.string.favoritesManyFavoritesCount, count)
  }

@Composable
private fun savedPlaceAlertConditionSummary(condition: SavedPlaceAlertCondition): String =
  when (condition) {
    is SavedPlaceAlertCondition.BikesAtLeast ->
      if (condition.count == 1) {
        stringResource(Res.string.favoritesAlertConditionBikesSingular)
      } else {
        stringResource(Res.string.favoritesAlertConditionBikesPlural, condition.count)
      }
    is SavedPlaceAlertCondition.DocksAtLeast ->
      if (condition.count == 1) {
        stringResource(Res.string.favoritesAlertConditionSlotsSingular)
      } else {
        stringResource(Res.string.favoritesAlertConditionSlotsPlural, condition.count)
      }
    SavedPlaceAlertCondition.BikesEqualsZero -> stringResource(Res.string.favoritesAlertConditionNoBikes)
    SavedPlaceAlertCondition.DocksEqualsZero -> stringResource(Res.string.favoritesAlertConditionNoSlots)
  }
