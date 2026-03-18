package com.gcaguilar.biciradar.core.geo

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
    val refreshToken: String,
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
    val platform: String,
    val appVersion: String,
    val osVersion: String,
    val publicKey: String,
)

@Serializable
internal data class RegisterResponse(
    val installId: String,
    val refreshToken: String,
)

@Serializable
internal data class TokenRefreshRequest(
    val refreshToken: String,
)

@Serializable
internal data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,       // seconds
)

@Serializable
internal data class GeoSearchRequest(
    val query: String,
    val limit: Int? = null,
)

@Serializable
internal data class ReverseGeocodeRequest(
    val lat: Double,
    val lon: Double,
    val lng: Double = lon,
)

@Serializable
internal data class GeoApiResponse(
    val results: List<GeoResultDto>,
)

@Serializable
internal data class GeoResultDto(
    val id: String,
    val name: String,
    val address: String = "",
    val lat: Double,
    val lon: Double,
)

@Serializable
internal data class ReverseGeocodeResponse(
    val address: String,
    val city: String,
    val district: String? = null,
    val lat: Double,
    val lon: Double,
)

internal fun GeoResultDto.toDomain() = GeoResult(
    id = id,
    name = name,
    address = address,
    latitude = lat,
    longitude = lon,
)

internal fun ReverseGeocodeResponse.toDomain() = GeoResult(
    id = "$lat,$lon",
    name = district ?: city,
    address = address,
    latitude = lat,
    longitude = lon,
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
