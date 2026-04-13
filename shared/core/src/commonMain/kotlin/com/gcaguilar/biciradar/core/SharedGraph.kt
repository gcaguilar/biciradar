package com.gcaguilar.biciradar.core

/**
 * Contrato raíz de dependencias de la aplicación.
 *
 * Compone todas las interfaces de feature para que las implementaciones concretas
 * (@DependencyGraph) solo necesiten satisfacer un único contrato. Los consumidores
 * de features individuales (shells, extensiones, tests) deben declarar dependencias
 * sobre la interfaz de feature mínima necesaria en lugar de sobre SharedGraph directamente.
 *
 * Jerarquía de interfaces de feature:
 * - [StationsFeatureDeps]  — consulta, refresco y monitorización de estaciones
 * - [FavoritesFeatureDeps] — favoritos, sincronización y alertas de lugar guardado
 * - [SessionFeatureDeps]   — sesión, ciudad seleccionada, asistente y ajustes
 * - [SurfaceFeatureDeps]   — snapshots de superficie para widgets y reloj
 * - [PlatformFeatureDeps]  — geo-búsqueda, navegación, logging y config remota
 * - [TripFeatureDeps]      — ciclo de vida del trip (scoped graph factory)
 * - [OnboardingFeatureDeps]— ciclo de vida del onboarding (scoped graph factory)
 *
 * @see CoreGraph   — implementación para wearApp, desktopApp y tests (en shared/core/di)
 * @see FeatureDeps.kt — declaraciones de cada interfaz de feature
 */
interface SharedGraph :
  StationsFeatureDeps,
  FavoritesFeatureDeps,
  SessionFeatureDeps,
  SurfaceFeatureDeps,
  PlatformFeatureDeps,
  TripFeatureDeps,
  OnboardingFeatureDeps
