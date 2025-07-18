import Foundation
import CoreLocation

protocol LocationPermissionDelegate {
    func didChangeLocationPermission(_ permission: CLAuthorizationStatus)
}
