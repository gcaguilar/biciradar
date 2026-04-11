package com.gcaguilar.biciradar.mobileui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.gcaguilar.biciradar.mobile_ui.generated.resources.*
import com.gcaguilar.biciradar.mobileui.experience.ChangelogCatalogEntry
import com.gcaguilar.biciradar.mobileui.experience.ChangelogVersionSection
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChangelogHistoryScreen(
  mobilePlatform: MobileUiPlatform,
  sections: List<ChangelogVersionSection>,
  highlightedVersion: String?,
  onBack: () -> Unit,
) {
  val colors = LocalBiziColors.current
  PlatformBackHandler(enabled = true, onBack = onBack)
  Surface(
    modifier =
      Modifier
        .fillMaxSize()
        .zIndex(8f),
    color = pageBackgroundColor(mobilePlatform),
  ) {
    Scaffold(
      containerColor = androidx.compose.ui.graphics.Color.Transparent,
      topBar = {
        TopAppBar(
          title = {
            Text(
              stringResource(Res.string.changelogWhatsNew),
              fontWeight = FontWeight.Bold,
            )
          },
          navigationIcon = {
            IconButton(onClick = onBack) {
              Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.back),
              )
            }
          },
          actions = {
            TextButton(onClick = onBack) {
              Text(stringResource(Res.string.gotIt))
            }
          },
        )
      },
    ) { innerPadding ->
      LazyColumn(
        modifier =
          Modifier
            .fillMaxSize()
            .responsivePageWidth(),
        contentPadding =
          PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = innerPadding.calculateTopPadding() + 8.dp,
            bottom = innerPadding.calculateBottomPadding() + 24.dp,
          ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        items(
          items = sections,
          key = { it.versionName },
        ) { section ->
          val isHighlighted = section.versionName == highlightedVersion
          Card(
            colors =
              CardDefaults.cardColors(
                containerColor =
                  if (isHighlighted) {
                    colors.red.copy(alpha = 0.10f)
                  } else {
                    colors.surface
                  },
              ),
            border =
              BorderStroke(
                width = 1.dp,
                color = if (isHighlighted) colors.red.copy(alpha = 0.24f) else colors.panel,
              ),
          ) {
            androidx.compose.foundation.layout.Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
              Text(
                text = "v${section.versionName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isHighlighted) colors.red else colors.ink,
              )
              section.entries.forEach { entry ->
                ChangelogEntryItem(entry)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ChangelogEntryItem(entry: ChangelogCatalogEntry) {
  val colors = LocalBiziColors.current
  androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(
      stringResource(entry.titleKey),
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.SemiBold,
    )
    Text(
      stringResource(entry.descriptionKey),
      style = MaterialTheme.typography.bodySmall,
      color = colors.muted,
    )
  }
}
