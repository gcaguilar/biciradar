package com.gcaguilar.bizizaragoza.mobileui.viewmodel

import com.gcaguilar.bizizaragoza.core.TripRepository
import com.gcaguilar.bizizaragoza.core.geo.GeoSearchUseCase
import com.gcaguilar.bizizaragoza.core.geo.ReverseGeocodeUseCase

class TripViewModelFactory(
  private val tripRepository: TripRepository,
  private val geoSearchUseCase: GeoSearchUseCase,
  private val reverseGeocodeUseCase: ReverseGeocodeUseCase,
  private val searchRadiusMeters: Int,
) {
  fun create(): TripViewModel {
    return TripViewModel(
      tripRepository = tripRepository,
      geoSearchUseCase = geoSearchUseCase,
      reverseGeocodeUseCase = reverseGeocodeUseCase,
      searchRadiusMeters = searchRadiusMeters,
    )
  }
}
