import CoreLocation
import Foundation
import UIKit
import result_channel

final class LocationPermissionManager: NSObject, PermissionManager {
    private let locationManager: CLLocationManager
    var resultChannel: ResultChannel?

    init(locationManager: CLLocationManager) {
        self.locationManager = locationManager
    }

    func checkAndRequestPermission(resultChannel: ResultChannel) {
        self.resultChannel = resultChannel
        locationManager.requestAlwaysAuthorization()
    }

    func checkPermission() -> LocationPermission {
        switch locationManager.authorizationStatus {
        case .authorizedWhenInUse, .authorizedAlways:
            return .granted
        case .denied, .restricted:
            return .neverAskAgain
        case .notDetermined:
            fallthrough
        @unknown default:
            return .denied
        }
    }

    func openAppSettings() {
        if #available(iOS 18.3, *) {
            if let url = URL(
                string: UIApplication.openDefaultApplicationsSettingsURLString
            ) {
                if UIApplication.shared.canOpenURL(url) {
                    UIApplication.shared.open(
                        url,
                        options: [:],
                        completionHandler: nil
                    )
                }
            }

            return
        }

        if let url = URL(string: UIApplication.openSettingsURLString) {
            if UIApplication.shared.canOpenURL(url) {
                UIApplication.shared.open(
                    url,
                    options: [:],
                    completionHandler: nil
                )
            }
        }
    }

    func dispose() {
        resultChannel?.failure(nil)
        resultChannel = nil
    }
}
