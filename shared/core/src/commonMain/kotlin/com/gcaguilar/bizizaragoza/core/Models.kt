package com.gcaguilar.bizizaragoza.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeoPoint(
  val latitude: Double,
  val longitude: Double,
)

@Serializable
data class Station(
  val id: String,
  val name: String,
  val address: String,
  val location: GeoPoint,
  val bikesAvailable: Int,
  val slotsFree: Int,
  val distanceMeters: Int,
  val sourceLabel: String = "Bizi Zaragoza",
)

@Serializable
data class StationsState(
  val stations: List<Station> = emptyList(),
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val userLocation: GeoPoint? = null,
)

@Serializable
data class GeminiPromptRequest(
  val prompt: String,
  val selectedStationId: String? = null,
)

@Serializable
data class GeminiPromptResponse(
  val answer: String,
  val recommendedStationId: String? = null,
)

@Serializable
sealed interface AssistantAction {
  @Serializable
  @SerialName("nearest_station")
  data object NearestStation : AssistantAction

  @Serializable
  @SerialName("nearest_station_with_bikes")
  data object NearestStationWithBikes : AssistantAction

  @Serializable
  @SerialName("nearest_station_with_slots")
  data object NearestStationWithSlots : AssistantAction

  @Serializable
  @SerialName("station_status")
  data class StationStatus(val stationId: String) : AssistantAction

  @Serializable
  @SerialName("favorite_stations")
  data object FavoriteStations : AssistantAction

  @Serializable
  @SerialName("route_to_station")
  data class RouteToStation(val stationId: String) : AssistantAction
}

@Serializable
data class AssistantResolution(
  val spokenResponse: String,
  val highlightedStationId: String? = null,
)
