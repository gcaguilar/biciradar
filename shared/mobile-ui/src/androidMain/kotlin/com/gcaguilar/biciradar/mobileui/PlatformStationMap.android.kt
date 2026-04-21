package com.gcaguilar.biciradar.mobileui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.desktopMapCurrentLocation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.desktopMapPickerHint
import com.gcaguilar.biciradar.mobile_ui.generated.resources.desktopMapPlaceholderBody
import com.gcaguilar.biciradar.mobile_ui.generated.resources.desktopMapPlaceholderTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.desktopMapSelectedLocation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapNoStations
import org.jetbrains.compose.resources.stringResource

@Composable
internal actual fun PlatformStationMap(
  modifier: Modifier,
  stations: List<Station>,
  userLocation: GeoPoint?,
  highlightedStationId: String?,
  isMapReady: Boolean,
  onStationSelected: (Station) -> Unit,
  onMapClick: ((GeoPoint) -> Unit)?,
  pinLocation: GeoPoint?,
  recenterRequestToken: Int,
  environmentalOverlay: EnvironmentalOverlayData?,
  stationSnippet: (Station) -> String,
  pinTitle: String,
) {
  val colors = LocalBiziColors.current
  val renderer = LocalAndroidStationMapRenderer.current

  if (isMapReady && renderer != null) {
    renderer.Render(
      modifier = modifier,
      stations = stations,
      userLocation = userLocation,
      highlightedStationId = highlightedStationId,
      onStationSelected = onStationSelected,
      onMapClick = onMapClick,
      pinLocation = pinLocation,
      recenterRequestToken = recenterRequestToken,
      environmentalOverlay = environmentalOverlay,
      stationSnippet = stationSnippet,
      pinTitle = pinTitle,
    )
    return
  }

  Surface(
    modifier = modifier,
    color = colors.surface,
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .background(colors.surface)
          .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = CircleShape,
          color = colors.red.copy(alpha = 0.10f),
        ) {
          Icon(
            imageVector = Icons.Filled.Map,
            contentDescription = null,
            tint = colors.red,
            modifier = Modifier.padding(12.dp),
          )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = stringResource(Res.string.desktopMapPlaceholderTitle),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.ink,
          )
          Text(
            text =
              if (onMapClick == null) {
                stringResource(Res.string.desktopMapPlaceholderBody)
              } else {
                stringResource(Res.string.desktopMapPickerHint)
              },
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
          )
        }
      }

      pinLocation?.let { location ->
        Text(
          text =
            stringResource(
              Res.string.desktopMapSelectedLocation,
              location.latitude,
              location.longitude,
            ),
          style = MaterialTheme.typography.bodySmall,
          color = colors.red,
        )
      }

      userLocation?.let { location ->
        Text(
          text =
            stringResource(
              Res.string.desktopMapCurrentLocation,
              location.latitude,
              location.longitude,
            ),
          style = MaterialTheme.typography.bodySmall,
          color = colors.muted,
        )
      }

      if (stations.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = stringResource(Res.string.mapNoStations),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(
            items = stations,
            key = { it.id },
          ) { station ->
            val highlighted = station.id == highlightedStationId
            Surface(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clickable { onStationSelected(station) },
              shape = MaterialTheme.shapes.large,
              color = if (highlighted) colors.red.copy(alpha = 0.06f) else colors.surface,
              border =
                BorderStroke(
                  width = 1.dp,
                  color = if (highlighted) colors.red.copy(alpha = 0.24f) else colors.panel,
                ),
              tonalElevation = if (highlighted) 2.dp else 0.dp,
            ) {
              Row(
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(
                  modifier = Modifier.weight(1f),
                  verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                  Text(
                    text = station.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = stationSnippet(station),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
                if (highlighted) {
                  Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = colors.red,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
