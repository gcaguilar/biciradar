# Conservative rules retained for the F-Droid variant. Android F-Droid
# currently disables minification; scoping its former rules keeps this change
# isolated to Play Store.

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
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

-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okio.**
-keep class okio.** { *; }

-keep interface com.gcaguilar.biciradar.core.SharedGraph { *; }
-keep class com.gcaguilar.biciradar.core.SharedGraph$* { *; }
-keep class * implements com.gcaguilar.biciradar.core.SharedGraph { *; }
-keep class **$$MetroDependencyGraph { *; }
-keep class **$$MetroDependencyGraph$* { *; }
-keepclasseswithmembernames class * {
    @dev.zacsweers.metro.Inject <init>(...);
}

-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Lazy {
    public <methods>;
}

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.work.impl.WorkDatabase_Impl { <init>(); *; }
-keep class com.gcaguilar.biciradar.SavedPlaceAlertsWorker { <init>(android.content.Context, androidx.work.WorkerParameters); }

-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }
-dontwarn com.google.android.gms.**
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

-keep class com.garmin.android.connectiq.** { *; }
-dontwarn com.garmin.android.connectiq.**
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}
-keep class * extends android.app.Activity
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep class * extends android.app.Service
