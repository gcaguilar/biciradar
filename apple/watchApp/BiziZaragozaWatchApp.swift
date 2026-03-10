import SwiftUI

@main
struct BiziZaragozaWatchApp: App {
    init() {
        WatchFavoritesSyncBridge.shared.activate()
    }

    var body: some Scene {
        WindowGroup {
            WatchDashboardView()
        }
    }
}

@MainActor
final class WatchDashboardModel: ObservableObject {
    @Published private(set) var nearbyStations: [WatchStationSnapshot] = []
    @Published private(set) var favoriteStations: [WatchStationSnapshot] = []
    @Published private(set) var isLoading = false
    @Published private(set) var errorMessage: String?

    func refresh(favoriteIds: Set<String>) async {
        isLoading = true
        errorMessage = nil
        do {
            nearbyStations = try await BiziWatchGraph.shared.nearbyStations()
            favoriteStations = try await BiziWatchGraph.shared.favoriteStations(favoriteIds: favoriteIds)
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }
}

struct WatchDashboardView: View {
    @StateObject private var model = WatchDashboardModel()
    @ObservedObject private var syncBridge = WatchFavoritesSyncBridge.shared

    var body: some View {
        NavigationStack {
            List {
                if let errorMessage = model.errorMessage {
                    Section {
                        Text(errorMessage)
                            .font(.footnote)
                            .foregroundStyle(.red)
                    }
                }

                Section("Cercanas") {
                    if model.nearbyStations.isEmpty, model.isLoading {
                        ProgressView()
                    } else {
                        ForEach(model.nearbyStations) { station in
                            NavigationLink {
                                WatchStationDetailView(station: station)
                            } label: {
                                StationRow(station: station)
                            }
                        }
                    }
                }

                Section("Favoritas") {
                    if model.favoriteStations.isEmpty {
                        Text(syncBridge.favoriteIds.isEmpty
                             ? "Esperando favoritas desde el iPhone."
                             : "Todavía no he podido resolver tus favoritas.")
                            .font(.footnote)
                    } else {
                        ForEach(model.favoriteStations) { station in
                            NavigationLink {
                                WatchStationDetailView(station: station)
                            } label: {
                                StationRow(station: station)
                            }
                        }
                    }
                }

                Section("Acciones") {
                    Button("Actualizar") {
                        Task {
                            await model.refresh(favoriteIds: syncBridge.favoriteIds)
                        }
                    }
                }
            }
            .navigationTitle("Bizi")
            .task {
                await model.refresh(favoriteIds: syncBridge.favoriteIds)
            }
            .onChange(of: syncBridge.favoriteIds) { favoriteIds in
                Task {
                    await model.refresh(favoriteIds: favoriteIds)
                }
            }
        }
    }
}

private struct StationRow: View {
    let station: WatchStationSnapshot

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(station.name)
                .font(.headline)
                .lineLimit(2)
            Text("\(station.distanceMeters) m · \(station.bikesAvailable) bicis · \(station.slotsFree) huecos")
                .font(.footnote)
                .foregroundStyle(.secondary)
        }
    }
}

private struct WatchStationDetailView: View {
    let station: WatchStationSnapshot
    @State private var routeStatus: String?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 8) {
                Text(station.name)
                    .font(.headline)
                Text(station.address)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                Label("\(station.distanceMeters) metros", systemImage: "location")
                Label("\(station.bikesAvailable) bicis", systemImage: "bicycle")
                Label("\(station.slotsFree) huecos", systemImage: "dock.rectangle")
                Button("Abrir ruta en el iPhone") {
                    let requested = WatchFavoritesSyncBridge.shared.requestRoute(to: station.id)
                    routeStatus = requested
                        ? "He enviado la ruta al iPhone."
                        : "No he podido contactar con el iPhone."
                }
                .buttonStyle(.borderedProminent)
                if let routeStatus {
                    Text(routeStatus)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .navigationTitle("Detalle")
    }
}
