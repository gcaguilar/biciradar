package com.gcaguilar.biciradar.mobileui.usecases

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.TripDestination
import com.gcaguilar.biciradar.core.TripRepository
import com.gcaguilar.biciradar.core.TripState
import com.gcaguilar.biciradar.core.geo.GeoError
import com.gcaguilar.biciradar.core.geo.GeoResult
import com.gcaguilar.biciradar.core.geo.GeoSearchUseCase
import com.gcaguilar.biciradar.core.geo.ReverseGeocodeUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow

/**
 * Use case que agrupa las operaciones relacionadas con la gestión de viajes,
 * destinos y navegación.
 */
class TripManagementUseCase(
  private val tripRepository: TripRepository,
  private val settingsRepository: SettingsRepository,
) {
  val tripState: StateFlow<TripState> = tripRepository.state
  val searchRadiusMeters: StateFlow<Int> = settingsRepository.searchRadiusMeters

  suspend fun setDestination(
    destination: TripDestination,
    searchRadiusMeters: Int,
  ) {
    tripRepository.setDestination(destination, searchRadiusMeters)
  }

  suspend fun selectStation(station: Station) {
    tripRepository.selectStation(station)
  }

  suspend fun startMonitoring(durationSeconds: Int) {
    tripRepository.startMonitoring(durationSeconds)
  }

  fun stopMonitoring() {
    tripRepository.stopMonitoring()
  }

  fun clearTrip() {
    tripRepository.clearTrip()
  }

  fun dismissAlert() {
    tripRepository.dismissAlert()
  }

  suspend fun preferredMonitoringDurationSeconds(): Int? = settingsRepository.preferredMonitoringDurationSeconds()

  suspend fun setPreferredMonitoringDurationSeconds(durationSeconds: Int) {
    settingsRepository.setPreferredMonitoringDurationSeconds(durationSeconds)
  }

  val nearestStationWithSlots: String?
    get() = tripState.value.nearestStationWithSlots?.id
}

/**
 * Use case para las operaciones de monitoreo en superficie
 */
class SurfaceMonitoringUseCase(
  private val surfaceMonitoringRepository: SurfaceMonitoringRepository,
) {
  suspend fun startMonitoring(
    stationId: String,
    durationSeconds: Int,
    kind: SurfaceMonitoringKind,
  ) {
    surfaceMonitoringRepository.startMonitoring(stationId, durationSeconds, kind)
  }

  fun stopMonitoring() {
    surfaceMonitoringRepository.stopMonitoring()
  }

  suspend fun clearMonitoring() {
    surfaceMonitoringRepository.clearMonitoring()
  }
}

/**
 * Use case para operaciones de búsqueda geográfica y geocodificación
 */
class GeoLocationUseCase(
  private val geoSearchUseCase: GeoSearchUseCase,
  private val reverseGeocodeUseCase: ReverseGeocodeUseCase,
) {
  /**
   * Ejecuta búsqueda geográfica con manejo de errores tipado
   */
  suspend fun search(query: String): GeoSearchResult =
    try {
      val results = geoSearchUseCase.execute(query)
      GeoSearchResult.Success(results)
    } catch (cancelled: CancellationException) {
      throw cancelled
    } catch (error: GeoError.Server) {
      GeoSearchResult.Error.Server
    } catch (error: GeoError.Network) {
      GeoSearchResult.Error.Network
    } catch (error: Throwable) {
      GeoSearchResult.Error.Unknown(error.message ?: error::class.simpleName ?: "")
    }

  /**
   * Realiza geocodificación inversa de una ubicación
   */
  suspend fun reverseGeocode(location: GeoPoint): ReverseGeocodeResult =
    try {
      val result = reverseGeocodeUseCase.execute(location)
      if (result != null) {
        ReverseGeocodeResult.Success(result.name)
      } else {
        ReverseGeocodeResult.Fallback("${location.latitude}, ${location.longitude}")
      }
    } catch (error: Throwable) {
      ReverseGeocodeResult.Fallback("${location.latitude}, ${location.longitude}")
    }

  sealed class GeoSearchResult {
    data class Success(
      val results: List<GeoResult>,
    ) : GeoSearchResult()

    sealed class Error : GeoSearchResult() {
      data object Server : Error()

      data object Network : Error()

      data class Unknown(
        val message: String,
      ) : Error()
    }
  }

  sealed class ReverseGeocodeResult {
    data class Success(
      val name: String,
    ) : ReverseGeocodeResult()

    data class Fallback(
      val coordinates: String,
    ) : ReverseGeocodeResult()
  }
}
