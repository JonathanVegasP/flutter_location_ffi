import Foundation
import result_channel

protocol LocationSettingsConfigurable: Disposable {
    func setSettings(settings: iOSLocationSettings)
}
