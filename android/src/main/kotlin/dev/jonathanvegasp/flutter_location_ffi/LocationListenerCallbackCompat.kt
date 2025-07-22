package dev.jonathanvegasp.flutter_location_ffi

import android.Manifest
import android.location.Location
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import dev.jonathanvegasp.result_channel.ResultChannel

class LocationListenerCallbackCompat(
    private val settings: AndroidLocationSettings,
    private val channel: ResultChannel,
    private val locationManager: LocationManager
) : LocationListenerCompat {
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onLocationChanged(location: Location) {
        val accuracy = if (location.hasAccuracy()) location.accuracy else 0.0F

        if (accuracy > 50.0F) return

        LocationManagerCompat.removeUpdates(locationManager, this)

        channel.success(
            LocationDataFactory.create(
                true,
                location.latitude,
                location.longitude,
                accuracy
            )
        )
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onProviderDisabled(provider: String) {
        LocationManagerCompat.removeUpdates(locationManager, this)

        channel.success(LocationDataFactory.create())
    }
}
