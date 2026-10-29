package com.gcaguilar.biciradar.core.backend

import com.gcaguilar.biciradar.core.local.BiciRadarDatabase

/**
 * Persists the routine synchronisation bookkeeping: whether the account has
 * already reconciled once, which routines changed locally and which deletions
 * are still pending. Backed by SQLDelight, so retries survive process death.
 *
 * When no database is available (desktop/wear/tests) all operations are no-ops
 * and the syncing repository degrades to local-only behaviour.
 */
class RoutineSyncStore(
  private val database: BiciRadarDatabase?,
) {
  fun hasSynced(): Boolean =
    database
      ?.biciradarQueries
      ?.getRoutineSyncState()
      ?.executeAsOneOrNull()
      ?.has_synced
      ?.let { it != 0L } ?: false

  fun markSynced() {
    database?.biciradarQueries?.upsertRoutineSyncState(hasSynced = 1L)
  }

  fun markDirty(routineId: String) {
    val queries = database?.biciradarQueries ?: return
    queries.markRoutineDirty(routineId)
    queries.clearRoutinePendingDelete(routineId)
  }

  fun clearDirty(routineId: String) {
    database?.biciradarQueries?.clearRoutineDirty(routineId)
  }

  fun dirtyIds(): Set<String> =
    database
      ?.biciradarQueries
      ?.getDirtyRoutineIds()
      ?.executeAsList()
      ?.toSet() ?: emptySet()

  fun markPendingDelete(routineId: String) {
    val queries = database?.biciradarQueries ?: return
    queries.markRoutinePendingDelete(routineId)
    queries.clearRoutineDirty(routineId)
  }

  fun clearPendingDelete(routineId: String) {
    database?.biciradarQueries?.clearRoutinePendingDelete(routineId)
  }

  fun pendingDeletes(): Set<String> =
    database
      ?.biciradarQueries
      ?.getPendingRoutineDeletes()
      ?.executeAsList()
      ?.toSet() ?: emptySet()

  fun clear() {
    val queries = database?.biciradarQueries ?: return
    queries.deleteAllRoutineDirty()
    queries.deleteAllRoutinePendingDeletes()
  }
}
