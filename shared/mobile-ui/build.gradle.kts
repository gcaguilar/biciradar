import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  android {
    compileSdk = 36
    minSdk = 26
    namespace = "com.gcaguilar.bizizaragoza.shared.mobileui"
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
      implementation(libs.androidx.lifecycle.runtime.compose)
      implementation(libs.navigation.compose)
    }
    androidMain.dependencies {
      implementation(libs.maps.compose)
      implementation(libs.play.services.maps)
      implementation(libs.androidx.activity.compose)
    }
  }
}
