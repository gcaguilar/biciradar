import Core
import KotlinRuntime
import MobileUi
import GoogleMaps
import UIKit

final class GoogleMapsStationMapFactory: KotlinBase, StationMapViewFactory {
    private var onStationSelected: ((Station) -> Void)?
    private weak var mapDelegate: GoogleMapsStationMapDelegate?
    private var hasZoomed = false
    // Track markers by station id to avoid full redraw on highlight change
    private var markersByStationId: [String: GMSMarker] = [:]
    private var lastStations: [Station] = []
    private var lastHighlightedStationId: String? = nil
    private var lastRecenterRequestToken: Int32 = 0
    private var environmentalCircles: [GMSCircle] = []
    // Last camera target + viewport, so a resize (fold/unfold, rotation, iPad resize) can
    // re-frame the map instead of leaving the previous shape's framing on screen.
    private var lastFocusPoint: GeoPoint?
    private var lastPrefersTightZoom = false
    private var lastMapSize: CGSize = .zero

    func createView() -> UIView {
        // Reset per-view state so a second call doesn't inherit stale markers/zoom
        hasZoomed = false
        markersByStationId.removeAll()
        environmentalCircles.removeAll()
        lastStations = []
        lastHighlightedStationId = nil
        lastRecenterRequestToken = 0
        lastFocusPoint = nil
        lastPrefersTightZoom = false
        lastMapSize = .zero

        let mapView = GMSMapView(options: GMSMapViewOptions())
        mapView.settings.rotateGestures = false
        mapView.settings.tiltGestures = false
        mapView.settings.compassButton = false
        mapView.settings.myLocationButton = false
        let del = GoogleMapsStationMapDelegate { [weak self] station in
            self?.onStationSelected?(station)
        }
        mapView.delegate = del
        objc_setAssociatedObject(
            mapView,
            &GoogleMapsStationMapFactory.delegateKey,
            del,
            .OBJC_ASSOCIATION_RETAIN_NONATOMIC
        )
        self.mapDelegate = del
        return mapView
    }

    func updateView(
        view: UIView,
        stations: [Station],
        userLocation: GeoPoint?,
        highlightedStationId: String?,
        isDarkTheme: Bool,
        onStationSelected: @escaping (Station) -> Void,
        recenterRequestToken: Int32,
        environmentalOverlay: EnvironmentalOverlayData?
    ) {
        guard let mapView = view as? GMSMapView else { return }
        self.onStationSelected = onStationSelected
        mapDelegate?.stations = stations
        mapDelegate?.highlightedStationId = highlightedStationId
        mapView.overrideUserInterfaceStyle = isDarkTheme ? .dark : .light
        mapView.mapStyle = isDarkTheme ? try? GMSMapStyle(jsonString: Self.darkMapStyleJSON) : nil

        // Enable native blue dot for user location
        mapView.isMyLocationEnabled = userLocation != nil

        let focusPoint = userLocation ?? stations.first?.location
        if let focus = focusPoint {
            lastFocusPoint = focus
            lastPrefersTightZoom = userLocation != nil
        }
        let recenterRequested = recenterRequestToken != lastRecenterRequestToken
        lastRecenterRequestToken = recenterRequestToken
        // `bounds` is only meaningful once the view has been laid out. Re-applying the camera
        // when it changes is what makes fold/unfold and rotation re-frame the map: without it
        // the framing stayed frozen at whatever shape the map had when it first appeared.
        let mapSize = mapView.bounds.size
        let hasSize = mapSize.width > 0 && mapSize.height > 0
        let sizeChanged = hasSize && mapSize != lastMapSize
        if recenterRequested || sizeChanged || !hasZoomed, let focus = lastFocusPoint {
            mapView.animate(
                to: camera(for: focus, prefersTightZoom: lastPrefersTightZoom, in: mapSize)
            )
            hasZoomed = true
        }
        if hasSize {
            lastMapSize = mapSize
        }

        let stationsChanged = stations.map(\.id) != lastStations.map(\.id)
            || zip(stations, lastStations).contains(where: {
                $0.bikesAvailable != $1.bikesAvailable || $0.slotsFree != $1.slotsFree
            })

        environmentalCircles.forEach { $0.map = nil }
        environmentalCircles.removeAll()
        if let overlay = environmentalOverlay {
            for zone in overlay.zones {
                let center = CLLocationCoordinate2DMake(zone.center.latitude, zone.center.longitude)
                let circle = GMSCircle(position: center, radius: 450)
                let tone = environmentalTone(layer: overlay.layer, value: Int(zone.value))
                circle.fillColor = tone.withAlphaComponent(
                    CGFloat(ENVIRONMENTAL_OVERLAY_FILL_ALPHA)
                )
                circle.strokeColor = tone.withAlphaComponent(
                    CGFloat(ENVIRONMENTAL_OVERLAY_STROKE_ALPHA)
                )
                circle.strokeWidth = 1
                circle.map = mapView
                environmentalCircles.append(circle)
            }
        }

        if stationsChanged {
            // Full redraw: station list or state changed
            mapView.clear()
            markersByStationId.removeAll()
            environmentalCircles.forEach { $0.map = mapView }

            for station in stations {
                let marker = GMSMarker()
                marker.position = CLLocationCoordinate2DMake(
                    station.location.latitude,
                    station.location.longitude
                )
                marker.title = station.name
                marker.snippet = "\(station.bikesAvailable) bicis · \(station.slotsFree) libres"
                marker.icon = GMSMarker.markerImage(with: stationMarkerColor(
                    station: station,
                    highlighted: station.id == highlightedStationId
                ))
                marker.userData = station
                marker.map = mapView
                markersByStationId[station.id] = marker
            }
            lastStations = stations
        } else if highlightedStationId != lastHighlightedStationId {
            // Only the selection changed — update the two affected markers in place
            let affectedIds: [String?] = [lastHighlightedStationId, highlightedStationId]
            for stationId in affectedIds.compactMap({ $0 }) {
                guard let marker = markersByStationId[stationId],
                      let station = lastStations.first(where: { $0.id == stationId }) else { continue }
                marker.icon = GMSMarker.markerImage(with: stationMarkerColor(
                    station: station,
                    highlighted: stationId == highlightedStationId
                ))
            }
        }

        lastHighlightedStationId = highlightedStationId
    }

    private func environmentalTone(layer: EnvironmentalOverlayLayer, value: Int) -> UIColor {
        let layerName = String(describing: layer).lowercased()
        if layerName.contains("air") || layerName.contains("aqi") {
            if value <= 50 { return UIColor(red: 0.20, green: 0.72, blue: 0.20, alpha: 1) }
            if value <= 100 { return UIColor(red: 0.95, green: 0.50, blue: 0.00, alpha: 1) }
            return UIColor(red: 0.84, green: 0.10, blue: 0.12, alpha: 1)
        }
        if value <= 10 { return UIColor(red: 0.20, green: 0.72, blue: 0.20, alpha: 1) }
        if value <= 30 { return UIColor(red: 0.95, green: 0.50, blue: 0.00, alpha: 1) }
        return UIColor(red: 0.84, green: 0.10, blue: 0.12, alpha: 1)
    }

    private func camera(
        for point: GeoPoint,
        prefersTightZoom: Bool,
        in size: CGSize
    ) -> GMSCameraPosition {
        GMSCameraPosition.camera(
            withLatitude: point.latitude,
            longitude: point.longitude,
            zoom: adjustedZoom(base: prefersTightZoom ? 15 : 13, for: size)
        )
    }

    /// Google Maps zoom is per-frame, not per-window: the same zoom shows a much larger area on
    /// a wider viewport (iPad, iPhone Duo unfolded, landscape), so the phone-tuned framing used
    /// to silently loosen as soon as the window changed shape. Scaling by the short side keeps
    /// the same visible neighbourhood, which is what the framing is actually about.
    private func adjustedZoom(base: Float, for size: CGSize) -> Float {
        let shortSide = min(size.width, size.height)
        guard shortSide > 0 else { return base }
        return base + Float(log2(Double(shortSide / Self.referenceShortSide)))
    }

    /// Short side (in points) of a standard phone window; the zoom levels above are tuned for it.
    private static let referenceShortSide: CGFloat = 393

    private func stationMarkerColor(station: Station, highlighted: Bool) -> UIColor {
        let hasBikes = station.bikesAvailable > 0
        let hasSlots = station.slotsFree > 0
        switch (hasBikes, hasSlots) {
        case (true, true):
            return highlighted
                ? UIColor(red: 0.10, green: 0.50, blue: 0.10, alpha: 1)  // dark green
                : UIColor(red: 0.20, green: 0.72, blue: 0.20, alpha: 1)  // green
        case (false, false):
            return highlighted
                ? UIColor(red: 0.66, green: 0.08, blue: 0.10, alpha: 1)  // dark red
                : UIColor(red: 0.84, green: 0.10, blue: 0.12, alpha: 1)  // red
        default:
            return highlighted
                ? UIColor(red: 0.70, green: 0.35, blue: 0.00, alpha: 1)  // dark orange
                : UIColor(red: 0.95, green: 0.50, blue: 0.00, alpha: 1)  // orange
        }
    }

    private static var delegateKey: UInt8 = 0
    private static let darkMapStyleJSON = """
    [
      {"elementType":"geometry","stylers":[{"color":"#0f172a"}]},
      {"elementType":"labels.text.fill","stylers":[{"color":"#cbd5e1"}]},
      {"elementType":"labels.text.stroke","stylers":[{"color":"#0f172a"}]},
      {"featureType":"administrative","elementType":"geometry.stroke","stylers":[{"color":"#334155"}]},
      {"featureType":"landscape.man_made","elementType":"geometry","stylers":[{"color":"#111827"}]},
      {"featureType":"landscape.natural","elementType":"geometry","stylers":[{"color":"#0b1220"}]},
      {"featureType":"poi","elementType":"geometry","stylers":[{"color":"#111827"}]},
      {"featureType":"poi.park","elementType":"geometry","stylers":[{"color":"#0b3b2e"}]},
      {"featureType":"road","elementType":"geometry","stylers":[{"color":"#1f2937"}]},
      {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#0f172a"}]},
      {"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#334155"}]},
      {"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#1e293b"}]},
      {"featureType":"transit","elementType":"geometry","stylers":[{"color":"#172033"}]},
      {"featureType":"water","elementType":"geometry","stylers":[{"color":"#0a2540"}]},
      {"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#93c5fd"}]}
    ]
    """
}

private final class GoogleMapsStationMapDelegate: NSObject, GMSMapViewDelegate {
    var stations: [Station] = []
    var highlightedStationId: String?
    private let onStationSelected: (Station) -> Void

    init(onStationSelected: @escaping (Station) -> Void) {
        self.onStationSelected = onStationSelected
    }

    func mapView(_ mapView: GMSMapView, didTap marker: GMSMarker) -> Bool {
        if let station = marker.userData as? Station {
            onStationSelected(station)
        }
        return false
    }
}
