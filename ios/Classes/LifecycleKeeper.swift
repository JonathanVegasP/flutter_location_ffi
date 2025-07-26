import Foundation
import result_channel

protocol LifecycleKeeper {
    func getCurrent(channel: ResultChannel, settings: iOSLocationSettings)
}
