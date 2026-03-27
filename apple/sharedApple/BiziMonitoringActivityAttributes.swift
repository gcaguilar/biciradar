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
                return "Alt: \(alternativeName) · \(alternativeDistanceMeters) m"
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
#endif
