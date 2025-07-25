package dev.jonathanvegasp.flutter_location_ffi

import android.location.Location
import com.google.android.gms.location.Granularity
import dev.jonathanvegasp.result_channel.toLong

class AndroidLocationSettings private constructor(
    val priority: AndroidLocationPriorityStrategy,
    val intervalMs: Long,
    val accuracyFilter: Float,
    val granularity: Int,
    val waitForAccurateLocation: Boolean,
    val durationMs: Long,
    val minUpdateDistanceMeters: Float,
    val minUpdateIntervalMs: Long,
    val maxUpdateDelayMs: Long,
    val maxUpdateAgeMillis: Long,
    val maxUpdates: Int,
) {

    fun validate(location: Location): Float? {
        if (!location.hasAccuracy()) return 0.0F
        val accuracy = location.accuracy
        return when {
            accuracy > accuracyFilter -> null
            else -> accuracy
        }
    }

    companion object {
        fun default(priority: AndroidLocationPriorityStrategy): AndroidLocationSettings {
            val intervalMs = 10000L

            val accuracyFilter = 200.0F

            val granularity = Granularity.GRANULARITY_PERMISSION_LEVEL

            val waitForAccurateLocation = true

            val durationMs = Long.MAX_VALUE

            val minUpdateDistanceMeters = 0.0F

            val maxUpdateDelayMs = 0L

            val maxUpdateAgeMillis = 0L

            val maxUpdates = Int.MAX_VALUE

            return AndroidLocationSettings(
                priority,
                intervalMs,
                accuracyFilter,
                granularity,
                waitForAccurateLocation,
                durationMs,
                minUpdateDistanceMeters,
                intervalMs,
                maxUpdateDelayMs,
                maxUpdateAgeMillis,
                maxUpdates
            )
        }

        fun create(
            data: List<Any?>
        ): AndroidLocationSettings {
            val priority = when {
                LocationStrategyFactory.isGooglePlayAvailable -> {
                    AndroidLocationPriority.entries[data[0] as Int]
                }

                else -> {
                    AndroidLegacyLocationPriority.entries[data[0] as Int]
                }
            }

            val intervalMs = data[1].toLong()

            val accuracyFilter = (data[2] as Double).toFloat()

            val granularity = data[3] as Int

            val waitForAccurateLocation = data[4] as Boolean

            var durationMs = data[5].toLong()

            durationMs = when {
                durationMs > 0 -> durationMs
                else -> Long.MAX_VALUE
            }

            val minUpdateDistanceMeters = (data[6] as Double).toFloat()

            val minUpdateIntervalMs = data[7].toLong()

            val maxUpdateDelayMs = data[8].toLong()

            val maxUpdateAgeMillis = data[9].toLong()

            var maxUpdates = data[10] as Int

            maxUpdates = when {
                maxUpdates > 0 -> maxUpdates
                else -> Int.MAX_VALUE
            }

            return AndroidLocationSettings(
                priority,
                intervalMs,
                accuracyFilter,
                granularity,
                waitForAccurateLocation,
                durationMs,
                minUpdateDistanceMeters,
                minUpdateIntervalMs,
                maxUpdateDelayMs,
                maxUpdateAgeMillis,
                maxUpdates
            )
        }
    }
}