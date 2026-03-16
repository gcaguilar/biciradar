package com.gcaguilar.bizizaragoza.core.geo

import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.geo.InstallationIdentityRepository.Companion.BASE_URL
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Low-level geo API client for datosbizi.com.
 *
 * Handles:
 * - Attaching `Authorization: Bearer <token>` and `X-Installation-Id` headers.
 * - One automatic retry on HTTP 401 (force-refreshes the token).
 */
interface GeoApi {
    /**
     * Searches for geographic locations matching [query].
     */
    suspend fun search(query: String): List<GeoResult>

    /** Reverse-geocodes a coordinate to a human-readable address. */
    suspend fun reverseGeocode(location: GeoPoint): GeoResult?
}

@Inject
class GeoApiImpl(
    private val httpClient: HttpClient,
    private val json: Json,
    private val tokenManager: TokenManager,
    private val identityRepo: InstallationIdentityRepository,
) : GeoApi {

    override suspend fun search(query: String): List<GeoResult> {
        val requestBody = json.encodeToString(
            GeoSearchRequest(query = query),
        )
        return executeSearchRequest(
            path = "/geo/search",
            bodyJson = requestBody,
        )
    }

    override suspend fun reverseGeocode(location: GeoPoint): GeoResult? {
        val requestBody = json.encodeToString(
            ReverseGeocodeRequest(
                lat = location.latitude,
                lon = location.longitude,
            ),
        )
        println("[GeoApi] >>> /geo/reverse body=$requestBody")

        val token = tokenManager.getValidToken()
        val (identity, _) = identityRepo.getOrRegister()

        val response = runCatching {
            httpClient.post("$BASE_URL/geo/reverse") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
                headers {
                    append("Authorization", "Bearer ${token.token}")
                    append("X-Installation-Id", identity.installationId)
                }
            }
        }.getOrElse { ex ->
            println("[GeoApi] NETWORK ERROR /geo/reverse: ${ex::class.simpleName} — ${ex.message}")
            throw GeoError.Network(ex)
        }

        println("[GeoApi] <<< /geo/reverse status=${response.status.value}")

        if (response.status == HttpStatusCode.Unauthorized) {
            println("[GeoApi] 401 on /geo/reverse — retrying with fresh token")
            return reverseGeocodeWithToken(requestBody, forceRefresh = true)
        }
        if (!response.status.isSuccess()) {
            println("[GeoApi] SERVER ERROR /geo/reverse: ${response.status.value} ${response.status.description}")
            throw GeoError.Server(response.status.value, response.status.description)
        }

        val reverseResponse = runCatching { response.body<ReverseGeocodeResponse>() }
            .getOrElse { ex ->
                println("[GeoApi] PARSE ERROR /geo/reverse: ${ex::class.simpleName} — ${ex.message}")
                throw GeoError.Unknown(ex)
            }
        println("[GeoApi] /geo/reverse OK address=${reverseResponse.address}")
        return reverseResponse.toDomain()
    }

    // ------------------------------------------------------------------

    private suspend fun executeSearchRequest(
        path: String,
        bodyJson: String,
        isRetry: Boolean = false,
    ): List<GeoResult> {
        println("[GeoApi] >>> $path retry=$isRetry body=$bodyJson")

        val token = if (isRetry) tokenManager.forceRefresh() else tokenManager.getValidToken()
        println("[GeoApi] token acquired (expires=${token.expiresAtEpochMs})")

        val (identity, _) = identityRepo.getOrRegister()
        println("[GeoApi] using installationId=${identity.installationId}")

        val response = runCatching {
            httpClient.post("$BASE_URL$path") {
                contentType(ContentType.Application.Json)
                setBody(bodyJson)
                headers {
                    append("Authorization", "Bearer ${token.token}")
                    append("X-Installation-Id", identity.installationId)
                }
            }
        }.getOrElse { ex ->
            println("[GeoApi] NETWORK ERROR $path: ${ex::class.simpleName} — ${ex.message}")
            throw GeoError.Network(ex)
        }

        println("[GeoApi] <<< $path status=${response.status.value}")

        if (response.status == HttpStatusCode.Unauthorized && !isRetry) {
            println("[GeoApi] 401 on $path — retrying with fresh token")
            return executeSearchRequest(path = path, bodyJson = bodyJson, isRetry = true)
        }
        if (!response.status.isSuccess()) {
            println("[GeoApi] SERVER ERROR $path: ${response.status.value} ${response.status.description}")
            throw GeoError.Server(response.status.value, response.status.description)
        }

        val apiResponse = runCatching { response.body<GeoApiResponse>() }
            .getOrElse { ex ->
                println("[GeoApi] PARSE ERROR $path: ${ex::class.simpleName} — ${ex.message}")
                throw GeoError.Unknown(ex)
            }
        println("[GeoApi] $path OK — ${apiResponse.results.size} results")
        return apiResponse.results.map { it.toDomain() }
    }

    private suspend fun reverseGeocodeWithToken(bodyJson: String, forceRefresh: Boolean): GeoResult? {
        val token = if (forceRefresh) tokenManager.forceRefresh() else tokenManager.getValidToken()
        val (identity, _) = identityRepo.getOrRegister()

        val response = runCatching {
            httpClient.post("$BASE_URL/geo/reverse") {
                contentType(ContentType.Application.Json)
                setBody(bodyJson)
                headers {
                    append("Authorization", "Bearer ${token.token}")
                    append("X-Installation-Id", identity.installationId)
                }
            }
        }.getOrElse { ex ->
            println("[GeoApi] NETWORK ERROR /geo/reverse (retry): ${ex::class.simpleName} — ${ex.message}")
            throw GeoError.Network(ex)
        }

        if (!response.status.isSuccess()) {
            println("[GeoApi] SERVER ERROR /geo/reverse (retry): ${response.status.value} ${response.status.description}")
            throw GeoError.Server(response.status.value, response.status.description)
        }

        val reverseResponse = runCatching { response.body<ReverseGeocodeResponse>() }
            .getOrElse { ex ->
                println("[GeoApi] PARSE ERROR /geo/reverse (retry): ${ex::class.simpleName} — ${ex.message}")
                throw GeoError.Unknown(ex)
            }
        return reverseResponse.toDomain()
    }
}
