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

internal class LocationListenerCallbackCompat(
    private val settings: AndroidLocationSettings,
    private val channel: ResultChannel,
    private val locationManager: LocationManager,
    private val nmeaManager: NmeaManager
) : LocationListenerCompat {

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onLocationChanged(location: Location) {
        val accuracy = settings.validate(location) ?: return

        LocationManagerCompat.removeUpdates(locationManager, this)

        nmeaManager.setAltitudeMsl(location)

        channel.success(
            LocationDataFactory.create(
                accuracy,
                location
            )
        )
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onProviderDisabled(provider: String) {
        LocationManagerCompat.removeUpdates(locationManager, this)

        channel.success(LocationDataFactory.create())
    }
}
