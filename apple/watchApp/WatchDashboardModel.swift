import BiziSharedCore
import Foundation

@MainActor
final class WatchDashboardModel: ObservableObject {
    private let graph: any WatchGraphClient
    @Published private(set) var nearbyStations: [WatchStationSnapshot] = []
    @Published private(set) var favoriteStations: [WatchStationSnapshot] = []
    @Published private(set) var isLoading = false
    @Published private(set) var errorMessage: String?

    init(graph: any WatchGraphClient = BiziWatchGraph.shared) {
        self.graph = graph
    }

    func refresh(favoriteIds: Set<String>) async {
        isLoading = true
        errorMessage = nil
        do {
            nearbyStations = try await graph.nearbyStations(limit: 5)
            favoriteStations = try await graph.favoriteStations(favoriteIds: favoriteIds)
        } catch {
            errorMessage = error.localizedDescription
            nearbyStations = []
            favoriteStations = []
        }
        isLoading = false
    }
}
