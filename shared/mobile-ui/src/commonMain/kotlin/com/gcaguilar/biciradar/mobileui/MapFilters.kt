package com.gcaguilar.biciradar.mobileui

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.TripMode
import com.gcaguilar.biciradar.mobile_ui.generated.resources.*
import org.jetbrains.compose.resources.StringResource

internal enum class MapFilter(
  val labelKey: StringResource,
) {
  HAS_BIKES(Res.string.mapFilterHasBikes),
  HAS_SLOTS(Res.string.mapFilterHasSlots),
  BIKES_AND_SLOTS(Res.string.mapFilterBikesAndSlots),
  ONLY_BIKES(Res.string.mapFilterOnlyBikes),
  ONLY_SLOTS(Res.string.mapFilterOnlySlots),
  ONLY_EBIKES(Res.string.mapFilterOnlyEbikes),
  ONLY_REGULAR_BIKES(Res.string.mapFilterOnlyRegularBikes),
  AIR_QUALITY(Res.string.mapFilterAirQuality),
  POLLEN(Res.string.mapFilterPollen),
}

internal val MapFilterSetSaver: Saver<Set<MapFilter>, Any> =
  listSaver(
    save = { filters -> filters.map(MapFilter::name) },
    restore = { names ->
      names
        .mapNotNull { name -> MapFilter.entries.firstOrNull { it.name == name } }
        .toSet()
    },
  )

private val environmentalMapFilters =
  setOf(
    MapFilter.AIR_QUALITY,
    MapFilter.POLLEN,
  )

private val stationAvailabilityMapFilters =
  setOf(
    MapFilter.HAS_BIKES,
    MapFilter.HAS_SLOTS,
    MapFilter.BIKES_AND_SLOTS,
    MapFilter.ONLY_BIKES,
    MapFilter.ONLY_SLOTS,
    MapFilter.ONLY_EBIKES,
    MapFilter.ONLY_REGULAR_BIKES,
  )

internal fun isEnvironmentalMapFilter(filter: MapFilter): Boolean = filter in environmentalMapFilters

internal fun stationAvailabilityFilters(activeFilters: Set<MapFilter>): Set<MapFilter> =
  activeFilters.filterTo(linkedSetOf()) { it in stationAvailabilityMapFilters }

/** Map availability filter that best matches a global [TripMode]. */
internal fun defaultAvailabilityMapFilterForTripMode(mode: TripMode): MapFilter =
  when (mode) {
    TripMode.Pedestrian -> MapFilter.HAS_BIKES
    TripMode.Cyclist -> MapFilter.HAS_SLOTS
  }

/**
 * Orders the visible chips so the mode's primary availability filter comes first
 * and its counterpart last, keeping the granular filters and environmental
 * layers in between and at the end.
 */
internal fun orderedMapFiltersForTripMode(
  mode: TripMode,
  availableFilters: Set<MapFilter>,
): List<MapFilter> {
  val primary = defaultAvailabilityMapFilterForTripMode(mode)
  val counterpart =
    when (primary) {
      MapFilter.HAS_BIKES -> MapFilter.HAS_SLOTS
      MapFilter.HAS_SLOTS -> MapFilter.HAS_BIKES
      else -> null
    }
  val orderedAvailability =
    stationAvailabilityMapFilters.toList().sortedBy { filter ->
      when (filter) {
        primary -> 0
        counterpart -> 2
        else -> 1
      }
    }
  return (orderedAvailability + environmentalMapFilters).filter { it in availableFilters }
}

internal fun toggleMapFilterSelection(
  activeFilters: Set<MapFilter>,
  toggledFilter: MapFilter,
): Set<MapFilter> {
  if (toggledFilter in activeFilters) {
    return activeFilters - toggledFilter
  }

  val filtersToReplace =
    if (isEnvironmentalMapFilter(toggledFilter)) {
      environmentalMapFilters
    } else {
      stationAvailabilityMapFilters
    }

  return activeFilters
    .filterNotTo(linkedSetOf()) { it in filtersToReplace }
    .apply { add(toggledFilter) }
}

internal fun clearEnvironmentalMapFilters(activeFilters: Set<MapFilter>): Set<MapFilter> =
  activeFilters - environmentalMapFilters

internal fun availableMapFilters(stations: List<Station>): Set<MapFilter> {
  if (stations.isEmpty()) return MapFilter.entries.toSet()

  val availableAvailabilityFilters =
    stationAvailabilityMapFilters.filterTo(linkedSetOf()) { filter ->
      stations.any { station ->
        when (filter) {
          MapFilter.HAS_BIKES -> station.bikesAvailable > 0

          MapFilter.HAS_SLOTS -> station.slotsFree > 0

          MapFilter.BIKES_AND_SLOTS -> station.bikesAvailable > 0 && station.slotsFree > 0

          MapFilter.ONLY_BIKES -> station.bikesAvailable > 0 && station.slotsFree == 0

          MapFilter.ONLY_SLOTS -> station.bikesAvailable == 0 && station.slotsFree > 0

          MapFilter.ONLY_EBIKES -> station.ebikesAvailable > 0

          MapFilter.ONLY_REGULAR_BIKES -> station.regularBikesAvailable > 0

          MapFilter.AIR_QUALITY,
          MapFilter.POLLEN,
          -> false
        }
      }
    }

  return linkedSetOf<MapFilter>().apply {
    addAll(availableAvailabilityFilters)
    addAll(environmentalMapFilters)
  }
}

internal fun sanitizeActiveMapFilters(
  activeFilters: Set<MapFilter>,
  availableFilters: Set<MapFilter>,
): Set<MapFilter> = activeFilters.filterTo(linkedSetOf()) { it in availableFilters }

internal fun applyMapFilters(
  stations: List<Station>,
  activeFilters: Set<MapFilter>,
): List<Station> {
  val availabilityFilters = stationAvailabilityFilters(activeFilters)
  if (availabilityFilters.isEmpty()) return stations

  return stations.filter { station ->
    availabilityFilters.any { filter ->
      when (filter) {
        MapFilter.HAS_BIKES -> station.bikesAvailable > 0

        MapFilter.HAS_SLOTS -> station.slotsFree > 0

        MapFilter.BIKES_AND_SLOTS -> station.bikesAvailable > 0 && station.slotsFree > 0

        MapFilter.ONLY_BIKES -> station.bikesAvailable > 0 && station.slotsFree == 0

        MapFilter.ONLY_SLOTS -> station.bikesAvailable == 0 && station.slotsFree > 0

        MapFilter.ONLY_EBIKES -> station.ebikesAvailable > 0

        MapFilter.ONLY_REGULAR_BIKES -> station.regularBikesAvailable > 0

        MapFilter.AIR_QUALITY,
        MapFilter.POLLEN,
        -> true
      }
    }
  }
}
