import BiziMobileUi
import SwiftUI

struct ComposeRootView: UIViewControllerRepresentable {
    let launchRequest: (any MobileLaunchRequest)?

    func makeUIViewController(context: Context) -> UIViewController {
        BiziMobileViewControllerKt.MainViewController(launchRequest: launchRequest)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
