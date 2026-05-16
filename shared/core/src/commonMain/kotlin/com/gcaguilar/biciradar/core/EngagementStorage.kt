package com.gcaguilar.biciradar.core

import kotlinx.coroutines.flow.StateFlow

/**
 * Low-level persistence seam for engagement metrics.
 *
 * [EngagementRepositoryImpl] depends on this interface instead of [SettingsRepository]
 * to avoid the inverted dependency where a use-case/repository layer reaches into
 * a lower-level repository that already depends on it.
 *
 * Implementations are provided by [SettingsRepository] via binding.
 */
interface EngagementStorage {
  val engagementSnapshot: StateFlow<EngagementSnapshot>

  suspend fun setEngagementSnapshot(snapshot: EngagementSnapshot)
}
