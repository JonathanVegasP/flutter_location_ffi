package dev.jonathanvegasp.flutter_location_ffi

import java.util.Calendar

interface NmeaDataReceiver {
    fun onDataReceived(nmea: String, calendar: Calendar)
}