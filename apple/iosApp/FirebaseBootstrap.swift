import Foundation
import FirebaseCore
import FirebaseCrashlytics

enum FirebaseBootstrap {
    static func configureIfAvailable() {
        guard Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil else { return }
        guard FirebaseApp.app() == nil else { return }

        FirebaseApp.configure()
        Crashlytics.crashlytics().setCustomValue("ios", forKey: "platform")
        Crashlytics.crashlytics().setCustomValue(Bundle.main.bundleIdentifier ?? "unknown", forKey: "bundle_id")
        Crashlytics.crashlytics().setCustomValue(Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "unknown", forKey: "app_version")
    }
}
