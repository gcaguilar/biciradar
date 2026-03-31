import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

compose.resources {
  publicResClass = true
  packageOfResClass = "com.gcaguilar.biciradar.mobile_ui.generated.resources"
  generateResClass = always
}

kotlin {
  android {
    compileSdk = 36
    minSdk = 26
    namespace = "com.gcaguilar.biciradar.shared.mobileui"
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
    androidResources {
      enable = true
    }
  }
  jvm {
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
      linkerOpts("-lsqlite3")
    }
  }

  sourceSets {
    commonMain.dependencies {
      api(project(":shared:core"))
      implementation(compose.foundation)
      implementation(compose.materialIconsExtended)
      implementation(libs.jetbrains.compose.material3)
      implementation(compose.runtime)
      implementation(compose.ui)
      implementation(libs.jetbrains.compose.material3.adaptive)
      implementation(libs.compose.resources)
      implementation(libs.metro.runtime)
      implementation(libs.androidx.lifecycle.runtime.compose)
      implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
      implementation(libs.navigation.compose)
    }
    commonTest.dependencies {
      implementation(libs.coroutines.test)
      implementation(libs.kotlin.test)
    }
    androidMain.dependencies {
      implementation(libs.maps.compose)
      implementation(libs.play.services.maps)
      implementation(libs.androidx.activity.compose)
    }
  }
}
