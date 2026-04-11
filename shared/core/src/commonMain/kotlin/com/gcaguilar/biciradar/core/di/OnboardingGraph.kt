package com.gcaguilar.biciradar.core.di

import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension

/**
 * Graph Extension para el flujo de Onboarding.
 *
 * Este grafo extiende el AppGraph base y proporciona dependencias específicas
 * para el flujo de onboarding del usuario. Se crea al inicio de la app si
 * el onboarding no está completo y se destruye cuando finaliza.
 *
 * Ejemplo de uso:
 * ```
 * val onboardingGraph = appGraph.onboardingGraphFactory.create()
 * // Usar repositories del grafo
 * val settingsRepository = onboardingGraph.settingsRepository
 * val favoritesRepository = onboardingGraph.favoritesRepository
 * ```
 */
@GraphExtension(OnboardingScope::class)
interface OnboardingGraph {
  /**
   * SettingsRepository para gestionar preferencias durante onboarding.
   */
  val settingsRepository: SettingsRepository

  /**
   * FavoritesRepository para gestionar favoritos durante onboarding.
   */
  val favoritesRepository: FavoritesRepository

  /**
   * Factory para crear instancias de OnboardingGraph.
   *
   * La factory se contribuye al AppScope padre para que esté disponible
   * desde el grafo base.
   */
  @ContributesTo(AppScope::class)
  @GraphExtension.Factory
  interface Factory {
    /**
     * Crea un nuevo OnboardingGraph.
     */
    fun createOnboardingGraph(): OnboardingGraph
  }
}
