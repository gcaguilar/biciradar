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
 * - Attaching `Authorization: Bearer <token>` and signed headers.
 * - One automatic retry on HTTP 401 (force-refreshes the token).
 */
interface GeoApi {
    /**
     * Searches for geographic locations matching [query].
     * @param bias Optional user location to bias results.
     */
    suspend fun search(query: String, bias: GeoPoint? = null): List<GeoResult>

    /** Reverse-geocodes a coordinate to a human-readable address. */
    suspend fun reverseGeocode(location: GeoPoint): GeoResult?
}

@Inject
class GeoApiImpl(
    private val httpClient: HttpClient,
    private val json: Json,
    private val tokenManager: TokenManager,
    private val requestSigner: RequestSigner,
) : GeoApi {

    override suspend fun search(query: String, bias: GeoPoint?): List<GeoResult> {
        val requestBody = json.encodeToString(
            GeoSearchRequest(
                query = query,
                latitude = bias?.latitude,
                longitude = bias?.longitude,
            ),
        )
        return executeGeoRequest(
            path = "/geo/search",
            bodyJson = requestBody,
        )
    }

    override suspend fun reverseGeocode(location: GeoPoint): GeoResult? {
        val requestBody = json.encodeToString(
            ReverseGeocodeRequest(
                latitude = location.latitude,
                longitude = location.longitude,
            ),
        )
        val results = executeGeoRequest(
            path = "/geo/reverse",
            bodyJson = requestBody,
        )
        return results.firstOrNull()
    }

    // ------------------------------------------------------------------

    private suspend fun executeGeoRequest(
        path: String,
        bodyJson: String,
        isRetry: Boolean = false,
    ): List<GeoResult> {
        val token = if (isRetry) tokenManager.forceRefresh() else tokenManager.getValidToken()
        val bodyBytes = bodyJson.encodeToByteArray()
        val signed = requestSigner.signedHeaders(method = "POST", path = path, body = bodyBytes)

        val response = runCatching {
            httpClient.post("$BASE_URL$path") {
                contentType(ContentType.Application.Json)
                setBody(bodyJson)
                headers {
                    append("Authorization", "Bearer ${token.token}")
                    append("X-Installation-Id", signed.installationId)
                    append("X-Timestamp", signed.timestamp.toString())
                    append("X-Nonce", signed.nonce)
                    append("X-Signature", signed.signature)
                }
            }
        }.getOrElse { throw GeoError.Network(it) }

        if (response.status == HttpStatusCode.Unauthorized && !isRetry) {
            // One automatic retry after refreshing the token
            return executeGeoRequest(path = path, bodyJson = bodyJson, isRetry = true)
        }
        if (!response.status.isSuccess()) {
            throw GeoError.Server(response.status.value, response.status.description)
        }

        val apiResponse = runCatching { response.body<GeoApiResponse>() }
            .getOrElse { throw GeoError.Unknown(it) }
        return apiResponse.results.map { it.toDomain() }
    }
}
