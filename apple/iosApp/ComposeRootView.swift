import BiziMobileUi
import SwiftUI
import UIKit

struct ComposeRootView: UIViewControllerRepresentable {
    let launchRequest: (any MobileLaunchRequest)?

    func makeUIViewController(context: Context) -> UIViewController {
        ComposeContainerViewController(contentViewController: makeContentViewController())
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}

    private func makeContentViewController() -> UIViewController {
        let factory: (any StationMapViewFactory)? = GoogleMapsBootstrap.isSdkLinked()
            ? GoogleMapsStationMapFactory()
            : nil
        if let launchRequest {
            return BiziMobileViewControllerKt.MainViewController(
                launchRequest: launchRequest,
                stationMapViewFactory: factory
            )
        } else {
            return BiziMobileViewControllerKt.MainViewController(
                launchRequest: nil,
                stationMapViewFactory: factory
            )
        }
    }
}

final class ComposeContainerViewController: UIViewController {
    private let contentViewController: UIViewController

    init(contentViewController: UIViewController) {
        self.contentViewController = contentViewController
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = .systemBackground
        addChild(contentViewController)
        contentViewController.view.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(contentViewController.view)

        NSLayoutConstraint.activate([
            contentViewController.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            contentViewController.view.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            contentViewController.view.topAnchor.constraint(equalTo: view.topAnchor),
            contentViewController.view.bottomAnchor.constraint(equalTo: view.bottomAnchor),
        ])

        contentViewController.didMove(toParent: self)
    }
}
