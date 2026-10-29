package com.gcaguilar.biciradar.core.backend

import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.auth.AuthProvider
import com.gcaguilar.biciradar.core.auth.AuthState
import com.gcaguilar.biciradar.core.push.DeviceTokenProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Pushes the user's station interests to `PUT /v1/subscriptions` so the backend
 * can watch them and raise push alerts. Favourites become `FAVORITE`
 * subscriptions; saved-place alert rules become `ALERT` subscriptions.
 *
 * Local-first: the device keeps owning the data and the backend is reconciled
 * with the full desired set whenever it changes or the session becomes active.
 */
@SingleIn(AppScope::class)
@Inject
class SubscriptionSyncService(
  private val apiClient: BiciRadarApiClient,
  private val authProvider: AuthProvider,
  private val favoritesRepository: FavoritesRepository,
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
  private val settingsRepository: SettingsRepository,
  private val deviceTokenProvider: DeviceTokenProvider,
  private val scope: CoroutineScope,
) {
  @kotlin.concurrent.Volatile private var started = false
  private val mutex = Mutex()

  /** Starts observing local changes. Idempotent. */
  fun start() {
    if (started) return
    started = true
    scope.launch {
      combine(
        favoritesRepository.favoriteIds,
        favoritesRepository.homeStationId,
        favoritesRepository.workStationId,
        savedPlaceAlertsRepository.rules,
      ) { favoriteIds, homeStationId, workStationId, rules ->
        InterestSnapshot(favoriteIds, homeStationId, workStationId, rules)
      }.collect { push(it) }
    }
    scope.launch {
      authProvider.credentialsState.collect { state ->
        if (state == AuthState.Authenticated) push(currentSnapshot())
      }
    }
  }

  /** Pushes the current desired set once. Exposed for tests and explicit retries. */
  suspend fun syncNow() {
    push(currentSnapshot())
  }

  private fun currentSnapshot(): InterestSnapshot =
    InterestSnapshot(
      favoriteIds = favoritesRepository.favoriteIds.value,
      homeStationId = favoritesRepository.homeStationId.value,
      workStationId = favoritesRepository.workStationId.value,
      rules = savedPlaceAlertsRepository.rules.value,
    )

  private suspend fun push(snapshot: InterestSnapshot) {
    if (!apiClient.isConfigured || authProvider.credentialsState.value != AuthState.Authenticated) return
    val cityId = settingsRepository.currentSelectedCity().id
    val subscriptions = buildSubscriptions(snapshot, cityId)
    mutex.withLock {
      registerDevice()
      runCatching { apiClient.putSubscriptions(subscriptions) }
    }
  }

  /** Registers/rotates the push token so the backend can deliver alerts. */
  private suspend fun registerDevice() {
    val token = deviceTokenProvider.currentToken()?.takeIf { it.isNotBlank() } ?: return
    runCatching {
      apiClient.registerDevice(
        RegisterDeviceRequest(
          token = token,
          platform = deviceTokenProvider.platform,
          appId = deviceTokenProvider.appId,
          locale = deviceTokenProvider.locale,
          pushEnvironment = deviceTokenProvider.pushEnvironment,
        ),
      )
    }
  }
}

/** Snapshot of the local interests that map to backend subscriptions. */
internal data class InterestSnapshot(
  val favoriteIds: Set<String>,
  val homeStationId: String?,
  val workStationId: String?,
  val rules: List<SavedPlaceAlertRule>,
)

internal fun buildSubscriptions(
  snapshot: InterestSnapshot,
  cityId: String,
): List<SubscriptionDto> {
  val favoriteStationIds =
    buildSet {
      addAll(snapshot.favoriteIds)
      snapshot.homeStationId?.let(::add)
      snapshot.workStationId?.let(::add)
    }
  val favorites =
    favoriteStationIds.map { stationId ->
      SubscriptionDto(
        id = "favorite:$stationId",
        stationId = stationId,
        cityId = cityId,
        kind = "FAVORITE",
        enabled = true,
      )
    }
  val alerts =
    snapshot.rules.map { rule ->
      SubscriptionDto(
        id = rule.id,
        stationId = rule.target.stationId,
        cityId = rule.target.cityId,
        kind = "ALERT",
        condition = rule.condition.toWireCondition(),
        threshold = rule.condition.wireThreshold(),
        enabled = rule.isEnabled,
      )
    }
  return favorites + alerts
}

internal fun SavedPlaceAlertCondition.toWireCondition(): String =
  when (this) {
    is SavedPlaceAlertCondition.BikesAtLeast -> "BIKES_AT_LEAST"
    is SavedPlaceAlertCondition.DocksAtLeast -> "DOCKS_AT_LEAST"
    SavedPlaceAlertCondition.BikesEqualsZero -> "BIKES_EQUALS_ZERO"
    SavedPlaceAlertCondition.DocksEqualsZero -> "DOCKS_EQUALS_ZERO"
  }

internal fun SavedPlaceAlertCondition.wireThreshold(): Int? =
  when (this) {
    is SavedPlaceAlertCondition.BikesAtLeast -> count
    is SavedPlaceAlertCondition.DocksAtLeast -> count
    else -> null
  }
