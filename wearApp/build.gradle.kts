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

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_17)
  }
}

android {
  namespace = "com.gcaguilar.bizizaragoza.wear"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.gcaguilar.bizizaragoza.wear"
    minSdk = 30
    targetSdk = 36
    versionCode = 29557312
    versionName = "2026.03.13.2252"
  }

  buildFeatures {
    buildConfig = true
    compose = true
  }

  buildTypes {
    release {
      isMinifyEnabled = false
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
