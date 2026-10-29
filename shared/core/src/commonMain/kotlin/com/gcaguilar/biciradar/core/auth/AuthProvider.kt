package com.gcaguilar.biciradar.core.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Credential state surfaced to the UI. `Anonymous` is the functional default:
 * the app keeps working locally without a backend account.
 */
enum class AuthState {
  Anonymous,
  Authenticated,
}

/**
 * Contract for the platform OIDC (Authorization Code + PKCE) session.
 *
 * Android uses the official `com.auth0.android:auth0` SDK and iOS `Auth0.swift`.
 * The shared code only consumes tokens and exposes the state.
 */
interface AuthProvider {
  val credentialsState: StateFlow<AuthState>

  /** Starts the interactive login. Returns `true` when credentials were obtained. */
  suspend fun login(): Boolean

  suspend fun logout()

  /** Restores any persisted session. Called once at bootstrap. */
  suspend fun bootstrap() = Unit

  /** Returns a valid access token, refreshing it when needed; `null` when anonymous. */
  suspend fun accessToken(): String?
}

/** Anonymous provider used on desktop/wear/tests and when no Auth0 config exists. */
object NoOpAuthProvider : AuthProvider {
  override val credentialsState: StateFlow<AuthState> = MutableStateFlow(AuthState.Anonymous)

  override suspend fun login(): Boolean = false

  override suspend fun logout() = Unit

  override suspend fun accessToken(): String? = null
}

/**
 * Development-only provider for local smoke testing against a backend running
 * with `AUTH_DISABLED=true`. It reports an authenticated session without a
 * token; it must never be wired when Auth0 is configured or in release builds.
 */
object DevAuthProvider : AuthProvider {
  override val credentialsState: StateFlow<AuthState> = MutableStateFlow(AuthState.Authenticated)

  override suspend fun login(): Boolean = true

  override suspend fun logout() = Unit

  override suspend fun accessToken(): String? = null
}

/**
 * Minimal source of the `Authorization: Bearer` value, decoupled from the full
 * [AuthProvider] so tests can substitute a fixed token.
 */
fun interface AuthTokenProvider {
  suspend fun accessToken(): String?
}

fun AuthProvider.asTokenProvider(): AuthTokenProvider = AuthTokenProvider { accessToken() }
