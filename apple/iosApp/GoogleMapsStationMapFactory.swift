import BiziMobileUi
import GoogleMaps
import UIKit

final class GoogleMapsStationMapFactory: StationMapViewFactory {
    private var onStationSelected: ((Station) -> Void)?
    private weak var mapDelegate: GoogleMapsStationMapDelegate?

    func createView() -> UIView {
        let mapView = GMSMapView(frame: .zero)
        let camera = GMSCameraPosition.camera(
            withLatitude: 41.6561,
            longitude: -0.8773,
            zoom: 14
        )
        mapView.animate(to: camera)
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

        mapView.clear()

        let focusPoint = userLocation ?? stations.first?.location
        if let focus = focusPoint {
            let camera = GMSCameraPosition.camera(
                withLatitude: focus.latitude,
                longitude: focus.longitude,
                zoom: 14
            )
            mapView.animate(to: camera)
        }

        if let location = userLocation {
            let marker = GMSMarker()
            marker.position = CLLocationCoordinate2DMake(location.latitude, location.longitude)
            marker.title = "Tu ubicación"
            marker.icon = GMSMarker.markerImage(with: .systemBlue)
            marker.map = mapView
        }

        for station in stations {
            let marker = GMSMarker()
            marker.position = CLLocationCoordinate2DMake(
                station.location.latitude,
                station.location.longitude
            )
            marker.title = station.name
            marker.snippet = "\(station.bikesAvailable) bicis · \(station.slotsFree) libres"
            marker.icon = GMSMarker.markerImage(
                with: station.id == highlightedStationId
                    ? UIColor(red: 0.66, green: 0.08, blue: 0.10, alpha: 1)
                    : UIColor(red: 0.84, green: 0.10, blue: 0.12, alpha: 1)
            )
            marker.userData = station
            marker.map = mapView
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
