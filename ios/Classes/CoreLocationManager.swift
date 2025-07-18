import Foundation
import CoreLocation
import result_channel

final class CoreLocationManager : NSObject,LocationManager {
    private let locationManager: CLLocationManager
    var channel: ResultChannel?
    var channelStream: ResultChannel?
    var delegate: LocationPermissionDelegate?
    var date: Date?
    
    init(locationManager: CLLocationManager) {
        self.locationManager = locationManager
        
        super.init()
        
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.distanceFilter = 1
        locationManager.allowsBackgroundLocationUpdates = false
        locationManager.activityType = .fitness
        locationManager.pausesLocationUpdatesAutomatically = false
        locationManager.showsBackgroundLocationIndicator = false
    }
    
    func getCurrent(resultChannel: ResultChannel) {
        DispatchQueue.global(qos: .userInitiated).async {
            guard CLLocationManager.locationServicesEnabled() else {
                resultChannel.success(LocationDataFactory.create())
                return
            }
            
            DispatchQueue.main.async {
                self.channel = resultChannel
                if let _ = self.channelStream {
                    return
                }
                self.locationManager.requestLocation()
            }
        }
    }
    
    func startUpdates(resultChannel: ResultChannel) {
        if let _ = channelStream  {
            resultChannel.failure(ErrorMessages.startUpdatesFailed)
            return
        }
        
        DispatchQueue.global(qos: .userInitiated).async {
            if !CLLocationManager.locationServicesEnabled()  {
                resultChannel.success(LocationDataFactory.create())
            }
            
            DispatchQueue.main.async {
                self.channelStream = resultChannel
                self.locationManager.startUpdatingLocation()
            }
        }
    }
    
    func stopUpdates() {
        guard let channel = channelStream else {
            return
        }
        
        channelStream = nil
        
        channel.failure(nil)
        self.locationManager.stopUpdatingLocation()
    }
    
    func dispose() {
        stopUpdates()
        channel?.failure(nil)
        channel = nil
        delegate = nil
    }
}
