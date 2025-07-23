import Foundation
import result_channel

protocol LocationManager: LocationSettingsConfigurable {
    func getCurrent(resultChannel: ResultChannel)
}
