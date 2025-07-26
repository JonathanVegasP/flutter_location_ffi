import CoreLocation
import Foundation
import result_channel

final class CoreLocationStreamManager: NSObject, LocationStreamManager {
    private let locationManager: CLLocationManager
    var settings: iOSLocationSettings
    var channelStream: ResultChannel?
    var delegate: LocationPermissionDelegate?

    init(locationManager: CLLocationManager, settings: iOSLocationSettings) {
        self.locationManager = locationManager
        self.settings = settings

        super.init()

        locationManager.delegate = self

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

    func startUpdates(resultChannel: ResultChannel) {
        if let channel = channelStream {
            channelStream = nil
            channel.failure(nil)
        }

        DispatchQueue.global(qos: .userInitiated).async {
            if !CLLocationManager.locationServicesEnabled() {
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

        self.locationManager.stopUpdatingLocation()

        channelStream = nil

        channel.failure(nil)
    }

    func setSettings(settings: iOSLocationSettings) {
        self.settings = settings

        initSettings()
    }

    func dispose() {
        stopUpdates()
    }
}
