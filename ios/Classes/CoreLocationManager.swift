import CoreLocation
import Foundation
import result_channel

final class CoreLocationManager: NSObject, LocationManager {
    private let locationManager: CLLocationManager = CLLocationManager()
    let memoryIndex: Int
    let settings: iOSLocationSettings
    var channel: ResultChannel?
    var delegate: LifecycleKeeperDelegate?

    init(index: Int, settings: iOSLocationSettings) {
        self.memoryIndex = index
        self.settings = settings

        super.init()

        self.locationManager.delegate = self

        self.initSettings()
    }

    private func initSettings() {
        let settings = settings

        locationManager.desiredAccuracy = settings.priority
        locationManager.distanceFilter = settings.distanceFilter
        locationManager.activityType = settings.activityType
        locationManager.pausesLocationUpdatesAutomatically =
            settings.pausesLocationUpdatesAutomatically
        locationManager.allowsBackgroundLocationUpdates =
            settings.allowsBackgroundLocationUpdates
        locationManager.showsBackgroundLocationIndicator =
            settings.showsBackgroundLocationIndicator
        locationManager.headingFilter = settings.headingFilter
    }

    func getCurrent(resultChannel: ResultChannel) {
        DispatchQueue.global(qos: .userInitiated).async {
            guard CLLocationManager.locationServicesEnabled() else {
                resultChannel.success(LocationDataFactory.create())
                return
            }

            DispatchQueue.main.async {
                self.channel = resultChannel
                self.locationManager.startUpdatingLocation()
            }
        }
    }
}
