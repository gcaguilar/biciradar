package com.gcaguilar.bizizaragoza.core.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// Installation identity
// ---------------------------------------------------------------------------

/**
 * Persisted after a successful `/install/register` call.
 * [publicKeyBase64] is the DER-encoded RSA public key, Base64-encoded.
 */
@Serializable
data class InstallationIdentity(
    val installationId: String,
    val publicKeyBase64: String,
)

// ---------------------------------------------------------------------------
// Access token
// ---------------------------------------------------------------------------

/**
 * Short-lived access token returned by `/token/refresh`.
 * [expiresAtEpochMs] is a client-side wall-clock deadline derived from
 * the server-supplied TTL so we can proactively refresh before expiry.
 */
@Serializable
data class AccessToken(
    val token: String,
    val expiresAtEpochMs: Long,
) {
    fun isExpiredOrExpiringSoon(nowMs: Long = currentTimeMs()): Boolean =
        nowMs >= expiresAtEpochMs - EXPIRY_MARGIN_MS

    companion object {
        private const val EXPIRY_MARGIN_MS = 30_000L // refresh 30 s before hard expiry
    }
}

// ---------------------------------------------------------------------------
// Geo results
// ---------------------------------------------------------------------------

/**
 * A single geographic result returned by `/geo/search` or `/geo/reverse`.
 */
@Serializable
data class GeoResult(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
)

// ---------------------------------------------------------------------------
// Request / response DTOs for the REST API
// ---------------------------------------------------------------------------

@Serializable
internal data class RegisterRequest(
    @SerialName("public_key") val publicKey: String,
    @SerialName("app_version") val appVersion: String,
    @SerialName("platform") val platform: String,
)

@Serializable
internal data class RegisterResponse(
    @SerialName("installation_id") val installationId: String,
)

@Serializable
internal data class TokenRefreshRequest(
    @SerialName("installation_id") val installationId: String,
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("nonce") val nonce: String,
    @SerialName("signature") val signature: String,
)

@Serializable
internal data class TokenRefreshResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Int,       // seconds
)

@Serializable
internal data class GeoSearchRequest(
    @SerialName("query") val query: String,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
)

@Serializable
internal data class ReverseGeocodeRequest(
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
)

@Serializable
internal data class GeoApiResponse(
    @SerialName("results") val results: List<GeoResultDto>,
)

@Serializable
internal data class GeoResultDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("address") val address: String = "",
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
)

internal fun GeoResultDto.toDomain() = GeoResult(
    id = id,
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
)

// ---------------------------------------------------------------------------
// Errors
// ---------------------------------------------------------------------------

sealed class GeoError : Exception() {
    data object NotRegistered : GeoError() {
        override val message: String get() = "Installation not registered"
    }
    data object Unauthorized : GeoError() {
        override val message: String get() = "Unauthorized (invalid token or installation)"
    }
    class Network(val rootCause: Throwable) : GeoError() {
        override val cause: Throwable get() = rootCause
        override val message: String get() = rootCause.message ?: rootCause::class.simpleName ?: "Network error"
    }
    class Server(val statusCode: Int, val errorMessage: String) : GeoError() {
        override val message: String get() = "HTTP $statusCode: $errorMessage"
    }
    class Unknown(val rootCause: Throwable) : GeoError() {
        override val cause: Throwable get() = rootCause
        override val message: String get() = rootCause.message ?: rootCause::class.simpleName ?: "Unknown error"
    }
}

// ---------------------------------------------------------------------------
// Platform clock shim (expect/actual)
// ---------------------------------------------------------------------------

internal expect fun currentTimeMs(): Long
