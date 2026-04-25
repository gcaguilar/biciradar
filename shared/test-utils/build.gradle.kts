import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
  android {
    compileSdk = 37
    minSdk = 26
    namespace = "com.gcaguilar.biciradar.shared.testutils"
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }
  jvm()
  iosArm64()
  iosSimulatorArm64()
  watchosArm64()
  watchosDeviceArm64()
  watchosSimulatorArm64()

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      api(project(":shared:core"))
      implementation(libs.coroutines.core)
    }
  }
}
