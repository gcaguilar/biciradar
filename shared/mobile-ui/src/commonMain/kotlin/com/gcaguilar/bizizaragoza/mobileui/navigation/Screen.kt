package com.gcaguilar.bizizaragoza.mobileui.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
  @Serializable
  data object Nearby : Screen()

  @Serializable
  data object Map : Screen()

  @Serializable
  data object Favorites : Screen()

  @Serializable
  data object Trip : Screen()

  @Serializable
  data object Profile : Screen()

  @Serializable
  data class StationDetail(val stationId: String) : Screen()

  @Serializable
  data class TripDestination(val name: String, val lat: Double, val lng: Double) : Screen()
}

object DeepLinks {
  const val BASE_URI = "bizi://"

  fun stationDetail(stationId: String) = "${BASE_URI}station/$stationId"
  fun tripDestination(name: String, lat: Double, lng: Double) = "${BASE_URI}trip?name=$name&lat=$lat&lng=$lng"
}
