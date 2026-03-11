import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.compiler)
}

val geminiProxyBaseUrl = providers.environmentVariable("BIZI_GEMINI_PROXY_BASE_URL")
  .orElse("http://10.0.2.2:8080")
val googleMapsApiKey = providers.environmentVariable("GOOGLE_MAPS_API_KEY")
  .orElse("")

kotlin {
  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }

  sourceSets {
    androidUnitTest.dependencies {
      implementation(libs.junit)
    }
  }
}

android {
  namespace = "com.gcaguilar.bizizaragoza"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.gcaguilar.bizizaragoza"
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "0.1.0"
    buildConfigField("String", "GEMINI_PROXY_BASE_URL", "\"${geminiProxyBaseUrl.get()}\"")
    manifestPlaceholders["googleMapsApiKey"] = googleMapsApiKey.get()
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

  dependencies {
    implementation(project(":shared:core"))
    implementation(project(":shared:mobile-ui"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
  }
}
