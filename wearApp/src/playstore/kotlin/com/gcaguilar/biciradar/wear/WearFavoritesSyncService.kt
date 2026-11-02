package com.gcaguilar.biciradar.wear

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Despierta el proceso del reloj cuando el teléfono publica cambios en `/bizi/favorites`.
 *
 * Sin este servicio, los `DataClient.OnDataChangedListener` registrados al crear el grafo
 * sólo entregan cambios mientras el proceso del reloj está vivo; con el proceso muerto
 * el cambio no se aplicaba hasta que el usuario abría la app.
 */
class WearFavoritesSyncService : WearableListenerService() {
  override fun onDataChanged(dataEvents: DataEventBuffer) {
    super.onDataChanged(dataEvents)

    val hasFavoritesChange =
      dataEvents.any { event ->
        event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == FAVORITES_PATH
      }
    if (!hasFavoritesChange) return

    // Mismo patrón que FavoriteStationTileService / StationComplicationProvider: el
    // servicio puede arrancar el proceso sin que WearActivity haya inicializado el grafo.
    if (!WearAppGraph.isInitialized()) {
      WearAppGraph.initialize(applicationContext as android.app.Application)
    }

    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
      runCatching { WearAppGraph.graph.syncFavoritesFromPeer.execute() }
        .onFailure { error -> Log.w(TAG, "No se pudieron sincronizar los favoritos del teléfono", error) }
    }
  }

  private companion object {
    private const val TAG = "WearFavoritesSync"
    private const val FAVORITES_PATH = "/bizi/favorites"
  }
}
