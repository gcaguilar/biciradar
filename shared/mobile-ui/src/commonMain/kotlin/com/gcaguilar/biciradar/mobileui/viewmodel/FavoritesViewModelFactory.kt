package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.StationsRepository

class FavoritesViewModelFactory(
  private val favoritesRepository: FavoritesRepository,
  private val stationsRepository: StationsRepository,
  private val routeLauncher: RouteLauncher,
) {
  fun create(): FavoritesViewModel = FavoritesViewModel(
    favoritesRepository = favoritesRepository,
    stationsRepository = stationsRepository,
    routeLauncher = routeLauncher,
  )
}
