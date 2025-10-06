package dev.jonathanvegasp.flutter_location_ffi.provider

import com.google.android.gms.location.LocationRequest
import dev.jonathanvegasp.result_channel.ResultChannel

internal class SettingsLocationStatusCheckerDelegate(private val resultChannel: ResultChannel) :
    LocationStatusCheckerDelegate {
    override fun onProviderEnabled(locationRequest: LocationRequest) {
        resultChannel.success(true)
    }

    override fun onProviderDisabled() {
        resultChannel.success(false)
    }
}