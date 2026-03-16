package com.gcaguilar.bizizaragoza.mobileui.viewmodel

import com.gcaguilar.bizizaragoza.core.FavoritesRepository
import com.gcaguilar.bizizaragoza.core.RouteLauncher
import com.gcaguilar.bizizaragoza.core.StationsRepository

class NearbyViewModelFactory(
  private val stationsRepository: StationsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val routeLauncher: RouteLauncher,
  private val searchRadiusMeters: Int,
) {
  fun create(): NearbyViewModel = NearbyViewModel(
    stationsRepository = stationsRepository,
    favoritesRepository = favoritesRepository,
    routeLauncher = routeLauncher,
    searchRadiusMeters = searchRadiusMeters,
  )
}
