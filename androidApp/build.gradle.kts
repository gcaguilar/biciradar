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
  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.firebase.crashlytics) apply false
}

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
      libs.plugins.google.services
        .get()
        .pluginId,
  )
  apply(
    plugin =
      libs.plugins.firebase.crashlytics
        .get()
        .pluginId,
  )

  tasks.configureEach {
    val taskName = name.lowercase()
    if (
      taskName.contains("fdroid") &&
      (
        taskName.endsWith("googleservices") ||
          taskName.contains("crashlytics")
      )
    ) {
      enabled = false
    }
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
    versionCode = 29568118
    versionName = "0.22.8"
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

  // Play Store flavor dependencies
  add("playstoreImplementation", "com.garmin.connectiq:ciq-companion-app-sdk:2.4.0@aar")
  add("playstoreImplementation", libs.maps.compose)
  add("playstoreImplementation", libs.firebase.config)
  add("playstoreImplementation", libs.play.services.maps)
  add("playstoreImplementation", libs.play.services.wearable)
  add("playstoreImplementation", libs.play.review.ktx)
  add("playstoreImplementation", libs.play.app.update.ktx)
  add("playstoreImplementation", platform(libs.firebase.bom))
  add("playstoreImplementation", libs.firebase.crashlytics)
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
        "com.google.firebase:",
        "com.garmin.connectiq:",
        "com.google.maps.android:",
      ),
    )
  }

tasks.matching { it.name == "assembleFdroidRelease" || it.name == "check" }.configureEach {
  dependsOn(verifyFdroidReleaseDependencies)
}
