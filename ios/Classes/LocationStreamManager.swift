import Foundation
import result_channel

protocol LocationStreamManager: LocationSettingsConfigurable {
    func startUpdates(resultChannel: ResultChannel)

    func stopUpdates()
}
