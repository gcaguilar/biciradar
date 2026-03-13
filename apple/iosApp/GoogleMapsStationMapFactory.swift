import BiziMobileUi
import GoogleMaps
import UIKit

final class GoogleMapsStationMapFactory: StationMapViewFactory {
    private var onStationSelected: ((Station) -> Void)?
    private weak var mapDelegate: GoogleMapsStationMapDelegate?
    private var hasZoomed = false

    func createView() -> UIView {
        let mapView = GMSMapView(frame: .zero)
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
        onStationSelected: @escaping (Station) -> Void
    ) {
        guard let mapView = view as? GMSMapView else { return }
        self.onStationSelected = onStationSelected
        mapDelegate?.stations = stations
        mapDelegate?.highlightedStationId = highlightedStationId

        // Enable native blue dot for user location
        mapView.isMyLocationEnabled = userLocation != nil

        mapView.clear()

        // Zoom to user location only on first load
        if !hasZoomed {
            let focusPoint = userLocation ?? stations.first?.location
            if let focus = focusPoint {
                let camera = GMSCameraPosition.camera(
                    withLatitude: focus.latitude,
                    longitude: focus.longitude,
                    zoom: 15
                )
                mapView.animate(to: camera)
                hasZoomed = true
            }
        }

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
        }
    }

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
