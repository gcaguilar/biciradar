package com.gcaguilar.biciradar.core.local

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.geo.distanceBetween

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
