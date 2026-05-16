package com.gcaguilar.biciradar.core.di

import com.gcaguilar.biciradar.core.EngagementStorage
import com.gcaguilar.biciradar.core.SettingsRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json

/**
 * Binding Container para dependencias core de la aplicación.
 *
 * Provee:
 * - CoroutineScope para el ciclo de vida de la app
 * - Configuración de Json
 * - EngagementStorage (delegado a SettingsRepository)
 */
@BindingContainer
object CoreBindings {
  @SingleIn(AppScope::class)
  @Provides
  fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  @SingleIn(AppScope::class)
  @Provides
  fun provideJson(): Json =
    Json {
      ignoreUnknownKeys = true
      explicitNulls = false
    }

  @SingleIn(AppScope::class)
  @Provides
  fun provideEngagementStorage(settingsRepository: SettingsRepository): EngagementStorage = settingsRepository
}
