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
        let store = WatchSurfaceSnapshotStoreStub()
        let model = WatchDashboardModel(graph: graph, surfaceStore: store)

        await model.refresh(favoriteIds: ["station-2"])

        XCTAssertEqual(model.nearbyStations.map(\.id), ["station-1", "station-2"])
        XCTAssertEqual(model.favoriteStations.map(\.id), ["station-2"])
        XCTAssertFalse(model.isLoading)
        XCTAssertNil(model.errorMessage)
        XCTAssertEqual(store.writtenSnapshots.last?.nearbyStations.map(\.id), ["station-1", "station-2"])
        XCTAssertEqual(store.writtenSnapshots.last?.favoriteStations.map(\.id), ["station-2"])
    }

    func testRefreshPrioritizesHomeAndWorkFavorites() async {
        let graph = DashboardGraphStub(
            favorites: [
                .fixture(id: "station-3", name: "Campus Río Ebro", distance: 350),
                .fixture(id: "station-2", name: "Universidad", distance: 120),
                .fixture(id: "station-1", name: "Plaza España", distance: 250)
            ]
        )
        let model = WatchDashboardModel(graph: graph, surfaceStore: WatchSurfaceSnapshotStoreStub())

        await model.refresh(
            favoriteIds: ["station-1", "station-2", "station-3"],
            homeStationId: "station-1",
            workStationId: "station-3"
        )

        XCTAssertEqual(model.favoriteStations.map(\.id), ["station-1", "station-3", "station-2"])
    }

    func testRefreshExposesErrors() async {
        let model = WatchDashboardModel(
            graph: DashboardGraphStub(error: DashboardGraphStub.StubError.offline),
            surfaceStore: WatchSurfaceSnapshotStoreStub()
        )

        await model.refresh(favoriteIds: [])

        XCTAssertEqual(model.nearbyStations, [])
        XCTAssertEqual(model.favoriteStations, [])
        XCTAssertEqual(model.errorMessage, DashboardGraphStub.StubError.offline.localizedDescription)
        XCTAssertFalse(model.isLoading)
    }

    func testInitLoadsCachedSnapshot() {
        let store = WatchSurfaceSnapshotStoreStub(
            snapshot: WatchSurfaceSnapshotBundle(
                generatedAtEpoch: 1234,
                nearbyStations: [.fixture(id: "station-1", name: "Plaza España")],
                favoriteStations: [.fixture(id: "station-2", name: "Universidad")]
            )
        )

        let model = WatchDashboardModel(graph: DashboardGraphStub(), surfaceStore: store)

        XCTAssertEqual(model.nearbyStations.map(\.id), ["station-1"])
        XCTAssertEqual(model.favoriteStations.map(\.id), ["station-2"])
    }

    func testRefreshFallsBackToCachedSnapshotOnError() async {
        let store = WatchSurfaceSnapshotStoreStub(
            snapshot: WatchSurfaceSnapshotBundle(
                generatedAtEpoch: 1234,
                nearbyStations: [.fixture(id: "station-1", name: "Plaza España")],
                favoriteStations: [
                    .fixture(id: "station-2", name: "Universidad", distance: 220),
                    .fixture(id: "station-3", name: "Campus Río Ebro", distance: 140)
                ]
            )
        )
        let model = WatchDashboardModel(
            graph: DashboardGraphStub(error: DashboardGraphStub.StubError.offline),
            surfaceStore: store
        )

        await model.refresh(
            favoriteIds: ["station-2", "station-3"],
            homeStationId: "station-3"
        )

        XCTAssertEqual(model.nearbyStations.map(\.id), ["station-1"])
        XCTAssertEqual(model.favoriteStations.map(\.id), ["station-3", "station-2"])
        XCTAssertEqual(model.errorMessage, DashboardGraphStub.StubError.offline.localizedDescription)
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

private final class WatchSurfaceSnapshotStoreStub: WatchSurfaceSnapshotStoring {
    private var snapshot: WatchSurfaceSnapshotBundle?
    private(set) var writtenSnapshots: [WatchSurfaceSnapshotBundle] = []

    init(snapshot: WatchSurfaceSnapshotBundle? = nil) {
        self.snapshot = snapshot
    }

    func read() -> WatchSurfaceSnapshotBundle? {
        snapshot
    }

    func write(_ snapshot: WatchSurfaceSnapshotBundle) {
        self.snapshot = snapshot
        writtenSnapshots.append(snapshot)
    }
}
