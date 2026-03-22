import BiziMobileUi
import XCTest

final class AppleShortcutRunnerTests: XCTestCase {
    func testFavoriteStationsDialogSummarizesFavoritesAndStoresLaunchRequest() async {
        let recorder = LaunchRequestRecorder()
        let runner = AppleShortcutRunner(
            graph: FakeAppleGraph(
                favorites: [
                    .fixture(id: "station-1", name: "Plaza España"),
                    .fixture(id: "station-2", name: "Plaza Aragón"),
                ]
            ),
            saveLaunchRequest: { request in
                await recorder.append(request)
            }
        )

        let dialog = await runner.favoriteStationsDialog()
        let requests = await recorder.requests()

        XCTAssertEqual(dialog, "Tus favoritas en Bici Radar. Tienes 2 en total: Plaza España, Plaza Aragón.")
        XCTAssertEqual(requests.count, 1)
        XCTAssertTrue(requests.first is MobileLaunchRequestFavorites)
    }

    func testFavoriteStationsDialogOnlyListsFirstThreeStations() async {
        let runner = AppleShortcutRunner(
            graph: FakeAppleGraph(
                favorites: [
                    .fixture(id: "station-1", name: "Plaza España"),
                    .fixture(id: "station-2", name: "Plaza Aragón"),
                    .fixture(id: "station-3", name: "Universidad"),
                    .fixture(id: "station-4", name: "Campus Río Ebro"),
                ]
            ),
            saveLaunchRequest: { _ in }
        )

        let dialog = await runner.favoriteStationsDialog()

        XCTAssertEqual(dialog, "Tus favoritas en Bici Radar. Tienes 4 en total: Plaza España, Plaza Aragón, Universidad.")
    }

    func testFavoriteStationsDialogHandlesEmptyFavorites() async {
        let recorder = LaunchRequestRecorder()
        let runner = AppleShortcutRunner(
            graph: FakeAppleGraph(),
            saveLaunchRequest: { request in
                await recorder.append(request)
            }
        )

        let dialog = await runner.favoriteStationsDialog()
        let requests = await recorder.requests()

        XCTAssertEqual(dialog, "Abre Bici Radar. Todavía no tienes estaciones favoritas guardadas.")
        XCTAssertEqual(requests.count, 1)
        XCTAssertTrue(requests.first is MobileLaunchRequestFavorites)
    }

    func testNearestStationDialogPromotesHighlightedStationWhenAvailable() async {
        let recorder = LaunchRequestRecorder()
        let runner = AppleShortcutRunner(
            graph: FakeAppleGraph(
                stationById: [
                    "station-48": .fixture(id: "station-48", name: "Universidad")
                ],
                assistantResolution: AssistantResolution(
                    spokenResponse: "La estación más cercana es Universidad con 6 bicis y 8 huecos.",
                    highlightedStationId: "station-48"
                )
            ),
            saveLaunchRequest: { request in
                await recorder.append(request)
            }
        )

        let dialog = await runner.nearestStationDialog()
        let requests = await recorder.requests()

        XCTAssertEqual(dialog, "La estación más cercana es Universidad con 6 bicis y 8 huecos.")
        XCTAssertEqual(requests.count, 2)
        XCTAssertTrue(requests.first is MobileLaunchRequestNearestStation)
        let detailRequest = requests.last as? MobileLaunchRequestShowStation
        XCTAssertEqual(detailRequest?.stationId, "station-48")
    }

    func testNearestStationWithBikesDialogUsesFallbackWhenAssistantCannotHighlightStation() async {
        let recorder = LaunchRequestRecorder()
        let runner = AppleShortcutRunner(
            graph: FakeAppleGraph(
                assistantResolution: AssistantResolution(
                    spokenResponse: "Esto no debería usarse.",
                    highlightedStationId: nil
                )
            ),
            saveLaunchRequest: { request in
                await recorder.append(request)
            }
        )

        let dialog = await runner.nearestStationWithBikesDialog()
        let requests = await recorder.requests()

        XCTAssertEqual(dialog, "Abre Bici Radar para buscar una estación cercana con bicis disponibles.")
        XCTAssertEqual(requests.count, 1)
        XCTAssertTrue(requests.first is MobileLaunchRequestNearestStationWithBikes)
    }

    func testNearestStationWithSlotsDialogReturnsSpokenResponseWhenHighlightedStationCannotLoad() async {
        let recorder = LaunchRequestRecorder()
        let runner = AppleShortcutRunner(
            graph: FakeAppleGraph(
                assistantResolution: AssistantResolution(
                    spokenResponse: "La estación más cercana tiene huecos libres.",
                    highlightedStationId: "station-missing"
                )
            ),
            saveLaunchRequest: { request in
                await recorder.append(request)
            }
        )

        let dialog = await runner.nearestStationWithSlotsDialog()
        let requests = await recorder.requests()

        XCTAssertEqual(dialog, "La estación más cercana tiene huecos libres.")
        XCTAssertEqual(requests.count, 1)
        XCTAssertTrue(requests.first is MobileLaunchRequestNearestStationWithSlots)
    }

    func testStationBikeCountDialogReportsMatchingStation() async {
        let runner = AppleShortcutRunner(
            graph: FakeAppleGraph(
                matchedStation: .fixture(id: "station-7", name: "Universidad", bikes: 7, slots: 5)
            ),
            saveLaunchRequest: { _ in }
        )

        let dialog = await runner.stationBikeCountDialog(stationName: "Universidad")

        XCTAssertEqual(dialog, "Universidad tiene 7 bicis disponibles.")
    }

    func testStationStatusDialogReportsStationByIdentifier() async {
        let runner = AppleShortcutRunner(
            graph: FakeAppleGraph(
                stationById: [
                    "station-7": .fixture(id: "station-7", name: "Universidad", bikes: 7, slots: 5)
                ]
            ),
            saveLaunchRequest: { _ in }
        )

        let dialog = await runner.stationStatusDialog(stationId: "station-7")

        XCTAssertEqual(dialog, "Universidad tiene 7 bicis disponibles y 5 huecos libres.")
    }

    func testStationSlotCountDialogReportsStationByIdentifier() async {
        let runner = AppleShortcutRunner(
            graph: FakeAppleGraph(
                stationById: [
                    "station-7": .fixture(id: "station-7", name: "Universidad", bikes: 7, slots: 5)
                ]
            ),
            saveLaunchRequest: { _ in }
        )

        let dialog = await runner.stationSlotCountDialog(stationId: "station-7")

        XCTAssertEqual(dialog, "Universidad tiene 5 huecos libres.")
    }

    func testRouteDialogStoresRouteLaunchRequest() async {
        let recorder = LaunchRequestRecorder()
        let runner = AppleShortcutRunner(
            graph: FakeAppleGraph(
                matchedStation: .fixture(id: "station-48", name: "Universidad")
            ),
            saveLaunchRequest: { request in
                await recorder.append(request)
            }
        )

        let dialog = await runner.routeToStationDialog(stationName: "Universidad")
        let requests = await recorder.requests()

        XCTAssertEqual(dialog, "Preparando ruta hacia Universidad en Bici Radar.")
        let routeRequest = requests.last as? MobileLaunchRequestRouteToStation
        XCTAssertEqual(routeRequest?.stationId, "station-48")
    }

    func testRouteDialogByIdentifierStoresRouteLaunchRequest() async {
        let recorder = LaunchRequestRecorder()
        let runner = AppleShortcutRunner(
            graph: FakeAppleGraph(
                stationById: [
                    "station-48": .fixture(id: "station-48", name: "Universidad")
                ]
            ),
            saveLaunchRequest: { request in
                await recorder.append(request)
            }
        )

        let dialog = await runner.routeToStationDialog(stationId: "station-48")
        let requests = await recorder.requests()

        XCTAssertEqual(dialog, "Preparando ruta hacia Universidad en Bici Radar.")
        let routeRequest = requests.last as? MobileLaunchRequestRouteToStation
        XCTAssertEqual(routeRequest?.stationId, "station-48")
    }

    func testSavedPlaceStatusDialogResolvesHomeAlias() async {
        let runner = AppleShortcutRunner(
            graph: FakeAppleGraph(
                queryMatches: [
                    "casa": .fixture(id: "station-home", name: "Plaza España", bikes: 8, slots: 4)
                ]
            ),
            saveLaunchRequest: { _ in }
        )

        let dialog = await runner.savedPlaceStatusDialog(savedPlace: "casa")

        XCTAssertEqual(dialog, "Plaza España tiene 8 bicis disponibles y 4 huecos libres.")
    }

    func testSavedPlaceRouteDialogStoresWorkRouteLaunchRequest() async {
        let recorder = LaunchRequestRecorder()
        let runner = AppleShortcutRunner(
            graph: FakeAppleGraph(
                queryMatches: [
                    "trabajo": .fixture(id: "station-work", name: "Campus Río Ebro")
                ]
            ),
            saveLaunchRequest: { request in
                await recorder.append(request)
            }
        )

        let dialog = await runner.savedPlaceRouteDialog(savedPlace: "trabajo")
        let requests = await recorder.requests()

        XCTAssertEqual(dialog, "Preparando ruta hacia Campus Río Ebro en Bici Radar.")
        let routeRequest = requests.last as? MobileLaunchRequestRouteToStation
        XCTAssertEqual(routeRequest?.stationId, "station-work")
    }

    func testSavedPlaceBikeCountDialogResolvesHomeAlias() async {
        let runner = AppleShortcutRunner(
            graph: FakeAppleGraph(
                queryMatches: [
                    "casa": .fixture(id: "station-home", name: "Plaza España", bikes: 8, slots: 4)
                ]
            ),
            saveLaunchRequest: { _ in }
        )

        let dialog = await runner.savedPlaceBikeCountDialog(savedPlace: "casa")

        XCTAssertEqual(dialog, "Plaza España tiene 8 bicis disponibles.")
    }

    func testSavedPlaceSlotCountDialogResolvesWorkAlias() async {
        let runner = AppleShortcutRunner(
            graph: FakeAppleGraph(
                queryMatches: [
                    "trabajo": .fixture(id: "station-work", name: "Campus Río Ebro", bikes: 5, slots: 9)
                ]
            ),
            saveLaunchRequest: { _ in }
        )

        let dialog = await runner.savedPlaceSlotCountDialog(savedPlace: "trabajo")

        XCTAssertEqual(dialog, "Campus Río Ebro tiene 9 huecos libres.")
    }
}

private actor LaunchRequestRecorder {
    private var storedRequests: [MobileLaunchRequest] = []

    func append(_ request: MobileLaunchRequest) {
        storedRequests.append(request)
    }

    func requests() -> [MobileLaunchRequest] {
        storedRequests
    }
}

private struct FakeAppleGraph: AppleGraphClient {
    var favorites: [BiziStationSnapshot] = []
    var matchedStation: BiziStationSnapshot?
    var queryMatches: [String: BiziStationSnapshot] = [:]
    var stationById: [String: BiziStationSnapshot] = [:]
    var assistantResolution: AssistantResolution = AssistantResolution(
        spokenResponse: "La estación más cercana es Plaza España con 6 bicis y 8 huecos.",
        highlightedStationId: "station-1"
    )

    func favoriteStations() async throws -> [BiziStationSnapshot] {
        favorites
    }

    func station(matching query: String?) async throws -> BiziStationSnapshot? {
        guard let query else { return matchedStation }
        return queryMatches[query] ?? matchedStation
    }

    func station(stationId: String) async throws -> BiziStationSnapshot? {
        stationById[stationId]
    }

    func assistantResponse(for action: any AssistantAction) async throws -> AssistantResolution {
        assistantResolution
    }
}

private extension BiziStationSnapshot {
    static func fixture(
        id: String,
        name: String,
        address: String = "Centro",
        bikes: Int = 4,
        slots: Int = 6,
        distance: Int = 120
    ) -> BiziStationSnapshot {
        BiziStationSnapshot(
            id: id,
            name: name,
            address: address,
            bikesAvailable: bikes,
            slotsFree: slots,
            distanceMeters: distance
        )
    }
}
