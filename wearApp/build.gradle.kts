import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.firebase.crashlytics) apply false
}

val googleServicesJson = file("google-services.json")
if (googleServicesJson.exists()) {
  apply(plugin = libs.plugins.google.services.get().pluginId)
  apply(plugin = libs.plugins.firebase.crashlytics.get().pluginId)
}

kotlin {
  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }
}

android {
  namespace = "com.gcaguilar.biciradar.wear"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.gcaguilar.biciradar"
    minSdk = 30
    targetSdk = 36
    versionCode = 29568076
    versionName = "0.15.0"
  }

  buildFeatures {
    buildConfig = true
    compose = true
  }

  signingConfigs {
    create("release") {
      val keystorePath = project.findProperty("BIZI_CI_KEYSTORE_PATH") as? String
        ?: System.getenv("BIZI_CI_KEYSTORE_PATH")
      val keystorePassword = project.findProperty("BIZI_CI_KEYSTORE_PASSWORD") as? String
        ?: System.getenv("BIZI_CI_KEYSTORE_PASSWORD")
      val keyAliasEnv = project.findProperty("BIZI_CI_KEY_ALIAS") as? String
        ?: System.getenv("BIZI_CI_KEY_ALIAS")
      val keyPassword = project.findProperty("BIZI_CI_KEY_PASSWORD") as? String
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
      isMinifyEnabled = false
      signingConfig = signingConfigs.getByName("release")
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
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
  }
}
