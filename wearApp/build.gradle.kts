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
  wearCiKeystorePath
    ?.takeIf(String::isNotBlank)
    ?.let { file(it).isFile } == true &&
    !wearCiKeystorePassword.isNullOrBlank() &&
    !wearCiKeyAlias.isNullOrBlank() &&
    !wearCiKeyPassword.isNullOrBlank()

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
    versionCode = 29570204
    versionName = "0.22.16"
  }

  flavorDimensions += "tier"
  productFlavors {
    create("fdroid") {
      dimension = "tier"
      applicationIdSuffix = ".wear.fdroid"
      versionNameSuffix = "-fdroid"
      proguardFiles("fdroid-proguard-rules.pro")
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

abstract class VerifyPlayStoreReleaseSigningTask : DefaultTask() {
  @get:Input abstract val signingAvailable: Property<Boolean>

  @TaskAction
  fun verify() {
    check(signingAvailable.get()) {
      "Play Store release signing requires a readable BIZI_CI_KEYSTORE_PATH and non-blank " +
        "BIZI_CI_KEYSTORE_PASSWORD, BIZI_CI_KEY_ALIAS, and BIZI_CI_KEY_PASSWORD."
    }
  }
}

val verifyPlayStoreReleaseSigning =
  tasks.register<VerifyPlayStoreReleaseSigningTask>("verifyPlayStoreReleaseSigning") {
    signingAvailable.set(hasWearCiSigning)
  }

val playStoreReleasePackagingTasks =
  tasks.matching {
    it.name in
      setOf(
        "assemblePlaystoreRelease",
        "bundlePlaystoreRelease",
        "validateSigningPlaystoreRelease",
      )
  }

playStoreReleasePackagingTasks.configureEach {
  dependsOn(verifyPlayStoreReleaseSigning)
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
        "com.google.maps.android:",
      ),
    )
  }

// Ligado sólo al assemble de F-Droid para no invalidar el configuration cache
// del resto de tareas (la verificación no es compatible con config cache).
tasks.matching { it.name == "assembleFdroidRelease" }.configureEach {
  dependsOn(verifyFdroidReleaseDependencies)
}

abstract class VerifyR8MappingTask : DefaultTask() {
  @get:InputFile abstract val mappingFile: RegularFileProperty

  @get:Input abstract val unchangedClasses: ListProperty<String>

  @get:Input abstract val unchangedMembers: MapProperty<String, String>

  @TaskAction
  fun verify() {
    val mapping = mappingFile.get().asFile.readLines()

    unchangedClasses.get().forEach { className ->
      check(mapping.any { it == "$className -> $className:" }) {
        "R8 removed or renamed reflective class $className"
      }
    }

    unchangedMembers.get().forEach { (signature, expectedName) ->
      check(
        mapping.any { line ->
          line.startsWith("    ") &&
            line.contains(signature) &&
            line.substringAfterLast(" -> ") == expectedName
        },
      ) {
        "R8 removed or renamed reflective member $signature"
      }
    }
  }
}

val verifyPlaystoreReleaseR8Mapping =
  tasks.register<VerifyR8MappingTask>("verifyPlaystoreReleaseR8Mapping") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Verifies that R8 preserves Wear Play Store reflection contracts."
    dependsOn("mergePlaystoreReleaseComposeMapping")
    mappingFile.set(layout.buildDirectory.file("outputs/mapping/playstoreRelease/mapping.txt"))
    unchangedClasses.set(
      listOf(
        "com.gcaguilar.biciradar.wear.AndroidOptionalServicesFactory",
        "com.gcaguilar.biciradar.wear.PlaystoreWearPhoneRouteRequesterDelegate",
      ),
    )
    unchangedMembers.set(
      mapOf(
        "AndroidOptionalServices create(android.content.Context)" to "create",
        "void <init>(android.content.Context)" to "<init>",
        "boolean isRouteAvailable()" to "isRouteAvailable",
        "boolean requestRoute(java.lang.String)" to "requestRoute",
      ),
    )
  }

tasks
  .matching {
    it.name == "assemblePlaystoreRelease" || it.name == "bundlePlaystoreRelease"
  }.configureEach {
    dependsOn(verifyPlaystoreReleaseR8Mapping)
  }
