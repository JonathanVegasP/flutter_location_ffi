package dev.jonathanvegasp.flutter_location_ffi.nmea

import android.location.GpsStatus

@Suppress("DEPRECATION")
internal class LegacyNmeaStreamListener(private val nmeaDataReceiver: LegacyNmeaManager) :
    GpsStatus.NmeaListener {
    override fun onNmeaReceived(timestamp: Long, nmea: String?) {
        nmeaDataReceiver.onDataReceived(nmea)
    }
}
