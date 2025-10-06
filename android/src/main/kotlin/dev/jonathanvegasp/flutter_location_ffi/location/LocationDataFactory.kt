package dev.jonathanvegasp.flutter_location_ffi.location

import android.location.Location
import android.os.Build
import java.time.ZoneOffset
import java.util.Calendar
import java.util.TimeZone

internal object LocationDataFactory {
    private val UTC: TimeZone = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        TimeZone.getTimeZone(ZoneOffset.UTC)
    } else {
        TimeZone.getTimeZone("UTC")
    }
    const val ALTITUDE_MSL_KEY = "altitude"

    @JvmStatic
    fun create(
        accuracy: Float,
        location: Location
    ): Array<Any?> {
        var altitudeAccuracy = 0.0F

        var headingAccuracy = 0.0F

        var speedAccuracy = 0.0F

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
                location.hasSpeedAccuracy() -> location.speedAccuracyMetersPerSecond
                else -> 0.0F
            }
        }

        return arrayOf(
            true,
            location.latitude,
            location.longitude,
            accuracy,
            when {
                location.time > 0L -> location.time
                else -> Calendar.getInstance(UTC).timeInMillis
            },
            when {
                location.hasAltitude() -> location.altitude
                else -> 0.0
            },
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                    when {
                        location.hasMslAltitude() -> location.mslAltitudeMeters
                        else -> location.extras!!.getDouble(ALTITUDE_MSL_KEY, 0.0)
                    }
                }
                else -> {
                    location.extras!!.getDouble(ALTITUDE_MSL_KEY, 0.0)
                }
            },
            altitudeAccuracy,
            when {
                location.hasBearing() -> location.bearing
                else -> 0.0F
            },
            headingAccuracy,
            when {
                location.hasSpeed() -> location.speed
                else -> 0.0F
            },
            speedAccuracy,
            null
        )
    }

    @JvmStatic
    fun create(): Array<Any?> {
        val coordinates = 0.0
        val accuracy = 0.0F

        return arrayOf(
            false,
            coordinates,
            coordinates,
            accuracy,
            Calendar.getInstance(UTC).timeInMillis,
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
