import Foundation
import result_channel

protocol LocationManager {
    func getCurrent(resultChannel: ResultChannel)
}
