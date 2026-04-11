package com.gcaguilar.biciradar.core.geo

import com.gcaguilar.biciradar.core.geo.InstallationIdentityRepository.Companion.BASE_URL
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Manages the short-lived access token for datosbizi.com.
 *
 * Guarantees:
 * - Only one refresh request in-flight at a time (Mutex).
 * - Proactively refreshes tokens that are within 30 s of expiry.
 * - Uses the refreshToken from [InstallationIdentityRepository] (rotated on each refresh).
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
    val (identity, _) = identityRepo.getOrRegister()

    val requestBody =
      json.encodeToString(
        TokenRefreshRequest(refreshToken = identity.refreshToken),
      )

    val response =
      try {
        httpClient.post("$BASE_URL/token/refresh") {
          expectSuccess = false
          contentType(ContentType.Application.Json)
          setBody(requestBody)
        }
      } catch (cancelled: CancellationException) {
        throw cancelled
      } catch (ex: Throwable) {
        throw GeoError.Network(ex)
      }

    if (response.status.value == 401 || response.status.value == 403) {
      identityRepo.clear()
      throw GeoError.Unauthorized
    }
    if (!response.status.isSuccess()) {
      throw GeoError.Server(response.status.value, response.status.description)
    }

    val tokenResponse =
      runCatching { response.body<TokenRefreshResponse>() }
        .getOrElse { ex ->
          throw GeoError.Unknown(ex)
        }

    identityRepo.updateRefreshToken(tokenResponse.refreshToken)

    val expiresAtMs = currentTimeMs() + (tokenResponse.expiresIn * 1000L)
    val token = AccessToken(token = tokenResponse.accessToken, expiresAtEpochMs = expiresAtMs)
    currentToken = token
    return token
  }

  companion object {
    internal fun generateNonce(): String =
      kotlin.random.Random
        .nextBytes(16)
        .joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
  }
}
