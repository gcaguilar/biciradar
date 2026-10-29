package com.gcaguilar.biciradar.core.backend

import com.gcaguilar.biciradar.core.RefreshWidgetDataUseCase
import com.gcaguilar.biciradar.core.StationsRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.json.Json

/**
 * Entry point for platform push receivers. Parses the backend data payload,
 * publishes it on [PushMessageBus] and refreshes the affected station cache so
 * widgets/watch surfaces render the new availability (plan §I).
 */
@SingleIn(AppScope::class)
@Inject
class PushMessageHandler(
  private val pushMessageBus: PushMessageBus,
  private val stationsRepository: StationsRepository,
  private val refreshWidgetDataUseCase: RefreshWidgetDataUseCase,
  private val json: Json,
) {
  /** Handles the `data` map of a remote notification. */
  suspend fun onDataMessage(data: Map<String, String>): PushMessage? {
    val payload = data["payload"] ?: return null
    val message = runCatching { json.decodeFromString(PushMessage.serializer(), payload) }.getOrNull() ?: return null
    pushMessageBus.publish(message)
    runCatching { stationsRepository.refreshAvailability(listOf(message.stationId)) }
    runCatching { refreshWidgetDataUseCase.execute() }
    return message
  }
}
