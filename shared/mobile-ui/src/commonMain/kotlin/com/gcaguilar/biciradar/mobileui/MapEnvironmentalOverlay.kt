package com.gcaguilar.biciradar.mobileui

import com.gcaguilar.biciradar.core.Station

internal enum class MapEnvironmentalLayer {
  AirQuality,
  Pollen,
}

internal data class MapEnvironmentalZoneSnapshot(
  val centerLatitude: Double,
  val centerLongitude: Double,
  val zoneLabel: String,
  val airQualityScore: Int? = null,
  val pollenScore: Int? = null,
)

internal fun activeEnvironmentalLayerForFilters(activeFilters: Set<MapFilter>): MapEnvironmentalLayer? =
  when {
    MapFilter.AIR_QUALITY in activeFilters -> MapEnvironmentalLayer.AirQuality
    MapFilter.POLLEN in activeFilters -> MapEnvironmentalLayer.Pollen
    else -> null
  }

internal fun buildMapEnvironmentalZoneSnapshots(stations: List<Station>): List<MapEnvironmentalZoneSnapshot> {
  if (stations.isEmpty()) return emptyList()
  val averageLatitude = stations.map { it.location.latitude }.average()
  val averageLongitude = stations.map { it.location.longitude }.average()
  val grouped =
    stations.groupBy { station ->
      val north = station.location.latitude >= averageLatitude
      val east = station.location.longitude >= averageLongitude
      when {
        north && east -> "Noreste"
        north && !east -> "Noroeste"
        !north && east -> "Sureste"
        else -> "Suroeste"
      }
    }
  return grouped.entries.sortedBy { it.key }.map { (zone, zoneStations) ->
    MapEnvironmentalZoneSnapshot(
      centerLatitude = zoneStations.map { it.location.latitude }.average(),
      centerLongitude = zoneStations.map { it.location.longitude }.average(),
      zoneLabel = zone,
    )
  }
}
