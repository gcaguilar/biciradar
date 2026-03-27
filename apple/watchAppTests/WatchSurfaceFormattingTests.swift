import XCTest

final class WatchSurfaceFormattingTests: XCTestCase {
    func testWatchStationSnapshotDerivesStatusLevels() {
        XCTAssertEqual(WatchStationSnapshot.fixture(id: "good", bikes: 6, slots: 7).statusLevel, .good)
        XCTAssertEqual(WatchStationSnapshot.fixture(id: "low", bikes: 2, slots: 5).statusLevel, .low)
        XCTAssertEqual(WatchStationSnapshot.fixture(id: "empty", bikes: 0, slots: 5).statusLevel, .empty)
        XCTAssertEqual(WatchStationSnapshot.fixture(id: "full", bikes: 4, slots: 0).statusLevel, .full)
    }

    func testWatchStationSnapshotFormatsShortStatusText() {
        XCTAssertEqual(WatchStationSnapshot.fixture(id: "good", bikes: 6, slots: 7).statusText, "Disponible")
        XCTAssertEqual(WatchStationSnapshot.fixture(id: "low", bikes: 2, slots: 5).statusText, "Pocas")
        XCTAssertEqual(WatchStationSnapshot.fixture(id: "empty", bikes: 0, slots: 5).statusText, "Sin bicis")
        XCTAssertEqual(WatchStationSnapshot.fixture(id: "full", bikes: 4, slots: 0).statusText, "Sin huecos")
    }
}

private extension WatchStationSnapshot {
    static func fixture(
        id: String,
        name: String = "Plaza España",
        address: String = "Centro",
        bikes: Int,
        slots: Int,
        distance: Int = 120
    ) -> WatchStationSnapshot {
        WatchStationSnapshot(
            id: id,
            name: name,
            address: address,
            bikesAvailable: bikes,
            slotsFree: slots,
            distanceMeters: distance
        )
    }
}
