import Core
import KotlinRuntime
import MobileUi
import Foundation
import FirebaseCore
import FirebaseCrashlytics
import FirebaseRemoteConfig

enum FirebaseBootstrap {
    static let remoteConfigBridge: platform.IOSRemoteConfigBridge = AppleFirebaseRemoteConfigBridge.shared
    static let crashlyticsBridge: platform.IOSCrashlyticsBridge = AppleCrashlyticsBridge.shared

    static var isConfigured: Bool { FirebaseApp.app() != nil }

    static func configureIfAvailable() {
        guard Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil else { return }
        guard FirebaseApp.app() == nil else { return }

        FirebaseApp.configure()
        let remoteConfig = RemoteConfig.remoteConfig()
        let settings = RemoteConfigSettings()
        settings.minimumFetchInterval = 3_600
        remoteConfig.configSettings = settings
        Crashlytics.crashlytics().setCustomValue("ios", forKey: "platform")
        Crashlytics.crashlytics().setCustomValue(Bundle.main.bundleIdentifier ?? "unknown", forKey: "bundle_id")
        Crashlytics.crashlytics().setCustomValue(Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "unknown", forKey: "app_version")
    }
}

private final class AppleCrashlyticsBridge: KotlinBase, platform.IOSCrashlyticsBridge {
    static let shared = AppleCrashlyticsBridge()

    func reportNonFatal(throwable: ExportedKotlinPackages.kotlin.Throwable) {
        Crashlytics.crashlytics().record(error: throwable)
    }
}

private final class AppleFirebaseRemoteConfigBridge: KotlinBase, platform.IOSRemoteConfigBridge {
    static let shared = AppleFirebaseRemoteConfigBridge()

    func getString(key: String, completionHandler: @escaping (String?) -> Void) {
        guard FirebaseApp.app() != nil else {
            completionHandler(nil)
            return
        }

        let remoteConfig = RemoteConfig.remoteConfig()
        remoteConfig.fetchAndActivate { _, error in
            guard error == nil else {
                completionHandler(nil)
                return
            }

            let value = remoteConfig.configValue(forKey: key).stringValue.trimmingCharacters(in: .whitespacesAndNewlines)
            completionHandler(value.isEmpty ? nil : value)
        }
    }
}
