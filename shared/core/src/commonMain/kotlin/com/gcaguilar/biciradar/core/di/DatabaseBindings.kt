package com.gcaguilar.biciradar.core.di

import com.gcaguilar.biciradar.core.DatabaseFactory
import com.gcaguilar.biciradar.core.NoOpStationsCacheManager
import com.gcaguilar.biciradar.core.StationCacheStore
import com.gcaguilar.biciradar.core.StationsCacheManager
import com.gcaguilar.biciradar.core.StationsCacheManagerImpl
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.json.Json

/**
 * Binding Container para dependencias de base de datos.
 *
 * Provee:
 * - BiciRadarDatabase (nullable si no hay factory disponible)
 * - StationsCacheManager (con implementación real o no-op)
 */
@BindingContainer
object DatabaseBindings {
    @SingleIn(AppScope::class)
    @Provides
    fun provideDatabase(
        databaseFactory: DatabaseFactory?,
        json: Json,
    ): BiciRadarDatabase? = databaseFactory?.create(json)

    @SingleIn(AppScope::class)
    @Provides
    fun provideStationsCacheManager(
        database: BiciRadarDatabase?,
    ): StationsCacheManager = if (database != null) {
        StationsCacheManagerImpl(database, StationCacheStore(database))
    } else {
        NoOpStationsCacheManager()
    }
}