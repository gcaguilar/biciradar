import org.jetbrains.kotlin.gradle.plugin.mpp.export.SwiftExportVisibility
import org.jetbrains.kotlin.gradle.swiftexport.ExperimentalSwiftExportDsl
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
  alias(libs.plugins.kotlin.multiplatform)
}

// Umbrella module for Swift export on Apple Watch. Mirrors :shared:ios-export but only
// exposes :shared:core, which is all the watch app consumes.
kotlin {
  watchosArm64()
  watchosDeviceArm64()
  watchosSimulatorArm64()

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      api(project(":shared:core"))
    }
  }

  @OptIn(ExperimentalSwiftExportDsl::class)
  export {
    swift {
      moduleName = "SharedWatch"
      xcodeIntegration {
        configure(project(":shared:core")) {
          moduleName = "Core"
          rootPackage = "com.gcaguilar.biciradar.core"
        }
        configure(libs.okio, SwiftExportVisibility.HIDDEN)
        configure(libs.ktor.client.core, SwiftExportVisibility.HIDDEN)
        configure(libs.sqldelight.runtime, SwiftExportVisibility.HIDDEN)
        configure(libs.serialization.json, SwiftExportVisibility.HIDDEN)
        configure(libs.metro.runtime, SwiftExportVisibility.HIDDEN)
      }
    }
  }
}

// Keep in sync with WATCHOS_DEPLOYMENT_TARGET in apple/project.yml.
// Kotlin/Native otherwise emits its own default minimum (14.0 / 7.0), which clashes with the
// Xcode project and triggers "Overriding deployment version" warnings at link time.
val konanOsVersionMin = "-Xoverride-konan-properties=osVersionMin.watchos_arm64=11.0;osVersionMin.watchos_device_arm64=11.0;osVersionMin.watchos_simulator_arm64=11.0"
tasks.withType<KotlinNativeCompile>().configureEach {
  compilerOptions.freeCompilerArgs.add(konanOsVersionMin)
}
tasks.withType<KotlinNativeLink>().configureEach {
  toolOptions.freeCompilerArgs.add(konanOsVersionMin)
}
