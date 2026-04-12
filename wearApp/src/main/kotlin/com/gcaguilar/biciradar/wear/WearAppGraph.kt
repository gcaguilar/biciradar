package com.gcaguilar.biciradar.wear

import android.app.Application
import com.gcaguilar.biciradar.core.AppConfiguration
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.di.CoreGraph
import com.gcaguilar.biciradar.core.platform.AndroidPlatformBindings

/**
 * Singleton que mantiene el SharedGraph único para la app Wear.
 */
object WearAppGraph {
  private var _graph: SharedGraph? = null
  private var _platformBindings: AndroidPlatformBindings? = null

  fun initialize(application: Application) {
    if (_graph != null) return

    val bindings =
      AndroidPlatformBindings(
        context = application.applicationContext,
        appConfiguration = AppConfiguration(),
      )
    _platformBindings = bindings
    _graph = CoreGraph.Companion.create(bindings)
  }

  val graph: SharedGraph
    get() = _graph ?: throw IllegalStateException("WearAppGraph no inicializado")

  val platformBindings: AndroidPlatformBindings
    get() = _platformBindings ?: throw IllegalStateException("WearAppGraph no inicializado")

  fun isInitialized(): Boolean = _graph != null
}
