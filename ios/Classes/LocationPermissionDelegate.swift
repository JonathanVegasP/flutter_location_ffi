import CoreLocation
import Foundation

protocol LocationPermissionDelegate {
    func didChangeLocationPermission(_ permission: CLAuthorizationStatus)
}
