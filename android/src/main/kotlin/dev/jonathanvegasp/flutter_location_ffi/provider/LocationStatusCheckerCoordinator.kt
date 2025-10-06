package dev.jonathanvegasp.flutter_location_ffi.provider

import android.content.Intent
import io.flutter.plugin.common.PluginRegistry

internal class LocationStatusCheckerCoordinator : PluginRegistry.ActivityResultListener {
    private val checkers = mutableListOf<LocationStatusChecker>()

    fun append(locationStatusChecker: LocationStatusChecker) {
        checkers.add(locationStatusChecker)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        var result = true

        for (item in checkers) {
            val itemResult = item.onActivityResult(requestCode, resultCode, data)

            if (!itemResult) {
                result = false
            }
        }

        checkers.clear()

        return result
    }
}