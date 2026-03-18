import BiziSharedCore
import XCTest

@MainActor
final class WatchDashboardModelTests: XCTestCase {
    func testRefreshPopulatesNearbyAndFavorites() async {
        let graph = DashboardGraphStub(
            nearby: [
                .fixture(id: "station-1", name: "Plaza España"),
                .fixture(id: "station-2", name: "Universidad")
            ],
            favorites: [
                .fixture(id: "station-2", name: "Universidad")
            ]
        )
        let model = WatchDashboardModel(graph: graph)

        await model.refresh(favoriteIds: ["station-2"])

        XCTAssertEqual(model.nearbyStations.map(\.id), ["station-1", "station-2"])
        XCTAssertEqual(model.favoriteStations.map(\.id), ["station-2"])
        XCTAssertFalse(model.isLoading)
        XCTAssertNil(model.errorMessage)
    }

    func testRefreshExposesErrors() async {
        let model = WatchDashboardModel(graph: DashboardGraphStub(error: DashboardGraphStub.StubError.offline))

        await model.refresh(favoriteIds: [])

        XCTAssertEqual(model.nearbyStations, [])
        XCTAssertEqual(model.favoriteStations, [])
        XCTAssertEqual(model.errorMessage, DashboardGraphStub.StubError.offline.localizedDescription)
        XCTAssertFalse(model.isLoading)
    }
}

private actor DashboardGraphStub: WatchGraphClient {
    enum StubError: LocalizedError {
        case offline

        var errorDescription: String? {
            switch self {
            case .offline:
                return "The watch is offline."
            }
        }
    }

    let nearby: [WatchStationSnapshot]
    let favorites: [WatchStationSnapshot]
    let error: Error?

    init(
        nearby: [WatchStationSnapshot] = [],
        favorites: [WatchStationSnapshot] = [],
        error: Error? = nil
    ) {
        self.nearby = nearby
        self.favorites = favorites
        self.error = error
    }

    func nearbyStations(limit: Int) async throws -> [WatchStationSnapshot] {
        if let error { throw error }
        return Array(nearby.prefix(limit))
    }

    func favoriteStations(favoriteIds: Set<String>) async throws -> [WatchStationSnapshot] {
        if let error { throw error }
        return favorites.filter { favoriteIds.contains($0.id) }
    }

    func station(matching query: String?) async throws -> WatchStationSnapshot? { nil }

    func station(stationId: String) async throws -> WatchStationSnapshot? { nil }

    func assistantResponse(for action: any AssistantAction) async throws -> AssistantResolution {
        fatalError("Not used by WatchDashboardModel tests")
    }

    func openRoute(to stationId: String) async throws -> WatchStationSnapshot? { nil }
}

private extension WatchStationSnapshot {
    static func fixture(
        id: String,
        name: String,
        address: String = "Centro",
        bikes: Int = 4,
        slots: Int = 6,
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
