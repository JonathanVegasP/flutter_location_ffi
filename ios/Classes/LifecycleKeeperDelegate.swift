import Foundation

protocol LifecycleKeeperDelegate {
    func objectWillRelease(at index: Int)
}
