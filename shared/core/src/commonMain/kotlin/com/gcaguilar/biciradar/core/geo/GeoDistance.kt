package com.gcaguilar.biciradar.core.geo

import com.gcaguilar.biciradar.core.GeoPoint
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Computes the great-circle distance between two [GeoPoint] coordinates using the Haversine formula.
 *
 * Returns the distance in **whole meters** (rounded down). This is the canonical implementation
 * used across the codebase — all other distance calculations should delegate here to avoid
 * duplication.
 *
 * Earth radius: 6,371,000 meters (WGS84 mean radius).
 */
fun distanceBetween(
  origin: GeoPoint,
  destination: GeoPoint,
): Int {
  val earthRadiusMeters = 6_371_000.0
  val latitudeDelta = (destination.latitude - origin.latitude).toRadians()
  val longitudeDelta = (destination.longitude - origin.longitude).toRadians()
  val a =
    sin(latitudeDelta / 2).pow(2) +
      cos(origin.latitude.toRadians()) * cos(destination.latitude.toRadians()) *
      sin(longitudeDelta / 2).pow(2)
  val c = 2 * asin(sqrt(a))
  return (earthRadiusMeters * c).roundToInt()
}

private fun Double.toRadians(): Double = this * PI / 180.0
