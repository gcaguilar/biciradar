import BiziMobileUi
import SwiftUI

struct ComposeRootView: UIViewControllerRepresentable {
    let launchRequest: (any MobileLaunchRequest)?

    func makeUIViewController(context: Context) -> UIViewController {
        if let launchRequest {
            return BiziMobileViewControllerKt.MainViewController(launchRequest: launchRequest)
        } else {
            return BiziMobileViewControllerKt.RootViewController()
        }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
