package dev.jonathanvegasp.flutter_location_ffi.location.googleplay

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import dev.jonathanvegasp.flutter_location_ffi.location.LocationDataFactory
import dev.jonathanvegasp.flutter_location_ffi.nmea.NmeaManager
import dev.jonathanvegasp.flutter_location_ffi.settings.AndroidLocationSettings
import dev.jonathanvegasp.result_channel.ResultChannel

internal class FusedLocationCallback(
    private val settings: AndroidLocationSettings,
    private val channel: ResultChannel,
    private val locationProviderClient: FusedLocationProviderClient,
    private val nmeaManager: NmeaManager
) : LocationCallback() {

    override fun onLocationResult(p0: LocationResult) {
        val location = p0.lastLocation ?: return

        val accuracy = settings.validate(location) ?: return

        locationProviderClient.removeLocationUpdates(this)

        nmeaManager.setAltitudeMsl(location)

        channel.success(
            LocationDataFactory.create(
                accuracy,
                location
            )
        )
    }

    override fun onLocationAvailability(p0: LocationAvailability) {
        if (p0.isLocationAvailable) return

        locationProviderClient.removeLocationUpdates(this)

        channel.success(LocationDataFactory.create())
    }
}
