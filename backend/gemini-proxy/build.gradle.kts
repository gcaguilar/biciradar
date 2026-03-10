plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  application
}

application {
  mainClass.set("com.gcaguilar.bizizaragoza.backend.gemini.GeminiProxyKt")
}

dependencies {
  implementation(libs.ktor.client.cio)
  implementation(libs.ktor.client.content.negotiation)
  implementation(libs.ktor.serialization.kotlinx.json)
  implementation(libs.ktor.server.cio)
  implementation(libs.ktor.server.content.negotiation)
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.status.pages)
  implementation(libs.serialization.json)

  testImplementation(libs.junit)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.ktor.server.test.host)
}
