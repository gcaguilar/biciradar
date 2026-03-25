import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.firebase.crashlytics) apply false
  alias(libs.plugins.google.services) apply false
}

val localProperties = Properties().apply {
  val localPropertiesFile = rootProject.file("local.properties")
  if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use(::load)
  }
}

val wearApplicationId = "com.gcaguilar.biciradar"
val firebaseConfigFile = layout.projectDirectory.file("google-services.json").asFile
val firebaseCrashlyticsEnabled = firebaseConfigFile.exists() &&
  firebaseConfigFile.readText().contains("\"package_name\": \"$wearApplicationId\"")
val ciKeystorePath: String = providers.environmentVariable("BIZI_CI_KEYSTORE_PATH").orElse("").get()
val ciKeystorePassword: String = providers.environmentVariable("BIZI_CI_KEYSTORE_PASSWORD").orElse("").get()
val ciKeyAlias: String = providers.environmentVariable("BIZI_CI_KEY_ALIAS").orElse("").get()
val ciKeyPassword: String = providers.environmentVariable("BIZI_CI_KEY_PASSWORD").orElse("").get()

if (firebaseCrashlyticsEnabled) {
  apply(plugin = "com.google.gms.google-services")
  apply(plugin = "com.google.firebase.crashlytics")
}

android {
  namespace = "com.gcaguilar.biciradar.wear"
  compileSdk = 36

  defaultConfig {
    applicationId = wearApplicationId
    minSdk = 30
    targetSdk = 36
    versionCode = 29568077
    versionName = "0.16.0"
  }

  buildFeatures {
    buildConfig = true
    compose = true
  }

  lint {
    disable += "InvalidFragmentVersionForActivityResult"
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      if (ciKeystorePath.isNotEmpty()) {
        signingConfig = signingConfigs.create("ciRelease") {
          storeFile = file(ciKeystorePath)
          storePassword = ciKeystorePassword
          keyAlias = ciKeyAlias
          keyPassword = ciKeyPassword
        }
      }
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
    }
    debug {
      if (ciKeystorePath.isNotEmpty()) {
        signingConfig = signingConfigs.create("ciDebug") {
          storeFile = file(ciKeystorePath)
          storePassword = ciKeystorePassword
          keyAlias = ciKeyAlias
          keyPassword = ciKeyPassword
        }
      }
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  dependencies {
    implementation(project(":shared:core"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.metro.runtime)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.navigation)
    if (firebaseCrashlyticsEnabled) {
      implementation(platform(libs.firebase.bom))
      implementation(libs.firebase.crashlytics)
    }
  }
}
