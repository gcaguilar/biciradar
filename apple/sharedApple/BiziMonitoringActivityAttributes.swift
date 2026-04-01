import Foundation

#if canImport(ActivityKit)
import ActivityKit

@available(iOS 16.1, *)
struct BiziMonitoringActivityAttributes: ActivityAttributes {
    struct ContentState: Codable, Hashable {
        var bikesAvailable: Int
        var docksAvailable: Int
        var statusText: String
        var alternativeStationId: String?
        var alternativeName: String?
        var alternativeDistanceMeters: Int?
        var isActive: Bool
        var expiresAtEpoch: Int64

        var alternativeSummaryText: String? {
            guard let alternativeName else { return nil }
            if let alternativeDistanceMeters {
                return "Alt: \(alternativeName) · \(activityDistanceText(alternativeDistanceMeters))"
            }
            return "Alt: \(alternativeName)"
        }

        var alternativeStationURL: URL? {
            alternativeStationId.flatMap { URL(string: "biciradar://station/\($0)") }
        }
    }

    var stationId: String
    var stationName: String
}

private func activityDistanceText(_ meters: Int) -> String {
    if meters >= 1000 {
        let km = Double(meters) / 1000.0
        let rounded = (km * 10).rounded(.towardZero) / 10
        if rounded.truncatingRemainder(dividingBy: 1) == 0 {
            return "\(Int(rounded)) km"
        }
        return "\(rounded) km"
    }
    return "\(meters) m"
}
#endif
