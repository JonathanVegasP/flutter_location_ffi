import CoreLocation
import Foundation
import result_channel

final class CoreLocationManager: NSObject, LocationManager {
    private let locationManager: CLLocationManager
    var settings: iOSLocationSettings
    var channel: ResultChannel?
    var delegate: LocationPermissionDelegate?

    init(locationManager: CLLocationManager, settings: iOSLocationSettings) {
        self.locationManager = locationManager
        self.settings = settings

        super.init()

        locationManager.delegate = self

        initSettings()
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

    func setSettings(settings: iOSLocationSettings) {
        self.settings = settings

        initSettings()
    }

    func dispose() {
        channel?.failure(nil)
        channel = nil
        delegate = nil
    }
}
