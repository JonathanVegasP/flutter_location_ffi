package dev.jonathanvegasp.flutter_location_ffi

import android.location.GpsStatus
import android.location.LocationManager
import java.util.Calendar

@Suppress("DEPRECATION")
class LegacyNmeaSingleListener(
    private val nmeaDataReceiver: NmeaDataReceiver,
    private val locationManager: LocationManager
) : GpsStatus.NmeaListener {
    override fun onNmeaReceived(timestamp: Long, nmea: String?) {
        nmea?.also {
            val isGGA = it[0] == '$' && it[3] == 'G' && it[4] == 'G' && it[5] == 'A'

            if (!isGGA) return

            locationManager.removeNmeaListener(this)
            nmeaDataReceiver.onDataReceived(it, Calendar.getInstance())
        }
    }
}