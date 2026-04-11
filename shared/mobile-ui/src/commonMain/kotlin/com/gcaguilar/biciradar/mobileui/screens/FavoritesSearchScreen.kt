package com.gcaguilar.biciradar.mobileui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.FavoriteCategory
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.filterStationsByQuery
import com.gcaguilar.biciradar.core.formatDistance
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.bikes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.distance
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesSearchStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesSearchNoMatchesTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesSearchStartTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesSearchSubtitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesSearchTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.home
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapTryAnotherQuery
import com.gcaguilar.biciradar.mobile_ui.generated.resources.slots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.useSearchToAssignStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.work
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.PlatformBackHandler
import com.gcaguilar.biciradar.mobileui.components.EmptyStatePlaceholder
import com.gcaguilar.biciradar.mobileui.components.inputs.StationSearchField
import com.gcaguilar.biciradar.mobileui.components.station.StationMetricPill
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FavoritesSearchScreen(
  mobilePlatform: MobileUiPlatform,
  allStations: List<Station>,
  favoriteStationIds: Set<String>,
  homeStationId: String?,
  workStationId: String?,
  categories: List<FavoriteCategory>,
  searchQuery: String,
  newCategoryName: String,
  onSearchQueryChange: (String) -> Unit,
  onNewCategoryNameChange: (String) -> Unit,
  onBack: () -> Unit,
  onOpenStationDetails: (Station) -> Unit,
  onToggleFavorite: (Station) -> Unit,
  onAssignHome: (Station) -> Unit,
  onAssignWork: (Station) -> Unit,
  onAssignStationToCategory: (Station, String) -> Unit,
  onCreateCustomCategory: () -> Unit,
) {
  PlatformBackHandler(enabled = true, onBack = onBack)
  val searchResults = androidx.compose.runtime.remember(allStations, searchQuery) {
    if (searchQuery.isBlank()) {
      emptyList()
    } else {
      filterStationsByQuery(allStations, searchQuery).take(12)
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(Res.string.favoritesSearchTitle),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = null,
            )
          }
        },
      )
    },
    containerColor = pageBackgroundColor(mobilePlatform),
  ) { innerPadding ->
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .responsivePageWidth(),
    contentPadding = PaddingValues(
      start = 16.dp,
      end = 16.dp,
      top = innerPadding.calculateTopPadding() + 16.dp,
      bottom = innerPadding.calculateBottomPadding() + 16.dp,
    ),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Text(
        text = stringResource(Res.string.favoritesSearchSubtitle),
        style = MaterialTheme.typography.bodySmall,
        color = LocalBiziColors.current.muted,
      )
    }
    item {
      StationSearchField(
        mobilePlatform = mobilePlatform,
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        label = stringResource(Res.string.favoritesSearchStation),
      )
    }
    if (searchQuery.isBlank()) {
      item {
        EmptyStatePlaceholder(
          title = stringResource(Res.string.favoritesSearchStartTitle),
          description = stringResource(Res.string.useSearchToAssignStation),
        )
      }
    } else if (searchResults.isEmpty()) {
      item {
        EmptyStatePlaceholder(
          title = stringResource(Res.string.favoritesSearchNoMatchesTitle),
          description = stringResource(Res.string.mapTryAnotherQuery),
        )
      }
    } else {
      items(searchResults, key = { it.id }) { station ->
        SearchResultCard(
          mobilePlatform = mobilePlatform,
          station = station,
          isHome = station.id == homeStationId,
          isWork = station.id == workStationId,
          onOpenStationDetails = { onOpenStationDetails(station) },
          onAssignHome = { onAssignHome(station) },
          onAssignWork = { onAssignWork(station) },
        )
      }
    }
  }
  }
}

@Composable
private fun SearchResultCard(
  mobilePlatform: MobileUiPlatform,
  station: Station,
  isHome: Boolean,
  isWork: Boolean,
  onOpenStationDetails: () -> Unit,
  onAssignHome: () -> Unit,
  onAssignWork: () -> Unit,
) {
  val colors = LocalBiziColors.current
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onOpenStationDetails),
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
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = station.name,
          style = MaterialTheme.typography.titleMedium,
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
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        SavedPlaceChoiceButton(
          modifier = Modifier.weight(1f),
          label = stringResource(Res.string.home),
          selected = isHome,
          colors = colors,
          onClick = onAssignHome,
        )
        SavedPlaceChoiceButton(
          modifier = Modifier.weight(1f),
          label = stringResource(Res.string.work),
          selected = isWork,
          colors = colors,
          onClick = onAssignWork,
        )
      }
    }
  }
}

@Composable
private fun SavedPlaceChoiceButton(
  modifier: Modifier,
  label: String,
  selected: Boolean,
  colors: com.gcaguilar.biciradar.mobileui.BiziColors,
  onClick: () -> Unit,
) {
  if (selected) {
    Button(
      modifier = modifier,
      onClick = onClick,
      colors = ButtonDefaults.buttonColors(
        containerColor = colors.blue,
        contentColor = colors.onAccent,
      ),
    ) {
      Text(label)
    }
  } else {
    OutlinedButton(
      modifier = modifier,
      onClick = onClick,
    ) {
      Text(label)
    }
  }
}
