import BiziSharedCore
import Foundation
import SwiftUI
import WidgetKit

@MainActor
final class WatchDashboardModel: ObservableObject {
    private let graph: any WatchGraphClient
    private let surfaceStore: any WatchSurfaceSnapshotStoring
    @Published private(set) var nearbyStations: [WatchStationSnapshot]
    @Published private(set) var favoriteStations: [WatchStationSnapshot]
    @Published private(set) var isLoading = false
    @Published private(set) var errorMessage: String?

    init(
        graph: any WatchGraphClient = BiziWatchGraph.shared,
        surfaceStore: any WatchSurfaceSnapshotStoring = WatchSurfaceSnapshotStore.shared
    ) {
        self.graph = graph
        self.surfaceStore = surfaceStore
        let cachedSnapshot = surfaceStore.read()
        _nearbyStations = Published(initialValue: cachedSnapshot?.nearbyStations ?? [])
        _favoriteStations = Published(initialValue: cachedSnapshot?.favoriteStations ?? [])
    }

    func refresh(
        favoriteIds: Set<String>,
        homeStationId: String? = nil,
        workStationId: String? = nil,
        forceRefresh: Bool = false
    ) async {
        withAnimation(.easeOut(duration: 0.18)) {
            isLoading = true
            errorMessage = nil
        }
        do {
            if forceRefresh, let watchGraph = graph as? BiziWatchGraph {
                try await watchGraph.refreshData(forceRefresh: true)
            }
            let nearbyStations = try await graph.nearbyStations(limit: 5)
            let favoriteStations = orderedFavoriteStations(
                try await graph.favoriteStations(favoriteIds: favoriteIds),
                homeStationId: homeStationId,
                workStationId: workStationId
            )
            surfaceStore.write(
                WatchSurfaceSnapshotBundle(
                    generatedAtEpoch: Int64(Date().timeIntervalSince1970 * 1000),
                    nearbyStations: nearbyStations,
                    favoriteStations: favoriteStations
                )
            )
            withAnimation(.spring(response: 0.36, dampingFraction: 0.82)) {
                self.nearbyStations = nearbyStations
                self.favoriteStations = favoriteStations
            }
            WidgetCenter.shared.reloadAllTimelines()
        } catch {
            let cachedSnapshot = surfaceStore.read()
            withAnimation(.easeOut(duration: 0.18)) {
                errorMessage = friendlyMessage(for: error)
                nearbyStations = cachedSnapshot?.nearbyStations ?? []
                favoriteStations = orderedFavoriteStations(
                    cachedSnapshot?.favoriteStations.filter { favoriteIds.contains($0.id) } ?? [],
                    homeStationId: homeStationId,
                    workStationId: workStationId
                )
            }
        }
        withAnimation(.easeOut(duration: 0.18)) {
            isLoading = false
        }
    }

    private func orderedFavoriteStations(
        _ favorites: [WatchStationSnapshot],
        homeStationId: String?,
        workStationId: String?
    ) -> [WatchStationSnapshot] {
        favorites.sorted { lhs, rhs in
            let lhsPriority = lhs.favoritePriority(homeStationId: homeStationId, workStationId: workStationId)
            let rhsPriority = rhs.favoritePriority(homeStationId: homeStationId, workStationId: workStationId)
            if lhsPriority != rhsPriority {
                return lhsPriority > rhsPriority
            }
            if lhs.distanceMeters != rhs.distanceMeters {
                return lhs.distanceMeters < rhs.distanceMeters
            }
            return lhs.name.localizedCaseInsensitiveCompare(rhs.name) == .orderedAscending
        }
    }

    private func friendlyMessage(for error: Error) -> String {
        error.localizedDescription
    }
}

private extension WatchStationSnapshot {
    func favoritePriority(homeStationId: String?, workStationId: String?) -> Int {
        if id == homeStationId { return 400 }
        if id == workStationId { return 380 }
        return 100
    }
}
