import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
}

kotlin {
  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }
  iosArm64()
  iosSimulatorArm64()

  applyDefaultHierarchyTemplate()

  listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
    target.binaries.framework {
      baseName = "BiziMobileUi"
      isStatic = true
      export(project(":shared:core"))
    }
  }

  sourceSets {
    commonMain.dependencies {
      api(project(":shared:core"))
      implementation(compose.foundation)
      implementation(compose.materialIconsExtended)
      implementation(compose.material3)
      implementation(compose.runtime)
      implementation(compose.ui)
      implementation(libs.metro.runtime)
    }
    androidMain.dependencies {
      implementation(libs.maps.compose)
      implementation(libs.play.services.maps)
    }
  }
}

android {
  namespace = "com.gcaguilar.bizizaragoza.shared.mobileui"
  compileSdk = 36

  defaultConfig {
    minSdk = 26
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}
