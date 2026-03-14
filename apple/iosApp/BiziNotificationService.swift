import Foundation
import UserNotifications

/// Manages local notification permission and delivery for Bizi Zaragoza.
@MainActor
final class BiziNotificationService {
    static let shared = BiziNotificationService()

    private let center: UNUserNotificationCenter

    init(center: UNUserNotificationCenter = .current()) {
        self.center = center
    }

    // MARK: - Permission

    /// Requests notification authorization. Safe to call multiple times (no-op if already determined).
    func requestAuthorization() async {
        let settings = await center.notificationSettings()
        guard settings.authorizationStatus == .notDetermined else { return }
        _ = try? await center.requestAuthorization(options: [.alert, .sound, .badge])
    }

    // MARK: - Posting

    /// Fires a local notification for each favorite station that newly has bikes available.
    /// - Parameter newlyAvailable: stations that transitioned from 0 → >0 bikes since last check.
    func notifyFavoriteStationsAvailable(_ newlyAvailable: [BiziStationSnapshot]) {
        guard !newlyAvailable.isEmpty else { return }

        for station in newlyAvailable {
            let content = UNMutableNotificationContent()
            content.title = station.name
            content.body = availabilityBody(bikes: station.bikesAvailable, slots: station.slotsFree)
            content.sound = .default
            content.userInfo = ["station_id": station.id]

            let request = UNNotificationRequest(
                identifier: "bizi.station.\(station.id)",
                content: content,
                trigger: nil  // deliver immediately
            )
            center.add(request) { error in
                if let error {
                    print("[BiziNotifications] Failed to schedule notification for \(station.id): \(error)")
                }
            }
        }
    }

    // MARK: - Private helpers

    private func availabilityBody(bikes: Int, slots: Int) -> String {
        switch (bikes > 0, slots > 0) {
        case (true, true):
            return "\(bikes) bici\(bikes == 1 ? "" : "s") disponible\(bikes == 1 ? "" : "s") · \(slots) hueco\(slots == 1 ? "" : "s") libre\(slots == 1 ? "" : "s")"
        case (true, false):
            return "\(bikes) bici\(bikes == 1 ? "" : "s") disponible\(bikes == 1 ? "" : "s")"
        case (false, true):
            return "\(slots) hueco\(slots == 1 ? "" : "s") libre\(slots == 1 ? "" : "s")"
        case (false, false):
            return "Estación disponible"
        }
    }
}
