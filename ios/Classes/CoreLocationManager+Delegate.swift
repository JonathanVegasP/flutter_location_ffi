import CoreLocation
import Foundation

extension CoreLocationManager: CLLocationManagerDelegate {
    func locationManager(
        _ manager: CLLocationManager,
        didUpdateLocations locations: [CLLocation]
    ) {
        guard let location = locations.last else {
            return
        }

        let timestamp: TimeInterval = location.timestamp.timeIntervalSinceNow

        guard timestamp <= LocationDataConstants.locationMaxIntervalSinceNow
        else {
            return
        }

        guard let accuracy = settings.validate(location: location) else {
            return
        }

        let result = LocationDataFactory.create(
            accuracy: accuracy,
            location: location
        )

        channel!.success(result)

        channel = nil

        manager.stopUpdatingLocation()

        delegate!.objectWillRelease(at: memoryIndex)
        delegate = nil
    }

    func locationManager(
        _ manager: CLLocationManager,
        didFailWithError error: any Error
    ) {
        let channel = channel
        self.channel = nil
        delegate!.objectWillRelease(at: memoryIndex)
        delegate = nil

        guard let error = error as? CLError, error.code == .denied else {
            channel!.failure(error.localizedDescription)

            return
        }

        channel!.failure(error.localizedDescription)
    }
}
