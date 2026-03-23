pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
  resolutionStrategy {
    eachPlugin {
      if (requested.id.id == "dev.icerock.mobile.multiplatform-resources") {
        useModule("dev.icerock.moko:resources-generator:${requested.version}")
      }
    }
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "biciradar"

include(":shared:core")
include(":shared:mobile-ui")
include(":androidApp")
include(":wearApp")
