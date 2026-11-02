package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.auth.AuthProvider
import com.gcaguilar.biciradar.core.auth.AuthState
import com.gcaguilar.biciradar.core.backend.AccountBackendApi
import com.gcaguilar.biciradar.core.backend.BackendException
import com.gcaguilar.biciradar.core.backend.RoutineSyncStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeleteAccountUseCaseTest {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

  @Test
  fun `deletes the backend account, clears local routines and closes the session`() =
    runTest {
      val authProvider = TestAccountAuthProvider()
      val backend = TestAccountBackendApi()
      val routines = RoutineRepositoryImpl(scope)
      routines.upsertRoutine(testRoutine("routine-1"))
      val useCase = useCase(authProvider, backend, routines)

      val result = useCase.execute()

      assertEquals(DeleteAccountResult.Deleted, result)
      assertEquals(1, backend.deleteCalls)
      assertEquals(1, authProvider.logoutCalls)
      assertEquals(AuthState.Anonymous, authProvider.credentialsState.value)
      assertTrue(routines.state.value.isEmpty())
    }

  @Test
  fun `keeps the session and local data when the backend rejects the deletion`() =
    runTest {
      val authProvider = TestAccountAuthProvider()
      val backend = TestAccountBackendApi(failure = BackendException("boom", status = 500))
      val routines = RoutineRepositoryImpl(scope)
      routines.upsertRoutine(testRoutine("routine-1"))
      val useCase = useCase(authProvider, backend, routines)

      val result = useCase.execute()

      assertEquals(DeleteAccountResult.Failed, result)
      assertEquals(1, backend.deleteCalls)
      assertEquals(0, authProvider.logoutCalls)
      assertEquals(AuthState.Authenticated, authProvider.credentialsState.value)
      assertEquals(listOf("routine-1"), routines.state.value.map { it.id })
    }

  @Test
  fun `does nothing when there is no signed-in account`() =
    runTest {
      val authProvider = TestAccountAuthProvider(initialState = AuthState.Anonymous)
      val backend = TestAccountBackendApi()
      val routines = RoutineRepositoryImpl(scope)
      routines.upsertRoutine(testRoutine("routine-1"))
      val useCase = useCase(authProvider, backend, routines)

      val result = useCase.execute()

      assertEquals(DeleteAccountResult.NotAuthenticated, result)
      assertEquals(0, backend.deleteCalls)
      assertEquals(0, authProvider.logoutCalls)
      assertEquals(listOf("routine-1"), routines.state.value.map { it.id })
    }

  private fun useCase(
    authProvider: AuthProvider,
    backend: AccountBackendApi,
    routines: LocalRoutineRepository,
  ) = DeleteAccountUseCase(
    authProvider = authProvider,
    accountBackendApi = backend,
    localRoutineRepository = routines,
    routineSyncStore = RoutineSyncStore(database = null),
    logger = NoOpLogger,
  )

  private fun testRoutine(id: String): Routine =
    Routine(
      id = id,
      name = "Rutina $id",
      daysOfWeek = setOf(RoutineDay.MONDAY),
      hour = 8,
      minute = 0,
      isEnabled = true,
      steps =
        listOf(
          RoutineStep(
            id = "$id-step",
            stationId = "station-1",
            stationName = "Plaza Espana",
            cityId = "zaragoza",
            durationSeconds = 600,
            kind = SurfaceMonitoringKind.Bikes,
          ),
        ),
    )
}

private class TestAccountAuthProvider(
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

private class TestAccountBackendApi(
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
