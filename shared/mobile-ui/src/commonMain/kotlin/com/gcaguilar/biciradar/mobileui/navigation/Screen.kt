package com.gcaguilar.biciradar.mobileui.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
  @Serializable
  data object Nearby : Screen()

  @Serializable
  data object Map : Screen()

  @Serializable
  data object Favorites : Screen()

  @Serializable
  data object FavoritesSearch : Screen()

  @Serializable
  data class Trip(val prefilledQuery: String? = null) : Screen()

  @Serializable
  data object TripDestinationSearch : Screen()

  @Serializable
  data class TripMapPicker(val mode: String) : Screen()

  @Serializable
  data object Profile : Screen()

  @Serializable
  data object Shortcuts : Screen()

  @Serializable
  data object SavedPlaceAlerts : Screen()

  @Serializable
  data class StationDetail(val stationId: String) : Screen()

  @Serializable
  data object CitySelection : Screen()
}

object DeepLinks {
  const val BASE_URI = "biciradar://"
  const val HOME_URI = "${BASE_URI}home"
  const val MAP_URI = "${BASE_URI}map"
  const val FAVORITES_URI = "${BASE_URI}favorites"
  const val SAVED_PLACE_ALERTS_URI = "${BASE_URI}alerts"

  fun stationDetail(stationId: String) = "${BASE_URI}station/$stationId"
  fun monitor(stationId: String) = "${BASE_URI}monitor/$stationId"
  fun city(cityId: String) = "${BASE_URI}city/$cityId"
}
