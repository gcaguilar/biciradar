import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
  base
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.android.kotlin.multiplatform.library) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.compose.multiplatform) apply false
  alias(libs.plugins.firebase.crashlytics) apply false
  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.ktlint) apply false
  alias(libs.plugins.metro) apply false
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
      exclude("**/build/**")
      exclude("**/generated/**")
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

tasks.named("check") {
  dependsOn(ktlintCheckAll)
}
