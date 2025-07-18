import Foundation
import CoreLocation

extension CoreLocationManager: CLLocationManagerDelegate {
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        delegate?.didChangeLocationPermission(manager.authorizationStatus)
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let now = Date()
        
        if let date = date, now.timeIntervalSince(date) < 1.0 {
            return
        }
        
        self.date = now
        
        guard let location = locations.last else {
            return
        }
        
        let accuracy = location.horizontalAccuracy
        
        guard accuracy < 50.0 else {
            return
        }
        
        let result = LocationDataFactory.create(gpsEnabled: true, latitude: location.coordinate.latitude, longitude: location.coordinate.longitude, accuracy: accuracy)
        
        if let channel = channel {
            self.channel = nil
            
            channel.success(result)
            
            if channelStream == nil {
                manager.stopUpdatingLocation()
                return
            }
        }
        
        channelStream?.success(result)
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: any Error) {
        if let error = error as? CLError, error.code == .denied {
            channelStream?.success(LocationDataFactory.create())
        }
    }
}
