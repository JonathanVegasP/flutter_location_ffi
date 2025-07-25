package dev.jonathanvegasp.flutter_location_ffi

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import dev.jonathanvegasp.result_channel.ResultChannel

class FusedLocationStreamCallback(
    private var settings: AndroidLocationSettings,
    private val channel: ResultChannel,
    private val statusChecker: StatusChecker,
    private val locationProviderClient: FusedLocationProviderClient,
    private val nmeaManager: NmeaManager
) : LocationCallback(), Destroyable {

    fun setSettings(settings: AndroidLocationSettings) {
        this.settings = settings
    }

    override fun onLocationResult(p0: LocationResult) {
        val location = p0.lastLocation ?: return

        val accuracy = settings.validate(location) ?: return

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

        channel.success(LocationDataFactory.create())
    }

    override fun onDestroy() {
        locationProviderClient.removeLocationUpdates(this)

        channel.failure(null)
    }
}
