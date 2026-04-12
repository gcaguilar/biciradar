package com.gcaguilar.biciradar.mobileui.di

import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.di.CoreBindings
import com.gcaguilar.biciradar.core.di.DatabaseBindings
import com.gcaguilar.biciradar.core.di.GeoBindings
import com.gcaguilar.biciradar.core.di.NetworkBindings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

/**
 * Grafo de dependencias para la capa mobile-ui.
 *
 * Extiende [SharedGraph] (todos los repos, use cases, APIs) y añade
 * [ViewModelGraph] para exponer [metroViewModelFactory].
 *
 * Este grafo se declara aquí (en shared/mobile-ui) en lugar de en shared/core
 * para que Metro pueda ver todas las contribuciones @ContributesIntoMap de los
 * ViewModels en tiempo de compilación.
 *
 * Es `internal` para poder referenciar ViewModels que son `internal`.
 * Usar [createMobileGraph] desde módulos externos para crear una instancia.
 */
@DependencyGraph(
  AppScope::class,
  bindingContainers = [CoreBindings::class, DatabaseBindings::class, NetworkBindings::class, GeoBindings::class],
)
internal interface MobileGraph :
  SharedGraph,
  ViewModelGraph {
  @DependencyGraph.Factory
  fun interface Factory {
    fun create(
      @Includes platformBindings: PlatformBindings,
    ): MobileGraph
  }
}

/**
 * Public factory function for modules that can't access the internal [MobileGraph] type.
 * Returns [SharedGraph] so callers can store the graph typed as the common interface.
 *
 * Use this from Kotlin/Android. For Swift/iOS use [MobileGraphFactory.shared.create].
 */
fun createMobileGraph(platformBindings: PlatformBindings): SharedGraph = MobileGraph.Companion.create(platformBindings)

/**
 * iOS-accessible singleton factory.
 *
 * Kotlin `object` declarations are exported to Swift as `ObjectName.shared`, making them
 * reliable to call from Swift — unlike top-level Kotlin functions, which Kotlin/Native
 * does not guarantee to expose as top-level Swift symbols.
 *
 * Swift usage:
 * ```swift
 * MobileGraphFactory.shared.create(platformBindings: bindings)
 * ```
 */
object MobileGraphFactory {
  fun create(platformBindings: PlatformBindings): SharedGraph = MobileGraph.Companion.create(platformBindings)
}
