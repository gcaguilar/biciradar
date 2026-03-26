import AppIntents
import SwiftUI

// MARK: - Paleta BiciRadar

private extension Color {
    // Tema claro
    static let biziPrimary = Color(hex: 0x1D74BD)
    static let biziSecondary = Color(hex: 0x64C23A)
    static let biziTertiary = Color(hex: 0x0D1B2A)
    static let biziNeutral = Color(hex: 0x64779D)
    // Tema oscuro (watch siempre oscuro)
    static let biziDarkPrimary = Color(hex: 0x1070CA)
    static let biziDarkSecondary = Color(hex: 0x64C832)
    static let biziDarkTertiary = Color(hex: 0xA05ABA)
    static let biziDarkNeutral = Color(hex: 0x0F172A)
    // Semánticos
    static let biziError = Color(hex: 0xCF6679)
    static let biziWarning = Color(hex: 0xF28000)

    init(hex: UInt32) {
        let r = Double((hex >> 16) & 0xFF) / 255
        let g = Double((hex >> 8) & 0xFF) / 255
        let b = Double(hex & 0xFF) / 255
        self.init(red: r, green: g, blue: b)
    }
}

// MARK: - App

@main
struct BiciRadarWatchApp: App {
    init() {
        WatchFavoritesSyncBridge.shared.activate()
        Task {
            try? await WatchBiziAppShortcuts.updateAppShortcutParameters()
        }
    }

    var body: some Scene {
        WindowGroup {
            WatchDashboardView()
        }
    }
}

// MARK: - Dashboard

struct WatchDashboardView: View {
    @StateObject private var model = WatchDashboardModel()
    @ObservedObject private var syncBridge = WatchFavoritesSyncBridge.shared

    var body: some View {
        NavigationStack {
            List {
                // Header
                Section {
                    VStack(alignment: .leading, spacing: 6) {
                        HStack(spacing: 6) {
                            Circle()
                                .fill(Color.biziDarkPrimary)
                                .frame(width: 8, height: 8)
                            Text("BiciRadar")
                                .font(.headline)
                                .foregroundStyle(Color.biziDarkPrimary)
                        }
                        Text("Estaciones cerca y favoritas.")
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                        HStack(spacing: 6) {
                            WatchCountBadge(
                                label: "Cerca",
                                count: model.nearbyStations.count,
                                color: .biziDarkPrimary
                            )
                            WatchCountBadge(
                                label: "Fav",
                                count: syncBridge.favoriteIds.count,
                                color: .biziDarkSecondary
                            )
                        }
                    }
                    .padding(.vertical, 4)
                    .animation(.spring(response: 0.36, dampingFraction: 0.82), value: model.nearbyStations.count)
                    .animation(.spring(response: 0.36, dampingFraction: 0.82), value: syncBridge.favoriteIds.count)
                }

                if let errorMessage = model.errorMessage {
                    Section {
                        Label(errorMessage, systemImage: "exclamationmark.triangle.fill")
                            .font(.footnote)
                            .foregroundStyle(Color.biziError)
                            .transition(.opacity)
                    }
                }

                Section("Cerca de ti") {
                    if model.nearbyStations.isEmpty, model.isLoading {
                        HStack {
                            ProgressView()
                                .tint(Color.biziDarkPrimary)
                            Text("Cargando…")
                                .font(.footnote)
                                .foregroundStyle(.secondary)
                        }
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
                            .foregroundStyle(.secondary)
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

                Section {
                    Button {
                        Task {
                            await model.refresh(favoriteIds: syncBridge.favoriteIds)
                        }
                    } label: {
                        Label("Actualizar", systemImage: "arrow.clockwise")
                            .foregroundStyle(Color.biziDarkPrimary)
                    }
                }
            }
            .navigationTitle("BiciRadar")
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

// MARK: - Station Row

private struct StationRow: View {
    let station: WatchStationSnapshot

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(station.name)
                .font(.headline)
                .lineLimit(2)
            Text(station.address)
                .font(.footnote)
                .foregroundStyle(.secondary)
                .lineLimit(1)
            HStack(spacing: 5) {
                WatchInfoBadge(
                    label: "m",
                    value: "\(station.distanceMeters)",
                    tint: .biziNeutral
                )
                WatchInfoBadge(
                    label: "B",
                    value: "\(station.bikesAvailable)",
                    tint: station.bikesAvailable > 0 ? .biziDarkPrimary : .biziError
                )
                WatchInfoBadge(
                    label: "H",
                    value: "\(station.slotsFree)",
                    tint: station.slotsFree > 0 ? .biziDarkSecondary : .biziError
                )
            }
        }
        .padding(.vertical, 2)
        .animation(.spring(response: 0.32, dampingFraction: 0.85), value: station.bikesAvailable)
        .animation(.spring(response: 0.32, dampingFraction: 0.85), value: station.slotsFree)
    }
}

// MARK: - Detail View

private struct WatchStationDetailView: View {
    let station: WatchStationSnapshot
    @State private var routeStatus: String?
    @State private var localRouteStatus: String?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 10) {
                Text(station.name)
                    .font(.headline)
                    .foregroundStyle(Color.biziDarkPrimary)
                Text(station.address)
                    .font(.footnote)
                    .foregroundStyle(.secondary)

                // Stats row
                HStack(spacing: 6) {
                    WatchStatPill(
                        systemImage: "location.fill",
                        value: "\(station.distanceMeters) m",
                        tint: .biziNeutral
                    )
                    WatchStatPill(
                        systemImage: "bicycle",
                        value: "\(station.bikesAvailable)",
                        tint: station.bikesAvailable > 0 ? .biziDarkPrimary : .biziError
                    )
                    WatchStatPill(
                        systemImage: "parkingsign",
                        value: "\(station.slotsFree)",
                        tint: station.slotsFree > 0 ? .biziDarkSecondary : .biziError
                    )
                }

                Button {
                    Task {
                        do {
                            let launchedStation = try await BiziWatchGraph.shared.openRoute(to: station.id)
                            localRouteStatus = launchedStation == nil
                                ? "No he encontrado esa estación."
                                : "Abriendo la ruta en el reloj."
                        } catch {
                            localRouteStatus = "No se pudo abrir la ruta."
                        }
                    }
                } label: {
                    Label("Ruta aquí", systemImage: "figure.walk")
                }
                .buttonStyle(.bordered)
                .tint(Color.biziDarkPrimary)

                Button {
                    let requested = WatchFavoritesSyncBridge.shared.requestRoute(to: station.id)
                    routeStatus = requested
                        ? "He enviado la ruta al iPhone."
                        : "No he podido contactar con el iPhone."
                } label: {
                    Label("Ruta en iPhone", systemImage: "iphone")
                }
                .buttonStyle(.borderedProminent)
                .tint(Color.biziDarkSecondary)

                if let localRouteStatus {
                    StatusMessage(text: localRouteStatus)
                }
                if let routeStatus {
                    StatusMessage(text: routeStatus)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .animation(.spring(response: 0.32, dampingFraction: 0.84), value: localRouteStatus)
            .animation(.spring(response: 0.32, dampingFraction: 0.84), value: routeStatus)
        }
        .navigationTitle("Detalle")
    }
}

// MARK: - Reusable Components

private struct WatchInfoBadge: View {
    let label: String
    let value: String
    var tint: Color = .biziNeutral

    var body: some View {
        VStack(spacing: 2) {
            Text(label)
                .font(.caption2)
                .foregroundStyle(tint.opacity(0.7))
            Text(value)
                .font(.footnote.weight(.semibold))
                .foregroundStyle(tint)
        }
        .padding(.horizontal, 7)
        .padding(.vertical, 5)
        .background(tint.opacity(0.15), in: RoundedRectangle(cornerRadius: 10, style: .continuous))
        .animation(.spring(response: 0.3, dampingFraction: 0.82), value: value)
    }
}

private struct WatchCountBadge: View {
    let label: String
    let count: Int
    let color: Color

    var body: some View {
        HStack(spacing: 3) {
            Text("\(count)")
                .font(.footnote.weight(.bold))
                .foregroundStyle(color)
            Text(label)
                .font(.caption2)
                .foregroundStyle(color.opacity(0.7))
        }
        .padding(.horizontal, 7)
        .padding(.vertical, 4)
        .background(color.opacity(0.15), in: RoundedRectangle(cornerRadius: 8, style: .continuous))
    }
}

private struct WatchStatPill: View {
    let systemImage: String
    let value: String
    let tint: Color

    var body: some View {
        HStack(spacing: 3) {
            Image(systemName: systemImage)
                .font(.caption2)
            Text(value)
                .font(.caption.weight(.semibold))
        }
        .foregroundStyle(tint)
        .padding(.horizontal, 6)
        .padding(.vertical, 4)
        .background(tint.opacity(0.15), in: RoundedRectangle(cornerRadius: 8, style: .continuous))
    }
}

private struct StatusMessage: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.footnote)
            .foregroundStyle(.secondary)
            .transition(.opacity.combined(with: .move(edge: .bottom)))
    }
}
