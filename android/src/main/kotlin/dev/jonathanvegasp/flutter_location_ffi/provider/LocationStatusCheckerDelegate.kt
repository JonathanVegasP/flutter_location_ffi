package dev.jonathanvegasp.flutter_location_ffi.provider

import com.google.android.gms.location.LocationRequest

internal interface LocationStatusCheckerDelegate {
    fun onProviderEnabled(locationRequest: LocationRequest)
    fun onProviderDisabled()
}
