import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(playstore.plugins.crash.reporting) apply false
  alias(playstore.plugins.google.services) apply false
}

val crashReportingTaskMarker = "crash" + "lytics"
val mobileServicesGroupPrefix = "com.google." + "fire" + "base:"

val localProperties =
  Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
      localPropertiesFile.inputStream().use(::load)
    }
  }

val wearApplicationId = "com.gcaguilar.biciradar"
val playServicesConfigFile = layout.projectDirectory.file("google-services.json").asFile
val requestedTasks =
  gradle.startParameter.taskNames
    .joinToString(" ")
    .lowercase()
val crashReportingEnabled =
  playServicesConfigFile.exists() &&
    playServicesConfigFile.readText().contains("\"package_name\": \"$wearApplicationId\"") &&
    (
      requestedTasks.isBlank() ||
        requestedTasks.contains("playstore")
    )

val wearCiKeystorePath =
  project.findProperty("BIZI_CI_KEYSTORE_PATH") as? String
    ?: System.getenv("BIZI_CI_KEYSTORE_PATH")
val wearCiKeystorePassword =
  project.findProperty("BIZI_CI_KEYSTORE_PASSWORD") as? String
    ?: System.getenv("BIZI_CI_KEYSTORE_PASSWORD")
val wearCiKeyAlias =
  project.findProperty("BIZI_CI_KEY_ALIAS") as? String
    ?: System.getenv("BIZI_CI_KEY_ALIAS")
val wearCiKeyPassword =
  project.findProperty("BIZI_CI_KEY_PASSWORD") as? String
    ?: System.getenv("BIZI_CI_KEY_PASSWORD")
val hasWearCiSigning =
  wearCiKeystorePath != null &&
    wearCiKeystorePassword != null &&
    wearCiKeyAlias != null &&
    wearCiKeyPassword != null

if (crashReportingEnabled) {
  apply(plugin = "com.google.gms.google-services")
  apply(
    plugin =
      playstore.plugins.crash.reporting
        .get()
        .pluginId,
  )

  tasks.configureEach {
    val taskName = name.lowercase()
    if (
      taskName.contains("fdroid") &&
      (
        taskName.endsWith("googleservices") ||
          taskName.contains(crashReportingTaskMarker)
      )
    ) {
      enabled = false
    }
  }
}

android {
  namespace = "com.gcaguilar.biciradar.wear"
  compileSdk = 37

  defaultConfig {
    applicationId = wearApplicationId
    minSdk = 30
    targetSdk = 36
    versionCode = 29568119
    versionName = "0.22.8"
  }

  flavorDimensions += "tier"
  productFlavors {
    create("fdroid") {
      dimension = "tier"
      applicationIdSuffix = ".wear.fdroid"
      versionNameSuffix = "-fdroid"
    }
    create("playstore") {
      dimension = "tier"
      // Play Store specific configuration (default)
    }
  }

  buildFeatures {
    buildConfig = true
    compose = true
  }

  signingConfigs {
    create("release") {
      if (hasWearCiSigning) {
        storeFile = file(wearCiKeystorePath!!)
        storePassword = wearCiKeystorePassword
        keyAlias = wearCiKeyAlias
        this.keyPassword = wearCiKeyPassword
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
      if (hasWearCiSigning) {
        signingConfig = signingConfigs.getByName("release")
      }
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

dependencies {
  implementation(project(":shared:core"))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.fragment)
  implementation(libs.androidx.core.ktx)
  implementation(libs.metro.runtime)
  implementation(libs.androidx.wear.protolayout)
  implementation(libs.androidx.wear.protolayout.material3)
  implementation(libs.androidx.wear.compose.foundation)
  implementation(libs.androidx.wear.compose.material3)
  implementation(libs.androidx.wear.compose.navigation)
  implementation(libs.androidx.wear.tiles)
  implementation(libs.androidx.wear.ongoing)
  implementation(libs.androidx.wear.watchface.complications.data.source)
  implementation(libs.androidx.wear.watchface.complications.data.source.ktx)
  testImplementation(libs.junit)

  // Play Store flavor dependencies
  add("playstoreImplementation", playstore.play.services.wearable)
  add("playstoreImplementation", platform(playstore.mobile.services.bom))
  add("playstoreImplementation", playstore.crash.reporting.sdk)
}

abstract class VerifyDependencyPrefixesTask : DefaultTask() {
  @get:Input abstract val configurationName: Property<String>

  @get:Input abstract val forbiddenPrefixes: ListProperty<String>

  @TaskAction
  fun verify() {
    val forbidden =
      project.configurations
        .getByName(configurationName.get())
        .incoming
        .resolutionResult
        .allComponents
        .mapNotNull { component ->
          component.moduleVersion?.let { "${it.group}:${it.name}:${it.version}" }
        }.filter { dependency ->
          forbiddenPrefixes.get().any(dependency::startsWith)
        }.sorted()

    check(forbidden.isEmpty()) {
      buildString {
        appendLine("Forbidden dependencies found in wearApp fdroidReleaseRuntimeClasspath:")
        forbidden.forEach { appendLine(" - $it") }
      }
    }
  }
}

val verifyFdroidReleaseDependencies by
  tasks.registering(VerifyDependencyPrefixesTask::class) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Fails when the F-Droid runtime classpath contains forbidden proprietary SDKs."
    notCompatibleWithConfigurationCache("Inspects resolved Gradle configurations during execution.")
    configurationName.set("fdroidReleaseRuntimeClasspath")
    forbiddenPrefixes.set(
      listOf(
        "com.google.android.gms:",
        "com.google.android.play:",
        mobileServicesGroupPrefix,
        "com.garmin.connectiq:",
        "com.google.maps.android:",
      ),
    )
  }

tasks.matching { it.name == "assembleFdroidRelease" || it.name == "check" }.configureEach {
  dependsOn(verifyFdroidReleaseDependencies)
}
