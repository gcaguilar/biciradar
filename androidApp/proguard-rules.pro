# ============================================================
# Kotlinx Serialization
# ============================================================
# Keep the serialization annotations and the generated serializers.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Keep all @Serializable classes and their companions/serializers.
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
# OkHttp engine used by Ktor on Android
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okio.**
-keep class okio.** { *; }

# ============================================================
# Metro (compile-time DI — generated code is concrete, no reflection)
# Keep graph interfaces and their factories so they are not removed.
# ============================================================
-keep interface com.gcaguilar.bizizaragoza.core.SharedGraph { *; }
-keep class com.gcaguilar.bizizaragoza.core.SharedGraph$* { *; }
-keep class * implements com.gcaguilar.bizizaragoza.core.SharedGraph { *; }
# Metro-generated $$MetroDependencyGraph implementations
-keep class **$$MetroDependencyGraph { *; }
-keep class **$$MetroDependencyGraph$* { *; }
# Keep @Inject-annotated constructors so Metro-generated factories can call them
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
-keepclassmembers class kotlin.Lazy {
    public <methods>;
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
# AndroidX / Compose
# Compose ships its own consumer rules via AAR; this covers
# the few edge cases that can still be stripped.
# ============================================================
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# ============================================================
# Google Maps / Play Services
# These ship their own consumer proguard rules via AAR, but
# keep the key public API surface just in case.
# ============================================================
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }
-dontwarn com.google.android.gms.**

# ============================================================
# Firebase (Analytics + Crashlytics)
# Firebase AARs ship their own consumer proguard rules.
# These are extra guards for the app-level entry points.
# ============================================================
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ============================================================
# App — keep Parcelable / Android entry points
# ============================================================
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}
-keep class * extends android.app.Activity
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep class * extends android.app.Service
