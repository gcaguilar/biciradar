package com.gcaguilar.biciradar.core.backend

import com.gcaguilar.biciradar.core.LocalRoutineRepository
import com.gcaguilar.biciradar.core.Logger
import com.gcaguilar.biciradar.core.Routine
import com.gcaguilar.biciradar.core.RoutineRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.auth.AuthProvider
import com.gcaguilar.biciradar.core.auth.AuthState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Local-first [RoutineRepository].
 *
 * Every mutation is persisted on device first (offline-first, as before) and
 * queued for the backend. On startup/login the queue is flushed and the user's
 * routines are reconciled with `GET /v1/routines`. Conflict resolution is by
 * the app-generated stable `id`: a local edit wins until it is pushed; after a
 * successful first reconcile the server is the source of truth.
 *
 * When the backend is not configured or the user is anonymous, this decorator
 * is a transparent pass-through to [LocalRoutineRepository].
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class SyncingRoutineRepository(
  private val local: LocalRoutineRepository,
  private val apiClient: RoutineBackendApi,
  private val authProvider: AuthProvider,
  private val syncStore: RoutineSyncStore,
  private val settingsRepository: SettingsRepository,
  private val stationsRepository: StationsRepository,
  private val logger: Logger,
  private val scope: CoroutineScope,
) : RoutineRepository {
  override val state: StateFlow<List<Routine>> = local.state

  private val syncMutex = Mutex()

  @kotlin.concurrent.Volatile private var bootstrapped = false

  override suspend fun bootstrap() {
    if (bootstrapped) return
    local.bootstrap()
    bootstrapped = true
    logger.debug(TAG, "bootstrap: syncable=${isSyncable()} backend=${apiClient.isConfigured}")
    scope.launch {
      authProvider.credentialsState.collect { state ->
        logger.debug(TAG, "auth state=$state")
        if (state == AuthState.Authenticated) runCatching { sync() }
      }
    }
    if (isSyncable()) runCatching { sync() }
  }

  override fun routineById(id: String): Routine? = local.routineById(id)

  override suspend fun upsertRoutine(routine: Routine) {
    val normalized = routine.withCity(settingsRepository.currentSelectedCity().id)
    local.upsertRoutine(normalized)
    syncStore.markDirty(normalized.id)
    logger.info(
      TAG,
      "routine ${normalized.id} saved locally, queued for backend (backend=${apiClient.isConfigured} auth=${authProvider.credentialsState.value})",
    )
    scheduleSync()
  }

  override suspend fun deleteRoutine(id: String) {
    local.deleteRoutine(id)
    syncStore.markPendingDelete(id)
    logger.info(TAG, "routine $id deleted locally, queued for backend")
    scheduleSync()
  }

  override suspend fun setEnabled(
    id: String,
    enabled: Boolean,
  ) {
    val routine = local.routineById(id) ?: return
    upsertRoutine(routine.copy(isEnabled = enabled))
  }

  private fun isSyncable(): Boolean =
    apiClient.isConfigured && authProvider.credentialsState.value == AuthState.Authenticated

  private fun scheduleSync() {
    if (!apiClient.isConfigured) {
      logger.debug(TAG, "sync not scheduled: backend is not configured")
      return
    }
    scope.launch { runCatching { sync() } }
  }

  /** Runs one synchronisation pass. Exposed for tests and explicit retries. */
  internal suspend fun syncNow() {
    sync()
  }

  private suspend fun sync() {
    if (!isSyncable()) {
      logger.debug(
        TAG,
        "sync skipped: backend=${apiClient.isConfigured} auth=${authProvider.credentialsState.value}",
      )
      return
    }
    syncMutex.withLock {
      val firstSync = !syncStore.hasSynced()
      logger.debug(TAG, "sync started (first=$firstSync)")
      if (firstSync) local.state.value.forEach { syncStore.markDirty(it.id) }
      pushPendingDeletes()
      pushDirtyRoutines()
      reconcile(firstSync)
      syncStore.markSynced()
      logger.debug(TAG, "sync finished")
    }
  }

  private suspend fun pushPendingDeletes() {
    syncStore.pendingDeletes().forEach { id ->
      val failure = runCatching { apiClient.deleteRoutine(id) }.exceptionOrNull()
      // 404 means another device already deleted it: the intent is satisfied.
      val satisfied = failure == null || (failure as? BackendException)?.status == 404
      if (!satisfied) {
        logger.warn(TAG, "delete routine $id failed", failure)
      }
      if (satisfied) syncStore.clearPendingDelete(id)
    }
  }

  private suspend fun pushDirtyRoutines() {
    syncStore.dirtyIds().forEach { id ->
      val routine = local.routineById(id)
      if (routine == null) {
        syncStore.clearDirty(id)
        return@forEach
      }
      val normalized = routine.withCity(settingsRepository.currentSelectedCity().id)
      val result = runCatching { apiClient.upsertRoutine(normalized) }
      result.exceptionOrNull()?.let { logger.warn(TAG, "push routine $id failed", it) }
      if (result.isSuccess) syncStore.clearDirty(id)
    }
  }

  private suspend fun reconcile(firstSync: Boolean) {
    val serverRoutines =
      runCatching { apiClient.listRoutines() }.getOrElse { error ->
        logger.warn(TAG, "reconcile failed: could not list routines", error)
        return
      }
    val protectedIds = syncStore.dirtyIds() + syncStore.pendingDeletes()
    val serverIds = serverRoutines.mapTo(mutableSetOf()) { it.id }

    if (!firstSync) {
      local.state.value
        .filter { it.id !in serverIds && it.id !in protectedIds }
        .forEach { local.deleteRoutine(it.id) }
    }

    val stationNames = stationNameIndex()
    serverRoutines
      .filter { it.id !in protectedIds }
      .forEach { dto -> local.upsertRoutine(dto.toRoutine(stationNames)) }
  }

  private fun stationNameIndex(): Map<String, String> =
    buildMap {
      stationsRepository.state.value.stations
        .forEach { put(it.id, it.name) }
      local.state.value.forEach { routine ->
        routine.steps.forEach { step ->
          if (step.stationName.isNotBlank()) put(step.stationId, step.stationName)
        }
      }
    }

  private fun Routine.withCity(cityId: String): Routine {
    if (cityId.isBlank() || steps.none { it.cityId.isBlank() }) return this
    return copy(steps = steps.map { step -> if (step.cityId.isBlank()) step.copy(cityId = cityId) else step })
  }
}

/** Log tag for routine local-first sync decisions and failures. */
private const val TAG = "RoutineSync"
