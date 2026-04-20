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

android {
  namespace = "com.gcaguilar.biciradar"
  compileSdk = 37

  defaultConfig {
    applicationId = "com.gcaguilar.biciradar"
    minSdk = 29
    targetSdk = 36
    versionCode = 29568117
    versionName = "0.22.7"
    manifestPlaceholders["googleMapsApiKey"] = googleMapsApiKey.get()
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
      val keystorePath =
        project.findProperty("BIZI_CI_KEYSTORE_PATH") as? String
          ?: System.getenv("BIZI_CI_KEYSTORE_PATH")
      val keystorePassword =
        project.findProperty("BIZI_CI_KEYSTORE_PASSWORD") as? String
          ?: System.getenv("BIZI_CI_KEYSTORE_PASSWORD")
      val keyAliasEnv =
        project.findProperty("BIZI_CI_KEY_ALIAS") as? String
          ?: System.getenv("BIZI_CI_KEY_ALIAS")
      val keyPassword =
        project.findProperty("BIZI_CI_KEY_PASSWORD") as? String
          ?: System.getenv("BIZI_CI_KEY_PASSWORD")

      if (keystorePath != null && keystorePassword != null && keyAliasEnv != null && keyPassword != null) {
        storeFile = file(keystorePath)
        storePassword = keystorePassword
        keyAlias = keyAliasEnv
        this.keyPassword = keyPassword
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
      signingConfig = signingConfigs.getByName("release")
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

  // F-Droid flavor dependencies
  add("fdroidImplementation", "org.osmdroid:osmdroid-android:6.1.14")
  add("fdroidImplementation", "org.osmdroid:osmdroid-wms:6.1.14")
  add("fdroidImplementation", "org.osmdroid:osmdroid-mapsforge:6.1.14")
  add("fdroidImplementation", "org.osmdroid:osmdroid-geopackage:6.1.14")

  // Play Store flavor dependencies
  add("playstoreImplementation", "com.garmin.connectiq:ciq-companion-app-sdk:2.4.0@aar")
  add("playstoreImplementation", libs.firebase.config)
  add("playstoreImplementation", libs.play.services.wearable)
  add("playstoreImplementation", libs.play.review.ktx)
  add("playstoreImplementation", libs.play.app.update.ktx)
  add("playstoreImplementation", platform(libs.firebase.bom))
  add("playstoreImplementation", libs.firebase.crashlytics)
}
