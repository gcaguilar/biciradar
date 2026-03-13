import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.metro)
}

kotlin {
  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }
  jvm()
  iosArm64()
  iosSimulatorArm64()
  watchosArm64()
  val watchosSimulatorTarget = watchosSimulatorArm64()

  applyDefaultHierarchyTemplate()

  listOf(
    iosArm64(),
    iosSimulatorArm64(),
    watchosArm64(),
    watchosSimulatorTarget,
  ).forEach { target ->
    target.binaries.framework {
      baseName = "BiziSharedCore"
      isStatic = true
    }
  }
  sourceSets {
    commonMain.dependencies {
      implementation(libs.coroutines.core)
      implementation(libs.ktor.client.content.negotiation)
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.serialization.kotlinx.json)
      implementation(libs.metro.runtime)
      implementation(libs.okio)
      implementation(libs.serialization.json)
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
    }
    jvmMain.dependencies {
      implementation(libs.ktor.client.cio)
    }
    val iosMain by getting {
      dependencies {
        implementation(libs.ktor.client.darwin)
      }
    }
    val watchosMain by getting {
      dependencies {
        implementation(libs.ktor.client.darwin)
      }
    }
  }
}

android {
  namespace = "com.gcaguilar.bizizaragoza.shared.core"
  compileSdk = 36

  defaultConfig {
    minSdk = 26
  }

}

tasks.matching { it.name == "watchosSimulatorArm64Test" }.configureEach {
  enabled = false
}
