plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.firebase.crashlytics) apply false
}

val googleServicesJson = file("google-services.json")
if (googleServicesJson.exists()) {
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
}

val googleMapsApiKey =
  providers
    .environmentVariable("GOOGLE_MAPS_API_KEY")
    .orElse("")

android {
  namespace = "com.gcaguilar.biciradar"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.gcaguilar.biciradar"
    minSdk = 26
    targetSdk = 36
    versionCode = 29568113
    versionName = "0.22.5"
    manifestPlaceholders["googleMapsApiKey"] = googleMapsApiKey.get()
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
    implementation("com.garmin.connectiq:ciq-companion-app-sdk:2.2.0@aar")
    implementation(libs.google.material)
    implementation(libs.play.services.wearable)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    testImplementation(libs.junit)
  }
}
