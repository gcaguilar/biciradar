package com.gcaguilar.biciradar.core.local

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import kotlin.math.*

data class StationEntity(
  val id: String,
  val name: String,
  val address: String,
  val latitude: Double,
  val longitude: Double,
  val bikesAvailable: Int,
  val slotsFree: Int,
  val ebikesAvailable: Int,
  val regularBikesAvailable: Int,
  val updatedAt: Long,
)

fun StationEntity.toDomain(origin: GeoPoint): Station {
  val location = GeoPoint(latitude, longitude)
  return Station(
    id = id,
    name = name,
    address = address,
    location = location,
    bikesAvailable = bikesAvailable,
    slotsFree = slotsFree,
    distanceMeters = distanceBetween(origin, location),
    sourceLabel = "BiciRadar",
    ebikesAvailable = ebikesAvailable,
    regularBikesAvailable = regularBikesAvailable,
  )
}

private fun distanceBetween(
  origin: GeoPoint,
  destination: GeoPoint,
): Int {
  val earthRadius = 6371000.0
  val lat1Rad = origin.latitude * PI / 180.0
  val lat2Rad = destination.latitude * PI / 180.0
  val deltaLat = (destination.latitude - origin.latitude) * PI / 180.0
  val deltaLon = (destination.longitude - origin.longitude) * PI / 180.0

  val a =
    sin(deltaLat / 2) * sin(deltaLat / 2) +
      cos(lat1Rad) * cos(lat2Rad) *
      sin(deltaLon / 2) * sin(deltaLon / 2)
  val c = 2 * atan2(sqrt(a), sqrt(1 - a))

  return (earthRadius * c).toInt()
}
