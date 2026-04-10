package com.gcaguilar.biciradar.core.di

import dev.zacsweers.metro.Scope

/**
 * Scope para el grafo de Trip.
 * 
 * Este scope representa el ciclo de vida de un viaje/trip activo.
 * El grafo se crea cuando el usuario inicia un trip y se destruye cuando
 * el trip finaliza o se cancela.
 */
@Scope
annotation class TripScope