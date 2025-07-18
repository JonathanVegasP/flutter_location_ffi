package dev.jonathanvegasp.flutter_location_ffi

import dev.jonathanvegasp.result_channel.ResultChannel
import io.flutter.plugin.common.PluginRegistry

interface PermissionManager : PluginRegistry.RequestPermissionsResultListener, Destroyable {
    companion object {
        const val LOCATION_REQUEST_PERMISSION = 1001
    }

    fun checkAndRequestPermission(channel: ResultChannel)

    fun checkPermission(): LocationPermission

    fun openAppSettings()

    fun openPermissionSettings()
}
