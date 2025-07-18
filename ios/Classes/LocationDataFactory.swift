import Foundation

enum LocationDataFactory {
    static func create(gpsEnabled: Bool = false, latitude: Double = 0.0, longitude: Double = 0.0, accuracy: Double = 0.0) -> [Any?] {
        
        return [gpsEnabled, latitude, longitude, accuracy]
    }
}
