import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.firebase.crashlytics) apply false
  alias(libs.plugins.google.services) apply false
}

val localProperties =
  Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
      localPropertiesFile.inputStream().use(::load)
    }
  }

val wearApplicationId = "com.gcaguilar.biciradar"
val firebaseConfigFile = layout.projectDirectory.file("google-services.json").asFile
val requestedTasks =
  gradle.startParameter.taskNames
    .joinToString(" ")
    .lowercase()
val firebaseCrashlyticsEnabled =
  firebaseConfigFile.exists() &&
    firebaseConfigFile.readText().contains("\"package_name\": \"$wearApplicationId\"") &&
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

if (firebaseCrashlyticsEnabled) {
  apply(plugin = "com.google.gms.google-services")
  apply(plugin = "com.google.firebase.crashlytics")

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

android {
  namespace = "com.gcaguilar.biciradar.wear"
  compileSdk = 36

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
      // F-Droid specific configuration
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

  // F-Droid flavor dependencies
  add("fdroidImplementation", "org.osmdroid:osmdroid-android:6.1.14")
  add("fdroidImplementation", "org.osmdroid:osmdroid-wms:6.1.14")
  add(
    "fdroidImplementation",
    "org.osmdroid:osmdroid-mapsforge:6.1.14",
  ) {
    exclude(group = "net.sf.kxml", module = "kxml2")
  }
  add("fdroidImplementation", "org.osmdroid:osmdroid-geopackage:6.1.14")

  // Play Store flavor dependencies
  add("playstoreImplementation", libs.play.services.wearable)
  add("playstoreImplementation", platform(libs.firebase.bom))
  add("playstoreImplementation", libs.firebase.crashlytics)
}
