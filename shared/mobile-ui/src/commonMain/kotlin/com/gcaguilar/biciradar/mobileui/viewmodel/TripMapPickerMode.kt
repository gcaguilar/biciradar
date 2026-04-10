package com.gcaguilar.biciradar.mobileui.viewmodel

import kotlinx.serialization.Serializable

/**
 * Modos disponibles para el selector de mapa de viaje.
 */
@Serializable
enum class TripMapPickerMode {
  /**
   * Modo para seleccionar un destino (coordenada arbitraria).
   */
  Destination,

  /**
   * Modo para seleccionar una estación existente.
   */
  Station,
}
