package com.gcaguilar.biciradar.core

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

private const val WATCH_SYNC_TIMEOUT_MILLIS = 2_000L

/**
 * Contrato base de favoritos.
 *
 * Cubre las operaciones de lectura y mutación fundamentales que todas las
 * implementaciones deben proveer: estado reactivo de favoritos, home/work,
 * toggle y limpieza.
 *
 * Las capacidades opcionales se declaran en interfaces hermanas:
 * - [FavoritesCategorizationCapability] — gestión de categorías personalizadas
 * - [FavoritesPeerSyncCapability]       — sincronización con el reloj (WatchConnectivity)
 *
 * **Identidad de flows**: todas las propiedades `StateFlow` deben ser referencias
 * estables (el mismo objeto en cada acceso). Los getters que creaban un nuevo
 * `MutableStateFlow` por acceso han sido eliminados del interfaz base.
 */
interface FavoritesRepository {
  val favoriteIds: StateFlow<Set<String>>
  val homeStationId: StateFlow<String?>
  val workStationId: StateFlow<String?>

  suspend fun bootstrap()

  suspend fun toggle(stationId: String)

  suspend fun setHomeStationId(stationId: String?)

  suspend fun setWorkStationId(stationId: String?)

  suspend fun clearAll()

  fun isFavorite(stationId: String): Boolean

  fun currentHomeStationId(): String?

  fun currentWorkStationId(): String?
}

/**
 * Capacidad opcional de sincronización con el reloj.
 *
 * Implementada por plataformas con WatchConnectivity (iOS, Android).
 * En watchOS y desktop se usa una implementación no-op explícita.
 */
interface FavoritesPeerSyncCapability {
  /** Sincroniza el estado de favoritos con el dispositivo emparejado. */
  suspend fun syncFromPeer()
}

/**
 * Capacidad opcional de categorización de estaciones favoritas.
 *
 * Las implementaciones que no soporten categorías no deben extender este
 * interface; los consumidores deben hacer cast explícito o usar inyección
 * tipada para acceder a esta funcionalidad.
 *
 * **Identidad de flows**: [categories] y [stationCategory] deben ser
 * referencias estables entre accesos.
 */
interface FavoritesCategorizationCapability {
  val categories: StateFlow<List<FavoriteCategory>>
  val stationCategory: StateFlow<Map<String, String>>

  fun currentCategories(): List<FavoriteCategory> = categories.value

  fun currentStationCategory(stationId: String): String? = stationCategory.value[stationId]

  suspend fun upsertCategory(
    id: String,
    label: String,
    isSystem: Boolean = false,
  )

  suspend fun removeCategory(categoryId: String)

  suspend fun assignStationToCategory(
    stationId: String,
    categoryId: String?,
  )
}

/**
 * Implementación de FavoritesRepository.
 * Registrado automáticamente en el grafo vía @ContributesBinding.
 *
 * Implementa las tres interfaces de favoritos:
 * - [FavoritesRepository]               — contrato base
 * - [FavoritesPeerSyncCapability]        — sincronización con reloj
 * - [FavoritesCategorizationCapability] — categorías personalizadas
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<FavoritesRepository>())
@ContributesBinding(AppScope::class, binding = binding<FavoritesPeerSyncCapability>())
@ContributesBinding(AppScope::class, binding = binding<FavoritesCategorizationCapability>())
@Inject
class FavoritesRepositoryImpl(
  private val fileSystem: FileSystem,
  private val json: Json,
  private val storageDirectoryProvider: StorageDirectoryProvider,
  private val watchSyncBridge: WatchSyncBridge,
  private val scope: CoroutineScope,
  private val database: BiciRadarDatabase? = null,
) : FavoritesRepository,
  FavoritesPeerSyncCapability,
  FavoritesCategorizationCapability {
  private val mutableFavoriteIds = MutableStateFlow(emptySet<String>())
  private val mutableHomeStationId = MutableStateFlow<String?>(null)
  private val mutableWorkStationId = MutableStateFlow<String?>(null)
  private val mutableCategories = MutableStateFlow(systemCategories())
  private val mutableStationCategory = MutableStateFlow<Map<String, String>>(emptyMap())
  @Volatile private var bootstrapped = false
  private val legacyMigrator =
    FavoritesLegacyMigrator(
      fileSystem = fileSystem,
      json = json,
      storageDirectoryProvider = storageDirectoryProvider,
      persistToDatabase = ::persistToDatabase,
    )

  init {
    database?.let { db ->
      scope.launch {
        combine(
          db.biciradarQueries
            .getAllFavoriteCategories()
            .asFlow()
            .mapToList(Dispatchers.Default),
          db.biciradarQueries
            .getAllFavoriteStationCategories()
            .asFlow()
            .mapToList(Dispatchers.Default),
          db.biciradarQueries
            .getAllFavoriteIds()
            .asFlow()
            .mapToList(Dispatchers.Default),
          db.biciradarQueries
            .getFavoriteRoles()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default),
        ) { categoryRows, stationRows, favoriteIdStrings, roles ->
          val categories =
            categoryRows.map {
              FavoriteCategory(
                id = it.id,
                label = it.label,
                isSystem = it.is_system != 0L,
              )
            }
          val stationCategory = stationRows.associate { row -> row.station_id to row.category_id }
          val ids = favoriteIdStrings.toSet()
          FavoritesSyncSnapshot(
            categories = categories,
            stationCategory = stationCategory,
            favoriteIds = ids,
            homeStationId = roles?.home_station_id,
            workStationId = roles?.work_station_id,
          ).normalized()
        }.collect { snapshot -> applySnapshot(snapshot) }
      }
    }
  }

  override val favoriteIds: StateFlow<Set<String>> = mutableFavoriteIds.asStateFlow()
  override val homeStationId: StateFlow<String?> = mutableHomeStationId.asStateFlow()
  override val workStationId: StateFlow<String?> = mutableWorkStationId.asStateFlow()
  override val categories: StateFlow<List<FavoriteCategory>> = mutableCategories.asStateFlow()
  override val stationCategory: StateFlow<Map<String, String>> = mutableStationCategory.asStateFlow()

  override suspend fun bootstrap() {
    if (bootstrapped) return
    syncMergedSnapshot(readPersistedSnapshot(), pushIfChanged = true)
    bootstrapped = true
  }

  override suspend fun syncFromPeer() {
    if (!bootstrapped) {
      bootstrap()
      return
    }
    syncMergedSnapshot(currentSnapshot(), pushIfChanged = false)
  }

  override suspend fun toggle(stationId: String) {
    if (!bootstrapped) bootstrap()
    val updatedAssignments = mutableStationCategory.value.toMutableMap()
    if (updatedAssignments[stationId] == FavoriteCategoryIds.FAVORITE) {
      updatedAssignments.remove(stationId)
    } else {
      updatedAssignments[stationId] = FavoriteCategoryIds.FAVORITE
    }
    val updatedSnapshot = currentSnapshot().copy(stationCategory = updatedAssignments).normalized()
    persist(updatedSnapshot)
    withTimeoutOrNull(WATCH_SYNC_TIMEOUT_MILLIS) {
      watchSyncBridge.pushFavorites(updatedSnapshot)
    }
  }

  override suspend fun setHomeStationId(stationId: String?) {
    if (!bootstrapped) bootstrap()
    val updatedAssignments = mutableStationCategory.value.toMutableMap()
    updatedAssignments.entries.removeAll { it.value == FavoriteCategoryIds.HOME }
    stationId?.takeIf(String::isNotBlank)?.let { updatedAssignments[it] = FavoriteCategoryIds.HOME }
    val updatedSnapshot = currentSnapshot().copy(stationCategory = updatedAssignments).normalized()
    persist(updatedSnapshot)
    withTimeoutOrNull(WATCH_SYNC_TIMEOUT_MILLIS) {
      watchSyncBridge.pushFavorites(updatedSnapshot)
    }
  }

  override suspend fun setWorkStationId(stationId: String?) {
    if (!bootstrapped) bootstrap()
    val updatedAssignments = mutableStationCategory.value.toMutableMap()
    updatedAssignments.entries.removeAll { it.value == FavoriteCategoryIds.WORK }
    stationId?.takeIf(String::isNotBlank)?.let { candidateWorkId ->
      val currentHomeId = mutableHomeStationId.value
      if (candidateWorkId != currentHomeId) {
        updatedAssignments[candidateWorkId] = FavoriteCategoryIds.WORK
      }
    }
    val updatedSnapshot = currentSnapshot().copy(stationCategory = updatedAssignments).normalized()
    persist(updatedSnapshot)
    withTimeoutOrNull(WATCH_SYNC_TIMEOUT_MILLIS) {
      watchSyncBridge.pushFavorites(updatedSnapshot)
    }
  }

  override suspend fun clearAll() {
    if (!bootstrapped) bootstrap()
    val emptySnapshot = FavoritesSyncSnapshot()
    persist(emptySnapshot)
    withTimeoutOrNull(WATCH_SYNC_TIMEOUT_MILLIS) {
      watchSyncBridge.pushFavorites(emptySnapshot)
    }
  }

  override suspend fun upsertCategory(
    id: String,
    label: String,
    isSystem: Boolean,
  ) {
    if (!bootstrapped) bootstrap()
    val normalizedId = id.trim()
    val normalizedLabel = label.trim()
    if (normalizedId.isBlank() || normalizedLabel.isBlank()) return
    val existing = mutableCategories.value.associateBy { it.id }.toMutableMap()
    val previous = existing[normalizedId]
    existing[normalizedId] =
      FavoriteCategory(
        id = normalizedId,
        label = normalizedLabel,
        isSystem = previous?.isSystem ?: isSystem,
      )
    val updatedSnapshot =
      currentSnapshot()
        .copy(categories = existing.values.sortedBy { it.label.lowercase() })
        .normalized()
    persist(updatedSnapshot)
  }

  override suspend fun removeCategory(categoryId: String) {
    if (!bootstrapped) bootstrap()
    val target = mutableCategories.value.firstOrNull { it.id == categoryId } ?: return
    if (target.isSystem) return
    val updatedCategories = mutableCategories.value.filterNot { it.id == categoryId }
    val updatedAssignments = mutableStationCategory.value.filterValues { it != categoryId }
    val updatedSnapshot =
      currentSnapshot()
        .copy(
          categories = updatedCategories,
          stationCategory = updatedAssignments,
        ).normalized()
    persist(updatedSnapshot)
  }

  override suspend fun assignStationToCategory(
    stationId: String,
    categoryId: String?,
  ) {
    if (!bootstrapped) bootstrap()
    val normalizedStationId = stationId.trim()
    if (normalizedStationId.isBlank()) return
    val updatedAssignments = mutableStationCategory.value.toMutableMap()
    if (categoryId.isNullOrBlank()) {
      updatedAssignments.remove(normalizedStationId)
    } else {
      val exists = mutableCategories.value.any { it.id == categoryId }
      if (!exists) return
      if (categoryId != FavoriteCategoryIds.FAVORITE) {
        updatedAssignments.entries.removeAll { it.value == categoryId }
      }
      updatedAssignments[normalizedStationId] = categoryId
    }
    val updatedSnapshot = currentSnapshot().copy(stationCategory = updatedAssignments).normalized()
    persist(updatedSnapshot)
  }

  override fun isFavorite(stationId: String): Boolean = mutableFavoriteIds.value.contains(stationId)

  override fun currentHomeStationId(): String? = mutableHomeStationId.value

  override fun currentWorkStationId(): String? = mutableWorkStationId.value

  private fun readPersistedSnapshot(): FavoritesSyncSnapshot {
    if (database == null) return legacyMigrator.readLegacySnapshot()
    return legacyMigrator.migrateIfNeeded(readDatabaseSnapshot())
  }

  private fun currentSnapshot(): FavoritesSyncSnapshot =
    FavoritesSyncSnapshot(
      categories = mutableCategories.value,
      stationCategory = mutableStationCategory.value,
      favoriteIds = mutableFavoriteIds.value,
      homeStationId = mutableHomeStationId.value,
      workStationId = mutableWorkStationId.value,
    )

  private suspend fun syncMergedSnapshot(
    localSnapshot: FavoritesSyncSnapshot,
    pushIfChanged: Boolean,
  ) {
    val normalizedLocal = localSnapshot.normalized()
    val remoteSnapshot =
      withTimeoutOrNull(WATCH_SYNC_TIMEOUT_MILLIS) {
        watchSyncBridge.latestFavorites()
      } ?: FavoritesSyncSnapshot()
    val normalizedRemote = remoteSnapshot.normalized()
    val mergedSnapshot =
      FavoritesSyncSnapshot(
        categories = mergeCategories(normalizedLocal.categories, normalizedRemote.categories),
        stationCategory = normalizedLocal.stationCategory + normalizedRemote.stationCategory,
        favoriteIds = normalizedLocal.favoriteIds + normalizedRemote.favoriteIds,
        homeStationId = normalizedLocal.homeStationId ?: normalizedRemote.homeStationId,
        workStationId = normalizedLocal.workStationId ?: normalizedRemote.workStationId,
      ).normalized()
    val existingSnapshot = currentSnapshot()
    val hasChanged = mergedSnapshot != existingSnapshot
    if (hasChanged) {
      persist(mergedSnapshot)
    } else if (!bootstrapped && mergedSnapshot.hasLegacyData()) {
      persist(mergedSnapshot)
    }
    if (mergedSnapshot.hasLegacyData() && (hasChanged || !bootstrapped || pushIfChanged)) {
      withTimeoutOrNull(WATCH_SYNC_TIMEOUT_MILLIS) {
        watchSyncBridge.pushFavorites(mergedSnapshot)
      }
    }
  }

  private fun applySnapshot(snapshot: FavoritesSyncSnapshot) {
    val normalized = snapshot.normalized()
    mutableCategories.value = normalized.categories
    mutableStationCategory.value = normalized.stationCategory
    mutableFavoriteIds.value = normalized.stationCategory.filterValues { it == FavoriteCategoryIds.FAVORITE }.keys
    mutableHomeStationId.value =
      normalized.stationCategory.entries
        .firstOrNull { it.value == FavoriteCategoryIds.HOME }
        ?.key
    mutableWorkStationId.value =
      normalized.stationCategory.entries
        .firstOrNull { it.value == FavoriteCategoryIds.WORK }
        ?.key
  }

  private suspend fun persist(snapshot: FavoritesSyncSnapshot) {
    if (database != null) {
      persistToDatabase(snapshot)
      legacyMigrator.deleteLegacyFile()
      applySnapshot(snapshot)
      return
    }
    applySnapshot(snapshot)
    persistToLocalFile(snapshot)
  }

  private fun readDatabaseSnapshot(): FavoritesSyncSnapshot? {
    val db = database ?: return null
    return runCatching {
      val categories =
        db.biciradarQueries.getAllFavoriteCategories().executeAsList().map {
          FavoriteCategory(
            id = it.id,
            label = it.label,
            isSystem = it.is_system != 0L,
          )
        }
      val stationCategory =
        db.biciradarQueries
          .getAllFavoriteStationCategories()
          .executeAsList()
          .associate { it.station_id to it.category_id }
      val ids =
        db.biciradarQueries
          .getAllFavoriteIds()
          .executeAsList()
          .toSet()
      val roles = db.biciradarQueries.getFavoriteRoles().executeAsOneOrNull()
      FavoritesSyncSnapshot(
        categories = categories,
        stationCategory = stationCategory,
        favoriteIds = ids,
        homeStationId = roles?.home_station_id,
        workStationId = roles?.work_station_id,
      ).normalized()
    }.getOrNull()
  }

  private fun persistToDatabase(snapshot: FavoritesSyncSnapshot): Boolean {
    val db = database ?: return false
    return runCatching {
      db.transaction {
        db.biciradarQueries.deleteAllFavoriteCategories()
        snapshot.categories.forEach { category ->
          db.biciradarQueries.insertFavoriteCategory(
            id = category.id,
            label = category.label,
            isSystem = if (category.isSystem) 1L else 0L,
          )
        }
        db.biciradarQueries.deleteAllFavoriteStationCategories()
        snapshot.stationCategory.forEach { (stationId, categoryId) ->
          db.biciradarQueries.insertFavoriteStationCategory(stationId, categoryId)
        }
        db.biciradarQueries.deleteAllFavoriteIds()
        val legacyFavoriteIds =
          snapshot.stationCategory
            .filterValues { it == FavoriteCategoryIds.FAVORITE }
            .keys
        legacyFavoriteIds.forEach { stationId ->
          db.biciradarQueries.insertFavoriteId(stationId)
        }
        val legacyHomeId =
          snapshot.stationCategory.entries
            .firstOrNull { it.value == FavoriteCategoryIds.HOME }
            ?.key
        val legacyWorkId =
          snapshot.stationCategory.entries
            .firstOrNull { it.value == FavoriteCategoryIds.WORK }
            ?.key
        if (legacyHomeId == null && legacyWorkId == null) {
          db.biciradarQueries.clearFavoriteRoles()
        } else {
          db.biciradarQueries.upsertFavoriteRoles(legacyHomeId, legacyWorkId)
        }
      }
    }.isSuccess
  }

  private fun persistToLocalFile(snapshot: FavoritesSyncSnapshot) {
    val path = "${storageDirectoryProvider.rootPath}/favorites.json".toPath()
    val dir = path.parent ?: return
    fileSystem.createDirectories(dir)
    fileSystem.write(path) {
      writeUtf8(json.encodeToString(snapshot))
    }
  }
}

private fun FavoritesSyncSnapshot.normalized(): FavoritesSyncSnapshot {
  val baseCategories = mergeCategories(systemCategories(), categories)
  val normalizedMap =
    when {
      stationCategory.isNotEmpty() -> stationCategory.toMutableMap()
      else -> {
        val migrated = mutableMapOf<String, String>()
        favoriteIds.forEach { migrated[it] = FavoriteCategoryIds.FAVORITE }
        homeStationId?.let { migrated[it] = FavoriteCategoryIds.HOME }
        workStationId?.let { migrated[it] = FavoriteCategoryIds.WORK }
        migrated
      }
    }
  normalizedMap.entries.removeAll { (_, categoryId) -> baseCategories.none { it.id == categoryId } }
  val legacyFavoriteIds = normalizedMap.filterValues { it == FavoriteCategoryIds.FAVORITE }.keys
  val legacyHomeId = normalizedMap.entries.firstOrNull { it.value == FavoriteCategoryIds.HOME }?.key
  val legacyWorkId = normalizedMap.entries.firstOrNull { it.value == FavoriteCategoryIds.WORK }?.key
  return copy(
    categories = baseCategories,
    stationCategory = normalizedMap,
    favoriteIds = legacyFavoriteIds,
    homeStationId = legacyHomeId,
    workStationId = legacyWorkId,
  )
}

internal fun FavoritesSyncSnapshot.hasLegacyData(): Boolean =
  stationCategory.isNotEmpty() ||
    favoriteIds.isNotEmpty() ||
    homeStationId != null ||
    workStationId != null

private fun systemCategories(): List<FavoriteCategory> =
  listOf(
    FavoriteCategory(FavoriteCategoryIds.FAVORITE, systemCategoryLabel(FavoriteCategoryIds.FAVORITE), isSystem = true),
    FavoriteCategory(FavoriteCategoryIds.HOME, systemCategoryLabel(FavoriteCategoryIds.HOME), isSystem = true),
    FavoriteCategory(FavoriteCategoryIds.WORK, systemCategoryLabel(FavoriteCategoryIds.WORK), isSystem = true),
  )

private fun mergeCategories(
  first: List<FavoriteCategory>,
  second: List<FavoriteCategory>,
): List<FavoriteCategory> {
  val merged = linkedMapOf<String, FavoriteCategory>()
  (first + second).forEach { category ->
    val existing = merged[category.id]
    merged[category.id] =
      if (existing == null) {
        category
      } else {
        category.copy(isSystem = existing.isSystem || category.isSystem)
      }
  }
  return merged.values.toList()
}
