package com.gcaguilar.bizizaragoza.mobileui.viewmodel

import com.gcaguilar.bizizaragoza.core.GooglePlacesApi
import com.gcaguilar.bizizaragoza.core.TripRepository

class TripViewModelFactory(
  private val tripRepository: TripRepository,
  private val googlePlacesApi: GooglePlacesApi,
  private val googleMapsApiKey: String?,
  private val searchRadiusMeters: Int,
) {
  fun create(): TripViewModel {
    return TripViewModel(
      tripRepository = tripRepository,
      googlePlacesApi = googlePlacesApi,
      googleMapsApiKey = googleMapsApiKey,
      searchRadiusMeters = searchRadiusMeters,
    )
  }
}
