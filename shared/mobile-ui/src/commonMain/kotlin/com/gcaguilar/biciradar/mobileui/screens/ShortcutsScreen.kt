package com.gcaguilar.biciradar.mobileui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.back
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favorites
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideFavoritesDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideFavoritesExampleOpen
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideFavoritesExampleWork
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideNearestStationDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideNearestStationExampleClosest
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideNearestStationExampleNearest
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideNearestWithBikesOrSlots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideNearestWithBikesOrSlotsDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideNearestWithBikesOrSlotsExampleBikes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideNearestWithBikesOrSlotsExampleSlots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideRouteToStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideRouteToStationDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideRouteToStationExamplePlazaEspana
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideRouteToStationExampleWork
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideStationStatus
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideStationStatusDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideStationStatusExampleHome
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideStationStatusExampleHomeBikes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.guideStationStatusExampleStationSlots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.howToInvoke
import com.gcaguilar.biciradar.mobile_ui.generated.resources.latestAnswer
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapNearestStationLabel
import com.gcaguilar.biciradar.mobile_ui.generated.resources.shortcuts
import com.gcaguilar.biciradar.mobile_ui.generated.resources.shortcutsAndroidInvocationHint
import com.gcaguilar.biciradar.mobile_ui.generated.resources.shortcutsAvailableOnIos
import com.gcaguilar.biciradar.mobile_ui.generated.resources.shortcutsAvailableWithAssistant
import com.gcaguilar.biciradar.mobile_ui.generated.resources.shortcutsCurrentRadius
import com.gcaguilar.biciradar.mobile_ui.generated.resources.shortcutsIosInvocationHint
import com.gcaguilar.biciradar.mobile_ui.generated.resources.shortcutsIosSubtitle
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.PlatformBackHandler
import com.gcaguilar.biciradar.mobileui.components.ShortcutGuide
import com.gcaguilar.biciradar.mobileui.components.ShortcutGuideCard
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import org.jetbrains.compose.resources.stringResource

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
    modifier =
      Modifier
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

private fun MobileUiPlatform.assistantDisplayName(): String =
  when (this) {
    MobileUiPlatform.Android -> "Gemini"
    MobileUiPlatform.IOS -> "Siri"
    MobileUiPlatform.Desktop -> "Asistente"
  }

@Composable
private fun shortcutGuidesFor(mobilePlatform: MobileUiPlatform): List<ShortcutGuide> =
  listOf(
    ShortcutGuide(
      title = stringResource(Res.string.mapNearestStationLabel),
      description = stringResource(Res.string.guideNearestStationDescription),
      examples =
        listOf(
          stringResource(Res.string.guideNearestStationExampleNearest),
          stringResource(Res.string.guideNearestStationExampleClosest),
        ),
      icon = Icons.Filled.Navigation,
    ),
    ShortcutGuide(
      title = stringResource(Res.string.guideNearestWithBikesOrSlots),
      description = stringResource(Res.string.guideNearestWithBikesOrSlotsDescription),
      examples =
        listOf(
          stringResource(Res.string.guideNearestWithBikesOrSlotsExampleBikes),
          stringResource(Res.string.guideNearestWithBikesOrSlotsExampleSlots),
        ),
      icon = Icons.AutoMirrored.Filled.DirectionsBike,
    ),
    ShortcutGuide(
      title = stringResource(Res.string.guideStationStatus),
      description = stringResource(Res.string.guideStationStatusDescription),
      examples =
        listOf(
          stringResource(Res.string.guideStationStatusExampleHome),
          stringResource(Res.string.guideStationStatusExampleHomeBikes),
          stringResource(Res.string.guideStationStatusExampleStationSlots),
        ),
      icon = Icons.Filled.Search,
    ),
    ShortcutGuide(
      title = stringResource(Res.string.favorites),
      description = stringResource(Res.string.guideFavoritesDescription),
      examples =
        listOf(
          stringResource(Res.string.guideFavoritesExampleOpen),
          stringResource(Res.string.guideFavoritesExampleWork),
        ),
      icon = Icons.Filled.Favorite,
    ),
    ShortcutGuide(
      title = stringResource(Res.string.guideRouteToStation),
      description = stringResource(Res.string.guideRouteToStationDescription),
      examples =
        listOf(
          stringResource(Res.string.guideRouteToStationExamplePlazaEspana),
          stringResource(Res.string.guideRouteToStationExampleWork),
        ),
      icon = Icons.Filled.Directions,
    ),
  )
