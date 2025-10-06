package dev.jonathanvegasp.flutter_location_ffi.nmea

import android.Manifest
import android.location.GpsStatus
import android.location.LocationManager
import androidx.annotation.RequiresPermission

@Suppress("DEPRECATION")
internal class LegacyNmeaManager(private val locationManager: LocationManager) : NmeaManager() {
    private var gnsStatusNmeaListener: GpsStatus.NmeaListener? = null


    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun startUpdates() {
        gnsStatusNmeaListener?.also {
            locationManager.removeNmeaListener(it)
        }

        val listener = LegacyNmeaStreamListener(this)

        locationManager.addNmeaListener(listener)

        gnsStatusNmeaListener = listener
    }

    override fun stopUpdates() {
        gnsStatusNmeaListener?.also {
            locationManager.removeNmeaListener(it)
            gnsStatusNmeaListener = null
        }
    }
}