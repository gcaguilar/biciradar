import UIKit

// SwiftUI owns the scene lifecycle, so Home Screen quick-action taps reach us through the
// application delegate:
//
//  * `didFinishLaunchingWithOptions` carries the shortcut item on a cold start.
//  * `performActionFor` carries it when the app is already in memory, including when it is in
//    the foreground and the scene phase therefore never changes.
//
// Both funnel into `BiziHomeScreenQuickActionRouter`, which stores the launch request and posts
// a notification that `BiciRadarApp` applies to the shared Compose instance.
//
// Note: do NOT implement `application(_:configurationForConnecting:options:)` here. Returning a
// scene configuration from a SwiftUI `@UIApplicationDelegateAdaptor` makes SwiftUI's
// `AppSceneDelegate` recurse in `responds(to:)` during `-[UIWindowScene setDelegate:]` and the
// app crashes at launch (verified on the iOS 26.5 simulator). The launch-options and
// perform-action methods cover both cold and warm starts without touching scene configuration.
final class BiziAppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        if let shortcutItem = launchOptions?[.shortcutItem] as? UIApplicationShortcutItem {
            // Cold start: the Compose instance does not exist yet, so only store the request.
            // `BiciRadarApp.applyPendingLaunchRequest()` picks it up when the view appears.
            BiziHomeScreenQuickActionRouter.handle(shortcutItem)
        }
        return true
    }

    func application(
        _ application: UIApplication,
        performActionFor shortcutItem: UIApplicationShortcutItem,
        completionHandler: @escaping (Bool) -> Void
    ) {
        completionHandler(BiziHomeScreenQuickActionRouter.handle(shortcutItem))
    }
}