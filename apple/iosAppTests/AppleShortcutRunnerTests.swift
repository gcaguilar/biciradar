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

        XCTAssertEqual(dialog, "Abriendo tus favoritas. Tienes 2 en total: Plaza España, Plaza Aragón.")
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

        XCTAssertEqual(dialog, "Abriendo una ruta hacia Universidad.")
        let routeRequest = requests.last as? MobileLaunchRequestRouteToStation
        XCTAssertEqual(routeRequest?.stationId, "station-48")
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
    var stationById: [String: BiziStationSnapshot] = [:]
    var assistantResolution: AssistantResolution = AssistantResolution(
        spokenResponse: "La estación más cercana es Plaza España con 6 bicis y 8 huecos.",
        highlightedStationId: "station-1"
    )

    func favoriteStations() async throws -> [BiziStationSnapshot] {
        favorites
    }

    func station(matching query: String?) async throws -> BiziStationSnapshot? {
        matchedStation
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
