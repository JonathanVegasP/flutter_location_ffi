package dev.jonathanvegasp.flutter_location_ffi

import android.location.Location
import android.os.Build
import java.util.Calendar
import java.util.TimeZone

object LocationDataFactory {
    private val UTC: TimeZone = TimeZone.getTimeZone("UTC")
    val altitudeMslKey = "altitude"
    private val isUpsideDownCake = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    private val isAndroidO = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    fun create(
        accuracy: Float,
        location: Location
    ): Array<Any?> {
        val isGpsEnabled = true

        val timestamp =
            when {
                location.time > 0L -> location.time
                else -> Calendar.getInstance(UTC).timeInMillis
            }

        val altitudeEllipsoid = when {
            location.hasAltitude() -> location.altitude
            else -> 0.0
        }

        var altitudeMSL = location.extras?.getDouble(altitudeMslKey) ?: 0.0

        if (isUpsideDownCake) {
            altitudeMSL = when {
                location.hasMslAltitude() -> location.mslAltitudeMeters
                else -> altitudeMSL
            }
        }

        var altitudeAccuracy = 0.0F

        var headingAccuracy = 0.0F

        var speedAccuracy = 0.0F

        if (isAndroidO) {
            altitudeAccuracy =
                when {
                    location.hasVerticalAccuracy() -> location.verticalAccuracyMeters
                    else -> 0.0F
                }

            headingAccuracy =
                when {
                    location.hasBearingAccuracy() -> location.bearingAccuracyDegrees
                    else -> 0.0F
                }

            speedAccuracy = when {
                location.hasSpeedAccuracy() -> location.speed
                else -> 0.0F
            }
        }

        val heading = when {
            location.hasBearing() -> location.bearing
            else -> 0.0F
        }

        val speed = when {
            location.hasSpeed() -> location.speed
            else -> 0.0F
        }

        return arrayOf(
            isGpsEnabled,
            location.latitude,
            location.longitude,
            accuracy,
            timestamp,
            altitudeEllipsoid,
            altitudeMSL,
            altitudeAccuracy,
            heading,
            headingAccuracy,
            speed,
            speedAccuracy,
            null
        )
    }

    fun create(): Array<Any?> {
        val isGpsEnabled = false
        val coordinates = 0.0
        val accuracy = 0.0F
        val timestamp = Calendar.getInstance(UTC).timeInMillis

        return arrayOf(
            isGpsEnabled,
            coordinates,
            coordinates,
            accuracy,
            timestamp,
            coordinates,
            coordinates,
            accuracy,
            accuracy,
            accuracy,
            accuracy,
            accuracy,
            null
        )
    }
}
