import XCTest

@MainActor
final class WatchFavoritesSyncBridgeTests: XCTestCase {
    private var defaults: UserDefaults!
    private var bridge: WatchFavoritesSyncBridge!
    private var suiteName: String!

    override func setUp() {
        super.setUp()
        suiteName = "com.gcaguilar.biciradar.tests.watchsync.\(UUID().uuidString)"
        defaults = UserDefaults(suiteName: suiteName)
        defaults.removePersistentDomain(forName: suiteName)
        bridge = WatchFavoritesSyncBridge(defaults: defaults)
    }

    override func tearDown() {
        defaults.removePersistentDomain(forName: suiteName)
        bridge = nil
        defaults = nil
        suiteName = nil
        super.tearDown()
    }

    func testApplyStoresFavoriteIdsFromApplicationContext() {
        bridge.apply(context: [
            "favorite_ids": ["station-1", "station-2"],
        ])

        XCTAssertEqual(bridge.favoriteIds, Set(["station-1", "station-2"]))
        XCTAssertEqual(
            Set(defaults.stringArray(forKey: WatchFavoritesSyncBridge.favoritesCacheKey) ?? []),
            Set(["station-1", "station-2"])
        )
    }

    func testApplyWithoutFavoriteIdsPreservesCachedFavorites() {
        defaults.set(["station-7"], forKey: WatchFavoritesSyncBridge.favoritesCacheKey)
        bridge = WatchFavoritesSyncBridge(defaults: defaults)

        bridge.apply(context: [:])

        XCTAssertEqual(bridge.favoriteIds, Set(["station-7"]))
        XCTAssertEqual(
            Set(defaults.stringArray(forKey: WatchFavoritesSyncBridge.favoritesCacheKey) ?? []),
            Set(["station-7"])
        )
    }
}
