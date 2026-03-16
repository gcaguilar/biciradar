package com.gcaguilar.bizizaragoza.core

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class PlacePrediction(
  val placeId: String,
  val description: String,
)

data class PlaceDetails(
  val placeId: String,
  val name: String,
  val location: GeoPoint,
)

interface GooglePlacesApi {
  suspend fun autocomplete(query: String, biasLocation: GeoPoint?, apiKey: String): List<PlacePrediction>
  suspend fun placeDetails(placeId: String, apiKey: String): PlaceDetails?
  suspend fun reverseGeocode(location: GeoPoint, apiKey: String): String?
  suspend fun autocompleteWithStatus(query: String, biasLocation: GeoPoint?, apiKey: String): AutocompleteResult
}

data class AutocompleteResult(
  val predictions: List<PlacePrediction>,
  val status: String,
  val error: Throwable? = null,
)

class GooglePlacesApiImpl(
  private val httpClient: HttpClient,
) : GooglePlacesApi {

  override suspend fun autocomplete(
    query: String,
    biasLocation: GeoPoint?,
    apiKey: String,
  ): List<PlacePrediction> = autocompleteWithStatus(query, biasLocation, apiKey).predictions

  override suspend fun autocompleteWithStatus(
    query: String,
    biasLocation: GeoPoint?,
    apiKey: String,
  ): AutocompleteResult {
    val result = runCatching {
      val response = httpClient.get("https://maps.googleapis.com/maps/api/place/autocomplete/json") {
        parameter("input", query)
        parameter("key", apiKey)
        parameter("language", "es")
        parameter("components", "country:es")
        if (biasLocation != null) {
          parameter("location", "${biasLocation.latitude},${biasLocation.longitude}")
          parameter("radius", 50000)
        }
      }.body<AutocompleteResponse>()
      AutocompleteResult(
        predictions = response.predictions.map { prediction ->
          PlacePrediction(placeId = prediction.placeId, description = prediction.description)
        },
        status = response.status,
      )
    }
    return result.getOrElse { throwable ->
      AutocompleteResult(predictions = emptyList(), status = "EXCEPTION", error = throwable)
    }
  }

  override suspend fun placeDetails(placeId: String, apiKey: String): PlaceDetails? = runCatching {
    val response = httpClient.get("https://maps.googleapis.com/maps/api/place/details/json") {
      parameter("place_id", placeId)
      parameter("fields", "place_id,name,geometry")
      parameter("key", apiKey)
      parameter("language", "es")
    }.body<PlaceDetailsResponse>()
    val result = response.result ?: return@runCatching null
    PlaceDetails(
      placeId = result.placeId,
      name = result.name,
      location = GeoPoint(
        latitude = result.geometry.location.lat,
        longitude = result.geometry.location.lng,
      ),
    )
  }.getOrNull()

  override suspend fun reverseGeocode(location: GeoPoint, apiKey: String): String? = runCatching {
    val response = httpClient.get("https://maps.googleapis.com/maps/api/geocode/json") {
      parameter("latlng", "${location.latitude},${location.longitude}")
      parameter("key", apiKey)
      parameter("language", "es")
      parameter("result_type", "street_address|premise|subpremise|route|neighborhood|locality")
    }.body<GeocodeResponse>()
    response.results.firstOrNull()?.formattedAddress
  }.getOrNull()
}

@Serializable
private data class AutocompleteResponse(
  val predictions: List<AutocompletePrediction> = emptyList(),
  val status: String = "",
)

@Serializable
private data class AutocompletePrediction(
  @SerialName("place_id") val placeId: String,
  val description: String,
)

@Serializable
private data class PlaceDetailsResponse(
  val result: PlaceDetailsResult? = null,
  val status: String = "",
)

@Serializable
private data class PlaceDetailsResult(
  @SerialName("place_id") val placeId: String,
  val name: String,
  val geometry: PlaceGeometry,
)

@Serializable
private data class PlaceGeometry(
  val location: PlaceLatLng,
)

@Serializable
private data class PlaceLatLng(
  val lat: Double,
  val lng: Double,
)

@Serializable
private data class GeocodeResponse(
  val results: List<GeocodeResult> = emptyList(),
  val status: String = "",
)

@Serializable
private data class GeocodeResult(
  @SerialName("formatted_address") val formattedAddress: String,
)
