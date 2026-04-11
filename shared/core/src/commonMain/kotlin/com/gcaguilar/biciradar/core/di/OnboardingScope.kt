package com.gcaguilar.biciradar.core.di

import dev.zacsweers.metro.Scope

/**
 * Scope para el grafo de Onboarding.
 *
 * Este scope representa el ciclo de vida del flujo de onboarding.
 * El grafo existe durante todo el proceso de onboarding del usuario
 * y se destruye cuando se completa o se salta.
 */
@Scope
annotation class OnboardingScope
