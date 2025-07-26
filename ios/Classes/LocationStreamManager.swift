import Foundation
import result_channel

protocol LocationStreamManager: Disposable {
    func startUpdates(resultChannel: ResultChannel)

    func setSettings(settings: iOSLocationSettings)

    func stopUpdates()
}
