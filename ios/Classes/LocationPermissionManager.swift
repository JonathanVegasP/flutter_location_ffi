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
        let permission = checkPermission()

        guard permission != .denied else {
            self.resultChannel = resultChannel
            locationManager.requestAlwaysAuthorization()
            return
        }

        resultChannel.success(permission.rawValue)
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
        guard #available(iOS 18.3, *) else {
            guard
                let url = URL(
                    string: UIApplication.openSettingsURLString
                )
            else {
                return
            }

            guard UIApplication.shared.canOpenURL(url) else {
                return
            }

            UIApplication.shared.open(
                url,
                options: [:],
                completionHandler: nil
            )

            return
        }

        guard
            let url = URL(
                string: UIApplication.openDefaultApplicationsSettingsURLString
            )
        else {
            return
        }

        guard UIApplication.shared.canOpenURL(url) else {
            return
        }

        UIApplication.shared.open(
            url,
            options: [:],
            completionHandler: nil
        )
    }

    func dispose() {
        guard let channel = resultChannel else {
            return
        }

        resultChannel = nil
        channel.failure(nil)
    }
}
