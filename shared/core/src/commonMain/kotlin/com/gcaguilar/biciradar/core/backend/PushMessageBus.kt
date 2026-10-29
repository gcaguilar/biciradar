package com.gcaguilar.biciradar.core.backend

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * In-process bus that decouples the platform push receiver (FCM/APNs) from the
 * shared consumers (trip monitoring, surfaces refresh).
 */
@SingleIn(AppScope::class)
@Inject
class PushMessageBus {
  private val mutableMessages = MutableSharedFlow<PushMessage>(extraBufferCapacity = 16)

  val messages: SharedFlow<PushMessage> = mutableMessages

  fun publish(message: PushMessage) {
    mutableMessages.tryEmit(message)
  }
}
