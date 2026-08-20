# Play Store release rules.
# Dependencies publish their own consumer rules. These entries protect only
# app classes and members reached through reflection.

# Loaded by AndroidPlatformBindings using Class.forName() and getMethod().
-keep,allowoptimization class com.gcaguilar.biciradar.wear.AndroidOptionalServicesFactory {
    public static com.gcaguilar.biciradar.core.platform.AndroidOptionalServices create(android.content.Context);
}

# Loaded by WearPhoneRouteRequester using a class name, constructor, and method
# names. Implementations called by these methods remain free to be optimized.
-keep,allowoptimization class com.gcaguilar.biciradar.wear.PlaystoreWearPhoneRouteRequesterDelegate {
    public <init>(android.content.Context);
    public boolean isRouteAvailable();
    public boolean requestRoute(java.lang.String);
}
