import Core
import KotlinRuntime
import MobileUi
import FirebaseCore
import FirebaseMessaging
import Foundation

/// APNs/FCM-backed `DeviceTokenProvider` for iOS. FCM is the single provider for
/// both platforms; it relays to APNs underneath.
final class ApplePushTokenProvider: KotlinBase, push.DeviceTokenProvider {
    var appId: String? { Bundle.main.bundleIdentifier }
    var locale: String? { Bundle.main.preferredLocalizations.first }
    var platform: String { "ios" }

    var pushEnvironment: String {
        #if DEBUG
        return "sandbox"
        #else
        return "production"
        #endif
    }

    func currentToken() async throws -> String? {
        guard FirebaseApp.app() != nil else { return nil }
        return await withCheckedContinuation { continuation in
            Messaging.messaging().token { token, _ in
                continuation.resume(returning: token)
            }
        }
    }
}
