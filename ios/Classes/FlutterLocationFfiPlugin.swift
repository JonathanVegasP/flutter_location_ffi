import CoreLocation
import Flutter
import Foundation
import UIKit
import result_channel

public final class FlutterLocationFfiPlugin: NSObject, FlutterPlugin {
    static var permissionManager: PermissionManager? = nil
    static var locationStreamManager: LocationStreamManager? = nil
    static var currentSettings: iOSLocationSettings? = nil
    static var lifecycleKeeper: LifecycleKeeper? = nil

    public static func register(with registrar: FlutterPluginRegistrar) {
        let manager = CLLocationManager()

        let locationPermissionManager = LocationPermissionManager(
            locationManager: manager
        )

        permissionManager = locationPermissionManager
        let settings = iOSLocationSettings.standard()
        currentSettings = settings

        let core = CoreLocationStreamManager(
            locationManager: manager,
            settings: settings
        )

        core.delegate = locationPermissionManager
        locationStreamManager = core
        lifecycleKeeper = LocationManagerLifecycleKeeper()
        let instance = FlutterLocationFfiPlugin()

        registrar.addApplicationDelegate(instance)
    }

    public func applicationWillTerminate(_ application: UIApplication) {
        Self.locationStreamManager!.dispose()
        Self.locationStreamManager = nil
        Self.permissionManager!.dispose()
        Self.permissionManager = nil
        Self.currentSettings = nil
    }
}

@_cdecl("flutter_location_ffi_set_settings")
public func setSettings(result: UnsafeMutablePointer<ResultNative>?) {
    let rawData = result!.pointee

    let data = ResultChannel.serializer.deserialize(
        value: Data(bytes: rawData.data, count: Int(rawData.size))
    )

    let settings = iOSLocationSettings.custom(data: data as! [Any?])

    FlutterLocationFfiPlugin.locationStreamManager!
        .setSettings(settings: settings)

    FlutterLocationFfiPlugin.currentSettings = settings
}

@_cdecl("flutter_location_ffi_check_and_request_permission")
public func checkAndRequestPermission(callback: @escaping ResultCallback) {
    FlutterLocationFfiPlugin.permissionManager!
        .checkAndRequestPermission(
            resultChannel: ResultChannel(resultCallback: callback)
        )
}

@_cdecl("flutter_location_ffi_check_permission")
public func checkPermission() -> UnsafeMutablePointer<ResultNative>? {
    let value = FlutterLocationFfiPlugin.permissionManager!.checkPermission()

    let result = ResultChannel.createResultNative(
        status: ResultChannelStatusOk,
        data: value.rawValue
    )

    return result
}

@_cdecl("flutter_location_ffi_get_current")
public func getCurrent(callback: @escaping ResultCallback) {
    let channel = ResultChannel(resultCallback: callback)
    let settings = FlutterLocationFfiPlugin.currentSettings!

    FlutterLocationFfiPlugin.lifecycleKeeper!.getCurrent(
        channel: channel,
        settings: settings
    )
}

@_cdecl("flutter_location_ffi_start_updates")
public func startUpdates(callback: @escaping ResultCallback) {
    FlutterLocationFfiPlugin.locationStreamManager!
        .startUpdates(resultChannel: ResultChannel(resultCallback: callback))
}

@_cdecl("flutter_location_ffi_stop_updates")
public func stopUpdates() {
    FlutterLocationFfiPlugin.locationStreamManager!.stopUpdates()
}

@_cdecl("flutter_location_ffi_open_app_settings")
public func openAppSettings() {
    FlutterLocationFfiPlugin.permissionManager!.openAppSettings()
}

@_cdecl("flutter_location_ffi_open_location_settings")
public func openLocationSettings() {
    FlutterLocationFfiPlugin.permissionManager!.openAppSettings()
}
