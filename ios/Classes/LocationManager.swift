import Foundation
import result_channel

protocol LocationManager : Disposable {
    func getCurrent(resultChannel: ResultChannel)
    
    func startUpdates(resultChannel: ResultChannel)
    
    func stopUpdates()
}
