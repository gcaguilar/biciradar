package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.TripRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.geo.GeoSearchUseCase
import com.gcaguilar.biciradar.core.geo.ReverseGeocodeUseCase

class TripViewModelFactory(
  private val tripRepository: TripRepository,
  private val surfaceMonitoringRepository: SurfaceMonitoringRepository,
  private val geoSearchUseCase: GeoSearchUseCase,
  private val reverseGeocodeUseCase: ReverseGeocodeUseCase,
  private val searchRadiusMeters: Int,
) {
  fun create(): TripViewModel {
    return TripViewModel(
      tripRepository = tripRepository,
      surfaceMonitoringRepository = surfaceMonitoringRepository,
      geoSearchUseCase = geoSearchUseCase,
      reverseGeocodeUseCase = reverseGeocodeUseCase,
      searchRadiusMeters = searchRadiusMeters,
    )
  }
}
