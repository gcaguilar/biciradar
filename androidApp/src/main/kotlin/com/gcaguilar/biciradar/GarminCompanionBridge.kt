package com.gcaguilar.biciradar

import android.app.Application
import com.gcaguilar.biciradar.core.SharedGraph

object GarminCompanionBridge {
  private const val DELEGATE_CLASS = "com.gcaguilar.biciradar.PlaystoreGarminCompanionBridge"

  fun initialize(
    application: Application,
    graph: SharedGraph,
  ) {
    runCatching {
      val delegateClass = Class.forName(DELEGATE_CLASS)
      val method =
        delegateClass.getMethod(
          "initialize",
          Application::class.java,
          SharedGraph::class.java,
        )
      method.invoke(null, application, graph)
    }
  }
}
