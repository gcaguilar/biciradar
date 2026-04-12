package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Returns the currently selected city without any I/O.
 */
@SingleIn(AppScope::class)
@Inject
class GetCurrentCity(
    private val settingsRepository: SettingsRepository,
) {
    fun execute(): City = settingsRepository.currentSelectedCity()
}
