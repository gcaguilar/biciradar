package com.gcaguilar.biciradar

import android.app.Activity
import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.storage.CredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.provider.WebAuthProvider
import com.gcaguilar.biciradar.core.AppConfiguration
import com.gcaguilar.biciradar.core.auth.AuthProvider
import com.gcaguilar.biciradar.core.auth.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Auth0 OIDC (Authorization Code + PKCE) using the official `com.auth0.android`
 * SDK. Credentials are stored by [CredentialsManager]; tokens are refreshed
 * transparently when requested. The SDK pulls Google Play Services through
 * `androidx.credentials`, so the F-Droid flavor excludes those transitives: the
 * browser (custom tabs) flow does not need them.
 */
class AndroidAuth0Provider(
  context: Context,
  private val configuration: AppConfiguration,
  private val activityProvider: () -> Activity?,
) : AuthProvider {
  private val account =
    Auth0.getInstance(configuration.auth0ClientId, configuration.auth0Domain)

  private val credentialsManager =
    CredentialsManager(
      AuthenticationAPIClient(account),
      SharedPreferencesStorage(context, "biciradar-auth"),
    )

  private val mutableState = MutableStateFlow(AuthState.Anonymous)
  override val credentialsState: StateFlow<AuthState> = mutableState

  override suspend fun bootstrap() {
    mutableState.value =
      if (credentialsManager.hasValidCredentials(MIN_TTL_SECONDS)) {
        AuthState.Authenticated
      } else {
        AuthState.Anonymous
      }
  }

  override suspend fun login(): Boolean {
    val activity = activityProvider() ?: return false
    return runCatching {
      val credentials =
        WebAuthProvider
          .login(account)
          .withScheme(configuration.auth0Scheme)
          .withAudience(configuration.auth0Audience)
          .withScope(SCOPE)
          .await(activity)
      credentialsManager.saveCredentials(credentials)
      mutableState.value = AuthState.Authenticated
      true
    }.getOrDefault(false)
  }

  override suspend fun logout() {
    val activity = activityProvider()
    runCatching {
      if (activity != null) {
        WebAuthProvider
          .logout(account)
          .withScheme(configuration.auth0Scheme)
          .await(activity)
      }
    }
    credentialsManager.clearCredentials()
    mutableState.value = AuthState.Anonymous
  }

  override suspend fun accessToken(): String? {
    if (!credentialsManager.hasValidCredentials(MIN_TTL_SECONDS)) {
      mutableState.value = AuthState.Anonymous
      return null
    }
    return runCatching { credentialsManager.awaitCredentials().accessToken }.getOrNull()
  }

  private companion object {
    const val MIN_TTL_SECONDS = 60L
    const val SCOPE = "openid profile email offline_access"
  }
}
