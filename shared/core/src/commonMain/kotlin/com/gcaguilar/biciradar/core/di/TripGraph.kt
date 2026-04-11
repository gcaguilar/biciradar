package com.gcaguilar.biciradar.core.di

import com.gcaguilar.biciradar.core.TripRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension

/**
 * Graph Extension para el flujo de Trip/Viaje.
 *
 * Este grafo extiende el AppGraph base y proporciona dependencias específicas
 * para el flujo de viaje. Se crea dinámicamente cuando el usuario inicia un trip
 * y se destruye cuando el trip finaliza.
 *
 * Ejemplo de uso:
 * ```
 * val tripGraph = appGraph.tripGraphFactory.create(destination, searchRadius)
 * val tripRepository = tripGraph.tripRepository
 * ```
 */
@GraphExtension(TripScope::class)
interface TripGraph {
  /**
   * Repositorio de trip con ciclo de vida acotado al grafo.
   * Cada instancia de TripGraph tiene su propio TripRepository.
   */
  val tripRepository: TripRepository

  /**
   * Factory para crear instancias de TripGraph.
   *
   * La factory se contribuye al AppScope padre para que esté disponible
   * desde el grafo base.
   */
  @ContributesTo(AppScope::class)
  @GraphExtension.Factory
  interface Factory {
    /**
     * Crea un nuevo TripGraph.
     */
    fun createTripGraph(): TripGraph
  }
}
