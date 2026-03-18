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
  data class Trip(val prefilledQuery: String? = null) : Screen()

  @Serializable
  data object Profile : Screen()

  @Serializable
  data object Shortcuts : Screen()

  @Serializable
  data class StationDetail(val stationId: String) : Screen()
}

object DeepLinks {
  const val BASE_URI = "biciradar://"

  fun stationDetail(stationId: String) = "${BASE_URI}station/$stationId"
}
