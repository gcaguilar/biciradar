package com.gcaguilar.biciradar

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Despierta el proceso del teléfono cuando el reloj publica cambios en `/bizi/favorites`.
 *
 * Sin este servicio, los `DataClient.OnDataChangedListener` registrados al crear el grafo
 * sólo entregan cambios mientras el proceso del teléfono está vivo; con el proceso muerto
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

    // En el flujo normal BiciRadarApplication.onCreate ya inicializó el grafo; el servicio
    // puede arrancar el proceso por su cuenta, así que se cubre ese caso.
    if (!BiziAppGraph.isInitialized()) {
      BiziAppGraph.initialize(application)
    }

    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
      runCatching { BiziAppGraph.graph.syncFavoritesFromPeer.execute() }
        .onFailure { error -> Log.w(TAG, "No se pudieron sincronizar los favoritos del reloj", error) }
    }
  }

  private companion object {
    private const val TAG = "WearFavoritesSync"
    private const val FAVORITES_PATH = "/bizi/favorites"
  }
}
