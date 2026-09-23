package com.gcaguilar.biciradar.core.backend

import com.gcaguilar.biciradar.core.Logger
import com.gcaguilar.biciradar.core.Routine
import com.gcaguilar.biciradar.core.auth.AuthTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

/** Raised for transport, protocol or error-status failures against `/v1`. */
class BackendException(
  message: String,
  cause: Throwable? = null,
  /** HTTP status when the failure came from an error response. */
  val status: Int? = null,
) : Exception(message, cause)

/**
 * Thin client for the BiciRadar `/v1` API. Reuses the shared [HttpClient] and
 * [Json]; the bearer token comes from [tokenProvider]. Every call is a no-op
 * guard when the backend is not configured.
 */
class BiciRadarApiClient(
  private val httpClient: HttpClient,
  private val json: Json,
  private val config: BackendConfig,
  private val tokenProvider: AuthTokenProvider,
  private val logger: Logger,
) : RoutineBackendApi {
  override val isConfigured: Boolean get() = config.isConfigured

  override suspend fun listRoutines(): List<RoutineDto> {
    requireConfigured()
    val body = execute(HttpMethod.Get, "/v1/routines", null)
    return json.decodeFromString(RoutinesResponse.serializer(), body).routines
  }

  override suspend fun upsertRoutine(routine: Routine): RoutineDto {
    requireConfigured()
    val body =
      execute(HttpMethod.Post, "/v1/routines", json.encodeToString(RoutineDto.serializer(), routine.toRoutineDto()))
    return json.decodeFromString(RoutineDto.serializer(), body)
  }

  override suspend fun deleteRoutine(id: String) {
    requireConfigured()
    execute(HttpMethod.Delete, "/v1/routines/${encodePath(id)}", null)
  }

  suspend fun registerDevice(request: RegisterDeviceRequest) {
    requireConfigured()
    execute(
      HttpMethod.Post,
      "/v1/devices",
      json.encodeToString(RegisterDeviceRequest.serializer(), request),
    )
  }

  suspend fun putSubscriptions(subscriptions: List<SubscriptionDto>): List<SubscriptionDto> {
    requireConfigured()
    val body =
      execute(
        HttpMethod.Put,
        "/v1/subscriptions",
        json.encodeToString(ReplaceSubscriptionsRequest.serializer(), ReplaceSubscriptionsRequest(subscriptions)),
      )
    return json.decodeFromString(SubscriptionsResponse.serializer(), body).subscriptions
  }

  suspend fun startMonitoringWindow(request: StartMonitoringWindowRequest): String {
    requireConfigured()
    val body =
      execute(
        HttpMethod.Post,
        "/v1/monitoring-windows",
        json.encodeToString(StartMonitoringWindowRequest.serializer(), request),
      )
    return json.decodeFromString(MonitoringWindowResponse.serializer(), body).id
  }

  suspend fun cancelMonitoringWindow(id: String) {
    requireConfigured()
    execute(HttpMethod.Delete, "/v1/monitoring-windows/${encodePath(id)}", null)
  }

  private fun requireConfigured() {
    if (!config.isConfigured) {
      logger.warn(TAG, "Backend is not configured: /v1 calls are disabled")
      throw BackendException("Backend is not configured")
    }
  }

  private suspend fun execute(
    method: HttpMethod,
    path: String,
    jsonBody: String?,
  ): String {
    val token = tokenProvider.accessToken()?.takeIf { it.isNotBlank() }
    val url = config.url(path)
    logger.debug(TAG, "-> ${method.value} $url (bearer=${if (token != null) "yes" else "no"})")
    jsonBody?.let { logger.debug(TAG, "   body=$it") }
    return try {
      val response =
        httpClient.request(url) {
          this.method = method
          if (jsonBody != null) {
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
          }
          token?.let { bearerAuth(it) }
        }
      val body = response.bodyAsText()
      logger.debug(TAG, "<- ${response.status.value} ${method.value} $path")
      body
    } catch (cancelled: CancellationException) {
      throw cancelled
    } catch (responseError: ResponseException) {
      val message = errorMessage(responseError)
      logger.warn(TAG, "<- ${responseError.response.status.value} ${method.value} $path: $message", responseError)
      throw BackendException(message, responseError, responseError.response.status.value)
    } catch (error: Throwable) {
      logger.warn(TAG, "<- no response for ${method.value} $path: ${error::class.simpleName}: ${error.message}", error)
      throw BackendException(error.message ?: "Backend request failed", error)
    }
  }

  private suspend fun errorMessage(exception: ResponseException): String {
    val body = runCatching { exception.response.bodyAsText() }.getOrNull()
    val decoded =
      body
        ?.takeIf { it.isNotBlank() }
        ?.let { runCatching { json.decodeFromString(ErrorResponse.serializer(), it) }.getOrNull() }
    return decoded?.message ?: "Backend request failed (${exception.response.status.value})"
  }

  private fun encodePath(value: String): String =
    buildString {
      value.forEach { char ->
        if (char.isLetterOrDigit() || char in "-._~") {
          append(char)
        } else {
          append('%')
          append(
            char.code
              .toString(16)
              .uppercase()
              .padStart(2, '0'),
          )
        }
      }
    }
}

/** Log tag for every `/v1` request made from the app. */
private const val TAG = "BiciRadarApi"
