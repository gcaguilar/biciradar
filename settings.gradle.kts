pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
  versionCatalogs {
    create("playstore") {
      from(files("gradle/libs.playstore.versions.toml"))
    }
  }
}

rootProject.name = "biciradar"

include(":shared:core")
include(":shared:mobile-ui")
include(":shared:test-utils")
include(":androidApp")
include(":wearApp")
include(":desktopApp")
