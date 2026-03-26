import Foundation

#if canImport(ActivityKit)
import ActivityKit

@available(iOS 16.1, *)
struct BiziMonitoringActivityAttributes: ActivityAttributes {
    struct ContentState: Codable, Hashable {
        var bikesAvailable: Int
        var docksAvailable: Int
        var statusText: String
        var alternativeName: String?
        var isActive: Bool
        var expiresAtEpoch: Int64
    }

    var stationId: String
    var stationName: String
}
#endif
