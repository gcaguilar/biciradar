import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.language.base.plugins.LifecycleBasePlugin

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(playstore.plugins.google.services) apply false
  alias(playstore.plugins.crash.reporting) apply false
}

val crashReportingTaskMarker = "crash" + "lytics"
val mobileServicesGroupPrefix = "com.google." + "fire" + "base:"

val googleServicesJson = file("google-services.json")
val requestedTasks =
  gradle.startParameter.taskNames
    .joinToString(" ")
    .lowercase()
val shouldApplyGoogleServices =
  googleServicesJson.exists() &&
    (
      requestedTasks.isBlank() ||
        requestedTasks.contains("playstore")
    )

if (shouldApplyGoogleServices) {
  apply(
    plugin =
      playstore.plugins.google.services
        .get()
        .pluginId,
  )
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

androidComponents {
  beforeVariants(selector().withBuildType("release").withFlavor("tier" to "fdroid")) { variantBuilder ->
    // Keep the Play Store release optimized while making the F-Droid APK easier to reproduce.
    variantBuilder.isMinifyEnabled = false
    variantBuilder.shrinkResources = false
  }
}

val googleMapsApiKey =
  providers
    .environmentVariable("GOOGLE_MAPS_API_KEY")
    .orElse("")

val androidCiKeystorePath =
  project.findProperty("BIZI_CI_KEYSTORE_PATH") as? String
    ?: System.getenv("BIZI_CI_KEYSTORE_PATH")
val androidCiKeystorePassword =
  project.findProperty("BIZI_CI_KEYSTORE_PASSWORD") as? String
    ?: System.getenv("BIZI_CI_KEYSTORE_PASSWORD")
val androidCiKeyAlias =
  project.findProperty("BIZI_CI_KEY_ALIAS") as? String
    ?: System.getenv("BIZI_CI_KEY_ALIAS")
val androidCiKeyPassword =
  project.findProperty("BIZI_CI_KEY_PASSWORD") as? String
    ?: System.getenv("BIZI_CI_KEY_PASSWORD")
val hasAndroidCiSigning =
  androidCiKeystorePath != null &&
    androidCiKeystorePassword != null &&
    androidCiKeyAlias != null &&
    androidCiKeyPassword != null

android {
  namespace = "com.gcaguilar.biciradar"
  compileSdk = 37

  defaultConfig {
    applicationId = "com.gcaguilar.biciradar"
    minSdk = 29
    targetSdk = 36
    versionCode = 29568121
    versionName = "0.22.10"
    manifestPlaceholders["googleMapsApiKey"] = googleMapsApiKey.get()
  }

  flavorDimensions += "tier"
  productFlavors {
    create("fdroid") {
      dimension = "tier"
      applicationIdSuffix = ".fdroid"
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

  if (requestedTasks.contains("fdroid")) {
    dependenciesInfo {
      includeInApk = false
      includeInBundle = false
    }
  }

  signingConfigs {
    create("release") {
      if (hasAndroidCiSigning) {
        storeFile = file(androidCiKeystorePath!!)
        storePassword = androidCiKeystorePassword
        keyAlias = androidCiKeyAlias
        this.keyPassword = androidCiKeyPassword
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
      if (hasAndroidCiSigning) {
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
  implementation(project(":shared:mobile-ui"))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.fragment)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.startup.runtime)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.androidx.glance.appwidget)
  implementation(libs.androidx.glance.material3)
  implementation(libs.google.material)
  testImplementation(libs.junit)
  add("fdroidImplementation", libs.osmdroid.android)

  // Play Store flavor dependencies
  add("playstoreImplementation", playstore.garmin.connectiq.sdk)
  add("playstoreImplementation", playstore.maps.compose)
  add("playstoreImplementation", playstore.remote.config.sdk)
  add("playstoreImplementation", playstore.play.services.maps)
  add("playstoreImplementation", playstore.play.services.wearable)
  add("playstoreImplementation", playstore.play.review.ktx)
  add("playstoreImplementation", playstore.play.app.update.ktx)
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
        appendLine("Forbidden dependencies found in androidApp fdroidReleaseRuntimeClasspath:")
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

tasks.configureEach {
  val taskName = name.lowercase()
  if (
    taskName.contains("fdroidrelease") &&
    (
      taskName.contains("artprofile") ||
        taskName.contains("baselineprofile") ||
        taskName.contains("versioncontrolinfo")
    )
  ) {
    enabled = false
  }
}
