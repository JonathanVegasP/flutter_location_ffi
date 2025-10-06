package dev.jonathanvegasp.flutter_location_ffi.nmea

import android.Manifest
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission

@RequiresApi(Build.VERSION_CODES.N)
internal class NmeaEventManager(private val locationManager: LocationManager) : NmeaManager() {
    private var nmeaListener: OnNmeaMessageListener? = null

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun startUpdates() {
        nmeaListener?.also {
            locationManager.removeNmeaListener(it)
        }

        val listener = NmeaStreamListener(this)

        locationManager.addNmeaListener(listener, Handler(Looper.myLooper()!!))

        nmeaListener = listener
    }

    override fun stopUpdates() {
        nmeaListener?.also {
            locationManager.removeNmeaListener(it)
            nmeaListener = null
        }
    }
}
