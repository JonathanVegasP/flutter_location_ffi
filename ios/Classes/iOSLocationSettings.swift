import CoreLocation
import Foundation

final class iOSLocationSettings {
    let priority: CLLocationAccuracy
    let distanceFilter: CLLocationDistance
    private let accuracyFilter: CLLocationAccuracy
    let activityType: CLActivityType
    let pausesLocationUpdatesAutomatically: Bool
    let allowsBackgroundLocationUpdates: Bool
    let showsBackgroundLocationIndicator: Bool
    let headingFilter: CLLocationDegrees

    private init(
        priority: CLLocationAccuracy,
        distanceFilter: CLLocationDistance,
        accuracyFilter: Double,
        activityType: CLActivityType,
        pausesLocationUpdatesAutomatically: Bool,
        allowsBackgroundLocationUpdates: Bool,
        showsBackgroundLocationIndicator: Bool,
        headingFilter: CLLocationDegrees
    ) {
        self.priority = priority
        self.distanceFilter = distanceFilter
        self.accuracyFilter = accuracyFilter
        self.activityType = activityType
        self.pausesLocationUpdatesAutomatically =
            pausesLocationUpdatesAutomatically
        self.allowsBackgroundLocationUpdates = allowsBackgroundLocationUpdates
        self.showsBackgroundLocationIndicator = showsBackgroundLocationIndicator
        self.headingFilter = headingFilter
    }

    static func standard() -> iOSLocationSettings {
        let priority = kCLLocationAccuracyHundredMeters
        let distanceFilter = kCLDistanceFilterNone
        let accuracyFilter = iOSLocationSettingsConstants.standardAccuracyFilter
        let activityType: CLActivityType = .other
        let pausesLocationUpdatesAutomatically = true
        let allowsBackgroundLocationUpdates = false
        let showsBackgroundLocationIndicator = true
        let headingFilter = kCLHeadingFilterNone

        return iOSLocationSettings(
            priority: priority,
            distanceFilter: distanceFilter,
            accuracyFilter: accuracyFilter,
            activityType: activityType,
            pausesLocationUpdatesAutomatically:
                pausesLocationUpdatesAutomatically,
            allowsBackgroundLocationUpdates: allowsBackgroundLocationUpdates,
            showsBackgroundLocationIndicator: showsBackgroundLocationIndicator,
            headingFilter: headingFilter
        )
    }

    static func custom(data: [Any?]) -> iOSLocationSettings {
        let priority: CLLocationAccuracy

        switch data[0] as! Int {
        case 0:
            priority = kCLLocationAccuracyReduced
        case 1:
            priority = kCLLocationAccuracyThreeKilometers
        case 2:
            priority = kCLLocationAccuracyKilometer
        case 3:
            priority = kCLLocationAccuracyHundredMeters
        case 4:
            priority = kCLLocationAccuracyNearestTenMeters
        case 5:
            priority = kCLLocationAccuracyBest
        case 6:
            priority = kCLLocationAccuracyBestForNavigation
        default:
            priority = kCLLocationAccuracyHundredMeters
        }

        let distanceFilter = data[1] as! CLLocationDistance
        let accuracyFilter = data[2] as! CLLocationAccuracy
        let activityType: CLActivityType

        switch data[3] as! Int {
        case 0:
            activityType = .other
        case 1:
            activityType = .automotiveNavigation
        case 2:
            activityType = .fitness
        case 3:
            activityType = .otherNavigation
        case 4:
            activityType = .airborne
        default:
            activityType = .other
        }

        let pausesLocationUpdatesAutomatically = data[4] as! Bool
        let allowsBackgroundLocationUpdates = data[5] as! Bool
        let showsBackgroundLocationIndicator = data[6] as! Bool
        let headingFilter = data[7] as! CLLocationDegrees

        return iOSLocationSettings(
            priority: priority,
            distanceFilter: distanceFilter > 0
                ? distanceFilter : kCLDistanceFilterNone,
            accuracyFilter: accuracyFilter,
            activityType: activityType,
            pausesLocationUpdatesAutomatically:
                pausesLocationUpdatesAutomatically,
            allowsBackgroundLocationUpdates: allowsBackgroundLocationUpdates,
            showsBackgroundLocationIndicator: showsBackgroundLocationIndicator,
            headingFilter: headingFilter > 0
                ? headingFilter : kCLHeadingFilterNone
        )
    }

    func validate(location: CLLocation) -> CLLocationAccuracy? {
        let accuracy = location.horizontalAccuracy

        guard accuracy <= accuracyFilter else {
            return nil
        }

        return accuracy
    }
}
