package com.gcaguilar.biciradar.core.geo

import com.gcaguilar.biciradar.core.AutocompleteResult
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.GooglePlacesApi
import com.gcaguilar.biciradar.core.PlaceDetails
import com.gcaguilar.biciradar.core.PlacePrediction
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GeoFallbackUseCaseTest {
  @Test
  fun `geo search falls back to Google Places when datosbizi search fails`() =
    runTest {
      val useCase =
        GeoSearchUseCase(
          geoApi =
            object : GeoApi {
              override suspend fun search(query: String): List<GeoResult> =
                throw GeoError.Server(500, "Failed to search locations")

              override suspend fun reverseGeocode(location: GeoPoint): GeoResult? = null
            },
          googlePlacesApi =
            FakeGooglePlacesApi(
              predictions =
                listOf(
                  PlacePrediction(placeId = "place-1", description = "Plaza España, Zaragoza, España"),
                ),
              detailsById =
                mapOf(
                  "place-1" to
                    PlaceDetails(
                      placeId = "place-1",
                      name = "Plaza España",
                      location = GeoPoint(41.6492, -0.8833),
                    ),
                ),
            ),
          googleMapsApiKey = "fake-key",
        )

      val results = useCase.execute("plaza españa")

      assertEquals(1, results.size)
      assertEquals("place-1", results.first().id)
      assertEquals("Plaza España", results.first().name)
      assertEquals("Plaza España, Zaragoza, España", results.first().address)
    }

  @Test
  fun `geo search prioritizes Zaragoza-normalized variants for plaza espana`() =
    runTest {
      val attemptedQueries = mutableListOf<String>()
      val useCase =
        GeoSearchUseCase(
          geoApi =
            object : GeoApi {
              override suspend fun search(query: String): List<GeoResult> {
                attemptedQueries += query
                return if (query == "Plaza de España, Zaragoza") {
                  listOf(
                    GeoResult(
                      id = "plaza-espana-centro",
                      name = "Plaza de España",
                      address = "Plaza de España, Centro, Zaragoza, Aragón, 50001, España",
                      latitude = 41.6522,
                      longitude = -0.8810,
                    ),
                  )
                } else {
                  emptyList()
                }
              }

              override suspend fun reverseGeocode(location: GeoPoint): GeoResult? = null
            },
          googlePlacesApi = FakeGooglePlacesApi(),
          googleMapsApiKey = null,
        )

      val results = useCase.execute("Plaza España, Zaragoza, Zaragoza")

      assertEquals("Plaza de España, Zaragoza", attemptedQueries.first())
      assertTrue(attemptedQueries.none { it.contains("Zaragoza, Zaragoza") })
      assertEquals(1, results.size)
      assertEquals("Plaza de España", results.first().name)
    }

  @Test
  fun `reverse geocode falls back to Google geocoding when datosbizi reverse fails`() =
    runTest {
      val location = GeoPoint(41.6492, -0.8833)
      val useCase =
        ReverseGeocodeUseCase(
          geoApi =
            object : GeoApi {
              override suspend fun search(query: String): List<GeoResult> = emptyList()

              override suspend fun reverseGeocode(location: GeoPoint): GeoResult? =
                throw GeoError.Server(500, "Failed to reverse geocode")
            },
          googlePlacesApi =
            FakeGooglePlacesApi(
              reverseGeocodeAddress = "Plaza España, Casco Histórico, Zaragoza, España",
            ),
          googleMapsApiKey = "fake-key",
        )

      val result = useCase.execute(location)

      assertEquals("Plaza España", result?.name)
      assertEquals("Plaza España, Casco Histórico, Zaragoza, España", result?.address)
      assertEquals(location.latitude, result?.latitude)
      assertEquals(location.longitude, result?.longitude)
    }

  @Test
  fun `reverse geocode returns null when datosbizi fails and no Google key is configured`() =
    runTest {
      val useCase =
        ReverseGeocodeUseCase(
          geoApi =
            object : GeoApi {
              override suspend fun search(query: String): List<GeoResult> = emptyList()

              override suspend fun reverseGeocode(location: GeoPoint): GeoResult? =
                throw GeoError.Server(500, "Failed to reverse geocode")
            },
          googlePlacesApi = FakeGooglePlacesApi(),
          googleMapsApiKey = null,
        )

      assertNull(useCase.execute(GeoPoint(41.6492, -0.8833)))
    }

  private class FakeGooglePlacesApi(
    private val predictions: List<PlacePrediction> = emptyList(),
    private val detailsById: Map<String, PlaceDetails> = emptyMap(),
    private val reverseGeocodeAddress: String? = null,
  ) : GooglePlacesApi {
    override suspend fun autocomplete(
      query: String,
      biasLocation: GeoPoint?,
      apiKey: String,
    ): List<PlacePrediction> = predictions

    override suspend fun placeDetails(
      placeId: String,
      apiKey: String,
    ): PlaceDetails? = detailsById[placeId]

    override suspend fun reverseGeocode(
      location: GeoPoint,
      apiKey: String,
    ): String? = reverseGeocodeAddress

    override suspend fun autocompleteWithStatus(
      query: String,
      biasLocation: GeoPoint?,
      apiKey: String,
    ) = AutocompleteResult(
      predictions = predictions,
      status = "OK",
    )
  }
}
