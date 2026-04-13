import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.metro)
  alias(libs.plugins.sqldelight)
}

kotlin {
  android {
    compileSdk = 36
    minSdk = 26
    namespace = "com.gcaguilar.biciradar.shared.core"
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

  listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
    target.binaries.framework {
      baseName = "BiziSharedCore"
      isStatic = true
      freeCompilerArgs += "-Xoverride-konan-properties=minVersion.ios=16.0"
      linkerOpts("-lsqlite3")
    }
  }

  listOf(watchosArm64(), watchosDeviceArm64(), watchosSimulatorArm64()).forEach { target ->
    target.binaries.framework {
      baseName = "BiziSharedCore"
      isStatic = true
      linkerOpts("-lsqlite3")
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
      implementation(libs.sqldelight.runtime)
      implementation(libs.sqldelight.coroutines)
    }
    commonTest.dependencies {
      implementation(libs.coroutines.test)
      implementation(libs.kotlin.test)
      implementation(project(":shared:test-utils"))
      implementation(libs.turbine)
    }
    androidMain.dependencies {
      implementation(libs.coroutines.play.services)
      implementation(project.dependencies.platform(libs.firebase.bom))
      implementation(libs.firebase.config)
      implementation(libs.firebase.crashlytics)
      implementation(libs.ktor.client.okhttp)
      implementation(libs.play.services.location)
      implementation(libs.play.services.wearable)
      implementation(libs.play.review.ktx)
      implementation(libs.play.app.update.ktx)
      implementation(libs.sqldelight.runtime)
      implementation(libs.sqldelight.android.driver)
    }
    jvmMain.dependencies {
      implementation(libs.ktor.client.cio)
      implementation(libs.sqldelight.sqlite.driver)
    }
    appleMain.dependencies {
      implementation(libs.ktor.client.darwin)
      implementation(libs.sqldelight.runtime)
      implementation(libs.sqldelight.native.driver)
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
