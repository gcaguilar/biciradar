import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
  base
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.android.kotlin.multiplatform.library) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.compose.multiplatform) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.ktlint) apply false
  alias(libs.plugins.metro) apply false
  id("com.github.ben-manes.versions") version "0.54.0"
}

allprojects {
  group = "com.gcaguilar.biciradar"
  version = "0.1.0"
}

subprojects {
  apply(plugin = "org.jlleitschuh.gradle.ktlint")

  extensions.configure<KtlintExtension> {
    outputToConsole.set(true)
    ignoreFailures.set(false)
    filter {
      exclude { element ->
        val path = element.file.path
        path.contains("/build/generated/")
      }
    }
  }
}

val ktlintCheckAll by tasks.registering {
  group = LifecycleBasePlugin.VERIFICATION_GROUP
  description = "Runs ktlint checks for all subprojects."
  dependsOn(subprojects.map { "${it.path}:ktlintCheck" })
}

val ktlintFormatAll by tasks.registering {
  group = "formatting"
  description = "Formats Kotlin sources with ktlint in all subprojects."
  dependsOn(subprojects.map { "${it.path}:ktlintFormat" })
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
  group = "help"
  description = "Checks for available dependency and Gradle version updates."

  fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
  }

  revision = "release"
  rejectVersionIf {
    isNonStable(candidate.version) && !isNonStable(currentVersion)
  }
  outputFormatter = "plain,json,html"
  outputDir = "build/dependencyUpdates"
  reportfileName = "report"
}

tasks.named("check") {
  dependsOn(ktlintCheckAll)
}
