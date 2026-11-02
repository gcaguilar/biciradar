package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.DeleteAccountUseCase
import com.gcaguilar.biciradar.core.NoOpLogger
import com.gcaguilar.biciradar.core.RoutineRepositoryImpl
import com.gcaguilar.biciradar.core.auth.AuthProvider
import com.gcaguilar.biciradar.core.auth.AuthState
import com.gcaguilar.biciradar.core.auth.NoOpAuthProvider
import com.gcaguilar.biciradar.core.backend.AccountBackendApi
import com.gcaguilar.biciradar.core.backend.RoutineSyncStore
import com.gcaguilar.biciradar.testutils.testCoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * [DeleteAccountUseCase] wired with in-memory doubles for the Profile tests.
 * The backend is "not configured", so the use case only clears the local state
 * and the session, which keeps these tests focused on the UI state machine.
 */
internal fun testDeleteAccountUseCase(
  authProvider: AuthProvider = NoOpAuthProvider,
  accountBackendApi: AccountBackendApi = NoOpAccountBackendApi,
): DeleteAccountUseCase =
  DeleteAccountUseCase(
    authProvider = authProvider,
    accountBackendApi = accountBackendApi,
    localRoutineRepository = RoutineRepositoryImpl(scope = testCoroutineScope()),
    routineSyncStore = RoutineSyncStore(database = null),
    logger = NoOpLogger,
  )

internal object NoOpAccountBackendApi : AccountBackendApi {
  override val isConfigured: Boolean = false

  override suspend fun deleteAccount() = Unit
}

internal class RecordingAccountBackendApi(
  override val isConfigured: Boolean = true,
  private val failure: Exception? = null,
) : AccountBackendApi {
  var deleteCalls: Int = 0
    private set

  override suspend fun deleteAccount() {
    deleteCalls += 1
    failure?.let { throw it }
  }
}

internal class TestAuthProvider(
  initialState: AuthState = AuthState.Authenticated,
) : AuthProvider {
  private val state = MutableStateFlow(initialState)

  override val credentialsState: StateFlow<AuthState> = state

  var logoutCalls: Int = 0
    private set

  override suspend fun login(): Boolean {
    state.value = AuthState.Authenticated
    return true
  }

  override suspend fun logout() {
    logoutCalls += 1
    state.value = AuthState.Anonymous
  }

  override suspend fun accessToken(): String? = null
}
