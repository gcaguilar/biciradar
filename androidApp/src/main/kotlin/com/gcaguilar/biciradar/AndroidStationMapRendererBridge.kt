package com.gcaguilar.biciradar

import com.gcaguilar.biciradar.mobileui.AndroidStationMapRenderer

object AndroidStationMapRendererBridge {
  private const val PROVIDER_CLASS = "com.gcaguilar.biciradar.PlaystoreAndroidStationMapRendererProvider"

  @Volatile private var resolved = false

  @Volatile private var cached: AndroidStationMapRenderer? = null

  fun load(): AndroidStationMapRenderer? {
    if (resolved) return cached
    synchronized(this) {
      if (resolved) return cached
      cached =
        runCatching {
          Class
            .forName(PROVIDER_CLASS)
            .getDeclaredConstructor()
            .newInstance() as? AndroidStationMapRenderer
        }.getOrNull()
      resolved = true
      return cached
    }
  }
}
