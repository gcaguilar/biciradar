# Play Store release rules.
# Dependencies publish their own consumer rules. Keep app rules limited to
# reflection, which R8 cannot infer from static usage.

# Loaded by AndroidPlatformBindings using Class.forName() and getMethod().
-keep,allowoptimization class com.gcaguilar.biciradar.AndroidOptionalServicesFactory {
    public static com.gcaguilar.biciradar.core.platform.AndroidOptionalServices create(android.content.Context);
}
