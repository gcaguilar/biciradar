package com.gcaguilar.biciradar.core.platform

import com.gcaguilar.biciradar.core.AppConfiguration
import com.gcaguilar.biciradar.core.AppUpdatePrompter
import com.gcaguilar.biciradar.core.AssistantIntentResolver
import com.gcaguilar.biciradar.core.BiziHttpClientFactory
import com.gcaguilar.biciradar.core.DatabaseFactory
import com.gcaguilar.biciradar.core.DefaultAssistantIntentResolver
import com.gcaguilar.biciradar.core.EmbeddedMapProvider
import com.gcaguilar.biciradar.core.ExternalLinks
import com.gcaguilar.biciradar.core.FavoritesSyncSnapshot
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.core.LocationProvider
import com.gcaguilar.biciradar.core.MapSupport
import com.gcaguilar.biciradar.core.MapSupportStatus
import com.gcaguilar.biciradar.core.NoOpAppUpdatePrompter
import com.gcaguilar.biciradar.core.PermissionPrompter
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.ReviewPrompter
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StorageDirectoryProvider
import com.gcaguilar.biciradar.core.WatchSyncBridge
import com.gcaguilar.biciradar.core.crypto.SecureKeyStore
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import com.gcaguilar.biciradar.core.local.LegacyBlobToRelationalMigration
import com.gcaguilar.biciradar.core.local.createJdbcDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okio.FileSystem
import java.awt.Desktop
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.image.BufferedImage
import java.io.File
import java.net.URI
import java.util.Locale

private const val REQUEST_TIMEOUT_MILLIS = 15_000L
private const val CONNECT_TIMEOUT_MILLIS = 10_000L

class DesktopPlatformBindings(
  override val appConfiguration: AppConfiguration = AppConfiguration(),
) : PlatformBindings {
  private val storageDirectory = DesktopStorageDirectoryProvider()
  private val desktopRouteLauncher = DesktopRouteLauncher()
  private var database: BiciRadarDatabase? = null

  override val appVersion: String = DesktopPlatformBindings::class.java.`package`?.implementationVersion
    ?.takeIf { it.isNotBlank() }
    ?: System.getProperty("biciradar.version")?.takeIf { it.isNotBlank() }
    ?: runCatching { File("VERSION").readText().trim() }.getOrNull()?.takeIf { it.isNotBlank() }
    ?: "desktop-dev"
  override val assistantIntentResolver: AssistantIntentResolver = DefaultAssistantIntentResolver()
  override val databaseFactory: DatabaseFactory = object : DatabaseFactory {
    override fun create(json: Json): BiciRadarDatabase? {
      if (database == null) {
        val driver = createJdbcDriver("${storageDirectory.rootPath}/biciradar.db")
        val db = BiciRadarDatabase(driver)
        LegacyBlobToRelationalMigration.ensure(driver, db, json)
        database = db
      }
      return database
    }
  }
  override val externalLinks: ExternalLinks = DesktopExternalLinks(appConfiguration)
  override val fileSystem: FileSystem = FileSystem.SYSTEM
  override val googleMapsApiKey: String? = System.getenv("GOOGLE_MAPS_API_KEY")
    ?.trim()
    ?.takeIf { it.isNotBlank() }
  override val httpClientFactory: BiziHttpClientFactory = DesktopHttpClientFactory()
  override val appUpdatePrompter: AppUpdatePrompter = DesktopAppUpdatePrompter()
  override val localNotifier: LocalNotifier = DesktopLocalNotifier()
  override val locationProvider: LocationProvider = DesktopLocationProvider()
  override val mapSupport: MapSupport = DesktopMapSupport()
  override val osVersion: String = listOf(
    System.getProperty("os.name").orEmpty(),
    System.getProperty("os.version").orEmpty(),
  ).filter { it.isNotBlank() }.joinToString(" ")
  override val platform: String = detectDesktopPlatform()
  override val permissionPrompter: PermissionPrompter = DesktopPermissionPrompter()
  override val reviewPrompter: ReviewPrompter = DesktopReviewPrompter(appConfiguration)
  override val routeLauncher: RouteLauncher = desktopRouteLauncher
  override val secureKeyStore: SecureKeyStore = SecureKeyStore()
  override val storageDirectoryProvider: StorageDirectoryProvider = storageDirectory
  override val watchSyncBridge: WatchSyncBridge = DesktopWatchSyncBridge()

  override fun onGraphCreated(graph: SharedGraph) {
    desktopRouteLauncher.settingsRepository = graph.settingsRepository
  }
}

private class DesktopHttpClientFactory : BiziHttpClientFactory {
  override fun create(json: Json): HttpClient = HttpClient(CIO) {
    expectSuccess = true
    install(HttpTimeout) {
      requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
      connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
      socketTimeoutMillis = REQUEST_TIMEOUT_MILLIS
    }
    install(ContentNegotiation) {
      json(json)
    }
  }
}

private class DesktopStorageDirectoryProvider : StorageDirectoryProvider {
  override val rootPath: String = "${System.getProperty("user.home").orEmpty().ifBlank { "." }}/.biciradar"
}

private class DesktopLocationProvider : LocationProvider {
  override suspend fun currentLocation(): GeoPoint? = null
}

private class DesktopMapSupport : MapSupport {
  override fun currentStatus(): MapSupportStatus = MapSupportStatus(
    embeddedProvider = EmbeddedMapProvider.None,
    googleMapsSdkLinked = false,
    googleMapsApiKeyConfigured = false,
  )
}

private class DesktopPermissionPrompter : PermissionPrompter {
  override suspend fun hasLocationPermission(): Boolean = false

  override suspend fun requestLocationPermission(): Boolean = false
}

private class DesktopExternalLinks(
  private val appConfiguration: AppConfiguration,
) : ExternalLinks {
  override fun openFeedbackForm() {
    browse(appConfiguration.feedbackFormUrl)
  }
}

private class DesktopReviewPrompter(
  private val appConfiguration: AppConfiguration,
) : ReviewPrompter {
  override suspend fun requestInAppReview() = Unit

  override fun openStoreWriteReview() {
    val reviewUrl = appConfiguration.iosAppStoreUrl
      ?: "https://apps.apple.com/us/search?term=BiciRadar"
    browse(reviewUrl)
  }
}

private class DesktopAppUpdatePrompter : AppUpdatePrompter by NoOpAppUpdatePrompter

private class DesktopRouteLauncher : RouteLauncher {
  var settingsRepository: SettingsRepository? = null

  override fun launch(station: Station) {
    browse(routeUrlFor(station.location))
  }

  override fun launchWalkToLocation(destination: GeoPoint) {
    browse(routeUrlFor(destination))
  }

  private fun routeUrlFor(destination: GeoPoint): String = when (
    settingsRepository?.currentPreferredMapApp() ?: PreferredMapApp.AppleMaps
  ) {
    PreferredMapApp.AppleMaps ->
      "https://maps.apple.com/?daddr=${destination.latitude},${destination.longitude}&dirflg=w"
    PreferredMapApp.GoogleMaps ->
      "https://www.google.com/maps/dir/?api=1&destination=${destination.latitude},${destination.longitude}&travelmode=walking"
  }
}

private class DesktopLocalNotifier : LocalNotifier {
  override suspend fun hasPermission(): Boolean = true

  override suspend fun requestPermission(): Boolean = true

  override suspend fun notify(title: String, body: String) {
    if (DesktopNotifications.show(title, body)) return
    println("[BiciRadar] $title: $body")
  }
}

private class DesktopWatchSyncBridge : WatchSyncBridge {
  override suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot) = Unit

  override suspend fun latestFavorites(): FavoritesSyncSnapshot? = null
}

private object DesktopNotifications {
  private val trayIcon: TrayIcon? by lazy(LazyThreadSafetyMode.NONE) {
    runCatching {
      if (!SystemTray.isSupported()) return@runCatching null
      val image = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
      TrayIcon(image, "BiciRadar").apply {
        isImageAutoSize = true
        SystemTray.getSystemTray().add(this)
      }
    }.getOrNull()
  }

  fun show(title: String, body: String): Boolean {
    val icon = trayIcon ?: return false
    icon.displayMessage(title, body, TrayIcon.MessageType.NONE)
    return true
  }
}

private fun browse(uri: String) {
  runCatching {
    if (!Desktop.isDesktopSupported()) return
    Desktop.getDesktop().browse(URI(uri))
  }
}

private fun detectDesktopPlatform(): String {
  val osName = System.getProperty("os.name").orEmpty().lowercase(Locale.US)
  return when {
    "mac" in osName -> "macos"
    "windows" in osName -> "windows"
    else -> "desktop"
  }
}
