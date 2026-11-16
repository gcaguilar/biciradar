import SwiftUI
import WidgetKit

private struct WatchFavoriteStationEntry: TimelineEntry {
    let date: Date
    let snapshot: WatchSurfaceSnapshotBundle?

    var favoriteStation: WatchStationSnapshot? {
        snapshot?.favoriteStations.first
    }

    var nearbyStations: [WatchStationSnapshot] {
        Array((snapshot?.nearbyStations ?? []).prefix(3))
    }

    var generatedAt: Date? {
        guard let generatedAtEpoch = snapshot?.generatedAtEpoch else { return nil }
        return Date(timeIntervalSince1970: Double(generatedAtEpoch) / 1000)
    }

    var isStale: Bool {
        guard let generatedAt else { return false }
        return date.timeIntervalSince(generatedAt) >= 30 * 60
    }
}

private struct WatchFavoriteStationTimelineProvider: TimelineProvider {
    private let surfaceStore: WatchSurfaceSnapshotStore

    init(surfaceStore: WatchSurfaceSnapshotStore = .shared) {
        self.surfaceStore = surfaceStore
    }

    func placeholder(in context: Context) -> WatchFavoriteStationEntry {
        WatchFavoriteStationEntry(date: .now, snapshot: sampleSnapshot)
    }

    func getSnapshot(in context: Context, completion: @escaping (WatchFavoriteStationEntry) -> Void) {
        let snapshot = surfaceStore.read() ?? sampleSnapshot
        completion(WatchFavoriteStationEntry(date: .now, snapshot: snapshot))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<WatchFavoriteStationEntry>) -> Void) {
        let entry = WatchFavoriteStationEntry(date: .now, snapshot: surfaceStore.read())
        let refreshDate = Calendar.current.date(byAdding: .minute, value: 15, to: .now) ?? .now.addingTimeInterval(900)
        completion(Timeline(entries: [entry], policy: .after(refreshDate)))
    }
}

@main
struct BiciRadarWatchWidgets: WidgetBundle {
    var body: some Widget {
        FavoriteStationComplication()
        NearbyStationsWatchWidget()
    }
}

struct NearbyStationsWatchWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(
            kind: "BiciRadarWatchNearbyWidget",
            provider: WatchFavoriteStationTimelineProvider()
        ) { entry in
            NearbyStationsWidgetView(entry: entry)
                .containerBackground(.fill.tertiary, for: .widget)
        }
        .configurationDisplayName("Cercanas")
        .description("Consulta las estaciones más cercanas con sus bicis y huecos desde la esfera o el Smart Stack.")
        .supportedFamilies([.accessoryInline, .accessoryCircular, .accessoryRectangular])
    }
}

private struct NearbyStationsWidgetView: View {
    let entry: WatchFavoriteStationEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        switch family {
        case .accessoryInline:
            if let station = entry.nearbyStations.first {
                Text("\(station.name) \(station.bikesAvailable)/\(station.slotsFree)")
            } else {
                Text("Sin cercanas")
            }
        case .accessoryCircular:
            circularView
        default:
            rectangularView
        }
    }

    private var circularView: some View {
        ZStack {
            AccessoryWidgetBackground()
            if let station = entry.nearbyStations.first {
                VStack(spacing: 1) {
                    Image(systemName: "location.fill")
                        .font(.system(size: 8))
                        .widgetAccentable()
                    Text("\(station.bikesAvailable)")
                        .font(.system(.title3, design: .rounded).bold())
                        .foregroundStyle(statusColor(for: station.statusLevel))
                        .widgetAccentable()
                    Text("\(station.slotsFree)")
                        .font(.system(.caption2, design: .rounded).weight(.semibold))
                        .foregroundStyle(.secondary)
                }
            } else {
                VStack(spacing: 2) {
                    Image(systemName: "location.slash")
                        .font(.caption2.weight(.semibold))
                    Text("--")
                        .font(.system(.title3, design: .rounded).bold())
                }
            }
        }
    }

    private var rectangularView: some View {
        Group {
            if entry.nearbyStations.isEmpty {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Sin cercanas")
                        .font(.caption.weight(.semibold))
                    Text("Abre la app para actualizar")
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                        .lineLimit(2)
                }
            } else {
                VStack(alignment: .leading, spacing: 2) {
                    ForEach(entry.nearbyStations) { station in
                        HStack(spacing: 4) {
                            Circle()
                                .fill(statusColor(for: station.statusLevel))
                                .frame(width: 6, height: 6)
                                .widgetAccentable()
                            Text(station.name)
                                .font(.caption2.weight(.semibold))
                                .lineLimit(1)
                            Spacer(minLength: 0)
                            Text("\(station.bikesAvailable)/\(station.slotsFree)")
                                .font(.system(.caption2, design: .rounded).weight(.semibold))
                        }
                    }
                }
            }
        }
    }
}

struct FavoriteStationComplication: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(
            kind: "BiciRadarWatchFavoriteComplication",
            provider: WatchFavoriteStationTimelineProvider()
        ) { entry in
            FavoriteStationComplicationView(entry: entry)
                // Required since watchOS 10; accessory families draw their own backdrop.
                .containerBackground(.fill.tertiary, for: .widget)
        }
        .configurationDisplayName("Favorita")
        .description("Consulta bicis, huecos y estado de tu estación favorita desde la esfera.")
        .supportedFamilies([.accessoryInline, .accessoryCircular, .accessoryRectangular])
    }
}

private struct FavoriteStationComplicationView: View {
    let entry: WatchFavoriteStationEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        switch family {
        case .accessoryInline:
            inlineView
        case .accessoryCircular:
            circularView
        default:
            rectangularView
        }
    }

    private var inlineView: some View {
        Group {
            if let station = entry.favoriteStation {
                Text("Fav \(station.bikesAvailable)/\(station.slotsFree)")
            } else {
                Text("Sin favorita")
            }
        }
    }

    private var circularView: some View {
        ZStack {
            AccessoryWidgetBackground()
            if let station = entry.favoriteStation {
                VStack(spacing: 1) {
                    Circle()
                        .fill(statusColor(for: station.statusLevel))
                        .frame(width: 7, height: 7)
                        .widgetAccentable()
                    Text("\(station.bikesAvailable)")
                        .font(.system(.title3, design: .rounded).bold())
                        .foregroundStyle(statusColor(for: station.statusLevel))
                        .widgetAccentable()
                    Text("\(station.slotsFree)")
                        .font(.system(.caption2, design: .rounded).weight(.semibold))
                        .foregroundStyle(.secondary)
                }
            } else {
                VStack(spacing: 2) {
                    Image(systemName: "heart.slash")
                        .font(.caption2.weight(.semibold))
                    Text("--")
                        .font(.system(.title3, design: .rounded).bold())
                }
            }
        }
    }

    private var rectangularView: some View {
        Group {
            if let station = entry.favoriteStation {
                VStack(alignment: .leading, spacing: 2) {
                    HStack(spacing: 4) {
                        Text(station.name)
                            .font(.caption.weight(.semibold))
                            .lineLimit(1)
                            .widgetAccentable()
                        Spacer(minLength: 0)
                        Circle()
                            .fill(statusColor(for: station.statusLevel))
                            .frame(width: 8, height: 8)
                            .widgetAccentable()
                    }
                    Text("\(station.bikesAvailable) bicis · \(station.slotsFree) huecos")
                        .font(.system(.caption, design: .rounded).weight(.semibold))
                        .lineLimit(1)
                    Text(rectangularFooter(for: station))
                        .font(.caption2)
                        .foregroundStyle(entry.isStale ? .orange : .secondary)
                        .lineLimit(1)
                }
            } else {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Sin favorita")
                        .font(.caption.weight(.semibold))
                    Text("Sincroniza una desde el iPhone")
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                        .lineLimit(2)
                }
            }
        }
    }

    private func rectangularFooter(for station: WatchStationSnapshot) -> String {
        if entry.isStale {
            return String(localized: "Snapshot desactualizado")
        }
        return "\(station.statusText) · \(relativeUpdateText(from: entry.generatedAt))"
    }
}

private func statusColor(for level: WatchStationStatusLevel) -> Color {
    switch level {
    case .good:
        return Color(red: 0.39, green: 0.78, blue: 0.23)
    case .low:
        return Color(red: 0.95, green: 0.54, blue: 0.0)
    case .empty, .full:
        return Color(red: 0.81, green: 0.4, blue: 0.47)
    }
}

private func relativeUpdateText(from date: Date?) -> String {
    guard let date else { return String(localized: "Sin snapshot") }
    let seconds = max(0, Int(Date().timeIntervalSince(date)))
    if seconds < 60 {
        return String(localized: "Ahora")
    }
    if seconds < 3600 {
        return String(localized: "Hace \(seconds / 60)m")
    }
    return String(localized: "Hace \(seconds / 3600)h")
}

private let sampleSnapshot = WatchSurfaceSnapshotBundle(
    generatedAtEpoch: Int64(Date().timeIntervalSince1970 * 1000),
    nearbyStations: [
        WatchStationSnapshot(
            id: "station-nearby",
            name: "Plaza España",
            address: "Centro",
            bikesAvailable: 7,
            slotsFree: 5,
            distanceMeters: 140
        )
    ],
    favoriteStations: [
        WatchStationSnapshot(
            id: "station-favorite",
            name: "Universidad",
            address: "Campus",
            bikesAvailable: 7,
            slotsFree: 5,
            distanceMeters: 180
        )
    ]
)
