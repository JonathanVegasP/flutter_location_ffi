package dev.jonathanvegasp.flutter_location_ffi

import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import dev.jonathanvegasp.result_channel.ResultChannel
import java.util.Calendar

class FusedLocationCallback(
    private val settings: AndroidLocationSettings,
    private val channel: ResultChannel,
    private val statusChecker: StatusChecker,
    private val locationProviderClient: FusedLocationProviderClient,
    private val nmeaManager: NmeaManager
) : LocationCallback(), NmeaDataReceiver {
    private var location: Location? = null
    private var receivedData: Boolean = false

    override fun onLocationResult(p0: LocationResult) {
        val location = p0.lastLocation ?: return

        if (settings.priority.isHighPriority && !receivedData) {
            this.location = location

            return
        }

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
        if (statusChecker.isEnabled()) return

        locationProviderClient.removeLocationUpdates(this)

        channel.success(LocationDataFactory.create())
    }

    override fun onDataReceived(nmea: String, calendar: Calendar) {
        receivedData = true
        val location = location ?: return
        val accuracy = settings.validate(location) ?: return
        val nmeaManager = nmeaManager

        locationProviderClient.removeLocationUpdates(this)
        nmeaManager.onDataReceived(nmea, calendar)
        nmeaManager.setAltitudeMsl(location)

        channel.success(
            LocationDataFactory.create(
                accuracy,
                location
            )
        )
    }
}
