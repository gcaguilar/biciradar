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
        bridge = FavoritesSyncBridge(defaults: defaults, routeRequestStore: routeStore)
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
        bridge = FavoritesSyncBridge(defaults: defaults, routeRequestStore: routeStore)

        bridge.apply(context: [:])

        XCTAssertEqual(bridge.favoriteIds, Set(["101", "202"]))
        XCTAssertEqual(
            Set(defaults.stringArray(forKey: FavoritesSyncBridge.favoritesCacheKey) ?? []),
            Set(["101", "202"])
        )
    }
}
