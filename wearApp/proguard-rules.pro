# ============================================================
# Kotlinx Serialization
# ============================================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses @kotlinx.serialization.Serializable class ** { *; }
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1>$$serializer {
    static <1>$$serializer INSTANCE;
    <1> deserialize(kotlinx.serialization.encoding.Decoder);
}

# ============================================================
# Ktor
# ============================================================
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okio.**
-keep class okio.** { *; }

# ============================================================
# Metro
# ============================================================
-keep interface com.gcaguilar.biciradar.core.SharedGraph { *; }
-keep class com.gcaguilar.biciradar.core.SharedGraph$* { *; }
-keep class * implements com.gcaguilar.biciradar.core.SharedGraph { *; }
-keep class **$$MetroDependencyGraph { *; }
-keep class **$$MetroDependencyGraph$* { *; }
-keepclasseswithmembernames class * {
    @dev.zacsweers.metro.Inject <init>(...);
}

# ============================================================
# Kotlin
# ============================================================
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# ============================================================
# Coroutines
# ============================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ============================================================
# AndroidX / Compose / Wear
# ============================================================
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**
-dontwarn androidx.wear.**

# ============================================================
# Firebase Crashlytics
# ============================================================
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ============================================================
# Android entry points
# ============================================================
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}
-keep class * extends android.app.Activity
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep class * extends android.app.Service
