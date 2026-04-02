package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.EnvironmentalRepository

internal class MapEnvironmentalViewModelFactory(
  private val environmentalRepository: EnvironmentalRepository,
) {
  fun create(): MapEnvironmentalViewModel = MapEnvironmentalViewModel(
    environmentalRepository = environmentalRepository,
  )
}
