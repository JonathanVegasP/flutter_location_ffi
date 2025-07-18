package dev.jonathanvegasp.flutter_location_ffi

import android.location.LocationManager
import androidx.core.location.LocationManagerCompat

class LocationStatusChecker(private val locationManager: LocationManager) : StatusChecker {
    override fun isEnabled(): Boolean {
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }
}
