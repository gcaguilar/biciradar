package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.currentTimeMs
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

private const val ALERT_COOLDOWN_MILLIS = 60L * 60L * 1000L

interface SavedPlaceAlertsRepository {
  val rules: StateFlow<List<SavedPlaceAlertRule>>
  suspend fun bootstrap()
  fun currentRules(): List<SavedPlaceAlertRule>
  fun ruleForTarget(target: SavedPlaceAlertTarget): SavedPlaceAlertRule?
  suspend fun upsertRule(target: SavedPlaceAlertTarget, condition: SavedPlaceAlertCondition, enabled: Boolean = true)
  suspend fun removeRule(ruleId: String)
  suspend fun removeRuleForTarget(target: SavedPlaceAlertTarget)
  suspend fun setRuleEnabled(ruleId: String, enabled: Boolean)
  suspend fun replaceAll(rules: List<SavedPlaceAlertRule>)
}

@Inject
class SavedPlaceAlertsRepositoryImpl(
  private val fileSystem: FileSystem,
  private val json: Json,
  private val storageDirectoryProvider: StorageDirectoryProvider,
  private val database: BiciRadarDatabase?,
) : SavedPlaceAlertsRepository {
  private val mutableRules = MutableStateFlow<List<SavedPlaceAlertRule>>(emptyList())
  private var bootstrapped = false

  override val rules: StateFlow<List<SavedPlaceAlertRule>> = mutableRules.asStateFlow()

  override suspend fun bootstrap() {
    if (bootstrapped) return
    mutableRules.value = readPersistedRules()
    bootstrapped = true
  }

  override fun currentRules(): List<SavedPlaceAlertRule> = mutableRules.value

  override fun ruleForTarget(target: SavedPlaceAlertTarget): SavedPlaceAlertRule? =
    mutableRules.value.firstOrNull { it.target.identityKey() == target.identityKey() }

  override suspend fun upsertRule(
    target: SavedPlaceAlertTarget,
    condition: SavedPlaceAlertCondition,
    enabled: Boolean,
  ) {
    if (!bootstrapped) bootstrap()
    val updated = mutableRules.value.filterNot { it.target.identityKey() == target.identityKey() } +
      SavedPlaceAlertRule(
        id = target.identityKey(),
        target = target,
        condition = condition,
        isEnabled = enabled,
      )
    persist(updated.sortedBy { it.id })
  }

  override suspend fun removeRule(ruleId: String) {
    if (!bootstrapped) bootstrap()
    persist(mutableRules.value.filterNot { it.id == ruleId })
  }

  override suspend fun removeRuleForTarget(target: SavedPlaceAlertTarget) {
    removeRule(target.identityKey())
  }

  override suspend fun setRuleEnabled(ruleId: String, enabled: Boolean) {
    if (!bootstrapped) bootstrap()
    persist(
      mutableRules.value.map { rule ->
        if (rule.id == ruleId) rule.copy(isEnabled = enabled) else rule
      },
    )
  }

  override suspend fun replaceAll(rules: List<SavedPlaceAlertRule>) {
    if (!bootstrapped) bootstrap()
    persist(rules.sortedBy { it.id })
  }

  private fun alertsPath() = "${storageDirectoryProvider.rootPath}/saved_place_alerts.json".toPath()

  private fun persist(rules: List<SavedPlaceAlertRule>) {
    if (database != null) {
      val persisted = persistToDatabase(rules)
      if (persisted) {
        deleteLegacyFile()
      } else {
        persistToFile(rules)
      }
    } else {
      persistToFile(rules)
    }
    mutableRules.value = rules
  }

  private fun readPersistedRules(): List<SavedPlaceAlertRule> {
    if (database == null) return readFromFile()
    val dbRules = readFromDatabase().orEmpty()
    val legacyRules = readFromFile()
    val dbHasData = dbRules.isNotEmpty()
    val legacyHasData = legacyRules.isNotEmpty()

    if (!legacyHasData) return dbRules
    if (dbHasData) {
      if (dbRules == legacyRules) {
        deleteLegacyFile()
      }
      return dbRules
    }

    // One-time migration: legacy JSON -> DB. Delete legacy only after successful write.
    val migrated = persistToDatabase(legacyRules)
    if (migrated) {
      deleteLegacyFile()
      return legacyRules
    }
    return legacyRules
  }

  private fun readFromFile(): List<SavedPlaceAlertRule> {
    val path = alertsPath()
    return if (fileSystem.exists(path)) {
      runCatching {
        json.decodeFromString<SavedPlaceAlertsSnapshot>(fileSystem.read(path) { readUtf8() }).rules
      }.getOrDefault(emptyList())
    } else {
      emptyList()
    }
  }

  private fun readFromDatabase(): List<SavedPlaceAlertRule>? {
    val db = database ?: return null
    return runCatching {
      db.biciradarQueries.getAllSavedPlaceAlertRules().executeAsList().mapNotNull { row ->
        val target = runCatching { json.decodeFromString<SavedPlaceAlertTarget>(row.target_json) }.getOrNull()
        val condition = runCatching { json.decodeFromString<SavedPlaceAlertCondition>(row.condition_json) }.getOrNull()
        if (target == null || condition == null) {
          null
        } else {
          SavedPlaceAlertRule(
            id = row.id,
            target = target,
            condition = condition,
            isEnabled = row.is_enabled != 0L,
            lastTriggeredEpoch = row.last_triggered_epoch,
            lastConditionMatched = row.last_condition_matched != 0L,
          )
        }
      }
    }.getOrNull()
  }

  private fun persistToDatabase(rules: List<SavedPlaceAlertRule>): Boolean {
    val db = database ?: return false
    return runCatching {
      db.transaction {
        db.biciradarQueries.deleteAllSavedPlaceAlertRules()
        rules.forEach { rule ->
          db.biciradarQueries.upsertSavedPlaceAlertRule(
            id = rule.id,
            targetJson = json.encodeToString(rule.target),
            conditionJson = json.encodeToString(rule.condition),
            isEnabled = if (rule.isEnabled) 1L else 0L,
            lastTriggeredEpoch = rule.lastTriggeredEpoch,
            lastConditionMatched = if (rule.lastConditionMatched) 1L else 0L,
          )
        }
      }
    }.isSuccess
  }

  private fun persistToFile(rules: List<SavedPlaceAlertRule>) {
    val path = alertsPath()
    val dir = path.parent ?: return
    fileSystem.createDirectories(dir)
    fileSystem.write(path) {
      writeUtf8(json.encodeToString(SavedPlaceAlertsSnapshot(rules = rules)))
    }
  }

  private fun deleteLegacyFile() {
    val path = alertsPath()
    if (fileSystem.exists(path)) {
      runCatching { fileSystem.delete(path) }
    }
  }
}

@Inject
class SavedPlaceAlertsEvaluator {
  fun evaluate(
    rules: List<SavedPlaceAlertRule>,
    stationsState: StationsState,
    nowEpoch: Long = currentTimeMs(),
  ): SavedPlaceAlertEvaluation {
    if (rules.isEmpty()) return SavedPlaceAlertEvaluation(updatedRules = rules, triggers = emptyList())

    val stationsById = stationsState.stations.associateBy { it.id }
    val updatedRules = mutableListOf<SavedPlaceAlertRule>()
    val triggers = mutableListOf<SavedPlaceAlertTrigger>()

    rules.forEach { rule ->
      if (!rule.isEnabled) {
        updatedRules += rule.copy(lastConditionMatched = false)
        return@forEach
      }
      val station = stationsById[rule.target.stationId]
      val matches = station?.let(rule::matches) == true
      val cooldownSatisfied = rule.lastTriggeredEpoch?.let { nowEpoch - it >= ALERT_COOLDOWN_MILLIS } ?: true
      val shouldTrigger = station != null && matches && !rule.lastConditionMatched && cooldownSatisfied

      if (shouldTrigger) {
        triggers += SavedPlaceAlertTrigger(
          ruleId = rule.id,
          target = rule.target,
          stationId = station.id,
          stationName = station.name,
          cityId = rule.target.cityId,
          condition = rule.condition,
          triggeredAtEpoch = nowEpoch,
          bikesAvailable = station.bikesAvailable,
          docksAvailable = station.slotsFree,
        )
      }

      updatedRules += rule.copy(
        lastTriggeredEpoch = if (shouldTrigger) nowEpoch else rule.lastTriggeredEpoch,
        lastConditionMatched = matches,
      )
    }

    return SavedPlaceAlertEvaluation(
      updatedRules = updatedRules,
      triggers = triggers,
    )
  }
}

data class SavedPlaceAlertEvaluation(
  val updatedRules: List<SavedPlaceAlertRule>,
  val triggers: List<SavedPlaceAlertTrigger>,
)

fun SavedPlaceAlertCondition.displayTitle(): String = when (this) {
  is SavedPlaceAlertCondition.BikesAtLeast -> "Bicis disponibles"
  is SavedPlaceAlertCondition.DocksAtLeast -> "Huecos disponibles"
  SavedPlaceAlertCondition.BikesEqualsZero -> "Sin bicis"
  SavedPlaceAlertCondition.DocksEqualsZero -> "Sin huecos"
}

fun SavedPlaceAlertCondition.displayDescription(): String = when (this) {
  is SavedPlaceAlertCondition.BikesAtLeast -> "Avisar cuando haya al menos $count bicis"
  is SavedPlaceAlertCondition.DocksAtLeast -> "Avisar cuando haya al menos $count huecos"
  SavedPlaceAlertCondition.BikesEqualsZero -> "Avisar cuando se quede sin bicis"
  SavedPlaceAlertCondition.DocksEqualsZero -> "Avisar cuando se quede sin huecos"
}

fun SavedPlaceAlertTrigger.notificationTitle(): String = when (condition) {
  is SavedPlaceAlertCondition.BikesAtLeast -> "$stationName ya tiene bicis"
  is SavedPlaceAlertCondition.DocksAtLeast -> "$stationName ya tiene huecos"
  SavedPlaceAlertCondition.BikesEqualsZero -> "$stationName se ha quedado sin bicis"
  SavedPlaceAlertCondition.DocksEqualsZero -> "$stationName se ha quedado sin huecos"
}

fun SavedPlaceAlertTrigger.notificationBody(): String = when (condition) {
  is SavedPlaceAlertCondition.BikesAtLeast -> "$bikesAvailable bicis · $docksAvailable huecos"
  is SavedPlaceAlertCondition.DocksAtLeast -> "$bikesAvailable bicis · $docksAvailable huecos"
  SavedPlaceAlertCondition.BikesEqualsZero -> "$docksAvailable huecos libres"
  SavedPlaceAlertCondition.DocksEqualsZero -> "$bikesAvailable bicis disponibles"
}

@Serializable
private data class SavedPlaceAlertsSnapshot(
  val rules: List<SavedPlaceAlertRule> = emptyList(),
)
