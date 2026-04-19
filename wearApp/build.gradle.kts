import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.google.services) apply false
}

val localProperties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use(::load)
        }
    }

val wearApplicationId = "com.gcaguilar.biciradar"
val firebaseConfigFile = layout.projectDirectory.file("google-services.json").asFile
val firebaseCrashlyticsEnabled =
    firebaseConfigFile.exists() &&
    firebaseConfigFile.readText().contains("\"package_name\": \"$wearApplicationId\"")

if (firebaseCrashlyticsEnabled) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

android {
    namespace = "com.gcaguilar.biciradar.wear"
    compileSdk = 36

    defaultConfig {
        applicationId = wearApplicationId
        minSdk = 30
        targetSdk = 36
        versionCode = 29568116
        versionName = "0.22.6"
    }

    flavorDimensions += "tier"
    productFlavors {
        create("fdroid") {
            dimension = "tier"
            // F-Droid specific configuration
            applicationIdSuffix = ".fdroid"
            versionNameSuffix = "-fdroid"
        }
        create("playstore") {
            dimension = "tier"
            // Play Store specific configuration (default)
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    signingConfigs {
        create("release") {
            val keystorePath =
                project.findProperty("BIZI_CI_KEYSTORE_PATH") as? String
                    ?: System.getenv("BIZI_CI_KEYSTORE_PATH")
            val keystorePassword =
                project.findProperty("BIZI_CI_KEYSTORE_PASSWORD") as? String
                    ?: System.getenv("BIZI_CI_KEYSTORE_PASSWORD")
            val keyAliasEnv =
                project.findProperty("BIZI_CI_KEY_ALIAS") as? String
                    ?: System.getenv("BIZI_CI_KEY_ALIAS")
            val keyPassword =
                project.findProperty("BIZI_CI_KEY_PASSWORD") as? String
                    ?: System.getenv("BIZI_CI_KEY_PASSWORD")

            if (keystorePath != null && keystorePassword != null && keyAliasEnv != null && keyPassword != null) {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                keyAlias = keyAliasEnv
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":shared:core"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.metro.runtime)
    implementation(libs.androidx.wear.protolayout)
    implementation(libs.androidx.wear.protolayout.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.navigation)
    implementation(libs.androidx.wear.tiles)
    implementation(libs.androidx.wear.ongoing)
    implementation(libs.androidx.wear.watchface.complications.data.source)
    implementation(libs.androidx.wear.watchface.complications.data.source.ktx)
    testImplementation(libs.junit)
    
    // Conditional dependencies for F-Droid vs Play Store flavors
    fdroidImplementation("org.osmdroid:osmdroid-android:6.1.14") // OpenStreetMap alternative (if needed)
    fdroidImplementation("org.osmdroid:osmdroid-wms:6.1.14")
    fdroidImplementation("org.osmdroid:osmdroid-mapsforge:6.1.14")
    fdroidImplementation("org.osmdroid:osmdroid-geopackage:6.1.14")
    
    playstoreImplementation(libs.play.services.wearable)
    playstoreImplementation(platform(libs.firebase.bom))
    playstoreImplementation(libs.firebase.crashlytics)
}