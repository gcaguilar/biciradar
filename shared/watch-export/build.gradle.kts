import org.jetbrains.kotlin.gradle.plugin.mpp.export.SwiftExportVisibility
import org.jetbrains.kotlin.gradle.swiftexport.ExperimentalSwiftExportDsl

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
