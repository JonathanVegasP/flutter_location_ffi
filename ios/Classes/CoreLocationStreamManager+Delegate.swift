import CoreLocation
import Foundation

extension CoreLocationStreamManager: CLLocationManagerDelegate {
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
        
        channelStream!.success(result)
    }

    func locationManager(
        _ manager: CLLocationManager,
        didFailWithError error: any Error
    ) {
        if let error = error as? CLError, error.code == .denied {
            channelStream?.success(LocationDataFactory.create())
        }
    }
}
