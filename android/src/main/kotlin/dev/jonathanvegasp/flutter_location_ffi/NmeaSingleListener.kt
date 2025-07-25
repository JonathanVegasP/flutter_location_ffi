package dev.jonathanvegasp.flutter_location_ffi

import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.N)
class NmeaSingleListener(
    private val nmeaDataReceiver: NmeaDataReceiver,
    private val locationManager: LocationManager
) : OnNmeaMessageListener {
    override fun onNmeaMessage(message: String?, timestamp: Long) {
        message?.also {
            val isGGA = it[0] == '$' && it[3] == 'G' && it[4] == 'G' && it[5] == 'A'

            if (!isGGA) return

            locationManager.removeNmeaListener(this)
            nmeaDataReceiver.onDataReceived(it, Calendar.getInstance())
        }
    }
}
