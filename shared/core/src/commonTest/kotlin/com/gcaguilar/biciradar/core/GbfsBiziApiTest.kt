package com.gcaguilar.biciradar.core

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Covers both GBFS wire versions the app consumes: 2.x (feeds nested per
 * language, `num_bikes_available`) and 3.0 (`data.feeds`,
 * `num_vehicles_available`).
 */
class GbfsBiziApiTest {
  private val discoveryUrl = City.MADRID.gbfsDiscoveryUrl
  private val infoUrl = "https://example.test/info.json"
  private val statusUrl = "https://example.test/status.json"
  private val vehicleTypesUrl = "https://example.test/vehicle_types.json"
  private val origin = GeoPoint(40.4168, -3.7038)

  private fun api(
    discovery: String,
    info: String,
    status: String,
    vehicleTypes: String = """{"data":{"vehicle_types":[]}}""",
  ): GbfsBiziApi =
    GbfsBiziApi(
      httpClient =
        HttpClient(
          MockEngine { request ->
            val body =
              when (request.url.toString()) {
                discoveryUrl -> discovery
                infoUrl -> info
                statusUrl -> status
                vehicleTypesUrl -> vehicleTypes
                else -> error("Unexpected URL ${request.url}")
              }
            respond(
              content = body,
              status = HttpStatusCode.OK,
              headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
          },
        ) {
          install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
          }
        },
      configuration = AppConfiguration(city = City.MADRID),
    )

  @Test
  fun `reads a GBFS 2 feed`() =
    runTest {
      val api =
        api(
          discovery =
            """
            {"data":{"en":{"feeds":[
              {"name":"station_information","url":"$infoUrl"},
              {"name":"station_status","url":"$statusUrl"}
            ]}}}
            """.trimIndent(),
          info =
            """
            {"data":{"stations":[
              {"station_id":"1406","name":"Plaza","lat":40.4168,"lon":-3.7038}
            ]}}
            """.trimIndent(),
          status =
            """
            {"data":{"stations":[
              {"station_id":"1406","num_bikes_available":4,"num_docks_available":6,
               "vehicle_types_available":[
                 {"vehicle_type_id":"cb:vehicle:ebike","count":1},
                 {"vehicle_type_id":"cb:vehicle:bike","count":3}
               ]}
            ]}}
            """.trimIndent(),
        )

      val station = api.fetchStations(origin).single()

      assertEquals("1406", station.id)
      assertEquals("Plaza", station.name)
      assertEquals(4, station.bikesAvailable)
      assertEquals(6, station.slotsFree)
      // No vehicle_types feed: the citybik id convention still classifies.
      assertEquals(1, station.ebikesAvailable)
      assertEquals(3, station.regularBikesAvailable)
    }

  @Test
  fun `reads a GBFS 3 feed with num_vehicles_available`() =
    runTest {
      val api =
        api(
          discovery =
            """
            {"data":{"feeds":[
              {"name":"station_information","url":"$infoUrl"},
              {"name":"station_status","url":"$statusUrl"},
              {"name":"vehicle_types","url":"$vehicleTypesUrl"}
            ]}}
            """.trimIndent(),
          info =
            """
            {"data":{"stations":[
              {"station_id":"1406","name":[{"text":"Plaza","language":"es"},{"text":"Plaça","language":"ca"}],
               "lat":40.4168,"lon":-3.7038,"capacity":20}
            ]}}
            """.trimIndent(),
          status =
            """
            {"data":{"stations":[
              {"station_id":"1406","num_vehicles_available":4,"num_docks_available":6,
               "vehicle_types_available":[{"vehicle_type_id":"EFIT","count":6}]}
            ]}}
            """.trimIndent(),
          vehicleTypes =
            """
            {"data":{"vehicle_types":[
              {"vehicle_type_id":"EFIT","propulsion_type":"electric","form_factor":"bicycle"}
            ]}}
            """.trimIndent(),
        )

      val station = api.fetchStations(origin).single()

      assertEquals(4, station.bikesAvailable)
      assertEquals(6, station.slotsFree)
      // EFIT is electric even though the id does not contain "ebike"; the type
      // count includes overflow bikes, so it is clamped to the station total.
      assertEquals(4, station.ebikesAvailable)
      assertEquals(0, station.regularBikesAvailable)
      // GBFS 3.0 sends the name as a language list; Spanish wins.
      assertEquals("Plaza", station.name)
    }
}
