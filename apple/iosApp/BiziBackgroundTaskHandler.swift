import BackgroundTasks
import Foundation

#if !os(macOS)

/// Encapsulates all BGTaskScheduler registration, scheduling, and execution logic.
///
/// Usage:
/// 1. Call `registerTasks()` once at app launch (before the first `WindowGroup` scene appears).
/// 2. Call `scheduleAppRefresh()` when the app enters the background.
/// 3. The system calls `handleAppRefresh(_:)` when it decides to run the task.
enum BiziBackgroundTaskHandler {
    static let appRefreshTaskIdentifier = "com.gcaguilar.bizizaragoza.ios.refresh"

    // MARK: - Registration (call once at launch)

    /// Registers the background task handler with the system.
    /// **Must** be called before the application finishes launching.
    static func registerTasks() {
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: appRefreshTaskIdentifier,
            using: nil
        ) { task in
            guard let refreshTask = task as? BGAppRefreshTask else {
                task.setTaskCompleted(success: false)
                return
            }
            handleAppRefresh(refreshTask)
        }
    }

    // MARK: - Scheduling (call when entering background)

    /// Schedules the next background app refresh. Safe to call multiple times.
    static func scheduleAppRefresh() {
        let request = BGAppRefreshTaskRequest(identifier: appRefreshTaskIdentifier)
        // Earliest begin: 15 minutes from now. The system may delay further.
        request.earliestBeginDate = Date(timeIntervalSinceNow: 15 * 60)
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch BGTaskScheduler.Error.notPermitted {
            // Background refresh disabled by user in Settings — silent fail.
            print("[BiziBGTask] Not permitted: user disabled background app refresh.")
        } catch BGTaskScheduler.Error.tooManyPendingTaskRequests {
            // Already scheduled — no action needed.
            print("[BiziBGTask] Task already scheduled.")
        } catch {
            print("[BiziBGTask] Failed to schedule app refresh: \(error)")
        }
    }

    // MARK: - Execution

    private static func handleAppRefresh(_ task: BGAppRefreshTask) {
        // Schedule the next refresh immediately so the chain continues.
        scheduleAppRefresh()

        let taskOperation = Task {
            await performRefresh(task: task)
        }

        task.expirationHandler = {
            taskOperation.cancel()
            task.setTaskCompleted(success: false)
        }
    }

    @MainActor
    private static func performRefresh(task: BGAppRefreshTask) async {
        do {
            // 1. Refresh station data via the shared graph.
            try await BiziAppleGraph.shared.refreshData()

            // 2. Fetch favorite stations with their current availability.
            let favorites = try await BiziAppleGraph.shared.favoriteStations()
            guard !favorites.isEmpty else {
                task.setTaskCompleted(success: true)
                return
            }

            // 3. Determine which favorites newly have bikes (0 → >0 transition).
            let cache = StationAvailabilityCache()
            let newlyAvailable = cache.newlyAvailableStations(from: favorites)

            // 4. Persist updated counts for next comparison.
            cache.persist(favorites)

            // 5. Fire local notifications for newly-available stations.
            if !newlyAvailable.isEmpty {
                await BiziNotificationService.shared.notifyFavoriteStationsAvailable(newlyAvailable)
            }

            task.setTaskCompleted(success: true)
        } catch {
            print("[BiziBGTask] Refresh failed: \(error)")
            task.setTaskCompleted(success: false)
        }
    }
}
#endif
