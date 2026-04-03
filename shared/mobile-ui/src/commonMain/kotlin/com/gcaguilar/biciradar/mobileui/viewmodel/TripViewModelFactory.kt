package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.TripRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.geo.GeoSearchUseCase
import com.gcaguilar.biciradar.core.geo.ReverseGeocodeUseCase
import com.gcaguilar.biciradar.mobileui.usecases.GeoLocationUseCase
import com.gcaguilar.biciradar.mobileui.usecases.SurfaceMonitoringUseCase
import com.gcaguilar.biciradar.mobileui.usecases.TripManagementUseCase

class TripViewModelFactory(
  private val tripRepository: TripRepository,
  private val surfaceMonitoringRepository: SurfaceMonitoringRepository,
  private val geoSearchUseCase: GeoSearchUseCase,
  private val reverseGeocodeUseCase: ReverseGeocodeUseCase,
  private val settingsRepository: SettingsRepository,
) {
  fun create(): TripViewModel {
    val tripManagementUseCase = TripManagementUseCase(
      tripRepository = tripRepository,
      settingsRepository = settingsRepository,
    )
    val surfaceMonitoringUseCase = SurfaceMonitoringUseCase(
      surfaceMonitoringRepository = surfaceMonitoringRepository,
    )
    val geoLocationUseCase = GeoLocationUseCase(
      geoSearchUseCase = geoSearchUseCase,
      reverseGeocodeUseCase = reverseGeocodeUseCase,
    )

    return TripViewModel(
      tripManagementUseCase = tripManagementUseCase,
      surfaceMonitoringUseCase = surfaceMonitoringUseCase,
      geoLocationUseCase = geoLocationUseCase,
    )
  }
}
