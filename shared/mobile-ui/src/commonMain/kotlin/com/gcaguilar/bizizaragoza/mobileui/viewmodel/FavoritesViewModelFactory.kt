package com.gcaguilar.bizizaragoza.mobileui.viewmodel

import com.gcaguilar.bizizaragoza.core.FavoritesRepository
import com.gcaguilar.bizizaragoza.core.RouteLauncher
import com.gcaguilar.bizizaragoza.core.StationsRepository

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
