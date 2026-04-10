package com.gcaguilar.biciradar

import android.os.Parcel
import android.os.Parcelable
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository

/**
 * Provee las dependencias necesarias para TripMonitorService.
 *
 * Este enfoque permite inyectar dependencias en un Service de Android
 * sin usar reflection ni holders estáticos, cumpliendo con el principio DIP.
 *
 * Nota: Los repositories no son realmente Parcelable, pero se pasan por referencia
 * a través del holder temporal. Esto es un workaround para Android Services.
 */
class TripMonitorServiceProvider private constructor(
  private val surfaceMonitoringRepositoryRef: Long,
  private val favoritesRepositoryRef: Long,
) : Parcelable {

  constructor(
    surfaceMonitoringRepository: SurfaceMonitoringRepository,
    favoritesRepository: FavoritesRepository,
  ) : this(
    surfaceMonitoringRepositoryRef = TripMonitorServiceDependenciesHolder.hold(surfaceMonitoringRepository),
    favoritesRepositoryRef = TripMonitorServiceDependenciesHolder.hold(favoritesRepository),
  )

  val surfaceMonitoringRepository: SurfaceMonitoringRepository
    get() = TripMonitorServiceDependenciesHolder.retrieveSurfaceMonitoring(surfaceMonitoringRepositoryRef)
      ?: throw IllegalStateException("SurfaceMonitoringRepository no longer available")

  val favoritesRepository: FavoritesRepository
    get() = TripMonitorServiceDependenciesHolder.retrieveFavorites(favoritesRepositoryRef)
      ?: throw IllegalStateException("FavoritesRepository no longer available")

  // Parcelable implementation
  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeLong(surfaceMonitoringRepositoryRef)
    parcel.writeLong(favoritesRepositoryRef)
  }

  override fun describeContents(): Int = 0

  companion object {
    fun cleanup() {
      TripMonitorServiceDependenciesHolder.clear()
    }

    @JvmField
    val CREATOR = object : Parcelable.Creator<TripMonitorServiceProvider> {
      override fun createFromParcel(parcel: Parcel): TripMonitorServiceProvider {
        val surfaceRef = parcel.readLong()
        val favoritesRef = parcel.readLong()
        return TripMonitorServiceProvider(surfaceRef, favoritesRef)
      }

      override fun newArray(size: Int): Array<TripMonitorServiceProvider?> {
        return arrayOfNulls(size)
      }
    }
  }
}

/**
 * Holder temporal que mantiene referencias a las dependencias mientras se pasan al Service.
 * Las referencias se limpian automáticamente después de ser recuperadas.
 */
internal object TripMonitorServiceDependenciesHolder {
  private var surfaceMonitoringRepository: SurfaceMonitoringRepository? = null
  private var favoritesRepository: FavoritesRepository? = null
  private var nextId = 1L

  fun hold(repository: SurfaceMonitoringRepository): Long {
    surfaceMonitoringRepository = repository
    return nextId++
  }

  fun hold(repository: FavoritesRepository): Long {
    favoritesRepository = repository
    return nextId++
  }

  fun retrieveSurfaceMonitoring(id: Long): SurfaceMonitoringRepository? {
    val repo = surfaceMonitoringRepository
    surfaceMonitoringRepository = null
    return repo
  }

  fun retrieveFavorites(id: Long): FavoritesRepository? {
    val repo = favoritesRepository
    favoritesRepository = null
    return repo
  }

  fun clear() {
    surfaceMonitoringRepository = null
    favoritesRepository = null
  }
}
