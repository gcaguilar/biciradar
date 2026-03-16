package com.gcaguilar.bizizaragoza.core.geo

import com.gcaguilar.bizizaragoza.core.geo.InstallationIdentityRepository.Companion.BASE_URL
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

/**
 * Manages the short-lived access token for datosbizi.com.
 *
 * Guarantees:
 * - Only one refresh request in-flight at a time (Mutex).
 * - Proactively refreshes tokens that are within 30 s of expiry.
 * - Delegates signing of the token-refresh request to [RequestSigner].
 */
@Inject
class TokenManager(
    private val httpClient: HttpClient,
    private val json: Json,
    private val identityRepo: InstallationIdentityRepository,
) {
    private val mutex = Mutex()

    @kotlin.concurrent.Volatile
    private var currentToken: AccessToken? = null

    /**
     * Returns a valid [AccessToken].
     * Refreshes if the current one is absent or near-expiry.
     */
    suspend fun getValidToken(): AccessToken {
        // Fast path: current token is valid (unsynchronised read is safe — worst case we refresh once extra)
        currentToken?.takeIf { !it.isExpiredOrExpiringSoon() }?.let { return it }

        return mutex.withLock {
            // Re-check inside the lock to avoid stampede
            currentToken?.takeIf { !it.isExpiredOrExpiringSoon() }?.let { return@withLock it }
            refresh()
        }
    }

    /** Forces a token refresh regardless of expiry. Call after receiving HTTP 401. */
    suspend fun forceRefresh(): AccessToken = mutex.withLock { refresh() }

    // ------------------------------------------------------------------

    private suspend fun refresh(): AccessToken {
        val (identity, keyPair) = identityRepo.getOrRegister()

        val timestamp = currentTimeMs() / 1000L   // unix seconds
        val nonce = generateNonce()

        // Signature payload for token refresh: installationId + "\n" + timestamp + "\n" + nonce
        val signingPayload = "${identity.installationId}\n$timestamp\n$nonce"
            .encodeToByteArray()
        val signature = keyPair.sign(signingPayload)

        val requestBody = json.encodeToString(
            TokenRefreshRequest(
                installationId = identity.installationId,
                timestamp = timestamp,
                nonce = nonce,
                signature = signature,
            ),
        )

        val response = runCatching {
            httpClient.post("$BASE_URL/token/refresh") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
        }.getOrElse { throw GeoError.Network(it) }

        if (response.status.value == 401 || response.status.value == 403) {
            // Identity may be invalid; clear and re-register on next call
            identityRepo.clear()
            throw GeoError.Unauthorized
        }
        if (!response.status.isSuccess()) {
            throw GeoError.Server(response.status.value, response.status.description)
        }

        val tokenResponse = runCatching { response.body<TokenRefreshResponse>() }
            .getOrElse { throw GeoError.Unknown(it) }

        val expiresAtMs = currentTimeMs() + (tokenResponse.expiresIn * 1000L)
        val token = AccessToken(token = tokenResponse.accessToken, expiresAtEpochMs = expiresAtMs)
        currentToken = token
        return token
    }

    companion object {
        internal fun generateNonce(): String =
            Random.nextBytes(16).joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
    }
}
