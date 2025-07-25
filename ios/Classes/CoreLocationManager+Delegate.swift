import CoreLocation
import Foundation

extension CoreLocationManager: CLLocationManagerDelegate {
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

        let timestamp: TimeInterval = location.timestamp.timeIntervalSinceNow

        if timestamp > LocationDataConstants.locationMaxIntervalSinceNow {
            return
        }

        let accuracy = location.horizontalAccuracy

        if accuracy > settings.accuracyFilter {
            return
        }

        let result = LocationDataFactory.create(
            gpsEnabled: true,
            latitude: location.coordinate.latitude,
            longitude: location.coordinate.longitude,
            accuracy: accuracy
        )

        let channel = channel
        
        self.channel = nil

        channel!.success(result)

        manager.stopUpdatingLocation()
    }
}
