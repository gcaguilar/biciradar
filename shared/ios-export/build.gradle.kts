import org.jetbrains.kotlin.gradle.swiftexport.ExperimentalSwiftExportDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.export.SwiftExportVisibility

plugins {
  alias(libs.plugins.kotlin.multiplatform)
}

// Umbrella module for Swift export. It has no code of its own: it only depends on the
// shared modules so that they are exported in full while their own dependencies are only
// exported transitively (or hidden below).
kotlin {
  iosArm64()
  iosSimulatorArm64()

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      api(project(":shared:core"))
      api(project(":shared:mobile-ui"))
    }
  }

  @OptIn(ExperimentalSwiftExportDsl::class)
  export {
    swift {
      moduleName = "Shared"
      xcodeIntegration {
        configure(project(":shared:core")) {
          moduleName = "Core"
          rootPackage = "com.gcaguilar.biciradar.core"
        }
        configure(project(":shared:mobile-ui")) {
          moduleName = "MobileUi"
          rootPackage = "com.gcaguilar.biciradar.mobileui"
        }
        configure(libs.androidx.lifecycle.viewmodel, SwiftExportVisibility.HIDDEN)
        configure("androidx.lifecycle:lifecycle-common", SwiftExportVisibility.HIDDEN)
        configure("androidx.lifecycle:lifecycle-viewmodel-savedstate", SwiftExportVisibility.HIDDEN)
        configure("androidx.savedstate:savedstate", SwiftExportVisibility.HIDDEN)
        configure("androidx.collection:collection", SwiftExportVisibility.HIDDEN)
        configure("org.jetbrains.androidx.navigation:navigation-common", SwiftExportVisibility.HIDDEN)
        configure("org.jetbrains.androidx.navigation:navigation-runtime", SwiftExportVisibility.HIDDEN)
        configure(libs.navigation.compose, SwiftExportVisibility.HIDDEN)
        configure(libs.okio, SwiftExportVisibility.HIDDEN)
        configure(libs.ktor.client.core, SwiftExportVisibility.HIDDEN)
        configure(libs.sqldelight.runtime, SwiftExportVisibility.HIDDEN)
        configure(libs.serialization.json, SwiftExportVisibility.HIDDEN)
        configure(libs.metro.runtime, SwiftExportVisibility.HIDDEN)
      }
    }
  }
}
