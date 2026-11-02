package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.auth.AuthProvider
import com.gcaguilar.biciradar.core.auth.AuthState
import com.gcaguilar.biciradar.core.backend.AccountBackendApi
import com.gcaguilar.biciradar.core.backend.RoutineSyncStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException

/** Outcome of an account deletion requested from the app. */
enum class DeleteAccountResult {
  /** The account and its data were deleted and the session was closed. */
  Deleted,

  /** There was no signed-in account to delete. */
  NotAuthenticated,

  /** The backend rejected the request or it could not be reached. Nothing was deleted. */
  Failed,
}

/**
 * Deletes the signed-in account and everything tied to it.
 *
 * 1. Calls `DELETE /v1/me`, which removes the user's routines, subscriptions,
 *    devices and monitoring windows on the server.
 * 2. Clears the local routine copy and its sync bookkeeping, so deleted data is
 *    not uploaded again on the next login.
 * 3. Closes the OIDC session and drops the stored credentials.
 *
 * The OIDC identity itself must be removed by the backend (Auth0 Management
 * API) as part of `DELETE /v1/me`; a client cannot delete it. If the call fails
 * the session is kept untouched so the user can retry; nothing is deleted
 * locally in that case.
 */
@Inject
class DeleteAccountUseCase(
  private val authProvider: AuthProvider,
  private val accountBackendApi: AccountBackendApi,
  private val localRoutineRepository: LocalRoutineRepository,
  private val routineSyncStore: RoutineSyncStore,
  private val logger: Logger,
) {
  suspend fun execute(): DeleteAccountResult {
    if (authProvider.credentialsState.value != AuthState.Authenticated) {
      return DeleteAccountResult.NotAuthenticated
    }

    if (accountBackendApi.isConfigured) {
      try {
        accountBackendApi.deleteAccount()
      } catch (cancelled: CancellationException) {
        throw cancelled
      } catch (error: Exception) {
        logger.warn(TAG, "account deletion failed; keeping the session so the user can retry", error)
        return DeleteAccountResult.Failed
      }
    }

    clearLocalAccountData()
    authProvider.logout()
    logger.info(TAG, "account deleted and session closed")
    return DeleteAccountResult.Deleted
  }

  private suspend fun clearLocalAccountData() {
    // The DB rows are the source of truth; make sure they are loaded even if the
    // user reaches Profile before the startup bootstrap finished.
    runCatching { localRoutineRepository.bootstrap() }
      .onFailure { logger.warn(TAG, "could not load local routines before deletion", it) }
    localRoutineRepository.state.value.forEach { routine ->
      runCatching { localRoutineRepository.deleteRoutine(routine.id) }
        .onFailure { logger.warn(TAG, "could not delete local routine ${routine.id}", it) }
    }
    routineSyncStore.reset()
  }

  private companion object {
    const val TAG = "DeleteAccount"
  }
}
