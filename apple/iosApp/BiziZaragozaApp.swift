import BiziMobileUi
import SwiftUI

@main
struct BiziZaragozaApp: App {
    @Environment(\.scenePhase) private var scenePhase
    @State private var launchRequest: (any MobileLaunchRequest)?
    @State private var launchToken: Int = 0

    init() {
        FavoritesSyncBridge.shared.activate()
    }

    var body: some Scene {
        WindowGroup {
            ComposeRootView(launchRequest: launchRequest)
                .id(launchToken)
                .onAppear {
                    applyPendingLaunchRequest()
                    syncFavoritesToWatch()
                }
                .onChange(of: scenePhase) { newPhase in
                    if newPhase == .active {
                        applyPendingLaunchRequest()
                        syncFavoritesToWatch()
                    }
                }
        }
    }

    private func applyPendingLaunchRequest() {
        guard let request = AppleLaunchRequestStore.shared.takePendingRequest() else { return }
        launchRequest = request
        launchToken += 1
    }

    private func syncFavoritesToWatch() {
        Task {
            guard let favorites = try? await BiziAppleGraph.shared.favoriteStations() else { return }
            await MainActor.run {
                FavoritesSyncBridge.shared.pushFavorites(Set(favorites.map(\.id)))
            }
        }
    }
}

struct IOSAssistantShortcutsView: View {
    var body: some View {
        List {
            Label("Estación más cercana", systemImage: "location.circle")
            Label("Mis favoritas", systemImage: "heart.circle")
            Label("Estado de estación", systemImage: "info.circle")
            Label("Ruta a estación", systemImage: "map.circle")
        }
        .navigationTitle("Atajos")
    }
}
