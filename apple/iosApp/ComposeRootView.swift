import BiziMobileUi
import SwiftUI
import UIKit

/// Hosts the Compose UIViewController. A single instance is created and reused;
/// new launch requests are pushed reactively via `updateLaunchRequest` so that
/// the Compose tree is never torn down on navigation intents.
struct ComposeRootView: UIViewControllerRepresentable {
    let wrapper: BiziMainViewControllerWrapper

    func makeUIViewController(context: Context) -> UIViewController {
        ComposeContainerViewController(contentViewController: wrapper.viewController)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // No-op: launch requests are pushed through the wrapper directly.
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
