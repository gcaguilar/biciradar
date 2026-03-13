import BiziMobileUi
import GoogleMaps
import UIKit

final class GoogleMapsStationMapFactory: StationMapViewFactory {
    private var onStationSelected: ((BiziMobileUiStation) -> Void)?
    private weak var delegate: GoogleMapsStationMapDelegate?

    func createView() -> UIView {
        let camera = GMSCameraPosition.camera(
            withLatitude: 41.6561,
            longitude: -0.8773,
            zoom: 14
        )
        let mapView = GMSMapView(frame: .zero, camera: camera)
        mapView.settings.rotateGestures = false
        mapView.settings.tiltGestures = false
        mapView.settings.compassButton = false
        mapView.settings.myLocationButton = false
        let del = GoogleMapsStationMapDelegate { [weak self] station in
            self?.onStationSelected?(station)
        }
        mapView.delegate = del
        // Retain the delegate on the mapView via objc association
        objc_setAssociatedObject(mapView, &GoogleMapsStationMapFactory.delegateKey, del, .OBJC_ASSOCIATION_RETAIN_NONATOMIC)
        self.delegate = del
        return mapView
    }

    func updateView(
        view: UIView,
        stations: [BiziMobileUiStation],
        userLocation: BiziMobileUiGeoPoint?,
        highlightedStationId: String?,
        onStationSelected: @escaping (BiziMobileUiStation) -> Void
    ) {
        guard let mapView = view as? GMSMapView else { return }
        self.onStationSelected = onStationSelected
        delegate?.stations = stations
        delegate?.highlightedStationId = highlightedStationId

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
            marker.position = CLLocationCoordinate2DMake(station.location.latitude, station.location.longitude)
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
    var stations: [BiziMobileUiStation] = []
    var highlightedStationId: String?
    private let onStationSelected: (BiziMobileUiStation) -> Void

    init(onStationSelected: @escaping (BiziMobileUiStation) -> Void) {
        self.onStationSelected = onStationSelected
    }

    func mapView(_ mapView: GMSMapView, didTap marker: GMSMarker) -> Bool {
        if let station = marker.userData as? BiziMobileUiStation {
            onStationSelected(station)
        }
        return false
    }
}
