import Core
import MobileUi
import Foundation
import WidgetKit

/// Receives backend push payloads and forwards them to the shared graph:
/// `PushMessageHandler` publishes the message (trip auto-cancel) and refreshes
/// the station cache and widget data. Tapping the alert deep-links to the station.
enum ApplePushBridge {
    @MainActor
    static func handleRemoteNotification(_ userInfo: [AnyHashable: Any]) {
        let data = stringPayload(userInfo)
        guard data["payload"] != nil || data["stationId"] != nil else { return }
        Task {
            await BiziAppleGraph.shared.handlePushData(data: data)
        }
    }

    /// FCM delivers custom data as top-level string entries.
    static func stringPayload(_ userInfo: [AnyHashable: Any]) -> [String: String] {
        var result: [String: String] = [:]
        for (key, value) in userInfo {
            guard let key = key as? String, let string = value as? String else { continue }
            result[key] = string
        }
        return result
    }
}
