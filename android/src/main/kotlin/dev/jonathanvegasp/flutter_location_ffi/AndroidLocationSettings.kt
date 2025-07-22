package dev.jonathanvegasp.flutter_location_ffi

import com.google.android.gms.location.Granularity
import dev.jonathanvegasp.result_channel.toLong

class AndroidLocationSettings private constructor(
    val priority: AndroidLocationPriorityStrategy,
    val intervalMs: Long,
    val granularity: Int,
    val waitForAccurateLocation: Boolean,
    val durationMs: Long,
    val minUpdateDistanceMeters: Float,
    val minUpdateIntervalMs: Long,
    val maxUpdateDelayMs: Long,
    val maxUpdateAgeMillis: Long,
    val maxUpdates: Int,
) {
    companion object {
        fun default(priority: AndroidLocationPriorityStrategy): AndroidLocationSettings {
            val intervalMs = 10000L

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
            val priority = if (LocationStrategyFactory.isGooglePlayAvailable) {
                AndroidLocationPriority.entries[data[0] as Int]
            } else {
                AndroidLegacyLocationPriority.entries[data[0] as Int]
            }

            val intervalMs = data[1].toLong()

            val granularity = data[2] as Int

            val waitForAccurateLocation = data[3] as Boolean

            var durationMs = data[4].toLong()

            durationMs = if (durationMs == -1L) Long.MAX_VALUE else durationMs

            val minUpdateDistanceMeters = (data[5] as Double).toFloat()

            val minUpdateIntervalMs = data[6].toLong()

            val maxUpdateDelayMs = data[7].toLong()

            val maxUpdateAgeMillis = data[8].toLong()

            val maxUpdates = data[9] as Int

            return AndroidLocationSettings(
                priority,
                intervalMs,
                granularity,
                waitForAccurateLocation,
                durationMs,
                minUpdateDistanceMeters,
                minUpdateIntervalMs,
                maxUpdateDelayMs,
                maxUpdateAgeMillis,
                if (maxUpdates == -1) Int.MAX_VALUE else maxUpdates
            )
        }
    }
}