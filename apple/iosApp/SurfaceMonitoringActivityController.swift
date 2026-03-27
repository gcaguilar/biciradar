import Foundation

#if canImport(ActivityKit)
import ActivityKit

@MainActor
final class SurfaceMonitoringActivityController {
    static let shared = SurfaceMonitoringActivityController()

    private var refreshTask: Task<Void, Never>?

    func startRefreshing() {
        guard #available(iOS 16.1, *) else { return }
        stopRefreshing()
        refreshTask = Task {
            await syncFromSurfaceSnapshot()
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 15_000_000_000)
                await syncFromSurfaceSnapshot()
            }
        }
    }

    func stopRefreshing() {
        refreshTask?.cancel()
        refreshTask = nil
    }

    func syncNow() {
        guard #available(iOS 16.1, *) else { return }
        Task {
            await syncFromSurfaceSnapshot()
        }
    }

    @available(iOS 16.1, *)
    private func syncFromSurfaceSnapshot() async {
        let session = BiziSurfaceStore.activeMonitoringSession()
        let activities = Activity<BiziMonitoringActivityAttributes>.activities
        FavoritesSyncBridge.shared.syncMonitoringFromSurfaceSnapshot()

        guard let session else {
            for activity in activities {
                await activity.end(using: activity.contentState, dismissalPolicy: .immediate)
            }
            return
        }

        let contentState = monitoringContentState(from: session)

        if let activity = activities.first(where: { $0.attributes.stationId == session.stationId }) {
            await activity.update(using: contentState)
            for otherActivity in activities where otherActivity.id != activity.id {
                await otherActivity.end(using: otherActivity.contentState, dismissalPolicy: .immediate)
            }
            return
        }

        let attributes = BiziMonitoringActivityAttributes(
            stationId: session.stationId,
            stationName: session.stationName
        )
        _ = try? Activity.request(
            attributes: attributes,
            contentState: contentState,
            pushType: nil
        )
    }

}

@available(iOS 16.1, *)
internal func monitoringContentState(
    from session: AppleSurfaceMonitoringSession
) -> BiziMonitoringActivityAttributes.ContentState {
    BiziMonitoringActivityAttributes.ContentState(
        bikesAvailable: session.bikesAvailable,
        docksAvailable: session.docksAvailable,
        statusText: monitoringSessionDisplayText(session),
        alternativeStationId: session.alternativeStationId,
        alternativeName: session.alternativeStationName,
        alternativeDistanceMeters: session.alternativeDistanceMeters,
        isActive: session.isActive,
        expiresAtEpoch: session.expiresAtEpoch
    )
}

@available(iOS 16.1, *)
internal func monitoringSessionDisplayText(_ session: AppleSurfaceMonitoringSession) -> String {
    switch session.status {
    case .monitoring:
        return "Monitorizando"
    case .changedToEmpty:
        return "Sin bicis"
    case .changedToFull:
        return "Sin huecos"
    case .alternativeAvailable:
        return "Alternativa disponible"
    case .ended:
        return "Finalizada"
    case .expired:
        return "Expirada"
    }
}

#else

@MainActor
final class SurfaceMonitoringActivityController {
    static let shared = SurfaceMonitoringActivityController()

    func startRefreshing() {}
    func stopRefreshing() {}
    func syncNow() {}
}

#endif
