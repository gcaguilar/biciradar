package com.gcaguilar.biciradar.mobileui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.appearance
import com.gcaguilar.biciradar.mobile_ui.generated.resources.appearanceDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.citySelectionSearchNoResults
import com.gcaguilar.biciradar.mobile_ui.generated.resources.citySelectionSearchPlaceholder
import com.gcaguilar.biciradar.mobile_ui.generated.resources.citySelectionSubtitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.close
import com.gcaguilar.biciradar.mobile_ui.generated.resources.dark
import com.gcaguilar.biciradar.mobile_ui.generated.resources.dataSourceDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.dataSourceDetailsAction
import com.gcaguilar.biciradar.mobile_ui.generated.resources.dataSourceEnvironmentalDetail
import com.gcaguilar.biciradar.mobile_ui.generated.resources.dataSourceGbfsDetail
import com.gcaguilar.biciradar.mobile_ui.generated.resources.dataSourceTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.feedbackAndSuggestions
import com.gcaguilar.biciradar.mobile_ui.generated.resources.feedbackDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.garminConnectAction
import com.gcaguilar.biciradar.mobile_ui.generated.resources.garminConnectDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.garminConnectTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.iPhoneRouteApp
import com.gcaguilar.biciradar.mobile_ui.generated.resources.iPhoneRouteAppDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.iPhoneRouteAppFallback
import com.gcaguilar.biciradar.mobile_ui.generated.resources.light
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyStationRadius
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyStationRadiusDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.openFeedbackForm
import com.gcaguilar.biciradar.mobile_ui.generated.resources.openPrivacyPolicy
import com.gcaguilar.biciradar.mobile_ui.generated.resources.openShortcutsGuide
import com.gcaguilar.biciradar.mobile_ui.generated.resources.privacyAndData
import com.gcaguilar.biciradar.mobile_ui.generated.resources.privacyDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.profileSetupCardAction
import com.gcaguilar.biciradar.mobile_ui.generated.resources.profileSetupCardBody
import com.gcaguilar.biciradar.mobile_ui.generated.resources.profileSetupCardTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.profileSubtitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.rateApp
import com.gcaguilar.biciradar.mobile_ui.generated.resources.selectedCity
import com.gcaguilar.biciradar.mobile_ui.generated.resources.settings
import com.gcaguilar.biciradar.mobile_ui.generated.resources.shortcuts
import com.gcaguilar.biciradar.mobile_ui.generated.resources.shortcutsReviewCommands
import com.gcaguilar.biciradar.mobile_ui.generated.resources.system
import com.gcaguilar.biciradar.mobile_ui.generated.resources.viewWhatsNew
import com.gcaguilar.biciradar.mobileui.FeedbackDialog
import com.gcaguilar.biciradar.mobileui.BiziSpacing
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.components.SearchRadiusSelector
import com.gcaguilar.biciradar.mobileui.components.buttons.RadiusSelectionButton
import com.gcaguilar.biciradar.mobileui.components.cards.BiziSectionCard
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import com.gcaguilar.biciradar.mobileui.viewmodel.ProfileUiState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ProfileScreen(
  state: ProfileUiState,
  mobilePlatform: MobileUiPlatform,
  paddingValues: PaddingValues,
  onSearchRadiusSelected: (Int) -> Unit,
  onPreferredMapAppSelected: (PreferredMapApp) -> Unit,
  onThemePreferenceSelected: (ThemePreference) -> Unit,
  onCitySelected: (City) -> Unit,
  onCitySearchQueryChange: (String) -> Unit,
  onClearCitySearchQuery: () -> Unit,
  onShowChangelog: () -> Unit,
  onOpenOnboarding: () -> Unit,
  onOpenShortcuts: () -> Unit,
  onOpenFeedback: () -> Unit,
  onOpenGarminPairing: () -> Unit,
  onRateApp: () -> Unit,
) {
  var showFeedbackDialog by remember { mutableStateOf(false) }
  var showDataSourcesDialog by remember { mutableStateOf(false) }
  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(pageBackgroundColor(mobilePlatform)),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.responsivePageWidth(),
      contentPadding = PaddingValues(BiziSpacing.screenPadding),
      verticalArrangement = Arrangement.spacedBy(BiziSpacing.screenPadding),
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
      if (state.showProfileSetupCard) {
        item {
          BiziSectionCard(
            title = stringResource(Res.string.profileSetupCardTitle),
            description = stringResource(Res.string.profileSetupCardBody),
            actionLabel = stringResource(Res.string.profileSetupCardAction),
            onAction = onOpenOnboarding,
            contentSpacing = BiziSpacing.medium,
          )
        }
      }
      item {
        BiziSectionCard(
          title = stringResource(Res.string.shortcuts),
          description = stringResource(Res.string.shortcutsReviewCommands, mobilePlatform.profileAssistantName()),
          actionLabel = stringResource(Res.string.openShortcutsGuide),
          onAction = onOpenShortcuts,
        )
      }
      item {
        BiziSectionCard(
          title = stringResource(Res.string.viewWhatsNew),
          actionLabel = stringResource(Res.string.viewWhatsNew),
          onAction = onShowChangelog,
        )
      }
      item {
        BiziSectionCard(
          title = stringResource(Res.string.nearbyStationRadius),
          description = stringResource(Res.string.nearbyStationRadiusDescription),
          contentSpacing = BiziSpacing.xxLarge,
        ) {
            SearchRadiusSelector(
              selectedRadiusMeters = state.searchRadiusMeters,
              onSearchRadiusSelected = onSearchRadiusSelected,
            )
        }
      }
      item {
        BiziSectionCard(
          title = stringResource(Res.string.selectedCity),
          description = stringResource(Res.string.citySelectionSubtitle),
          contentSpacing = BiziSpacing.xxLarge,
        ) {
            CitySelector(
              selectedCity = state.selectedCity,
              searchQuery = state.citySearchQuery,
              filteredCities = state.filteredCities,
              onCitySelected = onCitySelected,
              onSearchQueryChange = onCitySearchQueryChange,
              onClearSearchQuery = onClearCitySearchQuery,
            )
        }
      }
      item {
        BiziSectionCard(
          title = stringResource(Res.string.appearance),
          description = stringResource(Res.string.appearanceDescription),
          contentSpacing = BiziSpacing.xxLarge,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(BiziSpacing.xLarge)) {
              RadiusSelectionButton(
                modifier = Modifier.weight(1f),
                selected = state.themePreference == ThemePreference.System,
                label = stringResource(Res.string.system),
                onClick = { onThemePreferenceSelected(ThemePreference.System) },
              )
              RadiusSelectionButton(
                modifier = Modifier.weight(1f),
                selected = state.themePreference == ThemePreference.Light,
                label = stringResource(Res.string.light),
                onClick = { onThemePreferenceSelected(ThemePreference.Light) },
              )
              RadiusSelectionButton(
                modifier = Modifier.weight(1f),
                selected = state.themePreference == ThemePreference.Dark,
                label = stringResource(Res.string.dark),
                onClick = { onThemePreferenceSelected(ThemePreference.Dark) },
              )
            }
        }
      }
      if (mobilePlatform == MobileUiPlatform.IOS && state.canSelectGoogleMapsInIos) {
        item {
          BiziSectionCard(
            title = stringResource(Res.string.iPhoneRouteApp),
            description = stringResource(Res.string.iPhoneRouteAppDescription),
            contentSpacing = BiziSpacing.xxLarge,
          ) {
              Row(horizontalArrangement = Arrangement.spacedBy(BiziSpacing.xLarge)) {
                RadiusSelectionButton(
                  modifier = Modifier.weight(1f),
                  selected = state.preferredMapApp == PreferredMapApp.AppleMaps,
                  label = "Apple Maps",
                  onClick = { onPreferredMapAppSelected(PreferredMapApp.AppleMaps) },
                )
                RadiusSelectionButton(
                  modifier = Modifier.weight(1f),
                  selected = state.preferredMapApp == PreferredMapApp.GoogleMaps,
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
      if (state.showGarminSection && mobilePlatform == MobileUiPlatform.IOS) {
        item {
          BiziSectionCard(
            title = stringResource(Res.string.garminConnectTitle),
            description = stringResource(Res.string.garminConnectDescription),
            actionLabel = stringResource(Res.string.garminConnectAction),
            onAction = onOpenGarminPairing,
          )
        }
      }
      item {
        BiziSectionCard(
          title = stringResource(Res.string.rateApp),
          actionLabel = stringResource(Res.string.rateApp),
          onAction = onRateApp,
        )
      }
      item {
        BiziSectionCard(
          title = stringResource(Res.string.feedbackAndSuggestions),
          description = stringResource(Res.string.feedbackDescription),
          actionLabel = stringResource(Res.string.openFeedbackForm),
          onAction = { showFeedbackDialog = true },
        )
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
        BiziSectionCard(
          title = stringResource(Res.string.privacyAndData),
          description = stringResource(Res.string.privacyDescription),
          actionLabel = stringResource(Res.string.openPrivacyPolicy),
          onAction = { uriHandler.openUri("https://gcaguilar.github.io/biciradar-privacy-policy/") },
        )
        Spacer(modifier = Modifier.height(BiziSpacing.xLarge))
        BiziSectionCard(
          title = stringResource(Res.string.dataSourceTitle),
          description = stringResource(Res.string.dataSourceDescription),
          onClick = { showDataSourcesDialog = true },
        ) {
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

private fun MobileUiPlatform.profileAssistantName(): String =
  when (this) {
    MobileUiPlatform.Android -> "Gemini"
    MobileUiPlatform.IOS -> "Siri"
    MobileUiPlatform.Desktop -> "Asistente"
  }

@Composable
private fun CitySelector(
  selectedCity: City,
  searchQuery: String,
  filteredCities: List<City>,
  onCitySelected: (City) -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onClearSearchQuery: () -> Unit,
) {
  val colors = LocalBiziColors.current
  var expanded by remember { mutableStateOf(false) }

  Box {
    androidx.compose.material3.OutlinedButton(
      onClick = {
        onClearSearchQuery()
        expanded = true
      },
      modifier = Modifier.fillMaxWidth(),
      border = androidx.compose.foundation.BorderStroke(1.dp, colors.panel),
      colors =
        androidx.compose.material3.ButtonDefaults
          .outlinedButtonColors(containerColor = colors.surface),
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
        androidx.compose.material3.Icon(
          imageVector = Icons.Filled.Navigation,
          contentDescription = null,
          tint = colors.red,
        )
      }
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = {
        onClearSearchQuery()
        expanded = false
      },
      modifier = Modifier.background(colors.surface),
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        singleLine = true,
        placeholder = { Text(stringResource(Res.string.citySelectionSearchPlaceholder)) },
      )
      filteredCities.forEach { city ->
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
              androidx.compose.material3.Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = colors.red,
              )
            }
          },
        )
      }
      if (filteredCities.isEmpty()) {
        DropdownMenuItem(
          text = {
            Text(
              text = stringResource(Res.string.citySelectionSearchNoResults),
              color = colors.muted,
            )
          },
          onClick = {},
          enabled = false,
        )
      }
    }
  }
}
