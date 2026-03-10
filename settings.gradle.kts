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
}

rootProject.name = "bizi-zaragoza"

include(":shared:core")
include(":shared:mobile-ui")
include(":androidApp")
include(":wearApp")
include(":backend:gemini-proxy")

