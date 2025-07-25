package dev.jonathanvegasp.flutter_location_ffi

import android.Manifest
import android.location.Location
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import dev.jonathanvegasp.result_channel.ResultChannel
import java.util.Calendar

class LocationListenerCallbackCompat(
    private val settings: AndroidLocationSettings,
    private val channel: ResultChannel,
    private val locationManager: LocationManager,
    private val nmeaManager: NmeaManager
) : LocationListenerCompat, NmeaDataReceiver {
    private var location: Location? = null
    private var receivedNmea: Boolean = false

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onLocationChanged(location: Location) {
        if (settings.priority.isHighPriority && !receivedNmea) {
            this.location = location
            return
        }

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
    override fun onDataReceived(nmea: String, calendar: Calendar) {
        receivedNmea = true
        val location = location ?: return
        val accuracy = settings.validate(location) ?: return
        val nmeaManager = nmeaManager

        LocationManagerCompat.removeUpdates(locationManager, this)
        nmeaManager.onDataReceived(nmea, calendar)
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
