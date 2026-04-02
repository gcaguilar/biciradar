package com.gcaguilar.biciradar.mobileui

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.SEARCH_RADIUS_OPTIONS_METERS
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.core.formatDistance
import com.gcaguilar.biciradar.mobile_ui.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ProfileScreen(
  mobilePlatform: MobileUiPlatform,
  paddingValues: PaddingValues,
  searchRadiusMeters: Int,
  preferredMapApp: PreferredMapApp,
  themePreference: ThemePreference,
  selectedCity: City,
  onSearchRadiusSelected: (Int) -> Unit,
  onPreferredMapAppSelected: (PreferredMapApp) -> Unit,
  onThemePreferenceSelected: (ThemePreference) -> Unit,
  onCitySelected: (City) -> Unit,
  showProfileSetupCard: Boolean,
  onShowChangelog: () -> Unit,
  onOpenOnboarding: () -> Unit,
  onOpenFeedback: () -> Unit,
  onRateApp: () -> Unit,
) {
  var showFeedbackDialog by remember { mutableStateOf(false) }
  var showDataSourcesDialog by remember { mutableStateOf(false) }
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(pageBackgroundColor(mobilePlatform)),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.responsivePageWidth(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = stringResource(Res.string.settings),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = stringResource(Res.string.profileSubtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalBiziColors.current.muted,
          )
        }
      }
      if (showProfileSetupCard) {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
          ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(stringResource(Res.string.profileSetupCardTitle), fontWeight = FontWeight.SemiBold)
              Text(
                stringResource(Res.string.profileSetupCardBody),
                style = MaterialTheme.typography.bodySmall,
                color = LocalBiziColors.current.muted,
              )
              TextButton(
                onClick = onOpenOnboarding,
                contentPadding = PaddingValues(0.dp),
              ) {
                Text(stringResource(Res.string.profileSetupCardAction), style = MaterialTheme.typography.bodySmall)
              }
            }
          }
        }
      }
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(Res.string.viewWhatsNew), fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onShowChangelog, contentPadding = PaddingValues(0.dp)) {
              Text(stringResource(Res.string.viewWhatsNew), style = MaterialTheme.typography.bodySmall)
            }
          }
        }
      }
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(Res.string.nearbyStationRadius), fontWeight = FontWeight.SemiBold)
            Text(
              stringResource(Res.string.nearbyStationRadiusDescription),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            SearchRadiusSelector(
              selectedRadiusMeters = searchRadiusMeters,
              onSearchRadiusSelected = onSearchRadiusSelected,
            )
          }
        }
      }
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(Res.string.selectedCity), fontWeight = FontWeight.SemiBold)
            Text(
              stringResource(Res.string.citySelectionSubtitle),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            CitySelector(
              selectedCity = selectedCity,
              onCitySelected = onCitySelected,
            )
          }
        }
      }
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(Res.string.appearance), fontWeight = FontWeight.SemiBold)
            Text(
              stringResource(Res.string.appearanceDescription),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              RadiusSelectionButton(
                modifier = Modifier.weight(1f),
                selected = themePreference == ThemePreference.System,
                label = stringResource(Res.string.system),
                onClick = { onThemePreferenceSelected(ThemePreference.System) },
              )
              RadiusSelectionButton(
                modifier = Modifier.weight(1f),
                selected = themePreference == ThemePreference.Light,
                label = stringResource(Res.string.light),
                onClick = { onThemePreferenceSelected(ThemePreference.Light) },
              )
              RadiusSelectionButton(
                modifier = Modifier.weight(1f),
                selected = themePreference == ThemePreference.Dark,
                label = stringResource(Res.string.dark),
                onClick = { onThemePreferenceSelected(ThemePreference.Dark) },
              )
            }
          }
        }
      }
      if (mobilePlatform == MobileUiPlatform.IOS) {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
          ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
              Text(stringResource(Res.string.iPhoneRouteApp), fontWeight = FontWeight.SemiBold)
              Text(
                stringResource(Res.string.iPhoneRouteAppDescription),
                style = MaterialTheme.typography.bodySmall,
                color = LocalBiziColors.current.muted,
              )
              Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RadiusSelectionButton(
                  modifier = Modifier.weight(1f),
                  selected = preferredMapApp == PreferredMapApp.AppleMaps,
                  label = "Apple Maps",
                  onClick = { onPreferredMapAppSelected(PreferredMapApp.AppleMaps) },
                )
                RadiusSelectionButton(
                  modifier = Modifier.weight(1f),
                  selected = preferredMapApp == PreferredMapApp.GoogleMaps,
                  label = "Google Maps",
                  onClick = { onPreferredMapAppSelected(PreferredMapApp.GoogleMaps) },
                )
              }
              Text(
                stringResource(Res.string.iPhoneRouteAppFallback),
                style = MaterialTheme.typography.bodySmall,
                color = LocalBiziColors.current.muted,
              )
            }
          }
        }
      }
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(Res.string.rateApp), fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onRateApp, contentPadding = PaddingValues(0.dp)) {
              Text(stringResource(Res.string.rateApp), style = MaterialTheme.typography.bodySmall)
            }
          }
        }
      }
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(Res.string.feedbackAndSuggestions), fontWeight = FontWeight.SemiBold)
            Text(
              stringResource(Res.string.feedbackDescription),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            TextButton(
              onClick = { showFeedbackDialog = true },
              contentPadding = PaddingValues(0.dp),
            ) {
              Text(stringResource(Res.string.openFeedbackForm), style = MaterialTheme.typography.bodySmall)
            }
          }
        }
      }
      if (showFeedbackDialog) {
        item {
          FeedbackDialog(
            onDismiss = { showFeedbackDialog = false },
            onOpenFeedbackForm = {
              onOpenFeedback()
              showFeedbackDialog = false
            },
          )
        }
      }
      item {
        val uriHandler = LocalUriHandler.current
        Card(
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(Res.string.privacyAndData), fontWeight = FontWeight.SemiBold)
            Text(
              stringResource(Res.string.privacyDescription),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            TextButton(
              onClick = { uriHandler.openUri("https://gcaguilar.github.io/biciradar-privacy-policy/") },
              contentPadding = PaddingValues(0.dp),
            ) {
              Text(stringResource(Res.string.openPrivacyPolicy), style = MaterialTheme.typography.bodySmall)
            }
          }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showDataSourcesDialog = true }
              .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Text(stringResource(Res.string.dataSourceTitle), fontWeight = FontWeight.SemiBold)
            Text(
              stringResource(Res.string.dataSourceDescription),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            Text(
              text = stringResource(Res.string.dataSourceDetailsAction),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.blue,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
      }
    }
  }

  if (showDataSourcesDialog) {
    AlertDialog(
      onDismissRequest = { showDataSourcesDialog = false },
      title = { Text(stringResource(Res.string.dataSourceTitle)) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = stringResource(Res.string.dataSourceGbfsDetail),
            style = MaterialTheme.typography.bodySmall,
          )
          Text(
            text = stringResource(Res.string.dataSourceEnvironmentalDetail),
            style = MaterialTheme.typography.bodySmall,
          )
        }
      },
      confirmButton = {
        TextButton(onClick = { showDataSourcesDialog = false }) {
          Text(stringResource(Res.string.close))
        }
      },
    )
  }
}

@Composable
private fun SearchRadiusSelector(
  selectedRadiusMeters: Int,
  onSearchRadiusSelected: (Int) -> Unit,
) {
  val colors = LocalBiziColors.current
  var expanded by remember { mutableStateOf(false) }
  Box {
    OutlinedButton(
      onClick = { expanded = true },
      modifier = Modifier.fillMaxWidth(),
      border = BorderStroke(1.dp, colors.panel),
      colors = ButtonDefaults.outlinedButtonColors(containerColor = colors.surface),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = formatDistance(selectedRadiusMeters),
          color = colors.ink,
        )
        Icon(
          imageVector = Icons.Filled.Navigation,
          contentDescription = null,
          tint = colors.red,
        )
      }
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.background(colors.surface),
    ) {
      SEARCH_RADIUS_OPTIONS_METERS.forEach { radius ->
        DropdownMenuItem(
          text = {
            Text(
              text = formatDistance(radius),
              color = if (radius == selectedRadiusMeters) colors.red else colors.ink,
              fontWeight = if (radius == selectedRadiusMeters) FontWeight.SemiBold else FontWeight.Normal,
            )
          },
          onClick = {
            expanded = false
            onSearchRadiusSelected(radius)
          },
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShortcutsScreen(
  mobilePlatform: MobileUiPlatform,
  paddingValues: PaddingValues,
  searchRadiusMeters: Int,
  latestAnswer: String?,
  onBack: () -> Unit,
) {
  PlatformBackHandler(enabled = true, onBack = onBack)
  val shortcutGuides = shortcutGuidesFor(mobilePlatform)

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(pageBackgroundColor(mobilePlatform)),
    topBar = {
      TopAppBar(
        title = {
          if (mobilePlatform == MobileUiPlatform.IOS) {
            Text("")
          } else {
            Text(stringResource(Res.string.shortcuts))
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
          }
        },
      )
    },
    containerColor = pageBackgroundColor(mobilePlatform),
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(pageBackgroundColor(mobilePlatform)),
      contentAlignment = Alignment.TopCenter,
    ) {
      LazyColumn(
        modifier = Modifier.responsivePageWidth(),
        contentPadding = PaddingValues(
          start = 16.dp,
          top = innerPadding.calculateTopPadding() + 16.dp,
          end = 16.dp,
          bottom = innerPadding.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        if (mobilePlatform == MobileUiPlatform.IOS) {
          item {
            Column(
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = stringResource(Res.string.shortcuts),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = stringResource(Res.string.shortcutsIosSubtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalBiziColors.current.muted,
              )
            }
          }
        }
        item {
          Card(colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface)) {
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text(stringResource(Res.string.howToInvoke), fontWeight = FontWeight.SemiBold)
              Text(
                if (mobilePlatform == MobileUiPlatform.IOS) {
                  stringResource(Res.string.shortcutsAvailableOnIos)
                } else {
                  stringResource(Res.string.shortcutsAvailableWithAssistant, mobilePlatform.assistantDisplayName())
                },
                style = MaterialTheme.typography.bodySmall,
                color = LocalBiziColors.current.muted,
              )
              Text(
                if (mobilePlatform == MobileUiPlatform.IOS) {
                  stringResource(Res.string.shortcutsIosInvocationHint)
                } else {
                  stringResource(Res.string.shortcutsAndroidInvocationHint)
                },
                style = MaterialTheme.typography.bodySmall,
                color = LocalBiziColors.current.muted,
              )
              Text(
                stringResource(Res.string.shortcutsCurrentRadius, searchRadiusMeters),
                style = MaterialTheme.typography.bodySmall,
                color = LocalBiziColors.current.ink,
              )
            }
          }
        }
        latestAnswer?.let { answer ->
          item {
            Card(colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface)) {
              Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Text(stringResource(Res.string.latestAnswer), fontWeight = FontWeight.SemiBold)
                Text(answer)
              }
            }
          }
        }
        items(shortcutGuides, key = { it.title }) { guide ->
          ShortcutGuideCard(guide = guide)
        }
      }
    }
  }
}

@Composable
private fun CitySelector(
  selectedCity: City,
  onCitySelected: (City) -> Unit,
) {
  val colors = LocalBiziColors.current
  var expanded by remember { mutableStateOf(false) }

  Box {
    OutlinedButton(
      onClick = { expanded = true },
      modifier = Modifier.fillMaxWidth(),
      border = BorderStroke(1.dp, colors.panel),
      colors = ButtonDefaults.outlinedButtonColors(containerColor = colors.surface),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = selectedCity.displayName,
          color = colors.ink,
        )
        Icon(
          imageVector = Icons.Filled.Navigation,
          contentDescription = null,
          tint = colors.red,
        )
      }
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.background(colors.surface),
    ) {
      City.entries.sortedBy { it.displayName }.forEach { city ->
        DropdownMenuItem(
          text = {
            Text(
              text = city.displayName,
              color = if (city == selectedCity) colors.red else colors.ink,
              fontWeight = if (city == selectedCity) FontWeight.SemiBold else FontWeight.Normal,
            )
          },
          onClick = {
            onCitySelected(city)
            expanded = false
          },
          leadingIcon = {
            if (city == selectedCity) {
              Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = colors.red,
              )
            }
          },
        )
      }
    }
  }
}

private fun MobileUiPlatform.assistantDisplayName(): String = when (this) {
  MobileUiPlatform.Android -> "Google Assistant"
  MobileUiPlatform.IOS -> "Siri"
  MobileUiPlatform.Desktop -> "Asistente"
}

@Composable
private fun shortcutGuidesFor(
  mobilePlatform: MobileUiPlatform,
): List<ShortcutGuide> = listOf(
  ShortcutGuide(
    title = stringResource(Res.string.mapNearestStationLabel),
    description = stringResource(Res.string.guideNearestStationDescription),
    examples = listOf(
      stringResource(Res.string.guideNearestStationExampleNearest),
      stringResource(Res.string.guideNearestStationExampleClosest),
    ),
    icon = Icons.Filled.Navigation,
  ),
  ShortcutGuide(
    title = stringResource(Res.string.guideNearestWithBikesOrSlots),
    description = stringResource(Res.string.guideNearestWithBikesOrSlotsDescription),
    examples = listOf(
      stringResource(Res.string.guideNearestWithBikesOrSlotsExampleBikes),
      stringResource(Res.string.guideNearestWithBikesOrSlotsExampleSlots),
    ),
    icon = Icons.AutoMirrored.Filled.DirectionsBike,
  ),
  ShortcutGuide(
    title = stringResource(Res.string.guideStationStatus),
    description = stringResource(Res.string.guideStationStatusDescription),
    examples = listOf(
      stringResource(Res.string.guideStationStatusExampleHome),
      stringResource(Res.string.guideStationStatusExampleHomeBikes),
      stringResource(Res.string.guideStationStatusExampleStationSlots),
    ),
    icon = Icons.Filled.Search,
  ),
  ShortcutGuide(
    title = stringResource(Res.string.favorites),
    description = stringResource(Res.string.guideFavoritesDescription),
    examples = listOf(
      stringResource(Res.string.guideFavoritesExampleOpen),
      stringResource(Res.string.guideFavoritesExampleWork),
    ),
    icon = Icons.Filled.Favorite,
  ),
  ShortcutGuide(
    title = stringResource(Res.string.guideRouteToStation),
    description = stringResource(Res.string.guideRouteToStationDescription),
    examples = listOf(
      stringResource(Res.string.guideRouteToStationExamplePlazaEspana),
      stringResource(Res.string.guideRouteToStationExampleWork),
    ),
    icon = Icons.Filled.Directions,
  ),
)

private data class ShortcutGuide(
  val title: String,
  val description: String,
  val examples: List<String>,
  val icon: ImageVector,
)

@Composable
private fun ShortcutGuideCard(
  guide: ShortcutGuide,
) {
  Card(colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface)) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(guide.icon, contentDescription = null, tint = LocalBiziColors.current.red)
        Text(guide.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
      }
      Text(
        guide.description,
        style = MaterialTheme.typography.bodySmall,
        color = LocalBiziColors.current.muted,
      )
      guide.examples.forEach { example ->
        Text(
          "\u2022 $example",
          style = MaterialTheme.typography.bodyMedium,
          color = LocalBiziColors.current.ink,
        )
      }
    }
  }
}
