import SwiftUI
import WidgetKit

#if canImport(ActivityKit)
import ActivityKit
#endif

private struct SurfaceEntry: TimelineEntry {
    let date: Date
    let bundle: AppleSurfaceSnapshotBundle?

    var favoriteStation: AppleSurfaceStationSnapshot? { bundle?.favoriteStation }
    var nearbyStations: [AppleSurfaceStationSnapshot] { bundle?.nearbyStations ?? [] }
    var state: AppleSurfaceState? { bundle?.state }
}

private struct SurfaceTimelineProvider: TimelineProvider {
    func placeholder(in context: Context) -> SurfaceEntry {
        SurfaceEntry(date: .now, bundle: sampleBundle)
    }

    func getSnapshot(in context: Context, completion: @escaping (SurfaceEntry) -> Void) {
        completion(SurfaceEntry(date: .now, bundle: BiziSurfaceStore.readSnapshotBundle() ?? sampleBundle))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<SurfaceEntry>) -> Void) {
        let entry = SurfaceEntry(date: .now, bundle: BiziSurfaceStore.readSnapshotBundle())
        let refreshDate = Calendar.current.date(byAdding: .minute, value: 15, to: .now) ?? .now.addingTimeInterval(900)
        completion(Timeline(entries: [entry], policy: .after(refreshDate)))
    }
}

@main
struct BiciRadarWidgets: WidgetBundle {
    var body: some Widget {
        FavoriteStationWidget()
        NearbyStationsWidget()
        BikesLockScreenWidget()
        DocksLockScreenWidget()
        StationStatusLockScreenWidget()
        if #available(iOSApplicationExtension 16.1, *) {
            BiziMonitoringLiveActivityWidget()
        }
    }
}

struct FavoriteStationWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "BiciRadarFavoriteStation", provider: SurfaceTimelineProvider()) { entry in
            FavoriteStationWidgetView(entry: entry)
        }
        .configurationDisplayName("Estación favorita")
        .description("Consulta bicis, huecos y la última actualización sin abrir la app.")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

struct NearbyStationsWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "BiciRadarNearbyStations", provider: SurfaceTimelineProvider()) { entry in
            NearbyStationsWidgetView(entry: entry)
        }
        .configurationDisplayName("Estaciones cercanas")
        .description("Muestra las estaciones más próximas desde el último snapshot disponible.")
        .supportedFamilies([.systemMedium])
    }
}

struct BikesLockScreenWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "BiciRadarLockBikes", provider: SurfaceTimelineProvider()) { entry in
            BikesAccessoryView(entry: entry)
        }
        .configurationDisplayName("Bicis favorita")
        .description("Bicis disponibles de tu estación favorita.")
        .supportedFamilies([.accessoryInline, .accessoryCircular, .accessoryRectangular])
    }
}

struct DocksLockScreenWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "BiciRadarLockDocks", provider: SurfaceTimelineProvider()) { entry in
            DocksAccessoryView(entry: entry)
        }
        .configurationDisplayName("Huecos favorita")
        .description("Huecos libres de tu estación favorita.")
        .supportedFamilies([.accessoryInline, .accessoryCircular, .accessoryRectangular])
    }
}

struct StationStatusLockScreenWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "BiciRadarLockStatus", provider: SurfaceTimelineProvider()) { entry in
            StationStatusAccessoryView(entry: entry)
        }
        .configurationDisplayName("Estado favorita")
        .description("Estado compacto de tu estación favorita.")
        .supportedFamilies([.accessoryInline, .accessoryRectangular])
    }
}

private struct FavoriteStationWidgetView: View {
    let entry: SurfaceEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        ZStack {
            LinearGradient(colors: [Color(red: 0.98, green: 0.96, blue: 0.94), Color.white], startPoint: .topLeading, endPoint: .bottomTrailing)
            if let station = entry.favoriteStation {
                VStack(alignment: .leading, spacing: 10) {
                    Text(station.nameShort)
                        .font(family == .systemSmall ? .headline : .title3.weight(.semibold))
                        .foregroundStyle(Color(red: 0.05, green: 0.11, blue: 0.16))
                        .lineLimit(2)
                    HStack(spacing: 12) {
                        metricBlock(label: "Bicis", value: station.bikesAvailable, tint: Color(red: 0.11, green: 0.45, blue: 0.74))
                        metricBlock(label: "Huecos", value: station.docksAvailable, tint: Color(red: 0.21, green: 0.58, blue: 0.27))
                    }
                    Spacer(minLength: 0)
                    HStack {
                        Text(station.statusTextShort)
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(statusColor(station.statusLevel))
                        Spacer()
                        Text(BiziSurfaceStore.relativeUpdateText(lastUpdatedEpoch: station.lastUpdatedEpoch))
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                    }
                }
                .padding(16)
            } else {
                fallbackView(title: "BiciRadar", message: fallbackMessage(for: entry))
                    .padding(16)
            }
        }
        .widgetURL(entry.favoriteStation.map { URL(string: "biciradar://station/\($0.id)")! } ?? URL(string: "biciradar://favorites"))
    }

    private func metricBlock(label: String, value: Int, tint: Color) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label)
                .font(.caption2)
                .foregroundStyle(.secondary)
            Text("\(value)")
                .font(.system(size: 28, weight: .bold, design: .rounded))
                .foregroundStyle(tint)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct NearbyStationsWidgetView: View {
    let entry: SurfaceEntry

    var body: some View {
        ZStack {
            LinearGradient(colors: [Color(red: 0.95, green: 0.97, blue: 1.0), Color.white], startPoint: .topLeading, endPoint: .bottomTrailing)
            if entry.nearbyStations.isEmpty {
                fallbackView(title: "Cercanas", message: fallbackMessage(for: entry))
                    .padding(16)
            } else {
                VStack(alignment: .leading, spacing: 10) {
                    Text("Cerca de ti")
                        .font(.headline)
                        .foregroundStyle(Color(red: 0.05, green: 0.11, blue: 0.16))
                    ForEach(entry.nearbyStations.prefix(3)) { station in
                        HStack(spacing: 8) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(station.nameShort)
                                    .font(.caption.weight(.semibold))
                                    .lineLimit(1)
                                Text(station.distanceMeters.map { "\($0) m" } ?? station.statusTextShort)
                                    .font(.caption2)
                                    .foregroundStyle(.secondary)
                            }
                            Spacer()
                            Text("\(station.bikesAvailable) / \(station.docksAvailable)")
                                .font(.caption.weight(.bold))
                                .foregroundStyle(statusColor(station.statusLevel))
                        }
                    }
                }
                .padding(16)
            }
        }
        .widgetURL(URL(string: "biciradar://home"))
    }
}

private struct BikesAccessoryView: View {
    let entry: SurfaceEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        let bikes = entry.favoriteStation?.bikesAvailable
        switch family {
        case .accessoryInline:
            Text(bikes.map { "Bicis \($0)" } ?? "Sin favorita")
        case .accessoryCircular:
            ZStack {
                AccessoryWidgetBackground()
                Text(bikes.map(String.init) ?? "--")
                    .font(.system(.title3, design: .rounded).bold())
            }
        default:
            VStack(alignment: .leading) {
                Text("Bicis")
                Text(bikes.map(String.init) ?? "--")
                    .font(.title2.bold())
            }
        }
    }
}

private struct DocksAccessoryView: View {
    let entry: SurfaceEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        let docks = entry.favoriteStation?.docksAvailable
        switch family {
        case .accessoryInline:
            Text(docks.map { "Huecos \($0)" } ?? "Sin favorita")
        case .accessoryCircular:
            ZStack {
                AccessoryWidgetBackground()
                Text(docks.map(String.init) ?? "--")
                    .font(.system(.title3, design: .rounded).bold())
            }
        default:
            VStack(alignment: .leading) {
                Text("Huecos")
                Text(docks.map(String.init) ?? "--")
                    .font(.title2.bold())
            }
        }
    }
}

private struct StationStatusAccessoryView: View {
    let entry: SurfaceEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        let status = entry.favoriteStation?.statusTextShort ?? "Sin favorita"
        switch family {
        case .accessoryInline:
            Text(status)
        default:
            VStack(alignment: .leading) {
                Text(entry.favoriteStation?.nameShort ?? "BiciRadar")
                    .lineLimit(1)
                Text(status)
                    .foregroundStyle(entry.favoriteStation.map { statusColor($0.statusLevel) } ?? .secondary)
            }
        }
    }
}

#if canImport(ActivityKit)
@available(iOSApplicationExtension 16.1, *)
struct BiziMonitoringLiveActivityWidget: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: BiziMonitoringActivityAttributes.self) { context in
            VStack(alignment: .leading, spacing: 8) {
                Text(context.attributes.stationName)
                    .font(.headline)
                HStack(spacing: 12) {
                    Label("\(context.state.bikesAvailable)", systemImage: "bicycle")
                    Label("\(context.state.docksAvailable)", systemImage: "parkingsign")
                }
                Text(context.state.statusText)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                if let alternativeName = context.state.alternativeName {
                    Text("Alt: \(alternativeName)")
                        .font(.caption2)
                }
                Text(Date(timeIntervalSince1970: TimeInterval(context.state.expiresAtEpoch) / 1000), style: .timer)
                    .font(.caption2.monospacedDigit())
            }
            .padding(16)
            .activityBackgroundTint(Color(red: 0.95, green: 0.97, blue: 1.0))
            .activitySystemActionForegroundColor(Color(red: 0.11, green: 0.45, blue: 0.74))
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    Text("\(context.state.bikesAvailable) bicis")
                }
                DynamicIslandExpandedRegion(.trailing) {
                    Text("\(context.state.docksAvailable) huecos")
                }
                DynamicIslandExpandedRegion(.bottom) {
                    VStack(alignment: .leading) {
                        Text(context.attributes.stationName)
                            .font(.headline)
                        Text(context.state.statusText)
                            .font(.caption)
                        if let alternativeName = context.state.alternativeName {
                            Text("Alt: \(alternativeName)")
                                .font(.caption2)
                        }
                    }
                }
            } compactLeading: {
                Text("\(context.state.bikesAvailable)")
            } compactTrailing: {
                Text("\(context.state.docksAvailable)")
            } minimal: {
                Text("\(context.state.bikesAvailable)")
            }
        }
    }
}
#endif

private func fallbackMessage(for entry: SurfaceEntry) -> String {
    guard let state = entry.state else { return "Abre la app para actualizar" }
    if !state.hasFavoriteStation {
        return "Configura una estación favorita"
    }
    if !state.isDataFresh {
        return "Abre la app para actualizar"
    }
    return "Datos no disponibles"
}

private func fallbackView(title: String, message: String) -> some View {
    VStack(alignment: .leading, spacing: 8) {
        Text(title)
            .font(.headline)
        Text(message)
            .font(.caption)
            .foregroundStyle(.secondary)
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
}

private func statusColor(_ level: AppleSurfaceStatusLevel) -> Color {
    switch level {
    case .good:
        return Color(red: 0.21, green: 0.58, blue: 0.27)
    case .low:
        return Color.orange
    case .empty, .full:
        return Color.red
    case .unavailable:
        return Color.secondary
    }
}

private let sampleBundle = AppleSurfaceSnapshotBundle(
    generatedAtEpoch: 0,
    favoriteStation: AppleSurfaceStationSnapshot(
        id: "42",
        nameShort: "Plaza Espana",
        nameFull: "Plaza Espana",
        cityId: "zaragoza",
        latitude: 0,
        longitude: 0,
        bikesAvailable: 8,
        docksAvailable: 5,
        statusTextShort: "Disponible",
        statusLevel: .good,
        lastUpdatedEpoch: Int64(Date().timeIntervalSince1970 * 1000),
        distanceMeters: 120,
        isFavorite: true,
        alternativeStationId: nil,
        alternativeStationName: nil,
        alternativeDistanceMeters: nil
    ),
    nearbyStations: [
        AppleSurfaceStationSnapshot(
            id: "42",
            nameShort: "Plaza Espana",
            nameFull: "Plaza Espana",
            cityId: "zaragoza",
            latitude: 0,
            longitude: 0,
            bikesAvailable: 8,
            docksAvailable: 5,
            statusTextShort: "Disponible",
            statusLevel: .good,
            lastUpdatedEpoch: Int64(Date().timeIntervalSince1970 * 1000),
            distanceMeters: 120,
            isFavorite: true,
            alternativeStationId: nil,
            alternativeStationName: nil,
            alternativeDistanceMeters: nil
        ),
        AppleSurfaceStationSnapshot(
            id: "13",
            nameShort: "Paraninfo",
            nameFull: "Paraninfo",
            cityId: "zaragoza",
            latitude: 0,
            longitude: 0,
            bikesAvailable: 3,
            docksAvailable: 7,
            statusTextShort: "Pocas",
            statusLevel: .low,
            lastUpdatedEpoch: Int64(Date().timeIntervalSince1970 * 1000),
            distanceMeters: 260,
            isFavorite: false,
            alternativeStationId: nil,
            alternativeStationName: nil,
            alternativeDistanceMeters: nil
        )
    ],
    monitoringSession: nil,
    state: AppleSurfaceState(
        hasLocationPermission: true,
        hasNotificationPermission: true,
        hasFavoriteStation: true,
        isDataFresh: true,
        lastSyncEpoch: Int64(Date().timeIntervalSince1970 * 1000),
        cityId: "zaragoza",
        cityName: "Zaragoza",
        userLatitude: nil,
        userLongitude: nil
    )
)
