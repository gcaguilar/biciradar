package com.gcaguilar.biciradar.core.local

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station

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

private fun distanceBetween(origin: GeoPoint, destination: GeoPoint): Int {
  val earthRadius = 6371000.0
  val lat1Rad = Math.toRadians(origin.latitude)
  val lat2Rad = Math.toRadians(destination.latitude)
  val deltaLat = Math.toRadians(destination.latitude - origin.latitude)
  val deltaLon = Math.toRadians(destination.longitude - origin.longitude)

  val a = kotlin.math.sin(deltaLat / 2) * kotlin.math.sin(deltaLat / 2) +
    kotlin.math.cos(lat1Rad) * kotlin.math.cos(lat2Rad) *
    kotlin.math.sin(deltaLon / 2) * kotlin.math.sin(deltaLon / 2)
  val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

  return (earthRadius * c).toInt()
}
