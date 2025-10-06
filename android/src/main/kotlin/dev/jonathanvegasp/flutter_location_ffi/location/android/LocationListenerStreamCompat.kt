package dev.jonathanvegasp.flutter_location_ffi.location.android

import android.Manifest
import android.location.Location
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import dev.jonathanvegasp.flutter_location_ffi.location.LocationDataFactory
import dev.jonathanvegasp.flutter_location_ffi.nmea.NmeaManager
import dev.jonathanvegasp.flutter_location_ffi.settings.AndroidLocationSettings
import dev.jonathanvegasp.result_channel.ResultChannel

internal class LocationListenerStreamCompat(
    private var settings: AndroidLocationSettings,
    private val channel: ResultChannel,
    private val locationManager: LocationManager,
    private val nmeaManager: NmeaManager
) : LocationListenerCompat {
    fun setSettings(settings: AndroidLocationSettings) {
        this.settings = settings
    }

    override fun onLocationChanged(location: Location) {
        val accuracy = settings.validate(location) ?: return

        nmeaManager.setAltitudeMsl(location)

        channel.success(
            LocationDataFactory.create(
                accuracy,
                location
            )
        )
    }

    override fun onProviderDisabled(provider: String) {
        channel.success(LocationDataFactory.create())
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun onDestroy() {
        LocationManagerCompat.removeUpdates(locationManager, this)

        channel.failure(null)
    }
}
