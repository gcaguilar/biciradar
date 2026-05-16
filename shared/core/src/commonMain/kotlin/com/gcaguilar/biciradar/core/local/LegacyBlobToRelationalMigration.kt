package com.gcaguilar.biciradar.core.local

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.gcaguilar.biciradar.core.App_settings
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.SavedPlaceKind
import com.gcaguilar.biciradar.core.SettingsSnapshot
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.SurfaceMonitoringStatus
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle
import com.gcaguilar.biciradar.core.SurfaceState
import com.gcaguilar.biciradar.core.SurfaceStationSnapshot
import com.gcaguilar.biciradar.core.SurfaceStatusLevel
import com.gcaguilar.biciradar.core.Surface_header
import com.gcaguilar.biciradar.core.Surface_monitoring
import com.gcaguilar.biciradar.core.Surface_station_row
import kotlinx.serialization.json.Json

/**
 * One-time migration from JSON blob tables / legacy alert columns to relational schema.
 * Idempotent: safe on every process start.
 */
object LegacyBlobToRelationalMigration {
  fun ensure(
    driver: SqlDriver,
    database: BiciRadarDatabase,
    json: Json,
  ) {
    CORE_RELATIONAL_DDL.forEach { sql -> driver.execute(null, sql, 0) }
    database.transaction {
      migrateSavedPlaceAlertsIfBlobgy(driver, json, database)
      migrateSettingsBlobIfPresent(driver, json, database)
      migrateSurfaceBlobIfPresent(driver, json, database)
    }
  }

  private fun createSavedPlaceTableIfMissing(driver: SqlDriver) {
    driver.execute(null, SAVED_PLACE_ALERT_RULES_DDL, 0)
  }

  private fun migrateSettingsBlobIfPresent(
    driver: SqlDriver,
    json: Json,
    db: BiciRadarDatabase,
  ) {
    val blobJson =
      readSingleStringColumn(driver, "settings_snapshot", "snapshot_json")
        ?: readSingleStringColumn(driver, "settings_snapshot_legacy", "snapshot_json")
        ?: return
    val decoded = runCatching { json.decodeFromString<SettingsSnapshot>(blobJson) }.getOrNull() ?: return
    upsertSettingsFromSnapshot(db, normalizeLegacyOnboardingForMigration(decoded))
    driver.execute(null, "DROP TABLE IF EXISTS settings_snapshot", 0)
    driver.execute(null, "DROP TABLE IF EXISTS settings_snapshot_legacy", 0)
  }

  private fun migrateSurfaceBlobIfPresent(
    driver: SqlDriver,
    json: Json,
    db: BiciRadarDatabase,
  ) {
    val blobJson =
      readSingleStringColumn(driver, "surface_snapshot_bundle", "bundle_json")
        ?: readSingleStringColumn(driver, "surface_snapshot_bundle_legacy", "bundle_json")
        ?: return
    val bundle = runCatching { json.decodeFromString<SurfaceSnapshotBundle>(blobJson) }.getOrNull() ?: return
    persistSurfaceBundle(db, bundle)
    driver.execute(null, "DROP TABLE IF EXISTS surface_snapshot_bundle", 0)
    driver.execute(null, "DROP TABLE IF EXISTS surface_snapshot_bundle_legacy", 0)
  }

  private fun migrateSavedPlaceAlertsIfBlobgy(
    driver: SqlDriver,
    json: Json,
    db: BiciRadarDatabase,
  ) {
    if (!tableExists(driver, "saved_place_alert_rules")) return
    if (!savedPlaceAlertsHasTargetJsonColumn(driver)) return
    val rows = readLegacyAlertRows(driver)
    driver.execute(null, "ALTER TABLE saved_place_alert_rules RENAME TO saved_place_alert_rules_legacy", 0)
    createSavedPlaceTableIfMissing(driver)
    rows.forEach { legacy ->
      val target =
        runCatching { json.decodeFromString<SavedPlaceAlertTarget>(legacy.targetJson) }.getOrNull() ?: return@forEach
      val condition =
        runCatching { json.decodeFromString<SavedPlaceAlertCondition>(legacy.conditionJson) }.getOrNull()
          ?: return@forEach
      val rule =
        SavedPlaceAlertRule(
          id = legacy.id,
          target = target,
          condition = condition,
          isEnabled = legacy.isEnabled,
          lastTriggeredEpoch = legacy.lastTriggeredEpoch,
          lastObservedValue = null,
          lastConditionMatched = legacy.lastConditionMatched,
        )
      db.biciradarQueries.upsertSavedPlaceAlertRule(
        id = rule.id,
        targetKind = savedPlaceKindToDb(rule.target.kind),
        targetStationId = rule.target.stationId,
        targetCityId = rule.target.cityId,
        targetStationName = rule.target.stationName,
        targetCategoryId = (rule.target as? SavedPlaceAlertTarget.CategoryStation)?.categoryId,
        targetCategoryLabel = (rule.target as? SavedPlaceAlertTarget.CategoryStation)?.categoryLabel,
        conditionKind = conditionToKind(rule.condition),
        conditionThreshold = conditionToThreshold(rule.condition),
        isEnabled = if (rule.isEnabled) 1L else 0L,
        lastTriggeredEpoch = rule.lastTriggeredEpoch,
        lastConditionMatched = if (rule.lastConditionMatched) 1L else 0L,
        lastObservedValue = rule.lastObservedValue?.toLong(),
      )
    }
    driver.execute(null, "DROP TABLE IF EXISTS saved_place_alert_rules_legacy", 0)
  }

  private data class LegacyAlertRow(
    val id: String,
    val targetJson: String,
    val conditionJson: String,
    val isEnabled: Boolean,
    val lastTriggeredEpoch: Long?,
    val lastConditionMatched: Boolean,
  )

  private fun readLegacyAlertRows(driver: SqlDriver): List<LegacyAlertRow> =
    driver
      .executeQuery(
        null,
        "SELECT id, target_json, condition_json, is_enabled, last_triggered_epoch, last_condition_matched FROM saved_place_alert_rules",
        { cursor -> QueryResult.Value(readAllLegacyAlertRows(cursor)) },
        0,
      ).expectValue()

  private fun readAllLegacyAlertRows(cursor: SqlCursor): List<LegacyAlertRow> {
    val out = mutableListOf<LegacyAlertRow>()
    while (cursor.next().value) {
      out +=
        LegacyAlertRow(
          id = cursor.getString(0)!!,
          targetJson = cursor.getString(1)!!,
          conditionJson = cursor.getString(2)!!,
          isEnabled = cursor.getLong(3)!! != 0L,
          lastTriggeredEpoch = cursor.getLong(4),
          lastConditionMatched = cursor.getLong(5)!! != 0L,
        )
    }
    return out
  }

  private fun readSingleStringColumn(
    driver: SqlDriver,
    table: String,
    column: String,
  ): String? {
    if (!tableExists(driver, table)) return null
    return driver
      .executeQuery(
        null,
        "SELECT $column FROM $table WHERE id = 1 LIMIT 1",
        { cursor ->
          val v = if (cursor.next().value) cursor.getString(0) else null
          QueryResult.Value(v)
        },
        0,
      ).expectValue()
  }

  private fun tableExists(
    driver: SqlDriver,
    name: String,
  ): Boolean =
    driver
      .executeQuery(
        null,
        "SELECT 1 FROM sqlite_master WHERE type='table' AND name=? LIMIT 1",
        { cursor -> QueryResult.Value(cursor.next().value) },
        1,
      ) {
        bindString(0, name)
      }.expectValue()

  private fun savedPlaceAlertsHasTargetJsonColumn(driver: SqlDriver): Boolean =
    driver
      .executeQuery(
        null,
        "SELECT 1 FROM pragma_table_info('saved_place_alert_rules') WHERE name='target_json' LIMIT 1",
        { cursor -> QueryResult.Value(cursor.next().value) },
        0,
      ).expectValue()
}

private fun <T> QueryResult<T>.expectValue(): T =
  when (this) {
    is QueryResult.Value -> value
    is QueryResult.AsyncValue ->
      error("Legacy migration requires a synchronous SQLite driver (got async query result)")
  }

internal fun normalizeLegacyOnboardingForMigration(snapshot: SettingsSnapshot): SettingsSnapshot =
  DefaultSettingsAdapter().normalizeLegacyOnboarding(snapshot)

internal fun settingsSnapshotFromDbRow(
  row: App_settings,
  mapFilterNames: Set<String>,
): SettingsSnapshot = DefaultSettingsAdapter().toSnapshot(row, mapFilterNames)

@Suppress("LongMethod")
internal fun upsertSettingsFromSnapshot(
  db: BiciRadarDatabase,
  snapshot: SettingsSnapshot,
) {
  val args = DefaultSettingsAdapter().toUpsertArgs(snapshot)
  db.biciradarQueries.upsertAppSettings(
    id = args.id,
    searchRadiusMeters = args.searchRadiusMeters,
    preferredMapApp = args.preferredMapApp,
    lastSeenChangelogVersion = args.lastSeenChangelogVersion,
    lastSeenChangelogAppVersion = args.lastSeenChangelogAppVersion,
    themePreference = args.themePreference,
    selectedCityId = args.selectedCityId,
    hasCompletedOnboarding = args.hasCompletedOnboarding,
    onboardingCityConfirmed = args.onboardingCityConfirmed,
    onboardingFeatureHighlightsSeen = args.onboardingFeatureHighlightsSeen,
    onboardingLocationDecisionMade = args.onboardingLocationDecisionMade,
    onboardingNotificationsDecisionMade = args.onboardingNotificationsDecisionMade,
    onboardingFirstStationSaved = args.onboardingFirstStationSaved,
    onboardingSavedPlacesConfigured = args.onboardingSavedPlacesConfigured,
    onboardingSurfacesDiscovered = args.onboardingSurfacesDiscovered,
    onboardingCompletedAtEpoch = args.onboardingCompletedAtEpoch,
    engagementInstalledAtEpoch = args.engagementInstalledAtEpoch,
    engagementLastSessionEpoch = args.engagementLastSessionEpoch,
    engagementUsefulSessionsCount = args.engagementUsefulSessionsCount,
    engagementFavoritesSavedCount = args.engagementFavoritesSavedCount,
    engagementRoutesOpenedCount = args.engagementRoutesOpenedCount,
    engagementMonitoringsCompletedCount = args.engagementMonitoringsCompletedCount,
    engagementRepeatedErrorCount = args.engagementRepeatedErrorCount,
    engagementExpiredDataEventsCount = args.engagementExpiredDataEventsCount,
    engagementUnavailableDataEventsCount = args.engagementUnavailableDataEventsCount,
    engagementLastFeedbackNudgeAtEpoch = args.engagementLastFeedbackNudgeAtEpoch,
    engagementLastFeedbackDismissedAtEpoch = args.engagementLastFeedbackDismissedAtEpoch,
    engagementLastFeedbackOpenedAtEpoch = args.engagementLastFeedbackOpenedAtEpoch,
    engagementLastFeedbackNudgedVersion = args.engagementLastFeedbackNudgedVersion,
    engagementLastReviewRequestedAtEpoch = args.engagementLastReviewRequestedAtEpoch,
    engagementLastReviewRequestedVersion = args.engagementLastReviewRequestedVersion,
    engagementDismissedUpdateVersion = args.engagementDismissedUpdateVersion,
    engagementLastUpdateCheckAtEpoch = args.engagementLastUpdateCheckAtEpoch,
    engagementLastUpdateBannerDismissedAtEpoch = args.engagementLastUpdateBannerDismissedAtEpoch,
    preferredMonitoringDurationSeconds = args.preferredMonitoringDurationSeconds,
  )
  db.biciradarQueries.deleteAllSettingsMapFilterNames()
  snapshot.mapFilterNames.forEach { name: String ->
    db.biciradarQueries.insertSettingsMapFilterName(name)
  }
}

private fun persistSurfaceBundle(
  db: BiciRadarDatabase,
  bundle: SurfaceSnapshotBundle,
) {
  val st = bundle.state
  db.biciradarQueries.deleteAllSurfaceStationRows()
  db.biciradarQueries.upsertSurfaceHeader(
    id = 1L,
    generatedAtEpoch = bundle.generatedAtEpoch,
    stateHasLocation = if (st.hasLocationPermission) 1L else 0L,
    stateHasNotifications = if (st.hasNotificationPermission) 1L else 0L,
    stateHasFavorite = if (st.hasFavoriteStation) 1L else 0L,
    stateIsDataFresh = if (st.isDataFresh) 1L else 0L,
    stateLastSyncEpoch = st.lastSyncEpoch,
    stateCityId = st.cityId,
    stateCityName = st.cityName,
    stateUserLatitude = st.userLatitude,
    stateUserLongitude = st.userLongitude,
  )
  bundle.favoriteStation?.let { insertStationRow(db, "favorite", 0, it) }
  bundle.homeStation?.let { insertStationRow(db, "home", 0, it) }
  bundle.workStation?.let { insertStationRow(db, "work", 0, it) }
  bundle.nearbyStations.forEachIndexed { index, snap ->
    insertStationRow(db, "nearby", index, snap)
  }
  val session = bundle.monitoringSession
  if (session == null) {
    db.biciradarQueries.clearSurfaceMonitoringRow()
  } else {
    db.biciradarQueries.upsertSurfaceMonitoring(
      id = 1L,
      stationId = session.stationId,
      stationName = session.stationName,
      cityId = session.cityId,
      kind = session.kind.name,
      status = session.status.name,
      bikesAvailable = session.bikesAvailable.toLong(),
      docksAvailable = session.docksAvailable.toLong(),
      statusLevel = session.statusLevel.name,
      startedAtEpoch = session.startedAtEpoch,
      expiresAtEpoch = session.expiresAtEpoch,
      lastUpdatedEpoch = session.lastUpdatedEpoch,
      isActive = if (session.isActive) 1L else 0L,
      alternativeStationId = session.alternativeStationId,
      alternativeStationName = session.alternativeStationName,
      alternativeDistanceMeters = session.alternativeDistanceMeters?.toLong(),
    )
  }
}

private fun insertStationRow(
  db: BiciRadarDatabase,
  role: String,
  slotIndex: Int,
  s: SurfaceStationSnapshot,
) {
  db.biciradarQueries.insertSurfaceStationRow(
    role = role,
    slotIndex = slotIndex.toLong(),
    stationId = s.id,
    nameShort = s.nameShort,
    nameFull = s.nameFull,
    cityId = s.cityId,
    latitude = s.latitude,
    longitude = s.longitude,
    bikesAvailable = s.bikesAvailable.toLong(),
    docksAvailable = s.docksAvailable.toLong(),
    statusTextShort = s.statusTextShort,
    statusLevel = s.statusLevel.name,
    lastUpdatedEpoch = s.lastUpdatedEpoch,
    distanceMeters = s.distanceMeters?.toLong(),
    isFavorite = if (s.isFavorite) 1L else 0L,
    alternativeStationId = s.alternativeStationId,
    alternativeStationName = s.alternativeStationName,
    alternativeDistanceMeters = s.alternativeDistanceMeters?.toLong(),
  )
}

internal fun loadSurfaceBundleFromDb(db: BiciRadarDatabase): SurfaceSnapshotBundle? {
  val header = db.biciradarQueries.getSurfaceHeader().executeAsOneOrNull() ?: return null
  val rows = db.biciradarQueries.getAllSurfaceStationRows().executeAsList()
  val monRow = db.biciradarQueries.getSurfaceMonitoring().executeAsOneOrNull()
  return surfaceBundleFromRows(header, rows, monRow)
}

internal fun surfaceBundleFromRows(
  header: Surface_header,
  rows: List<Surface_station_row>,
  monRow: Surface_monitoring?,
): SurfaceSnapshotBundle? {
  if (rows.isEmpty() && monRow?.station_id == null) {
    return null
  }
  val byRole = rows.groupBy { it.role }

  fun mapRow(r: Surface_station_row) =
    SurfaceStationSnapshot(
      id = r.station_id,
      nameShort = r.name_short,
      nameFull = r.name_full,
      cityId = r.city_id,
      latitude = r.latitude,
      longitude = r.longitude,
      distanceMeters = r.distance_meters?.toInt(),
      bikesAvailable = r.bikes_available.toInt(),
      docksAvailable = r.docks_available.toInt(),
      statusTextShort = r.status_text_short,
      statusLevel = SurfaceStatusLevel.valueOf(r.status_level),
      lastUpdatedEpoch = r.last_updated_epoch,
      isFavorite = r.is_favorite != 0L,
      alternativeStationId = r.alternative_station_id,
      alternativeStationName = r.alternative_station_name,
      alternativeDistanceMeters = r.alternative_distance_meters?.toInt(),
    )
  val favorite = byRole["favorite"]?.firstOrNull()?.let(::mapRow)
  val home = byRole["home"]?.firstOrNull()?.let(::mapRow)
  val work = byRole["work"]?.firstOrNull()?.let(::mapRow)
  val nearby = byRole["nearby"].orEmpty().sortedBy { it.slot_index }.map(::mapRow)
  val monitoring =
    monRow?.station_id?.let { sid ->
      SurfaceMonitoringSession(
        stationId = sid,
        stationName = monRow.station_name!!,
        cityId = monRow.city_id!!,
        kind = SurfaceMonitoringKind.valueOf(monRow.kind!!),
        status = SurfaceMonitoringStatus.valueOf(monRow.status!!),
        bikesAvailable = monRow.bikes_available!!.toInt(),
        docksAvailable = monRow.docks_available!!.toInt(),
        statusLevel = SurfaceStatusLevel.valueOf(monRow.status_level!!),
        startedAtEpoch = monRow.started_at_epoch!!,
        expiresAtEpoch = monRow.expires_at_epoch!!,
        lastUpdatedEpoch = monRow.last_updated_epoch!!,
        isActive = monRow.is_active!! != 0L,
        alternativeStationId = monRow.alternative_station_id,
        alternativeStationName = monRow.alternative_station_name,
        alternativeDistanceMeters = monRow.alternative_distance_meters?.toInt(),
      )
    }
  return SurfaceSnapshotBundle(
    generatedAtEpoch = header.generated_at_epoch,
    favoriteStation = favorite,
    homeStation = home,
    workStation = work,
    nearbyStations = nearby,
    monitoringSession = monitoring,
    state =
      SurfaceState(
        hasLocationPermission = header.state_has_location != 0L,
        hasNotificationPermission = header.state_has_notifications != 0L,
        hasFavoriteStation = header.state_has_favorite != 0L,
        isDataFresh = header.state_is_data_fresh != 0L,
        lastSyncEpoch = header.state_last_sync_epoch,
        cityId = header.state_city_id,
        cityName = header.state_city_name,
        userLatitude = header.state_user_latitude,
        userLongitude = header.state_user_longitude,
      ),
  )
}

internal fun persistSurfaceBundleRelational(
  db: BiciRadarDatabase,
  bundle: SurfaceSnapshotBundle,
) {
  persistSurfaceBundle(db, bundle)
}

private fun savedPlaceKindToDb(kind: SavedPlaceKind): String = kind.name

private fun conditionToKind(c: SavedPlaceAlertCondition): String =
  when (c) {
    is SavedPlaceAlertCondition.BikesAtLeast -> "BikesAtLeast"
    is SavedPlaceAlertCondition.DocksAtLeast -> "DocksAtLeast"
    SavedPlaceAlertCondition.BikesEqualsZero -> "BikesEqualsZero"
    SavedPlaceAlertCondition.DocksEqualsZero -> "DocksEqualsZero"
  }

private fun conditionToThreshold(c: SavedPlaceAlertCondition): Long? =
  when (c) {
    is SavedPlaceAlertCondition.BikesAtLeast -> c.count.toLong()
    is SavedPlaceAlertCondition.DocksAtLeast -> c.count.toLong()
    else -> null
  }

private val SAVED_PLACE_ALERT_RULES_DDL: String =
  """
  CREATE TABLE IF NOT EXISTS saved_place_alert_rules (
    id TEXT PRIMARY KEY NOT NULL,
    target_kind TEXT NOT NULL,
    target_station_id TEXT NOT NULL,
    target_city_id TEXT NOT NULL,
    target_station_name TEXT,
    target_category_id TEXT,
    target_category_label TEXT,
    condition_kind TEXT NOT NULL,
    condition_threshold INTEGER,
    is_enabled INTEGER NOT NULL,
    last_triggered_epoch INTEGER,
    last_condition_matched INTEGER NOT NULL,
    last_observed_value INTEGER
  )
  """.trimIndent().replace("\n", " ")

private val CORE_RELATIONAL_DDL: List<String> =
  listOf(
    """
    CREATE TABLE IF NOT EXISTS stations (
      id TEXT PRIMARY KEY NOT NULL,
      name TEXT NOT NULL,
      address TEXT,
      latitude REAL NOT NULL,
      longitude REAL NOT NULL,
      bikes_available INTEGER NOT NULL,
      slots_free INTEGER NOT NULL,
      ebikes_available INTEGER NOT NULL DEFAULT 0,
      regular_bikes_available INTEGER NOT NULL DEFAULT 0,
      updated_at INTEGER NOT NULL
    )
    """.trimIndent().replace("\n", " "),
    """
    CREATE TABLE IF NOT EXISTS cache_metadata (
      city_id TEXT PRIMARY KEY NOT NULL,
      last_updated INTEGER NOT NULL
    )
    """.trimIndent().replace("\n", " "),
    """
    CREATE TABLE IF NOT EXISTS environmental_cache (
      zone_key TEXT PRIMARY KEY NOT NULL,
      air_quality_index INTEGER,
      pollen_index INTEGER,
      updated_at INTEGER NOT NULL
    )
    """.trimIndent().replace("\n", " "),
    """
    CREATE TABLE IF NOT EXISTS favorite_ids (
      station_id TEXT PRIMARY KEY NOT NULL
    )
    """.trimIndent().replace("\n", " "),
    """
    CREATE TABLE IF NOT EXISTS favorite_roles (
      id INTEGER PRIMARY KEY NOT NULL,
      home_station_id TEXT,
      work_station_id TEXT
    )
    """.trimIndent().replace("\n", " "),
    """
    CREATE TABLE IF NOT EXISTS favorite_categories (
      id TEXT PRIMARY KEY NOT NULL,
      label TEXT NOT NULL,
      is_system INTEGER NOT NULL DEFAULT 0
    )
    """.trimIndent().replace("\n", " "),
    """
    CREATE TABLE IF NOT EXISTS favorite_station_category (
      station_id TEXT PRIMARY KEY NOT NULL,
      category_id TEXT NOT NULL
    )
    """.trimIndent().replace("\n", " "),
    SAVED_PLACE_ALERT_RULES_DDL,
    """
    CREATE TABLE IF NOT EXISTS app_settings (
      id INTEGER PRIMARY KEY NOT NULL,
      search_radius_meters INTEGER NOT NULL,
      preferred_map_app TEXT NOT NULL,
      last_seen_changelog_version INTEGER NOT NULL,
      last_seen_changelog_app_version TEXT,
      theme_preference TEXT NOT NULL,
      selected_city_id TEXT NOT NULL,
      has_completed_onboarding INTEGER NOT NULL,
      onboarding_city_confirmed INTEGER NOT NULL,
      onboarding_feature_highlights_seen INTEGER NOT NULL,
      onboarding_location_decision_made INTEGER NOT NULL,
      onboarding_notifications_decision_made INTEGER NOT NULL,
      onboarding_first_station_saved INTEGER NOT NULL,
      onboarding_saved_places_configured INTEGER NOT NULL,
      onboarding_surfaces_discovered INTEGER NOT NULL,
      onboarding_completed_at_epoch INTEGER,
      engagement_installed_at_epoch INTEGER NOT NULL DEFAULT 0,
      engagement_last_session_epoch INTEGER,
      engagement_useful_sessions_count INTEGER NOT NULL DEFAULT 0,
      engagement_favorites_saved_count INTEGER NOT NULL DEFAULT 0,
      engagement_routes_opened_count INTEGER NOT NULL DEFAULT 0,
      engagement_monitorings_completed_count INTEGER NOT NULL DEFAULT 0,
      engagement_repeated_error_count INTEGER NOT NULL DEFAULT 0,
      engagement_expired_data_events_count INTEGER NOT NULL DEFAULT 0,
      engagement_unavailable_data_events_count INTEGER NOT NULL DEFAULT 0,
      engagement_last_feedback_nudge_at_epoch INTEGER,
      engagement_last_feedback_dismissed_at_epoch INTEGER,
      engagement_last_feedback_opened_at_epoch INTEGER,
      engagement_last_feedback_nudged_version TEXT,
      engagement_last_review_requested_at_epoch INTEGER,
      engagement_last_review_requested_version TEXT,
      engagement_dismissed_update_version TEXT,
      engagement_last_update_check_at_epoch INTEGER,
      engagement_last_update_banner_dismissed_at_epoch INTEGER,
      preferred_monitoring_duration_seconds INTEGER
    )
    """.trimIndent().replace("\n", " "),
    """
    CREATE TABLE IF NOT EXISTS settings_map_filter_name (
      name TEXT PRIMARY KEY NOT NULL
    )
    """.trimIndent().replace("\n", " "),
    """
    CREATE TABLE IF NOT EXISTS surface_header (
      id INTEGER PRIMARY KEY NOT NULL,
      generated_at_epoch INTEGER NOT NULL,
      state_has_location INTEGER NOT NULL,
      state_has_notifications INTEGER NOT NULL,
      state_has_favorite INTEGER NOT NULL,
      state_is_data_fresh INTEGER NOT NULL,
      state_last_sync_epoch INTEGER,
      state_city_id TEXT NOT NULL,
      state_city_name TEXT NOT NULL,
      state_user_latitude REAL,
      state_user_longitude REAL
    )
    """.trimIndent().replace("\n", " "),
    """
    CREATE TABLE IF NOT EXISTS surface_station_row (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      role TEXT NOT NULL,
      slot_index INTEGER NOT NULL DEFAULT 0,
      station_id TEXT NOT NULL,
      name_short TEXT NOT NULL,
      name_full TEXT NOT NULL,
      city_id TEXT NOT NULL,
      latitude REAL NOT NULL,
      longitude REAL NOT NULL,
      bikes_available INTEGER NOT NULL,
      docks_available INTEGER NOT NULL,
      status_text_short TEXT NOT NULL,
      status_level TEXT NOT NULL,
      last_updated_epoch INTEGER NOT NULL,
      distance_meters INTEGER,
      is_favorite INTEGER NOT NULL,
      alternative_station_id TEXT,
      alternative_station_name TEXT,
      alternative_distance_meters INTEGER
    )
    """.trimIndent().replace("\n", " "),
    """
    CREATE TABLE IF NOT EXISTS surface_monitoring (
      id INTEGER PRIMARY KEY NOT NULL,
      station_id TEXT,
      station_name TEXT,
      city_id TEXT,
      kind TEXT,
      status TEXT,
      bikes_available INTEGER,
      docks_available INTEGER,
      status_level TEXT,
      started_at_epoch INTEGER,
      expires_at_epoch INTEGER,
      last_updated_epoch INTEGER,
      is_active INTEGER,
      alternative_station_id TEXT,
      alternative_station_name TEXT,
      alternative_distance_meters INTEGER
    )
    """.trimIndent().replace("\n", " "),
  )
