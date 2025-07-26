import Foundation
import result_channel

final class LocationManagerLifecycleKeeper: LifecycleKeeper,
    LifecycleKeeperDelegate, Disposable
{
    private var managers: [LocationManager]? = []

    func getCurrent(channel: ResultChannel, settings: iOSLocationSettings) {
        let manager = CoreLocationManager(
            index: managers!.count,
            settings: settings
        )
        manager.delegate = self
        manager.getCurrent(resultChannel: channel)
        managers!.append(manager)
    }

    func objectWillRelease(at index: Int) {
        managers!.remove(at: index)
    }

    func dispose() {
        managers = nil
    }
}
