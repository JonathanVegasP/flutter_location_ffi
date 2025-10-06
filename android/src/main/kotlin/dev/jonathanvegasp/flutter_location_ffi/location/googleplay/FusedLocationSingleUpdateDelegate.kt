package dev.jonathanvegasp.flutter_location_ffi.location.googleplay

import android.Manifest
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationRequest
import dev.jonathanvegasp.flutter_location_ffi.provider.LocationStatusCheckerDelegate
import dev.jonathanvegasp.result_channel.ResultChannel

internal class FusedLocationSingleUpdateDelegate(
    private val resultChannel: ResultChannel,
    private val fusedLocationStrategy: FusedLocationStrategy
) : LocationStatusCheckerDelegate {
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onProviderEnabled(locationRequest: LocationRequest) {
        fusedLocationStrategy.getCurrentOnProviderEnabled(resultChannel, locationRequest)
    }

    override fun onProviderDisabled() {
        fusedLocationStrategy.onProviderDisabled(resultChannel)
    }
}
