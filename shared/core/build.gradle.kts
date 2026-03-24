import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.metro)
  alias(libs.plugins.sqldelight)
}

compose.resources {
  publicResClass = true
  packageOfResClass = "com.gcaguilar.biciradar.shared.core.generated.resources"
  generateResClass = always
}

kotlin {
  android {
    compileSdk = 36
    minSdk = 26
    namespace = "com.gcaguilar.biciradar.shared.core"
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
    androidResources {
      enable = true
    }
  }
  jvm()
  iosArm64()
  iosSimulatorArm64()

  applyDefaultHierarchyTemplate()

  listOf(
    iosArm64(),
    iosSimulatorArm64(),
  ).forEach { target ->
    target.binaries.framework {
      baseName = "BiziSharedCore"
      isStatic = true
      linkerOpts("-lsqlite3")
    }
  }
  sourceSets {
    commonMain.dependencies {
      implementation(compose.runtime)
      implementation(libs.compose.resources)
      implementation(libs.coroutines.core)
      implementation(libs.ktor.client.content.negotiation)
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.serialization.kotlinx.json)
      implementation(libs.metro.runtime)
      implementation(libs.okio)
      implementation(libs.serialization.json)
      implementation(libs.sqldelight.runtime)
      implementation(libs.sqldelight.coroutines)
    }
    commonTest.dependencies {
      implementation(libs.coroutines.test)
      implementation(libs.kotlin.test)
    }
    androidMain.dependencies {
      implementation(libs.coroutines.play.services)
      implementation(libs.ktor.client.okhttp)
      implementation(libs.play.services.location)
      implementation(libs.play.services.wearable)
      implementation(libs.sqldelight.runtime)
      implementation(libs.sqldelight.android.driver)
    }
    jvmMain.dependencies {
      implementation(libs.ktor.client.cio)
      implementation(libs.sqldelight.sqlite.driver)
    }
    val iosMain by getting {
      dependencies {
        implementation(libs.ktor.client.darwin)
        implementation(libs.sqldelight.runtime)
        implementation(libs.sqldelight.native.driver)
      }
    }
  }
}

sqldelight {
  databases {
    create("BiciRadarDatabase") {
      packageName.set("com.gcaguilar.biciradar.core.local")
    }
  }
}
