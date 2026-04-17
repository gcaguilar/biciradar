package com.gcaguilar.biciradar.core

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.gcaguilar.biciradar.core.geo.currentTimeMs
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

  suspend fun upsertRule(
    target: SavedPlaceAlertTarget,
    condition: SavedPlaceAlertCondition,
    enabled: Boolean = true,
  )

  suspend fun removeRule(ruleId: String)

  suspend fun removeRuleForTarget(target: SavedPlaceAlertTarget)

  suspend fun removeRulesForCity(cityId: String)

  suspend fun setRuleEnabled(
    ruleId: String,
    enabled: Boolean,
  )

  suspend fun replaceAll(rules: List<SavedPlaceAlertRule>)
}

/**
 * Implementación de SavedPlaceAlertsRepository.
 * Registrado automáticamente en el grafo vía @ContributesBinding.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class SavedPlaceAlertsRepositoryImpl(
  private val fileSystem: FileSystem,
  private val json: Json,
  private val storageDirectoryProvider: StorageDirectoryProvider,
  private val scope: CoroutineScope,
  private val database: BiciRadarDatabase? = null,
) : SavedPlaceAlertsRepository {
  private val mutableRules = MutableStateFlow<List<SavedPlaceAlertRule>>(emptyList())
  @Volatile private var bootstrapped = false

  override val rules: StateFlow<List<SavedPlaceAlertRule>> = mutableRules.asStateFlow()

  init {
    database?.let { db ->
      scope.launch {
        db.biciradarQueries
          .getAllSavedPlaceAlertRules()
          .asFlow()
          .mapToList(Dispatchers.Default)
          .collect { rows ->
            mutableRules.value = rows.mapNotNull { it.toRule() }.sortedBy { it.id }
          }
      }
    }
  }

  override suspend fun bootstrap() {
    if (bootstrapped) return
    mutableRules.value = readPersistedRules().sortedBy { it.id }
    bootstrapped = true
  }

  override fun currentRules(): List<SavedPlaceAlertRule> = mutableRules.value

  override fun ruleForTarget(target: SavedPlaceAlertTarget): SavedPlaceAlertRule? =
    findSavedPlaceAlertRule(mutableRules.value, target)

  override suspend fun upsertRule(
    target: SavedPlaceAlertTarget,
    condition: SavedPlaceAlertCondition,
    enabled: Boolean,
  ) {
    if (!bootstrapped) bootstrap()
    val updated =
      mutableRules.value.filterNot { it.target.identityKey() == target.identityKey() } +
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

  override suspend fun removeRulesForCity(cityId: String) {
    if (!bootstrapped) bootstrap()
    persist(mutableRules.value.filterNot { rule -> rule.target.cityId == cityId })
  }

  override suspend fun setRuleEnabled(
    ruleId: String,
    enabled: Boolean,
  ) {
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
      if (persistToDatabase(rules)) {
        deleteLegacyFile()
      }
    } else {
      persistToFile(rules)
      mutableRules.value = rules
    }
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
      db.biciradarQueries
        .getAllSavedPlaceAlertRules()
        .executeAsList()
        .mapNotNull { it.toRule() }
    }.getOrNull()
  }

  private fun persistToDatabase(rules: List<SavedPlaceAlertRule>): Boolean {
    val db = database ?: return false
    return runCatching {
      db.transaction {
        db.biciradarQueries.deleteAllSavedPlaceAlertRules()
        rules.forEach { rule ->
          val params = rule.toRelationalRow()
          db.biciradarQueries.upsertSavedPlaceAlertRule(
            id = params.id,
            targetKind = params.targetKind,
            targetStationId = params.targetStationId,
            targetCityId = params.targetCityId,
            targetStationName = params.targetStationName,
            targetCategoryId = params.targetCategoryId,
            targetCategoryLabel = params.targetCategoryLabel,
            conditionKind = params.conditionKind,
            conditionThreshold = params.conditionThreshold,
            isEnabled = if (rule.isEnabled) 1L else 0L,
            lastTriggeredEpoch = rule.lastTriggeredEpoch,
            lastConditionMatched = if (rule.lastConditionMatched) 1L else 0L,
            lastObservedValue = rule.lastObservedValue?.toLong(),
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
        triggers +=
          SavedPlaceAlertTrigger(
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

      updatedRules +=
        rule.copy(
          lastObservedValue = rule.metricValue(station),
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

fun SavedPlaceAlertCondition.displayTitle(): String =
  when (this) {
    is SavedPlaceAlertCondition.BikesAtLeast -> "Bicis disponibles"
    is SavedPlaceAlertCondition.DocksAtLeast -> "Huecos disponibles"
    SavedPlaceAlertCondition.BikesEqualsZero -> "Sin bicis"
    SavedPlaceAlertCondition.DocksEqualsZero -> "Sin huecos"
  }

fun SavedPlaceAlertCondition.displayDescription(): String =
  when (this) {
    is SavedPlaceAlertCondition.BikesAtLeast -> "Avisar cuando haya al menos $count bicis"
    is SavedPlaceAlertCondition.DocksAtLeast -> "Avisar cuando haya al menos $count huecos"
    SavedPlaceAlertCondition.BikesEqualsZero -> "Avisar cuando se quede sin bicis"
    SavedPlaceAlertCondition.DocksEqualsZero -> "Avisar cuando se quede sin huecos"
  }

fun SavedPlaceAlertTrigger.notificationTitle(): String =
  when (condition) {
    is SavedPlaceAlertCondition.BikesAtLeast -> "$stationName ya tiene bicis"
    is SavedPlaceAlertCondition.DocksAtLeast -> "$stationName ya tiene huecos"
    SavedPlaceAlertCondition.BikesEqualsZero -> "$stationName se ha quedado sin bicis"
    SavedPlaceAlertCondition.DocksEqualsZero -> "$stationName se ha quedado sin huecos"
  }

fun SavedPlaceAlertTrigger.notificationBody(): String =
  when (condition) {
    is SavedPlaceAlertCondition.BikesAtLeast -> "$bikesAvailable bicis · $docksAvailable huecos"
    is SavedPlaceAlertCondition.DocksAtLeast -> "$bikesAvailable bicis · $docksAvailable huecos"
    SavedPlaceAlertCondition.BikesEqualsZero -> "$docksAvailable huecos libres"
    SavedPlaceAlertCondition.DocksEqualsZero -> "$bikesAvailable bicis disponibles"
  }

@Serializable
private data class SavedPlaceAlertsSnapshot(
  val rules: List<SavedPlaceAlertRule> = emptyList(),
)

private data class RelationalAlertRow(
  val id: String,
  val targetKind: String,
  val targetStationId: String,
  val targetCityId: String,
  val targetStationName: String?,
  val targetCategoryId: String?,
  val targetCategoryLabel: String?,
  val conditionKind: String,
  val conditionThreshold: Long?,
)

private fun SavedPlaceAlertRule.toRelationalRow(): RelationalAlertRow {
  val cat = target as? SavedPlaceAlertTarget.CategoryStation
  return RelationalAlertRow(
    id = id,
    targetKind = target.kind.name,
    targetStationId = target.stationId,
    targetCityId = target.cityId,
    targetStationName = target.stationName,
    targetCategoryId = cat?.categoryId,
    targetCategoryLabel = cat?.categoryLabel,
    conditionKind = condition.toKindString(),
    conditionThreshold = condition.thresholdOrNull(),
  )
}

private fun SavedPlaceAlertCondition.toKindString(): String =
  when (this) {
    is SavedPlaceAlertCondition.BikesAtLeast -> "BikesAtLeast"
    is SavedPlaceAlertCondition.DocksAtLeast -> "DocksAtLeast"
    SavedPlaceAlertCondition.BikesEqualsZero -> "BikesEqualsZero"
    SavedPlaceAlertCondition.DocksEqualsZero -> "DocksEqualsZero"
  }

private fun SavedPlaceAlertCondition.thresholdOrNull(): Long? =
  when (this) {
    is SavedPlaceAlertCondition.BikesAtLeast -> count.toLong()
    is SavedPlaceAlertCondition.DocksAtLeast -> count.toLong()
    else -> null
  }

@Suppress("ComplexMethod")
private fun Saved_place_alert_rules.toRule(): SavedPlaceAlertRule? {
  val kind = runCatching { SavedPlaceKind.valueOf(target_kind) }.getOrNull() ?: return null
  val target: SavedPlaceAlertTarget =
    when (kind) {
      SavedPlaceKind.Favorite ->
        SavedPlaceAlertTarget.FavoriteStation(
          stationId = target_station_id,
          cityId = target_city_id,
          stationName = target_station_name,
        )
      SavedPlaceKind.Home ->
        SavedPlaceAlertTarget.Home(
          stationId = target_station_id,
          cityId = target_city_id,
          stationName = target_station_name,
        )
      SavedPlaceKind.Work ->
        SavedPlaceAlertTarget.Work(
          stationId = target_station_id,
          cityId = target_city_id,
          stationName = target_station_name,
        )
      SavedPlaceKind.Category ->
        SavedPlaceAlertTarget.CategoryStation(
          stationId = target_station_id,
          cityId = target_city_id,
          stationName = target_station_name,
          categoryId = target_category_id ?: return null,
          categoryLabel = target_category_label,
        )
    }
  val condition =
    when (condition_kind) {
      "BikesAtLeast" -> SavedPlaceAlertCondition.BikesAtLeast(condition_threshold?.toInt() ?: return null)
      "DocksAtLeast" -> SavedPlaceAlertCondition.DocksAtLeast(condition_threshold?.toInt() ?: return null)
      "BikesEqualsZero" -> SavedPlaceAlertCondition.BikesEqualsZero
      "DocksEqualsZero" -> SavedPlaceAlertCondition.DocksEqualsZero
      else -> return null
    }
  return SavedPlaceAlertRule(
    id = id,
    target = target,
    condition = condition,
    isEnabled = is_enabled != 0L,
    lastTriggeredEpoch = last_triggered_epoch,
    lastObservedValue = last_observed_value?.toInt(),
    lastConditionMatched = last_condition_matched != 0L,
  )
}
