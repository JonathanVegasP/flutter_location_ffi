package dev.jonathanvegasp.flutter_location_ffi

import android.location.GpsStatus
import android.util.Log
import java.util.Calendar

@Suppress("DEPRECATION")
class LegacyNmeaStreamListener(private val nmeaDataReceiver: NmeaDataReceiver) :
    GpsStatus.NmeaListener {
    override fun onNmeaReceived(timestamp: Long, nmea: String?) {
        nmea?.also {
            val isGGA = it[0] == '$' && it[3] == 'G' && it[4] == 'G' && it[5] == 'A'

            if (!isGGA) return

            nmeaDataReceiver.onDataReceived(it, Calendar.getInstance())
        }
    }
}
