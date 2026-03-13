import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.android.builtin.kotlin)
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

val firebaseConfigFile = layout.projectDirectory.file("google-services.json").asFile
val firebaseCrashlyticsEnabled = firebaseConfigFile.exists()

if (firebaseCrashlyticsEnabled) {
  apply(plugin = "com.google.gms.google-services")
  apply(plugin = "com.google.firebase.crashlytics")
}

val googleMapsApiKey = providers.environmentVariable("GOOGLE_MAPS_API_KEY")
  .orElse(providers.gradleProperty("googleMapsApiKey"))
  .orElse(localProperties.getProperty("googleMapsApiKey") ?: "")

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_17)
  }
}

android {
  namespace = "com.gcaguilar.bizizaragoza"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.gcaguilar.bizizaragoza"
    minSdk = 26
    targetSdk = 36
    versionCode = 29557013
    versionName = "2026.03.13.1753"
    manifestPlaceholders["googleMapsApiKey"] = googleMapsApiKey.get()
  }

  buildFeatures {
    buildConfig = true
    compose = true
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      manifestPlaceholders["googleMapsApiKey"] = googleMapsApiKey.get()
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  sourceSets {
    getByName("main") {
      manifest.srcFile("src/androidMain/AndroidManifest.xml")
      kotlin.srcDirs("src/androidMain/kotlin")
      res.srcDirs("src/androidMain/res")
    }
    getByName("test") {
      kotlin.srcDirs("src/androidUnitTest/kotlin")
    }
  }

  dependencies {
    implementation(project(":shared:core"))
    implementation(project(":shared:mobile-ui"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    if (firebaseCrashlyticsEnabled) {
      implementation(platform(libs.firebase.bom))
      implementation(libs.firebase.analytics)
      implementation(libs.firebase.crashlytics)
    }
    testImplementation(libs.junit)
  }
}
