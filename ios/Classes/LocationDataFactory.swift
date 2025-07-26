import CoreLocation
import Foundation

enum LocationDataFactory {
    static func create(
        accuracy: CLLocationAccuracy,
        location: CLLocation
    ) -> [Any?] {
        let gpsEnabled = true
        let coordinate = location.coordinate
        let latitude = coordinate.latitude
        let longitude = coordinate.longitude
        let timestamp = location.timestamp.timeIntervalSince1970 * 1000
        let verticalAccuracy = location.verticalAccuracy
        var altitudeAccuracy = 0.0
        var altitude = 0.0
        var altitudeEllipsoid = 0.0

        if verticalAccuracy > 0.0 {
            altitudeAccuracy = verticalAccuracy
            altitude = location.altitude

            if #available(iOS 15, *) {
                altitudeEllipsoid = location.ellipsoidalAltitude
            }
        }

        let courseAccuracy = location.courseAccuracy
        var headerAccuracy = 0.0
        var header = 0.0

        if courseAccuracy >= 0.0 {
            headerAccuracy = courseAccuracy
            header = location.course
        }

        let rawSpeedAccuracy = location.speedAccuracy
        var speedAccuracy = 0.0
        var speed = 0.0

        if rawSpeedAccuracy >= 0.0 {
            speedAccuracy = rawSpeedAccuracy
            speed = location.speed
        }

        let floor = location.floor?.level

        return [
            gpsEnabled, latitude, longitude, accuracy, Int(timestamp),
            altitudeEllipsoid, altitude, altitudeAccuracy, header,
            headerAccuracy, speed, speedAccuracy, floor,
        ]
    }

    static func create() -> [Any?] {
        let gpsEnabled = false
        let coordinate = 0.0
        let timestamp = Date().timeIntervalSince1970 * 1000
        
        return [
            gpsEnabled, coordinate, coordinate, coordinate, Int(timestamp),
            coordinate, coordinate, coordinate, coordinate, coordinate,
            coordinate, coordinate, nil,
        ]
    }
}
