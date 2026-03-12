import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.android.builtin.kotlin)
  alias(libs.plugins.compose.compiler)
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
    versionCode = 1
    versionName = "0.1.0"
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
  }
}
