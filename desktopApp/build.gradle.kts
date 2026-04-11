import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val appVersion = rootProject.file("VERSION").readText().trim()
val desktopPackageVersion =
  appVersion
    .split('.')
    .let { parts ->
      if (parts.firstOrNull() == "0") listOf("1") + parts.drop(1) else parts
    }.joinToString(".")

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
}

dependencies {
  implementation(project(":shared:mobile-ui"))
  implementation(compose.desktop.currentOs)
  implementation(libs.coroutines.swing)
  implementation(libs.slf4j.simple)
}

compose.desktop {
  application {
    mainClass = "com.gcaguilar.biciradar.desktop.MainKt"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg)
      packageName = "BiciRadar"
      packageVersion = desktopPackageVersion
      description = "BiciRadar for macOS"
    }
  }
}
