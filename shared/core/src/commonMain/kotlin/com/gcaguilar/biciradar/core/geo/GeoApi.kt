package com.gcaguilar.biciradar.core.geo

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.geo.InstallationIdentityRepository.Companion.BASE_URL
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
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
    private val requestSigner: RequestSigner,
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
        val signedHeaders = requestSigner.signedHeaders(
            method = "POST",
            path = "/geo/reverse",
            body = requestBody.encodeToByteArray(),
        )
        println("[GeoApi] using installationId=${signedHeaders.installationId}")

        val response = try {
            httpClient.post("$BASE_URL/geo/reverse") {
                expectSuccess = false
                contentType(ContentType.Application.Json)
                setBody(requestBody)
                headers {
                    append("Authorization", "Bearer ${token.token}")
                    append("X-Installation-Id", signedHeaders.installationId)
                    append("X-Timestamp", signedHeaders.timestamp.toString())
                    append("X-Nonce", signedHeaders.nonce)
                    append("X-Signature", signedHeaders.signature)
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (ex: Throwable) {
            println("[GeoApi] NETWORK ERROR /geo/reverse: ${ex::class.simpleName} — ${ex.message}")
            throw GeoError.Network(ex)
        }

        println("[GeoApi] <<< /geo/reverse status=${response.status.value}")

        if (response.status == HttpStatusCode.Unauthorized) {
            println("[GeoApi] 401 on /geo/reverse — retrying with fresh token")
            return reverseGeocodeWithToken(requestBody, forceRefresh = true)
        }
        if (!response.status.isSuccess()) {
            val errorBody = response.safeBodyAsText()
            println("[GeoApi] SERVER ERROR /geo/reverse: ${response.status.value} ${response.status.description} body=$errorBody")
            throw GeoError.Server(response.status.value, errorBody ?: response.status.description)
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

        val signedHeaders = requestSigner.signedHeaders(
            method = "POST",
            path = path,
            body = bodyJson.encodeToByteArray(),
        )
        println("[GeoApi] using installationId=${signedHeaders.installationId}")

        val response = try {
            httpClient.post("$BASE_URL$path") {
                expectSuccess = false
                contentType(ContentType.Application.Json)
                setBody(bodyJson)
                headers {
                    append("Authorization", "Bearer ${token.token}")
                    append("X-Installation-Id", signedHeaders.installationId)
                    append("X-Timestamp", signedHeaders.timestamp.toString())
                    append("X-Nonce", signedHeaders.nonce)
                    append("X-Signature", signedHeaders.signature)
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (ex: Throwable) {
            println("[GeoApi] NETWORK ERROR $path: ${ex::class.simpleName} — ${ex.message}")
            throw GeoError.Network(ex)
        }

        println("[GeoApi] <<< $path status=${response.status.value}")

        if (response.status == HttpStatusCode.Unauthorized && !isRetry) {
            println("[GeoApi] 401 on $path — retrying with fresh token")
            return executeSearchRequest(path = path, bodyJson = bodyJson, isRetry = true)
        }
        if (!response.status.isSuccess()) {
            val errorBody = response.safeBodyAsText()
            println("[GeoApi] SERVER ERROR $path: ${response.status.value} ${response.status.description} body=$errorBody")
            throw GeoError.Server(response.status.value, errorBody ?: response.status.description)
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
        val signedHeaders = requestSigner.signedHeaders(
            method = "POST",
            path = "/geo/reverse",
            body = bodyJson.encodeToByteArray(),
        )

        val response = try {
            httpClient.post("$BASE_URL/geo/reverse") {
                expectSuccess = false
                contentType(ContentType.Application.Json)
                setBody(bodyJson)
                headers {
                    append("Authorization", "Bearer ${token.token}")
                    append("X-Installation-Id", signedHeaders.installationId)
                    append("X-Timestamp", signedHeaders.timestamp.toString())
                    append("X-Nonce", signedHeaders.nonce)
                    append("X-Signature", signedHeaders.signature)
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (ex: Throwable) {
            println("[GeoApi] NETWORK ERROR /geo/reverse (retry): ${ex::class.simpleName} — ${ex.message}")
            throw GeoError.Network(ex)
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.safeBodyAsText()
            println("[GeoApi] SERVER ERROR /geo/reverse (retry): ${response.status.value} ${response.status.description} body=$errorBody")
            throw GeoError.Server(response.status.value, errorBody ?: response.status.description)
        }

        val reverseResponse = runCatching { response.body<ReverseGeocodeResponse>() }
            .getOrElse { ex ->
                println("[GeoApi] PARSE ERROR /geo/reverse (retry): ${ex::class.simpleName} — ${ex.message}")
                throw GeoError.Unknown(ex)
            }
        return reverseResponse.toDomain()
    }
}

private suspend fun HttpResponse.safeBodyAsText(): String? = runCatching {
    bodyAsText().takeIf { it.isNotBlank() }
}.getOrNull()
