@testable import BiciRadar
import XCTest

@available(iOS 16.1, *)
final class SurfaceMonitoringActivityControllerTests: XCTestCase {
    func testMonitoringContentStateIncludesAlternativeDetails() throws {
        let session = AppleSurfaceMonitoringSession(
            stationId: "station-1",
            stationName: "Plaza España",
            cityId: "zaragoza",
            kind: .bikes,
            status: .alternativeAvailable,
            bikesAvailable: 0,
            docksAvailable: 8,
            statusLevel: .empty,
            startedAtEpoch: 1_000,
            expiresAtEpoch: 2_000,
            lastUpdatedEpoch: 1_500,
            isActive: true,
            alternativeStationId: "station-2",
            alternativeStationName: "Puerta del Carmen",
            alternativeDistanceMeters: 320
        )

        let contentState = monitoringContentState(from: session)

        XCTAssertEqual(contentState.statusText, "Alternativa disponible")
        XCTAssertEqual(contentState.alternativeStationId, "station-2")
        XCTAssertEqual(contentState.alternativeName, "Puerta del Carmen")
        XCTAssertEqual(contentState.alternativeDistanceMeters, 320)
        XCTAssertEqual(contentState.alternativeSummaryText, "Alt: Puerta del Carmen · 320 m")
        XCTAssertEqual(contentState.alternativeStationURL?.absoluteString, "biciradar://station/station-2")
        XCTAssertEqual(contentState.expiresAtEpoch, 2_000)
    }

    func testMonitoringSessionDisplayTextMapsFinalStates() throws {
        let endedSession = AppleSurfaceMonitoringSession(
            stationId: "station-1",
            stationName: "Plaza España",
            cityId: "zaragoza",
            kind: .bikes,
            status: .ended,
            bikesAvailable: 0,
            docksAvailable: 8,
            statusLevel: .empty,
            startedAtEpoch: 1_000,
            expiresAtEpoch: 2_000,
            lastUpdatedEpoch: 1_500,
            isActive: false,
            alternativeStationId: nil,
            alternativeStationName: nil,
            alternativeDistanceMeters: nil
        )

        XCTAssertEqual(monitoringSessionDisplayText(endedSession), "Finalizada")
    }
}
