package dev.jonathanvegasp.flutter_location_ffi.nmea

import android.location.OnNmeaMessageListener
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
internal class NmeaStreamListener(private val nmeaDataReceiver: NmeaManager) : OnNmeaMessageListener {
    override fun onNmeaMessage(message: String?, timestamp: Long) {
        nmeaDataReceiver.onDataReceived(message)
    }
}