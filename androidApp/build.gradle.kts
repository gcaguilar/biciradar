import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.android.builtin.kotlin)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.firebase.crashlytics) apply false
  alias(libs.plugins.google.services) apply false
}

val localProperties = Properties().apply {
  val localPropertiesFile = rootProject.file("local.properties")
  if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use(::load)
  }
}

val firebaseConfigFile = layout.projectDirectory.file("google-services.json").asFile
val firebaseCrashlyticsEnabled = firebaseConfigFile.exists()

if (firebaseCrashlyticsEnabled) {
  apply(plugin = "com.google.gms.google-services")
  apply(plugin = "com.google.firebase.crashlytics")
}

val googleMapsApiKey: String = providers.environmentVariable("GOOGLE_MAPS_API_KEY")
  .orElse(providers.gradleProperty("googleMapsApiKey"))
  .orElse(localProperties.getProperty("googleMapsApiKey") ?: "")
  .get()
  .also { key ->
    if (key.isEmpty()) logger.warn("WARNING: googleMapsApiKey is empty — Google Maps will not work in this build")
  }

val ciKeystorePath: String = providers.environmentVariable("BIZI_CI_KEYSTORE_PATH").orElse("").get()
val ciKeystorePassword: String = providers.environmentVariable("BIZI_CI_KEYSTORE_PASSWORD").orElse("").get()
val ciKeyAlias: String = providers.environmentVariable("BIZI_CI_KEY_ALIAS").orElse("").get()
val ciKeyPassword: String = providers.environmentVariable("BIZI_CI_KEY_PASSWORD").orElse("").get()

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_17)
  }
}

android {
  namespace = "com.gcaguilar.bizizaragoza"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.gcaguilar.bizizaragoza"
    minSdk = 26
    targetSdk = 36
    versionCode = 29558236
    versionName = "2026.03.14.1416"
    manifestPlaceholders["googleMapsApiKey"] = googleMapsApiKey
  }

  buildFeatures {
    buildConfig = true
    compose = true
  }

  lint {
    // False positive: ComponentActivity ships its own modern FragmentActivity —
    // the fragment version check does not apply here.
    disable += "InvalidFragmentVersionForActivityResult"
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
      manifestPlaceholders["googleMapsApiKey"] = googleMapsApiKey
    }
    debug {
      if (ciKeystorePath.isNotEmpty()) {
        signingConfig = signingConfigs.create("ci") {
          storeFile = file(ciKeystorePath)
          storePassword = ciKeystorePassword
          keyAlias = ciKeyAlias
          keyPassword = ciKeyPassword
        }
      }
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  sourceSets {
    getByName("main") {
      manifest.srcFile("src/androidMain/AndroidManifest.xml")
      kotlin.srcDirs("src/androidMain/kotlin")
      res.srcDirs("src/androidMain/res")
    }
    getByName("test") {
      kotlin.srcDirs("src/androidUnitTest/kotlin")
    }
  }

  dependencies {
    implementation(project(":shared:core"))
    implementation(project(":shared:mobile-ui"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    if (firebaseCrashlyticsEnabled) {
      implementation(platform(libs.firebase.bom))
      implementation(libs.firebase.analytics)
      implementation(libs.firebase.crashlytics)
    }
    testImplementation(libs.junit)
  }
}
