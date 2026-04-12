package com.gcaguilar.biciradar.core.di

import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.SharedGraph
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes

/**
 * Grafo de dependencias concreto para módulos que no necesitan ViewModels:
 * wearApp, desktopApp y tests de shared/core.
 *
 * Para la app móvil (iOS y Android), usar MobileGraph (en shared/mobile-ui)
 * que además incluye ViewModelGraph con todos los ViewModels contribuidos.
 */
@DependencyGraph(
  AppScope::class,
  bindingContainers = [CoreBindings::class, DatabaseBindings::class, NetworkBindings::class, GeoBindings::class],
)
interface CoreGraph : SharedGraph {

  @DependencyGraph.Factory
  fun interface Factory {
    fun create(@Includes platformBindings: PlatformBindings): CoreGraph
  }
}
