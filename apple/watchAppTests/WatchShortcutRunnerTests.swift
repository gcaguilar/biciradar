import BiziSharedCore
import XCTest

final class WatchShortcutRunnerTests: XCTestCase {
    func testFavoritesDialogSummarizesSyncedStations() async {
        let runner = WatchShortcutRunner(
            graph: FakeWatchGraph(
                favorites: [
                    .fixture(id: "station-1", name: "Plaza España"),
                    .fixture(id: "station-2", name: "Plaza Aragón"),
                ]
            ),
            favoriteIdsProvider: { ["station-1", "station-2"] },
            routeRequester: { _ in true }
        )

        let dialog = await runner.favoriteStationsDialog()

        XCTAssertEqual(dialog, "Tus favoritas en el reloj son Plaza España, Plaza Aragón.")
    }

    func testBikeCountDialogReportsMatchingStation() async {
        let runner = WatchShortcutRunner(
            graph: FakeWatchGraph(
                matchedStation: .fixture(id: "station-48", name: "Universidad", bikes: 7, slots: 5)
            ),
            favoriteIdsProvider: { [] },
            routeRequester: { _ in true }
        )

        let dialog = await runner.stationBikeCountDialog(stationName: "Universidad")

        XCTAssertEqual(dialog, "Universidad tiene 7 bicis disponibles.")
    }

    func testRouteDialogConfirmsPhoneHandoff() async {
        let runner = WatchShortcutRunner(
            graph: FakeWatchGraph(
                matchedStation: .fixture(id: "station-48", name: "Universidad")
            ),
            favoriteIdsProvider: { [] },
            routeRequester: { stationId in
                XCTAssertEqual(stationId, "station-48")
                return true
            }
        )

        let dialog = await runner.routeToStationDialog(stationName: "Universidad")

        XCTAssertEqual(dialog, "He pedido al iPhone que abra la ruta a Universidad.")
    }

    func testNearestWithSlotsUsesEmptyFallbackWhenNoStationIsHighlighted() async {
        let resolution = AssistantResolution(
            spokenResponse: "Esto no debería usarse.",
            highlightedStationId: nil
        )
        let runner = WatchShortcutRunner(
            graph: FakeWatchGraph(assistantResolution: resolution),
            favoriteIdsProvider: { [] },
            routeRequester: { _ in false }
        )

        let dialog = await runner.nearestStationWithSlotsDialog()

        XCTAssertEqual(dialog, "No he encontrado estaciones cercanas con huecos libres ahora mismo.")
    }

    func testSavedPlaceStatusDialogResolvesHomeAlias() async {
        let runner = WatchShortcutRunner(
            graph: FakeWatchGraph(
                queryMatches: [
                    "casa": .fixture(id: "station-home", name: "Plaza España", bikes: 8, slots: 4)
                ]
            ),
            favoriteIdsProvider: { [] },
            routeRequester: { _ in true }
        )

        let dialog = await runner.savedPlaceStatusDialog(savedPlace: "casa")

        XCTAssertEqual(dialog, "Plaza España tiene 8 bicis disponibles y 4 huecos libres.")
    }

    func testSavedPlaceRouteDialogUsesWorkAliasForHandoff() async {
        let runner = WatchShortcutRunner(
            graph: FakeWatchGraph(
                queryMatches: [
                    "trabajo": .fixture(id: "station-work", name: "Campus Río Ebro")
                ]
            ),
            favoriteIdsProvider: { [] },
            routeRequester: { stationId in
                XCTAssertEqual(stationId, "station-work")
                return true
            }
        )

        let dialog = await runner.savedPlaceRouteDialog(savedPlace: "trabajo")

        XCTAssertEqual(dialog, "He pedido al iPhone que abra la ruta a Campus Río Ebro.")
    }
}

private struct FakeWatchGraph: WatchGraphClient {
    var nearby: [WatchStationSnapshot] = []
    var favorites: [WatchStationSnapshot] = []
    var matchedStation: WatchStationSnapshot?
    var queryMatches: [String: WatchStationSnapshot] = [:]
    var assistantResolution: AssistantResolution = AssistantResolution(
        spokenResponse: "La estación más cercana es Plaza España con 6 bicis y 8 anclajes.",
        highlightedStationId: "station-1"
    )

    func nearbyStations(limit: Int) async throws -> [WatchStationSnapshot] {
        Array(nearby.prefix(limit))
    }

    func favoriteStations(favoriteIds: Set<String>) async throws -> [WatchStationSnapshot] {
        favorites.filter { favoriteIds.contains($0.id) }
    }

    func station(matching query: String?) async throws -> WatchStationSnapshot? {
        guard let query else { return matchedStation }
        return queryMatches[query] ?? matchedStation
    }

    func assistantResponse(for action: any AssistantAction) async throws -> AssistantResolution {
        assistantResolution
    }

    func openRoute(to stationId: String) async throws -> WatchStationSnapshot? {
        matchedStation
    }
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
