package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.StateFlow

/**
 * Exposes the reactive settings flows needed by UI layers without leaking
 * [SettingsRepository] to consumers outside [shared/core].
 *
 * Used by [BiziMobileApp] to collect theme, map-app preference and search
 * radius without holding a direct reference to the repository.
 */
@SingleIn(AppScope::class)
@Inject
class ObserveSettings(
    private val settingsRepository: SettingsRepository,
) {
    val searchRadiusMeters: StateFlow<Int> get() = settingsRepository.searchRadiusMeters
    val preferredMapApp: StateFlow<PreferredMapApp> get() = settingsRepository.preferredMapApp
    val themePreference: StateFlow<ThemePreference> get() = settingsRepository.themePreference
}
