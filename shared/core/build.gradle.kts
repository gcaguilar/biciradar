import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.metro)
  alias(libs.plugins.moko.resources)
  alias(libs.plugins.sqldelight)
}

multiplatformResources {
  resourcesPackage.set("com.gcaguilar.biciradar.core")
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
      implementation(libs.moko.resources)
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
    val watchosMain by getting {
      dependencies {
        implementation(libs.ktor.client.darwin)
        implementation(libs.sqldelight.runtime)
        implementation(libs.sqldelight.native.driver)
      }
    }
  }
}

android {
  namespace = "com.gcaguilar.biciradar.shared.core"
  compileSdk = 36

  defaultConfig {
    minSdk = 26
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

tasks.matching { it.name == "watchosSimulatorArm64Test" }.configureEach {
  enabled = false
}

sqldelight {
  databases {
    create("BiciRadarDatabase") {
      packageName.set("com.gcaguilar.biciradar.core.local")
      srcDirs.setFrom("src/commonMain/sqldelight")
    }
  }
}
