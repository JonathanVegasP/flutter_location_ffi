import Foundation
import result_channel

protocol PermissionManager : Disposable {
    func checkAndRequestPermission(resultChannel: ResultChannel)
    
    func checkPermission() -> LocationPermission
    
    func openAppSettings()
}
