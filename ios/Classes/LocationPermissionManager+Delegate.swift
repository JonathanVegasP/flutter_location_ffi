import CoreLocation
import Foundation

extension LocationPermissionManager: LocationPermissionDelegate {
    func didChangeLocationPermission(_ permission: CLAuthorizationStatus) {
        guard let channel = resultChannel else {
            return
        }

        resultChannel = nil

        switch permission {
        case .authorizedWhenInUse, .authorizedAlways:
            channel.success(LocationPermission.granted.rawValue)
        case .denied, .restricted:
            channel.success(LocationPermission.neverAskAgain.rawValue)
        case .notDetermined:
            fallthrough
        @unknown default:
            channel.success(LocationPermission.denied.rawValue)
        }
    }
}
