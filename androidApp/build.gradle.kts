import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.google.services)
  alias(libs.plugins.firebase.crashlytics)
}

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
  namespace = "com.gcaguilar.biciradar"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.gcaguilar.biciradar"
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "0.1.0"
    manifestPlaceholders["googleMapsApiKey"] = googleMapsApiKey.get()
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
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
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
    implementation(project(":shared:mobile-ui"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.moko.resources)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
  }
}
