import CoreLocation
import Foundation

extension CoreLocationStreamManager: CLLocationManagerDelegate {
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        delegate!.didChangeLocationPermission(manager.authorizationStatus)
    }

    func locationManager(
        _ manager: CLLocationManager,
        didUpdateLocations locations: [CLLocation]
    ) {
        guard let location = locations.last else {
            return
        }

        guard let accuracy = settings.validate(location: location) else {
            return
        }

        let result = LocationDataFactory.create(
            accuracy: accuracy,
            location: location
        )

        channelStream!.success(result)
    }

    func locationManager(
        _ manager: CLLocationManager,
        didFailWithError error: any Error
    ) {
        guard let error = error as? CLError, error.code == .denied else {
            channelStream!.failure(error.localizedDescription)
            return
        }

        channelStream!.success(LocationDataFactory.create())
    }
}
