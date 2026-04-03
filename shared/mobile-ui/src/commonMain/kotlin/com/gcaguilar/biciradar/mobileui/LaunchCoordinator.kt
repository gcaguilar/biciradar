package com.gcaguilar.biciradar.mobileui

import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.ChangeCityUseCase
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.DEFAULT_SURFACE_MONITORING_DURATION_SECONDS
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.SurfaceSnapshotRepository
import com.gcaguilar.biciradar.core.findStationMatchingQueryOrPinnedAlias
import com.gcaguilar.biciradar.core.selectNearbyStation
import com.gcaguilar.biciradar.core.selectNearbyStationWithBikes
import com.gcaguilar.biciradar.core.selectNearbyStationWithSlots
import com.gcaguilar.biciradar.mobileui.navigation.AssistantLaunchRequest
import com.gcaguilar.biciradar.mobileui.navigation.MobileLaunchRequest
import com.gcaguilar.biciradar.mobileui.navigation.Screen

internal data class LaunchResolution(
  val screen: Screen? = null,
  val searchQuery: String? = null,
  val assistantAction: AssistantAction? = null,
)

internal class LaunchCoordinator(
  private val changeCityUseCase: ChangeCityUseCase,
  private val favoritesRepository: FavoritesRepository,
  private val localNotifier: LocalNotifier,
  private val routeLauncher: RouteLauncher,
  private val stationsRepository: StationsRepository,
  private val surfaceMonitoringRepository: SurfaceMonitoringRepository,
  private val surfaceSnapshotRepository: SurfaceSnapshotRepository,
) {
  suspend fun resolveMobileLaunch(
    request: MobileLaunchRequest,
    stations: List<Station>,
    searchRadiusMeters: Int,
  ): LaunchResolution? = when (request) {
    MobileLaunchRequest.Home -> LaunchResolution(screen = Screen.Nearby)
    MobileLaunchRequest.Map -> LaunchResolution(screen = Screen.Map)
    MobileLaunchRequest.Favorites -> LaunchResolution(screen = Screen.Favorites)
    MobileLaunchRequest.SavedPlaceAlerts -> LaunchResolution(screen = Screen.SavedPlaceAlerts)
    MobileLaunchRequest.NearestStation -> {
      val station = selectNearbyStation(stations, searchRadiusMeters).highlightedStation ?: return null
      LaunchResolution(screen = Screen.StationDetail(station.id))
    }
    MobileLaunchRequest.NearestStationWithBikes -> {
      val station = selectNearbyStationWithBikes(stations, searchRadiusMeters).highlightedStation ?: return null
      LaunchResolution(screen = Screen.StationDetail(station.id))
    }
    MobileLaunchRequest.NearestStationWithSlots -> {
      val station = selectNearbyStationWithSlots(stations, searchRadiusMeters).highlightedStation ?: return null
      LaunchResolution(screen = Screen.StationDetail(station.id))
    }
    MobileLaunchRequest.OpenAssistant -> LaunchResolution(screen = Screen.Shortcuts)
    MobileLaunchRequest.StationStatus -> {
      val station = stations.firstOrNull() ?: return null
      LaunchResolution(
        screen = Screen.Shortcuts,
        assistantAction = AssistantAction.StationStatus(station.id),
      )
    }
    is MobileLaunchRequest.MonitorStation -> {
      val station = stationsRepository.stationById(request.stationId)
        ?: stations.firstOrNull { it.id == request.stationId }
        ?: return null
      val notificationsGranted = localNotifier.requestPermission()
      surfaceSnapshotRepository.refreshSnapshot()
      if (notificationsGranted) {
        surfaceMonitoringRepository.startMonitoring(
          stationId = station.id,
          durationSeconds = DEFAULT_SURFACE_MONITORING_DURATION_SECONDS,
          kind = SurfaceMonitoringKind.Docks,
        )
      }
      LaunchResolution(screen = Screen.StationDetail(station.id))
    }
    is MobileLaunchRequest.SelectCity -> {
      val city = City.fromId(request.cityId) ?: return null
      changeCityUseCase.execute(city = city)
      LaunchResolution(screen = Screen.Nearby)
    }
    is MobileLaunchRequest.RouteToStation -> {
      val station = request.stationId?.let(stationsRepository::stationById)
        ?: stations.firstOrNull()
        ?: return null
      routeLauncher.launch(station)
      LaunchResolution()
    }
    is MobileLaunchRequest.ShowStation -> {
      if (stationsRepository.stationById(request.stationId) == null) return null
      LaunchResolution(screen = Screen.StationDetail(request.stationId))
    }
  }

  fun resolveAssistantLaunch(
    request: AssistantLaunchRequest,
    stations: List<Station>,
  ): LaunchResolution {
    val station = resolveLaunchStation(
      stations = stations,
      stationId = when (request) {
        is AssistantLaunchRequest.RouteToStation -> request.stationId
        is AssistantLaunchRequest.SearchStation -> null
        is AssistantLaunchRequest.StationBikeCount -> request.stationId
        is AssistantLaunchRequest.StationSlotCount -> request.stationId
        is AssistantLaunchRequest.StationStatus -> request.stationId
      },
      stationQuery = when (request) {
        is AssistantLaunchRequest.RouteToStation -> request.stationQuery
        is AssistantLaunchRequest.SearchStation -> request.stationQuery
        is AssistantLaunchRequest.StationBikeCount -> request.stationQuery
        is AssistantLaunchRequest.StationSlotCount -> request.stationQuery
        is AssistantLaunchRequest.StationStatus -> request.stationQuery
      },
    )

    return when (request) {
      is AssistantLaunchRequest.SearchStation -> {
        val screen = station?.let { Screen.StationDetail(it.id) } ?: Screen.Map
        LaunchResolution(
          screen = screen,
          searchQuery = request.stationQuery,
        )
      }
      is AssistantLaunchRequest.StationStatus -> assistantLaunchResolution(
        station = station,
        stationQuery = request.stationQuery,
        assistantAction = { AssistantAction.StationStatus(it) },
      )
      is AssistantLaunchRequest.StationBikeCount -> assistantLaunchResolution(
        station = station,
        stationQuery = request.stationQuery,
        assistantAction = { AssistantAction.StationBikeCount(it) },
      )
      is AssistantLaunchRequest.StationSlotCount -> assistantLaunchResolution(
        station = station,
        stationQuery = request.stationQuery,
        assistantAction = { AssistantAction.StationSlotCount(it) },
      )
      is AssistantLaunchRequest.RouteToStation -> {
        if (station != null) {
          routeLauncher.launch(station)
          LaunchResolution()
        } else {
          LaunchResolution(
            screen = Screen.Map,
            searchQuery = request.stationQuery.orEmpty(),
          )
        }
      }
    }
  }

  private fun assistantLaunchResolution(
    station: Station?,
    stationQuery: String?,
    assistantAction: (String) -> AssistantAction,
  ): LaunchResolution {
    return if (station != null) {
      LaunchResolution(
        screen = Screen.Shortcuts,
        assistantAction = assistantAction(station.id),
      )
    } else {
      LaunchResolution(
        screen = Screen.Map,
        searchQuery = stationQuery.orEmpty(),
      )
    }
  }

  private fun resolveLaunchStation(
    stations: List<Station>,
    stationId: String?,
    stationQuery: String?,
  ): Station? {
    return stationId?.let(stationsRepository::stationById) ?: findStationMatchingQueryOrPinnedAlias(
      stations = stations,
      query = stationQuery,
      homeStationId = favoritesRepository.currentHomeStationId(),
      workStationId = favoritesRepository.currentWorkStationId(),
    )
  }
}
