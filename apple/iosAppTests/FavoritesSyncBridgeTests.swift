@testable import BiciRadar
import BiziMobileUi
import XCTest

@MainActor
final class FavoritesSyncBridgeTests: XCTestCase {
    private var defaults: UserDefaults!
    private var routeStore: AppleLaunchRequestStore!
    private var bridge: FavoritesSyncBridge!
    private var suiteName: String!

    override func setUp() {
        super.setUp()
        suiteName = "com.gcaguilar.biciradar.tests.sync.\(UUID().uuidString)"
        defaults = UserDefaults(suiteName: suiteName)
        defaults.removePersistentDomain(forName: suiteName)
        routeStore = AppleLaunchRequestStore(defaults: defaults)
        bridge = FavoritesSyncBridge(
            defaults: defaults,
            routeRequestStore: routeStore,
            onSurfaceStateChanged: { _ in }
        )
    }

    override func tearDown() {
        defaults.removePersistentDomain(forName: suiteName)
        bridge = nil
        routeStore = nil
        defaults = nil
        suiteName = nil
        super.tearDown()
    }

    func testApplyCachesFavoriteIdsAndRouteRequest() {
        bridge.apply(context: [
            "favorite_ids": ["101", "202"],
            "route_station_id": "303",
            "route_requested_at": 100.0,
        ])

        XCTAssertEqual(bridge.favoriteIds, Set(["101", "202"]))
        XCTAssertEqual(
            Set(defaults.stringArray(forKey: FavoritesSyncBridge.favoritesCacheKey) ?? []),
            Set(["101", "202"])
        )
        let request = routeStore.takePendingRequest() as? MobileLaunchRequestRouteToStation
        XCTAssertEqual(request?.stationId, "303")
    }

    func testApplyIgnoresOlderRouteRequests() {
        bridge.apply(context: [
            "route_station_id": "303",
            "route_requested_at": 100.0,
        ])
        _ = routeStore.takePendingRequest()

        bridge.apply(context: [
            "route_station_id": "404",
            "route_requested_at": 99.0,
        ])

        XCTAssertNil(routeStore.takePendingRequest())
    }

    func testApplyIgnoresEqualRouteRequests() {
        bridge.apply(context: [
            "route_station_id": "303",
            "route_requested_at": 100.0,
        ])
        _ = routeStore.takePendingRequest()

        bridge.apply(context: [
            "route_station_id": "404",
            "route_requested_at": 100.0,
        ])

        XCTAssertNil(routeStore.takePendingRequest())
    }

    func testApplyIgnoresRouteRequestsWithoutTimestamp() {
        bridge.apply(context: [
            "route_station_id": "303",
        ])

        XCTAssertNil(routeStore.takePendingRequest())
    }

    func testApplyIgnoresEmptyRouteStationIdentifier() {
        bridge.apply(context: [
            "route_station_id": "",
            "route_requested_at": 100.0,
        ])

        XCTAssertNil(routeStore.takePendingRequest())
    }

    func testApplyWithoutFavoriteIdsPreservesCachedFavorites() {
        defaults.set(["101", "202"], forKey: FavoritesSyncBridge.favoritesCacheKey)
        bridge = FavoritesSyncBridge(
            defaults: defaults,
            routeRequestStore: routeStore,
            onSurfaceStateChanged: { _ in }
        )

        bridge.apply(context: [:])

        XCTAssertEqual(bridge.favoriteIds, Set(["101", "202"]))
        XCTAssertEqual(
            Set(defaults.stringArray(forKey: FavoritesSyncBridge.favoritesCacheKey) ?? []),
            Set(["101", "202"])
        )
    }

    func testSyncWatchContextFromAppGroupLoadsFavoritesSnapshotAndSavedPlaces() throws {
        let tempDirectory = FileManager.default.temporaryDirectory
            .appendingPathComponent("favorites-sync-\(UUID().uuidString)", isDirectory: true)
        try FileManager.default.createDirectory(at: tempDirectory, withIntermediateDirectories: true)
        defer { try? FileManager.default.removeItem(at: tempDirectory) }

        let snapshotURL = tempDirectory.appendingPathComponent("favorites.json")
        let snapshotData = """
        {"favoriteIds":["101","202"],"homeStationId":"home-1","workStationId":"work-2"}
        """.data(using: .utf8)!
        try snapshotData.write(to: snapshotURL, options: .atomic)

        bridge.syncWatchContextFromAppGroup(favoritesURL: snapshotURL)

        XCTAssertEqual(bridge.favoriteIds, Set(["101", "202"]))
        XCTAssertEqual(bridge.homeStationId, "home-1")
        XCTAssertEqual(bridge.workStationId, "work-2")
        XCTAssertEqual(
            Set(defaults.stringArray(forKey: FavoritesSyncBridge.favoritesCacheKey) ?? []),
            Set(["101", "202"])
        )
        XCTAssertEqual(defaults.string(forKey: FavoritesSyncBridge.homeStationCacheKey), "home-1")
        XCTAssertEqual(defaults.string(forKey: FavoritesSyncBridge.workStationCacheKey), "work-2")
    }

    func testApplyClearsSavedPlacesWhenApplicationContextSendsEmptyStrings() {
        defaults.set(["101", "202"], forKey: FavoritesSyncBridge.favoritesCacheKey)
        defaults.set("home-1", forKey: FavoritesSyncBridge.homeStationCacheKey)
        defaults.set("work-2", forKey: FavoritesSyncBridge.workStationCacheKey)
        bridge = FavoritesSyncBridge(
            defaults: defaults,
            routeRequestStore: routeStore,
            onSurfaceStateChanged: { _ in }
        )

        bridge.apply(context: [
            "favorite_ids": ["101", "202"],
            "home_station_id": "",
            "work_station_id": "",
        ])

        XCTAssertEqual(bridge.favoriteIds, Set(["101", "202"]))
        XCTAssertNil(bridge.homeStationId)
        XCTAssertNil(bridge.workStationId)
        XCTAssertEqual(
            Set(defaults.stringArray(forKey: FavoritesSyncBridge.favoritesCacheKey) ?? []),
            Set(["101", "202"])
        )
        XCTAssertNil(defaults.string(forKey: FavoritesSyncBridge.homeStationCacheKey))
        XCTAssertNil(defaults.string(forKey: FavoritesSyncBridge.workStationCacheKey))
    }
}
