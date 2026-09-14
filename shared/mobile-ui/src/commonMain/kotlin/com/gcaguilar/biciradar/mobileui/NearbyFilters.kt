package com.gcaguilar.biciradar.mobileui

import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationHourlyPattern
import com.gcaguilar.biciradar.core.TripMode
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyBikeTypeBoth
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyBikeTypeElectric
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyBikeTypeMechanical
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyDistanceOption1km
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyDistanceOption2km
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyDistanceOption500m
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbySortBestPredicted
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbySortMostBikes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbySortMostSlots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbySortName
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbySortNearest
import org.jetbrains.compose.resources.StringResource
import kotlin.math.abs

/** Distinción de tipo de bicicleta ofrecida por la ciudad seleccionada. */
enum class NearbyBikeType(
  val labelKey: StringResource,
) {
  BOTH(Res.string.nearbyBikeTypeBoth),
  ELECTRIC(Res.string.nearbyBikeTypeElectric),
  MECHANICAL(Res.string.nearbyBikeTypeMechanical),
}

/** Límite de distancia máxima del listado de Cerca. */
enum class NearbyMaxDistance(
  val meters: Int,
  val labelKey: StringResource,
) {
  METERS_500(500, Res.string.nearbyDistanceOption500m),
  KILOMETERS_1(1_000, Res.string.nearbyDistanceOption1km),
  KILOMETERS_2(2_000, Res.string.nearbyDistanceOption2km),
}

/** Criterios de ordenación del listado de Cerca. */
enum class NearbySort(
  val labelKey: StringResource,
) {
  NEAREST(Res.string.nearbySortNearest),
  MOST_BIKES(Res.string.nearbySortMostBikes),
  MOST_SLOTS(Res.string.nearbySortMostSlots),
  NAME(Res.string.nearbySortName),
  BEST_PREDICTED_AVAILABILITY(Res.string.nearbySortBestPredicted),
}

/**
 * Estado de filtros y orden del listado de Cerca.
 *
 * Por defecto no se oculta ninguna estación cercana: se usa el radio más amplio
 * disponible (2 km) y el orden natural por cercanía.
 */
data class NearbyFilters(
  val bikeType: NearbyBikeType = NearbyBikeType.BOTH,
  val maxDistance: NearbyMaxDistance = NearbyMaxDistance.KILOMETERS_2,
  val favoritesOnly: Boolean = false,
  val sort: NearbySort = NearbySort.NEAREST,
) {
  val isDefault: Boolean get() = this == NearbyFilters()

  /** Número de filtros/orden activos, para el indicador del botón. */
  val activeCount: Int
    get() =
      (if (bikeType != NearbyBikeType.BOTH) 1 else 0) +
        (if (maxDistance != NearbyMaxDistance.KILOMETERS_2) 1 else 0) +
        (if (favoritesOnly) 1 else 0) +
        (if (sort != NearbySort.NEAREST) 1 else 0)
}

/** Aplica los filtros de tipo, distancia y favoritas sobre estaciones ya relevantes al modo. */
internal fun filterNearbyStations(
  stations: List<Station>,
  filters: NearbyFilters,
  favoriteIds: Set<String>,
): List<Station> =
  stations.filter { station ->
    station.matchesBikeType(filters.bikeType) &&
      station.distanceMeters <= filters.maxDistance.meters &&
      (!filters.favoritesOnly || station.id in favoriteIds)
  }

/** Ordena el listado según [sort]; los empates se resuelven por cercanía. */
internal fun sortNearbyStations(
  stations: List<Station>,
  sort: NearbySort,
  tripMode: TripMode,
  patterns: Map<String, List<StationHourlyPattern>> = emptyMap(),
  hour: Int = 0,
  isWeekend: Boolean = false,
): List<Station> =
  when (sort) {
    NearbySort.NEAREST -> {
      stations.sortedBy(Station::distanceMeters)
    }

    NearbySort.MOST_BIKES -> {
      stations.sortedWith(
        compareByDescending<Station> { it.bikesAvailable }.thenBy { it.distanceMeters },
      )
    }

    NearbySort.MOST_SLOTS -> {
      stations.sortedWith(
        compareByDescending<Station> { it.slotsFree }.thenBy { it.distanceMeters },
      )
    }

    NearbySort.NAME -> {
      stations.sortedWith(
        compareBy<Station>({ it.name.lowercase() }, { it.distanceMeters }),
      )
    }

    NearbySort.BEST_PREDICTED_AVAILABILITY -> {
      stations.sortedWith(
        compareByDescending<Station> {
          predictedAvailability(it, tripMode, patterns, hour, isWeekend)
        }.thenBy { it.distanceMeters },
      )
    }
  }

/**
 * Disponibilidad esperada para [station] en el momento [hour]/[isWeekend].
 *
 * Cuando la ciudad publica patrones horarios se usa la media prevista de la hora
 * más cercana (bicis a pie, huecos en bici). Si no hay patrón disponible se cae a
 * la disponibilidad actual, de modo que el orden sigue siendo útil en todas las
 * ciudades.
 */
internal fun predictedAvailability(
  station: Station,
  tripMode: TripMode,
  patterns: Map<String, List<StationHourlyPattern>>,
  hour: Int,
  isWeekend: Boolean,
): Double {
  val pattern = patterns[station.id].predictionFor(hour, isWeekend)
  return when (tripMode) {
    TripMode.Pedestrian -> pattern?.bikesAvg ?: station.bikesAvailable.toDouble()
    TripMode.Cyclist -> pattern?.anchorsAvg ?: station.slotsFree.toDouble()
  }
}

private fun Station.matchesBikeType(bikeType: NearbyBikeType): Boolean =
  when (bikeType) {
    NearbyBikeType.BOTH -> true

    NearbyBikeType.ELECTRIC -> ebikesAvailable > 0

    // Si la ciudad no desglosa tipos, `regularBikesAvailable` puede ser 0 aunque
    // haya bicicletas; se usa la diferencia con las eléctricas como respaldo.
    NearbyBikeType.MECHANICAL -> regularBikesAvailable > 0 || bikesAvailable > ebikesAvailable
  }

private fun List<StationHourlyPattern>?.predictionFor(
  hour: Int,
  isWeekend: Boolean,
): StationHourlyPattern? {
  val dayType = if (isWeekend) WEEKEND_DAY_TYPE else WEEKDAY_DAY_TYPE
  val forDay = this?.filter { it.dayType == dayType }.orEmpty()
  return forDay.firstOrNull { it.hour == hour } ?: forDay.minByOrNull { abs(it.hour - hour) }
}

private const val WEEKDAY_DAY_TYPE = "WEEKDAY"
private const val WEEKEND_DAY_TYPE = "WEEKEND"
