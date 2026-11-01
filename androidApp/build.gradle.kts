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
    variantBuilder.dependenciesInfo.includeInApk = false
    variantBuilder.dependenciesInfo.includeInBundle = false
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
  androidCiKeystorePath
    ?.takeIf(String::isNotBlank)
    ?.let { file(it).isFile } == true &&
    !androidCiKeystorePassword.isNullOrBlank() &&
    !androidCiKeyAlias.isNullOrBlank() &&
    !androidCiKeyPassword.isNullOrBlank()

android {
  namespace = "com.gcaguilar.biciradar"
  compileSdk {
    version = release(37) { minorApiLevel = 1 }
  }

  defaultConfig {
    applicationId = "com.gcaguilar.biciradar"
    minSdk = 29
    targetSdk = 37
    versionCode = 29570207
    versionName = "0.22.18"
    manifestPlaceholders["googleMapsApiKey"] = googleMapsApiKey.get()

    fun quoted(value: String): String = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

    fun configValue(
      property: String,
      envVar: String,
    ): String = providers.gradleProperty(property).getOrElse(System.getenv(envVar).orEmpty())

    buildConfigField("String", "BACKEND_BASE_URL", quoted(configValue("bizi.backendBaseUrl", "BICI_BACKEND_URL")))
    buildConfigField("String", "AUTH0_DOMAIN", quoted(configValue("bizi.auth0Domain", "BICI_AUTH0_DOMAIN")))
    buildConfigField("String", "AUTH0_CLIENT_ID", quoted(configValue("bizi.auth0ClientId", "BICI_AUTH0_CLIENT_ID")))
    buildConfigField("String", "AUTH0_AUDIENCE", quoted(configValue("bizi.auth0Audience", "BICI_AUTH0_AUDIENCE")))

    // The Auth0 SDK's RedirectActivity manifest declares ${auth0Domain} and
    // ${auth0Scheme}; without them the manifest merger fails the release build.
    manifestPlaceholders["auth0Domain"] =
      configValue("bizi.auth0Domain", "BICI_AUTH0_DOMAIN")
        .removePrefix("https://")
        .trimEnd('/')
    manifestPlaceholders["auth0Scheme"] =
      configValue("bizi.auth0Scheme", "BICI_AUTH0_SCHEME").ifBlank { "biciradar" }
  }

  flavorDimensions += "tier"
  productFlavors {
    create("fdroid") {
      dimension = "tier"
      applicationIdSuffix = ".fdroid"
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
      signingConfig = signingConfigs.getByName("debug")
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
    signingAvailable.set(hasAndroidCiSigning)
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
  // Auth0 pulls androidx.credentials + Google Play Services; F-Droid must stay
  // GMS-free, so the transitive Play Services are excluded there. The browser
  // (custom tabs) login path does not need them.
  add("fdroidImplementation", libs.auth0.android)
  // Auth0 pulls androidx.credentials + Google Play Services; F-Droid must stay
  // GMS-free, so the transitive Play Services are excluded there. The browser
  // (custom tabs) login path does not need them.
  configurations.matching { it.name.startsWith("fdroid") }.configureEach {
    exclude(group = "com.google.android.gms")
    exclude(group = "androidx.credentials", module = "credentials-play-services-auth")
  }

  // Play Store flavor dependencies
  add("playstoreImplementation", libs.auth0.android)
  add("playstoreImplementation", playstore.maps.compose)
  add("playstoreImplementation", playstore.remote.config.sdk)
  add("playstoreImplementation", playstore.firebase.messaging)
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
        "com.google.maps.android:",
      ),
    )
  }

// Ligado sólo al assemble de F-Droid para no invalidar el configuration cache
// del resto de tareas (la verificación no es compatible con config cache).
tasks.matching { it.name == "assembleFdroidRelease" }.configureEach {
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
    description = "Verifies that R8 preserves Android Play Store reflection contracts."
    dependsOn("mergePlaystoreReleaseComposeMapping")
    mappingFile.set(layout.buildDirectory.file("outputs/mapping/playstoreRelease/mapping.txt"))
    unchangedClasses.set(
      listOf(
        "com.gcaguilar.biciradar.AndroidOptionalServicesFactory",
      ),
    )
    unchangedMembers.set(
      mapOf(
        "AndroidOptionalServices create(android.content.Context)" to "create",
      ),
    )
  }

tasks
  .matching {
    it.name == "assemblePlaystoreRelease" || it.name == "bundlePlaystoreRelease"
  }.configureEach {
    dependsOn(verifyPlaystoreReleaseR8Mapping)
  }
