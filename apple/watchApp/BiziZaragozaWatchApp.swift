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

struct WatchDashboardView: View {
    @StateObject private var model = WatchDashboardModel()
    @ObservedObject private var syncBridge = WatchFavoritesSyncBridge.shared

    var body: some View {
        NavigationStack {
            List {
                Section {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Bizi Zaragoza")
                            .font(.headline)
                        Text("Consulta cercanas, favoritas y abre la ruta más rápido desde el reloj.")
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                        HStack(spacing: 8) {
                            WatchInfoBadge(label: "Cerca", value: "\(model.nearbyStations.count)")
                            WatchInfoBadge(label: "Fav", value: "\(syncBridge.favoriteIds.count)")
                        }
                    }
                    .padding(.vertical, 4)
                    .animation(.spring(response: 0.36, dampingFraction: 0.82), value: model.nearbyStations.count)
                    .animation(.spring(response: 0.36, dampingFraction: 0.82), value: syncBridge.favoriteIds.count)
                }

                if let errorMessage = model.errorMessage {
                    Section {
                        Text(errorMessage)
                            .font(.footnote)
                            .foregroundStyle(.red)
                            .transition(.opacity)
                    }
                }

                Section("Cerca de ti") {
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

                Section("Sincronizadas") {
                    if model.favoriteStations.isEmpty {
                        Text(syncBridge.favoriteIds.isEmpty
                             ? "Esperando favoritas del iPhone."
                             : "Todavía no he podido cargar tus favoritas.")
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
            .animation(.spring(response: 0.36, dampingFraction: 0.82), value: model.nearbyStations.count)
            .animation(.spring(response: 0.36, dampingFraction: 0.82), value: model.favoriteStations.count)
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
        VStack(alignment: .leading, spacing: 6) {
            Text(station.name)
                .font(.headline)
                .lineLimit(2)
            Text(station.address)
                .font(.footnote)
                .foregroundStyle(.secondary)
                .lineLimit(1)
            HStack(spacing: 6) {
                WatchInfoBadge(label: "m", value: "\(station.distanceMeters)")
                WatchInfoBadge(label: "B", value: "\(station.bikesAvailable)")
                WatchInfoBadge(label: "H", value: "\(station.slotsFree)")
            }
        }
        .padding(.vertical, 2)
        .animation(.spring(response: 0.32, dampingFraction: 0.85), value: station.bikesAvailable)
        .animation(.spring(response: 0.32, dampingFraction: 0.85), value: station.slotsFree)
    }
}

private struct WatchStationDetailView: View {
    let station: WatchStationSnapshot
    @State private var routeStatus: String?
    @State private var localRouteStatus: String?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text(station.name)
                    .font(.headline)
                Text(station.address)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                HStack(spacing: 8) {
                    WatchInfoBadge(label: "Dist.", value: "\(station.distanceMeters) m")
                    WatchInfoBadge(label: "Bicis", value: "\(station.bikesAvailable)")
                    WatchInfoBadge(label: "Huecos", value: "\(station.slotsFree)")
                }
                Button("Abrir en Maps") {
                    Task {
                        do {
                            let launchedStation = try await BiziWatchGraph.shared.openRoute(to: station.id)
                            localRouteStatus = launchedStation == nil
                                ? "No he encontrado esa estación para abrir Maps."
                                : "Abriendo la ruta en el reloj."
                        } catch {
                            localRouteStatus = "No he podido abrir Maps ahora mismo."
                        }
                    }
                }
                .buttonStyle(.bordered)
                Button("Abrir ruta en el iPhone") {
                    let requested = WatchFavoritesSyncBridge.shared.requestRoute(to: station.id)
                    routeStatus = requested
                        ? "He enviado la ruta al iPhone."
                        : "No he podido contactar con el iPhone."
                }
                .buttonStyle(.borderedProminent)
                if let localRouteStatus {
                    Text(localRouteStatus)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .transition(.opacity.combined(with: .move(edge: .bottom)))
                }
                if let routeStatus {
                    Text(routeStatus)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .transition(.opacity.combined(with: .move(edge: .bottom)))
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .animation(.spring(response: 0.32, dampingFraction: 0.84), value: localRouteStatus)
            .animation(.spring(response: 0.32, dampingFraction: 0.84), value: routeStatus)
        }
        .navigationTitle("Detalle")
    }
}

private struct WatchInfoBadge: View {
    let label: String
    let value: String

    var body: some View {
        VStack(spacing: 2) {
            Text(label)
                .font(.caption2)
                .foregroundStyle(.secondary)
            Text(value)
                .font(.footnote.weight(.semibold))
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 6)
        .background(.quaternary, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
        .animation(.spring(response: 0.3, dampingFraction: 0.82), value: value)
    }
}
