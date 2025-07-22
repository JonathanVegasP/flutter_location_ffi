package dev.jonathanvegasp.flutter_location_ffi

import android.Manifest
import android.location.Location
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import dev.jonathanvegasp.result_channel.ResultChannel

class LocationListenerStreamCompat(
    private var settings: AndroidLocationSettings,
    private val channel: ResultChannel,
    private val locationManager: LocationManager
) : LocationListenerCompat, Destroyable {
    fun setSettings(settings: AndroidLocationSettings) {
        this.settings = settings
    }

    override fun onLocationChanged(location: Location) {
        val accuracy = if (location.hasAccuracy()) location.accuracy else 0.0F

        if (accuracy > settings.accuracyFilter) return

        channel.success(
            LocationDataFactory.create(
                true,
                location.latitude,
                location.longitude,
                accuracy
            )
        )
    }

    override fun onProviderDisabled(provider: String) {
        channel.success(LocationDataFactory.create())
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onDestroy() {
        LocationManagerCompat.removeUpdates(locationManager, this)

        channel.failure(null)
    }
}
