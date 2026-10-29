package com.gcaguilar.biciradar.core.platform

import com.gcaguilar.biciradar.core.auth.AuthProvider
import com.gcaguilar.biciradar.core.auth.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Swift-implemented OIDC gateway backed by the official `Auth0.swift` SDK.
 * Methods return the access token (or `null`) to avoid boxing Kotlin types in
 * Swift.
 */
interface Auth0Gateway {
  /** Returns the access token if a valid session is stored, or `null`. */
  suspend fun restoreToken(): String?

  /** Runs the interactive login and returns the access token, or `null` on failure. */
  suspend fun login(): String?

  suspend fun logout()

  /** Refreshes if needed and returns a valid access token, or `null`. */
  suspend fun accessToken(): String?
}

/**
 * [AuthProvider] for iOS. Owns the observable state and delegates the OIDC
 * ceremony to [gateway] (Auth0.swift), so the shared code stays platform-neutral.
 */
class Auth0AuthProvider(
  private val gateway: Auth0Gateway,
) : AuthProvider {
  private val mutableState = MutableStateFlow(AuthState.Anonymous)

  override val credentialsState: StateFlow<AuthState> = mutableState

  override suspend fun bootstrap() {
    mutableState.value = if (gateway.restoreToken() != null) AuthState.Authenticated else AuthState.Anonymous
  }

  override suspend fun login(): Boolean {
    val token = gateway.login()
    mutableState.value = if (token != null) AuthState.Authenticated else AuthState.Anonymous
    return token != null
  }

  override suspend fun logout() {
    gateway.logout()
    mutableState.value = AuthState.Anonymous
  }

  override suspend fun accessToken(): String? = gateway.accessToken()
}
