package com.gcaguilar.biciradar.mobileui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.citySelectionSearchNoResults
import com.gcaguilar.biciradar.mobile_ui.generated.resources.citySelectionSearchPlaceholder
import com.gcaguilar.biciradar.mobile_ui.generated.resources.citySelectionSubtitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.citySelectionTitle
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.normalizedForSearch
import org.jetbrains.compose.resources.stringResource

/**
 * Pantalla de selección de ciudad.
 * Permite al usuario seleccionar su ciudad para mostrar estaciones relevantes.
 */
@Composable
internal fun CitySelectionScreen(onCitySelected: (City) -> Unit) {
  val colors = LocalBiziColors.current
  var searchQuery by remember { mutableStateOf("") }
  val sortedCities = remember { City.entries.sortedBy { it.displayName } }
  val normalizedQuery = remember(searchQuery) { searchQuery.trim().normalizedForSearch() }
  val filteredCities =
    remember(normalizedQuery, sortedCities) {
      if (normalizedQuery.isBlank()) {
        sortedCities
      } else {
        sortedCities.filter { city ->
          city.displayName.normalizedForSearch().contains(normalizedQuery)
        }
      }
    }
  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .background(colors.background)
        .windowInsetsPadding(WindowInsets.statusBars)
        .padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = stringResource(Res.string.citySelectionTitle),
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold,
      color = colors.ink,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = stringResource(Res.string.citySelectionSubtitle),
      style = MaterialTheme.typography.bodyMedium,
      color = colors.muted,
    )
    Spacer(modifier = Modifier.height(20.dp))
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      placeholder = { Text(stringResource(Res.string.citySelectionSearchPlaceholder)) },
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedContainerColor = colors.surface,
          unfocusedContainerColor = colors.surface,
        ),
    )
    Spacer(modifier = Modifier.height(12.dp))
    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (filteredCities.isEmpty()) {
        item {
          Text(
            text = stringResource(Res.string.citySelectionSearchNoResults),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
            modifier = Modifier.padding(vertical = 8.dp),
          )
        }
      }
      items(filteredCities.size) { index ->
        val city = filteredCities[index]
        Card(
          modifier =
            Modifier
              .fillMaxWidth()
              .clickable { onCitySelected(city) },
          colors = CardDefaults.cardColors(containerColor = colors.surface),
          border = BorderStroke(1.dp, colors.panel),
        ) {
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = city.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.ink,
              )
              if (city.supportsEbikes) {
                Text(
                  text = "Bicis eléctricas disponibles",
                  style = MaterialTheme.typography.bodySmall,
                  color = colors.muted,
                )
              }
            }
            Icon(
              imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
              contentDescription = null,
              tint = colors.red,
            )
          }
        }
      }
    }
    Spacer(modifier = Modifier.height(24.dp))
  }
}
