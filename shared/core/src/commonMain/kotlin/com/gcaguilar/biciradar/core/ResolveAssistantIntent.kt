package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Resolves an assistant action against the current station state,
 * encapsulating favorites and search-radius lookups.
 *
 * Callers supply the [AssistantAction] and receive an [AssistantResolution]
 * without needing to access any repository directly.
 */
@SingleIn(AppScope::class)
@Inject
class ResolveAssistantIntent(
    private val assistantIntentResolver: AssistantIntentResolver,
    private val stationsRepository: StationsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun execute(action: AssistantAction): AssistantResolution {
        val state = stationsRepository.state.value
        val favoriteIds = state.stations
            .filter { favoritesRepository.isFavorite(it.id) }
            .map { it.id }
            .toSet()
        return assistantIntentResolver.resolve(
            action = action,
            stationsState = state,
            favoriteIds = favoriteIds,
            searchRadiusMeters = settingsRepository.currentSearchRadiusMeters(),
        )
    }
}
